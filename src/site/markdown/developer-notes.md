# Ontwikkelaar notities

## Bouwen

### Vereisten

- Java 17
- Maven 3.9.5 of hoger
- Docker 27.0.x met buildx 0.16.x en compose 2.29.x of hoger (dit vereiste kan worden overgeslagen als je geen 
  docker images wilt bouwen en/of geen release artifacten wilt bouwen)
- Graphviz 2.40.x of hoger (voor het maken van de database documentatie)

### Basisprocedure

1. clone de repository (gebruik bij voorbaat ssh): `git clone git@github.com:B3Partners/brmo.git`
1. maak een branch voor je wijzigingen, met de ticket in de naam, bijvoorbeeld: `git checkout -b BRMO-1234_mijn-wijzigingen` 
1. doe je ding, als het mogelijk is gebruik `google` styling (voer `mvn fmt:format sortpom:sort` uit om alle opmaak te corrigeren)
2. voer `mvn -T1 modernizer:modernizer` uit om te controleren of er geen Java 11 of lagere code-constructies worden gebruikt
2. voer `mvn clean install` uit om te controleren of alle vereiste opmaak is toegepast en of alle tests slagen
2. pas de upgrade scripts aan in de `datamodel/upgrade_scripts` directory voor de versie waarin je wijzigingen komen, 
   bijvoorbeeld in: `upgrade_scripts/3.0.2-4.0.0/postgresql/rsgb.sql` voor de upgrade van 3.0.2 naar 4.0.0 vam het rsgb schema in PostgreSQL 
3. commit en push je branch om een pull request te maken, gebruik de **Nederlandse taal** voor commit messages en pull
   request beschrijvingen zodat we consistente release notes krijgen. De release notes worden gegenereerd uit de titel van
   een pull request. Indien er iets gedaan moet worden (bijvoorbeeld views droppen oid.) bij een upgrade naar de 
   volgende versie dient de PR deze procedure te beschrijven. Zorg dat de Jira issue is vermeld in de PR titel.
   Vergeet niet om eventuele nieuwe scripts op te nemen in de CI en deployment scripts
5. wacht op het doorlopen van de Q&A procedures en volledige CI, pas eventueel je PR aan
6. wacht op het doorlopen van de code review, pas eventueel je PR aan en merge je PR
7. update de upgrade instructies op de wiki: https://github.com/B3Partners/brmo/wiki/Upgrade-Instructies voor de nieuwe 
   versie waarin je wijzigingen komen. Het is handig om (ook) de pull request beschrijving te gebruiken voor de upgrade instructies
8. update, indien nodig, de installatie handleiding op de wiki: https://github.com/B3Partners/brmo/wiki/Installatiehandleiding

## release maken

Een release bouwen begint met het uitvoeren van het commando `mvn clean release:prepare`
daarbij wordt voor de verschillende artifacten om versienummers gevraagd (evt. de
optie `-DautoVersionSubmodules=true` gebruiken als alles dezelfde versie moet krijgen),
zowel voor voor de release als de volgende ontwikkel versie.
Tevens wordt er om een naam voor een tag gevraagd. In principe kan alle informatie op de
commandline worden meegegeven, bijvoorbeeld:

```bash
mvn release:prepare -l rel-prepare.log -DautoVersionSubmodules=true -DdevelopmentVersion=6.0.1-SNAPSHOT -DreleaseVersion=6.0.0 -Dtag=v6.0.0 -T1
mvn release:perform -l rel-perform.log -T1
```

_NB_ Voor het maken van de database documentatie is een draaiende, up-2-date databases met de betreffende RSGB
schema's (public, brk, bag) nodig op `jdbc:postgresql://127.0.0.1:5432/rsgb`, dat kan met onderstaande commando's:
_Zorg dat de tabellen en views zijn aangemaakt (BAG!)._

```bash
export POSTGRES_PASSWORD=postgres
export PGPASSWORD=postgres
.build/ci/pgsql-start-docker.sh 17-3.5-alpine
.build/ci/pgsql-create-databases.sh
.build/ci/pgsql-setup.sh
mvn -e verify -B -Ppostgresql -T1 -Dtest.onlyITs=true -pl 'bag2-loader' -DskipQA=true
.build/ci/pgsql-setup-bag2_views.sh
```

Met het commando `mvn release:perform` wordt daarna, op basis van de tag uit de
stap hierboven, de release gebouwd en gedeployed naar de repository uit de (parent)
pom file. De release bestaat uit docker images en jar en war files met daarin oa. ook de javadoc.
Voor het hele project kan dit even duren, oa. omdat de javadoc gebouwd wordt.

### Maven site bouwen en online brengen

De Maven site voor de BRMO leeft in de `gh-pages` branch van de repository, met onderstaande commando's kan de site
worden bijgewerkt en online gebracht. De SchemaSpy tool waarmee database schema documentatie wordt gemaakt heeft 
Graphviz nodig, installeer met `sudo apt install -y --no-install-recommends graphviz`.

- `cd target/checkout` (als je dit direct na een release doet)
- `mvn -T1 site site:stage -DskipQA=true`
- `mvn scm-publish:publish-scm -T1`

_NB_ de git acties willen wel eens mislukken omdat de commandline te lang wordt; je kunt dan met de hand een commit 
doen van de staged site in jouw temp directory.

### Jira release publiceren

Release de gemaakte versie in Jira: https://b3partners.atlassian.net/projects/BRMO?selectedItem=com.atlassian.jira.jira-projects-plugin%3Arelease-page
en maak evt. de volgende versie aan.

### nieuwe ontwikkel cyclus

Na het maken van de release kun je het script `new-version-upgrades.sh` in de `datamodel/upgrade_scripts` directory
gebruiken om de initiële upgrade scripts voor de volgende release te maken.

```bash
cd datamodel/upgrade_scripts
./new-version-upgrades.sh
git push
```

Begin met de nieuwe upgrade instructies op de wiki: https://github.com/B3Partners/brmo/wiki/Upgrade-Instructies


### git configuratie

Op sommige systemen en bij sommige versies van git moet er eea. worden ingesteld voorafgaand aan het starten van de
release procedure.

```bash
git config --add status.displayCommentPrefix true
export LANG=C
mvn clean install
```

## Integratie en unit tests

Er zijn drie Maven profielen (postgresql, oracle) voor de ondersteunde databases gedefinieerd,
de profielen zorgen ervoor dat de juist JDBC driver beschikbaar komt in de test suites,
tevens kan daarmee de juiste configuratie worden geladen.

| unit tests                                                                                                                                                                                      | integratie tests                                                          |
|-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------|
| Naamgeving conventie `<Mijn>Test.java`                                                                                                                                                          | Naamgeving conventie `<Mijn>IntegrationTest.java`                         |
| Zelfstandige tests, zonder runtime omgeving benodigdheden, eventueel voorzien van een data bestand, maar zonder verdere afhankelijkheden.                                                       | Tests die een database omgeving en/of servlet container nodig hebben.     |
| Unit tests worden onafhankelijk van het gebruikte Maven profiel uitgevoerd, in principe tijdens iedere full build, tenzij er een `skip` optie voor het overslaan van de tests wordt meegegeven. | Unit tests worden afhankelijk van het gebruikte Maven profiel uitgevoerd. |

Het is mogelijk om bepaalde tests uit te sluiten voor een bepaalde omgeving, dat kan mbv. de marker interfaces in
de [`brmo-test-util` module](/brmo/brmo-test-util/index.html).

Bekijk onder `.github/workflow/` hoe de integratie tests worden gestart.

### database configuratie

Voor de verschillende database omgevingen zijn er in bijvoorbeeld de `brmo-service` module,
de `brmo-loader` module property files gemaakt met een
configuratie die gebruikt wordt in de verschillende CI omgevingen. Deze bestanden zijn
in de test resources te vinden. Lokaal kun je een override definieren voor een bepaalde
omgeving door een bestand naast het bestaande te zetten met de naam `local.<DB smaakje>.properties`.

De te gebruiken database smaak wordt middels de `database.properties.file` property in de pom.xml van de
module of via commandline ingesteld.

| property file       | gebruikt op | override                  |
|---------------------|-------------|---------------------------|
| postgres.properties | Github      | local.postgres.properties |
| oracle.properties   | Github      | local.oracle.properties   |

Voor gebruik van de property file in een integratie test kun je overerven van een
abstracte klasse in verschillende modules.

| module         | klasse                                   |
|----------------|------------------------------------------|
| brmo-loader    | `nl.b3p.AbstractDatabaseIntegrationTest` |
| brmo-service   | `nl.b3p.web.TestUtil`                    |
| brmo-stufbg204 | `nl.b3p.brmo.stufbg204.TestStub`         |

### servlet container configuratie

Voor de brmo-service module is een voorbeeld beschikbaar in de klasse `nl.b3p.web.IndexPageIntegrationTest`

