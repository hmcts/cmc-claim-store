  #!/usr/bin/env bash

set -e

if [ -z "$CLAIM_STORE_DB_USERNAME" ] || [ -z "$CLAIM_STORE_DB_PASSWORD" ]; then
  echo "ERROR: Missing environment variable. Set value for both 'CLAIM_STORE_DB_USERNAME' and 'CLAIM_STORE_DB_PASSWORD'."
  exit 1
fi

# Create role and database
psql -v ON_ERROR_STOP=1 --username postgres --set USERNAME=$CLAIM_STORE_DB_USERNAME --set PASSWORD=$CLAIM_STORE_DB_PASSWORD <<-EOSQL
  CREATE USER :USERNAME WITH PASSWORD ':PASSWORD';

  CREATE DATABASE claimstore
    WITH OWNER = :USERNAME
    ENCODING = 'UTF-8'
    CONNECTION LIMIT = -1;
EOSQL
