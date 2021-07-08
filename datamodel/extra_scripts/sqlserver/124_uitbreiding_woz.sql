-- voeg kolommen toe aan tabel woz_obj
ALTER TABLE woz_obj ADD waterschap varchar(4) NULL;
ALTER TABLE woz_obj ADD fk_verantw_gem_code numeric(4) NULL;
-- omdat testdata ongeldige waarden bevat vooralsnog geen constraint afdwingen voor gemeente
-- ALTER TABLE woz_obj ADD CONSTRAINT fk_gem_code FOREIGN KEY (fk_verantw_gem_code) REFERENCES gemeente(code) on delete no action;

ALTER TABLE woz_obj_archief ADD waterschap varchar(4) NULL;
ALTER TABLE woz_obj_archief ADD fk_verantw_gem_code numeric(4) NULL;

GO

-- koppeltabel voor onroerende zaak
CREATE TABLE woz_omvat (
    fk_sc_lh_kad_identif decimal(15,0) NOT NULL,
    fk_sc_rh_woz_nummer decimal(12,0) NOT NULL,
    toegekende_opp numeric(11) NULL,
    CONSTRAINT woz_omvat_pk PRIMARY KEY (fk_sc_lh_kad_identif, fk_sc_rh_woz_nummer),
    -- geen harde binding tussen WOZ en BRK maken
    -- CONSTRAINT fk_woz_omvat_sc_lh FOREIGN KEY (fk_sc_lh_kad_identif) REFERENCES kad_onrrnd_zk(kad_identif),
    CONSTRAINT fk_woz_omvat_sc_rh FOREIGN KEY (fk_sc_rh_woz_nummer) REFERENCES woz_obj(nummer) on delete cascade
);

GO
