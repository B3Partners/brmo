-- 
-- upgrade PostgreSQL RSGB datamodel van 4.0.0 naar 5.0.0
--

set search_path = public;



-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_4.0.0_naar_5.0.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='5.0.0' WHERE naam='brmoversie';
