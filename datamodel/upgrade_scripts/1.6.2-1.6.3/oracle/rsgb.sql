-- 
-- upgrade Oracle RSGB datamodel van 1.6.2 naar 1.6.3 
--
WHENEVER SQLERROR EXIT SQL.SQLCODE

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.2_naar_1.6.3','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.3' WHERE naam='brmoversie';
