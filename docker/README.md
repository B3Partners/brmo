# BRMO docker

Een project om een dockerfile te maken met daarin een Tomcat instantie waarin :
- brmo-service (http://localhost:8080/brmo-service/)


## build

Als de `brmo-dist` artifact is uitgepakt in `docker/src/main/docker/bin_unzipped`
dan kun je met onderstaande de image bouwen en pushen: 
```
docker build --file ./docker/src/main/docker/Dockerfile ./docker/src/main/docker/ --tag ghcr.io/b3partners/brmo:snapshot
docker push ghcr.io/b3partners/brmo:snapshot
```
(NB. voor `push` is authenticatie nodig middels `docker login`)

Anders met Maven: 

```
mvn install -Dmaven.test.skip=true -B -V -e -fae -q
mvn clean deploy
```

## run
Start een container met de volgende command line:
```
export CATALINA_OPTS="-DPG_PORT=5432 -DPG_HOST=172.17.0.1 -DDB_NAME_RSGB=rsgb -DDB_USER_RSGB=brmo -DDB_PASS_RSGB=brmo -DDB_NAME_STAGING=staging -DDB_USER_STAGING=brmo -DDB_PASS_STAGING=brmo -DDB_NAME_RSGBBGT=rsgbbgt -DDB_USER_RSGBBGT=brmo -DDB_PASS_RSGBBGT=brmo -DDB_NAME_TOPNL=topnl -DDB_USER_TOPNL=brmo -DDB_PASS_TOPNL=brmo -DAJP_ADDRESS=::1 -DAJP_SECRET=noisyPurpl315"
docker run --net bridge \
       -p 8080:8080 \
       -e CATALINA_OPTS \
       --rm \
       -it --name brmo -h brmo \
       --mount type=volume,dst=/opt/brmo-data,volume-driver=local,volume-opt=type=none,volume-opt=o=bind,volume-opt=device=/tmp/brmo-data \
       -v /tmp/logs:/usr/local/tomcat/logs \
       b3partners/brmo:snapshot
```

Uitgangspunt hierbij is dat de benodigde databases te benaderen zijn met account `brmo` 
password `brmo` ip-adres `172.17.0.1` (normaal de docker host in default bridge netwerk) 
en poort `5432`.

