#!/usr/bin/env bash
CURSNAPSHOT=$(grep "<version>.*<.version>" -m1 pom.xml | sed -e "s/^.*<version/<version/" | cut -f2 -d">"| cut -f1 -d"<")

NEXTRELEASE="${CURSNAPSHOT%-SNAPSHOT}"

MAJOR="${CURSNAPSHOT%.*}"

MINOR="${NEXTRELEASE##*.}"

PREVMINOR=$(($MINOR-1))

PREVRELEASE=$MAJOR.$PREVMINOR

echo "Huidige snapshot:" $CURSNAPSHOT", vorige release: "$PREVRELEASE", komende release: "$NEXTRELEASE

wget -nc --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "https://repo.b3p.nl/nexus/repository/releases/nl/b3p/brmo-dist/$PREVRELEASE/brmo-dist-$PREVRELEASE-bin.zip"  --output-document="$HOME/downloads/brmo-dist-old.zip"

unzip -o $HOME/downloads/brmo-dist-old.zip -d ./old *.sql
