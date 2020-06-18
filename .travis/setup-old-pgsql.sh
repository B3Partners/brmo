#!/bin/bash -e
# set up staging db
psql -U postgres -d staging -f ./old/db/staging/create-brmo-persistence-postgresql.sql
# TODO als 2.0.2 release is deze regels opruimen
psql -U postgres -d staging -f ./old/db/staging/01_create_indexes.sql
psql -U postgres -d staging -f ./old/db/staging/02_insert_default_user.sql
psql -U postgres -d staging -f ./old/db/staging/05_create_brmo_metadata_postgresql.sql
# set up rsgb tabellen
psql -U postgres -w -q -d rsgb -f ./old/db/rsgb/datamodel_postgresql.sql
# set up rsgbbgt tabellen
psql -U postgres -w -q -d rsgbbgt -f ./old/db/rsgbbgt/postgresql/create_rsgb_bgt.sql
# set up topnl tabellen
psql -U postgres -w -q -d topnl -f ./old/db/topnl/postgres.sql
