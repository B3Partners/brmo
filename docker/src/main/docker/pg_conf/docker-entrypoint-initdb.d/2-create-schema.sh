#!/usr/bin/env bash
set -e

echo "Initializing schemas"
PGPASSWORD=${DB_PASS_STAGING} psql -v ON_ERROR_STOP=1 --username staging --dbname staging -f /opt/brmo-scripts/create-brmo-persistence-postgresql.sql
PGPASSWORD=${DB_PASS_RSGB}    psql -v ON_ERROR_STOP=1 --username rsgb    --dbname rsgb    -f /opt/brmo-scripts/datamodel_postgresql.sql
PGPASSWORD=${DB_PASS_TOPNL}   psql -v ON_ERROR_STOP=1 --username topnl   --dbname topnl   -f /opt/brmo-scripts/postgres.sql
PGPASSWORD=${DB_PASS_RSGB}    psql -v ON_ERROR_STOP=1 --username rsgb    --dbname rsgb    -f /opt/brmo-scripts/brk2.0_postgresql.sql
PGPASSWORD=${DB_PASS_RSGB}    psql "dbname=rsgb options=--search_path=brk" -v ON_ERROR_STOP=1 --username rsgb -f /opt/brmo-scripts/brk2.0_commentaar.sql
