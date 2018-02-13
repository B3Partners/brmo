-- Archief hulp views
CREATE OR REPLACE VIEW pv_appartementsrecht_archief AS 
 SELECT b.ka_appartementsindex, b.ka_kad_gemeentecode, b.ka_perceelnummer, b.ka_sectie, b.sc_kad_identif, a.cu_aard_cultuur_onbebouwd, a.dat_beg_geldh, a.datum_einde_geldh, a.ks_bedrag, a.ks_koopjaar, a.ks_meer_onroerendgoed
   FROM kad_onrrnd_zk_archief a
   JOIN app_re_archief b ON a.kad_identif = b.sc_kad_identif;

CREATE OR REPLACE VIEW pv_info_i_koz_zak_recht_archief AS 
 SELECT koz.kad_identif AS koz_identif, zre.eindd_recht, zre.indic_betrokken_in_splitsing, zre.ingangsdatum_recht, pso.sc_identif AS pso_identif, 
        CASE
            WHEN npe.sc_identif IS NOT NULL THEN 'Natuurlijk persoon'::text
            ELSE 'Niet natuurlijk persoon'::text
        END AS soort_eigenaar, npe.nm_geslachtsnaam AS geslachtsnaam, npe.nm_voorvoegsel_geslachtsnaam AS voorvoegsel, npe.nm_voornamen AS voornamen, npe.geslachtsaand AS geslacht, inp.va_loc_beschrijving AS woonadres, inp.gb_geboortedatum AS geboortedatum, inp.gb_geboorteplaats AS geboorteplaats, inp.ol_overlijdensdatum AS overlijdensdatum, nnp.nnp_naam AS naam_niet_natuurlijk_persoon, nnp.rechtsvorm, nnp.statutaire_zetel, nnp.kvk_nummer, zre.ar_noemer AS aandeel_noemer, zre.ar_teller AS aandeel_teller, zre.arv_omschr, zre.fk_3avr_aand
   FROM pv_kad_onroerende_zaak_archief koz
   JOIN pv_zakelijk_recht zre ON zre.fk_7koz_kad_identif = koz.kad_identif -- geen archief aanwezig
   LEFT JOIN pv_persoon pso ON pso.sc_identif::text = zre.fk_8pes_sc_identif::text -- geen archief aanwezig
   LEFT JOIN pv_natuurlijk_persoon npe ON npe.sc_identif::text = pso.sc_identif::text -- geen archief aanwezig
   LEFT JOIN pv_ingeschr_natuurlijk_persoon inp ON inp.sc_identif::text = pso.sc_identif::text -- geen archief aanwezig
   LEFT JOIN pv_niet_natuurlijk_persoon nnp ON nnp.sc_identif::text = pso.sc_identif::text -- geen archief aanwezig
  WHERE npe.nm_geslachtsnaam IS NOT NULL OR nnp.nnp_naam IS NOT NULL;


/*
SELECT cu_aard_bebouwing, sc_kad_identif, aand_soort_grootte, grootte_perceel, 
       omschr_deelperceel, ka_kad_gemeentecode, ka_perceelnummer, ka_sectie, 
       begrenzing_perceel, aanduiding, cu_aard_cultuur_onbebouwd, dat_beg_geldh, 
       datum_einde_geldh, kad_identif, ks_bedrag, ks_koopjaar, ks_meer_onroerendgoed
       , zak.*
  FROM pv_map_i_kpe  map left join
  PV_INFO_I_KOZ_ZAK_RECHT zak
  on map.sc_kad_identif = zak.koz_identif
  limit 10; 
*/

--DROP TABLE pm_kadaster_perceel_eigenaar_archief_v2;
--DROP TABLE pm_kadaster_perceel_v2;

DROP TABLE pm_kadaster_perceel_eigenaar_archief cascade; 
 
CREATE TABLE pm_kadaster_perceel_eigenaar_archief
(
  org_pk_id SERIAL UNIQUE,
  Pk_id numeric(8,0),
  linkid character varying(17),				--was Aanduiding
  kad_gem_code character varying(5),			-- Kadaster gemeentecode
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
 --aard_bebouwd character varying(255),			--cu_aard_bebouwing
  aard_cultuur_onbebouwd character varying(65),		--cu_aard_cultuur_onbebouwd
  koopsom numeric(9,0),					--ks_bedrag
  koopjaar integer,					--ks_koopjaar
  meer_onroerendgoed character(1),			--ks_meer_onroerendgoed
  aand_soort_grootte character(1),
  omschr_deelperceel character varying(255),		--15
  geldh_begindatum character(10),			--dat_beg_geldh
  geldh_einddatum character(10),			--datum_einde_geldh
  soort_eigenaar character varying(255),
  soort_recht character varying(100),			--arv_omschr
  recht_ingangsdatum date, 				--ingangsdatum_recht
  recht_einddatum date,					--eindd_recht
  bsn_nummer character varying(20),
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
  geometry_data geometry(MultiPolygon,28992),			--begrenzing_perceel
  index_letter char(1),
  index_nummer char(4) ,
  burg_gem_code numeric(4,0),
  burg_gem_naam character varying(40)
--  indicatie_overleden char(1)
-- ,CONSTRAINT pk_pm_kadaster_perceel_eigenaar_archief PRIMARY KEY (pk_id)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE pm_kadaster_perceel_eigenaar_archief
  OWNER TO cgv;

insert into pm_kadaster_perceel_eigenaar_archief (
  linkid ,
  kad_gem_code ,			--is eigenlijk kadaster gemeentecode
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
  --aard_bebouwd ,			--cu_aard_bebouwing
  aard_cultuur_onbebouwd ,		--cu_aard_cultuur_onbebouwd
  koopsom ,				--ks_bedrag
  koopjaar ,				--ks_koopjaar
  meer_onroerendgoed ,			--ks_meer_onroerendgoed
  aand_soort_grootte ,
  omschr_deelperceel ,			--15
  geldh_begindatum ,			--dat_beg_geldh
  geldh_einddatum ,			--datum_einde_geldh
  soort_eigenaar ,
  soort_recht ,				--arv_omschr
  recht_ingangsdatum ,	 		--ingangsdatum_recht
  recht_einddatum ,			--eindd_recht
  bsn_nummer,
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
  geometry_data, 
  index_letter,
  index_nummer,
  burg_gem_code,
  burg_gem_naam
-- indicatie_overleden
  ) --values

 with cte_appartements_index
  as
  (
  SELECT sc_kad_identif, ka_appartementsindex 
  FROM v_bd_app_re_bij_perceel
  )
  , cte_complex
  as
  (
  select perceel_identif from v_bd_app_re_all_kad_perceel
  )
SELECT distinct
 cast(case when ar.sc_kad_identif is null
	then map.ka_kad_gemeentecode || rpad((map.ka_sectie::text),2,' ') || lpad(map.ka_perceelnummer::text,5,'0') || 'G0000'	
	else ar.ka_kad_gemeentecode  || rpad((ar.ka_sectie::text),2,' ') || lpad(ar.ka_perceelnummer::text,5,'0')|| 'A'||lpad(ai.ka_appartementsindex,4,'0')
  end as varchar(17)) AS linkid			--aanduiding->linkid(2)
, case when ar.sc_kad_identif is null
	then map.ka_kad_gemeentecode			
	else ar.ka_kad_gemeentecode	
	end 					--ka_kad_gemeentecode->kad_gem_code(3)+"G0000"
, gem.naam 					--KAD_GEM_NAM(4)
, case when ar.sc_kad_identif is null
	then map.ka_sectie
	else ar.ka_sectie
	end					--ka_sectie(5)
, case when ar.sc_kad_identif is null
	then map.ka_perceelnummer
	else ar.ka_perceelnummer
	end					as kad_perceelnummer --ka_perceelnummer->kad_perceelnummer(6)
, a.naam_openb_rmte            			AS straat_locatie
, a.huinummer                  			AS huisnummer_locatie
, a.huinummertoevoeging	       			AS toevoeging_locatie
, a.huisletter		       			as huisletter_locatie
, a.postcode                   			AS postcode_locatie
, a.wpl_naam                   			AS woonplaats_locatie
, case when ar.sc_kad_identif is null
	then map.grootte_perceel
	else pma.grootte_perceel
	end					--grootte_perceel->kad_opp_akr(7)
, 0						--oppervlakte_lki->kad_opp_lki(8)
--, cu_aard_bebouwing				--cu_aard_bebouwing->aard_bebouwd(9)
, onrzk.cu_aard_cultuur_onbebouwd		--cu_aard_cultuur_onbebouwd->aard_cultuur_onbebouwd(10)
, case when ar.sc_kad_identif is null
	then map.ks_bedrag					
	else ar.ks_bedrag
	end					--ks_bedrag->koopsom(11)
, onrzk.ks_koopjaar				--ks_koopjaar->koopjaar(12)
, onrzk.ks_meer_onroerendgoed			--ks_meer_onroerendgoed->meer_ontroerendgoed(13)
, case when ar.sc_kad_identif is null
	then map.aand_soort_grootte 
	else pma.aand_soort_grootte
	end	  				--aand_soort_grootte(14)
, case when ar.sc_kad_identif is null
	then map.omschr_deelperceel 
	else pma.omschr_deelperceel
	end					--omschr_deelperceel(15)
, case when strpos(onrzk.dat_beg_geldh,'-') = 3 then
		to_char(to_date(onrzk.dat_beg_geldh,'dd-mm-yyyy'),'yyyy-mm-dd')
		else onrzk.dat_beg_geldh
		end 				AS dat_beg_geldh		 --begin kadastrale onroerende zaak dat_beg_geldh->geldh_begindatum(16) 
, case when strpos(onrzk.datum_einde_geldh,'-') = 3 then
		to_char(to_date(onrzk.datum_einde_geldh,'dd-mm-yyyy'),'yyyy-mm-dd')
		else onrzk.datum_einde_geldh
		end 				AS datum_einde_geldh		--datum_einde_geldh->geldh_einddatum(17)
, cast (soort_eigenaar as varchar(255)) 	as soort_eigenaar		--soort_eigenaar(18)
, arv_omschr					--arv_omschr->soort_recht(19)
, cast(null as date)--ar.dat_beg_geldh		-- begin kadastrale onroerende zaak, niet het recht mail 12-10-2015
						as geldh_begindatum --ingangsdatum_recht->recht_begindatum(20)
, cast(null  as date)--ar.datum_einde_geldh
						as geldh_einddatum  --eindd_recht->recht_einddatum(21)
, 'Onbekend' 					as 	bsn_nummer
, geslacht					--(22)
, voornamen					--(23)
, voorvoegsel					--(24)
, geslachtsnaam					--geslachtsnaam(25)
, woonadres					--(26)
, geboorteplaats				--(27)
, geboortedatum					--(28)
, overlijdensdatum				--overlijdensdatum(29)
, naam_niet_natuurlijk_persoon			--naam_niet_natuurlijk_persoon->naam_niet_nat_persoon(30)
, rechtsvorm					--rechtsvorm(31)
, statutaire_zetel				--statutaire_zetel(33)
, kvk_nummer					--kvk_nummer(34)
, aandeel_noemer				--aandeel_noemer(35)
, aandeel_teller				--aandeel_teller(36)
, indic_betrokken_in_splitsing 			--indic_betrokken_in_splitsing->betrokken_in_splitsing(37)
, pso_identif					--pso_identif(38)
, onrzk.kad_identif				--kad_identif(39)
, case when ar.sc_kad_identif is null
	then map.begrenzing_perceel
	else pma.begrenzing_perceel
	end					--begrenzing_perceel->geometry(40)
, case 	when ar.sc_kad_identif is null and cc.perceel_identif is null and map.omschr_deelperceel is null
	then 'G' 
	when ar.sc_kad_identif is null and cc.perceel_identif is not null and map.omschr_deelperceel is null
	then 'C'
	when map.omschr_deelperceel is not null -- Deel bepalen aan hand van aanwezigheid omschrijving
	then 'D'
	else 'A' 				
	end 					as index_letter
, case 	when ar.sc_kad_identif is null
	then '0000'
	else  lpad(ai.ka_appartementsindex,4,'0')
	end 					as index_nummer
, -1 --aon.gem_code 					as burg_gem_code --'0000'
, 'Onbekend' --aon.gem_naam 					as burg_gem_naam
--, case when overlijdensdatum is not null then 'J' else 'N' end as indicatie_overleden
FROM pv_kad_onroerende_zaak_archief onrzk
  left join pv_kad_perceel_archief  map
 on map.sc_kad_identif = onrzk.kad_identif

-- Appartementen
 LEFT JOIN pv_appartementsrecht_archief ar
  on onrzk.kad_identif = ar.sc_kad_identif

 -- Koppeling appartement met perceel
 left join v_bd_app_re_all_kad_perceel akp		-- geen archief aanwezig
  on ar.sc_kad_identif::text = akp.app_re_identif::text

	-- Grootte perceel / geometrie
	left join v_bd_kad_perceel_met_app pma		-- geen archief aanwezig
	on pma.sc_kad_identif::text = akp.perceel_identif::text

	-- Appartements index
	left join cte_appartements_index ai		-- geen archief aanwezig
	on ar.sc_kad_identif = ai.sc_kad_identif

-- Complex
left join  cte_complex cc				-- geen archief aanwezig
on cc.perceel_identif = onrzk.kad_identif::text

-- Zakelijk recht
 left join PV_INFO_I_KOZ_ZAK_RECHT zak 			-- geen archief aanwezig
  on zak.koz_identif = (case when ar.sc_kad_identif is null
			then map.sc_kad_identif
			else ar.sc_kad_identif
			end )

-- Adres
LEFT JOIN pv_info_i_koz_adres a				-- geen archief aanwezig
  ON a.koz_identif = (case when ar.sc_kad_identif is null
			then map.sc_kad_identif
			else ar.sc_kad_identif
			end )

-- Kadastrale gemeente
 left join kad_gemeente gem 				-- geen archief aanwezig
  on gem.code = (case when ar.sc_kad_identif is null
			then map.ka_kad_gemeentecode
			else ar.ka_kad_gemeentecode
			end )
;

/* Traag 

-- Gemeente naam en code via bag
--left join pv_adr_object_nummeraand aon
-- on aon.huinummer = a.huinummer
-- and aon.postcode = a.postcode
 
 left join wnplts wp
  on st_within( case when ar.sc_kad_identif is null
			then map.begrenzing_perceel
			else pma.begrenzing_perceel
			end
		, wp.geom )

left join gemeente gemgem
 on gemgem.code = wp.fk_7gem_code;
 */

UPDATE pm_kadaster_perceel_eigenaar_archief SET pk_id = org_pk_id;

ALTER TABLE pm_kadaster_perceel_eigenaar_archief ADD CONSTRAINT pk_pm_kadaster_perceel_eigenaar_archief PRIMARY KEY(pk_id);


 -- DROP INDEX kad_perc_eig_gemcod_idx;
  
  CREATE INDEX kad_perc_eig_arch_gemcod_idx
  ON pm_kadaster_perceel_eigenaar_archief
    USING btree
  (kad_gem_code);

--  DROP INDEX kad_perc_eig_gemnam_idx;
  CREATE INDEX kad_perc_eig_arch_gemnam_idx
  ON pm_kadaster_perceel_eigenaar_archief
    USING btree
  (kad_gem_naam);

 -- DROP INDEX kad_perc_eig_kadsectie_idx;
  CREATE INDEX kad_perc_eig_arch_kadsectie_idx
  ON pm_kadaster_perceel_eigenaar_archief
    USING btree
  (kad_sectie);

--  DROP INDEX kad_perc_eig_koopjaar_idx;
  CREATE INDEX kad_perc_eig_arch_koopjaar_idx
  ON pm_kadaster_perceel_eigenaar_archief
    USING btree
  (koopjaar);

--  DROP INDEX kad_perc_eig_geldhbegindatum_idx;
  CREATE INDEX kad_perc_eig_arch_geldhbegindatum_idx
  ON pm_kadaster_perceel_eigenaar_archief
    USING btree
  (geldh_begindatum);

--  DROP INDEX kad_perc_eig_rechtsvorm_idx;
  CREATE INDEX kad_perc_eig_arch_rechtsvorm_idx
  ON pm_kadaster_perceel_eigenaar_archief
    USING btree
  (rechtsvorm);

--  DROP INDEX kad_perc_eig_aandeel_noemer_idx;
  --CREATE INDEX kad_perc_eig_aandeel_noemer_idx
  --ON pm_kadaster_perceel_eigenaar_archief
  --  USING btree
  --(aandeel_noemer);

--  DROP INDEX kad_perc_eig_aandeel_noemer_idx;
  CREATE INDEX kad_perc_eig_arch_kad_identif_idx
  ON pm_kadaster_perceel_eigenaar_archief
    USING btree
  (kad_identif);

 -- tbv linkid
CREATE INDEX kad_perc_eig_arch_linkid_idx
  ON pm_kadaster_perceel_eigenaar_archief
  USING btree
  (linkid);
 
-- tbv Adres 
CREATE INDEX kad_perc_eig_arch_postcode_locatie_huisnummer_locatie_idx
  ON pm_kadaster_perceel_eigenaar_archief
  USING btree
  (postcode_locatie, huisnummer_locatie);

update pm_kadaster_perceel_eigenaar_archief pe set 
 burg_gem_code=aon.gem_code, burg_gem_naam=aon.gem_naam
 from  pv_adr_object_nummeraand aon
  where aon.huinummer = huisnummer_locatie
  and aon.postcode = postcode_locatie;
  
--geoindex
--  DROP INDEX kad_perc_eig_geometrydata_idx;
  CREATE INDEX kad_perc_eig_arch_geometrydata_idx
  ON pm_kadaster_perceel_eigenaar_archief
  USING gist
  (geometry_data);

-- Oppervlakte berekenen
update pm_kadaster_perceel_eigenaar_archief set kad_opp_lki = st_area(geometry_data);

grant select on pm_kadaster_perceel_eigenaar_archief to rsgb_lezer;



 /****************************************************************
 **
 ** Stap 2: vullen
 **
 *****************************************************************/
 DROP TABLE pm_kadaster_perceel_archief cascade; 
 
CREATE TABLE pm_kadaster_perceel_archief
(
  org_pk_id SERIAL UNIQUE,
  pk_id numeric(8,0),
  linkid character varying(17),				--was Aanduiding	datatype text
  kad_gem_code character varying(5),			--was x? is eigenlijk ka_kad_gemeentecode
  kad_gem_naam character varying(40), 
  kad_sectie character varying(255),
  kad_perceelnummer character varying(5),
  kad_opp_akr numeric(8,0),				--was grootte_perceel
  kad_opp_lki numeric(14,6),				--was oppervlakte_lki
  aard_cultuur_onbebouwd character varying(65),		--cu_aard_cultuur_onbebouwd
  koopsom numeric(9,0),					--ks_bedrag
  koopjaar integer,					--ks_koopjaar
  meer_onroerendgoed character(1),			--ks_meer_onroerendgoed
  aand_soort_grootte character(1),
  omschr_deelperceel character varying(1120),		--15
  geldh_begindatum character varying(19),		--dat_beg_geldh
  geldh_einddatum character varying(19)	,		--datum_einde_geldh
  kad_identif numeric(15,0) ,
  geometry_data geometry(MultiPolygon,28992),		--begrenzing_perceel
   index_letter char(1),
 index_nummer char(4) ,
 burg_gem_code numeric(4,0),
 burg_gem_naam character varying(40)
-- indicatie_overleden char(1)
 --,CONSTRAINT pk_pm_kadaster_perceel PRIMARY KEY (pk_id)  
)
WITH (
  OIDS=FALSE
);
ALTER TABLE pm_kadaster_perceel_archief
  OWNER TO cgv;

insert into pm_kadaster_perceel_archief (
  linkid ,
  kad_gem_code ,			--kadaster_gemeentecode
  kad_gem_naam , 
  kad_sectie ,
  kad_perceelnummer ,
  kad_opp_akr ,				--was grootte_perceel
  kad_opp_lki ,				--was oppervlakte_lki
  aard_cultuur_onbebouwd ,		--cu_aard_cultuur_onbebouwd
  koopsom ,				--ks_bedrag
  koopjaar ,				--ks_koopjaar
  meer_onroerendgoed ,			--ks_meer_onroerendgoed
  aand_soort_grootte ,
  omschr_deelperceel ,			--15
  geldh_begindatum ,			--dat_beg_geldh
  geldh_einddatum ,			--datum_einde_geldh
  kad_identif ,
  geometry_data,
   index_letter,
 index_nummer,
 burg_gem_code,
 burg_gem_naam
-- indicatie_overleden
) --values
SELECT distinct
  pe.linkid ,
  pe.kad_gem_code ,			
  pe.kad_gem_naam , 
  pe.kad_sectie ,
  pe.kad_perceelnummer ,
  pe.kad_opp_akr ,				
  pe.kad_opp_lki ,				
  pe.aard_cultuur_onbebouwd ,		
  pe.koopsom ,				
  pe.koopjaar ,				
  pe.meer_onroerendgoed ,			
  pe.aand_soort_grootte ,
  pe.omschr_deelperceel ,			
  pe.geldh_begindatum ,			
  pe.geldh_einddatum ,
  pe.kad_identif ,
  pe.geometry_data,			
  index_letter,
  index_nummer,
  burg_gem_code,
  burg_gem_naam
-- indicatie_overleden
FROM pm_kadaster_perceel_eigenaar_archief pe;

--where not exists (select null 
--		from  v_bd_app_re_bij_perceel ap
--		where pe.kad_identif = ap.sc_kad_identif);

--index
--  CREATE INDEX kad_perc_objectid_idx
--  ON pm_kadaster_perceel
--    USING btree
--  (pk_id);
UPDATE pm_kadaster_perceel_archief SET pk_id = org_pk_id;

ALTER TABLE pm_kadaster_perceel_archief ADD CONSTRAINT pk_pm_kadaster_perceel_archief PRIMARY KEY(pk_id);


  CREATE INDEX kad_perc_arch_gemcod_idx
  ON pm_kadaster_perceel_archief
    USING btree
  (kad_gem_code);

  CREATE INDEX kad_perc_arch_gemnam_idx
  ON pm_kadaster_perceel_archief
    USING btree
  (kad_gem_naam);


  CREATE INDEX kad_perc_arch_kadsectie_idx
  ON pm_kadaster_perceel_archief
    USING btree
  (kad_sectie);

  CREATE INDEX kad_perc_arch_koopjaar_idx
  ON pm_kadaster_perceel_archief
    USING btree
  (koopjaar);

  CREATE INDEX kad_perc_arch_geldhbegindatum_idx
  ON pm_kadaster_perceel_archief
    USING btree
  (geldh_begindatum);

 -- tbv linkid
CREATE INDEX kad_perc_arch_linkid_idx
  ON pm_kadaster_perceel_archief
  USING btree
  (linkid);
  
 CREATE INDEX kad_perc_arch_kad_identif_idx
  ON pm_kadaster_perceel_archief
    USING btree
  (kad_identif);

  CREATE INDEX kad_perc_arch_geometrydata_idx
  ON pm_kadaster_perceel_archief
  USING gist
  (geometry_data);

-- verwijderen appartementen
delete from pm_kadaster_perceel_archief kp where exists (select null from pv_appartementsrecht_archief ar where ar.sc_kad_identif=kp.kad_identif);

-- View werkt niet in arcgis

--CREATE or replace VIEW vw_pm_kadaster_perceel_eigenaar_archief AS
--SELECT pk_id AS object_id, linkid, kad_gem_code, kad_gem_naam, kad_sectie, 
--        kad_perceelnummer, straat_locatie,  huisnummer_locatie,  toevoeging_locatie,  huisletter_locatie,  postcode_locatie,  woonplaats_locatie,kad_opp_akr, kad_opp_lki, aard_bebouwd, aard_cultuur_onbebouwd, 
--       koopsom, koopjaar, meer_onroerendgoed, aand_soort_grootte, omschr_deelperceel, 
--       geldh_begindatum, geldh_einddatum, soort_eigenaar, soort_recht, 
--       recht_ingangsdatum, recht_einddatum, geslacht, voornamen, voorvoegsel, 
--       geslachtsnaam, woonadres, geboorteplaats, geboortedatum, overlijdensdatum, 
--       naam_niet_nat_persoon, rechtsvorm, statutaire_zetel, kvk_nummer, 
--       aandeel_noemer, aandeel_teller, betrokken_in_splitsing, pso_identif, 
--      kad_identif, geometry_data
--FROM pm_kadaster_perceel_eigenaar_archief;


--CREATE or replace VIEW vw_pm_kadaster_perceel AS
--SELECT pk_id as object_id, linkid, kad_gem_code, kad_gem_naam, kad_sectie, 
--       kad_perceelnummer,
-- kad_opp_akr, kad_opp_lki, aard_bebouwd, aard_cultuur_onbebouwd, 
--      koopsom, koopjaar, meer_onroerendgoed, aand_soort_grootte, omschr_deelperceel, 
--       geldh_begindatum, geldh_einddatum, kad_identif, geometry_data
--FROM pm_kadaster_perceel;


-- grant select on vw_pm_kadaster_perceel_eigenaar_archief to rsgb_lezer;
--  grant select on vw_pm_kadaster_perceel to rsgb_lezer;
 grant select on pm_kadaster_perceel_archief to rsgb_lezer;
