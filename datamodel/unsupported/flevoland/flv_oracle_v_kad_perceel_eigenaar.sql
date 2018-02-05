--Aangepaste versie, die ook niet-natuurlijke personen als eigenaar meeneemt.
--- Door: Niek Bouma/Kees-Jan Lammers, 14-03-2016

CREATE VIEW
    V_KAD_PERCEEL_EIGENAAR
    (
        KADASTER_IDENTIFICATIE,
        ZAKELIJK_RECHT_IDENTIFICATIE,
        AANDEEL_TELLER,
        AANDEEL_NOEMER,
        AARD_RECHT_AAND,
        ZAKELIJK_RECHT_OMSCHRIJVING,
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
        KA_DEELPERCEELNUMMER,
        KA_PERCEELNUMMER,
        KA_KAD_GEMEENTECODE,
        KA_SECTIE,
        BEGRENZING_PERCEEL
    ) AS
SELECT
    p.sc_kad_identif    AS kadaster_identificatie,
    zr.kadaster_identif AS zakelijk_recht_identificatie,
    zr.ar_teller        AS aandeel_teller,
    zr.ar_noemer        AS aandeel_noemer,
    zr.fk_3avr_aand     AS aard_recht_aand,
    ark.omschr          AS zakelijk_recht_omschrijving,
    CASE
        WHEN np.sc_identif IS NOT NULL
        THEN 'Natuurlijk persoon'
        ELSE 'Niet natuurlijk persoon'
    END                             AS soort_eigenaar,
    np.nm_geslachtsnaam             AS geslachtsnaam,
    np.nm_voorvoegsel_geslachtsnaam AS voorvoegsel,
    np.nm_voornamen                 AS voornamen,
    np.geslachtsaand                AS geslacht,
    CASE
        WHEN np.sc_identif IS NOT NULL
        THEN np.NM_GESLACHTSNAAM || ', ' || np.NM_VOORNAMEN || ' ' ||
            np.NM_VOORVOEGSEL_GESLACHTSNAAM
        ELSE nnp.NAAM
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
    p.ka_deelperceelnummer,
    p.ka_perceelnummer,
    p.ka_kad_gemeentecode,
    p.ka_sectie,
    p.begrenzing_perceel
FROM
    kad_perceel p
JOIN
    zak_recht zr
ON
    zr.fk_7koz_kad_identif = p.sc_kad_identif
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
WHERE
    np.nm_geslachtsnaam IS NOT NULL
OR  nnp.naam IS NOT NULL;
