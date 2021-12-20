#!/usr/bin/env bash
set -e
psql -U postgres -h localhost -d rsgb -c 'DROP SCHEMA bag CASCADE;'