#!/bin/bash

# set up topnl tabellen
ls -l ./topparser/src/main/resources/nl/b3p/topnl/database/
echo "aanmaken top10nl schema"
sqlplus -l -S c##top10nl/top10nl@192.168.1.11:1521/ORCL < ./topparser/src/main/resources/nl/b3p/topnl/database/oracletop10nl.sql
echo "aanmaken top50nl schema"
sqlplus -l -S c##top50nl/top50nl@192.168.1.11:1521/ORCL < ./topparser/src/main/resources/nl/b3p/topnl/database/oracletop50nl.sql
echo "aanmaken top100nl schema"
sqlplus -l -S c##top100nl/top100nl@192.168.1.11:1521/ORCL < ./topparser/src/main/resources/nl/b3p/topnl/database/oracletop100nl.sql
echo "aanmaken top250nl schema"
sqlplus -l -S c##top250nl/top250nl@192.168.1.11:1521/ORCL < ./topparser/src/main/resources/nl/b3p/topnl/database/oracletop250nl.sql

echo "toebedelen grants aan JENKINS_TOPNL"
sqlplus -l -S c##top10nl/top10nl@192.168.1.11:1521/ORCL < ./.jenkins/db-grant-topnl.sql
sqlplus -l -S c##top50nl/top50nl@192.168.1.11:1521/ORCL < ./.jenkins/db-grant-topnl.sql
sqlplus -l -S c##top100nl/top100nl@192.168.1.11:1521/ORCL < ./.jenkins/db-grant-topnl.sql
sqlplus -l -S c##top250nl/top250nl@192.168.1.11:1521/ORCL < ./.jenkins/db-grant-topnl.sql
