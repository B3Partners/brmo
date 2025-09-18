-- 
-- upgrade Oracle STAGING datamodel van 6.0.1 naar 6.0.2 
--

WHENEVER SQLERROR EXIT SQL.SQLCODE


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_6.0.1_naar_6.0.2','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='6.0.2' WHERE naam='brmoversie';
