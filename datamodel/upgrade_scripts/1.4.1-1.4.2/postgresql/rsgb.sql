-- 
-- upgrade PostgreSQL RSGB datamodel van 1.4.1 naar 1.4.2 
--

-- vergroot kolombreedte van 'lo_loc__omschr' in onroerend zaak tabellen naar 255 char
ALTER TABLE kad_onrrnd_zk         ALTER COLUMN lo_loc__omschr TYPE character varying(255);
ALTER TABLE kad_onrrnd_zk_archief ALTER COLUMN lo_loc__omschr TYPE character varying(255);

-- versienummer update
UPDATE brmo_metadata SET waarde='1.4.2' WHERE naam='brmoversie';
