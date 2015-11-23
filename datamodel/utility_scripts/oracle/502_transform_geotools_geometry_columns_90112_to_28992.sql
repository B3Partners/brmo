-- Transformeer de geoserver metadata tabellen van de RSGB database 
--    van 90112 (Oracle Rijksdriehoek identifier) 
--    naar 28992 (EPSG Rijksdriehoek identifier)
-- 
-- Te gebruiken als er een GEOMETRY_COLUMNS met 90112 definitie is gemaakt.
--
UPDATE GEOMETRY_COLUMNS SET srid=28992 WHERE srid=90112;
