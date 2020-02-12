#!/usr/bin/env bash

source $(dirname $0)/env.sh

if [ "$VERSION" == "v2" ]
then
  echo "Using $VERSION"
  http GET $BASEURL/${VERSION}/schemas
else
  http GET $BASEURL/databases
fi
