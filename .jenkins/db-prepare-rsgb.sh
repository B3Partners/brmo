#!/bin/bash

# set up rsgb tabellen
ls -l ./datamodel/generated_scripts/
export SQLPATH=./.jenkins
sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.11:1521/ORCL < ./datamodel/generated_scripts/datamodel_oracle.sql
# sqlplus heeft een max. regel lengte van 2499 char, onderstaande scripts gaan uit van 32767 (max string lengte) dus die doen het niet
# sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.11:1521/ORCL < ./datamodel/utility_scripts/oracle/111a_update_gemeente_geom.sql
# sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.11:1521/ORCL < ./datamodel/utility_scripts/oracle/113a_update_wijk_geom.sql

# update geotools metadata
sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.11:1521/ORCL <<< "update GEOMETRY_COLUMNS set F_TABLE_SCHEMA = 'JENKINS_RSGB';"
sqlplus -l -S jenkins_rsgbbgt/jenkins_rsgbbgt@192.168.1.11:1521/ORCL <<< "update GT_PK_METADATA set TABLE_SCHEMA = 'JENKINS_RSGB';"