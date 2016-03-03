/****************************************************************
 ** Auteur	: S. Knoeff
 ** Versie 	: 1.1
 ** Datum	: 18-01-2016
 **
 ** Wijzigingen :
 ** Datum	Auteur		Soort
 ** xxxxxxxx	S.Knoeff	Initieel
 ** 18-01-2016	S.Knoeff	TMP tabel
 *****************************************************************/
--drop VIEW vw_p8_kadastraal_perceel_aantekeningen
/*
CREATE OR REPLACE VIEW vw_p8_kadastraal_perceel_aantekeningen
                            AS
  SELECT map.kadperceelcode AS kadperceelcode ,
    zak.kadaster_identif    AS aantekening_op --> 'recht' 'perceel' 'subject'
    ,
    aard_aantek_recht                     AS aantekening_aard ,
    --kadaster_identif_aantek_recht         
    case when map.objectindexletter='A' 
	then 'Appartementsrecht'
	else 'Perceel'
	end AS aantekening_recht_type , --> 'Perceel','Appartementsrecht'
    CAST(eindd_aantek_recht AS TIMESTAMP) AS datum_eind ,
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
    trim(SUBSTR(woonadres, LENGTH(woonadres)-strpos(reverse(woonadres),' ')+1,LENGTH(woonadres))) AS subject_woonplaats
  FROM vw_p8_kadastraal_perceel map -- pv_map_i_kpe
  INNER JOIN PV_INFO_I_KOZ_ZAK_RECHT_SK zak
  ON map.kadperceelcode = zak.koz_identif
  INNER JOIN pv_info_i_koz_zak_recht_aant aant
  ON aant.kadaster_identif = zak.kadaster_identif;
 */ 

CREATE TABLE pm_p8_kadastraal_perceel_aantekeningen_tmp AS
SELECT * FROM vw_p8_kadastraal_perceel_aantekeningen; --where 1=2

-- Technische PK voor tooling
ALTER TABLE pm_p8_kadastraal_perceel_aantekeningen_tmp ADD column pm_p8_kadastraal_perceel_aantekeningen_id serial;
CREATE UNIQUE INDEX pk_pm_p8_kadastraal_perceel_aantekeningen_tmp ON pm_p8_kadastraal_perceel_aantekeningen_tmp USING btree (pm_p8_kadastraal_perceel_aantekeningen_id ASC);

  -- Extra indexen
CREATE INDEX ix_kadastraal_perceel_aantekeningen_kadperceelcode_tmp ON pm_p8_kadastraal_perceel_aantekeningen_tmp USING btree
    (
      kadperceelcode ASC
    );

-- Omzetten van TMP naar 'normaal'
DROP TABLE pm_p8_kadastraal_perceel_aantekeningen;
ALTER TABLE pm_p8_kadastraal_perceel_aantekeningen_tmp RENAME TO pm_p8_kadastraal_perceel_aantekeningen;

-- Hernoemen indexen
ALTER INDEX pk_pm_p8_kadastraal_perceel_aantekeningen_tmp RENAME TO pk_pm_p8_kadastraal_perceel_aantekeningen;
ALTER INDEX ix_kadastraal_perceel_aantekeningen_kadperceelcode_tmp RENAME TO ix_kadastraal_perceel_aantekeningen_kadperceelcode;

ALTER TABLE pm_p8_kadastraal_perceel_aantekeningen ADD CONSTRAINT pk_pm_p8_kadastraal_perceel_aantekeningen PRIMARY KEY USING INDEX pk_pm_p8_kadastraal_perceel_aantekeningen;

-- Rechten
GRANT
SELECT ON TABLE pm_p8_kadastraal_perceel_aantekeningen TO rsgb_lezer;
