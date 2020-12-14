#!/bin/bash -e
# set up staging db
psql -U postgres -h localhost -d staging -f ./brmo-persistence/db/create-brmo-persistence-postgresql.sql
# set up rsgb
psql -U postgres -h localhost -w -q -d rsgb -f ./datamodel/generated_scripts/datamodel_postgresql.sql
# set up rsgbbgt
psql -U postgres -h localhost -w -q -d rsgbbgt -f ./bgt-gml-loader/target/generated-resources/ddl/postgresql/create_rsgb_bgt.sql
# set up topnl
psql -U postgres -h localhost -w -q -d topnl -f ./topparser/src/main/resources/nl/b3p/topnl/database/postgres.sql

