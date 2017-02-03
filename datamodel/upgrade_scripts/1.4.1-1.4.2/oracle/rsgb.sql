-- 
-- upgrade Oracle RSGB datamodel van 1.4.1 naar 1.4.2 
--

-- vergroot kolombreedte van 'lo_loc__omschr' in onroerend zaak tabellen naar 255 char
ALTER TABLE KAD_ONRRND_ZK         MODIFY LO_LOC__OMSCHR VARCHAR2(255);
ALTER TABLE KAD_ONRRND_ZK_ARCHIEF MODIFY LO_LOC__OMSCHR VARCHAR2(255);

-- versienummer update
UPDATE brmo_metadata SET waarde='1.4.2' WHERE naam='brmoversie';
