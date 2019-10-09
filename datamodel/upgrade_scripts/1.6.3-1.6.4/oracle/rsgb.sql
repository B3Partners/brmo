-- 
-- upgrade Oracle RSGB datamodel van 1.6.3 naar 1.6.4 
--

UPDATE aard_verkregen_recht SET omschr_aard_verkregenr_recht='Zakelijk recht als bedoeld in artikel 5, lid 3, onder b, van de Belemmeringenwet Privaatrecht' WHERE aand='10';
UPDATE aard_recht_verkort SET omschr='Zakelijk recht (als bedoeld in artikel 5, lid 3, onder b)' WHERE aand='10';
UPDATE aard_recht_verkort SET omschr='Zakelijk recht op gedeelte van perceel (als bedoeld in artikel 5, lid 3, onder b)' WHERE aand='24';

-- PR#617 verwijderen oude views
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME IN ('MB_KAD_ONRRND_ZK_ARCHIEF', 'MB_AVG_KOZ_RECHTH', 'MB_KOZ_RECHTH', 'MB_AVG_ZR_RECHTH', 'MB_ZR_RECHTH', 'MB_KAD_ONRRND_ZK_ADRES', 'MB_UTIL_APP_RE_KAD_PERCEEL', 'MB_AVG_SUBJECT', 'MB_SUBJECT', 'MB_BEN_OBJ_NEVENADRES', 'MB_BENOEMD_OBJ_ADRES', 'MB_PAND', 'MB_ADRES', 'VB_KAD_ONRRND_ZK_ARCHIEF', 'VB_AVG_KOZ_RECHTH', 'VB_KOZ_RECHTH', 'VB_AVG_ZR_RECHTH', 'VB_ZR_RECHTH', 'VB_UTIL_ZK_RECHT', 'VB_KAD_ONRRND_ZK_ADRES', 'VB_UTIL_APP_RE_KAD_PERCEEL', 'VB_UTIL_APP_RE_PARENT', 'VB_UTIL_APP_RE_PARENT_2', 'VB_UTIL_APP_RE_PARENT_3', 'VB_UTIL_APP_RE_SPLITSING', 'VB_AVG_SUBJECT', 'VB_SUBJECT', 'VB_BEN_OBJ_NEVENADRES', 'VB_BENOEMD_OBJ_ADRES', 'VB_PAND', 'VB_LIGPLAATS_ADRES', 'VB_STANDPLAATS_ADRES', 'VB_VBO_ADRES', 'VB_ADRES');
DROP MATERIALIZED VIEW mb_kad_onrrnd_zk_archief;
DROP MATERIALIZED VIEW mb_avg_koz_rechth;
DROP MATERIALIZED VIEW mb_koz_rechth;
DROP MATERIALIZED VIEW mb_avg_zr_rechth;
DROP MATERIALIZED VIEW mb_zr_rechth;
DROP MATERIALIZED VIEW mb_kad_onrrnd_zk_adres;
DROP MATERIALIZED VIEW mb_util_app_re_kad_perceel;
DROP MATERIALIZED VIEW mb_avg_subject;
DROP MATERIALIZED VIEW mb_subject;
DROP MATERIALIZED VIEW mb_ben_obj_nevenadres;
DROP MATERIALIZED VIEW mb_benoemd_obj_adres;
DROP MATERIALIZED VIEW mb_pand;
DROP MATERIALIZED VIEW mb_adres;
DROP VIEW vb_kad_onrrnd_zk_archief;
DROP VIEW vb_avg_koz_rechth;
DROP VIEW vb_koz_rechth;
DROP VIEW vb_avg_zr_rechth;
DROP VIEW vb_zr_rechth;
DROP VIEW vb_util_zk_recht;
DROP VIEW vb_kad_onrrnd_zk_adres;
DROP VIEW vb_util_app_re_kad_perceel;
DROP VIEW vb_util_app_re_parent;
DROP VIEW vb_util_app_re_parent_2;
DROP VIEW vb_util_app_re_parent_3;
DROP VIEW vb_util_app_re_splitsing;
DROP VIEW vb_avg_subject;
DROP VIEW vb_subject;
DROP view vb_ben_obj_nevenadres;
DROP VIEW vb_benoemd_obj_adres;
DROP VIEW vb_pand;
DROP VIEW vb_ligplaats_adres;
DROP VIEW vb_standplaats_adres;
DROP VIEW vb_vbo_adres;
DROP VIEW vb_adres;

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.6.3_naar_1.6.4','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';

CREATE TABLE zak_recht_archief
(
    kadaster_identif varchar2(255)NOT NULL,
    eindd_recht varchar2(255) ,
    indic_betrokken_in_splitsing varchar2(255) ,
    ingangsdatum_recht varchar2(19) NOT NULL,
    fk_7koz_kad_identif numeric(15,0),
    fk_8pes_sc_identif varchar2(255) ,
    ar_noemer numeric(8,0),
    ar_teller numeric(8,0),
    fk_2aard_recht_verkort_aand varchar2(4),
    fk_3avr_aand varchar2(6) ,
    CONSTRAINT zak_recht_archief_pk PRIMARY KEY (kadaster_identif,ingangsdatum_recht)
);


-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.4' WHERE naam='brmoversie';
