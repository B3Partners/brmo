/****************************************************************
 ** Auteur	: S. Knoeff
 ** Versie 	: 1.1
 ** Datum	: 14-01-2016
 **
 ** Wijzigingen :
 ** Datum	Auteur		Soort
 ** xxxxxxxx	S.Knoeff	Initieel
 ** 14-01-2016	S.Knoeff	zakelijk recht, left ipv inner join
 *****************************************************************/
 --DROP TABLE pm_p8_kadaster_perceel_eigenaar_tmp cascade; 
 
CREATE TABLE pm_p8_kadaster_perceel_eigenaar_tmp
(
  org_pk_id SERIAL UNIQUE,
  Pk_id numeric(8,0),
  linkid character varying(17),				--was Aanduiding
  kad_gem_code character varying(5),			--was x? is eigenlijk ka_kad_gemeentecode
  kad_gem_naam character varying(40), 
  kad_sectie character varying(255),
  kad_perceelnummer character varying(5),
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
  soort_eigenaar character varying(255),
  soort_recht character varying(100),			--arv_omschr
  recht_ingangsdatum date, 				--ingangsdatum_recht
  recht_einddatum date,					--eindd_recht
  geslacht character varying(1),			
  voornamen character varying(200),			
  voorvoegsel character varying(10),			
  geslachtsnaam character varying(200),			
  woonadres character varying(255),
  geboorteplaats character varying(40),
  geboortedatum numeric(8,0),
  overlijdensdatum numeric(8,0),
  naam_niet_nat_persoon character varying(255),		--naam_niet_natuurlijk_persoon
  rechtsvorm character varying(50),
  statutaire_zetel character varying(40),
  kvk_nummer numeric(8,0),
  aandeel_noemer numeric(8,0),
  aandeel_teller numeric(8,0),
  betrokken_in_splitsing character varying(255),	--ind_betrokken_in_splitsing
  pso_identif character varying(32),
  kad_identif numeric(15,0),
--koz_identif numeric(15,0),
--fk_3avr_aand character varying(6) 
  geometry_data geometry(MultiPolygon,28992)			--begrenzing_perceel
-- ,CONSTRAINT pk_pm_p8_kadaster_perceel_eigenaar_tmp PRIMARY KEY (pk_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE pm_p8_kadaster_perceel_eigenaar_tmp
  OWNER TO cgv;

insert into pm_p8_kadaster_perceel_eigenaar_tmp (
  linkid ,
  kad_gem_code ,			--was x? is eigenlijk ka_kad_gemeentecode
  kad_gem_naam , 
  kad_sectie ,
  kad_perceelnummer ,
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
  soort_eigenaar ,
  soort_recht ,				--arv_omschr
  recht_ingangsdatum ,	 		--ingangsdatum_recht
  recht_einddatum ,			--eindd_recht
  geslacht ,			
  voornamen ,			
  voorvoegsel ,			
  geslachtsnaam ,			
  woonadres ,
  geboorteplaats ,
  geboortedatum ,
  overlijdensdatum ,
  naam_niet_nat_persoon ,		--naam_niet_natuurlijk_persoon
  rechtsvorm ,
  statutaire_zetel ,
  kvk_nummer ,
  aandeel_noemer ,
  aandeel_teller ,
  betrokken_in_splitsing ,
  pso_identif ,
  kad_identif ,
  geometry_data 

  ) --values
SELECT distinct  
 cast(map.ka_kad_gemeentecode || rpad((map.ka_sectie::text),2,' ') || lpad(map.ka_perceelnummer::text,5,'0') || 'G0000'  as varchar(17)) AS linkid			--aanduiding->linkid(2)
, map.ka_kad_gemeentecode			--ka_kad_gemeentecode->kad_gem_code(3)+"G0000"
, gem.naam 					--KAD_GEM_NAM(4)
, map.ka_sectie					--ka_sectie(5)
, map.ka_perceelnummer				as kad_perceelnummer --ka_perceelnummer->kad_perceelnummer(6)
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
, cast (soort_eigenaar as varchar(255)) 	as soort_eigenaar				--soort_eigenaar(18)
, arv_omschr					--arv_omschr->soort_recht(19)
, cast (zak.ingangsdatum_recht as date)	as geldh_begindatum --ingangsdatum_recht->recht_begindatum(20)
, cast(zak.eindd_recht as date)	as geldh_einddatum  --eindd_recht->recht_einddatum(21)
, geslacht					--(22)
, voornamen					--(23)
, voorvoegsel					--(24)
, geslachtsnaam					--geslachtsnaam(25)
, woonadres					--(26)
, geboorteplaats				--(27)
, geboortedatum 				--(28)
, overlijdensdatum 				--overlijdensdatum(29)
, naam_niet_natuurlijk_persoon			--naam_niet_natuurlijk_persoon->naam_niet_nat_persoon(30)
, rechtsvorm					--rechtsvorm(31)
, statutaire_zetel				--statutaire_zetel(33)
, kvk_nummer					--kvk_nummer(34)
, aandeel_noemer				--aandeel_noemer(35)
, aandeel_teller				--aandeel_teller(36)
, indic_betrokken_in_splitsing 			--indic_betrokken_in_splitsing->betrokken_in_splitsing(37)
, pso_identif					--pso_identif(38)
, map.kad_identif				--kad_identif(39)
, map.begrenzing_perceel			--begrenzing_perceel->geometry(40)
FROM pv_kad_perceel  map

 inner join PV_INFO_I_KOZ_ZAK_RECHT_SK zak
  on zak.koz_identif = map.sc_kad_identif

 left join kad_gemeente gem
  on gem.code = map.ka_kad_gemeentecode;

--tbv Primary Key
UPDATE pm_p8_kadaster_perceel_eigenaar_tmp SET pk_id = org_pk_id;

  CREATE INDEX p8_kad_perc_eig_gemcod_sectie_perceelnr_idx_tmp
  ON pm_p8_kadaster_perceel_eigenaar_tmp
    USING btree
  (kad_gem_code,kad_sectie,kad_perceelnummer);
  
  CREATE INDEX p8_kad_perc_eig_gemnam_idx_tmp
  ON pm_p8_kadaster_perceel_eigenaar_tmp
    USING btree
  (kad_gem_naam);

  CREATE INDEX p8_kad_perc_eig_kadsectie_idx_tmp
  ON pm_p8_kadaster_perceel_eigenaar_tmp
    USING btree
  (kad_sectie);

  CREATE INDEX p8_kad_perc_eig_koopjaar_idx_tmp
  ON pm_p8_kadaster_perceel_eigenaar_tmp
    USING btree
  (koopjaar);

  CREATE INDEX p8_kad_perc_eig_geldhbegindatum_idx_tmp
  ON pm_p8_kadaster_perceel_eigenaar_tmp
    USING btree
  (geldh_begindatum);

  CREATE INDEX p8_kad_perc_eig_rechtsvorm_idx_tmp
  ON pm_p8_kadaster_perceel_eigenaar_tmp
    USING btree
  (rechtsvorm);

  CREATE INDEX p8_kad_perc_eig_aandeel_noemer_idx_tmp
  ON pm_p8_kadaster_perceel_eigenaar_tmp
    USING btree
  (aandeel_noemer);

  CREATE INDEX p8_kad_perc_eig_kad_identif_idx_tmp
  ON pm_p8_kadaster_perceel_eigenaar_tmp
    USING btree
  (kad_identif);

 -- tbv linkid
CREATE INDEX p8_kad_perc_eig_linkid_idx_tmp
  ON pm_p8_kadaster_perceel_eigenaar_tmp
  USING btree
  (linkid);
  
--geoindex
  CREATE INDEX p8_kad_perc_eig_geometrydata_idx_tmp
  ON pm_p8_kadaster_perceel_eigenaar_tmp
  USING gist
  (geometry_data);

-- Bereken oppervlakte aan hand van geometrie
update pm_p8_kadaster_perceel_eigenaar_tmp set kad_opp_lki = st_area(geometry_data);

/****************************************************************
 **
 ** Stap 2: omzetten TMP --> pm_p8_kadaster_perceel_eigenaar
 **
 *****************************************************************/
DROP TABLE pm_p8_kadaster_perceel_eigenaar cascade; 
ALTER TABLE pm_p8_kadaster_perceel_eigenaar_tmp RENAME TO pm_p8_kadaster_perceel_eigenaar;
ALTER TABLE pm_p8_kadaster_perceel_eigenaar ADD CONSTRAINT pk_pm_p8_kadaster_perceel_eigenaar PRIMARY KEY(pk_id);

--Hernoemen indexen
ALTER INDEX  p8_kad_perc_eig_gemcod_sectie_perceelnr_idx_tmp RENAME TO p8_kad_perc_eig_gemcod_sectie_perceelnr_idx;
ALTER INDEX  p8_kad_perc_eig_gemnam_idx_tmp RENAME TO p8_kad_perc_eig_gemnam_idx;
ALTER INDEX  p8_kad_perc_eig_kadsectie_idx_tmp RENAME TO p8_kad_perc_eig_kadsectie_idx;
ALTER INDEX  p8_kad_perc_eig_koopjaar_idx_tmp RENAME TO p8_kad_perc_eig_koopjaar_idx;
ALTER INDEX  p8_kad_perc_eig_geldhbegindatum_idx_tmp RENAME TO p8_kad_perc_eig_geldhbegindatum_idx;
ALTER INDEX  p8_kad_perc_eig_rechtsvorm_idx_tmp RENAME TO p8_kad_perc_eig_rechtsvorm_idx;
ALTER INDEX  p8_kad_perc_eig_kad_identif_idx_tmp RENAME TO p8_kad_perc_eig_kad_identif_idx;
ALTER INDEX  p8_kad_perc_eig_linkid_idx_tmp RENAME TO p8_kad_perc_eig_linkid_idx;
ALTER INDEX  p8_kad_perc_eig_aandeel_noemer_idx_tmp RENAME to p8_kad_perc_eig_aandeel_noemer_idx;
ALTER INDEX  p8_kad_perc_eig_geometrydata_idx_tmp RENAME TO p8_kad_perc_eig_geometrydata_idx;

-- Rechten
grant select on pm_p8_kadaster_perceel_eigenaar to rsgb_lezer;
