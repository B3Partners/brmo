#!/bin/bash
rm -Rf ../../../../../../../../java/nl/b3p/topnl/top10nl
xjc -extension -p nl.b3p.topnl.top10nl -b bindings.xml -d ../../../../../../../../java/ top10nl_resolved.xsd