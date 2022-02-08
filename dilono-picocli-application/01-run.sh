#!/usr/bin/env bash
set -e
###
set -e
if [[ -z "${DILONO_SERVER_URL}" ]]; then
  echo 'Please set environment variable DILONO_SERVER_URL and run this script again.'
  exit -1
fi
if [[ -z "${DILONO_SERVER_TOKEN_ID}" ]]; then
  echo 'Please set environment variable DILONO_SERVER_TOKEN_ID and run this script again.'
  exit -1
fi
if [[ -z "${DILONO_SERVER_TOKEN_SECRET}" ]]; then
  echo 'Please set environment variable DILONO_SERVER_TOKEN_SECRET and run this script again.'
  exit -1
fi
###
../mvnw clean package
echo
echo Starting CLI application...
echo
java -jar target/dilono-picocli-application-fat.jar \
  --d96a-orders-to-json \
  -i $(pwd)/../dilono-basic-sample/src/test/resources/fixtures/EdifactOrdersReaderTest/d96a-orders.edi \
  -o $(pwd)/target/orders.json
echo
echo
cat $(pwd)/target/orders.json
