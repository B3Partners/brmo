-- 
-- upgrade SQLserver STAGING datamodel van 1.6.3 naar 1.6.4 
--
alter table laadproces add bestand_naam_hersteld varchar(255) null;

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.3_naar_1.6.4','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.4' WHERE naam='brmoversie';
