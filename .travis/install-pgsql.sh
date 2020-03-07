#!/bin/bash -e
# zie ook: https://docs.travis-ci.com/user/database-setup/
sudo service postgresql stop
#sudo -E apt-get -qq update &>> ~/apt-get-update.log
#sudo apt-get -qq install postgis-2.3 gdal-bin graphviz
#sudo service postgresql stop
sudo service postgresql start $PG_VERSION
psql --version
psql -U postgres -d postgres -c 'SELECT Version();'
psql -U postgres -d postgres -c 'SHOW config_file;'
psql -U postgres -d postgres -c 'SHOW max_connections;'
psql -U postgres -a -c "CREATE ROLE staging LOGIN PASSWORD 'staging' SUPERUSER CREATEDB;"
psql -U postgres -a -c "CREATE ROLE rsgb LOGIN PASSWORD 'rsgb' SUPERUSER CREATEDB;"
psql -U postgres -a -c "CREATE ROLE rsgbbgt LOGIN PASSWORD 'rsgbbgt' SUPERUSER CREATEDB;"
psql -U postgres -a -c "CREATE ROLE topnl LOGIN PASSWORD 'topnl' SUPERUSER CREATEDB;"
psql -U postgres -c 'create database staging;'
psql -U postgres -c 'create database rsgb;'
psql -U postgres -d rsgb -c 'create extension postgis;'
psql -U postgres -d rsgb -c 'ALTER EXTENSION postgis UPDATE;'
psql -U postgres -d rsgb -c 'SELECT PostGIS_full_version();'
psql -U postgres -c 'create database rsgbbgt;'
psql -U postgres -d rsgbbgt -c 'create extension postgis;'
psql -U postgres -d rsgbbgt -c 'ALTER EXTENSION postgis UPDATE;'
psql -U postgres -d rsgbbgt -c 'SELECT PostGIS_full_version();'
psql -U postgres -c 'create database topnl;'
psql -U postgres -d topnl -c 'create extension postgis;'
psql -U postgres -d topnl -c 'ALTER EXTENSION postgis UPDATE;'
psql -U postgres -d topnl -c 'SELECT PostGIS_full_version();'
psql -U postgres -d postgres -c 'alter system set max_connections = 200;'
sudo service postgresql restart $PG_VERSION
psql -U postgres -d postgres -c 'SHOW max_connections;'
