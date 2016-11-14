CREATE OR REPLACE VIEW v_p8_subject_percelen
                           AS
  SELECT 
    ROWNUM                 AS oid, -- genereer een unieke id
    z.pso_identif          AS subjectid,
    p.sc_kad_identif       AS kadperceelcode,
    p.ka_kad_gemeentecode  AS gemeente_code,
    p.ka_sectie            AS sectie,
    p.ka_perceelnummer     AS perceelnummer,
    CAST (NULL AS CHAR(1)) AS objectindexletter,
    p.grootte_perceel      AS oppervlakte,
    p.begrenzing_perceel   AS geom,
    COALESCE(TO_CHAR(aandeel_teller),'')
    || '/'
    || COALESCE(TO_CHAR(aandeel_noemer),'') AS aandeel,
    z.arv_omschr                            AS rechtsoort,
    F_DATUM(p.dat_beg_geldh)                AS datum_ingang,
    F_DATUM(p.datum_einde_geldh)            AS datum_eind
  FROM pv_info_i_koz_zak_recht z
  LEFT JOIN pv_map_i_kpe p
  ON p.sc_kad_identif = z.koz_identif;

--metadata
DELETE
  FROM USER_SDO_GEOM_METADATA
  WHERE table_name ='V_P8_SUBJECT_PERCELEN';
INSERT
  INTO USER_SDO_GEOM_METADATA VALUES
    (
      'V_P8_SUBJECT_PERCELEN',
      'GEOM',
      MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 281000, .1),MDSYS.SDO_DIM_ELEMENT('Y' , 304000, 620000, .1)),
      28992
    );

--
-- materialized versie, verversing om 07:30
--
DROP MATERIALIZED VIEW vm_p8_subject_percelen;
CREATE MATERIALIZED VIEW vm_p8_subject_percelen REFRESH ON DEMAND START WITH TRUNC ( SYSDATE ) + ( 7.5/24 ) NEXT TRUNC ( SYSDATE ) +1+ ( 7.5/24 )
AS
  SELECT * FROM v_p8_subject_percelen;

--metadata
DELETE
  FROM USER_SDO_GEOM_METADATA
  WHERE table_name ='VM_P8_SUBJECT_PERCELEN';
INSERT
  INTO USER_SDO_GEOM_METADATA VALUES
    (
      'VM_P8_SUBJECT_PERCELEN',
      'GEOM',
      MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 281000, .1),MDSYS.SDO_DIM_ELEMENT('Y' , 304000, 620000, .1)),
      28992
    );

--indexen
CREATE UNIQUE INDEX vm_p8_subject_perc_oid_idx ON vm_p8_subject_percelen( oid ASC );
CREATE INDEX vm_p8_subject_perc_sid_idx ON vm_p8_subject_percelen( subjectid ASC );
CREATE INDEX vm_p8_subject_perc_kpcode_idx ON vm_p8_subject_percelen( kadperceelcode ASC );
CREATE INDEX vm_p8_subject_perc_sectie_idx ON vm_p8_subject_percelen( sectie ASC );
CREATE INDEX vm_p8_subject_perc_pnr_idx ON vm_p8_subject_percelen( perceelnummer ASC );
CREATE INDEX vm_p8_subject_perc_geom_idx ON vm_p8_subject_percelen( geom )
  INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON' );