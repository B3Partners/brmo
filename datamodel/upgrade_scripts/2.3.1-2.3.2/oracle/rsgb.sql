-- 
-- upgrade Oracle RSGB datamodel van 2.3.1 naar 2.3.2 
--

WHENEVER SQLERROR EXIT SQL.SQLCODE

ALTER TABLE ingeschr_niet_nat_prs MODIFY publiekrechtelijke_rechtsvorm VARCHAR2(60);
ALTER TABLE functionaris MODIFY soort_bev VARCHAR2(50);

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.3.1_naar_2.3.2','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.3.2' WHERE naam='brmoversie';
