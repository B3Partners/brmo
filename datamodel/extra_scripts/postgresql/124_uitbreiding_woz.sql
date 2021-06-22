-- voeg kolommen toe aan tabel woz_obj
-- xsd pattern: [0-9]{4}
ALTER TABLE woz_obj ADD waterschap varchar(4) NULL;
ALTER TABLE woz_obj ADD fk_verantw_gem_code numeric(4) NULL;
-- omdat testdata ongeldige waarden bevat vooralsnog geen constraint afdwingen voor gemeente
-- ALTER TABLE woz_obj ADD CONSTRAINT fk_gem_code FOREIGN KEY (fk_verantw_gem_code) REFERENCES gemeente(code);
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
    -- geen harde binding tussen WOZ en BRK maken
    -- CONSTRAINT fk_woz_omvat_sc_lh FOREIGN KEY (fk_sc_lh_kad_identif) REFERENCES kad_onrrnd_zk(kad_identif),
    CONSTRAINT fk_woz_omvat_sc_rh FOREIGN KEY (fk_sc_rh_woz_nummer) REFERENCES woz_obj(nummer) on delete cascade
);
COMMENT ON COLUMN woz_omvat.fk_sc_lh_kad_identif IS '[FK] N15, FK naar kad_onrrnd_zk.kad_identif';
COMMENT ON COLUMN woz_omvat.fk_sc_rh_woz_nummer IS '[FK] N12, FK naar woz_obj.nummer';
COMMENT ON COLUMN woz_omvat.toegekende_opp IS 'N12, toegekende oppervlakte';