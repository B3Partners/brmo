brmo datamodel
==============

Via onderstaande commando's kan het rsgb datamodel script voor oracle en postgresql worden gegenereerd op basis van het RSGB 2.2 UML. Alle aanvullende scripts in de map extra_scripts worden in het totaal script opgenomen. Indien een update gedaan moet worden op een oudere versie van de database, dan kunnen de scripts uit deze map natuurlijk wel zelfstandig gebruikt worden.

Voor het genereren van het totaal script voer je het volgende commando uit:

```
mvn generate-resources
``` 

De scripts in de map utility_scripts worden niet opgenomen in het totaal script. Deze zijn vaak klantspecifiek en dienen naar behoefte handmatig gedraaid te worden.

## GEM-WPL koppeling script bijwerken

- Plaats de zipfile `GEM-WPL-RELATIE-<datum>.zip` uit een recente landelijke BAG levering in de `referentiedata` directory
- Pas de property `filename.GEM-WPL-zipfile` in de datamodel pom file aan naar de bestandsnaam
- voer het commando `mvn clean package` uit in de datamodel module
- controleer de bijgewerkte versies van `201_update_wnplts_gemcode.sql`
- indien akkoord commit en push de bijgewerkte versies van `201_update_wnplts_gemcode.sql` naar github
- verwijder `GEM-WPL-RELATIE-<datum>.zip` uit de `referentiedata` directory

