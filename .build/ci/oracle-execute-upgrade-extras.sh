#!/usr/bin/env bash
set -e
CURSNAPSHOT=$(grep "<version>.*<.version>" -m1 pom.xml | sed -e "s/^.*<version/<version/" | cut -f2 -d">"| cut -f1 -d"<")

echo "Huidige snapshot:" $CURSNAPSHOT
echo "Verwerk extra upgrade scripts voor: " $1

#!/usr/bin/env bash
export SQLPATH=./.build/ci

if [ $CURSNAPSHOT = "6.0.0-SNAPSHOT" ] && [ $1 = "topnl" ]
then
    docker exec -i oracle_brmo sqlplus -L system/oracle@//localhost:1521/FREE < ./datamodel/upgrade_scripts/5.0.2-6.0.0/oracle/topnl.sql
fi
