drop VIEW vw_p8_kadastraal_perceel cascade;
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
    trim(CAST (coalesce(a.naam_openb_rmte,'')||' '|| coalesce(to_char(a.huinummer),'')||' '||coalesce(a.huisletter,'')||coalesce(a.huinummertoevoeging,'') AS VARCHAR(400)))        AS adres,
    a.naam_openb_rmte                  AS straat,
    a.huinummer                        AS huis_nummer,
    a.huinummertoevoeging	       AS huis_nummer_toevoeging,
    a.huisletter		       as huisletter,
    a.postcode                         AS postcode,
    a.wpl_naam                         AS woonplaats,
    a.gem_naam                         AS gemeente,
    p.cu_aard_cultuur_onbebouwd        AS cultuur,
    CAST(to_date (p.dat_beg_geldh, 'yyyy-mm-dd') AS date) AS datum_ingang,
    p.cu_aard_bebouwing                AS aard,
    p.begrenzing_perceel               AS geom
  FROM --pv_kad_onroerende_zaak z
    --inner JOIN
    pv_map_i_kpe p
    --ON p.sc_kad_identif = z.kad_identif
  LEFT JOIN pv_info_i_koz_adres a
  ON p.sc_kad_identif = a.koz_identif

  -- Complex
	left join  cte_complex cc
	on cc.perceel_identif = a.koz_identif
;

COMMENT ON table vw_p8_kadastraal_perceel
IS
  'Versie 0.1 september 2015 SK Provincie Gelderland';


DROP TABLE pm_p8_kadastraal_perceel;
CREATE TABLE pm_p8_kadastraal_perceel AS
SELECT * FROM vw_p8_kadastraal_perceel where 0=1;


-- Technische PK voor tooling
  ALTER TABLE pm_p8_kadastraal_perceel ADD pm_p8_kadastraal_perceel_id integer generated always as identity;
  insert into pm_p8_kadastraal_perceel (   KADPERCEELCODE,
   GEMEENTE_CODE,
   SECTIE,
   PERCEELNUMMER,
   OBJECTINDEXLETTER,
   OBJECTINDEXNUMMER,
   OPPERVLAKTE,
   ADRES,
   STRAAT,
   HUIS_NUMMER,
   HUIS_NUMMER_TOEVOEGING,
   HUISLETTER,
   POSTCODE,
   WOONPLAATS,
   GEMEENTE,
   CULTUUR,
   DATUM_INGANG,
   AARD,
   GEOM
) SELECT * FROM vw_p8_kadastraal_perceel;
commit;
CREATE UNIQUE INDEX pk_pm_p8_kadastraal_perceel ON pm_p8_kadastraal_perceel  (pm_p8_kadastraal_perceel_id ASC);
  ALTER TABLE pm_p8_kadastraal_perceel ADD CONSTRAINT pk_pm_p8_kadastraal_perceel PRIMARY KEY (pm_p8_kadastraal_perceel_id);

--Extra indexen
  CREATE INDEX ix_kadastraal_perceel_kpcode ON pm_p8_kadastraal_perceel 
    (
      kadperceelcode ASC
    );
  CREATE INDEX ix_kadastraal_perceel_gemeente ON pm_p8_kadastraal_perceel 
    (
      gemeente ASC
    );
  CREATE INDEX ix_kadastraal_perceel_sectie ON pm_p8_kadastraal_perceel 
    (
      sectie ASC
    );
  CREATE INDEX ix_kadastraal_perceel_pnummer ON pm_p8_kadastraal_perceel 
    (
      perceelnummer ASC
    );
  CREATE INDEX ix_kadastraal_perceel_straat ON pm_p8_kadastraal_perceel 
    (
      straat ASC
    );
  CREATE INDEX ix_kad_perceel_pc_huisnr ON pm_p8_kadastraal_perceel 
    (
      postcode ASC,
      huis_nummer ASC
    );
    
insert into user_sdo_geom_metadata values ( 'PM_P8_KADASTRAAL_PERCEEL', 'GEOM',
  sdo_dim_array(sdo_dim_element('X', 10000, 281000, 0.001), sdo_dim_element('Y', 304000, 623000, 0.001)), 28992);
  
  drop INDEX ix_kadastraal_perceel_geom;  
  CREATE INDEX ix_kadastraal_perceel_geom ON pm_p8_kadastraal_perceel 
    (
      geom
    ) INDEXTYPE IS MDSYS.SPATIAL_INDEX
PARAMETERS('index_status=cleanup, layer_gtype=MULTIPOLYGON ');

-- Rechten
  GRANT
  SELECT ON  pm_p8_kadastraal_perceel TO brmo_rsgbuser;
