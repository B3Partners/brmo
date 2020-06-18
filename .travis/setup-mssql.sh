#!/bin/bash -e
export PATH="$PATH:/opt/mssql-tools/bin"
export SQLCMDINI=.appveyor/init.sql
# set up staging db
sqlcmd -S localhost -U sa -P Password12! -d "master" -Q "CREATE DATABASE staging"
sqlcmd -S localhost -U sa -P Password12! -d "master" -Q "ALTER DATABASE staging SET RECOVERY SIMPLE"
sqlcmd -S localhost -U sa -P Password12! -d staging -i ./brmo-persistence/db/create-brmo-persistence-sqlserver.sql
sqlcmd -S localhost -U sa -P Password12! -d staging -i ./brmo-persistence/db/02_insert_default_user.sql
# set up rsgb tabellen
sqlcmd -S localhost -U sa -P Password12! -d "master" -Q "CREATE DATABASE rsgb"
sqlcmd -S localhost -U sa -P Password12! -d "master" -Q "ALTER DATABASE rsgb SET RECOVERY SIMPLE"
sqlcmd -r0 -x -b -S localhost -U sa -P Password12! -d "rsgb" -I -i ./datamodel/generated_scripts/datamodel_sqlserver.sql
# set up rsgbbgt tabellen
sqlcmd -S localhost -U sa -P Password12! -d "master" -Q "CREATE DATABASE bgttest"
sqlcmd -S localhost -U sa -P Password12! -d "master" -Q "ALTER DATABASE bgttest SET RECOVERY SIMPLE"
sqlcmd -r0 -b -S localhost -I -U sa -P Password12! -d "bgttest" -i ./bgt-gml-loader/target/generated-resources/ddl/sqlserver/create_rsgb_bgt.sql
# set up topnl tabellen
sqlcmd -S localhost -U sa -P Password12! -d "master" -Q "CREATE DATABASE topnl"
sqlcmd -S localhost -U sa -P Password12! -d "master" -Q "ALTER DATABASE topnl SET RECOVERY SIMPLE"
sqlcmd -r0 -b -S localhost -I -U sa -P Password12! -d "topnl" -i ./topparser/src/main/resources/nl/b3p/topnl/database/sqlserver.sql
