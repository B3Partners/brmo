#!/bin/bash -e
# set up staging db
psql -U postgres -h localhost -d staging -f ./old/db/staging/create-brmo-persistence-postgresql.sql
# set up rsgb tabellen
psql -U postgres -h localhost -w -q -d rsgb -f ./old/db/rsgb/datamodel_postgresql.sql
# set up rsgbbgt tabellen
# verwijderd in 2.1.0
# psql -U postgres -h localhost -w -q -d rsgbbgt -f ./old/db/rsgbbgt/postgresql/create_rsgb_bgt.sql
# set up topnl tabellen
psql -U postgres -h localhost -w -q -d topnl -f ./old/db/topnl/postgres.sql
