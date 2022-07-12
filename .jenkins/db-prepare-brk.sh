#!/bin/bash

# set up rsgb tabellen
ls -l ./datamodel/generated_scripts/
export SQLPATH=./.jenkins
sqlplus -l -S jenkins_brk/jenkins_brk@192.168.1.26:15210/XE < ./datamodel/brk/brk2.0_oracle.sql

# update geotools metadata
# sqlplus -l -S jenkins_brk/jenkins_brk@192.168.1.26:15210/XE <<< "update GEOMETRY_COLUMNS set F_TABLE_SCHEMA = 'JENKINS_RSGB';"
# sqlplus -l -S jenkins_brk/jenkins_brk@192.168.1.26:15210/XE <<< "update GT_PK_METADATA set TABLE_SCHEMA = 'JENKINS_RSGB';"
