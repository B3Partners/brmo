-- 
-- upgrade PostgreSQL STAGING datamodel van 2.3.2 naar 2.3.3 
--


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.3.2_naar_2.3.3','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.3.3' WHERE naam='brmoversie';
