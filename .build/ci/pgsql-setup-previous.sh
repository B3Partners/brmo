#!/usr/bin/env bash
set -e
# set up staging db
psql -U postgres -h localhost -d staging -f ./old/db/staging/create-brmo-persistence-postgresql.sql
# set up rsgb tabellen
psql -U postgres -h localhost -w -q -d rsgb -f ./old/db/rsgb/datamodel_postgresql.sql

psql -U postgres -h localhost -w -q -d rsgb -f ./old/db/brk/brk2.0_postgresql.sql
PGOPTIONS="--search_path=brk" psql -U postgres -h localhost -w -q -d rsgb -f ./old/db/brk/brk2.0_commentaar.sql
PGOPTIONS="--search_path=brk" psql -U postgres -h localhost -w -q -d rsgb -f ./old/db/brk/brk2.0_postgresql_views.sql