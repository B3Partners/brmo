-- 
-- upgrade Oracle STAGING datamodel van 5.0.2 naar 6.0.0
--

WHENEVER SQLERROR EXIT SQL.SQLCODE

-- opruimen van niet langer beschikbare automatische scanner processen
DELETE FROM automatisch_proces_config WHERE proces_id IN (SELECT id FROM automatisch_proces WHERE dtype IN ('LaadprocesTransformatieProces', 'TopNLScannerProces'));
DELETE FROM automatisch_proces WHERE dtype IN ('LaadprocesTransformatieProces', 'TopNLScannerProces');

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_5.0.2_naar_6.0.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='6.0.0' WHERE naam='brmoversie';
