--
create view v_map_kad_perceel as
select
    CAST(ROWNUM AS INTEGER) AS objectid,
    p.sc_kad_identif,
    p.begrenzing_perceel,
    p.ka_sectie || ' ' || p.ka_perceelnummer AS aanduiding,
    p.grootte_perceel,
    z.ks_koopjaar,
    z.ks_bedrag,
    z.cu_aard_cultuur_onbebouwd
from kad_perceel p
join kad_onrrnd_zk z on (z.kad_identif = p.sc_kad_identif);

create table prs_eigendom (
    fk_prs_sc_identif varchar(32),
    primary key (fk_prs_sc_identif),
    foreign key (fk_prs_sc_identif) references prs(sc_identif)
);

create or replace view v_kad_perceel_in_eigendom as
select
    CAST(ROWNUM AS INTEGER) AS objectid,
    p.begrenzing_perceel,
    p.sc_kad_identif,
    p.aanduiding,
    p.grootte_perceel,
    p.ks_koopjaar,
    p.ks_bedrag,
    p.cu_aard_cultuur_onbebouwd,
    nnprs.naam
    -- rownum as wtf -- Anders Oracle ORA-13276 SRID 0 not found.
from v_map_kad_perceel p
join zak_recht zr on (zr.fk_7koz_kad_identif = p.sc_kad_identif)
join prs_eigendom prs_e on (prs_e.fk_prs_sc_identif = zr.fk_8pes_sc_identif)
left join niet_nat_prs nnprs on (nnprs.sc_identif = prs_e.fk_prs_sc_identif)
where p.begrenzing_perceel.sdo_srid is not null;

create or replace view v_kad_perceel_adres as
select distinct
        kp.sc_kad_identif,
        kpvbo.FK_NN_LH_TGO_IDENTIF as kad_bag_koppeling_benobj,
        gor.naam_openb_rmte as straat,
        aoa.huinummer as huisnummer,
        aoa.huisletter,
        aoa.huinummertoevoeging as toevoeging,
        aoa.postcode,
        wp.naam as woonplaats
from kad_perceel kp
join benoemd_obj_kad_onrrnd_zk kpvbo on (kpvbo.FK_NN_RH_KOZ_KAD_IDENTIF = kp.SC_KAD_IDENTIF)
left join verblijfsobj vbo on (vbo.SC_IDENTIF = kpvbo.FK_NN_LH_TGO_IDENTIF)
left join nummeraand na on (na.SC_IDENTIF = vbo.FK_11NRA_SC_IDENTIF)
left join addresseerb_obj_aand aoa on (aoa.IDENTIF = na.SC_IDENTIF)
left join gem_openb_rmte gor on (gor.IDENTIFCODE = aoa.FK_7OPR_IDENTIFCODE)
left join openb_rmte_wnplts oprw on (oprw.FK_NN_LH_OPR_IDENTIFCODE = gor.IDENTIFCODE)
left join wnplts wp on (wp.IDENTIF = oprw.FK_NN_RH_WPL_IDENTIF);

create or replace view v_kad_perceel_eenvoudig as
select
        CAST(ROWNUM AS INTEGER) AS objectid,
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
from kad_perceel p
join v_kad_perceel_adres p_adr on (p_adr.sc_kad_identif = p.sc_kad_identif);

create or replace view v_kad_perceel_zak_recht as
  select
    p.sc_kad_identif as Kadaster_identificatie,
    zr.AR_TELLER  as Aandeel_teller,
    zr.AR_NOEMER as Aandeel_noemer,
    zr.FK_3AVR_AAND as Aard_recht_aand,
--    ark.omschr as Aard_recht_omschrijving_verkort, XXX referentielijst niet gevuld
--    ar.omschr_aard_verkregenr_recht as Aard_recht_omschrijving, XXX referentielijst niet gevuld
    case when np.sc_identif is not null
    then 'Natuurlijk persoon'
    else 'Niet natuurlijk persoon'
    end as soort_eigenaar,
    np.NM_GESLACHTSNAAM as Geslachtsnaam,
    np.NM_VOORVOEGSEL_GESLACHTSNAAM as Voorvoegsel,
    np.NM_VOORNAMEN as Voornamen,
    np.GESLACHTSAAND as Geslacht,
    inp.VA_LOC_BESCHRIJVING as Woonadres,
    inp.GB_GEBOORTEDATUM as Geboortedatum,
--    inp.GB_GEBOORTELAND as Code_geboorteland, XXX in XSL conversie naar 2-letterige ISO code
    inp.GB_GEBOORTEPLAATS as Geboorteplaats,
    inp.OL_OVERLIJDENSDATUM as Overlijdensdatum,
    nnp.NAAM as Naam_niet_natuurlijk_persoon,
    innp.RECHTSVORM as Rechtsvorm,
    innp.STATUTAIRE_ZETEL as Statutaire_zetel,
    innp_subject.kvk_nummer
  from kad_perceel p
  join zak_recht zr on (zr.FK_7KOZ_KAD_IDENTIF = p.sc_kad_identif)
  left join aard_recht_verkort ark on (zr.FK_3AVR_AAND = ark.AAND)
  left join aard_verkregen_recht ar on (zr.FK_3AVR_AAND = ar.AAND)
  left join nat_prs np on (np.SC_IDENTIF = zr.FK_8PES_SC_IDENTIF)
  left join ingeschr_nat_prs inp on (inp.SC_IDENTIF = np.SC_IDENTIF)
  left join niet_nat_prs nnp on (nnp.sc_identif = zr.FK_8PES_SC_IDENTIF)
  left join ingeschr_niet_nat_prs innp on (innp.SC_IDENTIF = nnp.sc_identif)
  left join subject innp_subject on (innp_subject.identif = innp.sc_identif)
  where np.NM_GESLACHTSNAAM is not null or nnp.NAAM is not null;

create or replace view v_kad_perceel_zr_adressen as
select
  CAST(ROWNUM AS INTEGER) AS objectid,
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

create or replace view v_kad_perceel_app_rechten as
select
 kpe.SC_KAD_IDENTIF as perceel_identificatie,
-- kpe.KA_SECTIE || ' ' || kpe.KA_PERCEELNUMMER as perceelnr,
 kpe.aanduiding,
 kpe.straat, kpe.huisnummer, kpe.toevoeging, kpe.huisletter,
 kpe.straat || ' ' || kpe.huisnummer || ' ' || kpe.huisletter || ' ' || kpe.toevoeging || ' ' || kpe.postcode as adres,
-- zr.kadaster_identif as links_zak_recht,
 zr.FK_3AVR_AAND as complex_zak_recht_aard_aand,
-- zr.FK2_PERSOON as links_zak_recht_persoon,
--    case when np1.PK_PERSOON is not null then 'Natuurlijk persoon' else 'Niet natuurlijk persoon' end as l_soort_eigenaar,
    CASE
      WHEN np1.sc_identif IS NOT NULL
      THEN np1.NM_GESLACHTSNAAM || ', ' || np1.NM_VOORNAMEN || ' ' || np1.NM_VOORVOEGSEL_GESLACHTSNAAM
      ELSE nnp1.NAAM
    END AS perceel_zak_recht_naam,
--    nnp1.NAAM as l_nnp,
-- bd1.identificatie as brondocument,
-- zr2.kadaster_identif as rechts_zak_recht,
 zr2.FK_3AVR_AAND as app_re_zak_recht_aard_aand,
-- zr2.FK2_PERSOON as rechts_zak_recht_persoon,
--    case when np2.PK_PERSOON is not null then 'Natuurlijk persoon' else 'Niet natuurlijk persoon' end as r_soort_eigenaar,
    CASE
      WHEN np2.sc_identif IS NOT NULL
      THEN np2.NM_GESLACHTSNAAM || ', ' || np2.NM_VOORNAMEN || ' ' || np2.NM_VOORVOEGSEL_GESLACHTSNAAM
      ELSE nnp2.NAAM
    END AS app_re_zak_recht_naam,
--    nnp2.NAAM as r_nnp,
ar.SC_KAD_IDENTIF as app_re_identificatie,
 to_number(ar.KA_APPARTEMENTSINDEX) as appartementsindex --,
-- ar.FK1_NIET_NAT_PERSOON as app_re_vve,
-- ar_vve_nnp.naam as app_re_vve_naam,
-- ar_vve_innp.rechtsvorm as app_re_vve_rechtsvorm,
-- ar_vve_innp.rsin as app_re_vve_rsin
from v_kad_perceel_eenvoudig kpe
join zak_recht zr on (zr.FK_7KOZ_KAD_IDENTIF = kpe.SC_KAD_IDENTIF)
  left join nat_prs np1 on (np1.SC_IDENTIF = zr.FK_8PES_SC_IDENTIF)
  left join ingeschr_nat_prs inp1 on (inp1.SC_IDENTIF = np1.SC_IDENTIF)
  left join niet_nat_prs nnp1 on (nnp1.sc_identif = zr.FK_8PES_SC_IDENTIF)
  left join ingeschr_niet_nat_prs innp1 on (innp1.sc_identif = nnp1.sc_identif)
join brondocument bd1 on (bd1.tabel = 'ZAK_RECHT' and bd1.tabel_identificatie = zr.kadaster_identif)
join brondocument bd2 on (bd2.tabel = 'ZAK_RECHT' and bd2.tabel_identificatie <> zr.kadaster_identif and bd2.identificatie = bd1.identificatie)
join zak_recht zr2 on (zr2.kadaster_identif = bd2.tabel_identificatie)
  left join nat_prs np2 on (np2.SC_IDENTIF = zr2.FK_8PES_SC_IDENTIF)
  left join ingeschr_nat_prs inp2 on (inp2.SC_IDENTIF = np2.SC_IDENTIF)
  left join niet_nat_prs nnp2 on (nnp2.sc_identif = zr2.FK_8PES_SC_IDENTIF)
  left join ingeschr_niet_nat_prs innp2 on (innp2.sc_identif = nnp2.sc_identif)
join app_re ar on (ar.SC_KAD_IDENTIF = zr2.FK_7KOZ_KAD_IDENTIF)
join niet_nat_prs ar_vve_nnp on (ar_vve_nnp.sc_identif = ar.FK_2NNP_SC_IDENTIF)
join INGESCHR_NIET_NAT_PRS ar_vve_innp on (ar_vve_innp.sc_identif = ar_vve_nnp.sc_identif)
where bd1.omschrijving like 'betrokkenBij%'
and zr2.FK_8PES_SC_IDENTIF is not null
order by kpe.SC_KAD_IDENTIF, kpe.straat, kpe.huisnummer, kpe.toevoeging, kpe.huisletter,  to_number(KA_APPARTEMENTSINDEX);

-- percelen plus appartementen op de percelen
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

-- aankoopdatum uit brondocumenten
CREATE OR REPLACE VIEW
    V_AANKOOPDATUM
    (
        KADASTER_IDENTIFICATIE,
        AANKOOPDATUM
    ) AS
SELECT
    b.ref_id AS KADASTER_IDENTIFICATIE,
    b.datum  AS AANKOOPDATUM
FROM
    (
        SELECT
            ref_id ,
            datum ,
            row_number() over (partition BY ref_id ORDER BY datum DESC) AS rnk
        FROM
            brondocument
        WHERE
            omschrijving = 'Akte van Koop en Verkoop' ) b
WHERE
    b.rnk = 1;

-- Eigenarenkaart - percelen en appartementen met hun eigenaren
CREATE MATERIALIZED VIEW VM_KAD_EIGENARENKAART
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
    ) BUILD IMMEDIATE AS
SELECT
    CAST(ROWNUM AS INTEGER) AS objectid,
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
    b.kadaster_identificatie = p.kadaster_identificatie
WHERE
    zr.kadaster_identif like 'NL.KAD.T%';

CREATE UNIQUE INDEX VM_KAD_EIGENARENKAART_OID_IDX ON VM_KAD_EIGENARENKAART (OBJECTID ASC);
INSERT INTO USER_SDO_GEOM_METADATA VALUES('VM_KAD_EIGENARENKAART', 'BEGRENZING_PERCEEL', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
CREATE INDEX VM_KAD_EIGENARENKAART_PERC_IDX ON VM_KAD_EIGENARENKAART (BEGRENZING_PERCEEL) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');

-- appartementsrecht aan bag adres
CREATE OR REPLACE VIEW v_app_re_adres AS
  SELECT DISTINCT
    kp.sc_kad_identif,
    kpvbo.FK_NN_LH_TGO_IDENTIF AS kad_bag_koppeling_benobj,
    gor.naam_openb_rmte AS straat,
    aoa.huinummer AS huisnummer,
    aoa.huisletter,
    aoa.huinummertoevoeging AS toevoeging,
    aoa.postcode,
    wp.naam AS woonplaats
  FROM app_re kp
    JOIN benoemd_obj_kad_onrrnd_zk kpvbo ON (kpvbo.FK_NN_RH_KOZ_KAD_IDENTIF = kp.SC_KAD_IDENTIF)
    LEFT JOIN verblijfsobj vbo ON (vbo.SC_IDENTIF = kpvbo.FK_NN_LH_TGO_IDENTIF)
    LEFT JOIN nummeraand na ON (na.SC_IDENTIF = vbo.FK_11NRA_SC_IDENTIF)
    LEFT JOIN addresseerb_obj_aand aoa ON (aoa.IDENTIF = na.SC_IDENTIF)
    LEFT JOIN gem_openb_rmte gor ON (gor.IDENTIFCODE = aoa.FK_7OPR_IDENTIFCODE)
    LEFT JOIN openb_rmte_wnplts oprw ON (oprw.FK_NN_LH_OPR_IDENTIFCODE = gor.IDENTIFCODE)
    LEFT JOIN wnplts wp ON (wp.IDENTIF = oprw.FK_NN_RH_WPL_IDENTIF);

COMMENT ON VIEW v_app_re_adres IS 'appartementsrecht met bag adres';

-- alle kad_onrrnd_zk gekoppeld aan bag adres
CREATE OR REPLACE VIEW v_kad_onrrd_zk_adres AS
  SELECT DISTINCT
    kp.kad_identif,
    kpvbo.fk_nn_lh_tgo_identif AS kad_bag_koppeling_benobj,
    gor.naam_openb_rmte AS straat,
    aoa.huinummer AS huisnummer,
    aoa.huisletter,
    aoa.huinummertoevoeging AS toevoeging,
    aoa.postcode,
    wp.naam AS woonplaats
  FROM kad_onrrnd_zk kp
    JOIN benoemd_obj_kad_onrrnd_zk kpvbo ON (kpvbo.FK_NN_RH_KOZ_KAD_IDENTIF = kp.KAD_IDENTIF)
    LEFT JOIN verblijfsobj vbo ON (vbo.SC_IDENTIF = kpvbo.FK_NN_LH_TGO_IDENTIF)
    LEFT JOIN nummeraand na ON (na.SC_IDENTIF = vbo.FK_11NRA_SC_IDENTIF)
    LEFT JOIN addresseerb_obj_aand aoa ON (aoa.IDENTIF = na.SC_IDENTIF)
    LEFT JOIN gem_openb_rmte gor ON (gor.IDENTIFCODE = aoa.FK_7OPR_IDENTIFCODE)
    LEFT JOIN openb_rmte_wnplts oprw ON (oprw.FK_NN_LH_OPR_IDENTIFCODE = gor.IDENTIFCODE)
    LEFT JOIN wnplts wp ON (wp.IDENTIF = oprw.FK_NN_RH_WPL_IDENTIF);

COMMENT ON VIEW v_kad_onrrd_zk_adres IS 'onroerende zaak met bag adres';
