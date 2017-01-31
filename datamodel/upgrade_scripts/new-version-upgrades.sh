#!/usr/bin/env bash
#
# na een release uitvoeren om migratie scripts voor de volgende release aan te maken
#
CURSNAPSHOT=$(grep "<version>.*<.version>" -m1 ../../pom.xml | sed -e "s/^.*<version/<version/" | cut -f2 -d">"| cut -f1 -d"<")

NEXTRELEASE="${CURSNAPSHOT%-SNAPSHOT}"

MAJOR="${CURSNAPSHOT%.*}"

MINOR="${NEXTRELEASE##*.}"

PREVMINOR=$(($MINOR-1))

PREVRELEASE=$MAJOR.$PREVMINOR

echo "Huidige snapshot:" $CURSNAPSHOT ", vorige release: " $PREVRELEASE " komende release" $NEXTRELEASE

mkdir -p "$PREVRELEASE-$NEXTRELEASE"/{oracle,postgresql,sqlserver}

for DB in Oracle PostgreSQL SQLserver 
do
  DIR=$PREVRELEASE-$NEXTRELEASE/${DB,,}
  echo Migratie bestanden aanmaken voor $DB in $DIR
  for b in rsgb rsgbbgt staging
  do
    if [ -f "$DIR/$b.sql" ]
    then
      echo Bestand $DIR/$b.sql bestaal al.
    else
      echo -- $'\n'-- upgrade $DB ${b^^} datamodel van $PREVRELEASE naar $NEXTRELEASE $n $'\n'-- > $DIR/$b.sql
      echo $'\n'-- versienummer update >> $DIR/$b.sql
      echo "UPDATE brmo_metadata SET waarde='$NEXTRELEASE' WHERE naam='brmoversie';" >> $DIR/$b.sql
    fi
  done
done
