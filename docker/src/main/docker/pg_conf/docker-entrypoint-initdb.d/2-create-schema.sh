#!/usr/bin/env bash
set -e

echo "Initializing schemas"
PGPASSWORD="$BRMO_PASSWORD" psql -v ON_ERROR_STOP=1 --username brmo --dbname staging -f /opt/brmo-scripts/create-brmo-persistence-postgresql.sql
PGPASSWORD="$BRMO_PASSWORD" psql -v ON_ERROR_STOP=1 --username brmo --dbname rsgb    -f /opt/brmo-scripts/datamodel_postgresql.sql
PGPASSWORD="$BRMO_PASSWORD" psql -v ON_ERROR_STOP=1 --username brmo --dbname topnl   -f /opt/brmo-scripts/postgres.sql
