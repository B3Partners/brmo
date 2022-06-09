-- 
-- upgrade PostgreSQL STAGING datamodel van 2.3.0 naar 2.3.1 
--


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.3.0_naar_2.3.1','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.3.1' WHERE naam='brmoversie';
