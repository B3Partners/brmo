#!/bin/bash
rm -Rf ../../../../../../../../java/nl/b3p/topnl/top250nl
xjc -p nl.b3p.topnl.top250nl -b bindings.xml -d ../../../../../../../../java/ top250nl_resolved.xsd