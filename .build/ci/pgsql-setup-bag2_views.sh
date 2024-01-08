#!/usr/bin/env bash
set -e

printf "\nset up BAG views...\n"
psql -U postgres -h localhost -w -d rsgb -f ./datamodel/extra_scripts/postgresql/208_bag2_views.sql
printf "\nset up RSGB BAG views...\n"
psql -U postgres -h localhost -w -d rsgb -f ./datamodel/extra_scripts/postgresql/209_bag2_rsgb_views.sql
printf "\nset up RSGB BAG and BRK materialized views...\n"
psql -U postgres -h localhost -w -d rsgb -f ./datamodel/extra_scripts/postgresql/210_bag2_brk2.0_mat_views.sql