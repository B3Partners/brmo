/***************************************************
VIEW PV_MAP_O_KPE, BRON BRK
OPMERKING: DEZE VIEW BEVAT GEO KOLOM IS BRUIKBAAR VOOR KAARTWEERGAVE, VERTROUWELIJKHEID: OPENBAAR
OPMERKING: DEZE VIEW BEVAT 2 GEO KOLOMMEN; ADVIES B3P? OPSPLITSEN IN APARTE VIEWS? BEGRENZING_PERCEEL IS IN ELK GEVAL RELEVANT. PLAATSCOORDINATEN_PERCEEL OOK?
select * from pv_map_o_kpe limit 10;
select * from pv_map_o_kpe where kad_identif=2650034670000
***************************************************/
CREATE OR REPLACE VIEW pv_map_o_kpe
AS
  SELECT a.cu_aard_bebouwing,
    a.sc_kad_identif,
    a.aand_soort_grootte,
    a.grootte_perceel,
    a.omschr_deelperceel,
    a.ka_kad_gemeentecode,
    a.ka_perceelnummer,
    a.ka_sectie,
    a.begrenzing_perceel,
    (a.ka_sectie
    || ' ')
    || a.ka_perceelnummer AS aanduiding,
    --  a.plaatscoordinaten_perceel,
    a.cu_aard_cultuur_onbebouwd,
    a.dat_beg_geldh,
    a.datum_einde_geldh,
    a.kad_identif,
    --    a.ks_bedrag,
    --    a.ks_koopjaar,
    a.ks_meer_onroerendgoed
  FROM pv_kad_perceel a ;
  /***************************************************
  VIEW PV_MAP_I_KPE, BRON BRK
  OPMERKING: DEZE VIEW BEVAT GEO KOLOM IS BRUIKBAAR VOOR KAARTWEERGAVE, VERTROUWELIJKHEID: INTERN
  OPMERKING: DEZE VIEW BEVAT 2 GEO KOLOMMEN; ADVIES B3P? OPSPLITSEN IN APARTE VIEWS? BEGRENZING_PERCEEL IS IN ELK GEVAL RELEVANT. PLAATSCOORDINATEN_PERCEEL OOK?
  select * from pv_map_i_kpe limit 10;
  select * from pv_map_i_kpe where kad_identif=2650034670000
  ***************************************************/
CREATE OR REPLACE VIEW pv_map_i_kpe
AS
  SELECT a.cu_aard_bebouwing,
    a.sc_kad_identif,
    a.aand_soort_grootte,
    a.grootte_perceel,
    a.omschr_deelperceel,
    a.ka_kad_gemeentecode,
    a.ka_perceelnummer,
    a.ka_sectie,
    a.begrenzing_perceel,
    (a.ka_sectie
    || ' ')
    || a.ka_perceelnummer AS aanduiding,
    --  a.plaatscoordinaten_perceel,
    a.cu_aard_cultuur_onbebouwd,
    a.dat_beg_geldh,
    a.datum_einde_geldh,
    a.kad_identif,
    a.ks_bedrag,
    a.ks_koopjaar,
    a.ks_meer_onroerendgoed
  FROM pv_kad_perceel a ;
  /****************************************
  AANMAKEN VIEW PV_INFO_O_KOZ_ADRES
  select * from pv_info_o_koz_adres where koz_identif='2650034670000';
  ****************************************/
CREATE OR REPLACE VIEW pv_info_o_koz_adres
AS
  SELECT bok.fk_nn_lh_tgo_identif bob_identif,
    bok.fk_nn_rh_koz_kad_identif koz_identif,
    aon.identif aon_identif,
    aon.dat_beg_geldh,
    aon.dat_eind_geldh,
    aon.huinummer,
    aon.huinummertoevoeging,
    aon.huisletter,
    aon.postcode,
    aon.naam_openb_rmte,
    aon.gem_code,
    aon.gem_naam,
    aon.wpl_naam,
    aon.status
  FROM pv_benoemd_obj_kad_onr_zk bok
  JOIN pv_benoemd_object bob
  ON bob.identif = bok.fk_nn_lh_tgo_identif
  JOIN pv_verblijfsobject vob
  ON vob.sc_identif = bob.identif
  JOIN pv_adr_object_nummeraand aon
  ON aon.identif = vob.fk_11nra_sc_identif;
  /****************************************
  AANMAKEN VIEW PV_INFO_I_KOZ_ADRES
  select * from pv_info_i_koz_adres where koz_identif='2650034670000';
  ****************************************/
CREATE OR REPLACE VIEW pv_info_i_koz_adres
AS
  SELECT bok.fk_nn_lh_tgo_identif bob_identif,
    bok.fk_nn_rh_koz_kad_identif koz_identif,
    aon.identif aon_identif,
    aon.dat_beg_geldh,
    aon.dat_eind_geldh,
    aon.huinummer,
    aon.huinummertoevoeging,
    aon.huisletter,
    aon.postcode,
    aon.naam_openb_rmte,
    aon.gem_code,
    aon.gem_naam,
    aon.wpl_naam,
    aon.status
  FROM pv_benoemd_obj_kad_onr_zk bok
  JOIN pv_benoemd_object bob
  ON bob.identif = bok.fk_nn_lh_tgo_identif
  JOIN pv_verblijfsobject vob
  ON vob.sc_identif = bob.identif
  JOIN pv_adr_object_nummeraand aon
  ON aon.identif = vob.fk_11nra_sc_identif;
  /****************************************
  AANMAKEN VIEW PV_INFO_I_KOZ_ZAK_RECHT
  select * from pv_info_i_koz_zak_recht where koz_identif='2650034670000';
  ****************************************/
CREATE OR REPLACE VIEW pv_info_i_koz_zak_recht
                         AS
  SELECT koz.kad_identif AS koz_identif,
    zre.eindd_recht,
    zre.indic_betrokken_in_splitsing,
    zre.ingangsdatum_recht,
    pso.sc_identif pso_identif,
    CASE
      WHEN npe.sc_identif IS NOT NULL
      THEN 'Natuurlijk persoon'
      ELSE 'Niet natuurlijk persoon'
    END                              AS soort_eigenaar,
    npe.nm_geslachtsnaam             AS geslachtsnaam,
    npe.nm_voorvoegsel_geslachtsnaam AS voorvoegsel,
    npe.nm_voornamen                 AS voornamen,
    npe.geslachtsaand                AS geslacht,
    inp.va_loc_beschrijving          AS woonadres,
    inp.gb_geboortedatum             AS geboortedatum,
    inp.gb_geboorteplaats            AS geboorteplaats,
    inp.ol_overlijdensdatum          AS overlijdensdatum,
    nnp.nnp_naam                     AS naam_niet_natuurlijk_persoon,
    nnp.rechtsvorm,
    nnp.statutaire_zetel,
    nnp.kvk_nummer,
    zre.ar_noemer AS aandeel_noemer,
    zre.ar_teller AS aandeel_teller,
    zre.arv_omschr,
    zre.fk_3avr_aand
  FROM pv_kad_onroerende_zaak koz
  JOIN pv_zakelijk_recht zre
  ON zre.fk_7koz_kad_identif = koz.kad_identif
  LEFT JOIN pv_persoon pso
  ON pso.sc_identif = zre.fk_8pes_sc_identif
  LEFT JOIN pv_natuurlijk_persoon npe
  ON npe.sc_identif = pso.sc_identif
  LEFT JOIN pv_ingeschr_natuurlijk_persoon inp
  ON inp.sc_identif = pso.sc_identif
  LEFT JOIN pv_niet_natuurlijk_persoon nnp
  ON nnp.sc_identif            = pso.sc_identif
  WHERE (npe.nm_geslachtsnaam IS NOT NULL
  OR nnp.nnp_naam             IS NOT NULL);
  /****************************************
  AANMAKEN VIEW PV_INFO_I_KOZ_ZAK_RECHT_AANT
  select * from pv_info_i_koz_zak_recht_aant where koz_identif='2650034670000';
  ****************************************/
CREATE OR REPLACE VIEW pv_info_i_koz_zak_recht_aant
                         AS
  SELECT koz.kad_identif AS koz_identif,
    zre.kadaster_identif,
    zra.kadaster_identif_aantek_recht,
    zra.aard_aantek_recht,
    zra.begindatum_aantek_recht,
    zra.beschrijving_aantek_recht,
    zra.eindd_aantek_recht
  FROM pv_kad_onroerende_zaak koz
  JOIN pv_zakelijk_recht zre
  ON zre.fk_7koz_kad_identif = koz.kad_identif
  JOIN pv_zakelijk_recht_aantekening zra
  ON zra.fk_5zkr_kadaster_identif = zre.kadaster_identif;
  /**********************************************************************
  AANMAKEN VIEW PV_MAP_O_PND PLUS DETAIL VIEWS (GEO VIEW VOOR PAND + DETAIL VIEWS)
  ***********************************************************************/
CREATE OR REPLACE VIEW pv_map_o_pnd
AS
  SELECT p.dat_beg_geldh,
    p.identif,
    p.datum_einde_geldh,
    p.indic_geconstateerd,
    p.oorspronkelijk_bouwjaar,
    p.status,
    p.geom_bovenaanzicht
  FROM pv_pand p;
  /**********************************************************************
  AANMAKEN VIEW PV_MAP_O_VOB PLUS DETAIL VIEWS (GEO VIEW VOOR VERBLIJFSOBJECT + DETAIL VIEW)
  DETAIL VIEW:
  VIEW PV_INFO_O_VOB_HOOFDADR
  ***********************************************************************/
CREATE OR REPLACE VIEW pv_map_o_vob
AS
  SELECT v.sc_identif,
    v.fk_11nra_sc_identif,
    v.indic_geconstateerd,
    v.status,
    v.dat_beg_geldh,
    v.clazz,
    v.datum_einde_geldh,
    v.oppervlakte_obj,
    v.puntgeom
  FROM pv_verblijfsobject v;
CREATE OR REPLACE VIEW pv_info_o_vob_hoofdadr
AS
  SELECT a.sc_identif vbo_identif,
    a.indic_geconstateerd,
    a.status vbo_status,
    a.dat_beg_geldh vbo_dat_beg_geldh,
    a.clazz ,
    a.datum_einde_geldh vbo_datum_einde_geldh,
    a.oppervlakte_obj ,
    b.identif nummeraand_identif,
    b.dat_beg_geldh nummeraand_dat_beg_geldh,
    b.dat_eind_geldh nummeraand_datum_einde_geldh,
    b.huinummer ,
    b.huinummertoevoeging,
    b.huisletter ,
    b.postcode ,
    b.naam_openb_rmte ,
    b.gem_code ,
    b.gem_naam ,
    b.wpl_naam ,
    b.indic_hoofdadres ,
    b.status nummeraand_status
  FROM pv_verblijfsobject a
  JOIN pv_adr_object_nummeraand b
  ON a.fk_11nra_sc_identif = b.identif;
  /**********************************************************************
  AANMAKEN VIEW PV_MAP_O_BTE PLUS(GEO VIEW VOOR BENOEMD TERREINS)
  ***********************************************************************/
CREATE OR REPLACE VIEW pv_map_o_bte
AS
  SELECT b.dat_beg_geldh,
    b.sc_identif,
    b.clazz,
    b.datum_einde_geldh,
    b.bt_geom
  FROM pv_benoemd_terrein b;
  /**********************************************************************
  AANMAKEN VIEW PV_MAP_O_STP PLUS DETAIL VIEWS (GEO VIEW VOOR STANDPLAATS + DETAIL VIEW)
  ***********************************************************************/
CREATE OR REPLACE VIEW pv_map_o_stp
AS
  SELECT s.sc_identif,
    s.indic_geconst,
    s.status,
    s.fk_4nra_sc_identif,
    s.dat_beg_geldh,
    s.clazz,
    s.datum_einde_geldh,
    s.bt_geom
  FROM pv_standplaats s;
CREATE OR REPLACE VIEW pv_info_o_stp_hoofdadr
AS
  SELECT a.sc_identif stp_indentif,
    a.indic_geconst,
    a.status stp_status,
    a.dat_beg_geldh stp_dat_beg_geldh,
    a.clazz,
    a.datum_einde_geldh stp_datum_einde_geldh,
    b.identif nummeraand_identif,
    b.dat_beg_geldh nummeraand_dat_beg_geldh,
    b.dat_eind_geldh nummeraand_datum_einde_geldh,
    b.huinummer ,
    b.huinummertoevoeging,
    b.huisletter ,
    b.postcode ,
    b.naam_openb_rmte ,
    b.gem_code ,
    b.gem_naam ,
    b.wpl_naam ,
    b.indic_hoofdadres ,
    b.status nummeraand_status
  FROM pv_standplaats a
  JOIN pv_adr_object_nummeraand b
  ON a.fk_4nra_sc_identif = b.identif;
  /**********************************************************************
  AANMAKEN VIEW PV_MAP_O_LIP PLUS DETAIL VIEWS (GEO VIEW VOOR LIGPLAATS + DETAIL VIEW)
  ***********************************************************************/
CREATE OR REPLACE VIEW pv_map_o_lip
AS
  SELECT l.sc_identif,
    l.indic_geconst,
    l.status,
    l.fk_4nra_sc_identif,
    l.dat_beg_geldh,
    l.clazz,
    l.datum_einde_geldh,
    l.bt_geom
  FROM pv_ligplaats l;
CREATE OR REPLACE VIEW pv_info_o_lip_hoofdadr
AS
  SELECT a.sc_identif lip_indentif,
    a.indic_geconst,
    a.status lip_status,
    a.dat_beg_geldh lip_dat_beg_geldh,
    a.clazz,
    a.datum_einde_geldh lip_datum_einde_geldh,
    b.identif nummeraand_identif,
    b.dat_beg_geldh nummeraand_dat_beg_geldh,
    b.dat_eind_geldh nummeraand_datum_einde_geldh,
    b.huinummer ,
    b.huinummertoevoeging,
    b.huisletter ,
    b.postcode ,
    b.naam_openb_rmte ,
    b.gem_code ,
    b.gem_naam ,
    b.wpl_naam ,
    b.indic_hoofdadres ,
    b.status nummeraand_status
  FROM pv_ligplaats a
  JOIN pv_adr_object_nummeraand b
  ON a.fk_4nra_sc_identif = b.identif;
