<!--
Copyright (C) 2021 B3Partners B.V.

SPDX-License-Identifier: MIT
-->

# BRMO BAG 2.0 lader

Met de B3Partners BRMO BAG 2.0 lader kan de [BAG 2.0](https://www.kadaster.nl/zakelijk/producten/adressen-en-gebouwen/bag-2.0-extract) (Basisregistratie
Objecten en Gebouwen 2.0) snel in een spatial database worden geladen.

### Copyright en licentie

Copyright (C) 2021 B3Partners B.V.

Voor de licentievoorwaarden en disclaimer zie [MIT](LICENSES/MIT.txt). Commerciële ondersteuning is te verkrijgen via
[B3Partners](https://www.b3partners.nl).

### Ondersteunde platformen en systeemeisen

Deze applicatie is beschikbaar als [Docker image](https://github.com/B3Partners/brmo/pkgs/container/brmo-bag2-loader).
Deze Linux containers draaien ook op Windows, MacOS en Raspberry Pi (64 bit). Het is niet nodig om het programma te
draaien op hetzelfde systeem als de database, als er via het netwerk verbinding mee kan worden gelegd.

Het programma werkt ook op andere platformen waar minimaal Java 11 beschikbaar is. Momenteel is er geen uitvoerbare JAR
download, maar het is mogelijk deze zelf te compileren vanaf de broncode of te verkrijgen via B3Partners.

Er is ongeveer 1 GB RAM nodig. De BAG 2.0 bestanden worden "streaming" verwerkt: er is niet meer geheugen nodig voor 
meerdere gemeentes of om heel Nederland in te laden.

Het kan zijn dat je veel schijfruimte nodig hebt voor de database waarin de BAG ingeladen wordt, afhankelijk van de
welke gemeente -- of als je heel Nederland wil inladen.

## Draaien van het programma

Met [Docker](https://www.docker.com):
```shell
docker pull ghcr.io/b3partners/brmo-bag2-loader
docker run -it --rm --network=host ghcr.io/b3partners/brmo-bag2-loader --help
````

Het programma is voornamelijk in het Nederlands, behalve als er mogelijk een foutmelding optreedt. Volledig Engelse
teksten zijn ook beschikbaar door de container te starten met de `-e LC_ALL=en_US` parameter.

Let op dat het uitvoeren van het `docker pull` commando nodig is om het image te updaten naar de laatste
versie. Door het opgeven van een _tag_ kan een specifieke versie worden gebruikt. Standaard wordt de tag _latest_
gebruikt met de laatste versie.

De onderstaande voorbeelden om de applicatie te starten laten het eerste stuk van `docker run` weg. Zet deze altijd voor
het commando wat je uitvoert.

### Compileren

Installeer Maven een draai `mvn package`. Dit bouwt een uitvoerbaar JAR-bestand dat uitgevoerd kan worden met:

```shell
java -jar target/bag2-loader-*.jar
````

## Command line

Het programma print nuttige help wanneer deze met de `--help` optie wordt aangeroepen. Voorlopig is er een enkel `load`
commando om een BAG 2.0 extract in te laden. Per commando geeft het programma de opties die je kan gebruiken, probeer
bijvoorbeeld `load --help`.

## Database voorbereiding

Een database is vereist voordat de BAG kan worden geladen. De volgende databases worden ondersteund:
- [PostgreSQL](https://www.postgresql.org/) met [PostGIS](https://www.postgis.org/), versie 9.6 met PostGIS 2.5 t/m versie 13 met PostGIS 3.1
- Oracle Spatial 18g

Let op dat de BAG van heel Nederland niet past binnen de database limiet van 12 GB van de Express Edition van Oracle, 
maar een enkele gemeente(s) zal wel lukken.

Andere versies werken mogelijk ook.

Gebruik bij voorkeur PostGIS, deze is verreweg het snelst! Naar PostGIS worden de gegevens met een efficiënt `COPY`
statement geladen.

### PostGIS

Installeer PostgreSQL en PostGIS. Dit kan snel en makkelijk met Docker:

```shell
docker run --name postgis --detach --publish 5432:5432 -e POSTGRES_PASSWORD=postgres postgis/postgis -c fsync=off
```
Heb je een Raspberry Pi? Gebruik de image naam `awill88/postgis:13-3.0` in plaats van `postgis/postgis`. Zonder Docker
ben je natuurlijk ook snel op weg met `apt install postgis`.

Wacht een momentje totdat PostgreSQL is opgestart totdat `docker logs postgis` aangeeft "database system is ready to
accept connections". En daarna:

```shell
docker exec -it -u postgres postgis bash -c "createuser bag; createdb --owner=bag bag; psql bag -c 'create extension postgis;'"
docker exec -it -u postgres postgis psql -c "alter role bag password 'bag2'"
```
De BAG zal ingeladen wordt in het `bag` schema (dit wordt automatisch aangemaakt als het nog niet bestaat). Voor integratie
met de rest van de BRMO (om bijvoorbeeld de BRK te koppelen aan de BAG) kan de BAG ook in de RSGB database van de BRMO worden
ingeladen.

Met deze commando's krijg je een database op je lokale computer en de database naam, gebruiker en wachtwoord allemaal
ingesteld op `bag`. Het programma kan dan met de standaard opties gebruikt worden om met de database te verbinden. Het is
mogelijk om andere gegevens op te geven, zie de uitvoer van `brmo-bag2-loader load --help` voor details.

Voorbeeldcommando om de hele BAG te laden in PostGIS:
```shell
brmo-bag2-loader load https://extracten.bag.kadaster.nl/lvbag/extracten/Nederland%20LVC/BAGNLDL-08092021.zip
```
Voor Docker Desktop op Windows of Mac: gebruik `host.docker.internal` in plaats van `localhost`.

Staat de database op een andere server, bijvoorbeeld met IP adres `10.0.0.1`? Gebruik dan de volgende optie:
`--connection="jdbc:postgresql://10.0.0.1:5432/bag?sslmode=disable&reWriteBatchedInserts=true"`. Alles na het vraagteken
kan weggelaten worden maar is bevorderlijk voor de snelheid.

Let op! Gebruik je Docker Desktop op Windows of Mac dan werkt `localhost` niet in combinatie met de `--network=host`
optie voor Docker. Gebruik dan de volgende optie:
`--connection="jdbc:postgresql://host.docker.internal:5432/bag?sslmode=disable&reWriteBatchedInserts=true"`

### Oracle Spatial

Het is mogelijk om Oracle Spatial zelf te installeren, of deze te starten in een Linux container met Docker (let op dat
je de gebruiksvoorwaarden van Oracle accepteert):

Onderstaand commando gebruikt ter illustratie een onofficieel image (deze is wel 18 GB groot).

```shell
docker run --detach --publish 1521:1521 --name oracle-xe -d pvargacl/oracle-xe-18.4.0:latest
```

Let op dat je eventueel de Oracle editie [kan opgeven](https://github.com/oracle/docker-images/blob/main/OracleDatabase/SingleInstance/README.md) als je de licentie ervoor hebt. Voor de BAG van heel Nederland 
voldoet de database limiet van 12 GB van de Express Edition niet.

Wacht totdat Oracle is gestart en voer het volgende uit om een schema en gebruiker aan te maken (met bash syntax):
```shell
{ 
echo "create user c##bag identified by bag default tablespace users temporary tablespace temp;"; 
echo "alter user c##bag quota unlimited on users;";
echo "grant connect, resource, create view to c##bag;";
echo "alter user c##bag default role connect, resource;"; 
} | docker exec -i oracle-xe sqlplus -l system/oracle@//localhost:1521/XE
```

Let op dat de tool niet zelf een schema aanmaakt omdat in Oracle hiervoor een gebruiker nodig is. Gebruik dus
de gebruikersnaam van de databaseconnectie om ook het schema op te geven. Door de BAG in te laden in dezelfde
database als de RSGB maar in een apart schema kan de BAG gecombineerd worden met andere basisregistraties.

Het is nodig om de `--connection` en `--user` opties aan de BAG lader mee te geven om met Oracle Spatial te verbinden.

Voorbeeldcommando om de hele BAG te laden in Oracle Spatial:

```shell
brmo-bag2-loader load --connection="jdbc:oracle:thin:@localhost:1521:XE" --user="c##bag" https://extracten.bag.kadaster.nl/lvbag/extracten/Nederland%20LVC/BAGNLDL-08092021.zip
```
Voor Docker Desktop op Windows of Mac: gebruik `host.docker.internal` in plaats van `localhost`.

Vergeet niet om de Docker container te stoppen als je deze niet meer nodig hebt met het `docker stop oracle-xe` commando.
Mogelijk wil je de container ook weer verwijderen met `docker rm oracle-xe`.

## Verkrijgen van de BAG extract

Tot 1 oktober was het mogelijk om BAG extracten te downloaden van https://extracten.bag.kadaster.nl/lvbag/extracten/

Hierna is een abonnement verplicht, zie de website van het Kadaster.

Het is ook mogelijk om een al gedownload bestand te laden. Gebruik voor Docker daarvoor een _mount_:

```bash
docker run -it --rm --network=host -v ${PWD}:/data ghcr.io/b3partners/brmo-bag2-loader load /data/BAGNLDL-08092021.zip
```

### Het database schema

TODO, zie interne Confluence.

#### OBJECTID voor ArcGIS

De BAG tabellen en views hebben een `objectid` kolom zodat deze direct gebruikt kunnen worden in ArcGIS.

## Downloaden van mutaties

TODO.

## Geavanceerde opties

Geavanceerde opties zijn normaal gezien niet nodig om te gebruiken, maar je kan altijd in de broncode kijken welke
beschikbaar zijn.
