#!/usr/bin/env bash
set -e
CURSNAPSHOT=$(grep "<version>.*<.version>" -m1 pom.xml | sed -e "s/^.*<version/<version/" | cut -f2 -d">"| cut -f1 -d"<")
NEXTRELEASE="${CURSNAPSHOT%-SNAPSHOT}"
MAJOR="${CURSNAPSHOT%.*}"
MINOR="${NEXTRELEASE##*.}"
PREVMINOR=$(($MINOR-1))
PREVRELEASE=$MAJOR.$PREVMINOR

#if [ $CURSNAPSHOT = "2.2.0-SNAPSHOT" ]
#then
#    PREVRELEASE="2.1.0"
#fi

echo "Huidige snapshot:" $CURSNAPSHOT", vorige release: "$PREVRELEASE", komende release: "$NEXTRELEASE
echo "Verwerk upgrade script voor:" $1

#!/usr/bin/env bash
export SQLPATH=./.build/ci
# -S voor silent
docker exec -i oracle_brmo sqlplus -L jenkins_$1/jenkins_$1@//localhost:1521/XE < ./datamodel/upgrade_scripts/$PREVRELEASE-$NEXTRELEASE/oracle/$1.sql
docker exec -i oracle_brmo sqlplus -L jenkins_$1/jenkins_$1@//localhost:1521/XE  <<< "select * from brmo_metadata"

echo "Verwerking upgrade script voor: "$1" afgerond"