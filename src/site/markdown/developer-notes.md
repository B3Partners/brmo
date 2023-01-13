# Ontwikkelaar notities

## Bouwen

### Vereisten

- Java 11
- Maven 3.8.4 of hoger
- Docker 20.10.x met buildx 0.9 of hoger (dit vereiste kan worden overgeslagen als u geen docker images wilt bouwen of release artifacts wilt bouwen)

### Basisprocedure

1. doe je ding, als het mogelijk is gebruik `aosp` styling (voer `mvn com.coveo:fmt-maven-plugin:format` uit om alle opmaak te corrigeren)
2. voer `mvn clean install` uit om te controleren of alle vereiste opmaak is toegepast en of alle tests slagen
3. commit en push je branch om een pull request te maken
4. wacht op het doorlopen van de Q&A procedures en volledige CI, pas eventueel je PR aan
4. wacht op het doorlopen van de code review, pas eventueel je PR aan en merge je PR


## release maken

Een release bouwen begint met het uitvoeren van het commando `mvn clean release:prepare`
daarbij wordt voor de verschillende artifacten om versienummers gevraagd (evt. de
optie `-DautoVersionSubmodules=true` gebruiken als alles dezelfde versie moet krijgen),
zowel voor voor de release als de volgende ontwikkel versie.
Tevens wordt er om een naam voor een tag gevraagd. In principe kan alle informatie op de
commandline worden meegegeven, bijvoorbeeld:

```
mvn release:prepare -l rel-prepare.log -DautoVersionSubmodules=true -DdevelopmentVersion=3.0.1-SNAPSHOT -DreleaseVersion=3.0.0 -Dtag=v3.0.0 -T1
mvn release:perform -l rel-perform.log -T1
```
_NB_ Voor het maken van de database documentatie is een draaienede database met de betreffende schema's 
nodig op `jdbc:postgresql://127.0.0.1:5432/rsgb`.

Met het commando `mvn release:perform` wordt daarna, op basis van de tag uit de
stap hierboven, de release gebouwd en gedeployed naar de repository uit de (parent)
pom file. De release bestaat uit jar en war files met daarin oa. ook de javadoc.
Voor het hele project kan dit even duren, oa. omdat de javadoc gebouwd wordt.

### Maven site bouwen en online brengen

De Maven site voor de BRMO leeft in de `gh-pages` branch van de repository, met onderstaande commando's kan de site worden bijgwerkt en online gebracht.

- `cd target/checkout` (als je dit direct na een release doet)
- `mvn -T1 site site:stage`
- `mvn scm-publish:publish-scm -T1`

_NB_ de git acties willen wel eens mislukken omdat de commandline te lang wordt

### nieuwe ontwikkel cyclus

Na het maken van de release kun je het script `new-version-upgrades.sh` in de `datamodel/upgrade_scripts` directory gebruiken om upgrade scripts voor de volgende release te maken.

```
cd datamodel/upgrade_scripts
./new-version-upgrades.sh
git push
```


### git configuratie

Op sommige systemen en bij sommige versies van git moet er eea. worden ingesteld voorafgaand aan het starten van de release procedure.

```
git config --add status.displayCommentPrefix true
export LANG=C
mvn clean release:prepare
```

## Integratie en unit tests

Er zijn drie Maven profielen (postgresql, oracle) voor de ondersteunde databases gedefinieerd,
de profiele zorgen ervoor dat de juist JDBC driver beschikbaar komt in de test suites,
tevens kan daarmee de juiste configuratie worden geladen.

| unit tests | integratie tests |
| ---------- | -----------------|
|Naamgeving conventie `<Mijn>Test.java`  |Naamgeving conventie `<Mijn>IntegrationTest.java`  |
|Zelfstandige tests, zonder runtime omgeving benodigdheden, eventueel voorzien van een data bestand, maar zonder verdere afhankelijkheden.  |Tests die een database omgeving en/of servlet container nodig hebben.  |
|Unit tests worden onafhankelijk van het gebruikte Maven profiel uitgevoerd, in principe tijdens iedere full build, tenzij er een `skip` optie voor het overslaan van de tests wordt meegegeven.  |Unit tests worden afhankelijk van het gebruikte Maven profiel uitgevoerd.  |

Het is mogelijk om bepaalde tests uit te sluiten voor een bepaalde omgeving, dat kan mbv. de marker interfaces in de [`brmo-test-util` module](/brmo/brmo-test-util/index.html).

Bekijk de `.github/workflow/` en `Jenkinsfile` hoe de integratie tests worden gestart.

### database configuratie

Voor de verschillende database omgevingen zijn er in bijvoorbeeld de `brmo-service` module,
de `brmo-loader` module property files gemaakt met een
configuratie die gebruikt wordt in de verschillende CI omgevingen. Deze bestanden zijn 
in de test resources te vinden. Lokaal kun je een override definieren voor een bepaalde
omgeving door een bestand naast het bestaande te zetten met de naam `local.<DB smaakje>.properties`.

De te gebruiken database smaak wordt middels de `database.properties.file` property in de pom.xml van de
module of via commandline ingesteld.

| property file       | gebruikt op | override                  |
| ------------------- |-------------| ------------------------- |
|postgres.properties  | Github      |local.postgres.properties  |
|oracle.properties    | Jenkins     |local.oracle.properties    |

Voor gebruik van de propertyfile in een integratie test kun je overerven van een
abstracte klasse in verschillende modules.

| module         | klasse                                  |
| -------------- | --------------------------------------- |
|brmo-loader     |`nl.b3p.AbstractDatabaseIntegrationTest` |
|brmo-service    |`nl.b3p.web.TestUtil`                    |
|brmo-soap       |`nl.b3p.brmo.soap.db.TestUtil`           |
|brmo-stufbg204  |`nl.b3p.brmo.stufbg204.TestStub`         |


### servlet container configuratie

Voor de brmo-service module is een voorbeeld beschikbaar in de klasse `nl.b3p.web.IndexPageIntegrationTest`

