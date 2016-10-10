--
-- upgrade RSGB datamodel van 1.3.6 naar 1.4.0 (MS SQLserver)
--
-- merge van de nieuwe waarden voor Aard Recht codelijst (issue#234)
MERGE INTO aard_recht_verkort t USING (
    VALUES 
        ('23','Opstalrecht Nutsvoorzieningen op gedeelte van perceel'),
        ('24','Zakelijk recht (als bedoeld in artikel 5, lid 3, onder b)')
    ) AS src (code,txt) ON t.aand = src.code
WHEN MATCHED THEN UPDATE SET omschr = src.txt
WHEN NOT MATCHED THEN INSERT (aand,omschr) VALUES (src.code, src.txt);

MERGE INTO aard_verkregen_recht t USING (
    VALUES 
        ('23','Opstalrecht Nutsvoorzieningen op gedeelte van perceel'),
        ('24','Zakelijk recht als bedoeld in artikel 5, lid 3, onder b, van de Belemmeringenwet Privaatrecht op gedeelte van perceel')
    ) AS src (code,txt) ON t.aand = src.code
WHEN MATCHED THEN UPDATE SET omschr_aard_verkregenr_recht = src.txt
WHEN NOT MATCHED THEN INSERT (aand,omschr_aard_verkregenr_recht) VALUES (src.code, src.txt);

-- toevoegen van een ObjectID aan kadaster views ten behoeve van arcgis

ALTER VIEW v_bd_app_re_bij_perceel AS 
 SELECT 
    CAST(ROW_NUMBER() over(ORDER BY ar.sc_kad_identif) AS INT) AS ObjectID,
    ar.sc_kad_identif,
    ar.fk_2nnp_sc_identif,
    ar.ka_appartementsindex,
    ar.ka_kad_gemeentecode,
    ar.ka_perceelnummer,
    ar.ka_sectie,
    kp.begrenzing_perceel
   FROM v_bd_app_re_all_kad_perceel v
     JOIN kad_perceel kp ON v.perceel_identif = kp.sc_kad_identif
     JOIN app_re ar ON v.app_re_identif = ar.sc_kad_identif;

ALTER VIEW v_map_kad_perceel as
select
    CAST(ROW_NUMBER() over(ORDER BY p.sc_kad_identif) AS INT) AS ObjectID,
    p.sc_kad_identif,
    p.begrenzing_perceel,
    p.ka_sectie + ' ' + p.ka_perceelnummer AS aanduiding,
    p.grootte_perceel,
    z.ks_koopjaar,
    z.ks_bedrag,
    z.cu_aard_cultuur_onbebouwd
from kad_perceel p
join kad_onrrnd_zk z on (z.kad_identif = p.sc_kad_identif);

ALTER view v_kad_perceel_in_eigendom as
select 
    CAST(ROW_NUMBER() over(ORDER BY p.sc_kad_identif) AS INT) AS ObjectID,
    p.begrenzing_perceel,
    p.sc_kad_identif,
    p.aanduiding,
    p.grootte_perceel,
    p.ks_koopjaar,
    p.ks_bedrag,
    p.cu_aard_cultuur_onbebouwd,
    nnprs.naam
from v_map_kad_perceel p
join zak_recht zr on (zr.fk_7koz_kad_identif = p.sc_kad_identif)
join prs_eigendom prs_e on (prs_e.fk_prs_sc_identif = zr.fk_8pes_sc_identif)
left join niet_nat_prs nnprs on (nnprs.sc_identif = prs_e.fk_prs_sc_identif);

ALTER view v_kad_perceel_eenvoudig as
select
        CAST(ROW_NUMBER() over(ORDER BY p.sc_kad_identif) AS INT) AS ObjectID,
        p.sc_kad_identif,
        p.begrenzing_perceel,
        p.ka_sectie + ' ' + p.ka_perceelnummer AS aanduiding,
        p.grootte_perceel,   
        p_adr.kad_bag_koppeling_benobj,             
        p_adr.straat,
        p_adr.huisnummer,
        p_adr.huisletter,
        p_adr.toevoeging,
        p_adr.postcode,
        p_adr.woonplaats
from kad_perceel p
join v_kad_perceel_adres p_adr on (p_adr.sc_kad_identif = p.sc_kad_identif);

ALTER view v_kad_perceel_zr_adressen as 
select 
  CAST(ROW_NUMBER() over(ORDER BY kp.sc_kad_identif) AS INT) AS ObjectID,
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
from v_kad_perceel_eenvoudig kp
join v_kad_perceel_zak_recht zr on (zr.KADASTER_IDENTIFICATIE = kp.sc_kad_identif);


-- Eigenarenkaart - percelen en appartementen met hun eigenaren
ALTER VIEW
    v_kad_eigenarenkaart
    (
        ObjectID,
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
    CAST(row_number() OVER (order by p.kadaster_identificatie) AS INT) AS ObjectID,
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
        THEN np.NM_GESLACHTSNAAM + ', ' + np.NM_VOORNAMEN + ' ' + np.NM_VOORVOEGSEL_GESLACHTSNAAM
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
   
