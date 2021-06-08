#!/usr/bin/env bash

for f in *.xml; do
  echo "posting $f naar brmo-service"
  curl -H "Content-Type: text/xml" --data-binary @"$f" http://localhost:8080/brmo-service/post/woz
done

