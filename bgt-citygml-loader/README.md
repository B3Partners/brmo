# BGT loader

## Build

Run `mvn package`. This will build a runnable JAR and a docker image named `b3partners/bgt-citygml-loader`.

## Run the command line tool

Using a JRE:
`java -jar target/bgt-citygml-loader-*.jar`

With docker:
`docker run -it --rm --network=host b3partners/bgt-citygml-loader`

The Docker container prints Dutch messages by default. For English messages add the `-e LC_ALL=en_US` parameter.

## Command line tool usage

The tool will print useful help when run without arguments. Use the `schema` command to output an SQL script 
and execute it in your database to create the tables and the `load` command to load BGT/IMGeo CityGML files into a database.

When using docker, create a volume to read files from. If you have a CityGML file in the current directory, execute the
following command to load it:

`docker run -it --rm --network=host -v $(pwd):/data b3partners/bgt-citygml-loader load --file=/data/bgt-citygml-nl-nopbp.zip`

## Full example

### PostGIS

Install PostgreSQL and PostGIS.

Create a PostgreSQL user, enter 'bgt' as password:
```
createuser bgt --password bgt -P
```
Create a database:
```
createdb --owner=bgt bgt
psql bgt -c 'create extension postgis';
```
Note: if you need to use a tablespace, create it, set it as default and grant privileges to the `bgt` user now.

Create tables:
```
(echo 'set session authorization bgt;'; bgt-citygml-loader schema) | psql bgt
```
Load a BGT CityGML file:
```
bgt-citygml-loader load --file=bgt-citygml-nl-nopbp.zip
```