#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER rsgb    with password '$DB_PASS_RSGB';
    CREATE USER staging with password '$DB_PASS_STAGING';
    CREATE USER rsgbbgt with password '$DB_PASS_RSGBBGT';
    CREATE DATABASE staging owner staging;
    CREATE DATABASE rsgb    owner rsgb;
    CREATE DATABASE rsgbbgt owner rsgbbgt;
EOSQL

for DB in rsgb rsgbbgt; do
  echo "Loading PostGIS extensions into $DB"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$DB" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS postgis;
EOSQL
done