#!/usr/bin/env bash

set -eu
set -o pipefail

JVERSION="$(java --version 2> /dev/null | head -1 | cut -d' ' -f2)" || {
  echo "Seems this Java doesn't know --version option, use Java 11 or newer."
  exit 1
}
[[ "$JVERSION" < "11" ]] &&
  echo "Java version should be 11 or newer, is \"$JVERSION\", but we'll try anyway"

./mvnw -f samples-test clean package
