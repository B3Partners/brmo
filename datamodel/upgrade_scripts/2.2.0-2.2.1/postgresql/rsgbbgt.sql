-- 
-- upgrade PostgreSQL RSGBBGT datamodel van 2.2.0 naar 2.2.1 
--

CREATE TABLE IF NOT EXISTS brmo_metadata(naam CHARACTER VARYING(255) NOT NULL,waarde TEXT,CONSTRAINT brmo_metadata_pk PRIMARY KEY (naam));
-- Update existing brmo_metadata table to use clob type for waarde
ALTER TABLE brmo_metadata ALTER COLUMN waarde TYPE TEXT;

INSERT INTO brmo_metadata(naam) VALUES('brmoversie') ON CONFLICT DO NOTHING;


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.2.0_naar_2.2.1','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.2.1' WHERE naam='brmoversie';
