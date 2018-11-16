#!/usr/bin/env bash
CURSNAPSHOT=$(grep "<version>.*<.version>" -m1 pom.xml | sed -e "s/^.*<version/<version/" | cut -f2 -d">"| cut -f1 -d"<")
NEXTRELEASE="${CURSNAPSHOT%-SNAPSHOT}"
MAJOR="${CURSNAPSHOT%.*}"
MINOR="${NEXTRELEASE##*.}"
PREVMINOR=$(($MINOR-1))
PREVRELEASE=$MAJOR.$PREVMINOR

echo "Huidige snapshot:" $CURSNAPSHOT", vorige release: "$PREVRELEASE", komende release: "$NEXTRELEASE

psql -U postgres -d $1 -f ./old/db//upgrade_scripts/$PREVRELEASE-$NEXTRELEASE/postgresql/$1.sql
