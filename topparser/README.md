# topparser

Parser voor het inlezen van top10nl, top50nl, top100nl en top250 in een rsgb database.

# Nieuw schema inladen

1. Download xsd naar brmo/topparser/src/main/resources/nl/b3p/topnl/<versie>/standaardGML/<versie>.xsd
2. Wijs naar lokale GML en BRT xsd's
3. Sla xsd plat: run brmo/topparser/src/main/resources/nl/b3p/topnl/<versie>/standaardGML/XSD-Resolver/XSD-Resolver.sh
4. Doe aanpassingen om xsd te herstellen:
   1. NillReasonEnumeration toevoegen aan gml xsd
   2. Voor top250nl: featuretypemember fixen (zie  https://github.com/B3Partners/brmo/commit/a3a0915f497bd0fb1dc8f1b280227949d2820c05)

5. Genereer jaxb classes: run: brmo/topparser/src/main/resources/nl/b3p/topnl/<versie>/standaardGML/XSD-Resolver/finalized/convertToJava.sh

[![Build Status](https://travis-ci.org/B3Partners/topparser.svg?branch=master)](https://travis-ci.org/B3Partners/topparser)
