-- 
-- upgrade Oracle STAGING datamodel van 1.6.0 naar 1.6.1 
--


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.0_naar_1.6.1','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.1' WHERE naam='brmoversie';
