#!/usr/bin/env bash
set -e
docker version

docker pull postgis/postgis:$1

# this container can be stopped using: docker stop pgsql_brmo
docker run --rm -p 5432:5432 -e POSTGRES_PASSWORD=postgres --name pgsql_brmo -h pgsql_brmo -d postgis/postgis:$1 -c max_connections=200 -c shared_buffers=8GB -c max_wal_size=3GB -c autovacuum_max_workers=4 -c maintenance_work_mem=2GB

printf "\n\nStarting PostGIS $1 container, this could take a few seconds..."
printf "\nWaiting for PostGIS $1 database to start up.... "
_WAIT=0;
while :
do
    printf " $_WAIT"
    if $(docker logs pgsql_brmo | grep -q 'database system is ready to accept connections'); then
        printf "\PostGIS $1 Database started\n\n"
        break
    fi
    sleep 10
    _WAIT=$(($_WAIT+10))
done

# print logs
docker logs pgsql_brmo
