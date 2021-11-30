#!/usr/bin/env bash
export SQLPATH=./.build/ci
printf "\nSetup database...\n"

# we hebben meer dan de default 300 processen nodig
docker exec -i oracle_brmo sqlplus -l sys/oracle@//localhost:1521/XE as sysdba < ./.build/ci/oracle-system-setup.sql
docker exec -i oracle_brmo sqlplus -s /nolog<<EOF
conn sys/oracle as sysdba
startup
exit
EOF

docker exec -i oracle_brmo sqlplus -l system/oracle@//localhost:1521/XE < ./.build/ci/oracle-create-users.sql

docker exec -i oracle_brmo sqlplus -l -S jenkins_rsgb/jenkins_rsgb@//localhost:1521/XE < ./datamodel/generated_scripts/datamodel_oracle.sql

docker exec -i oracle_brmo sqlplus -l -S jenkins_staging/jenkins_staging@//localhost:1521/XE < ./brmo-persistence/db/create-brmo-persistence-oracle.sql

docker exec -i oracle_brmo sqlplus -l -S top10nl/top10nl@//localhost:1521/XE < ./brmo-topnl-loader/src/main/resources/nl/b3p/topnl/database/oracletop10nl.sql
docker exec -i oracle_brmo sqlplus -l -S top50nl/top50nl@//localhost:1521/XE < ./brmo-topnl-loader/src/main/resources/nl/b3p/topnl/database/oracletop50nl.sql
docker exec -i oracle_brmo sqlplus -l -S top100nl/top100nl@//localhost:1521/XE < ./brmo-topnl-loader/src/main/resources/nl/b3p/topnl/database/oracletop100nl.sql
docker exec -i oracle_brmo sqlplus -l -S top250nl/top250nl@//localhost:1521/XE < ./brmo-topnl-loader/src/main/resources/nl/b3p/topnl/database/oracletop250nl.sql

printf "\nSetup TopNL grants...\n"
docker exec -i oracle_brmo sqlplus -l top10nl/top10nl@//localhost:1521/XE < ./.build/ci/oracle-db-grant-topnl.sql
docker exec -i oracle_brmo sqlplus -l top50nl/top50nl@//localhost:1521/XE < ./.build/ci/oracle-db-grant-topnl.sql
docker exec -i oracle_brmo sqlplus -l top100nl/top100nl@//localhost:1521/XE < ./.build/ci/oracle-db-grant-topnl.sql
docker exec -i oracle_brmo sqlplus -l top250nl/top250nl@//localhost:1521/XE < ./.build/ci/oracle-db-grant-topnl.sql

printf "\nSetup BAG grants...\n"
docker exec -i oracle_brmo sqlplus -l jenkins_bag/jenkins_bag@//localhost:1521/XE < ./.build/ci/oracle-db-grant-bag-top-rsgb.sql