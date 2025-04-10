#!/usr/bin/env bash
set -e
CURSNAPSHOT=$(grep "<version>.*<.version>" -m1 pom.xml | sed -e "s/^.*<version/<version/" | cut -f2 -d">"| cut -f1 -d"<")

echo "Huidige snapshot:" $CURSNAPSHOT
echo "Verwerk extra upgrade scripts voor: " $1

#!/usr/bin/env bash
export SQLPATH=./.build/ci

if [ $CURSNAPSHOT = "5.0.0-SNAPSHOT" ] && [ $1 = "rsgb" ]
then
    docker exec -i oracle_brmo sqlplus -l -S jenkins_$1/jenkins_$1@//localhost:1521/FREE < ./datamodel/utility_scripts/oracle/205_delete_brk_brondocumenten.sql
    docker exec -i oracle_brmo sqlplus -l -S jenkins_$1/jenkins_$1@//localhost:1521/FREE < ./datamodel/extra_scripts/oracle/301_drop_brk1_tabellen.sql
fi

if [ $CURSNAPSHOT = "5.0.0-SNAPSHOT" ] && [ $1 = "staging" ]
then
    docker exec -i oracle_brmo sqlplus -l -S jenkins_$1/jenkins_$1@//localhost:1521/FREE < ./datamodel/utility_scripts/oracle/1001_delete_brk1_berichten.sql
fi
