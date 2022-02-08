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
echo "Verwerk upgrade script voor: " $1

DB_NAME=$1
DB_SCHEMA="public"
if [ "bag" = $1 ]; then
  # bag is geen database, maar een schema in rsgb database
  DB_NAME=rsgb
  DB_SCHEMA=$1
fi
psql -U postgres -h localhost -d $DB_NAME -f ./datamodel/upgrade_scripts/$PREVRELEASE-$NEXTRELEASE/postgresql/$1.sql
psql -U postgres -h localhost -d $DB_NAME  -c "select * from $DB_SCHEMA.brmo_metadata"