#!/usr/bin/env bash
CURSNAPSHOT=$(grep "<version>.*<.version>" -m1 pom.xml | sed -e "s/^.*<version/<version/" | cut -f2 -d">"| cut -f1 -d"<")
NEXTRELEASE="${CURSNAPSHOT%-SNAPSHOT}"
MAJOR="${CURSNAPSHOT%.*}"
MINOR="${NEXTRELEASE##*.}"
PREVMINOR=$(($MINOR-1))
PREVRELEASE=$MAJOR.$PREVMINOR

if [ $CURSNAPSHOT = "2.3.0-SNAPSHOT" ]
then
    PREVRELEASE="2.2.2"
fi

echo "Huidige snapshot:" $CURSNAPSHOT", vorige release: "$PREVRELEASE", komende release: "$NEXTRELEASE
echo "Verwerk upgrade script voor: " $1

export SQLPATH=./.jenkins
sqlplus -l -S jenkins_$1/jenkins_$1@192.168.1.26:15210/XE < ./datamodel/upgrade_scripts/$PREVRELEASE-$NEXTRELEASE/oracle/$1.sql