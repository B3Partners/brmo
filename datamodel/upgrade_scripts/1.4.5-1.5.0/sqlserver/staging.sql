--
-- upgrade SQLserver STAGING datamodel van 1.4.5 naar 1.5.0
--


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.4.5_naar_1.5.0','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.5.0' WHERE naam='brmoversie';
