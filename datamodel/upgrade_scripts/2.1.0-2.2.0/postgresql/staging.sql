-- 
-- upgrade PostgreSQL STAGING datamodel van 2.1.0 naar 2.2.0
--


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.1.0_naar_2.2.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.2.0' WHERE naam='brmoversie';
