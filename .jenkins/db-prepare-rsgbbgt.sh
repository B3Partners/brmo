#!/bin/bash

# set up rsgbbgt tabellen
sqlplus -l -S c##jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.11:1521/ORCL < ./bgt-gml-loader/target/generated-resources/ddl/oracle/create_rsgb_bgt.sql
# update geotools metadata
sqlplus -l -S c##jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.11:1521/ORCL <<< "update GEOMETRY_COLUMNS set F_TABLE_SCHEMA = 'JENKINS_RSGB';"
sqlplus -l -S c##jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.11:1521/ORCL <<< "update GT_PK_METADATA set TABLE_SCHEMA = 'JENKINS_RSGB';"

