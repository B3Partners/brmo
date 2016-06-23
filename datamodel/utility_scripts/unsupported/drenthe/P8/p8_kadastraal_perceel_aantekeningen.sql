--drop VIEW vw_p8_kadastraal_perceel_aantekeningen
CREATE OR REPLACE VIEW vw_p8_kadastraal_perceel_aant
                            AS
  SELECT map.kadperceelcode AS kadperceelcode ,
    zak.pso_identif    AS aantekening_op --> 'recht' 'perceel' 'subject'
    ,
    aard_aantek_recht                     AS aantekening_aard ,
    --kadaster_identif_aantek_recht         
    case when map.objectindexletter='A' 
	then 'Appartementsrecht'
	else 'Perceel'
	end AS aantekening_recht_type , --> 'Perceel','Appartementsrecht'
    f_datum(AANT.BEGINDATUM_AANTEK_RECHT ) AS datum_ingang ,
    f_datum(aant.eindd_aantek_recht) AS datum_eind ,
    CASE
      WHEN LENGTH(COALESCE(naam_niet_natuurlijk_persoon,''))>0
      THEN naam_niet_natuurlijk_persoon
      ELSE trim(COALESCE(geslachtsnaam,'')
        ||' '
        ||COALESCE(voorvoegsel,''))
        ||', '
        ||COALESCE( voornamen,'')
    END                                                                                           AS subject_naam ,
    soort_eigenaar                                                                                AS subject_type ,
    zak.pso_identif                                                                               AS subjectid ,
    trim(SUBSTR(woonadres, LENGTH(woonadres)-instr(reverse(woonadres),' ')+1,LENGTH(woonadres))) AS subject_woonplaats
  FROM vw_p8_kadastraal_perceel map -- pv_map_i_kpe
  INNER JOIN PV_INFO_I_KOZ_ZAK_RECHT zak
  ON map.kadperceelcode = zak.koz_identif
  INNER JOIN pv_info_i_koz_zak_recht_aant aant
  ON aant.koz_identif = zak.koz_identif;
     
DROP TABLE pm_p8_kadastraal_perceel_aant;
CREATE TABLE pm_p8_kadastraal_perceel_aant AS
SELECT * FROM vw_p8_kadastraal_perceel_aant where 1=2;

insert into pm_p8_kadastraal_perceel_aant
(
   KADPERCEELCODE,
   AANTEKENING_OP,
   AANTEKENING_AARD,
   AANTEKENING_RECHT_TYPE,
   DATUM_INGANG,
   DATUM_EIND,
   SUBJECT_NAAM,
   SUBJECT_TYPE,
   SUBJECTID,
   SUBJECT_WOONPLAATS
)
SELECT * FROM vw_p8_kadastraal_perceel_aant;
commit;
-- Technische PK voor tooling
ALTER TABLE pm_p8_kadastraal_perceel_aant ADD  pm_p8_kad_perceel_aant_id integer generated always as identity;;
CREATE UNIQUE INDEX pk_pm_p8_kad_perceel_aant ON pm_p8_kadastraal_perceel_aant (pm_p8_kad_perceel_aant_id ASC);
ALTER TABLE pm_p8_kadastraal_perceel_aant ADD CONSTRAINT pk_pm_p8_kad_perceel_aant PRIMARY KEY (pm_p8_kad_perceel_aant_id);

  -- Extra indexen
CREATE INDEX ix_kad_perceel_aant_kpcode ON pm_p8_kadastraal_perceel_aant
    (
      kadperceelcode ASC
    );

GRANT
SELECT ON  pm_p8_kadastraal_perceel_aant TO brmo_rsgbuser;
