-- 
-- upgrade PostgreSQL RSGB datamodel van 2.0.3 naar 2.1.0
--

-- aanpassen woz tabellen
ALTER TABLE woz_deelobj ALTER COLUMN nummer TYPE numeric(12);
ALTER TABLE woz_deelobj ALTER COLUMN dat_beg_geldh_deelobj TYPE varchar(19);
ALTER TABLE woz_deelobj ALTER COLUMN datum_einde_geldh_deelobj TYPE varchar(19);

ALTER TABLE woz_deelobj_archief ALTER COLUMN nummer TYPE numeric(12);
ALTER TABLE woz_deelobj_archief ALTER COLUMN dat_beg_geldh_deelobj TYPE varchar(19);
ALTER TABLE woz_deelobj_archief ALTER COLUMN datum_einde_geldh_deelobj TYPE varchar(19);

COMMENT ON COLUMN woz_deelobj.nummer IS '[PK] N12 - Nummer WOZ-deelobject';
COMMENT ON COLUMN woz_deelobj.datum_einde_geldh_deelobj IS 'OnvolledigeDatum - Datum einde geldigheid deelobject';
COMMENT ON COLUMN woz_deelobj_archief.nummer IS '[PK] N12 - Nummer WOZ-deelobject';
COMMENT ON COLUMN woz_deelobj_archief.dat_beg_geldh_deelobj IS '[PK] OnvolledigeDatum - Datum begin geldigheid deelobject';

-- voeg kolommen toe aan tabel woz_obj
ALTER TABLE woz_obj ADD waterschap varchar(4) NULL;
ALTER TABLE woz_obj ADD fk_verantw_gem_code numeric(4) NULL;
COMMENT ON COLUMN woz_obj.waterschap IS 'ligt in waterschap (niet-RSGB)';
COMMENT ON COLUMN woz_obj.fk_verantw_gem_code IS 'verantwoordelijke gemeente (niet-RSGB)';

ALTER TABLE woz_obj_archief ADD waterschap varchar(4) NULL;
ALTER TABLE woz_obj_archief ADD fk_verantw_gem_code numeric(4) NULL;
COMMENT ON COLUMN woz_obj_archief.waterschap IS 'ligt in waterschap (niet-RSGB)';
COMMENT ON COLUMN woz_obj_archief.fk_verantw_gem_code IS 'verantwoordelijke gemeente (niet-RSGB)';


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.0.3_naar_2.1.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.1.0' WHERE naam='brmoversie';
