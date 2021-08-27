-- 
-- upgrade Oracle RSGBBGT datamodel van 2.1.0 naar 2.1.1 
--

WHENEVER SQLERROR EXIT SQL.SQLCODE


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.1.0_naar_2.1.1','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.1.1' WHERE naam='brmoversie';
