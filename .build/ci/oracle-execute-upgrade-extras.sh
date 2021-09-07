#!/usr/bin/env bash
CURSNAPSHOT=$(grep "<version>.*<.version>" -m1 pom.xml | sed -e "s/^.*<version/<version/" | cut -f2 -d">"| cut -f1 -d"<")

echo "Huidige snapshot:" $CURSNAPSHOT
echo "Verwerk extra upgrade scripts voor: " $1

#!/usr/bin/env bash
export SQLPATH=./.build/ci

#if [ $CURSNAPSHOT = "2.0.0-SNAPSHOT" ] && [ $1 = "rsgb" ]
#then
#    docker exec -i oracle_brmo sqlplus -l -S jenkins_$1/jenkins_$1@//localhost:1521/XE < ./datamodel/extra_scripts/oracle/206_bag_views.sql
#    docker exec -i oracle_brmo sqlplus -l -S jenkins_$1/jenkins_$1@//localhost:1521/XE < ./datamodel/extra_scripts/oracle/207_brk_views.sql
#fi