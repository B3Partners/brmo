-- 
-- upgrade SQLserver STAGING datamodel van 2.2.1 naar 2.2.2 
--


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.2.1_naar_2.2.2','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.2.2' WHERE naam='brmoversie';
