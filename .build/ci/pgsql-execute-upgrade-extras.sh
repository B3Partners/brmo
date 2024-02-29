#!/usr/bin/env bash
set -e
CURSNAPSHOT=$(grep "<version>.*<.version>" -m1 pom.xml | sed -e "s/^.*<version/<version/" | cut -f2 -d">"| cut -f1 -d"<")

echo "Huidige snapshot:" $CURSNAPSHOT
echo "Verwerk extra upgrade scripts voor: " $1

#if [ $CURSNAPSHOT = "2.0.0-SNAPSHOT" ] && [ $1 = "rsgb" ]
#then
#    psql -U postgres -h localhost -d $1 -f ./datamodel/extra_scripts/postgresql/206_bag_views.sql
#    psql -U postgres -h localhost -d $1 -f ./datamodel/extra_scripts/postgresql/207_brk_views.sql
#fi

if [ $CURSNAPSHOT = "4.0.0-SNAPSHOT" ] && [ $1 = "staging" ]
then
    docker exec -i oracle_brmo sqlplus -l -S jenkins_$1/jenkins_$1@//localhost:1521/XE < ./datamodel/utility_scripts/postgresql/1000_delete_bag_berichten.sql
fi

