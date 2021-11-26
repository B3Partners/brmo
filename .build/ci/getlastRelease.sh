#!/usr/bin/env bash
CURSNAPSHOT=$(grep "<version>.*<.version>" -m1 pom.xml | sed -e "s/^.*<version/<version/" | cut -f2 -d">"| cut -f1 -d"<")
NEXTRELEASE="${CURSNAPSHOT%-SNAPSHOT}"
MAJOR="${CURSNAPSHOT%.*}"
MINOR="${NEXTRELEASE##*.}"
PREVMINOR=$(($MINOR-1))
PREVRELEASE=$MAJOR.$PREVMINOR

if [ $CURSNAPSHOT = "2.2.0-SNAPSHOT" ]
then
    PREVRELEASE="2.1.0"
fi
echo "Huidige snapshot:" $CURSNAPSHOT", vorige, te downloaden, release: "$PREVRELEASE", komende release: "$NEXTRELEASE

REMOTE_FILE="https://repo.b3p.nl/nexus/repository/public/nl/b3p/brmo-dist/${PREVRELEASE}/brmo-dist-${PREVRELEASE}-bin.zip"
LOCAL_FILE="${HOME}/downloads/brmo-dist-old.zip"
#LOCAL_FILE="/tmp/downloads/brmo-dist-old.zip"

#modified=$(curl --silent --head $remote_file | awk '/^Last-Modified/{print $0}' | sed 's/^Last-Modified: //')
#remote_ctime=$(date --date="$modified" +%s)
#local_ctime=$(stat -c %z "$local_file")
#local_ctime=$(date --date="$local_ctime" +%s)
#echo "remote_ctime: "$remote_ctime" local_ctime: "$local_ctime

#[ $local_ctime -lt $remote_ctime ] && curl --create-dirs -o $local_file $remote_file
#wget -N -nc --tries=5 --timeout=60 --waitretry=300 --user-agent="" "https://repo.b3p.nl/nexus/repository/releases/nl/b3p/brmo-dist/$PREVRELEASE/brmo-dist-$PREVRELEASE-bin.zip"  --output-document="/tmp/downloads/brmo-dist-old.zip"

if [ -f "$LOCAL_FILE" ]; then
    LOCAL_SIZE=$(wc -c < $LOCAL_FILE)
else
    LOCAL_SIZE=0
fi
REMOTE_SIZE=$(curl -sI $REMOTE_FILE | awk '/Content-Length/ {sub("\r",""); print $2}')

echo "remote size: "$REMOTE_SIZE", local size: "$LOCAL_SIZE

if [ $LOCAL_SIZE != $REMOTE_SIZE ]; then
    echo "Size differs, downloading."
    curl --create-dirs -o $LOCAL_FILE $REMOTE_FILE
else
    echo "Same size, not downloading."
fi

unzip -o $LOCAL_FILE -d ./old *.sql
