#!/bin/bash
set -e

psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    CREATE USER rsgb    with password '$DB_PASS_RSGB';
    CREATE USER staging with password '$DB_PASS_STAGING';
    CREATE USER topnl   with password '$DB_PASS_TOPNL';
    CREATE USER rsgbbgt with password '$DB_PASS_RSGBBGT';
    CREATE DATABASE staging owner staging;
    CREATE DATABASE rsgb    owner rsgb;
    CREATE DATABASE topnl   owner topnl;
    CREATE DATABASE rsgbbgt owner rsgbbgt;
EOSQL

for DB in rsgb rsgbbgt topnl; do
  echo "Loading PostGIS extensions into $DB"
  psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$DB" <<-EOSQL
    CREATE EXTENSION IF NOT EXISTS postgis;
EOSQL
done