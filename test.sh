#!/usr/bin/env bash

set -eu
set -o pipefail

JVERSION="$(java --version 2>/dev/null | head -1 | cut -d' ' -f2 | cut -d'.' -f1)" || {
  echo "Seems this Java doesn't know --version option, use Java 11 or newer."
  exit 1
}
[[ "$JVERSION" -lt "11" ]] &&
  echo "Java version should be 11 or newer, is \"$JVERSION\", but we'll try anyway"

. ./test-setenv.sh

if [ -z "$POSTGRES_JDBC_URL" ]; then
  echo "Please set POSTGRES_JDBC_URL environment variable in test-setenv.sh"
  exit 1
fi

if [ -z "$POSTGRES_JDBC_USERNAME" ]; then
  echo "Please set POSTGRES_JDBC_USERNAME environment variable in test-setenv.sh"
  exit 1
fi

if [ -z "$POSTGRES_JDBC_PASSWORD" ]; then
  echo "Please set POSTGRES_JDBC_PASSWORD environment variable in test-setenv.sh"
  exit 1
fi

./mvnw clean install
./mvnw -f samples-test clean package \
    -Duser.language=en \
    -Dmidpoint.repository.jdbcUrl=$POSTGRES_JDBC_URL \
    -Dmidpoint.repository.jdbcUsername=$POSTGRES_JDBC_USERNAME \
    -Dmidpoint.repository.jdbcPassword=$POSTGRES_JDBC_PASSWORD \
    -Dtest.config.file=test-config-new-repo.xml