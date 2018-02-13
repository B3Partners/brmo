CREATE OR REPLACE VIEW V_P8_KADASTRAAL_ADRES
                                AS
  SELECT 
    ROWNUM                      AS OID,
    a.SC_KAD_IDENTIF            AS kadperceelcode,
    a.KAD_BAG_KOPPELING_BENOBJ,
    p.GEMEENTE_CODE,
    p.SECTIE,
    p.PERCEELNUMMER,
    p.OBJECTINDEXLETTER,
    p.OBJECTINDEXNUMMER,
    p.OPPERVLAKTE,
    p.STRAAT,
    p.ADRES,    
    p.HUIS_NUMMER               AS HUISNUMMER,
    p.HUIS_NUMMER_TOEVOEGING    AS HUISNUMMER_TOEVOEGING,
    p.HUISLETTER,
    p.POSTCODE,
    p.WOONPLAATS,
    p.GEMEENTE,
    p.CULTUUR,
    p.AARD,
    p.GEOM
  FROM V_KAD_PERCEEL_ADRES a
  JOIN V_P8_KADASTRAAL_PERCEEL p
  ON p.kadperceelcode = a.SC_KAD_IDENTIF;
  
--metadata
DELETE
  FROM USER_SDO_GEOM_METADATA
  WHERE table_name ='V_P8_KADASTRAAL_ADRES';
  
INSERT
  INTO USER_SDO_GEOM_METADATA VALUES (
      'V_P8_KADASTRAAL_ADRES',
      'GEOM',
      MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 281000, .1),MDSYS.SDO_DIM_ELEMENT('Y' , 304000, 620000, .1)),
      28992
    );
--
-- materialized versie, verversing om 07:30
--
DROP MATERIALIZED VIEW VM_P8_KADASTRAAL_ADRES;
CREATE MATERIALIZED VIEW VM_P8_KADASTRAAL_ADRES REFRESH ON DEMAND START WITH 
  TRUNC (SYSDATE)  +  ( 7.5/24 ) NEXT TRUNC ( SYSDATE ) +1+ ( 7.5/24 )
                AS
  SELECT 
    OID,
    KADPERCEELCODE,
    GEMEENTE_CODE,
    SECTIE,
    PERCEELNUMMER,
    OBJECTINDEXLETTER,
    OBJECTINDEXNUMMER,
    OPPERVLAKTE,
    ADRES,
    STRAAT,
    HUISNUMMER,
    HUISNUMMER_TOEVOEGING,
    HUISLETTER,
    POSTCODE,
    WOONPLAATS,
    GEMEENTE,
    CULTUUR,
    AARD,
    GEOM
  FROM V_P8_KADASTRAAL_ADRES;
  
  --metadata
  DELETE
    FROM USER_SDO_GEOM_METADATA
    WHERE table_name ='VM_P8_KADASTRAAL_ADRES';
  INSERT
    INTO USER_SDO_GEOM_METADATA VALUES (
      'VM_P8_KADASTRAAL_ADRES',
      'GEOM',
      MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 281000, .1),MDSYS.SDO_DIM_ELEMENT('Y' , 304000, 620000, .1)),
      28992
    );
    
-- indexen
CREATE UNIQUE INDEX vm_p8_kad_per_adr_oid ON vm_p8_kadastraal_adres (OID ASC ) ;
CREATE INDEX vm_p8_kad_perc_adr_code_idx ON vm_p8_kadastraal_adres ( kadperceelcode ASC );
CREATE INDEX vm_p8_kad_perc_adr_gem_idx ON vm_p8_kadastraal_adres ( gemeente_code ASC );
CREATE INDEX vm_p8_kad_perc_adr_sectie_idx ON vm_p8_kadastraal_adres ( sectie ASC );
CREATE INDEX vm_p8_kad_perc_adr_pnummer_idx ON vm_p8_kadastraal_adres ( perceelnummer ASC );
CREATE INDEX vm_p8_kad_perc_adr_straat_idx ON vm_p8_kadastraal_adres ( straat ASC );
CREATE INDEX vm_p8_kad_perc_adr_adres_idx ON vm_p8_kadastraal_adres ( adres ASC );
CREATE INDEX vm_p8_kad_perc_adr_pc_idx ON vm_p8_kadastraal_adres ( postcode ASC );
CREATE INDEX vm_p8_kad_perc_adr_wpl_idx ON vm_p8_kadastraal_adres ( woonplaats ASC );
CREATE INDEX vm_p8_kad_perc_adr_geom_idx ON vm_p8_kadastraal_adres ( geom )
 INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON' );