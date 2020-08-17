#!/usr/bin/env bash
docker version

# this docker image has the following users/credentials (user/password = system/oracle)
# docker pull pvargacl/oracle-xe-18.4.0:latest
# docker run -m 4g --cpus=2 -p 15210:1521 --name oracle-brmo -h oracle-brmo -d pvargacl/oracle-xe-18.4.0:latest

# start the dockerized oracle-xe instance
# this container can be stopped using:
#
#    docker stop oracle-brmo
#
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

# docker ps -a
# print logs
# docker logs oracle-brmo
