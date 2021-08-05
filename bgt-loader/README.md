<!--
Copyright (C) 2021 B3Partners B.V.

SPDX-License-Identifier: MIT
-->

# BRMO BGT lader

Met de B3Partners BRMO BGT lader kan de [BGT](https://docs.geostandaarden.nl/imgeo/catalogus/bgt/) (Basisregistratie
Grootschalige Topografie) en de [IMGeo](https://docs.geostandaarden.nl/imgeo/catalogus/imgeo/) (Informatiemodel
Geografie) uitbreidingen snel in een spatial database worden geladen en geüpdate worden door gebruik te maken van de
[PDOK BGT mutatieservice](https://api.pdok.nl/lv/bgt/download/v1_0/ui/). Zie de
[Modeldocumentatie mutatieformaat BGT](https://www.pdok.nl/bgt-mutatie) voor meer informatie.

### Copyright en licentie

Copyright (C) 2021 B3Partners B.V.

Voor de licentievoorwaarden en disclaimer zie [MIT](LICENSES/MIT.txt). Commerciële ondersteuning is te verkrijgen via
[B3Partners](https://www.b3partners.nl).

### Ondersteunde platformen en systeemeisen

Deze applicatie is beschikbaar als [Docker images](https://github.com/B3Partners/brmo/pkgs/container/brmo-bgt-loader).
Deze Linux containers draaien ook op Windows, MacOS en Raspberry Pi (64 bit). Het is niet nodig om het programma te
draaien op hetzelfde systeem als de database, als er via het netwerk verbinding mee kan worden gelegd.

Het programma werkt ook op andere platformen waar minimaal Java 11 beschikbaar is. Momenteel is er geen uitvoerbare JAR
download, maar het is mogelijk deze zelf te compileren vanaf de broncode of te verkrijgen via B3Partners.

Er is ongeveer 1 GB RAM nodig. De BGT bestanden worden "streaming" verwerkt: er is niet meer geheugen nodig voor een
groter gebied.

De BGT kan erg veel schijfruimte vereisen als deze is ingeladen in een database, afhankelijk van de geselecteerde
feature types en het gebied.

## Draaien van de applicatie

Met [Docker](https://www.docker.com):
```shell
docker pull ghcr.io/b3partners/brmo-bgt-loader
docker run -it --rm --network=host ghcr.io/b3partners/brmo-bgt-loader
````

De tool is voornamelijk in het Nederlands, behalve als er mogelijk een foutmelding optreedt. Volledig Engelse teksten
zijn ook beschikbaar door de container te starten met de `-e LC_ALL=en_US` parameter.

Let op dat het uitvoeren van bovenstaand `docker pull` commando nodig is om het image te updaten naar de laatste
versie.

De onderstaande voorbeelden om de applicatie te starten laten het eerste stuk van `docker run` weg. Zet deze altijd voor
het commando wat je uitvoert.

### Compileren

Installeer Maven een draai `mvn package`. Dit bouwt een uitvoerbaar JAR bestand dat uitgevoerd kan worden met:

```shell
java -jar target/bgt-loader-*.jar
````

## Command line

Het programma print nuttige help wanneer deze zonder parameters wordt aangeroepen. Er zijn drie hoofdcommando's:
`schema`, `load` and `download`. In de meeste gevallen is alleen het `download` commando nodig: het downloadt
automatisch de BGT gegevens en maakt de benodigde tabellen aan in de database.

## Database voorbereiding

Een database is vereist voordat de BGT kan worden geladen. De volgende databases worden ondersteund:
- [PostgreSQL](https://www.postgresql.org/) met [PostGIS](https://www.postgis.org/), versie 9.6 met PostGIS 2.5 t/m versie 13 met PostGIS 3.1
- Microsoft SQL Server 2019
- Oracle Spatial 18g

Andere versies werken mogelijk ook.

Gebruik bij voorkeur PostGIS, deze is verreweg het snelst.

### PostGIS

Installeer PostgreSQL en PostGIS. Dit kan ook heel snel met een Docker image!

Maak een PostgreSQL gebruiker, typ 'bgt' als wachtwoord:
```shell
createuser bgt --password bgt -P
```
Maak een database:
```shell
createdb --owner=bgt bgt
psql bgt -c 'create extension postgis'
```
Let op: als je een andere tablespace wil gebruiken, maak deze nu, zet deze als default voor de `bgt` database en grant
privileges aan de `bgt` gebruiker op dit moment.

Met de database op je lokale computer en de database naam, gebruiker en wachtwoord allemaal ingesteld op `bgt`, kan het
programma met de standaard opties gebruikt worden om met de database te verbinden. Het is mogelijk om andere gegevens op
te geven, zie de uitvoer van `brmo-bgt-loader download initial --help` voor details.

Staat de database op een andere server, bijvoorbeeld met IP adres `10.0.0.1`? Gebruik dan de volgende connection string:
`jdbc:postgresql://10.0.0.1:5432/bgt?sslmode=disable&reWriteBatchedInserts=true`. Alles na het vraagteken kan weggelaten 
worden maar is bevorderlijk voor de snelheid.

### Microsoft SQL Server

Het is mogelijk om SQL Server zelf te installeren, of deze te starten in een Linux container met Docker (let op dat je
de gebruiksvoorwaarden van Microsoft accepteert):

```shell
docker run --detach -e 'ACCEPT_EULA=Y' -e 'MSSQL_SA_PASSWORD=Password12!' --publish 1433:1433 --name mssql mcr.microsoft.com/mssql/server:2019-latest
docker exec -i mssql /opt/mssql-tools/bin/sqlcmd -S localhost -U SA -P "Password12!" -Q "create database bgt; create login bgt with password = 'bgt@1234', default_database=bgt; alter authorization on database::bgt to bgt"
```

Let op dat SQL Server niet het wachtwoord `bgt` accepteert als sterk genoeg wachtwoord. Gebruik de `--password="bgt@1234"`
optie bij het uitvoeren van de BGT lader om het wachtwoord op te geven. Het is ook nodig om de `--connection` optie op
te geven om met SQL server te verbinden.

Voorbeeldcommando om alleen een paar feature types te laden in SQL Server:

```shell
brmo-bgt-loader download initial --connection="jdbc:sqlserver://localhost:1433;databaseName=bgt" --password="bgt@1234" --feature-types=wijk,buurt --no-geo-filter
```
Vergeet niet om de Docker container te stoppen als je deze niet meer nodig hebt met het `docker stop mssql` commando.
Mogelijk wil je de container ook weer verwijderen met `docker rm mssql`.

### Oracle Spatial

Het is mogelijk om Oracle Spatial zelf te installeren, of deze te starten in een Linux container met Docker (let op dat
je de gebruiksvoorwaarden van Oracle accepteert):

Onderstaand commando gebruikt ter illustratie een onofficieel image.

```shell
docker run --detach --publish 1521:1521 --name oracle-xe -d pvargacl/oracle-xe-18.4.0:latest
```
Wacht totdat Oracle is gestart en voer het volgende uit om een schema en gebruiker aan te maken (met bash syntax):
```shell
{ 
echo "create user c##bgt identified by bgt default tablespace users temporary tablespace temp;"; 
echo "alter user c##bgt quota unlimited on users;";
echo "grant connect to c##bgt;";
echo "grant resource to c##bgt;";
echo "alter user c##bgt default role connect, resource;"; 
} | docker exec -i oracle-xe sqlplus -l system/oracle@//localhost:1521/XE
```

Het is nodig om de `--connection` en `--user` opties aan de BGT lader mee te geven om met Oracle Spatial te verbinden.

Voorbeeldcommando om alleen een paar feature types te laden in Oracle Spatial:

```shell
brmo-bgt-loader download initial --connection="jdbc:oracle:thin:@localhost:1521:XE" --user="c##bgt" --feature-types=wijk,buurt --no-geo-filter
```

Vergeet niet om de Docker container te stoppen als je deze niet meer nodig hebt met het `docker stop oracle-xe` commando.
Mogelijk wil je de container ook weer verwijderen met `docker rm oracle-xe`.

## Laden van de BGT

Het is mogelijk om de hele BGT of alleen een aantal objecttypen (tabellen) of gebied in te laden.

### Laden van de hele BGT

Om de hele BGT in te laden is voldoende schijfruimte nodig! Let op dat je voldoende ruimte heb en voer uit:
```shell
brmo-bgt-loader download initial --no-geo-filter
```
Dit commando downloadt de zogenaamde "predefined" download. Het is dus niet nodig om te wachten totdat de PDOK
mutatieservice een extract heeft gemaakt.

Plaatsbepalingspunten worden standaard overgeslagen. Zie onder bij "Filteren op objecttypen" hoe deze ook kunnen worden 
geladen.

### Filteren op geografisch gebied

Om alleen een gebied in te laden moet een polygoon WKT formaat in het Rijksdriehoekstelsel (RD) coordinatenstelsel
(EPSG:28992) worden opgegeven. Er kunnen ook objecten buiten het geselecteerde gebied worden ingeladen -- zie voor 
details de documentatie van de PDOK mutatieservice.

Een polygoon kan worden opgegeven met bijvoorbeeld de optie `--geo-filter="Polygon ((131021 458768, 131021 459259, 131694 459259, 131694 458768, 131021 458768))"`.

Tip: gebruik het Open Source [QGIS](https://www.qgis.org/) programma: stel de projectie in op EPSG:28992, gebruik de 
PDOK plugin om een achtergrond kaart toe te voegen, teken de polygoon, selecteer deze en kopieer naar het klembord om de 
WKT te verkrijgen. De grens van de polygoon luistert niet precies, omdat de PDOK mutatieservice objecten selecteert op
basis van een grid van 1 bij 1 kilometer. Gebruik dus liever wat minder punten.

Wanneer een geo filter wordt opgegeven, zal de PDOK mutatieservice eerst een extract samenstellen. Wanneer de polygoon
erg veel objecten bevat, kan dit erg lang duren. Als je minder geduld en meer schijfruimte hebt, is het vaak sneller om 
de hele BGT in te laden vanaf een 'predefined' download zoals hierboven beschreven (afhankelijk van de gebruikte 
database en je internetverbinding).

### Filteren op objecttypen

Gebruik de `--feature-types=<waarde>` optie om alleen bepaalde objecttypen te selecteren om in te laden. De speciale waarde `all`
selecteert alle feature types behalve plaatsbepalingspunten (dit is de standaardwaarde als geen optie wordt opgegeven).
Door `bgt` op te geven worden alleen de BGT objecttypen (welke bronhouders verplicht moeten bijhouden) en met `plus`
alleen de optioneel bijgehouden IMGeo objecttypen geselecteerd. Meerdere individuele objecttypenkunnen worden opgegeven
gescheiden door komma's, zoals bijvoorbeeld `--feature-types=pand,openbareruimtelabel`. De beschikbare objecttypen zijn:

| Objecttype              |
|-------------------------|
| bak                     |
| begroeidterreindeel     |
| bord                    |
| buurt                   |
| functioneelgebied       |
| gebouwinstallatie       |
| installatie             |
| kast                    |
| kunstwerkdeel           |
| mast                    |
| onbegroeidterreindeel   |
| ondersteunendwaterdeel  |
| ondersteunendwegdeel    |
| ongeclassificeerdobject |
| openbareruimte          |
| openbareruimtelabel     |
| overbruggingsdeel       |
| overigbouwwerk          |
| overigescheiding        |
| paal                    |
| pand                    |
| plaatsbepalingspunt     |
| put                     |
| scheiding               |
| sensor                  |
| spoor                   |
| stadsdeel               |
| straatmeubilair         |
| tunneldeel              |
| vegetatieobject         |
| waterdeel               |
| waterinrichtingselement |
| waterschap              |
| wegdeel                 |
| weginrichtingselement   |
| wijk                    |

Om alle objecttypen _en_ plaatsbepalingspunten te selecteren, gebruik `--feature-types=all,plaatsbepalingspunt`.

### Historische objecten

Historische objecten worden standaard overgeslagen. Deze zijn ook in te laden met de `--include-history` optie.
Bij historische objecten is een datum ingevuld in de `eindregistratie` kolom en kunnen er meerdere rijen zijn met 
dezelfde `identificatie`.

### Geometrieën met bogen

De BGT bevat op sommige plekken geometrieën met boogstukken (curves). Als je de BGT wil gebruiken met een applicatie die
deze niet begrijpt, kan je de optie `--linearize-curves` opgegeven. De bogen worden dan omgezet in meerdere kleine
rechte stukjes die de boog benaderen.

Geometrieën met bogen worden (nog) niet ondersteund voor Oracle Spatial, hoewel de database deze wel aankan. Deze worden
dus standaard _gelinearized_.

### Het database schema

De BGT objecttypen zijn direct gemapt naar database tabellen, waarbij de namen van attributen zijn aangepast en in het
geval van `identificatie` zijn samengevoegd. De primaire sleutel is het "GML ID" en niet de NEN3610 identificatie,
omdat het GML ID nodig is om mutaties toe te passen.

De meeste geometrie-attributen zijn hernoemd naar `geom`, behalve de kruinlijn geometrieën en enkele andere.

#### Pand

Pand is een bijzonder objecttype omdat deze een nul-tot-veel relatie heeft tot een nummeraanduidingreeks. De lader zet
de nummeraanduidingreeksen in een aparte tabel met een referentie naar de GML ID van het pand waar ze bij horen. Deze
tabel ziet eruit als volgt:

|                Kolom                 |          Type          | Nullable | Opmerking
|--------------------------------------|------------------------|----------|----------
| pandgmlid                            | character varying(255) | not null | Primaire sleutel
| idx                                  | integer                | not null | Volgnummer
| pandeindregistratie                  | boolean                | not null | Niet-NULL als historisch
| tekst                                | character varying(255) | not null |
| hoek                                 | double precision       | not null |
| plaatsingspunt                       | geometry(Point,28992)  |          |
| identificatiebagvbolaagstehuisnummer | character varying(16)  |          |
| identificatiebagvbohoogstehuisnummer | character varying(16)  |          |

#### OpenbareRuimteLabel

Een OpenbareRuimteLabel kan meerdere openbareRuimteNaam attributen hebben. Aangezien OpenbareRuimteLabel zelf niet
veel eigen interessante attributen heeft zoals een geometrie, worden alle openbareRuimteNaam labels met alle kolommen
van OpenbareRuimteLabel in de tabel `openbareruimtelabel` geplaatst. Deze tabel ziet eruit als volgt:

|         Kolom          |            Type             | Nullable | Opmerking
|------------------------|-----------------------------|----------|---------
| gmlid                  | character(32)               | not null | Primaire sleutel
| identificatie          | character varying(255)      | not null |
| lv_publicatiedatum     | timestamp without time zone | not null |
| creationdate           | date                        | not null |
| tijdstipregistratie    | timestamp without time zone | not null |
| eindregistratie        | timestamp without time zone |          |
| terminationdate        | date                        |          |
| bronhouder             | character varying(255)      | not null |
| inonderzoek            | boolean                     | not null |
| relatievehoogteligging | integer                     | not null |
| bgt_status             | character varying(255)      | not null |
| plus_status            | character varying(255)      | not null |
| idx                    | integer                     | not null | Primaire sleutel en volgnummer
| tekst                  | character varying(255)      | not null |
| hoek                   | double precision            | not null |
| plaatsingspunt         | geometry(Point,28992)       |          |
| openbareruimtetype     | character varying(255)      | not null |
| identificatiebagopr    | character varying(16)       |          |

Deze tabel heeft dus een _composite primary key_, net als `nummeraanduidingreeks`.

#### Indexen

Standaard worden alleen op de geometrie-kolommen indexen gemaakt. Wil je op kolommen filteren bij het gebruik van de
tabellen, kan je zelf indexen toevoegen na het laden, bijvoorbeeld op de `identificatie` kolom.

#### Unieke kolom of OBJECT_ID

Wil je de BGT gebruiken in ArcGIS en heb je een "OBJECT_ID" kolom nodig, maar kan dit niet met de `gmlid` kolom of de 
_composite primary keys_ voor `nummeraanduidingreeks` en `openbareruimtelabel`? Mogelijk kan je zelf een kolom toevoegen
die automatisch gevuld wordt (in PostGIS is dat mogelijk met een `serial` kolomtype). We horen graag je 
ervaringen met het gebruik!

Na het inladen kan je kolommen kan je natuurlijk altijd verwijderen of wijzigen, behalve als je mutaties wil laden.

## Downloaden van mutaties

De selectieopties gebruikt bij het `download initial` commando worden opgeslagen in de database in de `bgt_metadata`
tabel en worden gebruikt om de juiste mutaties te downloaden van de PDOK mutatieservice. Draai het volgende commando (met
eventuele database-verbindingsopties) om mutaties te laden:

```shell
brmo-bgt-loader download update
````

Dit commando kan dagelijks in een "cronjob" of geplande taak gedraaid worden om er zeker van zijn dat de BGT gegevens
up-to-date blijven. De data wordt door PDOK hoogstens eens per dag geüpdate.

## Laden van de hele 'predefined' BGT

De B3Partners BGT lader gebruikt bij het commando `download initial --no-geo-filter` automatisch de voorbereide BGT
download zodat niet gewacht hoeft te worden totdat PDOK een extract heeft samengesteld van de hele BGT (voor heel
Nederland kan dit kan uren duren).

Het voorbereide extract met mutatiegevens is alleen beschikbaar inclusief plaatsbepalingspunten, maar de B3Partners BRMO
lader kan deze en andere niet geselecteerde objecttypen (met de `--feature-types` optie) overslaan zonder dat deze
gedownload hoeven te worden!

De URL die gebruikt wordt voor de voorbereide hele BGT is
https://api.pdok.nl/lv/bgt/download/v1_0/delta/predefined/bgt-citygml-nl-delta.zip. Het heeft niet veel nut deze zelf te
downloaden, het bestand is minstens 47 GB groot en compleet uitgepakt >750 GB. Hiervan is >28 GB
plaatsbepalingspunten (>500 GB uitgepakt).

Om de hele BGT exclusief plaatsbepalingspunten in te laden moet (met de stand in augustus 2021) ongeveer 19 GB 
gedownload te worden.

### De 'predefined' BGT zonder mutatie-informatie

Via de link https://api.pdok.nl/lv/bgt/download/v1_0/full/predefined/bgt-citygml-nl-nopbp.zip is de hele BGT beschikbaar
zonder plaatsbepalingspunten in CityGML formaat, zonder mutatie-informatie. Vanwege de ZIP compressie is deze niet veel
kleiner dan de voorgedefinieerde delta download wanneer plaatsbepalingspunten worden overgeslagen, maar het is mogelijk
wat sneller te laden. Met onderstaand commando kan deze worden ingeladen, maar let op dat het `download update` commando
niet kan worden gebruikt omdat er geen "delta ID" in het bestand zit:

```shell
brmo-bgt-loader load https://api.pdok.nl/lv/bgt/download/v1_0/full/predefined/bgt-citygml-nl-nopbp.zip
```

De optie `--feature-types` kan ook met dit commando worden gebruikt om alleen bepaalde objecttypen in te laden zonder ze
te downloaden.

## Downloads laden gemaakt via de PDOK download viewer website

Via de [PDOK download viewer](https://app.pdok.nl/lv/bgt/download-viewer/) is het mogelijk om een gebied te tekenen op
de kaart en objecttypes te selecteren. De gemaakte download kan direct worden ingeladen door rechts te klikken op de
"Download" knop, de link te kopiëren en deze op te geven bij `brmo-bgt-loader load <link>`. Let op dat
plaatsbepalingspunten nog steeds worden overgeslagen, tenzij ook de optie `--feature-types=all,plaatsbepalingspunten` wordt opgegeven.

Deze downloads bevatten geen mutatie-informatie en kunnen niet geüpdate worden via de mutatieservice. Gebruik daarvoor
het `download initial` commando.

Alleen BGT CityGML bestanden worden ondersteund, geen GML-light of Stuf-geo.

Als je het bestand al gedownload hebt en gebruikt maakt van Docker, mount een volume zodat de container toegang heeft
tot het bestand. Als een BGT bestand in de huidige directory staat, voer het volgende uit om deze te laden:

```shell
docker run -it --rm --network=host -v $(pwd):/data ghcr.io/b3partners/brmo-bgt-loader load /data/bgt-citygml-nl-nopbp.zip
```
Lokale bestanden kunnen ook geladen worden in uitgepakt .gml of .xml formaat.

## Geavanceerde opties

Geavanceerde opties zijn normaal gezien niet nodig om te gebruiken, maar je kan altijd in de broncode kijken welke
beschikbaar zijn.
