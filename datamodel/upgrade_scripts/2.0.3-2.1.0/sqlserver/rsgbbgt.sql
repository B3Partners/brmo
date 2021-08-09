-- 
-- upgrade SQLserver RSGBBGT datamodel van 2.0.3 naar 2.1.0 
--

-- drop oude RSGBBGT tabellen
DROP TABLE bak;
DROP TABLE begroeid_terreindeel;
DROP TABLE bord;
DROP TABLE functioneelgebied;
DROP TABLE gebouw_installatie;
DROP TABLE installatie;
DROP TABLE kast;
DROP TABLE mast;
DROP TABLE paal;
DROP TABLE put;
DROP TABLE sensor;
DROP TABLE openbareruimtelabel;
DROP TABLE overige_scheiding;
DROP TABLE scheiding;
DROP TABLE overig_bouwwerk;
DROP TABLE ongeclassificeerdobject;
DROP TABLE waterdeel;
DROP TABLE kunstwerkdeel;
DROP TABLE onbegroeid_terreindeel;
DROP TABLE overbruggingsdeel;
DROP TABLE straatmeubilair;
DROP TABLE ondersteunend_waterdeel;
DROP TABLE ondersteunend_wegdeel;
DROP TABLE spoor;
DROP TABLE weginrichtingselement;
DROP TABLE vegetatieobject;
DROP TABLE tunneldeel;
DROP TABLE waterinrichtingselement;
DROP TABLE wegdeel;
DROP TABLE pand;
DROP TABLE buurt;
DROP TABLE stadsdeel;
DROP TABLE wijk;
DROP TABLE openbareruimte;
DROP TABLE waterschap;

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.0.3_naar_2.1.0','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.1.0' WHERE naam='brmoversie';
