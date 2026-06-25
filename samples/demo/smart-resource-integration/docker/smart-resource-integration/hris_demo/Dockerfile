### Ubuntu
FROM python:3.12-slim
# Alpine
###FROM python:3.12-alpine
# TODO update labels!!!
LABEL vendor="evolveum"

WORKDIR /src/

# Keeps Python from generating .pyc files in the container
ENV PYTHONDONTWRITEBYTECODE=1

# Turns off buffering for easier container logging
ENV PYTHONUNBUFFERED=1

# install dependencies
### Ubuntu
RUN apt-get update -y && apt-get install -y sqlite3 procps curl
### Alpine
###RUN apk add --no-cache sqlite bash procps curl

# Keep installed pip version
##############################RUN pip install --no-cache --upgrade pip==25.0.1

RUN groupadd -r -g 1000 hris && useradd -u 1000 -g hris -d /home/hris -m -s /sbin/nologin hris
RUN mkdir /var/opt/hris && chown -R hris:hris /var/opt/hris

COPY ./requirements.txt .
RUN pip install --no-cache -r requirements.txt

# copy application to WORKDIR
COPY hris.py .

# define volume
VOLUME /var/opt/hris

COPY docker-entrypoint.sh /docker-entrypoint.sh
RUN chmod +x /docker-entrypoint.sh

# Switch the user
USER hris
# entrypoint.sh makes sure proper environmental variables are set before starting Gunicorn
ENTRYPOINT ["/docker-entrypoint.sh"]
# TODO the port is hard-coded here
CMD ["gunicorn", "--bind", "0.0.0.0:8000", "--workers", "4", "--timeout", "60", "hris:app"]

EXPOSE 8000