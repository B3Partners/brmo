#!/usr/bin/env bash
# set up staging db
docker exec -i mssql_brmo /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P Password12! -d "master" -Q "CREATE DATABASE staging"
docker exec -i mssql_brmo /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P Password12! -d "master" -Q "ALTER DATABASE staging SET RECOVERY SIMPLE"
docker cp ./brmo-persistence/db/create-brmo-persistence-sqlserver.sql mssql_brmo:/home/
docker exec -i mssql_brmo /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P Password12! -d staging -i /home/create-brmo-persistence-sqlserver.sql
# set up rsgb tabellen
docker exec -i mssql_brmo /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P Password12! -d "master" -Q "CREATE DATABASE rsgb"
docker exec -i mssql_brmo /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P Password12! -d "master" -Q "ALTER DATABASE rsgb SET RECOVERY SIMPLE"
docker cp ./datamodel/generated_scripts/datamodel_sqlserver.sql mssql_brmo:/home/
docker exec -i mssql_brmo /opt/mssql-tools/bin/sqlcmd -r0 -x -b -S localhost -U sa -P Password12! -d "rsgb" -I -i /home/datamodel_sqlserver.sql
# set up rsgbbgt tabellen
docker exec -i mssql_brmo /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P Password12! -d "master" -Q "CREATE DATABASE bgttest"
docker exec -i mssql_brmo /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P Password12! -d "master" -Q "ALTER DATABASE bgttest SET RECOVERY SIMPLE"

# set up topnl tabellen
docker exec -i mssql_brmo /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P Password12! -d "master" -Q "CREATE DATABASE topnl"
docker exec -i mssql_brmo /opt/mssql-tools/bin/sqlcmd -S localhost -U sa -P Password12! -d "master" -Q "ALTER DATABASE topnl SET RECOVERY SIMPLE"
docker cp ./brmo-topnl-loader/src/main/resources/nl/b3p/topnl/database/sqlserver.sql mssql_brmo:/home/
docker exec -i mssql_brmo /opt/mssql-tools/bin/sqlcmd -r0 -b -S localhost -I -U sa -P Password12! -d "topnl" -i /home/sqlserver.sql



