#!/usr/bin/env bash
set -e
printf "\nset up staging DB...\n"
psql -v ON_ERROR_STOP=1 -U postgres -h localhost -d staging -f ./brmo-persistence/db/create-brmo-persistence-postgresql.sql
if [ $? -eq 0 ]; then
    echo Success
else
    echo Failed setting up staging DB
fi
printf "\nset up rsgb DB...\n"
psql -v ON_ERROR_STOP=1 -U postgres -h localhost -w -q -d rsgb -f ./datamodel/generated_scripts/datamodel_postgresql.sql
if [ $? -eq 0 ]; then
    echo Success
else
    echo Failed setting up rsgb DB
fi
printf "\nset up topnl DB...\n"
psql -v ON_ERROR_STOP=1 -U postgres -h localhost -w -q -d topnl -f ./brmo-topnl-loader/src/main/resources/nl/b3p/topnl/database/postgres.sql
if [ $? -eq 0 ]; then
    echo Success
else
    echo Failed setting up topnl DB
fi

printf "\nset up BRK 2.0 schema in rsgb DB...\n"
psql -v ON_ERROR_STOP=1 -U postgres -h localhost -w -q -d rsgb -f ./datamodel/brk/brk2.0_postgresql.sql
if [ $? -eq 0 ]; then
    echo Success
else
    echo Failed setting up BRK 2.0 schema in rsgb DB
fi

PGOPTIONS="--search_path=brk" psql -v ON_ERROR_STOP=1 -U postgres -h localhost -w -q -d rsgb -f ./datamodel/brk/brk2.0_commentaar.sql
if [ $? -eq 0 ]; then
    echo Success
else
    echo Failed setting up BRK 2.0 schema comments in rsgb DB
fi

PGOPTIONS="--search_path=brk" psql -v ON_ERROR_STOP=1 -U postgres -h localhost -w -q -d rsgb -f ./datamodel/brk/brk2.0_postgresql_views.sql
if [ $? -eq 0 ]; then
    echo Success
else
    echo Failed setting up BRK 2.0 schema comments in rsgb DB
fi