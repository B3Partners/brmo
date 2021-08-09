-- 
-- upgrade SQLserver STAGING datamodel van 2.0.3 naar 2.1.0 
--

-- opruimen van niet meer beschikbare automatische processen
DELETE FROM automatisch_proces_config WHERE proces_id IN (SELECT id FROM automatisch_proces WHERE dtype IN ('BGTLightScannerProces','BGTLightOphaalProces')) ;
DELETE FROM automatisch_proces WHERE dtype IN ('BGTLightScannerProces','BGTLightOphaalProces');

-- opruimen van niet meer verwerkbare BGT light laadprocessen
DELETE FROM laadproces WHERE soort='bgtlight';

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.0.3_naar_2.1.0','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.1.0' WHERE naam='brmoversie';
