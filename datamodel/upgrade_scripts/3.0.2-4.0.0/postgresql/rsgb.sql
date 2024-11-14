-- 
-- upgrade PostgreSQL RSGB datamodel van 3.0.2 naar 4.0.0 
--

set search_path = public,bag,brk;

-- update brk waardelijsten
INSERT INTO aard_verkregen_recht (aand, omschr_aard_verkregenr_recht) VALUES ('25', 'Huur afhankelijk opstal (recht van)');
INSERT INTO aard_verkregen_recht (aand, omschr_aard_verkregenr_recht) VALUES ('26','Pachtafhankelijk opstal (recht van)');
INSERT INTO aard_recht_verkort (aand, omschr) VALUES ('25', 'Huur afhankelijk opstal (recht van)');
INSERT INTO aard_recht_verkort (aand, omschr) VALUES ('26', 'Pachtafhankelijk opstal (recht van)');

-- Onderstaande materialized views kunnen verwijderd worden en worden niet langer ondersteund.
-- ivm potentiële afhankelijkheid wordt dit niet automatisch gedaan
-- zie https://b3partners.atlassian.net/browse/BRMO-381
-- DROP MATERIALIZED VIEW mb_kad_onrrnd_zk_adres_bag;
-- DROP MATERIALIZED VIEW mb_koz_rechth_bag;
-- DROP MATERIALIZED VIEW mb_avg_koz_rechth_bag;

-- https://b3partners.atlassian.net/browse/BRMO-383
DROP MATERIALIZED VIEW IF EXISTS mb_onroerendezakenmetrechthebbenden;
DROP MATERIALIZED VIEW IF EXISTS mb_avg_onroerendezakenmetrechthebbenden;

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_3.0.2_naar_4.0.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='4.0.0' WHERE naam='brmoversie';
