/****************************************************************
 ** Auteur	: S. Knoeff
 ** Versie 	: 2.1
 ** Datum	: 27-06-2016
 **
 ** Wijzigingen :
 ** Datum	Auteur		Soort
 ** xxxxxxxx	S.Knoeff	Initieel
 ** 15-01-2016	S.Knoeff	TMP tabel
 ** 19-05-2016	S.Knoeff	Uitbreiden met 'archief' ivm perceelsplitsingen
 ** 27-06-2016	S.Knoeff	Uitsluiten archief records die nog een actuele tegenhanger heeft
 **				Alleen laatst gewijzigde archiefrecord
 *****************************************************************/
--drop VIEW vw_p8_kadastraal_perceel cascade;
/*
*******************************
Versie 2 Compleet archief erbij
*******************************
CREATE OR REPLACE VIEW vw_p8_kadastraal_perceel
as
with cte_complex
  as
  (
  select perceel_identif from v_bd_app_re_all_kad_perceel
  )                                       
  SELECT p.sc_kad_identif              AS kadperceelcode,
    p.ka_kad_gemeentecode              AS gemeente_code,
    p.ka_sectie                        AS sectie,
    p.ka_perceelnummer                 AS perceelnummer,
    CAST (case when cc.perceel_identif is null and p.omschr_deelperceel is null
	then 'G' 
	when cc.perceel_identif is not null and p.omschr_deelperceel is null
	then 'C'
	when p.omschr_deelperceel is not null -- Deel bepalen aan hand van aanwezigheid omschrijving
	then 'D'
	else 'O' 				
	end   AS CHAR(1))              AS objectindexletter,
    -1                                 AS objectindexnummer,
    p.grootte_perceel                  AS oppervlakte,
    trim(CAST (coalesce(a.naam_openb_rmte,'')||' '|| coalesce(a.huinummer::text,'')||' '||coalesce(a.huisletter,'')||coalesce(a.huinummertoevoeging,'') AS VARCHAR(400)))        AS adres,
    a.naam_openb_rmte                  AS straat,
    a.huinummer                        AS huis_nummer,
    a.huinummertoevoeging	       AS huis_nummer_toevoeging,
    a.huisletter		       as huisletter,
    a.postcode                         AS postcode,
    a.wpl_naam                         AS woonplaats,
    a.gem_naam                         AS gemeente,
    p.cu_aard_cultuur_onbebouwd        AS cultuur,
    CAST(p.dat_beg_geldh AS TIMESTAMP) AS datum_ingang,
    p.cu_aard_bebouwing                AS aard,
    p.begrenzing_perceel               AS geom
  FROM pv_map_i_kpe p
  LEFT JOIN pv_info_i_koz_adres a
  ON p.sc_kad_identif = a.koz_identif
  -- Complex
  left join  cte_complex cc
  on cc.perceel_identif = p.kad_identif::text

   union
  select kadperceelcode,
	gemeente_code,
	sectie,
	perceelnummer,
	objectindexletter,
	objectindexnummer,
	oppervlakte,
	adres,
	straat,
	huis_nummer,
	huis_nummer_toevoeging,
	huisletter,
	postcode,
	woonplaats,
	gemeente,
	cultuur,
	datum_ingang,
	aard,
	geom 
	from 
   (
  SELECT p.sc_kad_identif              AS kadperceelcode,
    p.ka_kad_gemeentecode              AS gemeente_code,
    p.ka_sectie                        AS sectie,
    p.ka_perceelnummer                 AS perceelnummer,
    CAST (case when cc.perceel_identif is null and p.omschr_deelperceel is null
	then 'G' 
	when cc.perceel_identif is not null and p.omschr_deelperceel is null
	then 'C'
	when p.omschr_deelperceel is not null -- Deel bepalen aan hand van aanwezigheid omschrijving
	then 'D'
	else 'O' 				
	end   AS CHAR(1))              AS objectindexletter,
    -1                                 AS objectindexnummer,
    p.grootte_perceel                  AS oppervlakte,
    trim(CAST (coalesce(a.naam_openb_rmte,'')||' '|| coalesce(a.huinummer::text,'')||' '||coalesce(a.huisletter,'')||coalesce(a.huinummertoevoeging,'') AS VARCHAR(400)))        AS adres,
    a.naam_openb_rmte                  AS straat,
    a.huinummer                        AS huis_nummer,
    a.huinummertoevoeging	       AS huis_nummer_toevoeging,
    a.huisletter		       as huisletter,
    a.postcode                         AS postcode,
    a.wpl_naam                         AS woonplaats,
    a.gem_naam                         AS gemeente,
    p.cu_aard_cultuur_onbebouwd        AS cultuur,
    CAST(p.dat_beg_geldh AS TIMESTAMP) AS datum_ingang,
    p.cu_aard_bebouwing                AS aard,
    p.begrenzing_perceel               AS geom,
    -- Alleen de laatst geldige
    RANK () over (partition by p.sc_kad_identif ORDER BY p.dat_beg_geldh desc ) as ranking_nummer
  FROM  pv_map_i_kpe_archief p
     LEFT JOIN pv_info_i_koz_adres a
     ON p.sc_kad_identif = a.koz_identif
  -- Complex
     left join  cte_complex cc
     on cc.perceel_identif = p.kad_identif::text
  -- Mag alleen in archief bestaan
     where not exists ( select null from pv_map_i_kpe i where i.sc_kad_identif=p.sc_kad_identif )
  ) a where ranking_nummer=1
;
*/


COMMENT ON VIEW vw_p8_kadastraal_perceel
IS
  'Versie 2.1 juni 2016 SK Provincie Gelderland';

--DROP TABLE pm_p8_kadastraal_perceel;
*/

CREATE TABLE pm_p8_kadastraal_perceel_tmp AS
SELECT * FROM vw_p8_kadastraal_perceel; --where 1=2

-- Technische PK voor tooling
  ALTER TABLE pm_p8_kadastraal_perceel_tmp ADD column pm_p8_kadastraal_perceel_id serial;
  CREATE UNIQUE INDEX pk_pm_p8_kadastraal_perceel_tmp ON pm_p8_kadastraal_perceel_tmp USING btree (pm_p8_kadastraal_perceel_id ASC);

--Extra indexen
  CREATE INDEX ix_kadastraal_perceel_kadperceelcode_tmp ON pm_p8_kadastraal_perceel_tmp USING btree
    (
      kadperceelcode ASC
    );
  CREATE INDEX ix_kadastraal_perceel_gemeente_tmp ON pm_p8_kadastraal_perceel_tmp USING btree
    (
      gemeente ASC
    );
  CREATE INDEX ix_kadastraal_perceel_sectie_tmp ON pm_p8_kadastraal_perceel_tmp USING btree
    (
      sectie ASC
    );
  CREATE INDEX ix_kadastraal_perceel_perceelnummer_tmp ON pm_p8_kadastraal_perceel_tmp USING btree
    (
      perceelnummer ASC
    );
  CREATE INDEX ix_kadastraal_perceel_straat_tmp ON pm_p8_kadastraal_perceel_tmp USING btree
    (
      straat ASC
    );
  CREATE INDEX ix_kadastraal_perceel_postcode_huis_nummer_tmp ON pm_p8_kadastraal_perceel_tmp USING btree
    (
      postcode ASC,
      huis_nummer ASC
    );
  CREATE INDEX ix_kadastraal_perceel_geom_tmp ON pm_p8_kadastraal_perceel_tmp USING gist
    (
      geom
    );

-- Omzetten van TMP naar 'normaal'
drop table pm_p8_kadastraal_perceel cascade;
ALTER TABLE pm_p8_kadastraal_perceel_tmp RENAME TO pm_p8_kadastraal_perceel;

-- Hernoemen indexen
ALTER INDEX pk_pm_p8_kadastraal_perceel_tmp RENAME TO pk_pm_p8_kadastraal_perceel;
ALTER INDEX ix_kadastraal_perceel_kadperceelcode_tmp RENAME TO ix_kadastraal_perceel_kadperceelcode;
ALTER INDEX ix_kadastraal_perceel_gemeente_tmp RENAME TO ix_kadastraal_perceel_gemeente;
ALTER INDEX ix_kadastraal_perceel_sectie_tmp RENAME TO ix_kadastraal_perceel_sectie;
ALTER INDEX ix_kadastraal_perceel_perceelnummer_tmp RENAME TO ix_kadastraal_perceel_perceelnummer;
ALTER INDEX ix_kadastraal_perceel_straat_tmp RENAME TO ix_kadastraal_perceel_straat;
ALTER INDEX ix_kadastraal_perceel_postcode_huis_nummer_tmp RENAME TO ix_kadastraal_perceel_postcode_huis_nummer;
ALTER INDEX ix_kadastraal_perceel_geom_tmp RENAME TO ix_kadastraal_perceel_geom;

ALTER TABLE pm_p8_kadastraal_perceel ADD CONSTRAINT pk_pm_p8_kadastraal_perceel PRIMARY KEY USING INDEX pk_pm_p8_kadastraal_perceel;

-- Rechten
  GRANT
  SELECT ON TABLE pm_p8_kadastraal_perceel TO rsgb_lezer;
