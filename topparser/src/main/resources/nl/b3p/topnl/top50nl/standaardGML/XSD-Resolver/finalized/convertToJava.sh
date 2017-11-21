#!/bin/bash
rm -Rf ../../../../../../../../java/nl/b3p/topnl/top50nl
xjc -p nl.b3p.topnl.top50nl -b bindings.xml -d ../../../../../../../../java/ top50nl_1_1_resolved.xsd