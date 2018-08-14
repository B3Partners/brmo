--
-- upgrade PostgreSQL RSGB datamodel van 1.5.2 naar 1.6.0
--

-- 118

alter table subject drop constraint fk_sub_vb_4;

alter table ingeschr_nat_prs drop constraint fk_inp_gb_2;
alter table ingeschr_nat_prs drop constraint fk_inp_ol_1;
alter table ingeschr_nat_prs drop constraint fk_inp_rl_17;
alter table ingeschr_nat_prs drop constraint fk_inp_rl_18;

-- 119
insert into rsdocsoort (rsdoccode, rsdocomschr) values
('1','paspoort'),
('2','Europese identiteitskaart'),
('3','toeristenkaart'),
('4','gemeentelijke identiteitskaart'),
('5','verblijfsdocument van de Vreemdelingendienst'),
('6','vluchtelingenpaspoort'),
('7','vreemdelingenpaspoort'),
('8','paspoort met aantekening vergunning tot verblijf'),
('9','(electronisch) W-document');

-- 120
-- 107#245
drop view v_kad_eigenarenkaart;
-- 107#138
drop view kad_perceel_app_rechten;
-- 107#106
drop view v_kad_perceel_zr_adressen;
-- 107#73
drop view v_kad_perceel_zak_recht;
-- 107#21
drop view v_kad_perceel_in_eigendom;
-- 105#83
drop view v_bd_app_re_bij_perceel;

alter table ander_btnlnds_niet_nat_prs alter column sc_identif type character varying (255);
alter table ander_nat_prs alter column sc_identif type character varying (255);
alter table app_re alter column fk_2nnp_sc_identif type character varying (255);
alter table ingeschr_niet_nat_prs alter column sc_identif type character varying (255);
alter table ingeschr_nat_prs alter column sc_identif type character varying (255);
alter table ingezetene alter column sc_identif type character varying (255);
alter table kad_onrrnd_zk alter column fk_10pes_sc_identif type character varying (255);
alter table kad_onrrnd_zk_aantek alter column fk_5pes_sc_identif type character varying (255);
alter table maatschapp_activiteit alter column fk_4pes_sc_identif type character varying (255);
alter table nat_prs alter column sc_identif type character varying (255);
alter table niet_ingezetene alter column sc_identif type character varying (255);
alter table niet_nat_prs alter column sc_identif type character varying (255);
alter table prs alter column  sc_identif type character varying (255);
alter table subject alter column identif type character varying (255);
alter table vestg alter column  sc_identif type character varying (255);
alter table vestg alter column fk_18ves_sc_identif type character varying (255);
alter table zak_recht alter column fk_8pes_sc_identif type character varying (255);
alter table zak_recht_aantek alter column fk_6pes_sc_identif type character varying (255);
alter table vestg_naam alter column fk_ves_sc_identif type character varying (255);
alter table huishoudenrel alter column fk_sc_lh_inp_sc_identif type character varying (255);
alter table huw_ger_partn alter column fk_sc_lh_inp_sc_identif type character varying (255);
alter table huw_ger_partn alter column fk_sc_rh_inp_sc_identif type character varying (255);
alter table ouder_kind_rel alter column fk_sc_lh_inp_sc_identif type character varying (255);
alter table ouder_kind_rel alter column fk_sc_rh_inp_sc_identif type character varying (255);
alter table rsdoc_ingeschr_nat_prs alter column fk_nn_rh_inp_sc_identif type character varying (255);
alter table vestg_benoemd_obj alter column fk_nn_lh_ves_sc_identif type character varying (255);
alter table functionaris alter column fk_sc_lh_pes_sc_identif type character varying (255);
alter table functionaris alter column fk_sc_rh_pes_sc_identif type character varying (255);
alter table woz_belang alter column fk_sc_lh_sub_identif type character varying (255);
alter table vestg_activiteit alter column fk_vestg_nummer type character varying (255);
alter table prs_eigendom alter column fk_prs_sc_identif type character varying (255);

-- herstellen van de views
-- view om appartementsrechten bij percelen op te zoeken
CREATE OR REPLACE VIEW v_bd_app_re_bij_perceel AS
 SELECT
    (row_number() OVER ())::integer AS ObjectID,
    ar.sc_kad_identif,
    ar.fk_2nnp_sc_identif,
    ar.ka_appartementsindex,
    ar.ka_kad_gemeentecode,
    ar.ka_perceelnummer,
    ar.ka_sectie,
    kp.begrenzing_perceel
   FROM v_bd_app_re_all_kad_perceel v
     JOIN kad_perceel kp ON v.perceel_identif = kp.sc_kad_identif::varchar
     JOIN app_re ar ON v.app_re_identif = ar.sc_kad_identif::varchar;


create or replace view v_kad_perceel_in_eigendom as
select
   (row_number() OVER ())::integer AS ObjectID,
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

create or replace view v_kad_perceel_zak_recht as
 select
   p.sc_kad_identif as Kadaster_identificatie,
   zr.AR_TELLER  as Aandeel_teller,
   zr.AR_NOEMER as Aandeel_noemer,
   zr.FK_3AVR_AAND as Aard_recht_aand,
--    ark.omschr as Aard_recht_omschrijving_verkort, XXX referentielijst niet gevuld
--    ar.omschr_aard_verkregenr_recht as Aard_recht_omschrijving, XXX referentielijst niet gevuld
   case when np.sc_identif is not null then 'Natuurlijk persoon' else 'Niet natuurlijk persoon' end as soort_eigenaar,
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
   (row_number() OVER ())::integer AS ObjectID,
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




create or replace view kad_perceel_app_rechten as
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
    case when np1.sc_identif is not null then np1.NM_GESLACHTSNAAM || ', ' || np1.NM_VOORNAMEN || ' ' || np1.NM_VOORVOEGSEL_GESLACHTSNAAM else nnp1.NAAM end as perceel_zak_recht_naam,
--    nnp1.NAAM as l_nnp,

-- bd1.identificatie as brondocument,
-- zr2.kadaster_identif as rechts_zak_recht,
 zr2.FK_3AVR_AAND as app_re_zak_recht_aard_aand,
-- zr2.FK2_PERSOON as rechts_zak_recht_persoon,

--    case when np2.PK_PERSOON is not null then 'Natuurlijk persoon' else 'Niet natuurlijk persoon' end as r_soort_eigenaar,
    case when np2.sc_identif is not null then np2.NM_GESLACHTSNAAM || ', ' || np2.NM_VOORNAMEN || ' ' || np2.NM_VOORVOEGSEL_GESLACHTSNAAM else nnp2.NAAM end as app_re_zak_recht_naam,
--    nnp2.NAAM as r_nnp,

ar.SC_KAD_IDENTIF as app_re_identificatie,
 ar.KA_APPARTEMENTSINDEX::int as appartementsindex --,
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
order by kpe.SC_KAD_IDENTIF, kpe.straat, kpe.huisnummer, kpe.toevoeging, kpe.huisletter,  KA_APPARTEMENTSINDEX::int;


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
    (row_number() OVER ())::integer AS objectid,
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

-- issue #411
-- appartementsrecht aan bag adres
CREATE OR REPLACE VIEW v_app_re_adres AS
 SELECT DISTINCT
    kp.sc_kad_identif,
    kpvbo.fk_nn_lh_tgo_identif AS kad_bag_koppeling_benobj,
    gor.naam_openb_rmte AS straat,
    aoa.huinummer AS huisnummer,
    aoa.huisletter,
    aoa.huinummertoevoeging AS toevoeging,
    aoa.postcode,
    wp.naam AS woonplaats
 FROM app_re kp
    LEFT JOIN benoemd_obj_kad_onrrnd_zk kpvbo ON kpvbo.fk_nn_rh_koz_kad_identif = kp.sc_kad_identif
    LEFT JOIN verblijfsobj vbo ON vbo.sc_identif = kpvbo.fk_nn_lh_tgo_identif
    LEFT JOIN nummeraand na ON na.sc_identif = vbo.fk_11nra_sc_identif
    LEFT JOIN addresseerb_obj_aand aoa ON aoa.identif = na.sc_identif
    LEFT JOIN gem_openb_rmte gor ON gor.identifcode = aoa.fk_7opr_identifcode
    LEFT JOIN openb_rmte_wnplts oprw ON oprw.fk_nn_lh_opr_identifcode = gor.identifcode
    LEFT JOIN wnplts wp ON wp.identif = oprw.fk_nn_rh_wpl_identif;

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
    LEFT JOIN benoemd_obj_kad_onrrnd_zk kpvbo ON kpvbo.fk_nn_rh_koz_kad_identif = kp.kad_identif
    LEFT JOIN verblijfsobj vbo ON vbo.sc_identif = kpvbo.fk_nn_lh_tgo_identif
    LEFT JOIN nummeraand na ON na.sc_identif = vbo.fk_11nra_sc_identif
    LEFT JOIN addresseerb_obj_aand aoa ON aoa.identif = na.sc_identif
    LEFT JOIN gem_openb_rmte gor ON gor.identifcode = aoa.fk_7opr_identifcode
    LEFT JOIN openb_rmte_wnplts oprw ON oprw.fk_nn_lh_opr_identifcode = gor.identifcode
    LEFT JOIN wnplts wp ON wp.identif = oprw.fk_nn_rh_wpl_identif;

COMMENT ON VIEW v_kad_onrrd_zk_adres IS 'onroerende zaak met bag adres';

-- 121
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (0,'Onbekend',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (1,'Nederlandse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (2,'Behandeld als Nederlander','20070401','20070401');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (27,'Slowaakse','19930101',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (28,'Tsjechische','19930101',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (29,'Burger van Bosnië-Herzegovina','19920406',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (30,'Georgische','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (31,'Turkmeense','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (32,'Tadzjiekse','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (33,'Oezbeekse','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (34,'Oekraïense','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (35,'Kirgizische','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (36,'Moldavische','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (37,'Kazachse','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (38,'Belarussische','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (39,'Azerbeidzjaanse','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (40,'Armeense','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (41,'Russische','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (42,'Sloveense','19920115',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (43,'Kroatische','19920115',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (44,'Letse','19910828',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (45,'Estische','19910828',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (46,'Litouwse','19910828',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (47,'Marshalleilandse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (48,'Myanmarese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (49,'Namibische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (50,'Albanese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (51,'Andorrese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (52,'Belgische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (53,'Bulgaarse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (54,'Deense',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (55,'Burger van de Bondsrepubliek Duitsland',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (56,'Finse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (57,'Franse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (58,'Jemenitische','19900522',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (59,'Griekse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (60,'Brits burger',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (61,'Hongaarse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (62,'Ierse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (63,'IJslandse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (64,'Italiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (65,'Joegoslavische',null,'20040201');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (66,'Liechtensteinse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (67,'Luxemburgse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (68,'Maltese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (69,'Monegaskische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (70,'Noorse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (71,'Oostenrijkse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (72,'Poolse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (73,'Portugese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (74,'Roemeense',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (75,'Burger van de Sovjet-Unie',null,'19911231');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (76,'San Marinese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (77,'Spaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (78,'Tsjecho-Slowaakse',null,'19930101');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (79,'Vaticaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (80,'Zweedse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (81,'Zwitserse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (82,'Oost-Duitse',null,'19901003');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (83,'Brits onderdaan',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (84,'Eritrese','19930528',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (85,'Brits overzees burger',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (86,'Macedonische','19930419',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (87,'Kosovaarse','20080615',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (100,'Algerijnse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (101,'Angolese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (104,'Burundese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (105,'Botswaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (106,'Burkinese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (108,'Centraal-Afrikaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (109,'Comorese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (110,'Burger van Congo',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (111,'Beninse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (112,'Egyptische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (113,'Equatoriaal-Guinese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (114,'Ethiopische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (115,'Djiboutiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (116,'Gabonese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (117,'Gambiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (118,'Ghanese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (119,'Guinese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (120,'Ivoriaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (121,'Kaapverdische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (122,'Kameroense',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (123,'Kenyaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (124,'Zaïrese',null,'19970715');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (125,'Lesothaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (126,'Liberiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (127,'Libische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (128,'Malagassische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (129,'Malawische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (130,'Malinese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (131,'Marokkaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (132,'Mauritaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (133,'Mauritiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (134,'Mozambikaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (135,'Swazische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (136,'Nigerese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (137,'Nigeriaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (138,'Ugandese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (139,'Guinee-Bissause',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (140,'Zuid-Afrikaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (142,'Zimbabwaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (143,'Rwandese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (144,'Burger van São Tomé en Principe',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (145,'Senegalese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (147,'Sierra Leoonse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (148,'Soedanese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (149,'Somalische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (151,'Tanzaniaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (152,'Togolese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (154,'Tsjadische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (155,'Tunesische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (156,'Zambiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (157,'Zuid-Soedanese','20110709',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (200,'Bahamaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (202,'Belizaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (204,'Canadese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (205,'Costa Ricaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (206,'Cubaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (207,'Dominicaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (208,'Salvadoraanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (211,'Guatemalaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (212,'Haïtiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (213,'Hondurese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (214,'Jamaicaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (216,'Mexicaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (218,'Nicaraguaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (219,'Panamese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (222,'Burger van Trinidad en Tobago',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (223,'Amerikaans burger',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (250,'Argentijnse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (251,'Barbadaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (252,'Boliviaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (253,'Braziliaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (254,'Chileense',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (255,'Colombiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (256,'Ecuadoraanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (259,'Guyaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (261,'Paraguayaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (262,'Peruaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (263,'Surinaamse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (264,'Uruguayaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (265,'Venezolaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (267,'Grenadaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (268,'Burger van Saint Kitts en Nevis',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (300,'Afghaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (301,'Bahreinse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (302,'Bhutaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (303,'Burmaanse',null,'19890618');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (304,'Bruneise',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (305,'Cambodjaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (306,'Sri Lankaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (307,'Chinese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (308,'Cyprische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (309,'Filipijnse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (310,'Taiwanese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (312,'Indiase',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (313,'Indonesische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (314,'Iraakse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (315,'Iraanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (316,'Israëlische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (317,'Japanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (318,'Noord-Jemenitische',null,'19900522');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (319,'Jordaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (320,'Koeweitse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (321,'Laotiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (322,'Libanese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (324,'Maldivische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (325,'Maleisische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (326,'Mongolische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (327,'Omaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (328,'Nepalese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (329,'Noord-Koreaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (331,'Pakistaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (333,'Qatarese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (334,'Saoedi-Arabische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (335,'Singaporese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (336,'Syrische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (337,'Thaise',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (338,'Burger van de Verenigde Arabische Emiraten',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (339,'Turkse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (340,'Zuid-Jemenitische',null,'19900522');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (341,'Zuid-Koreaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (342,'Vietnamese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (345,'Bengalese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (400,'Australische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (401,'Papoea-Nieuw-Guinese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (402,'Nieuw-Zeelandse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (404,'West-Samoaanse',null,'19970704');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (405,'Samoaanse','19970704',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (421,'Burger van Antigua en Barbuda',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (424,'Vanuatuaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (425,'Fijische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (429,'Burger van Britse afhankelijke gebieden',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (430,'Tongaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (431,'Nauruaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (432,'Palause','19941001',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (437,'Amerikaans onderdaan',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (442,'Salomonseilandse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (443,'Micronesische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (444,'Seychelse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (445,'Kiribatische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (446,'Tuvaluaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (447,'Saint Luciaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (448,'Burger van Dominica',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (449,'Burger van Saint Vincent en de Grenadines',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (450,'British National (overseas)','19870701',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (451,'Burger van Democratische Republiek Congo','19970517',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (452,'Burger van Timor Leste','20020520',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (453,'Burger van Servië en Montenegro','20030204','20060603');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (454,'Servische','20060603',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (455,'Montenegrijnse','20060603',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (499,'Staatloos',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (500,'Vastgesteld niet-Nederlander','20070401','20070401');

-- data update voor issue #435
-- brk
UPDATE kad_onrrnd_zk_archief SET datum_einde_geldh = TO_CHAR(TO_DATE(datum_einde_geldh, 'DD-MM-YYYY'),'YYYY-MM-DD') WHERE datum_einde_geldh ~ '\d{2}-\d{2}-\d{4}';
UPDATE kad_onrrnd_zk_aantek_archief SET eindd_aantek_kad_obj = TO_CHAR(TO_DATE(eindd_aantek_kad_obj, 'DD-MM-YYYY'),'YYYY-MM-DD') WHERE eindd_aantek_kad_obj ~ '\d{2}-\d{2}-\d{4}';
-- bag
UPDATE addresseerb_obj_aand_archief SET dat_eind_geldh = TO_CHAR(TO_DATE(dat_eind_geldh, 'DD-MM-YYYY'),'YYYY-MM-DD') WHERE dat_eind_geldh ~ '\d{2}-\d{2}-\d{4}';
UPDATE benoemd_terrein_archief SET datum_einde_geldh = TO_CHAR(TO_DATE(datum_einde_geldh, 'DD-MM-YYYY'),'YYYY-MM-DD') WHERE datum_einde_geldh ~ '\d{2}-\d{2}-\d{4}';
UPDATE gebouwd_obj_archief SET datum_einde_geldh = TO_CHAR(TO_DATE(datum_einde_geldh, 'DD-MM-YYYY'),'YYYY-MM-DD') WHERE datum_einde_geldh ~ '\d{2}-\d{2}-\d{4}';
UPDATE gem_openb_rmte_archief SET datum_einde_geldh = TO_CHAR(TO_DATE(datum_einde_geldh, 'DD-MM-YYYY'),'YYYY-MM-DD') WHERE datum_einde_geldh ~ '\d{2}-\d{2}-\d{4}';
UPDATE pand_archief SET datum_einde_geldh = TO_CHAR(TO_DATE(datum_einde_geldh, 'DD-MM-YYYY'),'YYYY-MM-DD') WHERE datum_einde_geldh ~ '\d{2}-\d{2}-\d{4}';
UPDATE wnplts_archief SET datum_einde_geldh = TO_CHAR(TO_DATE(datum_einde_geldh, 'DD-MM-YYYY'),'YYYY-MM-DD') WHERE datum_einde_geldh ~ '\d{2}-\d{2}-\d{4}';


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.5.2_naar_1.6.0','vorige versie was '||waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.0' WHERE naam='brmoversie';
