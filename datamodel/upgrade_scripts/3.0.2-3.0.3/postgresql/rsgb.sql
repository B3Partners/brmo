-- 
-- upgrade PostgreSQL RSGB datamodel van 3.0.2 naar 3.0.3 
--

set search_path = public,bag,brk;


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_3.0.2_naar_3.0.3','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='3.0.3' WHERE naam='brmoversie';