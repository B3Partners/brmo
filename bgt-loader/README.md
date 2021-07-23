# BGT loader

The B3Partners BRMO BGT loader can load the Dutch [BGT](https://docs.geostandaarden.nl/imgeo/catalogus/bgt/) (Basisregistratie Grootschalige Topografie) and the [IMGeo](https://docs.geostandaarden.nl/imgeo/catalogus/imgeo/) (Informatiemodel Geografie) extensions in a spatial database and keep the data updated using the [PDOK BGT mutatieservice](https://api.pdok.nl/lv/bgt/download/v1_0/ui/). See the [Modeldocumentatie mutatieformaat BGT](https://www.pdok.nl/bgt-mutatie) for more information.  

### Copyright and license

Copyright (c) 2021 B3Partners B.V.

This program comes with ABSOLUTELY NO WARRANTY. This is free software, and you are welcome to redistribute it
under certain conditions; see the [LICENSE](../LICENSE) file for details. Commercial support is available from [B3Partners](https://www.b3partners.nl).

## Running

With [Docker](https://www.docker.org):
```shell
docker pull ghcr.io/b3partners/brmo-bgt-loader
docker run -it --rm --network=host ghcr.io/b3partners/brmo-bgt-loader
````

The Docker container prints Dutch messages by default. For English messages add the `-e LC_ALL=en_US` parameter.

### Building 

To build from source, install Maven and run `mvn package`. This will build a runnable JAR you can run with:

`java -jar target/bgt-loader-*.jar`


## Command line

The program will print useful help when run without arguments. There are three main commands: `schema`, `load` and `download`.
Most people need only use the `download` command: this creates database tables and downloads the BGT data automatically.

## Database setup

A database must be setup before using the program. The following databases are supported:
- [PostgreSQL](https://www.postgresql.org/) with the [PostGIS](https://www.postgis.org/) spatial extension
- Microsoft SQL Server
- Oracle Spatial

### PostGIS

Install PostgreSQL and PostGIS.

Create a PostgreSQL user, enter 'bgt' as password:
```
createuser bgt --password bgt -P
```
Create a user and database:
```
createdb --owner=bgt bgt
psql bgt -c 'create extension postgis'
```
Note: if you need to use a tablespace, create it, set it as default and grant privileges to the `bgt` user now.

## Loading the BGT

The download service supports loading the entire BGT for the entire continental Netherlands, or selecting only a few 
feature types (tables) or region(s) using a polygon defined in WKT in the Rijksdriehoekstelsel (RD) coordinate system 
(EPSG:28992).

### Loading the entire BGT

Loading the entire BGT takes a lot of disk space! Make sure you have ample space available and run:
```
docker run -it --rm --network=host ghcr.io/b3partners/brmo-bgt-loader download initial --no-geo-filter
```

### Specifying feature types and a geo filter

Use the ```--feature-types``` option to select only a few feature types. The special values `all` selects all feature types
(the default), `bgt` only the BGT feature types (which _bronhouders_ are required to maintain) and `plus` only the optionally 
maintained IMGeo feature types. Individual feature types can be specified separated by spaces, such as 
`--feature-types=pand,openbareruimtelabel`. The available list of feature types is:

| Feature type            |
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

A geo filter can be specified using the `--geo-filter` option. Tip: use the free [QGIS](https://www.qgis.org/) program 
to draw a polygon. Select the polygon and copy it to the clipboard to get the WKT.

## Downloading updates

The options used for the `download initial` command are saved in the database in the `bgt_metadata` table and are used to keep the data 
up-to-date. Run the following command to get changes from the PDOK service:

`docker run -it --rm --network=host ghcr.io/b3partners/brmo-bgt-loader download update`

You can run this daily in a cronjob or scheduled task to make sure the data stays updated. The data is only updated once
a day.

## Loading the entire BGT faster (without updates)

The `load` command can read BGT CityGML files (no GML-light or Stuf-geo formats) from disk. If you want to load an 
initial dataset without a geo filter and don't need to keep it updated, you can download the entire BGT from PDOK using 
this link: https://api.pdok.nl/lv/bgt/download/v1_0/full/predefined/bgt-citygml-nl-nopbp.zip (warning: very large file!).
After downloading the file, execute the following command to load it:

`docker run -it --rm --network=host -v $(pwd):/data ghcr.io/b3partners/brmo-bgt-loader load /data/bgt-citygml-nl-nopbp.zip`

This skips the time needed to wait for the PDOK service to create a custom download. Unfortunately, this file does not 
contain a 'delta ID' required to apply updates from the service to it (although you may try to put the latest delta ID in 
the `bgt_metadata` table).