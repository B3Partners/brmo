-- Transformeer de geoserver metadata tabellen van de RSGB database 
--    van 90112 (Oracle Rijksdriehoek identifier) 
--    naar 28992 (EPSG Rijksdriehoek identifier)
-- tbv. update/migratie BRMO 1.2.10 naar 1.2.11
--
-- Te gebruiken als er een GEOMETRY_COLUMNS met 90112 definitie is gemaakt.
--
UPDATE GEOMETRY_COLUMNS SET srid=28992 WHERE srid=90112;
