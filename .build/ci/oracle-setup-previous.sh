#!/usr/bin/env bash
set -e
export SQLPATH=./.build/ci
# we hebben meer dan de default 300 processen nodig
docker exec -i oracle_brmo sqlplus -l sys/oracle@//localhost:1521/XE as sysdba < ./.build/ci/oracle-system-setup.sql
docker exec -i oracle_brmo sqlplus -s /nolog<<EOF
conn sys/oracle as sysdba
startup
exit
EOF

docker exec -i oracle_brmo sqlplus -l system/oracle@//localhost:1521/XE < ./.build/ci/oracle-create-users.sql

# set up staging db
docker exec -i oracle_brmo sqlplus -l -S jenkins_staging/jenkins_staging@//localhost:1521/XE < ./old/db/staging/create-brmo-persistence-oracle.sql

# set up rsgb tabellen
docker exec -i oracle_brmo sqlplus -l -S jenkins_rsgb/jenkins_rsgb@//localhost:1521/XE < ./old/db/rsgb/datamodel_oracle.sql

# set up rsgbbgt tabellen
# verwijderd in 2.1.0
# docker exec -i oracle_brmo sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@//localhost:1521/XE < ./old/db/rsgbbgt/oracle/create_rsgb_bgt.sql

# update geotools metadata
docker exec -i oracle_brmo sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@//localhost:1521/XE <<< "update GEOMETRY_COLUMNS set F_TABLE_SCHEMA = 'JENKINS_RSGB';"
docker exec -i oracle_brmo sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@//localhost:1521/XE <<< "update GT_PK_METADATA set TABLE_SCHEMA = 'JENKINS_RSGB';"

# overslaan voor nu, er zijn geen upgrade scripts voor de TopNL
#docker exec -i oracle_brmo sqlplus -l -S top10nl/top10nl@//localhost:1521/XE < ./brmo-topnl-loader/src/main/resources/nl/b3p/topnl/database/oracletop10nl.sql
#docker exec -i oracle_brmo sqlplus -l -S top50nl/top50nl@//localhost:1521/XE < ./brmo-topnl-loader/src/main/resources/nl/b3p/topnl/database/oracletop50nl.sql
#docker exec -i oracle_brmo sqlplus -l -S top100nl/top100nl@//localhost:1521/XE < ./brmo-topnl-loader/src/main/resources/nl/b3p/topnl/database/oracletop100nl.sql
#docker exec -i oracle_brmo sqlplus -l -S top250nl/top250nl@//localhost:1521/XE < ./brmo-topnl-loader/src/main/resources/nl/b3p/topnl/database/oracletop250nl.sql
#
#printf "\nSetup TopNL grants...\n"
#docker exec -i oracle_brmo sqlplus -l top10nl/top10nl@//localhost:1521/XE < ./.build/ci/oracle-db-grant-topnl.sql
#docker exec -i oracle_brmo sqlplus -l top50nl/top50nl@//localhost:1521/XE < ./.build/ci/oracle-db-grant-topnl.sql
#docker exec -i oracle_brmo sqlplus -l top100nl/top100nl@//localhost:1521/XE < ./.build/ci/oracle-db-grant-topnl.sql
#docker exec -i oracle_brmo sqlplus -l top250nl/top250nl@//localhost:1521/XE < ./.build/ci/oracle-db-grant-topnl.sql