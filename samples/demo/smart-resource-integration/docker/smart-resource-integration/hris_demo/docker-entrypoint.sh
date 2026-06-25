#!/bin/bash

# Validate required environment variables
MISSING_VARS=""

for var in FLASK_APP DATABASE INITIAL_CSV_FILE EXPORT_CSV_FILE; do
  # Indirect variable reference; needs Bash
  if [ -z "${!var}" ]; then
    MISSING_VARS="$MISSING_VARS $var"
  fi
done

if [ -n "$MISSING_VARS" ]; then
  echo "Error: The following environment variables are not set: $MISSING_VARS" >&2
  exit 1
fi

# If all variables are set, proceed
#echo "DEBUG entrypoint.sh: env. variables are OK."
#echo "DEBUG entrypoint.sh: current pwd: `pwd`"
#echo "DEBUG content of current directory: current pwd: `ls -la`"
[ ! -f "/var/tmp/export.csv" ] && cp /src/export-initial.csv /var/tmp/export.csv && echo "INITIAL_CSV_FILE_COPIED" || echo "CSV_EXPORT_ALREADY_INITIALIZED";

# Run Gunicorn (passed as CMD)
exec "$@"