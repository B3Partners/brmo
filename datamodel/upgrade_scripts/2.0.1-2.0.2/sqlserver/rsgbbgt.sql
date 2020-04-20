-- 
-- upgrade SQLserver RSGBBGT datamodel van 2.0.1 naar 2.0.2 
--

-- GH#826 actuele bronhouder code
ALTER TABLE bak ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE begroeid_terreindeel ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE bord ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE buurt ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE functioneelgebied ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE gebouw_installatie ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE installatie ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE kast ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE kunstwerkdeel ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE mast ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE onbegroeid_terreindeel ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE ondersteunend_waterdeel ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE ondersteunend_wegdeel ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE ongeclassificeerdobject ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE openbareruimte ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE openbareruimtelabel ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE overbruggingsdeel ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE overig_bouwwerk ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE overige_scheiding ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE paal ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE pand ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE put ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE scheiding ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE sensor ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE spoor ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE stadsdeel ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE straatmeubilair ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE tunneldeel ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE vegetatieobject ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE waterdeel ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE waterinrichtingselement ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE waterschap ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE wegdeel ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE weginrichtingselement ADD COLUMN bronhouder varchar(5) null;
ALTER TABLE wijk ADD COLUMN bronhouder varchar(5) null;

GO

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.0.1_naar_2.0.2','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.0.2' WHERE naam='brmoversie';
