#!/bin/bash
rm -Rf ../../../../../../../../java/nl/b3p/topnl/top100nl
xjc -p nl.b3p.topnl.top100nl -b bindings.xml -d ../../../../../../../../java/ top100nl_resolved.xsd