#!/usr/bin/env bash
docker start oracle-brmo
# print logs
# docker logs oracle-brmo


printf "\n\nStarting Oracle XE container, this could take a few minutes..."
printf "\nWaiting for Oracle XE database to start up.... "
_WAIT=0;
while :
do
    printf " $_WAIT"
    if $(docker logs oracle-brmo | grep -q 'DATABASE IS READY TO USE!'); then
        printf "\nOracle XE Database started\n\n"
        break
    fi
    sleep 10
    _WAIT=$(($_WAIT+10))
done

