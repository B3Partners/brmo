-- 
-- upgrade PostgreSQL RSGB datamodel van 2.3.1 naar 2.3.2 
--

set search_path = public,bag;

ALTER TABLE ingeschr_niet_nat_prs ALTER COLUMN publiekrechtelijke_rechtsvorm TYPE VARCHAR(60);
ALTER TABLE functionaris ALTER COLUMN soort_bev TYPE VARCHAR(50);

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.3.1_naar_2.3.2','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.3.2' WHERE naam='brmoversie';
