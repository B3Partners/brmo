#!/bin/bash
./remove.sh
java -Xmx1024m -jar saxon/saxon9he.jar -o:generated/temp.xml -s:input/configuration.xml -xsl:xslt/resolveXSDs.xslt versie=