#!/bin/bash -e
docker version

# this docker image has the following users/credentials (user/password = system/oracle)
docker pull gvenzl/oracle-xe:full

# start the dockerized oracle-xe instance (the container will be destroyed/removed on stopping)
# this container can be stopped using: docker stop oracle_brmo
docker run --rm -p 1521:1521 --name oracle_brmo -h oracle_brmo -e ORACLE_PASSWORD=oracle -d gvenzl/oracle-xe:full

printf "\n\nStarting Oracle XE container, this could take a few minutes..."
printf "\nWaiting for Oracle XE database to start up.... "
_WAIT=0;
while :
do
    printf " $_WAIT"
    if $(docker logs oracle_brmo | grep -q 'DATABASE IS READY TO USE!'); then
        printf "\nOracle XE Database started\n\n"
        break
    fi
    sleep 10
    _WAIT=$(($_WAIT+10))
done

# print logs
docker logs oracle_brmo
