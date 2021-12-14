#!/usr/bin/env bash
set -e
psql -U postgres -h localhost -a -c "CREATE ROLE staging LOGIN PASSWORD 'staging' SUPERUSER CREATEDB;"
psql -U postgres -h localhost -a -c "CREATE ROLE rsgb LOGIN PASSWORD 'rsgb' SUPERUSER CREATEDB;"
psql -U postgres -h localhost -a -c "CREATE ROLE rsgbbgt LOGIN PASSWORD 'rsgbbgt' SUPERUSER CREATEDB;"
psql -U postgres -h localhost -a -c "CREATE ROLE topnl LOGIN PASSWORD 'topnl' SUPERUSER CREATEDB;"

psql -U postgres -h localhost -c 'create database staging;'

psql -U postgres -h localhost -c 'create database rsgb;'
psql -U postgres -h localhost -d rsgb -c 'create extension postgis;'
psql -U postgres -h localhost -d rsgb -c 'ALTER EXTENSION postgis UPDATE;'
psql -U postgres -h localhost -d rsgb -c 'SELECT PostGIS_full_version();'

psql -U postgres -h localhost -c 'create database rsgbbgt;'
psql -U postgres -h localhost -d rsgbbgt -c 'create extension postgis;'
psql -U postgres -h localhost -d rsgbbgt -c 'ALTER EXTENSION postgis UPDATE;'
psql -U postgres -h localhost -d rsgbbgt -c 'SELECT PostGIS_full_version();'

psql -U postgres -h localhost -c 'create database topnl;'
psql -U postgres -h localhost -d topnl -c 'create extension postgis;'
psql -U postgres -h localhost -d topnl -c 'ALTER EXTENSION postgis UPDATE;'
psql -U postgres -h localhost -d topnl -c 'SELECT PostGIS_full_version();'