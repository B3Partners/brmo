--
-- upgrade PostgreSQL RSGB datamodel van 2.2.0 naar 2.2.1
--

set search_path = bag,public;

-- BRMO-130 / GH #1231 De BAG 2 views moeten vervangen worden
drop view vb_adresseerbaar_object_geometrie;
drop view vb_verblijfsobject_adres;
drop view vb_standplaats_adres;
drop view vb_ligplaats_adres;
drop view vb_adres;

CREATE TABLE IF NOT EXISTS brmo_metadata(naam CHARACTER VARYING(255) NOT NULL,waarde CHARACTER VARYING(255),CONSTRAINT brmo_metadata_pk PRIMARY KEY (naam));
INSERT INTO brmo_metadata(naam) VALUES('brmoversie') ON CONFLICT DO NOTHING;

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.2.0_naar_2.2.1','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.2.1' WHERE naam='brmoversie';