--
-- upgrade PostgreSQL RSGB datamodel van 2.2.0 naar 2.2.1
--

set search_path = bag,public;

-- BRMO-130 / GH #1231 De BAG 2 views moeten vervangen worden
DROP VIEW IF EXISTS bag.vb_adresseerbaar_object_geometrie;
DROP VIEW IF EXISTS bag.vb_verblijfsobject_adres;
DROP VIEW IF EXISTS bag.vb_standplaats_adres;
DROP VIEW IF EXISTS bag.vb_ligplaats_adres;
DROP VIEW IF EXISTS bag.vb_adres;

CREATE SCHEMA IF NOT EXISTS bag;
CREATE TABLE IF NOT EXISTS bag.brmo_metadata(naam CHARACTER VARYING(255) NOT NULL,waarde CHARACTER VARYING(255),CONSTRAINT brmo_metadata_pk PRIMARY KEY (naam));
INSERT INTO bag.brmo_metadata(naam) VALUES('brmoversie') ON CONFLICT DO NOTHING;

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO bag.brmo_metadata (naam,waarde) SELECT 'upgrade_2.2.0_naar_2.2.1','vorige versie was ' || waarde FROM bag.brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE bag.brmo_metadata SET waarde='2.2.1' WHERE naam='brmoversie';