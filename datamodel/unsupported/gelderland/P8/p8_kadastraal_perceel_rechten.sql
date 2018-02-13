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
/*
CREATE OR REPLACE VIEW vw_p8_kadastraal_perceel_rechten AS 
 SELECT 
	map.sc_kad_identif 		as kadperceelcode
,	zak.pso_identif			as subjectid
,	case when length(coalesce(naam_niet_natuurlijk_persoon,''))>0
	then naam_niet_natuurlijk_persoon
	else 
	trim(coalesce(geslachtsnaam,'') ||' '||coalesce(voorvoegsel,''))||', '||coalesce( voornamen,'') 
	end as subject_naam
,	soort_eigenaar			as subject_type
,	trim(substr(woonadres, length(woonadres)-strpos(reverse(woonadres),' ')+1,length(woonadres)))	as subject_woonplaats
,	coalesce(aandeel_teller::text,'') || '/' || coalesce(aandeel_noemer::text,'') as aandeel
,	rechtsvorm				as recht_soort
,	cast( ingangsdatum_recht as TIMESTAMP) 	as datum_ingang
,	cast(eindd_recht as TIMESTAMP) 		as datum_eind
FROM pv_map_i_kpe  map 
 inner join PV_INFO_I_KOZ_ZAK_RECHT_SK zak
 on map.sc_kad_identif = zak.koz_identif;
-- limit 100;
*/
  
  CREATE TABLE pm_p8_kadastraal_perceel_rechten_tmp AS
  SELECT * FROM vw_p8_kadastraal_perceel_rechten; --where 1=2

-- Technische PK voor tooling
ALTER TABLE pm_p8_kadastraal_perceel_rechten_tmp ADD column pm_p8_kadastraal_perceel_rechten_tmp_id serial;

CREATE UNIQUE INDEX pk_pm_p8_kadastraal_perceel_rechten_tmp ON pm_p8_kadastraal_perceel_rechten_tmp USING btree (pm_p8_kadastraal_perceel_rechten_tmp_id ASC);
  
  -- Extra indexen
  CREATE INDEX ix_kadastraal_perceel_rechten_subjectid_tmp ON pm_p8_kadastraal_perceel_rechten_tmp USING btree
    (
      subjectid ASC
    );
  
  CREATE INDEX ix_kadastraal_perceel_rechten_kadperceelcode_tmp ON pm_p8_kadastraal_perceel_rechten_tmp USING btree
    (
      kadperceelcode ASC
    );

-- Omzetten van TMP naar 'normaal'
DROP TABLE pm_p8_kadastraal_perceel_rechten;
ALTER TABLE pm_p8_kadastraal_perceel_rechten_tmp RENAME TO pm_p8_kadastraal_perceel_rechten;

-- Hernoemen indexen
ALTER INDEX pk_pm_p8_kadastraal_perceel_rechten_tmp RENAME TO pk_pm_p8_kadastraal_perceel_rechten;
ALTER INDEX ix_kadastraal_perceel_rechten_subjectid_tmp RENAME TO ix_kadastraal_perceel_rechten_subjectid;
ALTER INDEX ix_kadastraal_perceel_rechten_kadperceelcode_tmp RENAME TO ix_kadastraal_perceel_rechten_kadperceelcode;

ALTER TABLE pm_p8_kadastraal_perceel_rechten ADD CONSTRAINT pk_pm_p8_kadastraal_perceel_rechten PRIMARY KEY USING INDEX pk_pm_p8_kadastraal_perceel_rechten;

  
-- Rechten
   GRANT
  SELECT ON TABLE pm_p8_kadastraal_perceel_rechten TO rsgb_lezer;
