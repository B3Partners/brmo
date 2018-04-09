#!/bin/bash

# set up rsgb tabellen
ls -l ./datamodel/generated_scripts/
sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.41:1521/DB01 < ./datamodel/generated_scripts/datamodel_oracle.sql
sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.41:1521/DB01 < ./datamodel/utility_scripts/oracle/111a_update_gemeente_geom.sql
sqlplus -l -S jenkins_rsgb/jenkins_rsgb@192.168.1.41:1521/DB01 < ./datamodel/utility_scripts/oracle/113a_update_wijk_geom.sql
