#!/usr/bin/env bash
set -e
export SQLPATH=./.build/ci
printf "\nset up BAG views...\n"
docker exec -i oracle_brmo sqlplus -l jenkins_bag/jenkins_bag@//localhost:1521/XE < ./datamodel/extra_scripts/oracle/208_bag2_views.sql
printf "\nSetup BAG grants...\n"
docker exec -i oracle_brmo sqlplus -l jenkins_bag/jenkins_bag@//localhost:1521/XE < ./.build/ci/oracle-db-grant-bag-to-rsgb.sql
printf "\nset up RSGB BAG views...\n"
docker exec -i oracle_brmo sqlplus -l jenkins_rsgb/jenkins_rsgb@//localhost:1521/XE < ./datamodel/extra_scripts/oracle/209_bag2_rsgb_views.sql
printf "\nset up RSGB BAG and BRK materialized views...\n"
printf "\nSetup BRK grants...\n"
docker exec -i oracle_brmo sqlplus -l jenkins_brk/jenkins_brk@//localhost:1521/XE < ./.build/ci/oracle-db-grant-brk-to-rsgb.sql
sed -i 's/BRMO_BRK/JENKINS_BRK/g' ./datamodel/extra_scripts/oracle/210_bag2_brk2.0_mat_views.sql
docker exec -i oracle_brmo sqlplus -l jenkins_rsgb/jenkins_rsgb@//localhost:1521/XE < ./datamodel/extra_scripts/oracle/210_bag2_brk2.0_mat_views.sql
