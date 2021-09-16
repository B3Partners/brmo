#!/usr/bin/env bash
export SQLPATH=./.jenkins

# set up staging db
sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.26:15210/XE < ./old/db/staging/create-brmo-persistence-oracle.sql

# set up rsgb tabellen
sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.26:15210/XE < ./old/db/rsgb/datamodel_oracle.sql

# set up rsgbbgt tabellen
# verwijderd in 2.1.0
# sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.26:15210/XE < ./old/db/rsgbbgt/oracle/create_rsgb_bgt.sql

# update geotools metadata
sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.26:15210/XE <<< "update GEOMETRY_COLUMNS set F_TABLE_SCHEMA = 'JENKINS_RSGB';"
sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.26:15210/XE <<< "update GT_PK_METADATA set TABLE_SCHEMA = 'JENKINS_RSGB';"
