docker  exec -i mssql_brmo cmd /c dir 'C:\*.sql'

# docker cp .\.appveyor\init.sql mssql_brmo:C:\
([xml]$pom=Get-Content .\pom.xml)
(Get-Content .\brmo-persistence\db\create-brmo-persistence-sqlserver.sql) -replace '\${project.version}', $pom.project.version | Set-Content .\brmo-persistence\db\create-brmo-persistence-sqlserver.sql
docker cp .\brmo-persistence\db\create-brmo-persistence-sqlserver.sql mssql_brmo:C:\
docker cp .\datamodel\generated_scripts\datamodel_sqlserver.sql mssql_brmo:C:\
docker cp .\bgt-gml-loader\target\generated-resources\ddl\sqlserver\create_rsgb_bgt.sql mssql_brmo:C:\
docker cp .\brmo-topnl-loader\src\main\resources\nl\b3p\topnl\database\sqlserver.sql mssql_brmo:C:\
docker  exec -i mssql_brmo cmd /c dir 'C:\*.sql'

docker exec -i mssql_brmo sqlcmd -S localhost -U SA -P 'Password12!' -d 'master' -Q 'SELECT @@version'

printf "\n\nSet up STAGING db\n"
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d 'master' -Q 'CREATE DATABASE staging'
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d 'master' -Q 'ALTER DATABASE staging SET RECOVERY SIMPLE'
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d staging -i 'C:\create-brmo-persistence-sqlserver.sql'

printf "\n\nSet up RSGB tabellen\n"
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d 'master' -Q 'CREATE DATABASE rsgb'
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d 'master' -Q 'ALTER DATABASE rsgb SET RECOVERY SIMPLE'
# -e SQLCMDINI='C:/init.sql'
docker exec -i mssql_brmo sqlcmd -r0 -x -b -S localhost -U sa -P 'Password12!' -d 'rsgb' -I -i 'C:\datamodel_sqlserver.sql'

printf "\n\nSet up RSGBBGT tabellen\n"
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d 'master' -Q 'CREATE DATABASE bgttest'
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d 'master' -Q 'ALTER DATABASE bgttest SET RECOVERY SIMPLE'
docker exec -i mssql_brmo sqlcmd -r0 -b -S localhost -I -U sa -P 'Password12!' -d 'bgttest' -i 'C:\create_rsgb_bgt.sql'

printf "\n\nSet up TOPNL tabellen\n"
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d 'master' -Q 'CREATE DATABASE topnl'
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d 'master' -Q 'ALTER DATABASE topnl SET RECOVERY SIMPLE'
docker exec -i mssql_brmo sqlcmd -r0 -b -S localhost -I -U sa -P 'Password12!' -d 'topnl' -i 'C:\sqlserver.sql'
