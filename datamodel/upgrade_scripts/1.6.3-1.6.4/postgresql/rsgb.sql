-- 
-- upgrade PostgreSQL RSGB datamodel van 1.6.3 naar 1.6.4 
--

UPDATE aard_verkregen_recht SET omschr_aard_verkregenr_recht='Zakelijk recht als bedoeld in artikel 5, lid 3, onder b, van de Belemmeringenwet Privaatrecht' WHERE aand='10';
UPDATE aard_recht_verkort SET omschr='Zakelijk recht (als bedoeld in artikel 5, lid 3, onder b)' WHERE aand='10';
UPDATE aard_recht_verkort SET omschr='Zakelijk recht op gedeelte van perceel (als bedoeld in artikel 5, lid 3, onder b)' WHERE aand='24';

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.3_naar_1.6.4','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.4' WHERE naam='brmoversie';
