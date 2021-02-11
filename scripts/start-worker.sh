#!/bin/bash

SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"

cd $SCRIPT_DIR/../line-queue-worker/build/libs
JAR_FILE=`find . -name \*uber-jar\* -printf "%f\n"`

echo "executing $JAR_FILE"

java -jar $JAR_FILE "$@"