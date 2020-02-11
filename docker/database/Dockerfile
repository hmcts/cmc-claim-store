FROM postgres:9.6-alpine

COPY init-db.sh /docker-entrypoint-initdb.d

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD psql -c 'select 1' -d cmc -U $CMC_DB_USERNAME

EXPOSE 5432
