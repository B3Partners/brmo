-- 
-- upgrade PostgreSQL STAGING datamodel van 3.0.2 naar 4.0.0 
--


-- opruimen van niet langer beschikbare automatische scanner processen
DELETE FROM automatisch_proces_config WHERE proces_id IN (SELECT id FROM automatisch_proces WHERE dtype IN ('BAGScannerProces','WebMirrorBAGScannerProces')) ;
DELETE FROM automatisch_proces WHERE dtype IN ('BAGScannerProces','WebMirrorBAGScannerProces');
-- BAG1 GDS2 processen verwijderen
CREATE TEMP TABLE pids AS SELECT apc.proces_id FROM automatisch_proces_config apc WHERE apc.value = 'bag' AND apc.config_key='gds2_br_soort';
DELETE FROM automatisch_proces_config WHERE proces_id IN (SELECT proces_id FROM pids);
DELETE FROM automatisch_proces WHERE id IN (SELECT proces_id FROM pids);
DROP TABLE pids;

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_3.0.2_naar_4.0.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='4.0.0' WHERE naam='brmoversie';
