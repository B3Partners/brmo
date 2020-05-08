-- voeg kolom hoofdvestiging toe aan tabel vestg
ALTER TABLE VESTG ADD HOOFDVESTIGING VARCHAR2(3);
COMMENT ON COLUMN VESTG.HOOFDVESTIGING IS 'indicatie hoofdvestiging (niet-RSGB)';