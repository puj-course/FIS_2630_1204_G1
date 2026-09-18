#!/usr/bin/env bash
set -euo pipefail

: "${DB_URL:?Debe definir DB_URL}"
: "${DB_USER:?Debe definir DB_USER}"
: "${DB_PASSWORD:?Debe definir DB_PASSWORD}"
export CARESTOCK_USER_EMAIL="${CARESTOCK_USER_EMAIL:-admin@carestock.com}"

mvn javafx:run
