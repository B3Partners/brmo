-- actuele percelen
CREATE OR REPLACE VIEW v_p8_kadastraal_perceel_act
                 AS
WITH cte_complex AS
  ( SELECT perceel_identif FROM v_bd_app_re_all_kad_perceel )
SELECT 
  p.sc_kad_identif      AS kadperceelcode,
  p.ka_kad_gemeentecode AS gemeente_code,
  p.ka_sectie           AS sectie,
  p.ka_perceelnummer    AS perceelnummer,
  CAST (
  CASE
    WHEN cc.perceel_identif  IS NULL
    AND p.omschr_deelperceel IS NULL
    THEN 'G'
    WHEN cc.perceel_identif  IS NOT NULL
    AND p.omschr_deelperceel IS NULL
    THEN 'C'
    WHEN p.omschr_deelperceel IS NOT NULL -- Deel bepalen aan hand van aanwezigheid omschrijving
    THEN 'D'
    ELSE 'O'
  END AS CHAR(1))   AS objectindexletter,
  -1                AS objectindexnummer,
  p.grootte_perceel AS oppervlakte,
  trim(CAST (COALESCE(a.naam_openb_rmte,'')
  ||' '
  || COALESCE(TO_CHAR(a.huinummer),'')
  ||' '
  ||COALESCE(a.huisletter,'')
  ||COALESCE(a.huinummertoevoeging,'') AS VARCHAR(400)))   AS adres,
  a.naam_openb_rmte                                        AS straat,
  a.huinummer                                              AS huis_nummer,
  a.huinummertoevoeging                                    AS huis_nummer_toevoeging,
  a.huisletter                                             AS huisletter,
  a.postcode                                               AS postcode,
  a.wpl_naam                                               AS woonplaats,
  a.gem_naam                                               AS gemeente,
  p.cu_aard_cultuur_onbebouwd                              AS cultuur,
  F_DATUM (p.dat_beg_geldh)                                AS datum_ingang,
  F_DATUM (p.datum_einde_geldh)                            AS datum_einde,
  p.cu_aard_bebouwing                                      AS aard,
  p.begrenzing_perceel                                     AS geom
FROM --pv_kad_onroerende_zaak z
  --inner JOIN
  pv_map_i_kpe p
  --ON p.sc_kad_identif = z.kad_identif
LEFT JOIN pv_info_i_koz_adres a
ON p.sc_kad_identif = a.koz_identif
  -- Complex
LEFT JOIN cte_complex cc
ON cc.perceel_identif = a.koz_identif;

COMMENT ON TABLE v_p8_kadastraal_perceel_act IS 'P8 actuele percelen view';

--metadata
DELETE FROM USER_SDO_GEOM_METADATA WHERE table_name ='V_P8_KADASTRAAL_PERCEEL_ACT';
INSERT INTO USER_SDO_GEOM_METADATA VALUES (
    'V_P8_KADASTRAAL_PERCEEL_ACT',
    'GEOM',
    MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 281000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
  
-- meest recente archief/historische percelen hulp view met percelen waarvan 
-- geen actuele versie meer is/die vervallen zijn
CREATE OR REPLACE VIEW v_p8_kad_perceel_archief_hulp AS
SELECT
    pp.sc_kad_identif,
    pp.aand_soort_grootte,
    pp.grootte_perceel,
    pp.omschr_deelperceel,
    pp.ka_deelperceelnummer,
    pp.ka_kad_gemeentecode,
    pp.ka_perceelnummer,
    pp.ka_sectie,
    aa.cu_aard_cultuur_onbebouwd,
    aa.cu_aard_bebouwing,
    F_DATUM (aa.dat_beg_geldh) AS dat_beg_geldh,
    F_DATUM (aa.datum_einde_geldh) AS datum_einde_geldh,
    pp.begrenzing_perceel
FROM
    kad_perceel_archief pp, kad_onrrnd_zk_archief aa
WHERE
    -- geen actueel perceel meer
    NOT EXISTS ( SELECT 0 FROM kad_perceel c WHERE pp.sc_kad_identif = c.sc_kad_identif)
AND
    -- jongste "archief" perceel voor een sc_kad_identif
    (pp.sc_kad_identif, F_DATUM (pp.sc_dat_beg_geldh)) IN ( SELECT pa.sc_kad_identif, MAX ( F_DATUM ( pa.sc_dat_beg_geldh)) FROM kad_perceel_archief pa WHERE pa.sc_kad_identif=pp.sc_kad_identif GROUP BY pa.sc_kad_identif )
AND
    -- "archief" onroerende zaak behorhend bij bovenstaande jongste "archief" perceel
    (aa.kad_identif, F_DATUM (aa.dat_beg_geldh)) IN ( SELECT pa.sc_kad_identif, MAX ( F_DATUM ( pa.sc_dat_beg_geldh)) FROM kad_perceel_archief pa WHERE pa.sc_kad_identif=pp.sc_kad_identif GROUP BY pa.sc_kad_identif )
AND 
    aa.kad_identif = pp.sc_kad_identif; 
    
--metadata
DELETE FROM USER_SDO_GEOM_METADATA WHERE table_name ='V_P8_KAD_PERCEEL_ARCHIEF_HULP';
INSERT INTO USER_SDO_GEOM_METADATA VALUES
  (
    'V_P8_KAD_PERCEEL_ARCHIEF_HULP',
    'BEGRENZING_PERCEEL',
    MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 281000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );

COMMENT ON TABLE V_P8_KAD_PERCEEL_ARCHIEF_HULP IS 'P8 archief/historische percelen hulp view';

  
  
-- historische percelen waarvan de sc_kad_identif niet meer voorkomt in kad_perceel
CREATE OR REPLACE VIEW v_p8_kadastraal_perceel_hist
                         AS
SELECT 
  p.sc_kad_identif       AS kadperceelcode,
  p.ka_kad_gemeentecode  AS gemeente_code,
  p.ka_sectie            AS sectie,
  p.ka_perceelnummer     AS perceelnummer,
  CAST (NULL AS CHAR(1)) AS objectindexletter,
  -1                     AS objectindexnummer,
  p.grootte_perceel      AS oppervlakte,
  
  trim(CAST (COALESCE(a.naam_openb_rmte,'')
  ||' '
  ||COALESCE(TO_CHAR(a.huinummer),'')
  ||' '
  ||COALESCE(a.huisletter,'')
  ||COALESCE(a.huinummertoevoeging,'') AS VARCHAR(400)))   AS adres,
  a.naam_openb_rmte                                        AS straat,
  a.huinummer                                              AS huis_nummer,
  a.huinummertoevoeging                                    AS huis_nummer_toevoeging,
  a.huisletter                                             AS huisletter,
  a.postcode                                               AS postcode,
  a.wpl_naam                                               AS woonplaats,
  a.gem_naam                                               AS gemeente,
  p.cu_aard_cultuur_onbebouwd                              AS cultuur,
  p.dat_beg_geldh                                          AS datum_ingang,
  p.datum_einde_geldh                                      AS datum_einde,
  p.cu_aard_bebouwing                                      AS aard,
  p.begrenzing_perceel                                     AS geom
FROM
  V_P8_KAD_PERCEEL_ARCHIEF_HULP p
LEFT JOIN pv_info_i_koz_adres a
  ON p.sc_kad_identif = a.koz_identif;

COMMENT ON TABLE v_p8_kadastraal_perceel_hist IS 'P8 vervallen percelen view';

-- metadata
DELETE FROM USER_SDO_GEOM_METADATA WHERE table_name ='V_P8_KADASTRAAL_PERCEEL_HIST';
INSERT INTO USER_SDO_GEOM_METADATA VALUES 
  (
    'V_P8_KADASTRAAL_PERCEEL_HIST',
    'GEOM',
    MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 281000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );
  
-- actuele + histrische percelen
CREATE OR REPLACE VIEW v_p8_kadastraal_perceel 
AS
  SELECT * FROM v_p8_kadastraal_perceel_act
  UNION ALL
  SELECT * FROM v_p8_kadastraal_perceel_hist;
  
COMMENT ON TABLE v_p8_kadastraal_perceel IS 'P8 alle percelen view';

--metadata
DELETE FROM USER_SDO_GEOM_METADATA WHERE table_name ='V_P8_KADASTRAAL_PERCEEL';
INSERT INTO USER_SDO_GEOM_METADATA VALUES
  (
    'V_P8_KADASTRAAL_PERCEEL',
    'GEOM',
    MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 281000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
    28992
  );

--
-- materialized versie, verversing om 07:30
--
DROP MATERIALIZED VIEW vm_p8_kadastraal_perceel;
CREATE MATERIALIZED VIEW vm_p8_kadastraal_perceel REFRESH ON DEMAND START WITH TRUNC(SYSDATE) +(7.5/24) NEXT TRUNC(SYSDATE) +1+ (7.5/24)
AS
  SELECT * FROM v_p8_kadastraal_perceel;

--metadata
DELETE FROM USER_SDO_GEOM_METADATA WHERE table_name ='VM_P8_KADASTRAAL_PERCEEL';
  
INSERT INTO USER_SDO_GEOM_METADATA VALUES
    (
      'VM_P8_KADASTRAAL_PERCEEL',
      'GEOM',
      MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 281000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
      28992
    );

-- indexen
CREATE UNIQUE INDEX vm_p8_kad_perc_kpcode_idx ON vm_p8_kadastraal_perceel ( kadperceelcode ASC );
CREATE INDEX vm_p8_kad_perc_gemeente_idx ON vm_p8_kadastraal_perceel ( gemeente ASC );
CREATE INDEX vm_p8_kad_perc_sectie_idx ON vm_p8_kadastraal_perceel( sectie ASC );
CREATE INDEX vm_p8_kad_perc_pnummer_idx ON vm_p8_kadastraal_perceel( perceelnummer ASC );
CREATE INDEX vm_p8_kad_perc_straat_idx ON vm_p8_kadastraal_perceel( straat ASC );
CREATE INDEX vm_p8_kad_perc_pc_huisnr_idx ON vm_p8_kadastraal_perceel( postcode ASC, huis_nummer ASC );
CREATE INDEX vm_p8_kad_perc_geom_idx ON vm_p8_kadastraal_perceel( geom )
    INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS('LAYER_GTYPE=MULTIPOLYGON');
