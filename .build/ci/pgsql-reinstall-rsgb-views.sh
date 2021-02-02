export PGPASSWORD=${rsgb.username}
psql -b -U ${rsgb.username} -d ${rsgb.database} -h ${rsgb.host} -p ${rsgb.port} -f ./.build/ci/pgsql-drop-rsgb-views.sql
psql -b -U ${rsgb.username} -d ${rsgb.database} -h ${rsgb.host} -p ${rsgb.port} -f ./datamodel/extra_scripts/postgresql/206_bag_views.sql
psql -b -U ${rsgb.username} -d ${rsgb.database} -h ${rsgb.host} -p ${rsgb.port} -f ./datamodel/extra_scripts/postgresql/207_brk_views.sql
psql -b -U ${rsgb.username} -d ${rsgb.database} -h ${rsgb.host} -p ${rsgb.port} -f ./.build/ci/pgsql-refresh-rsgb-mviews.sql