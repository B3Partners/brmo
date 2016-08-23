--
-- upgrade RSGB datamodel van 1.3.5 naar 1.3.6 (PostgreSQL)
--
-- vergroten van het veld 'omschrijving' in de tabel brondocument van 40 naar 255 characters
--    drop de van van brondocument afhankelijke views
DROP VIEW IF EXISTS v_kad_eigenarenkaart;
DROP VIEW IF EXISTS v_aankoopdatum;
DROP VIEW IF EXISTS v_bd_kad_perceel_met_app;
DROP VIEW IF EXISTS v_bd_kad_perceel_with_app_re;
DROP VIEW IF EXISTS v_bd_app_re_bij_perceel;
DROP VIEW IF EXISTS v_bd_app_re_and_kad_perceel;
DROP VIEW IF EXISTS v_bd_app_re_all_kad_perceel;
DROP VIEW IF EXISTS v_bd_app_re_app_re;
DROP VIEW IF EXISTS kad_perceel_app_rechten;

--    pas tabel brondocument aan
ALTER TABLE
    brondocument ALTER COLUMN omschrijving TYPE CHARACTER VARYING(255);

--    herstel gedropte views uit 105_appartements_rechten.sql
--    v_bd_app_re_app_re
CREATE OR REPLACE VIEW
    v_bd_app_re_app_re AS
SELECT
    b1.ref_id AS app_re_identif,
    b2.ref_id AS parent_app_re_identif
FROM
    brondocument b1
JOIN
    brondocument b2
ON
    b2.identificatie = b1.identificatie
WHERE
    b2.omschrijving = 'betrokkenBij Ondersplitsing'
AND b1.omschrijving = 'ontstaanUit Ondersplitsing'
GROUP BY
    b1.ref_id,
    b2.ref_id;
--    v_bd_app_re_all_kad_perceel
CREATE OR REPLACE VIEW
    v_bd_app_re_all_kad_perceel AS
WITH
    recursive related_app_re
    (
        app_re_identif,
        perceel_identif
    ) AS
    (
        SELECT
            b1.ref_id AS app_re_identif,
            b2.ref_id AS perceel_identif
        FROM
            brondocument b1
        JOIN
            brondocument b2
        ON
            b2.identificatie = b1.identificatie
        WHERE
            b2.omschrijving = 'betrokkenBij HoofdSplitsing'
        AND b1.omschrijving = 'ontstaanUit HoofdSplitsing'
        GROUP BY
            b1.ref_id,
            b2.ref_id
        UNION
        SELECT
            vaa.app_re_identif,
            vap.perceel_identif
        FROM
            v_bd_app_re_app_re vaa
        JOIN
            related_app_re vap
        ON
            vaa.parent_app_re_identif = vap.app_re_identif
        GROUP BY
            vaa.app_re_identif,
            vap.perceel_identif
    )
SELECT
    rar.*
FROM
    related_app_re rar;
--    v_bd_app_re_bij_perceel
CREATE OR REPLACE VIEW
    v_bd_app_re_bij_perceel AS
SELECT
    ar.sc_kad_identif,
    ar.fk_2nnp_sc_identif,
    ar.ka_appartementsindex,
    ar.ka_kad_gemeentecode,
    ar.ka_perceelnummer,
    ar.ka_sectie,
    kp.begrenzing_perceel
FROM
    v_bd_app_re_all_kad_perceel v
JOIN
    kad_perceel kp
ON
    v.perceel_identif = kp.sc_kad_identif::VARCHAR
JOIN
    app_re ar
ON
    v.app_re_identif = ar.sc_kad_identif::VARCHAR;
--     v_bd_kad_perceel_with_app_re
CREATE OR REPLACE VIEW
    v_bd_kad_perceel_with_app_re AS
SELECT DISTINCT
    b2.ref_id AS perceel_identif
FROM
    brondocument b1
JOIN
    brondocument b2
ON
    b2.identificatie = b1.identificatie
WHERE
    b2.omschrijving = 'betrokkenBij HoofdSplitsing'
AND b1.omschrijving = 'ontstaanUit HoofdSplitsing';
CREATE OR REPLACE VIEW
    v_bd_kad_perceel_met_app AS
SELECT
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
    kp.begrenzing_perceel,
    kp.plaatscoordinaten_perceel
FROM
    v_bd_kad_perceel_with_app_re v
JOIN
    kad_perceel kp
ON
    v.perceel_identif = kp.sc_kad_identif::VARCHAR;


--    herstel gedropte views uit 107_brk_views.sql
--    kad_perceel_app_rechten
CREATE OR REPLACE VIEW
    kad_perceel_app_rechten AS
SELECT
    kpe.SC_KAD_IDENTIF AS perceel_identificatie,
    -- kpe.KA_SECTIE || ' ' || kpe.KA_PERCEELNUMMER as perceelnr,
    kpe.aanduiding,
    kpe.straat,
    kpe.huisnummer,
    kpe.toevoeging,
    kpe.huisletter,
    kpe.straat || ' ' || kpe.huisnummer || ' ' || kpe.huisletter || ' ' || kpe.toevoeging || ' ' ||
    kpe.postcode AS adres,
    -- zr.kadaster_identif as links_zak_recht,
    zr.FK_3AVR_AAND AS complex_zak_recht_aard_aand,
    -- zr.FK2_PERSOON as links_zak_recht_persoon,
    --    case when np1.PK_PERSOON is not null then 'Natuurlijk persoon' else 'Niet natuurlijk
    -- persoon' end as l_soort_eigenaar,
    CASE
        WHEN np1.sc_identif IS NOT NULL
        THEN np1.NM_GESLACHTSNAAM || ', ' || np1.NM_VOORNAMEN || ' ' ||
            np1.NM_VOORVOEGSEL_GESLACHTSNAAM
        ELSE nnp1.NAAM
    END AS perceel_zak_recht_naam,
    --    nnp1.NAAM as l_nnp,
    -- bd1.identificatie as brondocument,
    -- zr2.kadaster_identif as rechts_zak_recht,
    zr2.FK_3AVR_AAND AS app_re_zak_recht_aard_aand,
    -- zr2.FK2_PERSOON as rechts_zak_recht_persoon,
    --    case when np2.PK_PERSOON is not null then 'Natuurlijk persoon' else 'Niet natuurlijk
    -- persoon' end as r_soort_eigenaar,
    CASE
        WHEN np2.sc_identif IS NOT NULL
        THEN np2.NM_GESLACHTSNAAM || ', ' || np2.NM_VOORNAMEN || ' ' ||
            np2.NM_VOORVOEGSEL_GESLACHTSNAAM
        ELSE nnp2.NAAM
    END AS app_re_zak_recht_naam,
    --    nnp2.NAAM as r_nnp,
    ar.SC_KAD_IDENTIF            AS app_re_identificatie,
    ar.KA_APPARTEMENTSINDEX::INT AS appartementsindex --,
    -- ar.FK1_NIET_NAT_PERSOON as app_re_vve,
    -- ar_vve_nnp.naam as app_re_vve_naam,
    -- ar_vve_innp.rechtsvorm as app_re_vve_rechtsvorm,
    -- ar_vve_innp.rsin as app_re_vve_rsin
FROM
    v_kad_perceel_eenvoudig kpe
JOIN
    zak_recht zr
ON
    (
        zr.FK_7KOZ_KAD_IDENTIF = kpe.SC_KAD_IDENTIF)
LEFT JOIN
    nat_prs np1
ON
    (
        np1.SC_IDENTIF = zr.FK_8PES_SC_IDENTIF)
LEFT JOIN
    ingeschr_nat_prs inp1
ON
    (
        inp1.SC_IDENTIF = np1.SC_IDENTIF)
LEFT JOIN
    niet_nat_prs nnp1
ON
    (
        nnp1.sc_identif = zr.FK_8PES_SC_IDENTIF)
LEFT JOIN
    ingeschr_niet_nat_prs innp1
ON
    (
        innp1.sc_identif = nnp1.sc_identif)
JOIN
    brondocument bd1
ON
    (
        bd1.tabel = 'ZAK_RECHT'
    AND bd1.tabel_identificatie = zr.kadaster_identif)
JOIN
    brondocument bd2
ON
    (
        bd2.tabel = 'ZAK_RECHT'
    AND bd2.tabel_identificatie <> zr.kadaster_identif
    AND bd2.identificatie = bd1.identificatie)
JOIN
    zak_recht zr2
ON
    (
        zr2.kadaster_identif = bd2.tabel_identificatie)
LEFT JOIN
    nat_prs np2
ON
    (
        np2.SC_IDENTIF = zr2.FK_8PES_SC_IDENTIF)
LEFT JOIN
    ingeschr_nat_prs inp2
ON
    (
        inp2.SC_IDENTIF = np2.SC_IDENTIF)
LEFT JOIN
    niet_nat_prs nnp2
ON
    (
        nnp2.sc_identif = zr2.FK_8PES_SC_IDENTIF)
LEFT JOIN
    ingeschr_niet_nat_prs innp2
ON
    (
        innp2.sc_identif = nnp2.sc_identif)
JOIN
    app_re ar
ON
    (
        ar.SC_KAD_IDENTIF = zr2.FK_7KOZ_KAD_IDENTIF)
JOIN
    niet_nat_prs ar_vve_nnp
ON
    (
        ar_vve_nnp.sc_identif = ar.FK_2NNP_SC_IDENTIF)
JOIN
    INGESCHR_NIET_NAT_PRS ar_vve_innp
ON
    (
        ar_vve_innp.sc_identif = ar_vve_nnp.sc_identif)
WHERE
    bd1.omschrijving LIKE 'betrokkenBij%'
AND zr2.FK_8PES_SC_IDENTIF IS NOT NULL
ORDER BY
    kpe.SC_KAD_IDENTIF,
    kpe.straat,
    kpe.huisnummer,
    kpe.toevoeging,
    kpe.huisletter,
    KA_APPARTEMENTSINDEX::INT;

-- percelen plus appartementen op de percelen
CREATE OR REPLACE VIEW v_bd_app_re_and_kad_perceel AS
select
    p.sc_kad_identif    AS kadaster_identificatie,
    'perceel' as type,
    p.ka_deelperceelnummer,
    '' as ka_appartementsindex,
    p.ka_perceelnummer,
    p.ka_kad_gemeentecode,
    p.ka_sectie,
    p.begrenzing_perceel
FROM
    kad_perceel p
union all
SELECT
    ar.sc_kad_identif,
    'appartement' as type,
    '' as ka_deelperceelnummer,
    ar.ka_appartementsindex,
    ar.ka_perceelnummer,
    ar.ka_kad_gemeentecode,
    ar.ka_sectie,
    kp.begrenzing_perceel
   FROM v_bd_app_re_all_kad_perceel v
     JOIN kad_perceel kp ON v.perceel_identif::NUMERIC = kp.sc_kad_identif
     JOIN app_re ar ON v.app_re_identif::NUMERIC = ar.sc_kad_identif;

-- aankoopdatum uit brondocumenten
CREATE OR REPLACE VIEW
    v_aankoopdatum AS
SELECT
    b.ref_id AS kadaster_identificatie,
    b.datum  AS aankoopdatum
FROM
    (
        SELECT
            ref_id,
            MAX(datum) datum
        FROM
            brondocument
        WHERE
            omschrijving = 'Akte van Koop en Verkoop'
        GROUP BY
            ref_id
    ) b ;

-- aanpassen eigenarenkaart

-- Eigenarenkaart - percelen en appartementen met hun eigenaren
CREATE OR REPLACE VIEW
    V_KAD_EIGENARENKAART
    (
        OBJECTID,
        KADASTER_IDENTIFICATIE,
        TYPE,
        ZAKELIJK_RECHT_IDENTIFICATIE,
        AANDEEL_TELLER,
        AANDEEL_NOEMER,
        AARD_RECHT_AAND,
        ZAKELIJK_RECHT_OMSCHRIJVING,
        AANKOOPDATUM,
        SOORT_EIGENAAR,
        GESLACHTSNAAM,
        VOORVOEGSEL,
        VOORNAMEN,
        GESLACHT,
        PERCEEL_ZAK_RECHT_NAAM,
        PERSOON_IDENTIFICATIE,
        WOONADRES,
        GEBOORTEDATUM,
        GEBOORTEPLAATS,
        OVERLIJDENSDATUM,
        NAAM_NIET_NATUURLIJK_PERSOON,
        RECHTSVORM,
        STATUTAIRE_ZETEL,
        KVK_NUMMER,
        KA_APPARTEMENTSINDEX,
        KA_DEELPERCEELNUMMER,
        KA_PERCEELNUMMER,
        KA_KAD_GEMEENTECODE,
        KA_SECTIE,
        BEGRENZING_PERCEEL
    ) AS
SELECT
    row_number() OVER () AS objectid,
    p.kadaster_identificatie    AS kadaster_identificatie,
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
        THEN np.NM_GESLACHTSNAAM || ', ' || np.NM_VOORNAMEN || ' ' ||
            np.NM_VOORVOEGSEL_GESLACHTSNAAM
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
FROM
    v_bd_app_re_and_kad_perceel p
JOIN
    zak_recht zr
ON
    zr.fk_7koz_kad_identif = p.kadaster_identificatie
LEFT JOIN
    aard_recht_verkort ark
ON
    zr.fk_3avr_aand = ark.aand
LEFT JOIN
    aard_verkregen_recht ar
ON
    zr.fk_3avr_aand = ar.aand
LEFT JOIN
    nat_prs np
ON
    np.sc_identif = zr.fk_8pes_sc_identif
LEFT JOIN
    ingeschr_nat_prs inp
ON
    inp.sc_identif = np.sc_identif
LEFT JOIN
    niet_nat_prs nnp
ON
    nnp.sc_identif = zr.fk_8pes_sc_identif
LEFT JOIN
    ingeschr_niet_nat_prs innp
ON
    innp.sc_identif = nnp.sc_identif
LEFT JOIN
    subject innp_subject
ON
    innp_subject.identif = innp.sc_identif
LEFT JOIN
    v_aankoopdatum b
ON
    b.kadaster_identificatie::numeric(15,0) = p.kadaster_identificatie
WHERE
    zr.kadaster_identif like 'NL.KAD.Tenaamstelling%';

