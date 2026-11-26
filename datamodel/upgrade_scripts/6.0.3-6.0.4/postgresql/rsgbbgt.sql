-- 
-- upgrade PostgreSQL RSGBBGT datamodel van 6.0.3 naar 6.0.4 
--

CREATE TABLE IF NOT EXISTS brmo_metadata(naam CHARACTER VARYING(255) NOT NULL, waarde TEXT, CONSTRAINT brmo_metadata_pk PRIMARY KEY (naam));
INSERT INTO brmo_metadata(naam) VALUES('brmoversie') ON CONFLICT DO NOTHING;


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_6.0.3_naar_6.0.4','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='6.0.4' WHERE naam='brmoversie';
