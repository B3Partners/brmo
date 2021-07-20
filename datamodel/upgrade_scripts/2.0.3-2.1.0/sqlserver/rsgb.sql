-- 
-- upgrade SQLserver RSGB datamodel van 2.0.3 naar 2.1.0 
--

-- aanpassen woz tabellen
ALTER TABLE woz_deelobj DROP CONSTRAINT woz_deelobj_pk;
ALTER TABLE woz_deelobj ALTER COLUMN nummer DECIMAL(12,0) NOT NULL;
ALTER TABLE woz_deelobj ALTER COLUMN dat_beg_geldh_deelobj VARCHAR(19);
ALTER TABLE woz_deelobj ALTER COLUMN datum_einde_geldh_deelobj VARCHAR(19)
ALTER TABLE woz_deelobj ADD CONSTRAINT woz_deelobj_pk PRIMARY KEY (nummer);

ALTER TABLE woz_deelobj_archief DROP CONSTRAINT ar_woz_deelobj_pk;
ALTER TABLE woz_deelobj_archief ALTER COLUMN nummer DECIMAL(12,0) NOT NULL;
ALTER TABLE woz_deelobj_archief ALTER COLUMN dat_beg_geldh_deelobj VARCHAR(19) NOT NULL;
ALTER TABLE woz_deelobj_archief ALTER COLUMN datum_einde_geldh_deelobj VARCHAR(19) NULL;
ALTER TABLE woz_deelobj_archief ADD CONSTRAINT ar_woz_deelobj_pk PRIMARY KEY (dat_beg_geldh_deelobj,nummer);

ALTER TABLE woz_obj ADD waterschap varchar(4) NULL;
ALTER TABLE woz_obj ADD fk_verantw_gem_code numeric(4) NULL;

ALTER TABLE woz_obj_archief ADD waterschap varchar(4) NULL;
ALTER TABLE woz_obj_archief ADD fk_verantw_gem_code numeric(4) NULL;

GO

-- koppeltabel voor onroerende zaak
CREATE TABLE woz_omvat (
    fk_sc_lh_kad_identif decimal(15,0) NOT NULL,
    fk_sc_rh_woz_nummer decimal(12,0) NOT NULL,
    toegekende_opp numeric(11) NULL,
    CONSTRAINT woz_omvat_pk PRIMARY KEY (fk_sc_lh_kad_identif, fk_sc_rh_woz_nummer),
    CONSTRAINT fk_woz_omvat_sc_rh FOREIGN KEY (fk_sc_rh_woz_nummer) REFERENCES woz_obj(nummer) on delete cascade
);

GO

-- voeg kolom nonmailing toe aan tabel maatschapp_activiteit
ALTER TABLE maatschapp_activiteit ADD nonmailing varchar(3) NULL;
GO

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.0.3_naar_2.1.0','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.1.0' WHERE naam='brmoversie';
