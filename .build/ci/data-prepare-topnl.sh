#!/usr/bin/env bash
set -e

# ophalen topnl test data
wget --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "https://api.pdok.nl/brt/top10nl/download/v1_0/extract/fae0cf64-f1f4-4726-a2cd-56988c12a67c/extract.zip" --output-document=./TOP10NL.zip
wget --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "https://api.pdok.nl/brt/top50nl/download/v1_0/extract/920d9e8e-a235-4dc0-b66c-1f36e9ab1477/extract.zip" --output-document=./TOP50NL.zip
wget --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "https://api.pdok.nl/brt/top100nl/download/v1_0/full/predefined/top100nl-gml-nl-nohist.zip" --output-document=./TOP100NL.zip
wget --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "https://api.pdok.nl/brt/top250nl/download/v1_0/full/predefined/top250nl-gml-nl-nohist.zip" --output-document=./TOP250NL.zip

# uitpakken in de /tmp dir
ls *.zip | while read filename; do unzip -j -o -d "/tmp" "$filename" *.gml; done;
# lowercase alle gml files uit de zips
for f in /tmp/*.gml ; do mv -vn -- "$f" "$(tr '[:upper:]' '[:lower:]' <<< "$f")" ; done
# van ieder alleen de eerste 5 features
for f in /tmp/top250nl_*.gml ; do xmlstarlet transform .build/ci/data-prepare-top250nl.xsl "$f" > "brmo-loader/src/test/resources/topnl/${f##*/}"; done
for f in /tmp/top100nl_*.gml ; do xmlstarlet transform .build/ci/data-prepare-top100nl.xsl "$f" > "brmo-loader/src/test/resources/topnl/${f##*/}"; done
for f in /tmp/top50nl_*.gml ; do xmlstarlet transform .build/ci/data-prepare-top50nl.xsl "$f" > "brmo-loader/src/test/resources/topnl/${f##*/}"; done
for f in /tmp/top10nl_*.gml ; do xmlstarlet transform .build/ci/data-prepare-top10nl.xsl "$f" > "brmo-loader/src/test/resources/topnl/${f##*/}"; done

