# BRMO docker

Een project om een dockerfile te maken met daarin een Tomcat instantie waarin :
- brmo-service (http://localhost:8080/brmo-service/)
- postgis database

## build

Gebruik Maven: 

```
mvn install -Dmaven.test.skip=true -B -V -e -fae -q
mvn clean deploy
```
(NB. voor `deploy` is authenticatie nodig middels `docker login` en een geldige `~/.m2/settings.xml`)

## run
Start een stack met de bijvoorbeeld volgende command line:

```shell
docker compose --env-file /home/mark/dev/projects/brmo/docker/localhost.env \
         -f /home/mark/dev/projects/brmo/docker/docker-compose.yml \
         -f /home/mark/dev/projects/brmo/docker/docker-compose-ports.yml \
         -p brmo-service up --always-recreate-deps --remove-orphans -d --build
```

Het default password dient te worden aangepast voordat er data wordt geladen.
Gebruik de procedure op https://github.com/B3Partners/brmo/wiki/update-wachtwoord-procedure#versies-vanaf-210

```shell
# maak has in de tomcat container
/usr/local/tomcat/bin/digest.sh -a PBKDF2WithHmacSHA512 -i 100000 -s 16 -k 256 -h "org.apache.catalina.realm.SecretKeyCredentialHandler" <STERK WACHTWOORD>
```
Update in de database container
```shell
# login staging db
PGPASSWORD=${DB_PASS_STAGING} psql -v ON_ERROR_STOP=1 --username staging --dbname staging
update gebruiker_ set wachtwoord = '<HASH>' where gebruikersnaam = 'brmo';
\q
```


## data laden

Kopieer grote stand files naar het `brmo-service_brmo-data` volume, bijvoorbeeld:

```shell
docker cp /opt/data/brk/brk2-stand.zip brmo-service-brmo-1:/opt/brmo-data/brk2-stand.zip
``` 

Gebruik daarna de webinterface om het bestand in de staging te laden en te transformeren