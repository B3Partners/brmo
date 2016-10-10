--
-- upgrade RSGB datamodel van 1.3.6 naar 1.4.0 (Oracle)
--
-- Als er gebruik wordt gemaakt van Geotools (Flamingo)/Geoserver dan 
--   ook de inserts van de GT_PK_METADATA en GEOMETRY_COLUMNS uitvoeren 
--   na aanpassen (regel 239~249 hieronder).
--
-- merge van de nieuwe waarden voor Aard Recht codelijst (issue#234)
MERGE INTO aard_recht_verkort USING dual ON (aand='23')
WHEN MATCHED THEN UPDATE SET omschr='Opstalrecht Nutsvoorzieningen op gedeelte van perceel'
WHEN NOT MATCHED THEN INSERT (aand, omschr) VALUES ('23','Opstalrecht Nutsvoorzieningen op gedeelte van perceel');

MERGE INTO aard_recht_verkort USING dual ON (aand='24')
WHEN MATCHED THEN UPDATE SET omschr='Zakelijk recht (als bedoeld in artikel 5, lid 3, onder b)'
WHEN NOT MATCHED THEN INSERT (aand, omschr) VALUES ('24','Zakelijk recht (als bedoeld in artikel 5, lid 3, onder b)');

MERGE INTO aard_verkregen_recht USING dual ON (aand='23')
WHEN MATCHED THEN UPDATE SET omschr_aard_verkregenr_recht='Opstalrecht Nutsvoorzieningen op gedeelte van perceel'
WHEN NOT MATCHED THEN INSERT (aand, omschr_aard_verkregenr_recht) VALUES ('23','Opstalrecht Nutsvoorzieningen op gedeelte van perceel');
  
MERGE INTO aard_verkregen_recht USING dual ON (aand='24')
WHEN MATCHED THEN UPDATE SET omschr_aard_verkregenr_recht='Zakelijk recht als bedoeld in artikel 5, lid 3, onder b, van de Belemmeringenwet Privaatrecht op gedeelte van perceel'
WHEN NOT MATCHED THEN INSERT (aand, omschr_aard_verkregenr_recht) VALUES ('24','Zakelijk recht als bedoeld in artikel 5, lid 3, onder b, van de Belemmeringenwet Privaatrecht op gedeelte van perceel');

-- toevoegen van een ObjectID aan kadaster views ten behoeve van arcgis
CREATE OR REPLACE VIEW v_bd_app_re_bij_perceel
                                 AS
  SELECT CAST(ROWNUM AS INTEGER) AS objectid,
    ar.sc_kad_identif,
    ar.fk_2nnp_sc_identif,
    ar.ka_appartementsindex,
    ar.ka_kad_gemeentecode,
    ar.ka_perceelnummer,
    ar.ka_sectie,
    kp.begrenzing_perceel
  FROM v_bd_app_re_all_kad_perceel v
  JOIN kad_perceel kp
  ON v.perceel_identif = kp.sc_kad_identif
  JOIN app_re ar
  ON v.app_re_identif = ar.sc_kad_identif;
     
CREATE OR REPLACE VIEW v_map_kad_perceel
                                 AS
  SELECT CAST(ROWNUM AS INTEGER) AS objectid,
    p.sc_kad_identif,
    p.begrenzing_perceel,
    p.ka_sectie
    || ' '
    || p.ka_perceelnummer AS aanduiding,
    p.grootte_perceel,
    z.ks_koopjaar,
    z.ks_bedrag,
    z.cu_aard_cultuur_onbebouwd
  FROM kad_perceel p
  JOIN kad_onrrnd_zk z
  ON (z.kad_identif = p.sc_kad_identif);


CREATE OR REPLACE VIEW v_kad_perceel_in_eigendom
                                 AS
  SELECT CAST(ROWNUM AS INTEGER) AS objectid,
    p.begrenzing_perceel,
    p.sc_kad_identif,
    p.aanduiding,
    p.grootte_perceel,
    p.ks_koopjaar,
    p.ks_bedrag,
    p.cu_aard_cultuur_onbebouwd,
    nnprs.naam
    -- rownum as wtf -- Anders Oracle ORA-13276 SRID 0 not found.
  FROM v_map_kad_perceel p
  JOIN zak_recht zr
  ON (zr.fk_7koz_kad_identif = p.sc_kad_identif)
  JOIN prs_eigendom prs_e
  ON (prs_e.fk_prs_sc_identif = zr.fk_8pes_sc_identif)
  LEFT JOIN niet_nat_prs nnprs
  ON (nnprs.sc_identif  = prs_e.fk_prs_sc_identif)
  WHERE p.begrenzing_perceel.sdo_srid IS NOT NULL;


CREATE OR REPLACE VIEW v_kad_perceel_eenvoudig
                                 AS
  SELECT CAST(ROWNUM AS INTEGER) AS objectid,
    p.sc_kad_identif,
    p.begrenzing_perceel,
    p.ka_sectie || ' ' || p.ka_perceelnummer AS aanduiding,
    p.grootte_perceel,
    p_adr.kad_bag_koppeling_benobj,
    p_adr.straat,
    p_adr.huisnummer,
    p_adr.huisletter,
    p_adr.toevoeging,
    p_adr.postcode,
    p_adr.woonplaats
  FROM kad_perceel p
  JOIN v_kad_perceel_adres p_adr
  ON (p_adr.sc_kad_identif = p.sc_kad_identif);


CREATE OR REPLACE VIEW v_kad_perceel_zr_adressen
                                 AS
  SELECT CAST(ROWNUM AS INTEGER) AS objectid,
    kp.SC_KAD_IDENTIF,
    kp.BEGRENZING_PERCEEL,
    kp.AANDUIDING,
    kp.GROOTTE_PERCEEL,
    kp.STRAAT,
    kp.HUISNUMMER,
    kp.HUISLETTER,
    kp.TOEVOEGING,
    kp.POSTCODE,
    kp.WOONPLAATS,
    zr.AANDEEL_TELLER,
    zr.AANDEEL_NOEMER,
    zr.AARD_RECHT_AAND,
    zr.SOORT_EIGENAAR,
    zr.GESLACHTSNAAM,
    zr.VOORVOEGSEL,
    zr.VOORNAMEN,
    zr.GESLACHT,
    zr.WOONADRES,
    zr.GEBOORTEDATUM,
    zr.GEBOORTEPLAATS,
    zr.OVERLIJDENSDATUM,
    zr.NAAM_NIET_NATUURLIJK_PERSOON,
    zr.RECHTSVORM,
    zr.STATUTAIRE_ZETEL,
    zr.KVK_NUMMER
  FROM v_kad_perceel_eenvoudig kp
  JOIN v_kad_perceel_zak_recht zr
  ON (zr.KADASTER_IDENTIFICATIE = kp.sc_kad_identif);


CREATE OR REPLACE VIEW v_bd_app_re_and_kad_perceel
                                 AS
  SELECT CAST(ROWNUM AS INTEGER) AS objectid,
    qry.*
  FROM
    (SELECT p.sc_kad_identif AS kadaster_identificatie,
      'perceel'              AS type,
      p.ka_deelperceelnummer,
      '' AS ka_appartementsindex,
      p.ka_perceelnummer,
      p.ka_kad_gemeentecode,
      p.ka_sectie,
      p.begrenzing_perceel
    FROM kad_perceel p
    UNION ALL
    SELECT ar.sc_kad_identif AS kadaster_identificatie,
      'appartement'          AS type,
      ''                     AS ka_deelperceelnummer,
      ar.ka_appartementsindex,
      ar.ka_perceelnummer,
      ar.ka_kad_gemeentecode,
      ar.ka_sectie,
      kp.begrenzing_perceel
    FROM v_bd_app_re_all_kad_perceel v
    JOIN kad_perceel kp
    ON v.perceel_identif = kp.sc_kad_identif
    JOIN app_re ar
    ON v.app_re_identif = ar.sc_kad_identif
    ) qry ;


DROP MATERIALIZED VIEW VM_KAD_EIGENARENKAART;
CREATE MATERIALIZED VIEW VM_KAD_EIGENARENKAART ( OBJECTID, KADASTER_IDENTIFICATIE, TYPE, ZAKELIJK_RECHT_IDENTIFICATIE, AANDEEL_TELLER, AANDEEL_NOEMER, AARD_RECHT_AAND, ZAKELIJK_RECHT_OMSCHRIJVING, AANKOOPDATUM, SOORT_EIGENAAR, GESLACHTSNAAM, VOORVOEGSEL, VOORNAMEN, GESLACHT, PERCEEL_ZAK_RECHT_NAAM, PERSOON_IDENTIFICATIE, WOONADRES, GEBOORTEDATUM, GEBOORTEPLAATS, OVERLIJDENSDATUM, NAAM_NIET_NATUURLIJK_PERSOON, RECHTSVORM, STATUTAIRE_ZETEL, KVK_NUMMER, KA_APPARTEMENTSINDEX, KA_DEELPERCEELNUMMER, KA_PERCEELNUMMER, KA_KAD_GEMEENTECODE, KA_SECTIE, BEGRENZING_PERCEEL ) BUILD IMMEDIATE
                                 AS
  SELECT CAST(ROWNUM AS INTEGER) AS objectid,
    p.kadaster_identificatie     AS kadaster_identificatie,
    p.type,
    zr.kadaster_identif AS zakelijk_recht_identificatie,
    zr.ar_teller        AS aandeel_teller,
    zr.ar_noemer        AS aandeel_noemer,
    zr.fk_3avr_aand     AS aard_recht_aand,
    ark.omschr          AS zakelijk_recht_omschrijving,
    b.aankoopdatum,
    CASE
      WHEN np.sc_identif IS NOT NULL
      THEN 'Natuurlijk persoon'
      WHEN nnp.sc_identif IS NOT NULL
      THEN 'Niet natuurlijk persoon'
      ELSE 'Onbekend'
    END                             AS soort_eigenaar,
    np.nm_geslachtsnaam             AS geslachtsnaam,
    np.nm_voorvoegsel_geslachtsnaam AS voorvoegsel,
    np.nm_voornamen                 AS voornamen,
    np.geslachtsaand                AS geslacht,
    CASE
      WHEN np.sc_identif IS NOT NULL
      THEN np.NM_GESLACHTSNAAM
        || ', '
        || np.NM_VOORNAMEN
        || ' '
        || np.NM_VOORVOEGSEL_GESLACHTSNAAM
      WHEN nnp.sc_identif IS NOT NULL
      THEN nnp.NAAM
      ELSE 'Onbekend'
    END                     AS perceel_zak_recht_naam,
    inp.sc_identif          AS persoon_identificatie,
    inp.va_loc_beschrijving AS woonadres,
    inp.gb_geboortedatum    AS geboortedatum,
    inp.gb_geboorteplaats   AS geboorteplaats,
    inp.ol_overlijdensdatum AS overlijdensdatum,
    nnp.naam                AS naam_niet_natuurlijk_persoon,
    innp.rechtsvorm,
    innp.statutaire_zetel,
    innp_subject.kvk_nummer,
    p.ka_appartementsindex,
    p.ka_deelperceelnummer,
    p.ka_perceelnummer,
    p.ka_kad_gemeentecode,
    p.ka_sectie,
    p.begrenzing_perceel
  FROM v_bd_app_re_and_kad_perceel p
  JOIN zak_recht zr
  ON zr.fk_7koz_kad_identif = p.kadaster_identificatie
  LEFT JOIN aard_recht_verkort ark
  ON zr.fk_3avr_aand = ark.aand
  LEFT JOIN aard_verkregen_recht ar
  ON zr.fk_3avr_aand = ar.aand
  LEFT JOIN nat_prs np
  ON np.sc_identif = zr.fk_8pes_sc_identif
  LEFT JOIN ingeschr_nat_prs inp
  ON inp.sc_identif = np.sc_identif
  LEFT JOIN niet_nat_prs nnp
  ON nnp.sc_identif = zr.fk_8pes_sc_identif
  LEFT JOIN ingeschr_niet_nat_prs innp
  ON innp.sc_identif = nnp.sc_identif
  LEFT JOIN subject innp_subject
  ON innp_subject.identif = innp.sc_identif
  LEFT JOIN v_aankoopdatum b
  ON b.kadaster_identificatie = p.kadaster_identificatie
  WHERE zr.kadaster_identif LIKE 'NL.KAD.Tenaamstelling%';

CREATE UNIQUE INDEX VM_KAD_EIGENARENKAART_OID_IDX ON VM_KAD_EIGENARENKAART (OBJECTID ASC);
INSERT INTO USER_SDO_GEOM_METADATA VALUES('VM_KAD_EIGENARENKAART', 'BEGRENZING_PERCEEL', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
CREATE INDEX VM_KAD_EIGENARENKAART_PERC_IDX ON VM_KAD_EIGENARENKAART (BEGRENZING_PERCEEL) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');

-- optioneel: bijwerken Geotools / geoserver metadata tabellen, zie ook utility scripts:
--    402_create_geotools_geometrycolumns_metatable.sql
--    403_create_geotools_primarykey_metatable.sql
-- in directory brmo/datamodel/utility_scripts/oracle/ 
-- (let op de schemanaam 'RSGB' in onderstaande inserts moet mogelijk aangepast worden)
-- 
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_KAD_PERCEEL_ZR_ADRESSEN', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_BD_APP_RE_AND_KAD_PERCEEL', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
--    VALUES ('RSGB', 'VM_KAD_EIGENARENKAART', 'BEGRENZING_PERCEEL', 2, 28992, 'MULTIPOLYGON');
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'VM_KAD_EIGENARENKAART', 'OBJECTID', NULL, 'assigned', NULL);
