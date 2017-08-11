#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER foreman WITH PASSWORD 'foreman';
    CREATE DATABASE foreman;
    GRANT ALL PRIVILEGES ON DATABASE foreman TO foreman;
EOSQL

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" <<-EOSQL
    CREATE USER puppetdb WITH PASSWORD 'puppetdb';
    CREATE DATABASE puppetdb;
    GRANT ALL PRIVILEGES ON DATABASE puppetdb TO puppetdb;
EOSQL
