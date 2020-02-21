-- 
-- upgrade SQLserver STAGING datamodel van 2.0.1 naar 2.0.2 
--


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.0.1_naar_2.0.2','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.0.2' WHERE naam='brmoversie';
