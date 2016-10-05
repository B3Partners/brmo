-- extra brk views voor vlaardingen
-- 31-08-2016 (hersteld)
-----------------------------------
-- v_app_re_adres
-- v_bd_app_re_bij_perceel_vld
-- v_kad_perceel_deelperceel
-- v_kad_perceel_eigenaar
-- v_kad_perceel_eigenaar_geen_erfpacht
-- v_kad_perceel_eigenaar_nnp
-- v_kad_perceel_eigenaar_np
-- v_kad_perceel_eneco
-- v_kad_perceel_eneco_overig_zak_recht
-- v_kad_perceel_erfpacht
-- v_kad_perceel_erfpacht_gemeente
-- v_kad_perceel_erfpacht_test
-- v_kad_perceel_opzoeklijst_eigenaar
-- v_kad_perceel_sectie
-- v_kad_perceel_woningcorps
-- v_kad_perceel_woningcorps_erfpacht
-- v_kad_percelen_historische_relaties
-- v_kad_stukdelen_zr
-- v_kad_stukken_zr
-- v_zak_recht_aantek
------------------------------------

------------------------------------
-- v_app_re_adres
------------------------------------
CREATE OR REPLACE VIEW
    v_app_re_adres
    (
        app_re_identif,
        kad_bag_koppeling_benobj,
        straat,
        huisnummer,
        huisletter,
        toevoeging,
        postcode,
        woonplaats,
        the_geom
    ) AS
SELECT
    kpvbo.fk_nn_rh_koz_kad_identif AS app_re_identif,
    kpvbo.fk_nn_lh_tgo_identif     AS kad_bag_koppeling_benobj,
    gor.naam_openb_rmte            AS straat,
    aoa.huinummer                  AS huisnummer,
    aoa.huisletter,
    aoa.huinummertoevoeging AS toevoeging,
    aoa.postcode,
    wp.naam      AS woonplaats,
    gob.puntgeom AS the_geom
FROM
    ((((((benoemd_obj_kad_onrrnd_zk kpvbo
LEFT JOIN
    verblijfsobj vbo
ON
    (((
                vbo.sc_identif)::text = (kpvbo.fk_nn_lh_tgo_identif)::text)))
LEFT JOIN
    nummeraand na
ON
    (((
                na.sc_identif)::text = (vbo.fk_11nra_sc_identif)::text)))
LEFT JOIN
    addresseerb_obj_aand aoa
ON
    (((
                aoa.identif)::text = (na.sc_identif)::text)))
LEFT JOIN
    gem_openb_rmte gor
ON
    (((
                gor.identifcode)::text = (aoa.fk_7opr_identifcode)::text)))
LEFT JOIN
    wnplts wp
ON
    (((
                wp.identif)::text = (aoa.fk_6wpl_identif)::text)))
LEFT JOIN
    gebouwd_obj gob
ON
    (((
                gob.sc_identif)::text = (vbo.sc_identif)::text)))
WHERE
    ((
            na.status)::text = 'Naamgeving uitgegeven'::text);

------------------------------------
-- v_bd_app_re_bij_perceel_vld
------------------------------------

-- View: v_bd_app_re_bij_perceel_vld

-- DROP VIEW v_bd_app_re_bij_perceel_vld;

CREATE OR REPLACE VIEW v_bd_app_re_bij_perceel_vld AS 
 SELECT ar.sc_kad_identif,
    ar.fk_2nnp_sc_identif,
    ar.ka_appartementsindex,
    ar.ka_kad_gemeentecode,
    ar.ka_perceelnummer,
    ar.ka_sectie,
    kp.begrenzing_perceel,
    kp.sc_kad_identif AS perceel_identif,
    zr.ar_teller,
    zr.ar_noemer,
    zr.fk_3avr_aand,
    zr.fk_2aard_recht_verkort_aand,
    np.nm_geslachtsnaam,
    np.nm_voornamen,
    nnp.naam,
    arv.omschr,
    kp.plaatscoordinaten_perceel
   FROM v_bd_app_re_all_kad_perceel v
     JOIN kad_perceel kp ON v.perceel_identif::text = kp.sc_kad_identif::text
     JOIN app_re ar ON v.app_re_identif::text = ar.sc_kad_identif::text
     JOIN zak_recht zr ON zr.fk_7koz_kad_identif::text = ar.sc_kad_identif::text
     LEFT JOIN nat_prs np ON zr.fk_8pes_sc_identif::text = np.sc_identif::text
     LEFT JOIN niet_nat_prs nnp ON nnp.sc_identif::text = zr.fk_8pes_sc_identif::text
     JOIN aard_recht_verkort arv ON zr.fk_3avr_aand::text = arv.aand::text
  WHERE zr.fk_8pes_sc_identif IS NOT NULL
  ORDER BY ar.ka_appartementsindex::integer;

------------------------------------
-- v_kad_perceel_deelperceel
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_perceel_deelperceel
    (
        uniquecol,
        sc_kad_identif,
        aand_soort_grootte,
        grootte_perceel,
        omschr_deelperceel,
        fk_7kdp_sc_kad_identif,
        ka_deelperceelnummer,
        ka_kad_gemeentecode,
        ka_perceelnummer,
        ka_sectie,
        begrenzing_perceel,
        plaatscoordinaten_perceel
    ) AS
SELECT DISTINCT
    kp.sc_kad_identif AS uniquecol,
    kp.sc_kad_identif,
    kp.aand_soort_grootte,
    kp.grootte_perceel,
    kp.omschr_deelperceel,
    kp.fk_7kdp_sc_kad_identif,
    kp.ka_deelperceelnummer,
    kp.ka_kad_gemeentecode,
    kp.ka_perceelnummer,
    kp.ka_sectie,
    np.begrenzing_perceel,
    np.plaatscoordinaten_perceel
FROM
    ((kad_perceel kp
JOIN
    v_kad_percelen_historische_relaties hr
ON
    ((
            hr.root = kp.sc_kad_identif)))
LEFT JOIN
    kad_perceel np
ON
    ((
            hr.sc_kad_identif = np.sc_kad_identif)))
WHERE
    ((
            kp.begrenzing_perceel IS NULL)
    AND (
            np.begrenzing_perceel IS NOT NULL));

------------------------------------
-- v_kad_perceel_eigenaar
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_perceel_eigenaar
    (
        kadaster_identificatie,
        zakelijk_recht_identificatie,
        aandeel_teller,
        aandeel_noemer,
        aard_recht_aand,
        zakelijk_recht_omschrijving,
        soort_eigenaar,
        geslachtsnaam,
        voorvoegsel,
        voornamen,
        geslacht,
        totaal_naam,
        persoon_identificatie,
        woonadres,
        geboortedatum,
        geboorteplaats,
        overlijdensdatum,
        naam_niet_natuurlijk_persoon,
        rechtsvorm,
        statutaire_zetel,
        kvk_nummer,
        ka_deelperceelnummer,
        ka_perceelnummer,
        ka_kad_gemeentecode,
        ka_sectie,
        begrenzing_perceel
    ) AS
SELECT
    p.sc_kad_identif    AS kadaster_identificatie,
    zr.kadaster_identif AS zakelijk_recht_identificatie,
    zr.ar_teller        AS aandeel_teller,
    zr.ar_noemer        AS aandeel_noemer,
    zr.fk_3avr_aand     AS aard_recht_aand,
    ark.omschr          AS zakelijk_recht_omschrijving,
    CASE
        WHEN (np.sc_identif IS NOT NULL)
        THEN 'Natuurlijk persoon'::text
        ELSE 'Niet natuurlijk persoon'::text
    END                             AS soort_eigenaar,
    np.nm_geslachtsnaam             AS geslachtsnaam,
    np.nm_voorvoegsel_geslachtsnaam AS voorvoegsel,
    np.nm_voornamen                 AS voornamen,
    np.geslachtsaand                AS geslacht,
    CASE
        WHEN (np.sc_identif IS NOT NULL)
        THEN ((((((np.nm_voornamen)::text || ' '::text) || (COALESCE
            (np.nm_voorvoegsel_geslachtsnaam, ''::CHARACTER VARYING))::text) || ' '::text) ||
            (np.nm_geslachtsnaam)::text))::CHARACTER VARYING
        ELSE nnp.naam
    END                     AS totaal_naam,
    inp.sc_identif          AS persoon_identificatie,
    inp.va_loc_beschrijving AS woonadres,
    inp.gb_geboortedatum    AS geboortedatum,
    inp.gb_geboorteplaats   AS geboorteplaats,
    inp.ol_overlijdensdatum AS overlijdensdatum,
    nnp.naam                AS naam_niet_natuurlijk_persoon,
    innp.rechtsvorm,
    innp.statutaire_zetel,
    innp_subject.kvk_nummer,
    p.ka_deelperceelnummer,
    p.ka_perceelnummer,
    p.ka_kad_gemeentecode,
    p.ka_sectie,
    p.begrenzing_perceel
FROM
    ((((((((kad_perceel p
JOIN
    zak_recht zr
ON
    ((
            zr.fk_7koz_kad_identif = p.sc_kad_identif)))
LEFT JOIN
    aard_recht_verkort ark
ON
    (((
                zr.fk_3avr_aand)::text = (ark.aand)::text)))
LEFT JOIN
    aard_verkregen_recht ar
ON
    (((
                zr.fk_3avr_aand)::text = (ar.aand)::text)))
LEFT JOIN
    nat_prs np
ON
    (((
                np.sc_identif)::text = (zr.fk_8pes_sc_identif)::text)))
LEFT JOIN
    ingeschr_nat_prs inp
ON
    (((
                inp.sc_identif)::text = (np.sc_identif)::text)))
LEFT JOIN
    niet_nat_prs nnp
ON
    (((
                nnp.sc_identif)::text = (zr.fk_8pes_sc_identif)::text)))
LEFT JOIN
    ingeschr_niet_nat_prs innp
ON
    (((
                innp.sc_identif)::text = (nnp.sc_identif)::text)))
LEFT JOIN
    subject innp_subject
ON
    (((
                innp_subject.identif)::text = (innp.sc_identif)::text)))
WHERE
    ((
            np.nm_geslachtsnaam IS NOT NULL)
    OR  ((
                nnp.naam IS NOT NULL)
        AND ((
                    zr.fk_3avr_aand)::text ~~ '2'::text)));

------------------------------------
-- v_kad_perceel_eigenaar_geen_erfpacht
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_perceel_eigenaar_geen_erfpacht
    (
        sc_kad_identif,
        zakelijk_recht_identificatie,
        aandeel_teller,
        aandeel_noemer,
        aard_recht_aand,
        zakelijk_recht_omschrijving,
        soort_eigenaar,
        geslachtsnaam,
        voorvoegsel,
        voornamen,
        geslacht,
        totaal_naam,
        persoon_identificatie,
        woonadres,
        geboortedatum,
        geboorteplaats,
        overlijdensdatum,
        naam_niet_natuurlijk_persoon,
        rechtsvorm,
        statutaire_zetel,
        kvk_nummer,
        ka_deelperceelnummer,
        aanduiding,
        ka_kad_gemeentecode,
        grootte_perceel,
        begrenzing_perceel
    ) AS
SELECT
    p.sc_kad_identif,
    v_kad_perceel_eigenaar.zakelijk_recht_identificatie,
    v_kad_perceel_eigenaar.aandeel_teller,
    v_kad_perceel_eigenaar.aandeel_noemer,
    v_kad_perceel_eigenaar.aard_recht_aand,
    v_kad_perceel_eigenaar.zakelijk_recht_omschrijving,
    v_kad_perceel_eigenaar.soort_eigenaar,
    v_kad_perceel_eigenaar.geslachtsnaam,
    v_kad_perceel_eigenaar.voorvoegsel,
    v_kad_perceel_eigenaar.voornamen,
    v_kad_perceel_eigenaar.geslacht,
    v_kad_perceel_eigenaar.totaal_naam,
    v_kad_perceel_eigenaar.persoon_identificatie,
    v_kad_perceel_eigenaar.woonadres,
    v_kad_perceel_eigenaar.geboortedatum,
    v_kad_perceel_eigenaar.geboorteplaats,
    v_kad_perceel_eigenaar.overlijdensdatum,
    v_kad_perceel_eigenaar.naam_niet_natuurlijk_persoon,
    v_kad_perceel_eigenaar.rechtsvorm,
    v_kad_perceel_eigenaar.statutaire_zetel,
    v_kad_perceel_eigenaar.kvk_nummer,
    v_kad_perceel_eigenaar.ka_deelperceelnummer,
    (((v_kad_perceel_eigenaar.ka_sectie)::text || ' '::text) ||
    (v_kad_perceel_eigenaar.ka_perceelnummer)::text) AS aanduiding,
    v_kad_perceel_eigenaar.ka_kad_gemeentecode,
    p.grootte_perceel,
    p.begrenzing_perceel
FROM
    (v_kad_perceel_eigenaar
JOIN
    kad_perceel p
ON
    ((
            p.sc_kad_identif = v_kad_perceel_eigenaar.kadaster_identificatie)))
WHERE
    ((((
                    v_kad_perceel_eigenaar.zakelijk_recht_omschrijving)::text ~~
                '%Eigendom (recht van)%'::text)
        AND ((
                    v_kad_perceel_eigenaar.totaal_naam)::text ~~ '%Gemeente Vlaardingen%'::text))
    AND (
            NOT (
                v_kad_perceel_eigenaar.kadaster_identificatie IN
                (
                    SELECT
                        v_kad_perceel_erfpacht.sc_kad_identif
                    FROM
                        v_kad_perceel_erfpacht))));

------------------------------------
-- v_kad_perceel_eigenaar_nnp
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_perceel_eigenaar_nnp
    (
        niet_nat_pers_identif,
        naam_niet_natuurlijk_persoon,
        rechtsvorm,
        statutaire_zetel,
        kvk_nummer,
        fk_7koz_kad_identif,
        fk_zr_kadaster_identif
    ) AS
SELECT
    nnp.sc_identif AS niet_nat_pers_identif,
    nnp.naam       AS naam_niet_natuurlijk_persoon,
    innp.rechtsvorm,
    innp.statutaire_zetel,
    innp_subject.kvk_nummer,
    zr.fk_7koz_kad_identif,
    zr.kadaster_identif AS fk_zr_kadaster_identif
FROM
    (((niet_nat_prs nnp
JOIN
    zak_recht zr
ON
    (((
                zr.fk_8pes_sc_identif)::text = (nnp.sc_identif)::text)))
LEFT JOIN
    ingeschr_niet_nat_prs innp
ON
    (((
                innp.sc_identif)::text = (nnp.sc_identif)::text)))
LEFT JOIN
    subject innp_subject
ON
    (((
                innp_subject.identif)::text = (innp.sc_identif)::text)));

------------------------------------
-- v_kad_perceel_eigenaar_np
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_perceel_eigenaar_np
    (
        nat_pers_identif,
        clazz,
        aand_naamgebruik,
        geslachtsaand,
        nm_adellijke_titel_predikaat,
        nm_geslachtsnaam,
        nm_voornamen,
        voorvoegsel,
        geboortedatum,
        adres,
        fk_zr_kadaster_identif,
        fk_7koz_kad_identif
    ) AS
SELECT
    np.sc_identif AS nat_pers_identif,
    np.clazz,
    np.aand_naamgebruik,
    np.geslachtsaand,
    np.nm_adellijke_titel_predikaat,
    np.nm_geslachtsnaam,
    np.nm_voornamen,
    np.nm_voorvoegsel_geslachtsnaam AS voorvoegsel,
    (TO_CHAR((to_date((inp.gb_geboortedatum)::text, 'YYYYMMDD'::text))::TIMESTAMP WITH TIME zone,
    'DD-MM-YYYY'::text))::CHARACTER VARYING(19) AS geboortedatum,
    inp.va_loc_beschrijving                     AS adres,
    zr.kadaster_identif                         AS fk_zr_kadaster_identif,
    zr.fk_7koz_kad_identif
FROM
    ((nat_prs np
JOIN
    zak_recht zr
ON
    (((
                zr.fk_8pes_sc_identif)::text = (np.sc_identif)::text)))
JOIN
    ingeschr_nat_prs inp
ON
    (((
                inp.sc_identif)::text = (np.sc_identif)::text)));

------------------------------------
-- v_kad_perceel_eneco
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_perceel_eneco
    (
        begrenzing_perceel,
        sc_kad_identif,
        aanduiding,
        grootte_perceel,
        ks_koopjaar,
        ks_bedrag,
        cu_aard_cultuur_onbebouwd,
        naam,
        rsin
    ) AS
SELECT
    p.begrenzing_perceel,
    p.sc_kad_identif,
    p.aanduiding,
    p.grootte_perceel,
    p.ks_koopjaar,
    p.ks_bedrag,
    p.cu_aard_cultuur_onbebouwd,
    nnprs.naam,
    prs_e.fk_prs_sc_identif AS rsin
FROM
    (((v_map_kad_perceel p
JOIN
    zak_recht zr
ON
    ((
            zr.fk_7koz_kad_identif = p.sc_kad_identif)))
JOIN
    prs_eneco prs_e
ON
    (((
                prs_e.fk_prs_sc_identif)::text = (zr.fk_8pes_sc_identif)::text)))
LEFT JOIN
    niet_nat_prs nnprs
ON
    (((
                nnprs.sc_identif)::text = (prs_e.fk_prs_sc_identif)::text)))
WHERE
    ((
            zr.fk_3avr_aand)::text = '2'::text);

------------------------------------
-- v_kad_perceel_eneco_overig_zak_recht
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_perceel_eneco_overig_zak_recht
    (
        sc_kad_identif,
        aanduiding,
        grootte_perceel,
        ks_koopjaar,
        ks_bedrag,
        cu_aard_cultuur_onbebouwd,
        naam,
        rsin,
        fk_3avr_aand,
        begrenzing_perceel
    ) AS
SELECT
    p.sc_kad_identif,
    p.aanduiding,
    p.grootte_perceel,
    p.ks_koopjaar,
    p.ks_bedrag,
    p.cu_aard_cultuur_onbebouwd,
    nnprs.naam,
    prs_e.fk_prs_sc_identif AS rsin,
    zr.fk_3avr_aand,
    p.begrenzing_perceel
FROM
    (((v_map_kad_perceel p
JOIN
    zak_recht zr
ON
    ((
            zr.fk_7koz_kad_identif = p.sc_kad_identif)))
JOIN
    prs_eneco prs_e
ON
    (((
                prs_e.fk_prs_sc_identif)::text = (zr.fk_8pes_sc_identif)::text)))
LEFT JOIN
    niet_nat_prs nnprs
ON
    (((
                nnprs.sc_identif)::text = (prs_e.fk_prs_sc_identif)::text)))
WHERE
    ((
            zr.fk_3avr_aand)::text = ANY (ARRAY[('3'::CHARACTER VARYING)::text, ('7'::CHARACTER
        VARYING)::text, ('13'::CHARACTER VARYING)::text, ('14'::CHARACTER VARYING)::text]));

------------------------------------
-- v_kad_perceel_erfpacht
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_perceel_erfpacht
    (
        begrenzing_perceel,
        sc_kad_identif,
        aanduiding,
        grootte_perceel,
        ks_koopjaar,
        ks_bedrag,
        cu_aard_cultuur_onbebouwd,
        naam,
        omschr
    ) AS
SELECT
    p.begrenzing_perceel,
    p.sc_kad_identif,
    p.aanduiding,
    p.grootte_perceel,
    p.ks_koopjaar,
    p.ks_bedrag,
    p.cu_aard_cultuur_onbebouwd,
    nnprs.naam,
    arv.omschr
FROM
    (((v_map_kad_perceel p
JOIN
    zak_recht zr
ON
    ((
            zr.fk_7koz_kad_identif = p.sc_kad_identif)))
JOIN
    aard_recht_verkort arv
ON
    (((
                zr.fk_3avr_aand)::text = (arv.aand)::text)))
LEFT JOIN
    niet_nat_prs nnprs
ON
    (((
                nnprs.sc_identif)::text = (zr.fk_8pes_sc_identif)::text)))
WHERE
    ((
            arv.aand)::text = (3)::text);

------------------------------------
-- v_kad_perceel_erfpacht_gemeente
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_perceel_erfpacht_gemeente
    (
        sc_kad_identif,
        aanduiding,
        grootte_perceel,
        ks_koopjaar,
        ks_bedrag,
        cu_aard_cultuur_onbebouwd,
        totaal_naam,
        omschr,
        begrenzing_perceel
    ) AS
SELECT DISTINCT
    p.sc_kad_identif,
    p.aanduiding,
    p.grootte_perceel,
    p.ks_koopjaar,
    p.ks_bedrag,
    p.cu_aard_cultuur_onbebouwd,
    eig.totaal_naam,
    arv.omschr,
    p.begrenzing_perceel
FROM
    (((v_map_kad_perceel p
JOIN
    v_kad_perceel_eigenaar eig
ON
    ((
            eig.kadaster_identificatie = p.sc_kad_identif)))
JOIN
    zak_recht zr
ON
    ((
            zr.fk_7koz_kad_identif = p.sc_kad_identif)))
JOIN
    aard_recht_verkort arv
ON
    (((
                zr.fk_3avr_aand)::text = (arv.aand)::text)))
WHERE
    (((
                arv.aand)::text = (3)::text)
    AND ((
                eig.totaal_naam)::text ~~* 'Gemeente Vlaardingen%'::text));

------------------------------------
-- v_kad_perceel_erfpacht_test
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_perceel_erfpacht_test
    (
        sc_kad_identif,
        aanduiding,
        grootte_perceel,
        ks_koopjaar,
        ks_bedrag,
        cu_aard_cultuur_onbebouwd,
        omschr,
        begrenzing_perceel
    ) AS
SELECT
    p.sc_kad_identif,
    p.aanduiding,
    p.grootte_perceel,
    p.ks_koopjaar,
    p.ks_bedrag,
    p.cu_aard_cultuur_onbebouwd,
    arv.omschr,
    p.begrenzing_perceel
FROM
    ((v_map_kad_perceel p
JOIN
    zak_recht zr
ON
    ((
            zr.fk_7koz_kad_identif = p.sc_kad_identif)))
JOIN
    aard_recht_verkort arv
ON
    (((
                zr.fk_3avr_aand)::text = (arv.aand)::text)))
WHERE
    ((
            arv.aand)::text = (3)::text);

------------------------------------
-- v_kad_perceel_opzoeklijst_eigenaar
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_perceel_opzoeklijst_eigenaar
    (
        totaal_naam
    ) AS
SELECT DISTINCT
    v_kad_perceel_eigenaar.totaal_naam
FROM
    v_kad_perceel_eigenaar
ORDER BY
    v_kad_perceel_eigenaar.totaal_naam;

------------------------------------
-- v_kad_perceel_sectie
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_perceel_sectie
    (
        ka_sectie
    ) AS
SELECT DISTINCT
    k.ka_sectie
FROM
    kad_perceel k
GROUP BY
    k.ka_sectie;

------------------------------------
-- v_kad_perceel_woningcorps
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_perceel_woningcorps
    (
        begrenzing_perceel,
        sc_kad_identif,
        aanduiding,
        grootte_perceel,
        ks_koopjaar,
        ks_bedrag,
        cu_aard_cultuur_onbebouwd,
        naam,
        rsin
    ) AS
SELECT
    p.begrenzing_perceel,
    p.sc_kad_identif,
    p.aanduiding,
    p.grootte_perceel,
    p.ks_koopjaar,
    p.ks_bedrag,
    p.cu_aard_cultuur_onbebouwd,
    nnprs.naam,
    prs_e.fk_prs_sc_identif AS rsin
FROM
    (((v_map_kad_perceel p
JOIN
    zak_recht zr
ON
    ((
            zr.fk_7koz_kad_identif = p.sc_kad_identif)))
JOIN
    prs_woningcorporatie prs_e
ON
    (((
                prs_e.fk_prs_sc_identif)::text = (zr.fk_8pes_sc_identif)::text)))
LEFT JOIN
    niet_nat_prs nnprs
ON
    (((
                nnprs.sc_identif)::text = (prs_e.fk_prs_sc_identif)::text)))
WHERE
    ((
            zr.fk_3avr_aand)::text = '2'::text);

------------------------------------
-- v_kad_perceel_woningcorps_erfpacht
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_perceel_woningcorps_erfpacht
    (
        sc_kad_identif,
        aanduiding,
        grootte_perceel,
        ks_koopjaar,
        ks_bedrag,
        cu_aard_cultuur_onbebouwd,
        naam,
        rsin,
        fk_3avr_aand,
        begrenzing_perceel
    ) AS
SELECT
    p.sc_kad_identif,
    p.aanduiding,
    p.grootte_perceel,
    p.ks_koopjaar,
    p.ks_bedrag,
    p.cu_aard_cultuur_onbebouwd,
    nnprs.naam,
    prs_e.fk_prs_sc_identif AS rsin,
    zr.fk_3avr_aand,
    p.begrenzing_perceel
FROM
    (((v_map_kad_perceel p
JOIN
    zak_recht zr
ON
    ((
            zr.fk_7koz_kad_identif = p.sc_kad_identif)))
JOIN
    prs_woningcorporatie prs_e
ON
    (((
                prs_e.fk_prs_sc_identif)::text = (zr.fk_8pes_sc_identif)::text)))
LEFT JOIN
    niet_nat_prs nnprs
ON
    (((
                nnprs.sc_identif)::text = (prs_e.fk_prs_sc_identif)::text)))
WHERE
    ((
            zr.fk_3avr_aand)::text = '3'::text);

------------------------------------
-- v_kad_perceel_zak_recht_vld
------------------------------------

-- View: v_kad_perceel_zak_recht_vld

-- DROP VIEW v_kad_perceel_zak_recht_vld;

CREATE OR REPLACE VIEW v_kad_perceel_zak_recht_vld AS 
 SELECT p.sc_kad_identif AS koz_kad_identif,
    zr.kadaster_identif AS zr_kad_identificatie,
    zr.ar_teller AS aandeel_teller,
    zr.ar_noemer AS aandeel_noemer,
    zr.fk_3avr_aand AS aard_recht_aand,
        CASE
            WHEN np.sc_identif IS NOT NULL THEN 'Natuurlijk persoon'::text
            ELSE 'Niet natuurlijk persoon'::text
        END AS soort_eigenaar,
    np.nm_geslachtsnaam AS geslachtsnaam,
    np.nm_voorvoegsel_geslachtsnaam AS voorvoegsel,
    np.nm_voornamen AS voornamen,
    np.geslachtsaand AS geslacht,
    inp.va_loc_beschrijving AS woonadres,
    inp.gb_geboortedatum AS geboortedatum,
    inp.gb_geboorteplaats AS geboorteplaats,
    inp.ol_overlijdensdatum AS overlijdensdatum,
    nnp.naam AS naam_niet_natuurlijk_persoon,
    innp.rechtsvorm,
    innp.statutaire_zetel,
    innp_subject.kvk_nummer,
    ark.omschr AS zak_recht_omschrijving
   FROM kad_perceel p
     JOIN zak_recht zr ON zr.fk_7koz_kad_identif = p.sc_kad_identif
     LEFT JOIN aard_recht_verkort ark ON zr.fk_3avr_aand::text = ark.aand::text
     LEFT JOIN aard_verkregen_recht ar ON zr.fk_3avr_aand::text = ar.aand::text
     LEFT JOIN nat_prs np ON np.sc_identif::text = zr.fk_8pes_sc_identif::text
     LEFT JOIN ingeschr_nat_prs inp ON inp.sc_identif::text = np.sc_identif::text
     LEFT JOIN niet_nat_prs nnp ON nnp.sc_identif::text = zr.fk_8pes_sc_identif::text
     LEFT JOIN ingeschr_niet_nat_prs innp ON innp.sc_identif::text = nnp.sc_identif::text
     LEFT JOIN subject innp_subject ON innp_subject.identif::text = innp.sc_identif::text
  WHERE np.nm_geslachtsnaam IS NOT NULL OR nnp.naam IS NOT NULL;


------------------------------------
-- v_kad_percelen_historische_relaties
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_percelen_historische_relaties
    (
        sc_kad_identif,
        root
    ) AS
WITH
    RECURSIVE historie
    (
        sc_kad_identif,
        root
    ) AS
    (
        SELECT
            kp.sc_kad_identif,
            kp.sc_kad_identif
        FROM
            kad_perceel kp
        WHERE
            ((
                    kp.begrenzing_perceel IS NULL)
            AND (
                    EXISTS
                    (
                        SELECT
                            1
                        FROM
                            kad_onrrnd_zk_his_rel
                        WHERE
                            (
                                kad_onrrnd_zk_his_rel.fk_sc_lh_koz_kad_identif = kp.sc_kad_identif)
                    )))
        UNION
        SELECT
            child.fk_sc_rh_koz_kad_identif,
            parent.root
        FROM
            kad_onrrnd_zk_his_rel child,
            historie parent
        WHERE
            (
                parent.sc_kad_identif = child.fk_sc_lh_koz_kad_identif)
    )
SELECT
    historie.sc_kad_identif,
    historie.root
FROM
    historie;

------------------------------------
-- v_kad_stukdelen_zr
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_stukdelen_zr
    (
        tabel,
        tabel_identificatie,
        identificatie,
        gemeente,
        omschrijving,
        datum,
        ref_id
    ) AS
SELECT
    brondocument.tabel,
    brondocument.tabel_identificatie,
    brondocument.identificatie,
    brondocument.gemeente,
    brondocument.omschrijving,
    brondocument.datum,
    brondocument.ref_id
FROM
    brondocument
WHERE
    ((
            brondocument.tabel)::text ~~ '%BRONDOCUMENT%'::text);

------------------------------------
-- v_kad_stukken_zr
------------------------------------
CREATE OR REPLACE VIEW
    v_kad_stukken_zr
    (
        tabel,
        tabel_identificatie,
        identificatie,
        gemeente,
        omschrijving,
        datum,
        ref_id
    ) AS
SELECT
    brondocument.tabel,
    brondocument.tabel_identificatie,
    brondocument.identificatie,
    brondocument.gemeente,
    brondocument.omschrijving,
    brondocument.datum,
    brondocument.ref_id
FROM
    brondocument
WHERE
    ((
            brondocument.tabel)::text ~~ '%KAD%'::text);
            
------------------------------------
-- v_map_kad_perceel_vld
------------------------------------

-- View: v_map_kad_perceel_vld

-- DROP VIEW v_map_kad_perceel_vld;

CREATE OR REPLACE VIEW v_map_kad_perceel_vld AS 
 SELECT p.sc_kad_identif,
    p.begrenzing_perceel,
    (p.ka_sectie::text || ' '::text) || p.ka_perceelnummer::text AS aanduiding,
    p.grootte_perceel,
    z.ks_koopjaar,
    z.ks_bedrag,
    z.ks_meer_onroerendgoed,
    z.cu_aard_cultuur_onbebouwd
   FROM kad_perceel p
     JOIN kad_onrrnd_zk z ON z.kad_identif = p.sc_kad_identif;

------------------------------------
-- v_zak_recht_aantek
------------------------------------
                    
CREATE OR REPLACE VIEW
    v_zak_recht_aantek
    (
        kadaster_identif_aantek_recht,
        aard_aantek_recht,
        begindatum_aantek_recht,
        beschrijving_aantek_recht,
        eindd_aantek_recht,
        fk_5zkr_kadaster_identif,
        fk_6pes_sc_identif,
        kadaster_identificatie,
        zakelijk_recht_omschrijving
    ) AS
SELECT
    zra.kadaster_identif_aantek_recht,
    zra.aard_aantek_recht,
    zra.begindatum_aantek_recht,
    zra.beschrijving_aantek_recht,
    zra.eindd_aantek_recht,
    zra.fk_5zkr_kadaster_identif,
    zra.fk_6pes_sc_identif,
    zr.fk_7koz_kad_identif AS kadaster_identificatie,
    ark.omschr             AS zakelijk_recht_omschrijving
FROM
    ((zak_recht_aantek zra
JOIN
    zak_recht zr
ON
    (((
                zr.kadaster_identif)::text = (zra.fk_5zkr_kadaster_identif)::text)))
LEFT JOIN
    aard_recht_verkort ark
ON
    (((
                zr.fk_3avr_aand)::text = (ark.aand)::text)));
                

