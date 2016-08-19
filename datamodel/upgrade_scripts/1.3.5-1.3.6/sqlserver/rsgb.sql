-- upgrade RSGB datamodel van 1.3.5 naar 1.3.6 (MS SQLserver)

-- vergroten van het veld 'omschrijving' in de tabel brondocument van 40 naar 255 characters
ALTER TABLE
    brondocument
ALTER COLUMN
    omschrijving VARCHAR(255);

-- aankoopdatum uit brondocumenten
CREATE VIEW
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
    ) b;


-- aanpassen eigenarenkaart
DROP VIEW v_kad_eigenarenkaart;
-- Eigenarenkaart - percelen en appartementen met hun eigenaren
CREATE VIEW
    v_kad_eigenarenkaart
    (
        objectid,
        kadaster_identificatie,
        type,
        zakelijk_recht_identificatie,
        aandeel_teller,
        aandeel_noemer,
        aard_recht_aand,
        zakelijk_recht_omschrijving,
        aankoopdatum,
        soort_eigenaar,
        geslachtsnaam,
        voorvoegsel,
        voornamen,
        geslacht,
        perceel_zak_recht_naam,
        persoon_identificatie,
        woonadres,
        geboortedatum,
        geboorteplaats,
        overlijdensdatum,
        naam_niet_natuurlijk_persoon,
        rechtsvorm,
        statutaire_zetel,
        kvk_nummer,
        ka_appartementsindex,
        ka_deelperceelnummer,
        ka_perceelnummer,
        ka_kad_gemeentecode,
        ka_sectie,
        begrenzing_perceel
    ) AS
SELECT
    row_number() OVER (order by p.kadaster_identificatie) AS objectid,
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
        THEN np.NM_GESLACHTSNAAM + ', ' + np.NM_VOORNAMEN + ' ' +
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
    b.kadaster_identificatie = p.kadaster_identificatie
WHERE
    zr.kadaster_identif like 'NL.KAD.Tenaamstelling%';

