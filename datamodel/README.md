brmo datamodel
==============

```
java -jar lib/saxon9he.jar -s:rsgb22.xml -xsl:rsgbsst2.xsl -o:rsgb_converted.xml

java -jar lib/saxon9he.jar -s:rsgb_converted.xml -xsl:rsgbrepair.xsl -o:rsgb_converted_repaired.xml

java -jar lib/saxon9he.jar -s:rsgb_converted_repaired.xml -xsl:rsgb_db_identifiers.xsl -o:rsgb_db_identifiers.xml

java -jar lib/saxon9he.jar -s:rsgb_converted_repaired.xml -xsl:datamodel.xsl -o:datamodel.xml

java -jar lib/saxon9he.jar -s:datamodel.xml -xsl:datamodel_postgres.xsl -o:datamodel_postgresql.sql

java -jar lib/saxon9he.jar -s:datamodel.xml -xsl:datamodel_oracle.xsl -o:datamodel_oracle.sql
```
