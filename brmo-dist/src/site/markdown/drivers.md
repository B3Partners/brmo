# database en mail drivers

## Mail en JDBC drivers

Ten behoeve van een snelle installatie worden de volgende drivers meegepakt (voor iedere database omgeving een setje):

  - PostgreSQL JDBC, Postgis en mail drivers
  - jTDS JDBC driver voor MSSQL en mail drivers
  - mail drivers, de Oracle driver is te vinden op https://www.oracle.com/technetwork/database/application-development/jdbc/downloads/index.html

De bestanden dienen in de `lib` directory van Tomcat te worden geplaatst zodat ze gebruikt kunnen worden in de verschillende JNDI bronnen.

## Java 11 extra

Voor Het draaien van de applicaties met een Java 11 runtime zijn een aantal extra libraries nodig, deze zijn te vinden in de `java11-libs`