 /***************************************************
VIEW PV_MAP_O_KPE, BRON BRK

OPMERKING: DEZE VIEW BEVAT GEO KOLOM IS BRUIKBAAR VOOR KAARTWEERGAVE, VERTROUWELIJKHEID: OPENBAAR
OPMERKING: DEZE VIEW BEVAT 2 GEO KOLOMMEN; ADVIES B3P? OPSPLITSEN IN APARTE VIEWS? BEGRENZING_PERCEEL IS IN ELK GEVAL RELEVANT. PLAATSCOORDINATEN_PERCEEL OOK?

select * from pv_map_o_kpe limit 10;

select * from pv_map_o_kpe where kad_identif=2650034670000
***************************************************/  
create or replace view pv_map_o_kpe as
 SELECT a.cu_aard_bebouwing,
    a.sc_kad_identif,
    a.aand_soort_grootte,
    a.grootte_perceel,
    a.omschr_deelperceel,                                        
    a.ka_kad_gemeentecode,
    a.ka_perceelnummer,
    a.ka_sectie,
    a.begrenzing_perceel,
	(a.ka_sectie::text || ' '::text) || a.ka_perceelnummer::text AS aanduiding,
--  a.plaatscoordinaten_perceel,
    a.cu_aard_cultuur_onbebouwd,
    a.dat_beg_geldh,
    a.datum_einde_geldh,
    a.kad_identif,
--    a.ks_bedrag,
--    a.ks_koopjaar,
    a.ks_meer_onroerendgoed
   FROM pm_kad_perceel a
;


/***************************************************
VIEW PV_MAP_I_KPE, BRON BRK

OPMERKING: DEZE VIEW BEVAT GEO KOLOM IS BRUIKBAAR VOOR KAARTWEERGAVE, VERTROUWELIJKHEID: INTERN
OPMERKING: DEZE VIEW BEVAT 2 GEO KOLOMMEN; ADVIES B3P? OPSPLITSEN IN APARTE VIEWS? BEGRENZING_PERCEEL IS IN ELK GEVAL RELEVANT. PLAATSCOORDINATEN_PERCEEL OOK?
select * from pv_map_i_kpe limit 10;

select * from pv_map_i_kpe where kad_identif=2650034670000
***************************************************/  
create or replace view pv_map_i_kpe as
 SELECT a.cu_aard_bebouwing,
    a.sc_kad_identif,
    a.aand_soort_grootte,
    a.grootte_perceel,
    a.omschr_deelperceel,                                        
    a.ka_kad_gemeentecode,
    a.ka_perceelnummer,
    a.ka_sectie,
    a.begrenzing_perceel,
	(a.ka_sectie::text || ' '::text) || a.ka_perceelnummer::text AS aanduiding,
--  a.plaatscoordinaten_perceel,
    a.cu_aard_cultuur_onbebouwd,
    a.dat_beg_geldh,
    a.datum_einde_geldh,
    a.kad_identif,
    a.ks_bedrag,
    a.ks_koopjaar,
    a.ks_meer_onroerendgoed
   FROM pv_kad_perceel a
;


/****************************************
AANMAKEN VIEW PV_INFO_O_KOZ_ADRES

select * from pv_info_o_koz_adres where koz_identif='2650034670000';

****************************************/
create or replace view pv_info_o_koz_adres as
select bok.fk_nn_lh_tgo_identif bob_identif,
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
from pm_benoemd_obj_kad_onr_zk bok 
join pm_benoemd_object bob 
on bob.identif = bok.fk_nn_lh_tgo_identif
join pm_verblijfsobject vob 
on vob.sc_identif = bob.identif
join pm_adr_object_nummeraand aon
on aon.identif = vob.fk_11nra_sc_identif;

/****************************************
AANMAKEN VIEW PV_INFO_I_KOZ_ADRES

select * from pv_info_i_koz_adres where koz_identif='2650034670000';

****************************************/
create or replace view pv_info_i_koz_adres as
select bok.fk_nn_lh_tgo_identif bob_identif,
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
from pm_benoemd_obj_kad_onr_zk bok 
join pm_benoemd_object bob 
on bob.identif = bok.fk_nn_lh_tgo_identif
join pm_verblijfsobject vob 
on vob.sc_identif = bob.identif
join pm_adr_object_nummeraand aon
on aon.identif = vob.fk_11nra_sc_identif;

/****************************************
AANMAKEN VIEW PV_INFO_I_KOZ_ZAK_RECHT

select * from pv_info_i_koz_zak_recht where koz_identif='2650034670000';

****************************************/

create or replace view pv_info_i_koz_zak_recht
as
 SELECT koz.kad_identif AS koz_identif,
    zre.eindd_recht,
	zre.indic_betrokken_in_splitsing,
	zre.ingangsdatum_recht,
    pso.sc_identif pso_identif,
	CASE
        WHEN npe.sc_identif IS NOT NULL THEN 'Natuurlijk persoon'::text
            ELSE 'Niet natuurlijk persoon'::text
        END AS soort_eigenaar,
    npe.nm_geslachtsnaam AS geslachtsnaam,
    npe.nm_voorvoegsel_geslachtsnaam AS voorvoegsel,
    npe.nm_voornamen AS voornamen,
    npe.geslachtsaand AS geslacht,
    inp.va_loc_beschrijving AS woonadres,
    inp.gb_geboortedatum AS geboortedatum,
    inp.gb_geboorteplaats AS geboorteplaats,
    inp.ol_overlijdensdatum AS overlijdensdatum,
    nnp.nnp_naam AS naam_niet_natuurlijk_persoon,
    nnp.rechtsvorm,
    nnp.statutaire_zetel,
    nnp.kvk_nummer,
    zre.ar_noemer AS aandeel_noemer,
	zre.ar_teller AS aandeel_teller,
	zre.arv_omschr,
    zre.fk_3avr_aand
   FROM pm_kad_onroerende_zaak koz
     JOIN pm_zakelijk_recht zre ON zre.fk_7koz_kad_identif = koz.kad_identif
	 LEFT JOIN pm_persoon pso on  pso.sc_identif::text = zre.fk_8pes_sc_identif::text
     LEFT JOIN pm_natuurlijk_persoon npe ON npe.sc_identif::text = pso.sc_identif::text
     LEFT JOIN pm_ingeschr_natuurlijk_persoon inp ON inp.sc_identif::text = pso.sc_identif::text
     LEFT JOIN pm_niet_natuurlijk_persoon nnp ON nnp.sc_identif::text =  pso.sc_identif::text
    WHERE (npe.nm_geslachtsnaam IS NOT NULL OR nnp.nnp_naam IS NOT NULL) 
  ;

  
/****************************************
AANMAKEN VIEW PV_INFO_I_KOZ_ZAK_RECHT_AANT

select * from pv_info_i_koz_zak_recht_aant where koz_identif='2650034670000';

****************************************/
create or replace view pv_info_i_koz_zak_recht_aant as
select 
    koz.kad_identif AS koz_identif,
    zre.kadaster_identif,
    zra.kadaster_identif_aantek_recht,
    zra.aard_aantek_recht,          
    zra.begindatum_aantek_recht,     
    zra.beschrijving_aantek_recht,   
    zra.eindd_aantek_recht           
   FROM pm_kad_onroerende_zaak koz
     JOIN pm_zakelijk_recht zre ON zre.fk_7koz_kad_identif = koz.kad_identif
     JOIN pm_zakelijk_recht_aantekening zra on zra.fk_5zkr_kadaster_identif = zre.kadaster_identif;

/**********************************************************************  
AANMAKEN VIEW PV_MAP_O_PND PLUS DETAIL VIEWS (GEO VIEW VOOR PAND + DETAIL VIEWS)
***********************************************************************/
create or replace view pv_map_o_pnd as
 SELECT pm_pand.dat_beg_geldh,
    pm_pand.identif,
    pm_pand.datum_einde_geldh,
    pm_pand.indic_geconstateerd,
    pm_pand.oorspronkelijk_bouwjaar,
    pm_pand.status,
    pm_pand.geom_bovenaanzicht
   FROM pm_pand;

/**********************************************************************  
AANMAKEN VIEW PV_MAP_O_VOB PLUS DETAIL VIEWS (GEO VIEW VOOR VERBLIJFSOBJECT + DETAIL VIEW)

DETAIL VIEW:
	VIEW PV_INFO_O_VOB_HOOFDADR 
***********************************************************************/
create or replace view pv_map_o_vob as
 SELECT pm_verblijfsobject.sc_identif,
    pm_verblijfsobject.fk_11nra_sc_identif,
    pm_verblijfsobject.indic_geconstateerd,
    pm_verblijfsobject.status,
    pm_verblijfsobject.dat_beg_geldh,
    pm_verblijfsobject.clazz,
    pm_verblijfsobject.datum_einde_geldh,
    pm_verblijfsobject.oppervlakte_obj,
    pm_verblijfsobject.puntgeom
   FROM pm_verblijfsobject;
   
create or replace view pv_info_o_vob_hoofdadr as
select 
a.sc_identif vbo_identif,
a.indic_geconstateerd,
a.status vbo_status,
a.dat_beg_geldh vbo_dat_beg_geldh,
a.clazz              ,
a.datum_einde_geldh  vbo_datum_einde_geldh,
a.oppervlakte_obj    ,
b.identif nummeraand_identif,
b.dat_beg_geldh nummeraand_dat_beg_geldh,
b.dat_eind_geldh nummeraand_datum_einde_geldh,
b.huinummer          ,
b.huinummertoevoeging,
b.huisletter         ,
b.postcode           ,
b.naam_openb_rmte    ,
b.gem_code           ,
b.gem_naam           ,
b.wpl_naam           ,
b.indic_hoofdadres   ,
b.status nummeraand_status           
from pm_verblijfsobject a join pm_adr_object_nummeraand b
on a.fk_11nra_sc_identif = b.identif;


/**********************************************************************  
AANMAKEN VIEW PV_MAP_O_BTE PLUS(GEO VIEW VOOR BENOEMD TERREINS)	 
***********************************************************************/
create or replace view pv_map_o_bte as
 SELECT pm_benoemd_terrein.dat_beg_geldh,
    pm_benoemd_terrein.sc_identif,
    pm_benoemd_terrein.clazz,
    pm_benoemd_terrein.datum_einde_geldh,
    pm_benoemd_terrein.bt_geom
   FROM pm_benoemd_terrein;

/**********************************************************************  
AANMAKEN VIEW PV_MAP_O_STP PLUS DETAIL VIEWS (GEO VIEW VOOR STANDPLAATS + DETAIL VIEW)
***********************************************************************/
create or replace view pv_map_o_stp as
 SELECT pm_standplaats.sc_identif,
    pm_standplaats.indic_geconst,
    pm_standplaats.status,
    pm_standplaats.fk_4nra_sc_identif,
    pm_standplaats.dat_beg_geldh,
    pm_standplaats.clazz,
    pm_standplaats.datum_einde_geldh,
    pm_standplaats.bt_geom
   FROM pm_standplaats;
   
create or replace view pv_info_o_stp_hoofdadr as
select a.sc_identif stp_indentif,
a.indic_geconst,
a.status stp_status,
a.dat_beg_geldh stp_dat_beg_geldh,
a.clazz,
a.datum_einde_geldh stp_datum_einde_geldh, 
b.identif nummeraand_identif,
b.dat_beg_geldh nummeraand_dat_beg_geldh,
b.dat_eind_geldh nummeraand_datum_einde_geldh,
b.huinummer          ,
b.huinummertoevoeging,
b.huisletter         ,
b.postcode           ,
b.naam_openb_rmte    ,
b.gem_code           ,
b.gem_naam           ,
b.wpl_naam           ,
b.indic_hoofdadres   ,
b.status nummeraand_status           
from pm_standplaats a join
pm_adr_object_nummeraand b
on a.fk_4nra_sc_identif = b.identif; 

/**********************************************************************  
AANMAKEN VIEW PV_MAP_O_LIP PLUS DETAIL VIEWS (GEO VIEW VOOR LIGPLAATS + DETAIL VIEW)
***********************************************************************/
create or replace view pv_map_o_lip as 
 SELECT pm_ligplaats.sc_identif,
    pm_ligplaats.indic_geconst,
    pm_ligplaats.status,
    pm_ligplaats.fk_4nra_sc_identif,
    pm_ligplaats.dat_beg_geldh,
    pm_ligplaats.clazz,
    pm_ligplaats.datum_einde_geldh,
    pm_ligplaats.bt_geom
   FROM pm_ligplaats;
   
create or replace view pv_info_o_lip_hoofdadr as
select a.sc_identif lip_indentif,
a.indic_geconst,
a.status lip_status,
a.dat_beg_geldh lip_dat_beg_geldh,
a.clazz,
a.datum_einde_geldh lip_datum_einde_geldh, 
b.identif nummeraand_identif,
b.dat_beg_geldh nummeraand_dat_beg_geldh,
b.dat_eind_geldh nummeraand_datum_einde_geldh,
b.huinummer          ,
b.huinummertoevoeging,
b.huisletter         ,
b.postcode           ,
b.naam_openb_rmte    ,
b.gem_code           ,
b.gem_naam           ,
b.wpl_naam           ,
b.indic_hoofdadres   ,
b.status nummeraand_status           
from pm_ligplaats a join
pm_adr_object_nummeraand b
on a.fk_4nra_sc_identif = b.identif; 
