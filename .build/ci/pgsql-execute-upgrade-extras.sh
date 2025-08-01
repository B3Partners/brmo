#!/usr/bin/env bash
set -e
CURSNAPSHOT=$(grep "<version>.*<.version>" -m1 pom.xml | sed -e "s/^.*<version/<version/" | cut -f2 -d">"| cut -f1 -d"<")

echo "Huidige snapshot:" $CURSNAPSHOT
echo "Verwerk extra upgrade scripts voor: " $1

if [ $CURSNAPSHOT = "6.0.0-SNAPSHOT" ] && [ $1 = "topnl" ]
then
    psql -U postgres -h localhost -f ./datamodel/upgrade_scripts/5.0.2-6.0.0/postgresql/topnl.sql
fi
