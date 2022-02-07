#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER brmo with password '$BRMO_PASSWORD';
    CREATE DATABASE staging owner brmo;
    CREATE DATABASE rsgb owner brmo;
    CREATE DATABASE topnl owner brmo;
    CREATE DATABASE rsgbbgt owner brmo;
EOSQL

for DB in rsgb rsgbbgt topnl; do
  echo "Loading PostGIS extensions into $DB"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$DB" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS postgis;
EOSQL
done