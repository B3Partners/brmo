docker  exec -i mssql_brmo cmd /c dir 'C:\*.sql'

# NB Window 10 hypervisor kan geen bestanden kopiere naar een draaiende container,
# dus daar moet de container herstart worden met tussendoor de kopieer stappen
# in dat geval moet je dus de container zonder de --rm vlag starten!
# docker stop mssql_brmo
([xml]$pom=Get-Content .\pom.xml)
(Get-Content .\brmo-persistence\db\create-brmo-persistence-sqlserver.sql) -replace '\${project.version}', $pom.project.version | Set-Content .\brmo-persistence\db\create-brmo-persistence-sqlserver.sql
docker cp .\brmo-persistence\db\create-brmo-persistence-sqlserver.sql mssql_brmo:C:\
docker cp .\datamodel\generated_scripts\datamodel_sqlserver.sql mssql_brmo:C:\
docker cp .\brmo-topnl-loader\src\main\resources\nl\b3p\topnl\database\sqlserver.sql mssql_brmo:C:\
# docker start mssql_brmo
docker  exec -i mssql_brmo cmd /c dir 'C:\*.sql'
docker exec -i mssql_brmo sqlcmd -S localhost -U SA -P 'Password12!' -d 'master' -Q 'SELECT @@version'

Write-Output "`n`nSet up STAGING db`n"
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d 'master' -Q 'CREATE DATABASE staging'
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d 'master' -Q 'ALTER DATABASE staging SET RECOVERY SIMPLE'
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d staging -i 'C:\create-brmo-persistence-sqlserver.sql'

Write-Output "`n`nSet up RSGB tabellen`n"
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d 'master' -Q 'CREATE DATABASE rsgb'
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d 'master' -Q 'ALTER DATABASE rsgb SET RECOVERY SIMPLE'
# -e SQLCMDINI='C:/init.sql'
# soms treeds onderstaande melding op
# Msg 701, Level 17, State 123, Server D07AF59162DC, Line 11606
# There is insufficient system memory in resource pool 'default' to run this query.
docker exec -i mssql_brmo sqlcmd -r0 -x -b -S localhost -U sa -P 'Password12!' -d 'rsgb' -I -i 'C:\datamodel_sqlserver.sql'

Write-Output "`n`nSet up RSGBBGT tabellen`n"
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d 'master' -Q 'CREATE DATABASE bgttest'
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d 'master' -Q 'ALTER DATABASE bgttest SET RECOVERY SIMPLE'

Write-Output "`n`nSet up TOPNL tabellen`n"
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d 'master' -Q 'CREATE DATABASE topnl'
docker exec -i mssql_brmo sqlcmd -S localhost -U sa -P 'Password12!' -d 'master' -Q 'ALTER DATABASE topnl SET RECOVERY SIMPLE'
docker exec -i mssql_brmo sqlcmd -r0 -b -S localhost -I -U sa -P 'Password12!' -d 'topnl' -i 'C:\sqlserver.sql'
