-- 
-- upgrade Oracle RSGB datamodel van 3.0.2 naar 4.0.0 
--

WHENEVER SQLERROR EXIT SQL.SQLCODE

-- update brk waardelijsten
INSERT INTO aard_verkregen_recht (aand, omschr_aard_verkregenr_recht) VALUES ('25', 'Huur afhankelijk opstal (recht van)');
INSERT INTO aard_verkregen_recht (aand, omschr_aard_verkregenr_recht) VALUES ('26', 'Pachtafhankelijk opstal (recht van)');
INSERT INTO aard_recht_verkort (aand, omschr) VALUES ('25', 'Huur afhankelijk opstal (recht van)');
INSERT INTO aard_recht_verkort (aand, omschr) VALUES ('26', 'Pachtafhankelijk opstal (recht van)');

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_3.0.2_naar_4.0.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='4.0.0' WHERE naam='brmoversie';
