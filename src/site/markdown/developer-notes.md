# Developer notes


## release maken

Een release bouwen begint met het uitvoeren van het commando `mvn clean release:prepare`
daarbij wordt voor de verschillende artifacten om versienummers gevraagd (evt. de
optie `-DautoVersionSubmodules=true` gebruiken als alles dezelfde versie moet krijgen),
zowel voor voor de release als de volgende ontwikkel versie.
Tevens wordt er om een naam voor een tag gevraagd. In principe kan alle informatie op de
commandline worden meegegeven, bijvoorbeeld:

```
mvn release:prepare -l rel-prepare.log -DautoVersionSubmodules=true -DdevelopmentVersion=1.4.1-SNAPSHOT -DreleaseVersion=1.4.0 -Dtag=v1.4.0 -T1
mvn release:perform -l rel-perform.log
```

Met het commando `mvn release:perform` wordt daarna, op basis van de tag uit de
stap hierboven, de release gebouwd en gedeployed naar de repository uit de (parent)
pom file. De release bestaat uit jar en war files met daarin oa. ook de javadoc en
mogelijke site.
Voor het hele project kan dit even duren, oa. omdat de javadoc ook gebouwd wordt.


### git configuratie

```
git config --add status.displayCommentPrefix true
export LANG=C
mvn clean release:prepare
```

## Integratie en unit tests

Er zijn drie Maven profielen (postgresql, oracle, mssql) voor de ondersteunde databases gedefinieerd,
de profiele zorgen ervoor dat de juist JDBC driver beschikbaar komt in de test suites,
tevens kan daarmee de juiste configuratie worden geladen.

| unit tests | integratie tests |
| ---------- | -----------------|
|Naamgeving conventie `<Mijn>Test.java`  |Naamgeving conventie `<Mijn>IntegrationTest.java`  |
|Zelfstandige tests, zonder runtime omgeving benodigdheden, eventueel voorzien van een data bestand, maar zonder verdere afhankelijkheden.  |Tests die een database omgeving en/of servlet container nodig hebben.  |
|Unit tests worden onafhankelijk van het gebruikte Maven profiel uitgevoerd, in principe tijdens iedere full build, tenzij er een `skip` optie voor het overslaan van de tests wordt meegegeven.  |Unit tests worden afhankelijk van het gebruikte Maven profiel uitgevoerd.  |

Bekijk de `.travis.yml` en `appveyor.yml` hoe de integratie tests worden gestart.

### database configuratie

Voor de verschillende database omgevingen zijn er in bijvoorbeeld de `brmo-service` module,
de `brmo-loader` module en de `bgt-gml-loader` module property files gemaakt met een
configuratie die gebruikt wordt in de verschillende CI omgevingen. Deze bestanden zijn 
in de test resources te vinden. Lokaal kun je een override definieren voor een bepaalde
omgeving door een bestand naast het bestaande te zetten met de naam `local.<DB smaakje>.properties`.

De te gebruiken database smaak wordt middels de `database.properties.file` property in de pom.xml van de
module of via commandline ingesteld.

| property file       | gebruikt op | override                  |
| ------------------- | ----------- | ------------------------- |
|postgres.properties  |Travis-CI    |local.postgres.properties  |
|sqlserver.properties |AppVeyor     |local.sqlserver.properties |
|oracle.properties    |  <TODO>     |local.oracle.properties    |

Voor gebruik van de propertyfile in een integratie test kun je overerven van een
abstracte klasse in verschillende modules.

| module         | klasse                                  |
| -------------- | --------------------------------------- |
|bgt-gml-loader  |`nl.b3p.brmo.loader.gml.TestingBase`     |
|brmo-loader     |`nl.b3p.AbstractDatabaseIntegrationTest` |
|brmo-service    |`nl.b3p.web.TestUtil`                    |


### servlet container configuratie

Voor de brmo-service module is een voorbeeld beschikbaar in de klasse `nl.b3p.web.IndexPageIntegrationTest`

