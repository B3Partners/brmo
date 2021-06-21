-- 
-- upgrade SQLserver RSGB datamodel van 2.0.3 naar 2.1.0 
--

-- aanpassen woz tabellen
ALTER TABLE woz_deelobj ALTER COLUMN nummer DECIMAL(12,0);
ALTER TABLE woz_deelobj ALTER COLUMN dat_beg_geldh_deelobj VARCHAR(19);
ALTER TABLE woz_deelobj ALTER COLUMN datum_einde_geldh_deelobj VARCHAR(19)

ALTER TABLE woz_deelobj_archief ALTER COLUMN nummer DECIMAL(12,0)
ALTER TABLE woz_deelobj_archief ALTER COLUMN dat_beg_geldh_deelobj VARCHAR(19);
ALTER TABLE woz_deelobj_archief ALTER COLUMN datum_einde_geldh_deelobj VARCHAR(19)

-- voeg kolommen toe aan tabel woz_obj
ALTER TABLE woz_obj ADD waterschap varchar(4) NULL;
ALTER TABLE woz_obj ADD fk_verantw_gem_code numeric(4) NULL;

ALTER TABLE woz_obj_archief ADD waterschap varchar(4) NULL;
ALTER TABLE woz_obj_archief ADD fk_verantw_gem_code numeric(4) NULL;

GO

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.0.3_naar_2.1.0','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.1.0' WHERE naam='brmoversie';
