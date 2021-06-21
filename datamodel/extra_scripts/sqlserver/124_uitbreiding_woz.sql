-- voeg kolommen toe aan tabel woz_obj
ALTER TABLE woz_obj ADD waterschap varchar(4) NULL;
ALTER TABLE woz_obj ADD fk_verantw_gem_code numeric(4) NULL;
-- omdat testdata ongeldige waarden bevat vooralsnog geen constraint afdwingen
-- ALTER TABLE woz_obj ADD CONSTRAINT fk_gem_code FOREIGN KEY (fk_verantw_gem_code) REFERENCES gemeente(code) on delete no action;

ALTER TABLE woz_obj_archief ADD waterschap varchar(4) NULL;
ALTER TABLE woz_obj_archief ADD fk_verantw_gem_code numeric(4) NULL;


GO
