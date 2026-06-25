from flask import Flask, render_template, request, redirect, url_for, send_file, flash, jsonify
import sqlite3
import csv
import os
from os import environ
from io import StringIO, BytesIO
import werkzeug.exceptions

###app = Flask(__name__)
# Fix static_url_path, otherwise it is /static without prefix which breaks reverse proxy redirects
app = Flask(__name__, static_url_path='/hris/static')
app.secret_key = 'demo_secret_key'

DATABASE = environ.get("DATABASE")
EXPORT_CSV_FILE = environ.get("EXPORT_CSV_FILE")
INITIAL_CSV_FILE = environ.get("INITIAL_CSV_FILE")

# Employee fields definition. Kind of "schema".
# TODO there is no "type" as we will load/export data from/to a CSV file - all fields considered Strings
# FYI "id" field is not present in this "schema"
# TODO do NOT remove "empnum", "firstname" and "surname", they are used in multiple functions
EMPLOYEE_FIELDS = [
    {
        "csv_name": "empnum",
        "display_name": "Employee Number",
        "allowed_values": None,
        "displayed": True,
        "required": True,
        "disableUpdate": True
    },
    {
        "csv_name": "firstname",
        "display_name": "First Name",
        "allowed_values": None,
        "displayed": True,
        "required": True,
        "disableUpdate": False
    },
    {
        "csv_name": "surname",
        "display_name": "Surname",
        "allowed_values": None,
        "displayed": True,
        "required": True,
        "disableUpdate": False
    },
    {
        "csv_name": "emptype",
        "display_name": "Employee Type",
        "allowed_values": ["FTE", "CONTRACTOR"],
        "value_styles": {
            "FTE": "emptype-fte",
            "CONTRACTOR": "emptype-contractor",
        },
        "displayed": True,
        "required": True,
        "disableUpdate": False
    },
    {
        "csv_name": "job",
        "display_name": "Job Title",
        "allowed_values": None,
        "displayed": True,
        "required": True,
        "disableUpdate": False
    },
    {
        "csv_name": "status",
        "display_name": "Status",
        "allowed_values": ["Active", "Long-term leave", "Former employee"],
        "value_styles": {
            "Active": "status-active",
            "Long-term leave": "status-long-term-leave",
            "Former employee": "status-former-employee",
    },
        "displayed": True,
        "required": True,
        "disableUpdate": False
    },
    {
        "csv_name": "locality",
        "display_name": "Locality",
        "allowed_values": None,
        "displayed": True,
        "required": True,
        "disableUpdate": False
    },
    {
        "csv_name": "country",
        "display_name": "Country",
        "allowed_values": None,
        "displayed": False, # TODO We can hide Country, won't be exported, but column may be in initial data and may be fetched by midPoint connector
        "required": False,
        "disableUpdate": False
    },
]

# Helper function to get database connection
def get_db():
    conn = sqlite3.connect(DATABASE)
    conn.row_factory = sqlite3.Row
    return conn

# Initialize database
# TODO this is hard-coded, does not respect the EMPLOYEE_FIELDS (yet)
def init_db():
    with app.app_context():
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute('''
                       CREATE TABLE IF NOT EXISTS employees (
                                                                id INTEGER PRIMARY KEY AUTOINCREMENT,
                                                                empnum TEXT,
                                                                firstname TEXT,
                                                                surname TEXT,
                                                                status TEXT,
                                                                emptype TEXT,
                                                                locality TEXT,
                                                                country TEXT,
                                                                job TEXT
                       )
                       ''')
        cursor.execute("SELECT COUNT(*) FROM employees")
        if cursor.fetchone()[0] == 0:
            load_db_from_csv() # Load database contents from CSV if empty
        conn.commit()
        conn.close()

# Load database content from CSV
# TODO field names are hard-coded, does not respect EMPLOYEE_FIELDS (yet)
# TODO decide what to do if the contents does not match (missing or extra fields)
def load_db_from_csv():
    conn = get_db()
    cursor = conn.cursor()

    if os.path.exists(INITIAL_CSV_FILE):
        with open(INITIAL_CSV_FILE, 'r') as f:
            csv_input = csv.DictReader(f, delimiter=",", quotechar='"', quoting=csv.QUOTE_ALL)
            for row in csv_input:
                cursor.execute('''
                               INSERT INTO employees
                                   (empnum, firstname, surname, status, emptype, locality, country, job)
                               VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                               ''', (row['empnum'], row['firstname'], row['surname'], row['status'],
                                     row['emptype'], row['locality'], row['country'], row['job']))
            conn.commit()
            conn.close()

# Initialize databaze upon application start (if needed)
init_db()

###########################################################
# Error handling
@app.errorhandler(404)
def handle_not_found_error(e):
    return render_template("error404.html"), 404

@app.errorhandler(500)
def handle_internal_server_error(e):
    return render_template("error500.html"), 500

# Testing the error page
# @app.route('/hris/test-error-page')
# def test_error_page():
#     return render_template("error500.html")

# End of error handling
###########################################################

# Route to list all employees
@app.route('/hris')
def index():
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute('SELECT * FROM employees')
    employees = cursor.fetchall()
    conn.close()
    return render_template('index.html', employees=employees, fields=EMPLOYEE_FIELDS)

# Route to add an employee
@app.route('/hris/add', methods=['GET', 'POST'])
def add():
    if request.method == 'POST':
        # Insert new employee into the database
        data = {field['csv_name']: request.form.get(field['csv_name']) for field in EMPLOYEE_FIELDS}
        # Check mandatory fields (server-side)
        mandatory_fields = [field['csv_name'] for field in EMPLOYEE_FIELDS if field.get('required', False)]
        for field_name in mandatory_fields:
            if not data.get(field_name):
                flash(f"{field_name} is required!", 'danger')
                return render_template(
                    'add.html',
                    fields=EMPLOYEE_FIELDS,
                    employee=data
                )

        conn = get_db()
        cursor = conn.cursor()
        # TODO empnum field hard-coded (for now)
        # Check uniqueness of empnum
        cursor.execute('SELECT id FROM employees WHERE empnum = ?', (data['empnum'],))
        if cursor.fetchone():  # If a row is returned, empnum already exists
            flash(f"Employee with number {data['empnum']} already exists!", 'danger')
            conn.close()
            return render_template('add.html', fields=EMPLOYEE_FIELDS, employee=data)

        # Add new employee
        # TODO field names are hard-coded, does not respect EMPLOYEE_FIELDS (yet)
        cursor.execute('''
                       INSERT INTO employees
                           (empnum, firstname, surname, status, emptype, locality, country, job)
                       VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                       ''', (data['empnum'], data['firstname'], data['surname'], data['status'],
                             data['emptype'], data['locality'], data['country'], data['job']))
        conn.commit()
        conn.close()
        # Flash a message with selected attributes
        # TODO limitation: this means these are always used; we do not yet check if they are displayed
        flash(
            f"Employee {data['firstname']} {data['surname']} "
            f"({data['empnum']}) added successfully!",
            'success'
        )
        return redirect(url_for('index'))
    else:
        # Display empty form
        return render_template('add.html', fields=EMPLOYEE_FIELDS)

# Route to edit an employee
@app.route('/hris/edit/<int:id>', methods=['GET', 'POST'])
def edit(id):
    conn = get_db()
    cursor = conn.cursor()
    if request.method == 'POST':
        # Update employee data
        data = {field['csv_name']: request.form.get(field['csv_name']) for field in EMPLOYEE_FIELDS}
        # Check mandatory fields (server-side)
        mandatory_fields = [field['csv_name'] for field in EMPLOYEE_FIELDS if field.get('required', False)]
        for field_name in mandatory_fields:
            if not data.get(field_name):
                flash(f"{field_name} is required!", 'error')
                return render_template(
                    'edit.html',
                    fields=EMPLOYEE_FIELDS,
                    employee=data
                )
        # TODO field names are hard-coded, does not respect EMPLOYEE_FIELDS (yet)
        cursor.execute('''
                       UPDATE employees SET
                                            empnum = ?,
                                            firstname = ?,
                                            surname = ?,
                                            status = ?,
                                            emptype = ?,
                                            locality = ?,
                                            country = ?,
                                            job = ?
                       WHERE id = ?
                       ''', (data['empnum'], data['firstname'], data['surname'], data['status'],
                             data['emptype'], data['locality'], data['country'], data['job'], id))
        conn.commit()
        conn.close()
        # Flash a message with selected attributes
        # TODO limitation: this means these are always used; we do not yet check if they are displayed
        flash(
            f"Employee {data['firstname']} {data['surname']} "
            f"({data['empnum']}) updated successfully!",
            'success'
        )
        return redirect(url_for('index'))
    else:
        cursor.execute('SELECT * FROM employees WHERE id = ?', (id,))
        employee = cursor.fetchone()
        conn.close()
        return render_template('edit.html', employee=employee, fields=EMPLOYEE_FIELDS)

# Route to confirm and delete an employee
@app.route('/hris/delete/<int:id>', methods=['GET', 'POST'])
def delete(id):
    if request.method == 'POST':
        conn = get_db()
        cursor = conn.cursor()
        # get information about the to-be-deleted employee for the flash message
        cursor.execute('SELECT * FROM employees WHERE id = ?', (id,))
        employee = cursor.fetchone()

        cursor.execute('DELETE FROM employees WHERE id = ?', (id,))
        conn.commit()
        conn.close()
        flash(
            f"Employee {employee['firstname']} {employee['surname']} "
            f"({employee['empnum']}) deleted successfully!",
            'success'
        )
        return redirect(url_for('index'))
    else:
        conn = get_db()
        cursor = conn.cursor()
        cursor.execute('SELECT * FROM employees WHERE id = ?', (id,))
        employee = cursor.fetchone()
        conn.close()
        return render_template('confirm_delete.html', employee=employee, fields=EMPLOYEE_FIELDS)

# Route to import CSV
# @app.route('/hris/import', methods=['GET', 'POST'])
# def import_csv():
#     if request.method == 'POST':
#         if 'file' not in request.files:
#             flash('No file uploaded', 'error')
#             return redirect(url_for('index'))
#         file = request.files['file']
#         if file.filename == '':
#             flash('No file selected', 'error')
#             return redirect(url_for('index'))
#         if file and file.filename.endswith('.csv'):
#             stream = StringIO(file.stream.read().decode("UTF8"), newline=None)
#             csv_input = csv.DictReader(stream)
#             # Map CSV headers to database fields
#             csv_field_names = [field['csv_name'] for field in EMPLOYEE_FIELDS]
#             for row in csv_input:
#                 # Only insert if all required fields are present
#                 if all(field in row for field in csv_field_names):
#                     conn = get_db()
#                     cursor = conn.cursor()
#                     cursor.execute('''
#                                    INSERT INTO employees
#                                        (empnum, firstname, surname, status, emptype, locality, country, job)
#                                    VALUES (?, ?, ?, ?, ?, ?, ?, ?)
#                                    ''', (row['empnum'], row['firstname'], row['surname'], row['status'],
#                                          row['emptype'], row['locality'], row['country'], row['job']))
#                     conn.commit()
#                     conn.close()
#             flash('CSV file imported successfully', 'success')
#             return redirect(url_for('index'))
#         else:
#             flash('Invalid file type. Please upload a CSV file.', 'error')
#             return redirect(url_for('index'))
#     return render_template('import.html')

# Route to export database to CSV
@app.route('/hris/export')
def export_csv():
    conn = get_db()
    cursor = conn.cursor()
    cursor.execute('SELECT * FROM employees')
    employees = cursor.fetchall()
    conn.close()

    # Write CSV data to file on the server
    with open(EXPORT_CSV_FILE, 'w', newline='', encoding='utf-8') as csvfile:
        csv_output = csv.writer(csvfile, delimiter=",", quotechar='"', quoting=csv.QUOTE_ALL, lineterminator='\n')
        # Write header
        csv_output.writerow([field['csv_name'] for field in EMPLOYEE_FIELDS if field.get('displayed', False)])
        # Write data
        for employee in employees:
            csv_output.writerow([employee[field['csv_name']] for field in EMPLOYEE_FIELDS if field.get('displayed', False)])

    flash(f"CSV export with {len(employees)} entries created!", 'success')
    return redirect(url_for('index'))


if __name__ == '__main__':
    app.run(debug=True)