#!/usr/bin/env bash
set -e
export SQLPATH=./.build/ci
printf "\nSetup database...\n"

# we hebben meer dan de default 300 processen nodig
docker exec -i oracle_brmo sqlplus -l sys/oracle@//localhost:1521/FREE as sysdba < ./.build/ci/oracle-system-setup.sql
docker exec -i oracle_brmo sqlplus -s /nolog<<EOF
conn sys/oracle as sysdba
startup
exit
EOF

docker exec -i oracle_brmo sqlplus -l system/oracle@//localhost:1521/FREE < ./.build/ci/oracle-create-users.sql

docker exec -i oracle_brmo sqlplus -l -S jenkins_rsgb/jenkins_rsgb@//localhost:1521/FREE < ./datamodel/generated_scripts/datamodel_oracle.sql

docker exec -i oracle_brmo sqlplus -l -S jenkins_staging/jenkins_staging@//localhost:1521/FREE < ./brmo-persistence/db/create-brmo-persistence-oracle.sql

printf "\nSetup BAG grants...\n"
docker exec -i oracle_brmo sqlplus -l jenkins_bag/jenkins_bag@//localhost:1521/FREE < ./.build/ci/oracle-db-grant-bag-to-rsgb.sql

printf "\nset up BRK 2.0 DB...\n"
docker exec -i oracle_brmo sqlplus -l -S jenkins_brk/jenkins_brk@//localhost:1521/FREE < ./datamodel/brk/brk2.0_oracle.sql
docker exec -i oracle_brmo sqlplus -l -S jenkins_brk/jenkins_brk@//localhost:1521/FREE < ./datamodel/brk/brk2.0_commentaar.sql
docker exec -i oracle_brmo sqlplus -l -S jenkins_brk/jenkins_brk@//localhost:1521/FREE < ./datamodel/brk/brk2.0_oracle_views.sql