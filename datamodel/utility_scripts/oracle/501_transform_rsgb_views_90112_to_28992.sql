-- Transformeer alle metadata van geometrieen in de tabellen van de RSGB database 
--    van 90112 (Oracle Rijksdriehoek identifier) 
--    naar 28992 (EPSG Rijksdriehoek identifier)
--
UPDATE USER_SDO_GEOM_METADATA SET SRID=28992 WHERE srid=90112;
