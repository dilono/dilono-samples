#!/usr/bin/env bash
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
../mvnw clean package
../mvnw azure-functions:run
