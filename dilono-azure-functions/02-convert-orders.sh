#!/usr/bin/env bash
set -e
curl --data-binary '@../dilono-basic-sample/src/test/resources/fixtures/EdifactOrdersReaderTest/d96a-orders.edi' \
    -H 'Content-Type: application/octet-stream' \
     http://localhost:7071/api/OrdersConverter 2>&1
