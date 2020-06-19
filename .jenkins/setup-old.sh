#!/usr/bin/env bash
# set up staging db
export SQLPATH=./.jenkins
sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.26:15210/XE < ./old/db/staging/create-brmo-persistence-oracle.sql
# TODO opschonen na 2.0.2 release
sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.26:15210/XE < ./old/db/staging/02_insert_default_user.sql
sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.26:15210/XE < ./old/db/staging/04_create_triggers_oracle.sql
sqlplus -l -S jenkins_staging/jenkins_staging@192.168.1.26:15210/XE < ./old/db/staging/05_create_brmo_metadata_oracle.sql

# set up rsgb tabellen
sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.26:15210/XE < ./old/db/rsgb/datamodel_oracle.sql

# set up rsgbbgt tabellen
sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.26:15210/XE < ./old/db/rsgbbgt/oracle/create_rsgb_bgt.sql

# update geotools metadata
sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.26:15210/XE <<< "update GEOMETRY_COLUMNS set F_TABLE_SCHEMA = 'JENKINS_RSGB';"
sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.26:15210/XE <<< "update GT_PK_METADATA set TABLE_SCHEMA = 'JENKINS_RSGB';"
