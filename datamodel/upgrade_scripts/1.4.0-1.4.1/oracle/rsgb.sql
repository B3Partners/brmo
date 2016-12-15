--
-- upgrade RSGB datamodel van 1.4.0 naar 1.4.1 (Oracle)
--
-- brmo versie informatie
CREATE TABLE BRMO_METADATA
    (
        NAAM VARCHAR2(255 CHAR) NOT NULL,
        WAARDE VARCHAR2(255 CHAR),
        PRIMARY KEY (NAAM)
    );
COMMENT ON TABLE BRMO_METADATA IS 'BRMO metadata en versie gegevens';

INSERT INTO brmo_metadata (naam, waarde) VALUES ('brmoversie','1.4.1');

-- optimalisatie VM_KAD_EIGENARENKAART (#268)
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
  WHERE zr.kadaster_identif LIKE 'NL.KAD.T%';

CREATE UNIQUE INDEX VM_KAD_EIGENARENKAART_OID_IDX ON VM_KAD_EIGENARENKAART (OBJECTID ASC);
DELETE FROM USER_SDO_GEOM_METADATA WHERE TABLE_NAME='VM_KAD_EIGENARENKAART' AND  COLUMN_NAME='BEGRENZING_PERCEEL';
INSERT INTO USER_SDO_GEOM_METADATA VALUES('VM_KAD_EIGENARENKAART', 'BEGRENZING_PERCEEL', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
CREATE INDEX VM_KAD_EIGENARENKAART_PERC_IDX ON VM_KAD_EIGENARENKAART (BEGRENZING_PERCEEL) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
