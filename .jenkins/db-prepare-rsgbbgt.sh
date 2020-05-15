#!/bin/bash

export SQLPATH=./.jenkins
# set up rsgbbgt tabellen
sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.26:15210/XE < ./bgt-gml-loader/target/generated-resources/ddl/oracle/create_rsgb_bgt.sql
# update geotools metadata
sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.26:15210/XE <<< "update GEOMETRY_COLUMNS set F_TABLE_SCHEMA = 'JENKINS_RSGBBGT';"
sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.26:15210/XE <<< "update GT_PK_METADATA set TABLE_SCHEMA = 'JENKINS_RSGBBGT';"

