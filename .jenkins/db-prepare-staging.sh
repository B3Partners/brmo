#!/bin/bash
ls -l ./brmo-persistence/db/
export SQLPATH=./.jenkins
projectversion=$(grep "<version>.*<.version>" -m1 pom.xml | sed -e "s/^.*<version/<version/" | cut -f2 -d">"| cut -f1 -d"<")
sed -i s/\${project.version}/$projectversion/g ./brmo-persistence/db/create-brmo-persistence-oracle.sql
sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.26:15210/XE < ./brmo-persistence/db/create-brmo-persistence-oracle.sql
sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.26:15210/XE < ./brmo-persistence/db/02_insert_default_user.sql
