#!/usr/bin/env bash

for f in *.xml; do
  echo "posting $f"
  xmlstarlet transform ./soapify.xsl $f > /tmp/$f
  curl -H "Content-Type: text/xml" --data-binary @/tmp/$f http://localhost:8080/brmo-stufwoz312/OntvangAsynchroon
done

