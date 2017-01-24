#!/bin/bash

# set up rsgbbgt tabellen
sqlplus jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.41:1521/DB01 < ./bgt-gml-loader/target/generated-resources/ddl/oracle/create_rsgb_bgt.sql
# update geotools metadata
sqlplus jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.41:1521/DB01 <<< "update GEOMETRY_COLUMNS set F_TABLE_SCHEMA = 'JENKINS_RSGB';"
sqlplus jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.41:1521/DB01 <<< "update GT_PK_METADATA set TABLE_SCHEMA = 'JENKINS_RSGB';"

