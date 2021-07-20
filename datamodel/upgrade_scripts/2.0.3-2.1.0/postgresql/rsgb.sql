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

-- koppeltabel voor onroerende zaak
CREATE TABLE woz_omvat (
    fk_sc_lh_kad_identif numeric(15) NOT NULL,
    fk_sc_rh_woz_nummer numeric(12) NOT NULL,
    toegekende_opp numeric(11) NULL,
    CONSTRAINT woz_omvat_pk PRIMARY KEY (fk_sc_lh_kad_identif, fk_sc_rh_woz_nummer),
    CONSTRAINT fk_woz_omvat_sc_rh FOREIGN KEY (fk_sc_rh_woz_nummer) REFERENCES woz_obj(nummer) on delete cascade
);
COMMENT ON COLUMN woz_omvat.fk_sc_lh_kad_identif IS '[FK] N15, FK naar kad_onrrnd_zk.kad_identif';
COMMENT ON COLUMN woz_omvat.fk_sc_rh_woz_nummer IS '[FK] N12, FK naar woz_obj.nummer';
COMMENT ON COLUMN woz_omvat.toegekende_opp IS 'N12, toegekende oppervlakte';

-- voeg kolom nonmailing toe aan tabel maatschapp_activiteit
ALTER TABLE maatschapp_activiteit ADD nonmailing varchar(3) NULL;
COMMENT ON COLUMN maatschapp_activiteit.nonmailing IS 'nonMailing attribuut (niet-RSGB)';

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.0.3_naar_2.1.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.1.0' WHERE naam='brmoversie';
