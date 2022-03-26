([xml]$pom=Get-Content .\pom.xml)
(Get-Content .\brmo-persistence\db\create-brmo-persistence-postgresql) -replace '\${project.version}', $pom.project.version | Set-Content .\brmo-persistence\db\create-brmo-persistence-postgresql

