-- voeg kolom hoofdvestiging toe aan tabel vestg
ALTER TABLE vestg ADD hoofdvestiging varchar(3) NULL;
COMMENT ON COLUMN vestg.hoofdvestiging IS 'indicatie hoofdvestiging (niet-RSGB)';