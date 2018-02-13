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
CREATE TABLE pm_p8_kadaster_perceel_tmp
(
  org_pk_id SERIAL UNIQUE,
  Pk_id numeric(8,0),
  linkid character varying(17),				--was Aanduiding
  kad_gem_code character varying(5),			--was x? is eigenlijk ka_kad_gemeentecode
  kad_gem_naam character varying(40), 
  kad_sectie character varying(255),
  kad_perceelnummer character varying(5),
  straat_locatie character varying(80),
  huisnummer_locatie numeric(5,0),
  toevoeging_locatie character varying(4),
  huisletter_locatie character varying(1),
  postcode_locatie character varying(6),
  woonplaats_locatie character varying(80),
  kad_opp_akr numeric(8,0),				--was grootte_perceel
  kad_opp_lki numeric(14,6),				--was oppervlakte_lki
  aard_bebouwd character varying(255),			--cu_aard_bebouwing
  aard_cultuur_onbebouwd character varying(65),		--cu_aard_cultuur_onbebouwd
  koopsom numeric(9,0),					--ks_bedrag
  koopjaar integer,					--ks_koopjaar
  meer_onroerendgoed character(1),			--ks_meer_onroerendgoed
  aand_soort_grootte character(1),
  omschr_deelperceel character varying(255),		--15
--  sc_kad_identif numeric(15,0),			--vervalt
  geldh_begindatum date,				--dat_beg_geldh
  geldh_einddatum date,					--datum_einde_geldh
  kad_identif numeric(15,0),
  geometry_data geometry(MultiPolygon,28992)			--begrenzing_perceel
-- ,CONSTRAINT pk_pm_p8_kadaster_perceel_tmp PRIMARY KEY (pk_id)
)
WITH (
  OIDS=FALSE
);

insert into pm_p8_kadaster_perceel_tmp (
  linkid ,
  kad_gem_code ,			--was x? is eigenlijk ka_kad_gemeentecode
  kad_gem_naam , 
  kad_sectie ,
  kad_perceelnummer ,
  straat_locatie,
  huisnummer_locatie,
  toevoeging_locatie,
  huisletter_locatie,
  postcode_locatie,
  woonplaats_locatie,
  kad_opp_akr ,				--was grootte_perceel
  kad_opp_lki ,				--was oppervlakte_lki
  aard_bebouwd ,			--cu_aard_bebouwing
  aard_cultuur_onbebouwd ,		--cu_aard_cultuur_onbebouwd
  koopsom ,				--ks_bedrag
  koopjaar ,				--ks_koopjaar
  meer_onroerendgoed ,			--ks_meer_onroerendgoed
  aand_soort_grootte ,
  omschr_deelperceel ,			--15
--  sc_kad_identif ,			--vervalt
  geldh_begindatum ,			--dat_beg_geldh
  geldh_einddatum ,			--datum_einde_geldh
  kad_identif ,
  geometry_data 

  ) --values
SELECT distinct  
 cast(map.ka_kad_gemeentecode || rpad((map.ka_sectie::text),2,' ') || lpad(map.ka_perceelnummer::text,5,'0') || 'G0000'  as varchar(17)) AS linkid			--aanduiding->linkid(2)
, map.ka_kad_gemeentecode			--ka_kad_gemeentecode->kad_gem_code(3)+"G0000"
, gem.naam 					--KAD_GEM_NAM(4)
, map.ka_sectie					--ka_sectie(5)
, map.ka_perceelnummer				as kad_perceelnummer --ka_perceelnummer->kad_perceelnummer(6)
, a.naam_openb_rmte            			AS straat_locatie
, a.huinummer                  			AS huisnummer_locatie
, a.huinummertoevoeging	       			AS toevoeging_locatie
, a.huisletter		       			as huisletter_locatie
, a.postcode                   			AS postcode_locatie
, a.wpl_naam                   			AS woonplaats_locatie
, map.grootte_perceel				--grootte_perceel->kad_opp_akr(7)
, 0						--oppervlakte_lki->kad_opp_lki(8)
, cu_aard_bebouwing				--cu_aard_bebouwing->aard_bebouwd(9)
, map.cu_aard_cultuur_onbebouwd			--cu_aard_cultuur_onbebouwd->aard_cultuur_onbebouwd(10)
, map.ks_bedrag					--ks_bedrag->koopsom(11)
, map.ks_koopjaar				--ks_koopjaar->koopjaar(12)
, map.ks_meer_onroerendgoed			--ks_meer_onroerendgoed->meer_ontroerendgoed(13)
, map.aand_soort_grootte 			--aand_soort_grootte(14)
, map.omschr_deelperceel			--omschr_deelperceel(15)
, cast (map.dat_beg_geldh as date)		--dat_beg_geldh->geldh_begindatum(16)
, cast(map.datum_einde_geldh as date)		--datum_einde_geldh->geldh_einddatum(17)
, map.kad_identif				--kad_identif(39)
, map.begrenzing_perceel			--begrenzing_perceel->geometry(40)
FROM pv_kad_perceel  map

 left join kad_gemeente gem
  on gem.code = map.ka_kad_gemeentecode

-- Adres
 LEFT JOIN pv_info_i_koz_adres_sk a
  ON a.koz_identif = map.sc_kad_identif;

  
UPDATE pm_p8_kadaster_perceel_tmp SET pk_id = org_pk_id;
 
  CREATE INDEX p8_kad_percgemcod_sectie_perceelnr_idx_tmp
  ON pm_p8_kadaster_perceel_tmp
    USING btree
  (kad_gem_code,kad_sectie,kad_perceelnummer);
  
--  DROP INDEX p8_kad_percgemnam_idx;
  CREATE INDEX p8_kad_percgemnam_idx_tmp
  ON pm_p8_kadaster_perceel_tmp
    USING btree
  (kad_gem_naam);

 -- DROP INDEX p8_kad_perckadsectie_idx;
  CREATE INDEX p8_kad_perckadsectie_idx_tmp
  ON pm_p8_kadaster_perceel_tmp
    USING btree
  (kad_sectie);

--  DROP INDEX p8_kad_perckoopjaar_idx;
  CREATE INDEX p8_kad_perckoopjaar_idx_tmp
  ON pm_p8_kadaster_perceel_tmp
    USING btree
  (koopjaar);

--  DROP INDEX p8_kad_percgeldhbegindatum_idx;
  CREATE INDEX p8_kad_percgeldhbegindatum_idx_tmp
  ON pm_p8_kadaster_perceel_tmp
    USING btree
  (geldh_begindatum);


--  DROP INDEX p8_kad_percaandeel_noemer_idx;
  CREATE INDEX p8_kad_perckad_identif_idx_tmp
  ON pm_p8_kadaster_perceel_tmp
    USING btree
  (kad_identif);

 -- tbv linkid
CREATE INDEX p8_kad_perclinkid_idx_tmp
  ON pm_p8_kadaster_perceel_tmp
  USING btree
  (linkid);
  
--geoindex
--  DROP INDEX p8_kad_percgeometrydata_idx;
  CREATE INDEX p8_kad_percgeometrydata_idx_tmp
  ON pm_p8_kadaster_perceel_tmp
  USING gist
  (geometry_data);

-- Oppervlakte berekenen
update pm_p8_kadaster_perceel_tmp set kad_opp_lki = st_area(geometry_data);

-- Omzetten van TMP naar 'normaal'
DROP TABLE pm_p8_kadaster_perceel cascade; 
ALTER TABLE pm_p8_kadaster_perceel_tmp RENAME TO pm_p8_kadaster_perceel;
ALTER TABLE pm_p8_kadaster_perceel ADD CONSTRAINT pk_pm_p8_kadaster_perceel PRIMARY KEY(pk_id);

-- Hernoemen indexen
ALTER INDEX p8_kad_percgemcod_sectie_perceelnr_idx_tmp RENAME TO p8_kad_percgemcod_sectie_perceelnr_idx;
ALTER INDEX p8_kad_percgemnam_idx_tmp RENAME TO p8_kad_percgemnam_idx;
ALTER INDEX p8_kad_perckadsectie_idx_tmp RENAME TO p8_kad_perckadsectie_idx;
ALTER INDEX p8_kad_perckoopjaar_idx_tmp RENAME TO p8_kad_perckoopjaar_idx;
ALTER INDEX p8_kad_percgeldhbegindatum_idx_tmp RENAME TO p8_kad_percgeldhbegindatum_idx;
ALTER INDEX p8_kad_perckad_identif_idx_tmp RENAME TO p8_kad_perckad_identif_idx;
ALTER INDEX p8_kad_perclinkid_idx_tmp RENAME TO p8_kad_perclinkid_idx;
ALTER INDEX p8_kad_percgeometrydata_idx_tmp RENAME TO p8_kad_percgeometrydata_idx;

grant select on pm_p8_kadaster_perceel to rsgb_lezer;
