FROM postgres:18.1-alpine

# Add a new user "user_pantheon_ac" with user id 8877
RUN useradd -u 8877 civil_user

# Change to non-root privilege
USER civil_user

COPY init-db.sh /docker-entrypoint-initdb.d

HEALTHCHECK --interval=10s --timeout=10s --retries=10 CMD psql -c 'select 1' -d cmc -U $CMC_DB_USERNAME

EXPOSE 5432
