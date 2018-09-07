#!/bin/bash -e
# set up staging db
psql -U postgres -d staging -f ./brmo-persistence/db/create-brmo-persistence-postgresql.sql
psql -U postgres -d staging -f ./brmo-persistence/db/01_create_indexes.sql
psql -U postgres -d staging -f ./brmo-persistence/db/02_insert_default_user.sql
psql -U postgres -d staging -f ./brmo-persistence/db/05_create_brmo_metadata_postgresql.sql
# set up rsgb tabellen
psql -U postgres -w -q -d rsgb -f ./datamodel/generated_scripts/datamodel_postgresql.sql
# set up rsgbbgt tabellen
psql -U postgres -w -q -d rsgbbgt -f ./bgt-gml-loader/target/generated-resources/ddl/postgresql/create_rsgb_bgt.sql
# set up topnl tabellen
psql -U postgres -w -q -d topnl -f ./topparser/src/main/resources/nl/b3p/topnl/database/postgres.sql
#
# geom van gemeente en wijk hebben we niet echt nodig, mogen evt. uit want duurt lang en veel log output
#- travis_wait psql -U postgres -w -d rsgb -f ./datamodel/utility_scripts/postgresql/111a_update_gemeente_geom.sql
#- travis_wait psql -U postgres -w -d rsgb -f ./datamodel/utility_scripts/postgresql/113a_update_wijk_geom.sql
