#!/usr/bin/env bash
set -e
CURSNAPSHOT=$(grep "<version>.*<.version>" -m1 pom.xml | sed -e "s/^.*<version/<version/" | cut -f2 -d">"| cut -f1 -d"<")

echo "Huidige snapshot:" $CURSNAPSHOT
echo "Verwerk extra upgrade scripts voor: " $1

#!/usr/bin/env bash
export SQLPATH=./.build/ci

if [ $CURSNAPSHOT = "4.0.0-SNAPSHOT" ] && [ $1 = "rsgb" ]
then
    docker exec -i oracle_brmo sqlplus -l -S jenkins_$1/jenkins_$1@//localhost:1521/FREE < ./datamodel/utility_scripts/oracle/202_delete_bag_brondocumenten.sql
    docker exec -i oracle_brmo sqlplus -l -S jenkins_$1/jenkins_$1@//localhost:1521/FREE < ./datamodel/extra_scripts/oracle/300_drop_bag_tabellen.sql
fi

if [ $CURSNAPSHOT = "4.0.0-SNAPSHOT" ] && [ $1 = "staging" ]
then
    docker exec -i oracle_brmo sqlplus -l -S jenkins_$1/jenkins_$1@//localhost:1521/FREE < ./datamodel/utility_scripts/oracle/1000_delete_bag_berichten.sql
fi

if [ $CURSNAPSHOT = "4.0.0-SNAPSHOT" ] && [ $1 = "topnl" ]
then
    docker exec -i oracle_brmo sqlplus -l -S sys/oracle@//localhost:1521/XE < ./datamodel/upgrade_scripts/3.0.2-4.0.0/oracle/topnl.sql
fi