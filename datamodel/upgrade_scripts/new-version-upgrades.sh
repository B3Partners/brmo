#!/usr/bin/env bash
#
# na een release uitvoeren om migratie scripts voor de volgende release aan te maken
#
CURSNAPSHOT=$(grep "<version>.*<.version>" -m1 ../../pom.xml | sed -e "s/^.*<version/<version/" | cut -f2 -d">" | cut -f1 -d"<")

NEXTRELEASE="${CURSNAPSHOT%-SNAPSHOT}"

MAJOR="${CURSNAPSHOT%.*}"

MINOR="${NEXTRELEASE##*.}"

PREVMINOR=$(($MINOR - 1))

PREVRELEASE=$MAJOR.$PREVMINOR

echo "Huidige snapshot:" $CURSNAPSHOT", vorige release: "$PREVRELEASE", komende release: "$NEXTRELEASE

mkdir -p "$PREVRELEASE-$NEXTRELEASE"/{oracle,postgresql,sqlserver}

for DB in Oracle PostgreSQL SQLserver; do
  DIR=$PREVRELEASE-$NEXTRELEASE/${DB,,}
  echo Migratie bestanden aanmaken voor $DB in $DIR
  for b in bag rsgb rsgbbgt staging; do
    if [ -f "$DIR/$b.sql" ]; then
      echo Bestand $DIR/$b.sql bestaal al.
    else
      echo -- $'\n'-- upgrade $DB ${b^^} datamodel van $PREVRELEASE naar $NEXTRELEASE $'\n'-- >$DIR/$b.sql

      if [ "$DB" == "Oracle" ]; then
        echo $'\n'WHENEVER SQLERROR EXIT SQL.SQLCODE >>$DIR/$b.sql
        if [ "${b}" == "rsgbbgt" ] || [ "${b}" == "bag" ]; then
          # brmo_metadata tabel aanmaken in oracle bag en rsgbbgt schema als die niet bestaat
          echo $'\n\n'
          echo $"BEGIN" >>$DIR/$b.sql
          echo $"    EXECUTE IMMEDIATE 'CREATE TABLE brmo_metadata(naam VARCHAR2(255 CHAR) NOT NULL, waarde CLOB, PRIMARY KEY (naam))';" >>$DIR/$b.sql
          echo $"EXCEPTION" >>$DIR/$b.sql
          echo $"WHEN OTHERS THEN" >>$DIR/$b.sql
          echo $"IF" >>$DIR/$b.sql
          echo $"    SQLCODE = -955 THEN" >>$DIR/$b.sql
          echo $"    NULL;" >>$DIR/$b.sql
          echo $"ELSE RAISE;" >>$DIR/$b.sql
          echo $"END IF;" >>$DIR/$b.sql
          echo $"END;" >>$DIR/$b.sql
          echo $"/" >>$DIR/$b.sql
          echo $"MERGE INTO brmo_metadata USING DUAL ON (naam = 'brmoversie') WHEN NOT MATCHED THEN INSERT (naam) VALUES('brmoversie');" >>$DIR/$b.sql
        fi
      fi

      if [ "${DB}" == "SQLserver" ] && [ "${b}" == "rsgbbgt" ]; then
        # brmo_metadata tabel aanmaken in sqlserver rsgbbgt schema als die niet bestaat
        echo $'\n'"IF OBJECT_ID('brmo_metadata', 'U') IS NULL" >>$DIR/$b.sql
        echo $"CREATE TABLE brmo_metadata(naam VARCHAR(255) NOT NULL, waarde NTEXT, PRIMARY KEY (naam));" >>$DIR/$b.sql
        echo $"GO" >>$DIR/$b.sql
        echo $"INSERT INTO brmo_metadata(naam) SELECT naam FROM brmo_metadata WHERE NOT('brmoversie' IN (SELECT naam FROM brmo_metadata));" >>$DIR/$b.sql
      fi

      if [ "${DB}" == "PostgreSQL" ]; then
        if [ "${b}" == "rsgbbgt" ]; then
          echo $'\n'"CREATE TABLE IF NOT EXISTS brmo_metadata(naam CHARACTER VARYING(255) NOT NULL, waarde TEXT, CONSTRAINT brmo_metadata_pk PRIMARY KEY (naam));" >>$DIR/$b.sql
          echo $"INSERT INTO brmo_metadata(naam) VALUES('brmoversie') ON CONFLICT DO NOTHING;" >>$DIR/$b.sql
        fi

        if [ "${b}" == "bag" ]; then
          echo $'\n'"CREATE SCHEMA IF NOT EXISTS bag;" >>$DIR/$b.sql
          echo $'\n'"SET search_path = bag,public;" >>$DIR/$b.sql
        fi

        if [ "${b}" == "rsgb" ]; then
          echo $'\n'"set search_path = public,bag;" >>$DIR/$b.sql
        fi
      fi

      echo $'\n\n'-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd >>$DIR/$b.sql
      if [ "${DB}" == "SQLserver" ]; then
        echo "INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_"$PREVRELEASE"_naar_"$NEXTRELEASE"','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';" >>$DIR/$b.sql
        echo -- versienummer update >>$DIR/$b.sql
        echo "UPDATE brmo_metadata SET waarde='$NEXTRELEASE' WHERE naam='brmoversie';" >>$DIR/$b.sql
      else
        echo "INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_"$PREVRELEASE"_naar_"$NEXTRELEASE"','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';" >>$DIR/$b.sql
        echo -- versienummer update >>$DIR/$b.sql
        echo "UPDATE brmo_metadata SET waarde='$NEXTRELEASE' WHERE naam='brmoversie';" >>$DIR/$b.sql
      fi
    fi
  done
done

# geen ondersteuning voor bag 2 in sql server
rm $PREVRELEASE-$NEXTRELEASE/sqlserver/bag.sql

git add "$PREVRELEASE-$NEXTRELEASE"/
git commit -m "Migratie bestanden aanmaken voor upgrade $PREVRELEASE-$NEXTRELEASE"
