#!/usr/bin/env bash

# ophalen topnl test data
wget --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "http://geodata.nationaalgeoregister.nl/top10nlv2/extract/kaartbladen/TOP10NL_07W.zip?formaat=gml" --output-document=./TOP10NL_07W.zip
wget --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "http://geodata.nationaalgeoregister.nl/top50nl/extract/kaartbladen/TOP50NL_07W.zip?formaat=gml" --output-document=./TOP50NL_07W.zip
wget --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "http://geodata.nationaalgeoregister.nl/top100nl/extract/chunkdata/top100nl_gml_filechunks.zip?formaat=gml" --output-document=./TOP100NL.zip
wget --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "http://geodata.nationaalgeoregister.nl/top250nl/extract/kaartbladtotaal/top250nl.zip?formaat=gml" --output-document=./TOP250NL.zip
# uitpakken in de /tmp dir
ls *.zip | while read filename; do unzip -j -o -d "/tmp" "$filename" *.gml; done;
# lowercase alle gml files uit de zips
for f in /tmp/*.gml ; do mv -- "$f" "$(tr [:upper:] [:lower:] <<< "$f")" ; done
# van ieder alleen de eerste 5 features
xmlstarlet transform .jenkins/data-prepare-top250nl.xsl /tmp/Top250NL.gml > brmo-loader/src/test/resources/topnl/TOP250NL.gml
xmlstarlet transform .jenkins/data-prepare-top100nl.xsl /tmp/Top100NL_000001.gml > brmo-loader/src/test/resources/topnl/Top100NL_000001.gml
xmlstarlet transform .jenkins/data-prepare-top50nl.xsl /tmp/Top50NL_07W.gml > brmo-loader/src/test/resources/topnl/Top50NL_07W.gml
xmlstarlet transform .jenkins/data-prepare-top10nl.xsl /tmp/TOP10NL_07W.gml > brmo-loader/src/test/resources/topnl/TOP10NL_07W.gml

