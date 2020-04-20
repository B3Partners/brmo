-- 
-- upgrade Oracle RSGBBGT datamodel van 2.0.1 naar 2.0.2 
--

-- GH#826 actuele bronhouder code
ALTER TABLE bak ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE begroeid_terreindeel ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE bord ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE buurt ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE functioneelgebied ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE gebouw_installatie ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE installatie ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE kast ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE kunstwerkdeel ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE mast ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE onbegroeid_terreindeel ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE ondersteunend_waterdeel ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE ondersteunend_wegdeel ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE ongeclassificeerdobject ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE openbareruimte ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE openbareruimtelabel ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE overbruggingsdeel ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE overig_bouwwerk ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE overige_scheiding ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE paal ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE pand ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE put ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE scheiding ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE sensor ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE spoor ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE stadsdeel ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE straatmeubilair ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE tunneldeel ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE vegetatieobject ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE waterdeel ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE waterinrichtingselement ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE waterschap ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE wegdeel ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE weginrichtingselement ADD(bronhouder VARCHAR2(5 CHAR));
ALTER TABLE wijk ADD(bronhouder VARCHAR2(5 CHAR));


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.0.1_naar_2.0.2','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.0.2' WHERE naam='brmoversie';
