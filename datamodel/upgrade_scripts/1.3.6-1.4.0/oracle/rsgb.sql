--
-- upgrade RSGB datamodel van 1.3.6 naar 1.4.0 (Oracle)
--
-- Als er gebruik wordt gemaakt van Geotools (Flamingo)/Geoserver dan
--   ook de inserts van de GT_PK_METADATA en GEOMETRY_COLUMNS uitvoeren
--   na aanpassen (onderaan in dit bestand).
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

-- view om vlakken kaart te maken met percelen die 1 of meerdere appartementen hebben
CREATE OR REPLACE VIEW v_bd_kad_perceel_met_app_vlak AS
 SELECT
    CAST(ROWNUM AS INTEGER) AS objectid,
    v.perceel_identif,
    kp.sc_kad_identif,
    kp.aand_soort_grootte,
    kp.grootte_perceel,
    kp.omschr_deelperceel,
    kp.fk_7kdp_sc_kad_identif,
    kp.ka_deelperceelnummer,
    kp.ka_kad_gemeentecode,
    kp.ka_perceelnummer,
    kp.ka_sectie,
    kp.begrenzing_perceel
   FROM v_bd_kad_perceel_with_app_re v
     JOIN kad_perceel kp ON v.perceel_identif = kp.sc_kad_identif;

DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME='V_BD_KAD_PERCEEL_MET_APP_VLAK' AND  COLUMN_NAME='BEGRENZING_PERCEEL';
INSERT INTO USER_SDO_GEOM_METADATA VALUES('V_BD_KAD_PERCEEL_MET_APP_VLAK', 'BEGRENZING_PERCEEL', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

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
CREATE MATERIALIZED VIEW VM_KAD_EIGENARENKAART ( OBJECTID, KADASTER_IDENTIFICATIE, TYPE, ZAKELIJK_RECHT_IDENTIFICATIE, AANDEEL_TELLER, AANDEEL_NOEMER, AARD_RECHT_AAND, ZAKELIJK_RECHT_OMSCHRIJVING, AANKOOPDATUM, SOORT_EIGENAAR, GESLACHTSNAAM, VOORVOEGSEL, VOORNAMEN, GESLACHT, PERCEEL_ZAK_RECHT_NAAM, PERSOON_IDENTIFICATIE, WOONADRES, GEBOORTEDATUM, GEBOORTEPLAATS, OVERLIJDENSDATUM, NAAM_NIET_NATUURLIJK_PERSOON, RECHTSVORM, STATUTAIRE_ZETEL, KVK_NUMMER, KA_APPARTEMENTSINDEX, KA_DEELPERCEELNUMMER, KA_PERCEELNUMMER, KA_KAD_GEMEENTECODE, KA_SECTIE, BEGRENZING_PERCEEL )
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
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME='VM_KAD_EIGENARENKAART' AND  COLUMN_NAME='BEGRENZING_PERCEEL';
INSERT INTO USER_SDO_GEOM_METADATA VALUES('VM_KAD_EIGENARENKAART', 'BEGRENZING_PERCEEL', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
CREATE INDEX VM_KAD_EIGENARENKAART_PERC_IDX ON VM_KAD_EIGENARENKAART (BEGRENZING_PERCEEL) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');


-------------------------------------------------
-- V_VERBLIJFSOBJECT_ALLES
-------------------------------------------------
CREATE OR REPLACE VIEW
    V_VERBLIJFSOBJECT_ALLES
    (
        OBJECTID,
        FID,
        PAND_ID,
        GEMEENTE,
        WOONPLAATS,
        STRAATNAAM,
        HUISNUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        STATUS,
        OPPERVLAKTE,
        THE_GEOM
    ) AS
SELECT
    CAST(ROWNUM AS INTEGER)     AS OBJECTID,
    VBO.SC_IDENTIF              AS FID,
    FKPAND.FK_NN_RH_PND_IDENTIF AS PAND_ID,
    GEM.NAAM                    AS GEMEENTE,
    CASE
         WHEN ADDROBJ.FK_6WPL_IDENTIF IS NOT NULL
         -- opzoeken want in andere woonplaats
         THEN  (SELECT NAAM FROM WNPLTS WHERE IDENTIF = FK_6WPL_IDENTIF)
         ELSE WP.NAAM
    END                         AS WOONPLAATS,
    GEOR.NAAM_OPENB_RMTE        AS STRAATNAAM,
    ADDROBJ.HUINUMMER           AS HUISNUMMER,
    ADDROBJ.HUISLETTER,
    ADDROBJ.HUINUMMERTOEVOEGING AS HUISNUMMER_TOEV,
    ADDROBJ.POSTCODE,
    VBO.STATUS,
    GOBJ.OPPERVLAKTE_OBJ AS OPPERVLAKTE,
    GOBJ.PUNTGEOM        AS THE_GEOM
FROM
    ((((((((VERBLIJFSOBJ VBO
JOIN
    VERBLIJFSOBJ_PAND FKPAND
ON
    ((FKPAND.FK_NN_LH_VBO_SC_IDENTIF = VBO.SC_IDENTIF)))
JOIN
    GEBOUWD_OBJ GOBJ
ON
    ((GOBJ.SC_IDENTIF = VBO.SC_IDENTIF)))
JOIN
    NUMMERAAND NA
ON
    ((NA.SC_IDENTIF = VBO.FK_11NRA_SC_IDENTIF)))
JOIN
    ADDRESSEERB_OBJ_AAND ADDROBJ
ON
    ((ADDROBJ.IDENTIF = NA.SC_IDENTIF)))
JOIN
    GEM_OPENB_RMTE GEOR
ON
    ((GEOR.IDENTIFCODE = ADDROBJ.FK_7OPR_IDENTIFCODE)))
LEFT JOIN
    OPENB_RMTE_WNPLTS ORWP
ON
    ((GEOR.IDENTIFCODE = ORWP.FK_NN_LH_OPR_IDENTIFCODE)))
LEFT JOIN
    WNPLTS WP
ON
    ((ORWP.FK_NN_RH_WPL_IDENTIF = WP.IDENTIF)))
LEFT JOIN
    GEMEENTE GEM
ON
    ((
            WP.FK_7GEM_CODE = GEM.CODE)))
WHERE
    ((((
                    ADDROBJ.DAT_EIND_GELDH IS NULL)
            AND (
                    GEOR.DATUM_EINDE_GELDH IS NULL))
        AND (
                GEM.DATUM_EINDE_GELDH IS NULL))
    AND (
            GOBJ.DATUM_EINDE_GELDH IS NULL));
-------------------------------------------------
-- V_VERBLIJFSOBJECT_GEVORMD
-------------------------------------------------
CREATE OR REPLACE VIEW
    V_VERBLIJFSOBJECT_GEVORMD
    (
        OBJECTID,
        FID,
        PAND_ID,
        GEMEENTE,
        WOONPLAATS,
        STRAATNAAM,
        HUISNUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        --GEBRUIKSDOEL,
        STATUS,
        OPPERVLAKTE,
        THE_GEOM
    ) AS
SELECT
    OBJECTID,
    FID,
    PAND_ID,
    GEMEENTE,
    WOONPLAATS,
    STRAATNAAM,
    HUISNUMMER,
    HUISLETTER,
    HUISNUMMER_TOEV,
    POSTCODE,
    --GEBRUIKSDOEL,
    STATUS,
    OPPERVLAKTE,
    THE_GEOM
FROM
    V_VERBLIJFSOBJECT_ALLES
WHERE
    STATUS = 'Verblijfsobject gevormd';
-------------------------------------------------
-- V_VERBLIJFSOBJECT
-------------------------------------------------
CREATE OR REPLACE VIEW
    V_VERBLIJFSOBJECT
    (
        OBJECTID,
        FID,
        PAND_ID,
        GEMEENTE,
        WOONPLAATS,
        STRAATNAAM,
        HUISNUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        --GEBRUIKSDOEL,
        STATUS,
        OPPERVLAKTE,
        THE_GEOM
    ) AS
SELECT
    OBJECTID,
    FID,
    PAND_ID,
    GEMEENTE,
    WOONPLAATS,
    STRAATNAAM,
    HUISNUMMER,
    HUISLETTER,
    HUISNUMMER_TOEV,
    POSTCODE,
    --GEBRUIKSDOEL,
    STATUS,
    OPPERVLAKTE,
    THE_GEOM
FROM
    V_VERBLIJFSOBJECT_ALLES
WHERE
    STATUS = 'Verblijfsobject in gebruik (niet ingemeten)'
OR  STATUS = 'Verblijfsobject in gebruik';
-------------------------------------------------
-- V_PAND_IN_GEBRUIK
-------------------------------------------------
CREATE OR REPLACE VIEW
    V_PAND_IN_GEBRUIK
    (
        OBJECTID,
        FID,
        EIND_DATUM_GELDIG,
        BEGIN_DATUM_GELDIG,
        STATUS,
        BOUWJAAR,
        THE_GEOM
    ) AS
SELECT
    CAST(ROWNUM AS INTEGER) AS OBJECTID,
    P.IDENTIF               AS FID,
    P.DATUM_EINDE_GELDH     AS EIND_DATUM_GELDIG,
    P.DAT_BEG_GELDH         AS BEGIN_DATUM_GELDIG,
    P.STATUS,
    P.OORSPRONKELIJK_BOUWJAAR AS BOUWJAAR,
    P.GEOM_BOVENAANZICHT      AS THE_GEOM
FROM
    PAND P
WHERE
    STATUS IN ('Sloopvergunning verleend',
               'Pand in gebruik (niet ingemeten)',
               'Pand in gebruik',
               'Bouw gestart')
AND DATUM_EINDE_GELDH IS NULL;
-------------------------------------------------
-- V_PAND_GEBRUIK_NIET_INGEMETEN
-------------------------------------------------
CREATE OR REPLACE VIEW
    V_PAND_GEBRUIK_NIET_INGEMETEN
    (
        OBJECTID,
        FID,
        BEGIN_DATUM_GELDIG,
        STATUS,
        BOUWJAAR,
        THE_GEOM
    ) AS
SELECT
    CAST(ROWNUM AS INTEGER) AS OBJECTID,
    P.IDENTIF               AS FID,
    P.DAT_BEG_GELDH         AS BEGIN_DATUM_GELDIG,
    P.STATUS,
    P.OORSPRONKELIJK_BOUWJAAR AS BOUWJAAR,
    P.GEOM_BOVENAANZICHT      AS THE_GEOM
FROM
    PAND P
WHERE
    STATUS = 'Pand in gebruik (niet ingemeten)'
AND DATUM_EINDE_GELDH IS NULL;
-------------------------------------------------
-- V_STANDPLAATS
-------------------------------------------------
CREATE OR REPLACE VIEW
    V_STANDPLAATS
    (
        OBJECTID,
        SC_IDENTIF,
        STATUS,
        FK_4NRA_SC_IDENTIF,
        DATUM_BEGIN_GELDH,
        GEOMETRIE
    ) AS
SELECT
    CAST(ROWNUM AS INTEGER) AS OBJECTID,
    SP.SC_IDENTIF,
    SP.STATUS,
    SP.FK_4NRA_SC_IDENTIF,
    BT.DAT_BEG_GELDH,
    BT.GEOM AS GEOMETRIE
FROM
    STANDPLAATS SP
LEFT JOIN
    BENOEMD_TERREIN BT
ON
    (
        SP.SC_IDENTIF = BT.SC_IDENTIF);
-------------------------------------------------
-- V_LIGPLAATS
-------------------------------------------------
CREATE OR REPLACE VIEW
    V_LIGPLAATS
    (
        OBJECTID,
        SC_IDENTIF,
        STATUS,
        FK_4NRA_SC_IDENTIF,
        DAT_BEG_GELDH,
        GEOMETRIE
    ) AS
SELECT
    CAST(ROWNUM AS INTEGER) AS OBJECTID,
    LP.SC_IDENTIF,
    LP.STATUS,
    LP.FK_4NRA_SC_IDENTIF,
    BT.DAT_BEG_GELDH,
    BT.GEOM AS GEOMETRIE
FROM
    LIGPLAATS LP
LEFT JOIN
    BENOEMD_TERREIN BT
ON
    (
        LP.SC_IDENTIF = BT.SC_IDENTIF) ;
-------------------------------------------------
-- V_LIGPLAATS_ALLES
-------------------------------------------------
/*
LIGPLAATS MET HOOFDADRES
*/
CREATE OR REPLACE VIEW
    V_LIGPLAATS_ALLES
    (
        OBJECTID,
        FID,
        GEMEENTE,
        WOONPLAATS,
        STRAATNAAM,
        HUISNUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        STATUS,
        THE_GEOM
    ) AS
SELECT
    CAST(ROWNUM AS INTEGER) AS OBJECTID,
    LP.SC_IDENTIF           AS FID,
    GEM.NAAM                AS GEMEENTE,
    CASE
         WHEN ADDROBJ.FK_6WPL_IDENTIF IS NOT NULL
         -- opzoeken want in andere woonplaats
         THEN  (SELECT NAAM FROM WNPLTS WHERE IDENTIF = FK_6WPL_IDENTIF)
         ELSE WP.NAAM
    END                     AS WOONPLAATS,
    GEOR.NAAM_OPENB_RMTE    AS STRAATNAAM,
    ADDROBJ.HUINUMMER       AS HUISNUMMER,
    ADDROBJ.HUISLETTER,
    ADDROBJ.HUINUMMERTOEVOEGING AS HUISNUMMER_TOEV,
    ADDROBJ.POSTCODE,
    LP.STATUS,
    BT.GEOM AS THE_GEOM
FROM
    (((((((LIGPLAATS LP
JOIN
    BENOEMD_TERREIN BT
ON
    ((LP.SC_IDENTIF = BT.SC_IDENTIF)))
JOIN
    NUMMERAAND NA
ON
    ((NA.SC_IDENTIF = LP.FK_4NRA_SC_IDENTIF)))
JOIN
    ADDRESSEERB_OBJ_AAND ADDROBJ
ON
    ((ADDROBJ.IDENTIF = NA.SC_IDENTIF)))
JOIN
    GEM_OPENB_RMTE GEOR
ON
    ((GEOR.IDENTIFCODE = ADDROBJ.FK_7OPR_IDENTIFCODE)))
LEFT JOIN
    OPENB_RMTE_WNPLTS ORWP
ON
    ((GEOR.IDENTIFCODE = ORWP.FK_NN_LH_OPR_IDENTIFCODE)))
LEFT JOIN
    WNPLTS WP
ON
    ((ORWP.FK_NN_RH_WPL_IDENTIF = WP.IDENTIF)))
LEFT JOIN
    GEMEENTE GEM
ON
    ((
            WP.FK_7GEM_CODE = GEM.CODE)))
WHERE
    ((((
                    ADDROBJ.DAT_EIND_GELDH IS NULL)
            AND (
                    GEOR.DATUM_EINDE_GELDH IS NULL))
        AND (
                GEM.DATUM_EINDE_GELDH IS NULL))
    AND (
            BT.DATUM_EINDE_GELDH IS NULL));
-------------------------------------------------
-- V_STANDPLAATS_ALLES
-------------------------------------------------
/*
STANDPLAATS MET HOOFDADRES
*/
CREATE OR REPLACE VIEW
    V_STANDPLAATS_ALLES
    (
        OBJECTID,
        FID,
        GEMEENTE,
        WOONPLAATS,
        STRAATNAAM,
        HUISNUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        STATUS,
        THE_GEOM
    ) AS
SELECT
    CAST(ROWNUM AS INTEGER) AS OBJECTID,
    SP.SC_IDENTIF           AS FID,
    GEM.NAAM                AS GEMEENTE,
    CASE
         WHEN ADDROBJ.FK_6WPL_IDENTIF IS NOT NULL
         -- opzoeken want in andere woonplaats
         THEN  (SELECT NAAM FROM WNPLTS WHERE IDENTIF = FK_6WPL_IDENTIF)
         ELSE WP.NAAM
    END                     AS WOONPLAATS,
    GEOR.NAAM_OPENB_RMTE    AS STRAATNAAM,
    ADDROBJ.HUINUMMER       AS HUISNUMMER,
    ADDROBJ.HUISLETTER,
    ADDROBJ.HUINUMMERTOEVOEGING AS HUISNUMMER_TOEV,
    ADDROBJ.POSTCODE,
    SP.STATUS,
    BT.GEOM AS THE_GEOM
FROM
    (((((((STANDPLAATS SP
JOIN
    BENOEMD_TERREIN BT
ON
    ((SP.SC_IDENTIF = BT.SC_IDENTIF)))
JOIN
    NUMMERAAND NA
ON
    ((NA.SC_IDENTIF = SP.FK_4NRA_SC_IDENTIF)))
JOIN
    ADDRESSEERB_OBJ_AAND ADDROBJ
ON
    ((ADDROBJ.IDENTIF = NA.SC_IDENTIF)))
JOIN
    GEM_OPENB_RMTE GEOR
ON
    ((GEOR.IDENTIFCODE = ADDROBJ.FK_7OPR_IDENTIFCODE)))
LEFT JOIN
    OPENB_RMTE_WNPLTS ORWP
ON
    ((GEOR.IDENTIFCODE = ORWP.FK_NN_LH_OPR_IDENTIFCODE)))
LEFT JOIN
    WNPLTS WP
ON
    ((ORWP.FK_NN_RH_WPL_IDENTIF = WP.IDENTIF)))
LEFT JOIN
    GEMEENTE GEM
ON
    ((
            WP.FK_7GEM_CODE = GEM.CODE)))
WHERE
    ((((
                    ADDROBJ.DAT_EIND_GELDH IS NULL)
            AND (
                    GEOR.DATUM_EINDE_GELDH IS NULL))
        AND (
                GEM.DATUM_EINDE_GELDH IS NULL))
    AND (
            BT.DATUM_EINDE_GELDH IS NULL));
-------------------------------------------------
-- V_ADRES
-------------------------------------------------
/*
VOLLEDIGE ADRESSENLIJST
STANDPLAATS EN LIGPLAATS VIA BENOEMD_TERREIN,
WAARBIJ CENTROIDE VAN POLYGON WORDT GENOMEN
PLUS VERBLIJFSOBJECT VIA PUNT OBJECT VAN GEBOUWD_OBJ
*/
CREATE OR REPLACE VIEW
    V_ADRES
    (
        OBJECTID,
        FID,
        GEMEENTE,
        WOONPLAATS,
        STRAATNAAM,
        HUISNUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        STATUS,
        OPPERVLAKTE,
        THE_GEOM
    ) AS
SELECT
    CAST(ROWNUM AS INTEGER) AS OBJECTID,
    VBO.SC_IDENTIF          AS FID,
    GEM.NAAM                AS GEMEENTE,
    CASE
         WHEN ADDROBJ.FK_6WPL_IDENTIF IS NOT NULL
         -- opzoeken want in andere woonplaats
         THEN  (SELECT NAAM FROM WNPLTS WHERE IDENTIF = FK_6WPL_IDENTIF)
         ELSE WP.NAAM
    END                     AS WOONPLAATS,
    GEOR.NAAM_OPENB_RMTE    AS STRAATNAAM,
    ADDROBJ.HUINUMMER       AS HUISNUMMER,
    ADDROBJ.HUISLETTER,
    ADDROBJ.HUINUMMERTOEVOEGING AS HUISNUMMER_TOEV,
    ADDROBJ.POSTCODE,
    VBO.STATUS,
    GOBJ.OPPERVLAKTE_OBJ || ' m2' AS OPPERVLAKTE,
    GOBJ.PUNTGEOM                 AS THE_GEOM
FROM
    VERBLIJFSOBJ VBO
JOIN
    GEBOUWD_OBJ GOBJ
ON
    (
        GOBJ.SC_IDENTIF = VBO.SC_IDENTIF )
LEFT JOIN
    VERBLIJFSOBJ_NUMMERAAND VNA
ON
    (
        VNA.FK_NN_LH_VBO_SC_IDENTIF = VBO.SC_IDENTIF )
LEFT JOIN
    NUMMERAAND NA
ON
    (
        NA.SC_IDENTIF = VBO.FK_11NRA_SC_IDENTIF)
LEFT JOIN
    ADDRESSEERB_OBJ_AAND ADDROBJ
ON
    (
        ADDROBJ.IDENTIF = NA.SC_IDENTIF )
JOIN
    GEM_OPENB_RMTE GEOR
ON
    (
        GEOR.IDENTIFCODE = ADDROBJ.FK_7OPR_IDENTIFCODE )
LEFT JOIN
    OPENB_RMTE_WNPLTS ORWP
ON
    (
        GEOR.IDENTIFCODE = ORWP.FK_NN_LH_OPR_IDENTIFCODE)
LEFT JOIN
    WNPLTS WP
ON
    (
        ORWP.FK_NN_RH_WPL_IDENTIF = WP.IDENTIF)
LEFT JOIN
    GEMEENTE GEM
ON
    (
        WP.FK_7GEM_CODE = GEM.CODE )
WHERE
    NA.STATUS = 'Naamgeving uitgegeven'
AND (
        VBO.STATUS = 'Verblijfsobject in gebruik (niet ingemeten)'
    OR  VBO.STATUS = 'Verblijfsobject in gebruik');

-------------------------------------------------
-- V_ADRES_LIGPLAATS
-------------------------------------------------
CREATE OR REPLACE VIEW
    V_ADRES_LIGPLAATS
    (
        FID,
        GEMEENTE,
        WOONPLAATS,
        STRAATNAAM,
        HUISNUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        STATUS,
        THE_GEOM,
        CENTROIDE
    ) AS
SELECT
    LPA.SC_IDENTIF       AS FID,
    GEM.NAAM             AS GEMEENTE,
    CASE
         WHEN ADDROBJ.FK_6WPL_IDENTIF IS NOT NULL
         -- opzoeken want in andere woonplaats
         THEN  (SELECT NAAM FROM WNPLTS WHERE IDENTIF = FK_6WPL_IDENTIF)
         ELSE WP.NAAM
    END                  AS WOONPLAATS,
    GEOR.NAAM_OPENB_RMTE AS STRAATNAAM,
    ADDROBJ.HUINUMMER    AS HUISNUMMER,
    ADDROBJ.HUISLETTER,
    ADDROBJ.HUINUMMERTOEVOEGING AS HUISNUMMER_TOEV,
    ADDROBJ.POSTCODE,
    LPA.STATUS,
    BENTER.GEOM AS THE_GEOM,
    SDO_GEOM.SDO_CENTROID(BENTER.GEOM,2)
FROM
    LIGPLAATS LPA
JOIN
    BENOEMD_TERREIN BENTER
ON
    (
        BENTER.SC_IDENTIF = LPA.SC_IDENTIF )
LEFT JOIN
    LIGPLAATS_NUMMERAAND LNA
ON
    (
        LNA.FK_NN_LH_LPL_SC_IDENTIF = LPA.SC_IDENTIF )
LEFT JOIN
    NUMMERAAND NA
ON
    (
        NA.SC_IDENTIF = LPA.FK_4NRA_SC_IDENTIF )
LEFT JOIN
    ADDRESSEERB_OBJ_AAND ADDROBJ
ON
    (
        ADDROBJ.IDENTIF = NA.SC_IDENTIF )
JOIN
    GEM_OPENB_RMTE GEOR
ON
    (
        GEOR.IDENTIFCODE = ADDROBJ.FK_7OPR_IDENTIFCODE )
LEFT JOIN
    OPENB_RMTE_WNPLTS ORWP
ON
    (
        GEOR.IDENTIFCODE = ORWP.FK_NN_LH_OPR_IDENTIFCODE)
LEFT JOIN
    WNPLTS WP
ON
    (
        ORWP.FK_NN_RH_WPL_IDENTIF = WP.IDENTIF)
LEFT JOIN
    GEMEENTE GEM
ON
    (
        WP.FK_7GEM_CODE = GEM.CODE )
WHERE
    NA.STATUS = 'Naamgeving uitgegeven'
AND LPA.STATUS = 'Plaats aangewezen';
-------------------------------------------------
-- V_ADRES_STANDPLAATS
-------------------------------------------------
CREATE OR REPLACE VIEW
    V_ADRES_STANDPLAATS
    (
        FID,
        GEMEENTE,
        WOONPLAATS,
        STRAATNAAM,
        HUISNUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        STATUS,
        THE_GEOM,
        CENTROIDE
    ) AS
SELECT
    SPL.SC_IDENTIF       AS FID,
    GEM.NAAM             AS GEMEENTE,
    CASE
         WHEN ADDROBJ.FK_6WPL_IDENTIF IS NOT NULL
         -- opzoeken want in andere woonplaats
         THEN  (SELECT NAAM FROM WNPLTS WHERE IDENTIF = FK_6WPL_IDENTIF)
         ELSE WP.NAAM
    END                  AS WOONPLAATS,
    GEOR.NAAM_OPENB_RMTE AS STRAATNAAM,
    ADDROBJ.HUINUMMER    AS HUISNUMMER,
    ADDROBJ.HUISLETTER,
    ADDROBJ.HUINUMMERTOEVOEGING AS HUISNUMMER_TOEV,
    ADDROBJ.POSTCODE,
    SPL.STATUS,
    BENTER.GEOM AS THE_GEOM,
    SDO_GEOM.SDO_CENTROID(BENTER.GEOM,2)
FROM
    STANDPLAATS SPL
JOIN
    BENOEMD_TERREIN BENTER
ON
    (
        BENTER.SC_IDENTIF = SPL.SC_IDENTIF )
LEFT JOIN
    STANDPLAATS_NUMMERAAND SNA
ON
    (
        SNA.FK_NN_LH_SPL_SC_IDENTIF = SPL.SC_IDENTIF )
LEFT JOIN
    NUMMERAAND NA
ON
    (
        NA.SC_IDENTIF = SPL.FK_4NRA_SC_IDENTIF)
LEFT JOIN
    ADDRESSEERB_OBJ_AAND ADDROBJ
ON
    (
        ADDROBJ.IDENTIF = NA.SC_IDENTIF )
JOIN
    GEM_OPENB_RMTE GEOR
ON
    (
        GEOR.IDENTIFCODE = ADDROBJ.FK_7OPR_IDENTIFCODE )
LEFT JOIN
    OPENB_RMTE_WNPLTS ORWP
ON
    (
        GEOR.IDENTIFCODE = ORWP.FK_NN_LH_OPR_IDENTIFCODE)
LEFT JOIN
    WNPLTS WP
ON
    (
        ORWP.FK_NN_RH_WPL_IDENTIF = WP.IDENTIF)
LEFT JOIN
    GEMEENTE GEM
ON
    (
        WP.FK_7GEM_CODE = GEM.CODE )
WHERE
    NA.STATUS = 'Naamgeving uitgegeven'
AND SPL.STATUS = 'Plaats aangewezen';

-------------------------------------------------
-- V_ADRES_TOTAAL
-------------------------------------------------
CREATE OR REPLACE VIEW
    V_ADRES_TOTAAL
    (
        OBJECTID,
        FID,
        STRAATNAAM,
        HUISNUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        GEMEENTE,
        WOONPLAATS,
        THE_GEOM
    ) AS
  SELECT
    CAST(ROWNUM AS INTEGER) AS OBJECTID,
    QRY.*
    FROM (
        SELECT
            FID ,
            STRAATNAAM,
            HUISNUMMER,
            HUISLETTER,
            HUISNUMMER_TOEV,
            POSTCODE,
            GEMEENTE,
        		WOONPLAATS,
            THE_GEOM
        FROM
            V_ADRES
        UNION ALL
        SELECT
            FID ,
            STRAATNAAM,
            HUISNUMMER,
            HUISLETTER,
            HUISNUMMER_TOEV,
            POSTCODE,
            GEMEENTE,
        		WOONPLAATS,
            CENTROIDE AS THE_GEOM
        FROM
            V_ADRES_LIGPLAATS
        UNION ALL
        SELECT
            FID ,
            STRAATNAAM,
            HUISNUMMER,
            HUISLETTER,
            HUISNUMMER_TOEV,
            POSTCODE,
            GEMEENTE,
        		WOONPLAATS,
            CENTROIDE AS THE_GEOM
        FROM
            V_ADRES_STANDPLAATS
    ) QRY;

    
-------------------------------------------------
-- v_adres_pandvlak: adressen met (maaiveld) pandvlak
-------------------------------------------------
CREATE OR REPLACE VIEW
    v_adres_pandvlak
    (
        objectid,
        fid,
        pand_id,
        gemeente,
        woonplaats,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        status,
        the_geom
    ) AS
SELECT
    CAST(ROWNUM AS INTEGER) AS OBJECTID,
    vbo.sc_identif              AS fid,
    fkpand.fk_nn_rh_pnd_identif AS pand_id,
    gem.naam                    AS gemeente,
    CASE
        WHEN addrobj.fk_6wpl_identif IS NOT NULL
        -- opzoeken want in andere woonplaats
        THEN  (select naam from wnplts where identif = fk_6wpl_identif)
        ELSE wp.naam           
    END                  AS woonplaats,
    geor.naam_openb_rmte AS straatnaam,
    addrobj.huinummer    AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    vbo.status,
    pand.geom_bovenaanzicht AS the_geom
FROM (
    verblijfsobj vbo
JOIN
    verblijfsobj_pand fkpand
ON
    (fkpand.fk_nn_lh_vbo_sc_identif = vbo.sc_identif)
JOIN 
    pand 
ON 
    (fkpand.fk_nn_rh_pnd_identif = pand.identif) 
)    
LEFT JOIN
    verblijfsobj_nummeraand vna
ON
    (vna.fk_nn_lh_vbo_sc_identif = vbo.sc_identif)

LEFT JOIN
    nummeraand na
ON
    (na.sc_identif = vbo.fk_11nra_sc_identif)

LEFT JOIN
    addresseerb_obj_aand addrobj
ON
    (addrobj.identif = na.sc_identif)
JOIN
    gem_openb_rmte geor
ON
    ( geor.identifcode = addrobj.fk_7opr_identifcode )
    
LEFT JOIN
    openb_rmte_wnplts orwp
ON
    ( geor.identifcode = orwp.fk_nn_lh_opr_identifcode)

LEFT JOIN
    wnplts wp
ON
    ( orwp.fk_nn_rh_wpl_identif = wp.identif)

LEFT JOIN
    gemeente gem
ON
    ( wp.fk_7gem_code = gem.code )
WHERE
    na.status = 'Naamgeving uitgegeven'
AND ( vbo.status = 'Verblijfsobject in gebruik (niet ingemeten)'
    OR  vbo.status = 'Verblijfsobject in gebruik');

-------------------------------------------------
-- v_adres_totaal_vlak: adressen met maaiveld vlak van pand 
--   of openbare ruimte in geval stand of ligplaats
-------------------------------------------------
CREATE OR REPLACE VIEW 
    v_adres_totaal_vlak
    (
        objectid,
        fid,
        straatnaam,
        huisnummer,
        huisletter,
        huisnummer_toev,
        postcode,
        gemeente,
        woonplaats,
        the_geom
    ) AS
SELECT 
    CAST(ROWNUM AS INTEGER) AS OBJECTID,
    qry.*
    FROM (
        SELECT
            fid,
            straatnaam,
            huisnummer,
            huisletter,
            huisnummer_toev,
            postcode,
            gemeente,
            woonplaats,
            the_geom
        FROM
            v_adres_pandvlak
        UNION ALL
        SELECT
            fid ,
            straatnaam,
            huisnummer,
            huisletter,
            huisnummer_toev,
            postcode,
            gemeente,
            woonplaats,
            the_geom
        FROM
            v_adres_ligplaats
        UNION ALL
        SELECT
            fid ,
            straatnaam,
            huisnummer,
            huisletter,
            huisnummer_toev,
            postcode,
            gemeente,
            woonplaats,
            the_geom
        FROM
            v_adres_standplaats
    ) qry;
    
INSERT INTO USER_SDO_GEOM_METADATA VALUES('V_ADRES_PANDVLAK', 'THE_GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
INSERT INTO USER_SDO_GEOM_METADATA VALUES('V_ADRES_TOTAAL_VLAK', 'THE_GEOM', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);


-- optioneel: bijwerken Geotools / geoserver metadata tabellen, zie ook utility scripts:
--    402_create_geotools_geometrycolumns_metatable.sql
--    403_create_geotools_primarykey_metatable.sql
-- in directory brmo/datamodel/utility_scripts/oracle/
-- (let op de schemanaam 'RSGB' in onderstaande inserts moet mogelijk aangepast worden) en mogelijk zitten
--   er al records voor betreffende views in de tabel, in dat geval deze eerste verwijderen.
--
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_BD_KAD_PERCEEL_MET_APP_VLAK', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE)
--     VALUES ('RSGB', 'V_BD_KAD_PERCEEL_MET_APP_VLAK', 'BEGRENZING_PERCEEL', 2, 28992, 'MULTIPOLYGON');

-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_KAD_PERCEEL_ZR_ADRESSEN', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_BD_APP_RE_AND_KAD_PERCEEL', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE)
--    VALUES ('RSGB', 'VM_KAD_EIGENARENKAART', 'BEGRENZING_PERCEEL', 2, 28992, 'MULTIPOLYGON');
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'VM_KAD_EIGENARENKAART', 'OBJECTID', NULL, 'assigned', NULL);

-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_BD_APP_RE_AND_KAD_PERCEEL', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_BD_APP_RE_BIJ_PERCEEL', 'OBJECTID', NULL, 'assigned', NULL);

-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_KAD_PERCEEL_EENVOUDIG', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_KAD_PERCEEL_IN_EIGENDOM', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_KAD_PERCEEL_ZR_ADRESSEN', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_MAP_KAD_PERCEEL', 'OBJECTID', NULL, 'assigned', NULL);

-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_ADRES', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_ADRES_TOTAAL', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_LIGPLAATS', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_LIGPLAATS_ALLES', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_PAND_GEBRUIK_NIET_INGEMETEN', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_PAND_IN_GEBRUIK', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_STANDPLAATS', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_STANDPLAATS_ALLES', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_VERBLIJFSOBJECT', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_VERBLIJFSOBJECT_ALLES', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_VERBLIJFSOBJECT_GEVORMD', 'OBJECTID', NULL, 'assigned', NULL);

-- adres vlakken
-- INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
--    VALUES ('RSGBTEST', 'V_ADRES_PANDVLAK', 'THE_GEOM', 2, 28992, 'MULTIPOLYGON');
-- INSERT INTO GEOMETRY_COLUMNS (F_TABLE_SCHEMA, F_TABLE_NAME, F_GEOMETRY_COLUMN, COORD_DIMENSION, SRID, TYPE) 
--    VALUES ('RSGBTEST', 'V_ADRES_TOTAAL_VLAK', 'THE_GEOM', 2, 28992, 'MULTIPOLYGON');
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_ADRES_PANDVLAK', 'OBJECTID', NULL, 'assigned', NULL);
-- INSERT INTO GT_PK_METADATA VALUES ('RSGB', 'V_ADRES_TOTAAL_VLAK', 'OBJECTID', NULL, 'assigned', NULL);
