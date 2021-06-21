-- voeg kolommen toe aan tabel woz_obj
-- xsd pattern: [0-9]{4}
ALTER TABLE woz_obj ADD waterschap varchar(4) NULL;
ALTER TABLE woz_obj ADD fk_verantw_gem_code numeric(4) NULL;
-- omdat testdata ongeldige waarden bevat vooralsnog geen constraint afdwingen
-- ALTER TABLE woz_obj ADD CONSTRAINT fk_gem_code FOREIGN KEY (fk_verantw_gem_code) REFERENCES gemeente(code);
COMMENT ON COLUMN woz_obj.waterschap IS 'ligt in waterschap (niet-RSGB)';
COMMENT ON COLUMN woz_obj.fk_verantw_gem_code IS 'verantwoordelijke gemeente (niet-RSGB)';

ALTER TABLE woz_obj_archief ADD waterschap varchar(4) NULL;
ALTER TABLE woz_obj_archief ADD fk_verantw_gem_code numeric(4) NULL;
COMMENT ON COLUMN woz_obj_archief.waterschap IS 'ligt in waterschap (niet-RSGB)';
COMMENT ON COLUMN woz_obj_archief.fk_verantw_gem_code IS 'verantwoordelijke gemeente (niet-RSGB)';