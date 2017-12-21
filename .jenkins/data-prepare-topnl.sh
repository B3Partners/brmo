#!/bin/bash

# ophalen topnl test data
wget --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "http://geodata.nationaalgeoregister.nl/top10nlv2/extract/kaartbladen/TOP10NL_07W.zip?formaat=gml" --output-document=./TOP10NL_07W.zip
wget --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "http://geodata.nationaalgeoregister.nl/top50nl/extract/kaartbladen/TOP50NL_07W.zip?formaat=gml" --output-document=./TOP50NL_07W.zip
wget --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "http://geodata.nationaalgeoregister.nl/top100nl/extract/chunkdata/top100nl_gml_filechunks.zip?formaat=gml" --output-document=./TOP100NL.zip
wget --no-verbose --tries=5 --timeout=60 --waitretry=300 --user-agent="" "http://geodata.nationaalgeoregister.nl/top250nl/extract/kaartbladtotaal/top250nl.zip?formaat=gml" --output-document=./TOP250NL.zip
ls *.zip | while read filename; do unzip -j -o -d "brmo-loader/src/test/resources/topnl/" "$filename" *.gml; done;
