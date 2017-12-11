#!/bin/bash

# set up topnl tabellen
ls -l ./topparser/src/main/resources/nl/b3p/topnl/database/
sqlplus -l -S top10nl/top10nl@192.168.1.41:1521/DB01 < ./topparser/src/main/resources/nl/b3p/topnl/database/oracletop10nl.sql
sqlplus -l -S top50nl/top50nl@192.168.1.41:1521/DB01 < ./topparser/src/main/resources/nl/b3p/topnl/database/oracletop50nl.sql
sqlplus -l -S top100nl/top100nl@192.168.1.41:1521/DB01 < ./topparser/src/main/resources/nl/b3p/topnl/database/oracletop100nl.sql
sqlplus -l -S top250nl/top250nl@192.168.1.41:1521/DB01 < ./topparser/src/main/resources/nl/b3p/topnl/database/oracletop250nl.sql


sqlplus -l -S top10nl/top10nl@192.168.1.41:1521/DB01 < ./.jenkins/db-grant-topnl.sql
sqlplus -l -S top50nl/top50nl@192.168.1.41:1521/DB01 < ./.jenkins/db-grant-topnl.sql
sqlplus -l -S top100nl/top100nl@192.168.1.41:1521/DB01 < ./.jenkins/db-grant-topnl.sql
sqlplus -l -S top250nl/top250nl@192.168.1.41:1521/DB01 < ./.jenkins/db-grant-topnl.sql


