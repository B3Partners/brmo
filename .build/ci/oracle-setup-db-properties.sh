#!/usr/bin/env bash
set -e

cp -f .build/ci/oracle/github.oracle.brmo-loader.properties ./brmo-loader/src/test/resources/local.oracle.properties
cp -f .build/ci/oracle/github.oracle.brmo-service.properties ./brmo-service/src/test/resources/local.oracle.properties
cp -f .build/ci/oracle/github.connections.brmo-persistence.properties ./brmo-persistence/src/test/resources/local.connections.properties
cp -f .build/ci/oracle/github.oracle.brmo-soap.properties ./brmo-soap/src/test/resources/local.oracle.properties
cp -f .build/ci/oracle/github.oracle.brmo-stufbg204.properties ./brmo-stufbg204/src/test/resources/local.oracle.properties
cp -f .build/ci/oracle/github.oracle.datamodel.properties ./datamodel/src/test/resources/local.oracle.properties
cp -f .build/ci/oracle/github.oracle.brmo-commandline.properties ./brmo-commandline/src/test/resources/oracle.properties