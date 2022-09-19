#!/usr/bin/env bash
set -e

# ophalen topnl test data
wget --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "https://api.pdok.nl/brt/top250nl/download/v1_0/full/predefined/top250nl-gml-nl-nohist.zip" --output-document=./TOP250NL.zip

# curl -X 'GET' 'https://api.pdok.nl/brt/top10nl/download/v1_0/dataset' -H 'accept: application/json' | jq '.timeliness[].featuretype'
TOP10NL_BODY_JSON='{
             "featuretypes": [
               "functioneelgebied",
               "gebouw",
               "geografischgebied",
               "hoogte",
               "inrichtingselement",
               "plaats",
               "plantopografie",
               "registratiefgebied",
               "relief",
               "spoorbaandeel",
               "terrein",
               "waterdeel",
               "wegdeel"
             ],
             "format": "gml",
             "geofilter": "POLYGON((130011 458031, 130011 459995, 132703 459995, 132703 458031, 130011 458031))"
           }'

# # curl -X 'GET' 'https://api.pdok.nl/brt/top50nl/download/v1_0/dataset' -H 'accept: application/json' | jq '.timeliness[].featuretype'
# ook voor top100nl
TOPNL_BODY_JSON='{
             "featuretypes": [
               "functioneelgebied",
               "gebouw",
               "hoogte",
               "inrichtingselement",
               "registratiefgebied",
               "relief",
               "spoorbaandeel",
               "terrein",
               "waterdeel",
               "wegdeel"
             ],
             "format": "gml",
             "geofilter": "POLYGON((130011 458031, 130011 459995, 132703 459995, 132703 458031, 130011 458031))"
           }'

SLEEPTIME=10
API_URL="https://api.pdok.nl"
TOP10NL_URL="/brt/top10nl/download/v1_0/full/custom"
TOP50NL_URL="/brt/top50nl/download/v1_0/full/custom"
TOP100NL_URL="/brt/top100nl/download/v1_0/full/custom"
STATUS=-1

echo "Requesting top10nl full custom extract"
while [ STATUS != 202 ]; do
  STATUS=$(curl -s -w "\\n%{http_code}" -X "POST" -H "Content-Type: application/json" -H "accept: application/json" -d "${TOP10NL_BODY_JSON}" "${API_URL}${TOP10NL_URL}")
  BODY=$(echo "$STATUS" | head -n 1)
  STATUS=$(echo "$STATUS" | tail -n 1)
  if [ "$STATUS" == "202" ]; then
    break
  fi
  sleep $SLEEPTIME
done

STATUS_URL=$(echo "$BODY" | jq -r '._links.status.href')
while [ STATUS != 201 ]; do
  echo "Waiting for extract to be ready"
  STATUS=$(curl -s -w "\\n%{http_code}" -X "GET" -H "Content-Type: application/json" -H "accept: application/json" "${API_URL}${STATUS_URL}")
  BODY=$(echo "$STATUS" | head -n 1)
  STATUS=$(echo "$STATUS" | tail -n 1)
  if [ "$STATUS" == "201" ]; then
    break
  fi
  sleep $SLEEPTIME
done

DOWNLOAD_URL=$(echo "$BODY" | jq -r '._links.download.href')
echo "top10nl download URL: $DOWNLOAD_URL"
wget --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "${API_URL}${DOWNLOAD_URL}" --output-document=./TOP10NL.zip


echo "Requesting top50nl full custom extract"
while [ STATUS != 202 ]; do
  STATUS=$(curl -s -w "\\n%{http_code}" -X "POST" -H "Content-Type: application/json" -H "accept: application/json" -d "${TOPNL_BODY_JSON}" "${API_URL}${TOP50NL_URL}")
  BODY=$(echo "$STATUS" | head -n 1)
  STATUS=$(echo "$STATUS" | tail -n 1)
  if [ "$STATUS" == "202" ]; then
    break
  fi
  sleep $SLEEPTIME
done

STATUS_URL=$(echo "$BODY" | jq -r '._links.status.href')
while [ STATUS != 201 ]; do
  echo "Waiting for extract to be ready"
  STATUS=$(curl -s -w "\\n%{http_code}" -X "GET" -H "Content-Type: application/json" -H "accept: application/json" "${API_URL}${STATUS_URL}")
  BODY=$(echo "$STATUS" | head -n 1)
  STATUS=$(echo "$STATUS" | tail -n 1)
  if [ "$STATUS" == "201" ]; then
    break
  fi
  sleep $SLEEPTIME
done

DOWNLOAD_URL=$(echo "$BODY" | jq -r '._links.download.href')
echo "top50nl download URL: $DOWNLOAD_URL"
wget --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "${API_URL}${DOWNLOAD_URL}" --output-document=./TOP50NL.zip


echo "Requesting top100nl full custom extract"
while [ STATUS != 202 ]; do
  STATUS=$(curl -s -w "\\n%{http_code}" -X "POST" -H "Content-Type: application/json" -H "accept: application/json" -d "${TOPNL_BODY_JSON}" "${API_URL}${TOP100NL_URL}")
  BODY=$(echo "$STATUS" | head -n 1)
  STATUS=$(echo "$STATUS" | tail -n 1)
  if [ "$STATUS" == "202" ]; then
    break
  fi
  sleep $SLEEPTIME
done

STATUS_URL=$(echo "$BODY" | jq -r '._links.status.href')
while [ STATUS != 201 ]; do
  echo "Waiting for extract to be ready"
  STATUS=$(curl -s -w "\\n%{http_code}" -X "GET" -H "Content-Type: application/json" -H "accept: application/json" "${API_URL}${STATUS_URL}")
  BODY=$(echo "$STATUS" | head -n 1)
  STATUS=$(echo "$STATUS" | tail -n 1)
  if [ "$STATUS" == "201" ]; then
    break
  fi
  sleep $SLEEPTIME
done

DOWNLOAD_URL=$(echo "$BODY" | jq -r '._links.download.href')
echo "top100nl download URL: $DOWNLOAD_URL"
wget --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "${API_URL}${DOWNLOAD_URL}" --output-document=./TOP100NL.zip


# uitpakken in de /tmp dir
ls *.zip | while read filename; do unzip -j -o -d "/tmp" "$filename" *.gml; done;
# lowercase alle gml files uit de zips
for f in /tmp/*.gml ; do mv -vn -- "$f" "$(tr '[:upper:]' '[:lower:]' <<< "$f")" ; done
# van ieder alleen de eerste 5 features
for f in /tmp/top250nl_*.gml ; do xmlstarlet transform .build/ci/data-prepare-top250nl.xsl "$f" > "brmo-loader/src/test/resources/topnl/${f##*/}"; done
for f in /tmp/top100nl_*.gml ; do xmlstarlet transform .build/ci/data-prepare-top100nl.xsl "$f" > "brmo-loader/src/test/resources/topnl/${f##*/}"; done
for f in /tmp/top50nl_*.gml ; do xmlstarlet transform .build/ci/data-prepare-top50nl.xsl "$f" > "brmo-loader/src/test/resources/topnl/${f##*/}"; done
for f in /tmp/top10nl_*.gml ; do xmlstarlet transform .build/ci/data-prepare-top10nl.xsl "$f" > "brmo-loader/src/test/resources/topnl/${f##*/}"; done

