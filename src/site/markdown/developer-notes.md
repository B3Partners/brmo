#Developer notes


## release maken

Een release bouwen begint met het uitvoeren van het commando `mvn clean release:prepare`
daarbij wordt voor de verschillende artifacten om versienummers gevraagd (evt. de
optie `-DautoVersionSubmodules=true` gebruiken als alles dezelfde versie moet krijgen),
zowel voor voor de release als de volgende ontwikkel versie.
Tevens wordt er om een naam voor een tag gevraagd.

Met het commando `mvn release:perform` wordt daarna, op basis van de tag uit de
stap hierboven, de release gebouwd en gedeployed naar de repository uit de (parent)
pom file. De release bestaat uit jar en war files met daarin oa. ook de javadoc en
mogelijke site. Voor het hele project kan dit even duren, oa. omdat de javadoc ook gebouwd wordt.


###Bugginess

Bij de eerste keer bouwen liep ik tegen een bugginess aan tussen git en maven
(https://jira.codehaus.org/browse/SCM-740) waardoor er -snapshot versie in de tag op
github terecht kwamen, niet de bedoeling. de oplossing zat 'm in:

```
git config --add status.displayCommentPrefix true
export LANG=C
mvn clean release:prepare
```