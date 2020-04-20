-- 
-- upgrade PostgreSQL RSGBBGT datamodel van 2.0.1 naar 2.0.2 
--

-- GH#826 actuele bronhouder code
ALTER TABLE bak ADD COLUMN bronhouder varchar(5);
ALTER TABLE begroeid_terreindeel ADD COLUMN bronhouder varchar(5);
ALTER TABLE bord ADD COLUMN bronhouder varchar(5);
ALTER TABLE buurt ADD COLUMN bronhouder varchar(5);
ALTER TABLE functioneelgebied ADD COLUMN bronhouder varchar(5);
ALTER TABLE gebouw_installatie ADD COLUMN bronhouder varchar(5);
ALTER TABLE installatie ADD COLUMN bronhouder varchar(5);
ALTER TABLE kast ADD COLUMN bronhouder varchar(5);
ALTER TABLE kunstwerkdeel ADD COLUMN bronhouder varchar(5);
ALTER TABLE mast ADD COLUMN bronhouder varchar(5);
ALTER TABLE onbegroeid_terreindeel ADD COLUMN bronhouder varchar(5);
ALTER TABLE ondersteunend_waterdeel ADD COLUMN bronhouder varchar(5);
ALTER TABLE ondersteunend_wegdeel ADD COLUMN bronhouder varchar(5);
ALTER TABLE ongeclassificeerdobject ADD COLUMN bronhouder varchar(5);
ALTER TABLE openbareruimte ADD COLUMN bronhouder varchar(5);
ALTER TABLE openbareruimtelabel ADD COLUMN bronhouder varchar(5);
ALTER TABLE overbruggingsdeel ADD COLUMN bronhouder varchar(5);
ALTER TABLE overig_bouwwerk ADD COLUMN bronhouder varchar(5);
ALTER TABLE overige_scheiding ADD COLUMN bronhouder varchar(5);
ALTER TABLE paal ADD COLUMN bronhouder varchar(5);
ALTER TABLE pand ADD COLUMN bronhouder varchar(5);
ALTER TABLE put ADD COLUMN bronhouder varchar(5);
ALTER TABLE scheiding ADD COLUMN bronhouder varchar(5);
ALTER TABLE sensor ADD COLUMN bronhouder varchar(5);
ALTER TABLE spoor ADD COLUMN bronhouder varchar(5);
ALTER TABLE stadsdeel ADD COLUMN bronhouder varchar(5);
ALTER TABLE straatmeubilair ADD COLUMN bronhouder varchar(5);
ALTER TABLE tunneldeel ADD COLUMN bronhouder varchar(5);
ALTER TABLE vegetatieobject ADD COLUMN bronhouder varchar(5);
ALTER TABLE waterdeel ADD COLUMN bronhouder varchar(5);
ALTER TABLE waterinrichtingselement ADD COLUMN bronhouder varchar(5);
ALTER TABLE waterschap ADD COLUMN bronhouder varchar(5);
ALTER TABLE wegdeel ADD COLUMN bronhouder varchar(5);
ALTER TABLE weginrichtingselement ADD COLUMN bronhouder varchar(5);
ALTER TABLE wijk ADD COLUMN bronhouder varchar(5);


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.0.1_naar_2.0.2','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.0.2' WHERE naam='brmoversie';
