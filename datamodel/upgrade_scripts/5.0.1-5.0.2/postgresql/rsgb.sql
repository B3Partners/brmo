-- 
-- upgrade PostgreSQL RSGB datamodel van 5.0.1 naar 5.0.2 
--

set search_path = public,bag,brk;

alter table brmo_metadata alter column waarde type text;

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_5.0.1_naar_5.0.2','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='5.0.2' WHERE naam='brmoversie';
