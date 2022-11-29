-- 
-- upgrade PostgreSQL RSGB datamodel van 2.3.3 naar 2.3.4 
--

set search_path = public,bag;


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.3.3_naar_2.3.4','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.3.4' WHERE naam='brmoversie';
