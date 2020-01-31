#!/usr/bin/env bash

set -e

if [ -z "$CMC_DB_USERNAME" ] || [ -z "$CMC_DB_PASSWORD" ]; then
  echo "ERROR: Missing environment variable. Set value for both '$CMC_DB_USERNAME' and '$CMC_DB_PASSWORD'."
  exit 1
fi

# Create role and database
psql -v ON_ERROR_STOP=1 --username postgres --set USERNAME=$CMC_DB_USERNAME --set PASSWORD=$CMC_DB_PASSWORD <<-EOSQL
  CREATE USER :USERNAME WITH PASSWORD ':PASSWORD';

  CREATE DATABASE cmc
    WITH OWNER = :USERNAME
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;
EOSQL
