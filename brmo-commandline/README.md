# BRMO commandline tool

## Introductie

Met de commandline tool kunnen een aantal beheer taken worden uitgevoerd. Hieronder vallen bijvoorbeeld het opvragen van informatie over de databases, het laden en verwijderen van bestanden (laadprocessen), het transformeren en het dumpen van GDS2 berichten.

De basis commandoregel is: `java -jar ./bin/brmo-commandline.jar` deze geeft output met de gebruiksinfo als onderstaand:

```
usage: java -jar brmo-commandline.jar --<actie> --dbprops <db-props>

Acties:
  -v,--versieinfo <[format]>                                 Versie informatie van de verschillende
                                                             schema's
  -l,--list <[format]>                                       Geef overzicht van laadprocessen in
                                                             staging database
  -s,--berichtstatus <[format]>                              Geef aantallen van bericht status in
                                                             staging database
  -j,--jobstatus <[format]>                                  Geef aantal berichten in job tabel van
                                                             staging database
  -a,--load <bestandsnaam> <type-br> <[archief-directory]>   Laad totaalstand of mutatie uit bestand
                                                             (.zip of .xml) in database
  -ad,--loaddir <directory> <type-br> <[archief-directory]>  Laad stand of mutatie berichten (.zip
                                                             of .xml) uit directory in database
  -d,--delete <id>                                           Verwijder laadprocessen in database
                                                             (geef id weer met -list)
  -t,--torsgb <[error-state]>                                Transformeer alle 'STAGING_OK'
                                                             berichten naar rsgb.
  -e,--exportgds <output-directory>                          Maak van berichten uit staging gezipte
                                                             xml-files in de opgegeven directory.
                                                             Dit zijn alleen BRK mutaties van GDS2
                                                             processen.
  -al,--afgiftelijst <afgiftelijst> <uitvoerbestand>         Controleer of de berichten in de
                                                             opgegeven afgiftelijst in de staging
                                                             staan.
Configuratie:
  -db,--dbprops <bestand>  database properties file


```
De `[format]` optie is optioneel en kan de waarde "json" hebben, de default is tekst output.

De `[archief-directory]` optie is optioneel en kan gebruikt worden om bestanden na laden in een archief directory te plaatsen.

De `[error-state]` is optioneel, default is "ignore".

De `[loadingUpdate]` optie is optioneel met een default waarde van "false".

__De `--afgiftelijst` is beschikbaar vanaf versie 2.0.0__

De transformatie naar rsgb wordt normaal met de "ignore" `error-state` optie gestart om te voorkomen dat het proces afbreekt als er een transformatie fout optreed bij de verwerking van een bericht, om dit te voorkomen kan er een andere waarde worden opgegeven, bijvoorbeeld "false".
Na afloop van een transformatie kan de status van de berichten worden gecontroleerd met de `--berichtstatus` optie.

## Voorbeelden

Onderstaand een aantal voorbeelden.

  - `java -jar ./bin/brmo-commandline.jar --dbprops ./conf/commandline-example.properties --versieinfo`
  
     output:  
     
     ```
     staging versie: 3.0.2
     rsgb    versie: 3.0.2
     ```
     
  - `java -jar ./bin/brmo-commandline.jar -db conf/commandline-example.properties --load ./MUT01.xml brk`
  - `java -jar ./bin/brmo-commandline.jar --dbprops conf/commandline-example.properties --list json`
  
     output:  
     `{"aantalprocessen":0}`  
     of:
     `{"aantalprocessen":1,"processen":[{"id":86,"bestand_naam":"./MUT01.xml","bestand_datum":"2016-04-08","soort":"brk","status":"STAGING_OK","contact":"null"}]}`

  - `java -jar ./bin/brmo-commandline.jar --dbprops conf/brmo-db.properties --torsgb`
  - `java -jar ./bin/brmo-commandline.jar --dbprops ./conf/brmo-db.properties -s`
  
     output:  

     ```
     status, aantal  
     STAGING_OK,0  
     STAGING_NOK,0  
     RSGB_OK,0  
     RSGB_NOK,1
     RSGB_OUTDATED,0  
     ARCHIVE,0  

     ```
  - `java -jar bin/brmo-commandline.jar --dbprops brmo-db.properties -j`
  
     output:  
     
     ```
     aantal  
     0  

     ```
  - `java -jar bin/brmo-commandline.jar -db conf/brmo-commandline.properties -ad /tmp/brk brk`


## Logging configuratie

De tool gebruikt Log4J logging voor de logging configuratie (NB. programma output van bijvoorbeeld het `list` commando is geen log output). 
De logging kan worden aangepast worden door de log4j.xml te bewerken welke in de `brmo-commandline.jar` te vinden is. De default gebruikt `info` level logging naar het bestand `brmo-commandline.log`.
Vervolgens kan de ingebouwde log file overruled worden door op de commandoregel de optie naar de logging configuratie te geven als JVM optie, bijvoorbeeld: `java -Dlog4j.configuration=file:./mijnlog4j.xml ./brmo-commandline.jar -db commandline-example.properties --list json`
Meer informatie over de mogelijkheden van Log4J is te vinden in de [manual](https://logging.apache.org/log4j/1.2/manual.html) en de [FAQ](https://logging.apache.org/log4j/1.2/faq.html).
