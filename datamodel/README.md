brmo datamodel
==============

```
java -jar lib/saxon9he.jar -s:rsgb22.xml -xsl:rsgbsst2.xsl -o:rsgb_converted.xml

java -jar lib/saxon9he.jar -s:rsgb_converted.xml -xsl:rsgb_db_identifiers.xsl -o:rsgb_db_identifiers.xml

java -jar lib/saxon9he.jar -s:rsgb_converted.xml -xsl:datamodel.xsl -o:datamodel.xml
Associaties naar een class die een associatie als primary key heeft worden niet ondersteund
Error at xsl:message on line 114 of keys_types.xsl:
  XTMM9000: Processing terminated by xsl:message at line 114 in keys_types.xsl
  at xsl:call-template name="get-class-primary-key-properties-hierarchy" (file:/home/matthijsln/dev/brmo/datamodel/datamodel.xsl#444)
  at xsl:call-template name="create-foreign-key" (file:/home/matthijsln/dev/brmo/datamodel/datamodel.xsl#247)
  at xsl:call-template name="association" (file:/home/matthijsln/dev/brmo/datamodel/datamodel.xsl#130)
  at xsl:apply-templates (file:/home/matthijsln/dev/brmo/datamodel/datamodel.xsl#24)
     processing /RSGB/Objecttypes[1]/Class[7]
  in built-in template rule
  in built-in template rule
Processing terminated by xsl:message at line 114 in keys_types.xsl

java -jar lib/saxon9he.jar -s:datamodel.xml -xsl:datamodel_postgres.xsl -o:datamodel_postgresql.sql
Error at xsl:variable on line 23 column 83 of datamodel_postgres.xsl:
  XPST0008: Variable geom-types has not been declared (or its declaration is not in scope)
Stylesheet compilation failed: 1 error reported

java -jar lib/saxon9he.jar -s:datamodel.xml -xsl:datamodel_oracle.xsl -o:datamodel_oracle.sql
```
