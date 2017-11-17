# Developer notes


## release maken

Een release bouwen begint met het uitvoeren van het commando `mvn clean release:prepare`
daarbij wordt voor de verschillende artifacten om versienummers gevraagd,
zowel voor voor de release als de volgende ontwikkel versie.
Tevens wordt er om een naam voor een tag gevraagd. In principe kan alle informatie op de
commandline worden meegegeven, bijvoorbeeld:

```
mvn release:prepare -l rel-prepare.log -DdevelopmentVersion=1.6-SNAPSHOT -DreleaseVersion=1.5 -Dtag=topnlparser-1.5 -T1
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
