-- 
-- upgrade PostgreSQL RSGB datamodel van 5.0.0 naar 5.0.1
--

set search_path = public,bag,brk;


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_5.0.0_naar_5.0.1','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='5.0.1' WHERE naam='brmoversie';