-- 
-- upgrade SQLserver RSGB datamodel van 1.6.3 naar 1.6.4 
--

UPDATE aard_verkregen_recht SET omschr_aard_verkregenr_recht='Zakelijk recht als bedoeld in artikel 5, lid 3, onder b, van de Belemmeringenwet Privaatrecht' WHERE aand='10';
UPDATE aard_recht_verkort SET omschr='Zakelijk recht (als bedoeld in artikel 5, lid 3, onder b)' WHERE aand='10';
UPDATE aard_recht_verkort SET omschr='Zakelijk recht op gedeelte van perceel (als bedoeld in artikel 5, lid 3, onder b)' WHERE aand='24';
GO

-- PR#617 verwijderen oude views
DROP VIEW IF EXISTS vb_kad_onrrnd_zk_archief;
DROP VIEW IF EXISTS vb_avg_koz_rechth;
DROP VIEW IF EXISTS vb_koz_rechth;
DROP VIEW IF EXISTS vb_avg_zr_rechth;
DROP VIEW IF EXISTS vb_zr_rechth;
DROP VIEW IF EXISTS vb_util_zk_recht;
DROP VIEW IF EXISTS vb_kad_onrrnd_zk_adres;
DROP VIEW IF EXISTS vb_util_app_re_kad_perceel;
DROP VIEW IF EXISTS vb_util_app_re_parent;
DROP VIEW IF EXISTS vb_util_app_re_parent_2;
DROP VIEW IF EXISTS vb_util_app_re_parent_3;
DROP VIEW IF EXISTS vb_util_app_re_splitsing;
DROP VIEW IF EXISTS vb_avg_subject;
DROP VIEW IF EXISTS vb_subject;
DROP VIEW IF EXISTS vb_ben_obj_nevenadres;
DROP VIEW IF EXISTS vb_benoemd_obj_adres;
DROP VIEW IF EXISTS vb_pand;
DROP VIEW IF EXISTS vb_ligplaats_adres;
DROP VIEW IF EXISTS vb_standplaats_adres;
DROP VIEW IF EXISTS vb_vbo_adres;
DROP VIEW IF EXISTS vb_adres;
GO

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.3_naar_1.6.4','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
CREATE TABLE zak_recht_archief
(
   kadaster_identif character varying(255) NOT NULL,
   eindd_recht character varying(255),
   indic_betrokken_in_splitsing character varying(255),
   ingangsdatum_recht character varying(19 NOT NULL,
   fk_7koz_kad_identif numeric(15,0),
   fk_8pes_sc_identif character varying(255),
   ar_noemer numeric(8,0),
   ar_teller numeric(8,0),
   fk_2aard_recht_verkort_aand character varying(4),
   fk_3avr_aand character varying(6),
   CONSTRAINT zak_recht_archief_pk PRIMARY KEY (kadaster_identif,ingangsdatum_recht)
);
 
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.4' WHERE naam='brmoversie';
