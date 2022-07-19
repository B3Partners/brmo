# brmo-topnl-loader

Parser voor het inlezen van top10nl, top50nl, top100nl en top250 in een rsgb database.

# Nieuw schema inladen

1. Download xsd naar `brmo/brmo-topnl-loader/src/main/resources/nl/b3p/topnl/<versie>/standaardGML/<versie>.xsd`
2. Wijs naar lokale GML en BRT xsd's door de schemalocation aan te passen
2. Pas de `configuration.xml` aan voor de nieuwe xsd
3. Sla de xsd plat met de XSDresolver: run brmo/brmo-topnl-loader/src/main/resources/nl/b3p/topnl/<versie>/standaardGML/XSD-Resolver/XSD-Resolver.sh
4. Doe aanpassingen om xsd te herstellen:
   1. NillReasonEnumeration toevoegen aan gml xsd
   2. Voor top250nl: featuretypemember fixen (zie  https://github.com/B3Partners/brmo/commit/a3a0915f497bd0fb1dc8f1b280227949d2820c05)
   3. voor de top50 ook featuretypemember fixen

5. Genereer jaxb classes: run: `brmo/brmo-topnl-loader/src/main/resources/nl/b3p/topnl/<versie>/standaardGML/XSD-Resolver/finalized/convertToJava.sh`

**Let op**: gebruik xjc versie 3 (evt apart te downloaden via https://github.com/eclipse-ee4j/jaxb-ri/releases) en zorg 
dat de `jakarta` namespace urls en packages worden gebruikt.

run ./convertToJava.sh