#!/bin/bash

# set up rsgb tabellen
ls -l ./datamodel/brk/
export SQLPATH=./.jenkins
projectversion=$(grep "<version>.*<.version>" -m1 pom.xml | sed -e "s/^.*<version/<version/" | cut -f2 -d">"| cut -f1 -d"<")
sed -i s/\${project.version}/$projectversion/g ./datamodel/brk/brk2.0_oracle.sql
sqlplus -l -S jenkins_brk/jenkins_brk@192.168.1.26:15210/XE < ./datamodel/brk/brk2.0_oracle.sql

# TODO update geotools metadata
# sqlplus -l -S jenkins_brk/jenkins_brk@192.168.1.26:15210/XE <<< "update GEOMETRY_COLUMNS set F_TABLE_SCHEMA = 'JENKINS_BRK';"
# sqlplus -l -S jenkins_brk/jenkins_brk@192.168.1.26:15210/XE <<< "update GT_PK_METADATA set TABLE_SCHEMA = 'JENKINS_BRK';"
