#!/usr/bin/env bash

# Variables used by midpoint-samples release/next scripts since test.sh needs existing postgresql database
# to execute test suite. Database has to have correct schema (tables, sequences, etc.) already present.
JDBC_URL=
JDBC_USERNAME=
JDBC_PASSWORD=

if [ -z "$POSTGRES_JDBC_URL" ]; then
  export POSTGRES_JDBC_URL=$JDBC_URL
fi

if [ -z "$POSTGRES_JDBC_USERNAME" ]; then
  export POSTGRES_JDBC_USERNAME=$JDBC_USERNAME
fi

if [ -z "$POSTGRES_JDBC_PASSWORD" ]; then
  export POSTGRES_JDBC_PASSWORD=$JDBC_PASSWORD
fi
