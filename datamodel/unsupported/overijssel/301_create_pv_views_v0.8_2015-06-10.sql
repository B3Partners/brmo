/**************************************************
VIEW PV_GEMEENTE, BRON ALGEMEEN
ACTIE 27-3-2015: BEVAT NU GEMEENTEN. B3P MAAKT HERSTELSCRIPT. DEZE WEEK GEREED
ACTIE 27-3-2015: WIJK EN BUURT: B3P ZORG VOOR VULLING, ZODRA BESCHIKBAAR VIEWS MAKEN
***************************************************/
CREATE OR REPLACE VIEW pv_gemeente
AS
  SELECT a.dat_beg_geldh,
    a.code,
    a.datum_einde_geldh,
    a.naam,
    --       a.naam_nen,
    a.geom
  FROM gemeente a ;
  /**************************************************
  VIEW PV_WOONPLAATS, BRON ALGEMEEN
  ACTIE 27-3-2015: ZODRA SCRIPT B3 GEREED LEFT JOIN WEGHALEN,
  OPMERKING: WP_GEOM NIET BETROUWBAAR VANUIT BRON, DAAROM UITCOMMENTARIEREN, 31-3-2015 GEREED
  OPMERKING 22-04-2015: IN ONDERLIGGENDE TABEL GEMEENTE ZIJN DAT_BEG_GELDH, DATUM_EINDE_GELDH, NAAM_NEN, GEOM NIET GEVULD EN IN ONDERLIGGENDE TABEL WNPLTS ZIJN DATUM_EINDE_GELDH, INDIC_GECONST EN NAAM_NEN NIET GEVULD
  OPMERKING2 22-04-2015: DE TABEL BEVAT 154 DUBBELE WOONPLAATSEN:
  select count(*), naam from wnplts group by naam having count(*) > 1;
  ***************************************************/
CREATE OR REPLACE VIEW pv_woonplaats
AS
  SELECT a.dat_beg_geldh,
    a.identif,
    a.datum_einde_geldh,
    --        a.indic_geconst,
    a.naam,
    --      a.naam_nen
    a.status,
    a.fk_7gem_code,
    --      a.geom AS wp_geom,
    b.dat_beg_geldh gem_dat_beg_geldh,
    b.code gem_code,
    b.datum_einde_geldh gem_datum_einde_geldh,
    b.naam gem_naam
    --      b.naam_nen gem_naam_nen
  FROM wnplts a
  LEFT JOIN gemeente b
  ON a.fk_7gem_code = b.code;
  /***************************************************
  VIEW PV_ADRES, HERKOMST BAG
  --------------------------------------+ openb_rmte_wnplts +-------------------------------------------
  |                                     (0)                 (0)                                        |
  |                                                                                                    |
  |                                                                                                    |
  |                                                                                                    |
  openb_rmte ------------------------------+ addresseerb_obj_aand +-------------------------------------- wnplts 2594
  |272942                         (8915887)     8915887      (1783)                                   (0)
  |                                       |                                                             +
  |                                       |                                                             |
  |                                       |                                                             |
  |                                       |                                                             |
  + (272942)                               |                                                             |
  openb_rmte_gem_openb_rmte                  |                                                            gemeente   2594
  + (272942)                             |                                                             |
  |                                      | (8915887)                                                   |
  |                                   nummeraand                                                       |
  |                                                                                                    |
  |                                                                                                    |
  gem_openb_rmte +------------------------------------------------------------------------------------------
  272942      (0)
  OPMERKING 27-3-2015: IN SLECHTS 1783 GEVALLEN IS EEN RELATIE NAAR DE WOONPLAATS VIA DE TABEL ADRESSEERBAAR_OBJ_AAND. OORZAAK: VELD FK_6WPL_IDENTIF IS NIET GEVULD:
  rsgb=# select count(*), case when fk_6wpl_identif is not null then -1 else 0 end as "lege velden 0" from addresseerb_obj_aand group by case when fk_6wpl_identif is not null then -1 else 0 end ;
  count  | lege velden 0
  ---------+---------------
  8914104 |             0
  1783 |            -1
  (2 rows)
  OPMERKING 27-3-2015: IN GEEN ENKEL GEVAL IS EEN RELATIE TE LEGGEN NAAR DE GEMEENTE VIA DE TABEL GEM_OPENB_RMTE. OORZAAK: VELD FK_7GEM_CODE IS NIET GEVULD:
  OPMERKING 27-3-2015: TABEL OPENB_RMTE_WNPLTS IS NIET GEVULD, VOLGENS OVERZICHT B3 WEL
  rsgb=# select count(*), case when fk_7gem_code is null then 0 else -1 end as "lege velden 0" from gem_openb_rmte group by fk_7gem_code;
  count  | lege velden 0
  --------+---------------
  272942 |             0
  (1 row)
  ACTIE 27-3-2015: B3P MAAKT HERSTELSCRIPT VOOR JUISTE VULLING GEMEENTETABEL EN KOPPELING NAAR ANDERE TABELLEN
  STEEKPROEF O.B.V. VOORBEELDPERCEEL ALLEMEKINDERS
  select * from nummeraand where sc_identif='687200000018461'
  select * from addresseerb_obj_aand where identif='687200000018461'
  select * from openb_rmte where identifcode='687300000000433'
  select * from openb_rmte_gem_openb_rmte where fk_nn_lh_opr_identifcode = '687300000000433'
  select * from gem_openb_rmte where identifcode='687300000000433'
  select * from pv_adr_object_nummeraand where identif='687200000018461'
  OPMERKING 27-3-2015: DE NAAM_OPENB_RMTE IS GEVULD MET STRAATNAAM. DIT KLOPT, IS CONFORM BRONBESTAND.
  OPMERKING 27-3-2015: IN DE VIEW ZIJN OUTER JOINS GEBRUIKT ZODAT DE VIEW ALLE RECORDS BEVAT VAN ADDRESSEERB_OBJ_AAND
  OPMERKING 30-04-2015: IN DE TABEL NUMMERAAND IS HET VELD INDIC_HOOFDADRES NIET INGEVULD. HIERDOOR IS HET NIET DUIDELIJK WAT DE NEVENADRESSEN ZIJN.
  ACTIE 27-3-2015: HERNOEM VIEW NAAR PV_ADR_OBJECT_NUMMERAAND, GEOM KOLOMMEN WEGHALEN MBT WOONPLAATS, GEREED
  OPMERKING 05-07-2015: OPENB_RMTE_WNPLTS IS ALLEEN VOOR DE IDENTIFIER GEVULD, DAT BETEKENT DAT IN EEN STRAAT HUISNUMMERRANGES NIET ONDERSCHEIDEND ZIJN; IN HET GEVAL EEN STRAAT IN TWEE WOONPLAATSEN ZULLEN ER TWEE RECORDS BIJ EEN OBJECT OPGEHAALD WORDEN. OPLOSSING ZOU MOETEN ZIJN DAT HUISNUMMERRANGES ERVOOR ZORGEN DAT EEN WOONPLAATS PER OBJECT OPGEHAALD WORDT.
  ************************************************************/
CREATE OR REPLACE VIEW pv_adr_object_nummeraand
AS
  SELECT a.identif,
    a.dat_beg_geldh,
    a.dat_eind_geldh,
    a.huinummer,
    a.huinummertoevoeging,
    a.huisletter,
    a.postcode,
    d.naam_openb_rmte,
    --    d.straatnaam,
    g.code AS gem_code,
    --  g.geom AS gem_geom,
    g.naam AS gem_naam,
    --  f.geom AS wpl_geom,
    f.naam AS wpl_naam,
    n.indic_hoofdadres,
    n.status
  FROM nummeraand n
  JOIN addresseerb_obj_aand a
  ON n.sc_identif = a.identif
  JOIN openb_rmte b
  ON a.fk_7opr_identifcode = b.identifcode
  JOIN openb_rmte_gem_openb_rmte c
  ON b.identifcode = c.fk_nn_lh_opr_identifcode
  JOIN gem_openb_rmte d
  ON c.fk_nn_rh_gor_identifcode = d.identifcode
  JOIN openb_rmte_wnplts e
  ON b.identifcode = e.fk_nn_lh_opr_identifcode
  JOIN wnplts f
  ON e.fk_nn_rh_wpl_identif = f.identif
  JOIN gemeente g
  ON f.fk_7gem_code = g.code;
  /**************************************************
  VIEW PV_PAND, BRON BAG
  select * from pv_pand where identif = '687100000025186';
  ACTIE 27-3-2015: VOLGORDE KOLOMMEN CHECKEN, UITCOMMENTARIEREN WAT NIET MEEGENOMEN WORDT, GEREED
  ***************************************************/
CREATE OR REPLACE VIEW pv_pand
AS
  SELECT a.dat_beg_geldh,
    a.identif,
    --a.bruto_inhoud,
    a.datum_einde_geldh,
    --a.hoogste_bouwlaag,
    --a.identif_bgtpnd,
    a.indic_geconstateerd,
    --a.inwwijze_geom_bovenaanzicht
    --a.inwwijze_geom_maaiveld
    --a.laagste_bouwlaag,
    a.oorspronkelijk_bouwjaar,
    --a.oppervlakte,
    a.status,
    --a.relve_hoogteligging,
    --a.status_voortgang_bouw,
    a.geom_bovenaanzicht
    --a.geom_maaiveld
  FROM pand a;
  /**************************************************
  VIEW PV_VERBLIJFSOBJECT, BRON BAG
  select * from pv_pand where identif = '687100000025186';
  VIEW PV_VERBLIJFSOBJECT, BRON BAG
  select * from pv_verblijfsobject where sc_identif ='687010000018461'
  OPMERKING 27-3-2015: OPPERVLAKTE_OBJ IS OVERAL LEEG.
  OPMERKING 27-3-2015: AANTALLEN WIJKEN AF:
  select count(*) from benoemd_obj where clazz='verblijfsobject';
  9.004.517
  select count(*) from verblijfsobj;
  8.333.021
  select count(*) from gebouwd_obj where clazz='verblijfsobject'
  8.998.295
  ACTIE 27-3-2015: B3P ONDERZOEKT DE OORZAAK VAN AFWIJKENDE AANTALLEN
  ***************************************************/
  -- Gebruiksdoel zit via FK in aparte tabel
CREATE OR REPLACE VIEW pv_verblijfsobject
AS
  SELECT a.sc_identif,
    --    a.aantal_kamers,
    a.fk_11nra_sc_identif,
    --    a.hoogste_bouwlaag,
    a.indic_geconstateerd,
    --    a.laagste_bouwlaag,
    --    a.ontsluiting_verdieping,
    --    a.soort_woonobj,
    --    a.toegang_bouwlaag
    a.status, -- toegevoegd
    b.dat_beg_geldh,
    b.clazz,
    --         b.bouwk_best_act,
    --    b.bruto_inhoud,
    b.datum_einde_geldh,
    --    b.gebruiksdoel,
    --    b.inwwijze_oppervlakte,
    b.oppervlakte_obj,
    --    b.status_voortgang_bouw,
    --    b.vlakgeom,
    b.puntgeom
  FROM verblijfsobj a
  JOIN gebouwd_obj b
  ON a.sc_identif = b.sc_identif
    --     JOIN benoemd_obj c ON b.sc_identif::text = c.identif::text
    ;
  /***************************************************
  VIEW PV_VERBLIJFSOBJ_PAND, BRON BAG
  select * from pv_verblijfsobj_pand where fk_nn_rh_pnd_identif = '687100000025186';
  select * from pv_verblijfsobj_pand where fk_nn_lh_vbo_sc_identif ='687010000018461';
  ***************************************************/
CREATE OR REPLACE VIEW pv_verblijfsobj_pand
AS
  SELECT a.fk_nn_lh_vbo_sc_identif,
    a.fk_nn_lh_vbo_sc_dat_beg_geldh,
    a.fk_nn_rh_pnd_identif
  FROM verblijfsobj_pand a;
  /***************************************************
  VIEW PV_VERBLIJFSOBJ_NUMMERAAND, BRON BAG
  select * from pv_verblijfsobj_nummeraand limit 10;
  OPMERKING 27-3-2015: EXTRA VIEW MET NEVENADRESSEN VERBLIJFSOBJECT
  ***************************************************/
CREATE OR REPLACE VIEW pv_verblijfsobj_nummeraand
AS
  SELECT a.fk_nn_lh_vbo_sc_identif,
    a.fk_nn_lh_vbo_sc_dat_beg_geldh,
    a.fk_nn_rh_nra_sc_identif
  FROM verblijfsobj_nummeraand a;
  /***************************************************
  VIEW PV_GEBOUWD_OBJ_GEBRUIKSDOEL, BRON BAG
  select * from pv_gebouwd_obj_gebruiksdoel where fk_gbo_sc_identif ='687010000018461'
  OPMERKING: SOMMIGE OBJECTEN HEBBEN MEERDERE GEBRUIKSDOELEN, VANDAAR DE APARTE VIEW
  select * from gebouwd_obj where sc_identif='10010000050028'
  select * from gebouwd_obj_gebruiksdoel where fk_gbo_sc_identif ='10010000050028'
  ***************************************************/
CREATE OR REPLACE VIEW pv_gebouwd_obj_gebruiksdoel
AS
  SELECT gebruiksdoel_gebouwd_obj,
    fk_gbo_sc_identif
  FROM gebouwd_obj_gebruiksdoel ;
  /***************************************************
  VIEW PV_BENOEMD_OBJECT, BRON BAG
  select * from pv_benoemd_object where identif ='687010000018461'
  ***************************************************/
CREATE OR REPLACE VIEW pv_benoemd_object
AS
  SELECT a.identif, a.clazz FROM benoemd_obj a ;
  /***************************************************
  VIEW PV_GEBOUWD_OBJECT, BRON BAG
  select * from pv_gebouwd_object where sc_identif ='687010000018461'
  OPMERKKING 27-3-2015: JOIN MET BENOEMD_OBJ IS VERVALLEN, GEEN MEERWAARDE
  ***************************************************/
CREATE OR REPLACE VIEW pv_gebouwd_object
AS
  SELECT a.dat_beg_geldh,
    a.sc_identif,
    a.clazz,
    --     a.bouwk_best_act,
    --      a.bruto_inhoud,
    a.datum_einde_geldh,
    --      a.gebruiksdoel,
    --      a.inwwijze_oppervlakte,
    a.oppervlakte_obj,
    --      a.status_voortgang_bouw,
    a.vlakgeom,
    a.puntgeom
  FROM gebouwd_obj a ;
  /***************************************************
  VIEW PV_BENOEMD_TERREIN, BRON BAG
  select * from pv_benoemd_terrein limit 10;
  OPMERKKING 27-3-2015: JOIN MET BENOEMD_OBJ IS VERVALLEN, GEEN MEERWAARDE
  ***************************************************/
CREATE OR REPLACE VIEW pv_benoemd_terrein
AS
  SELECT a.dat_beg_geldh,
    a.sc_identif,
    a.clazz,
    a.datum_einde_geldh,
    a.geom AS bt_geom
  FROM benoemd_terrein a ;
  /***************************************************
  VIEW PV_STANDPLAATS, BRON BAG
  select * from pv_standplaats limit 10;
  OPMERKKING 27-3-2015: JOIN MET BENOEMD_OBJ IS VERVALLEN, GEEN MEERWAARDE
  ***************************************************/
CREATE OR REPLACE VIEW pv_standplaats
AS
  SELECT a.sc_identif,
    a.indic_geconst,
    a.status,
    a.fk_4nra_sc_identif,
    b.dat_beg_geldh,
    b.clazz,
    b.datum_einde_geldh,
    b.geom AS bt_geom
  FROM standplaats a
  JOIN benoemd_terrein b
  ON a.sc_identif = b.sc_identif;
  /***************************************************
  VIEW PV_STANDPLAATS_NUMMERAAND, BRON BAG
  select * from pv_standplaats_nummeraand limit 10;
  OPMERKING 27-3-2015: EXTRA VIEW MET NEVENADRESSEN STANDPLAATS
  ***************************************************/
CREATE OR REPLACE VIEW pv_standplaats_nummeraand
AS
  SELECT a.fk_nn_lh_spl_sc_identif,
    a.fk_nn_lh_spl_sc_dat_beg_geldh,
    a.fk_nn_rh_nra_sc_identif
  FROM standplaats_nummeraand a;
  /***************************************************
  VIEW PV_LIGPLAATS, BRON BAG
  select * from pv_ligplaats limit 10;
  ***************************************************/
CREATE OR REPLACE VIEW pv_ligplaats
AS
  SELECT a.sc_identif,
    a.indic_geconst,
    a.status,
    a.fk_4nra_sc_identif,
    b.dat_beg_geldh,
    b.clazz,
    b.datum_einde_geldh,
    b.geom AS bt_geom
  FROM ligplaats a
  JOIN benoemd_terrein b
  ON a.sc_identif = b.sc_identif;
  /***************************************************
  VIEW PV_LIGPLAATS_NUMMERAAND, BRON BAG
  select * from pv_ligplaats_nummeraand limit 10;
  OPMERKING 27-3-2015: EXTRA VIEW MET NEVENADRESSEN LIGPLAATS
  ***************************************************/
CREATE OR REPLACE VIEW pv_ligplaats_nummeraand
AS
  SELECT a.fk_nn_lh_lpl_sc_identif,
    a.fk_nn_lh_lpl_sc_dat_beg_geldh,
    a.fk_nn_rh_nra_sc_identif
  FROM ligplaats_nummeraand a;
  /***************************************************
  VIEW PV_KAD_PERCEEL, BRON BRK
  select * from pv_kad_perceel limit 10;
  select * from pv_kad_perceel where kad_identif=2650034670000
  gemcode in BRK heeft geen status. Via de BAG opzoeken
  ACTIE 27-3-2015: APARTE INFO VIEW MAKEN OM GEM CODE OP TE HALEN, NOG EEN PROBLEEM MET VOORLOOPNUL IN KOPPELVIEW BAG EN BRK, VRAAG B3. ACTIE PAS NA OPLOSSING DIT PROBLEEM.
  ***************************************************/
CREATE OR REPLACE VIEW pv_kad_perceel
AS
  SELECT a.cu_aard_bebouwing,
    b.sc_kad_identif,
    b.aand_soort_grootte,
    b.grootte_perceel,
    b.omschr_deelperceel,
    --b.fk_7kdp_sc_kad_identif            --recursieve relatie niet gevuld
    --    b.ka_deelperceelnummer,
    b.ka_kad_gemeentecode,
    b.ka_perceelnummer,
    b.ka_sectie,
    b.begrenzing_perceel,
    b.plaatscoordinaten_perceel,
    a.cu_aard_cultuur_onbebouwd,
    --    a.cu_meer_culturen,
    a.dat_beg_geldh,
    a.datum_einde_geldh,
    a.kad_identif,
    --    a.ks_aard_bedrag,
    a.ks_bedrag,
    a.ks_koopjaar,
    a.ks_meer_onroerendgoed
    --    a.ks_transactiedatum,
    --    a.ks_valutasoort,
    --    a.lo_cultuur_bebouwd,
    --    a.lo_loc__omschr,
    --    a.lr_aand_aard_liproject,
    --    a.lr_aard_bedrag,
    --    a.lr_bedrag,
    --    a.lr_eindjaar,
    --    a.lr_valutasoort,
    --    a.typering,
  FROM kad_perceel b
  JOIN kad_onrrnd_zk a
  ON b.sc_kad_identif = a.kad_identif;
  /***************************************************
  VIEW PV_KAD_ONROERENDE_ZAAK, BRON BRK
  select * from pv_kad_onroerende_zaak limit 10;
  select * from pv_kad_onroerende_zaak where kad_identif=2650034670000
  ***************************************************/
CREATE OR REPLACE VIEW pv_kad_onroerende_zaak
AS
  SELECT
    --  a.cu_aard_bebouwing,
    a.cu_aard_cultuur_onbebouwd,
    --    a.cu_meer_culturen,
    a.dat_beg_geldh,
    a.datum_einde_geldh,
    a.kad_identif,
    --    a.ks_aard_bedrag,
    a.ks_bedrag,
    a.ks_koopjaar,
    a.ks_meer_onroerendgoed
    --    a.ks_transactiedatum,
    --    a.ks_valutasoort,
    --    a.lo_cultuur_bebouwd,
    --    a.lo_loc__omschr,
    --    a.lr_aand_aard_liproject,
    --    a.lr_aard_bedrag,
    --    a.lr_bedrag,
    --    a.lr_eindjaar,
    --    a.lr_valutasoort,
    --    a.typering,
    --    a.fk_10pes_sc_identif as prs_identifier
  FROM kad_onrrnd_zk a ;
  /***************************************************
  VIEW PV_KAD_ONR_ZK_HIS_REL, BRON BRK
  select * from pv_kad_onr_zk_his_rel limit 10;
  select * from pv_kad_onr_zk_his_rel where fk_sc_lh_koz_kad_identif=2650034670000
  ***************************************************/
CREATE OR REPLACE VIEW pv_kad_onr_zk_his_rel
AS
  SELECT a.fk_sc_lh_koz_kad_identif,
    a.fk_sc_rh_koz_kad_identif,
    a.aard,
    a.overgangsgrootte
  FROM kad_onrrnd_zk_his_rel a ;
  /***************************************************
  VIEW PV_KAD_ONR_ZK_AANTEK, BRON BRK
  select * from pv_kad_onr_zk_aantek limit 10;
  select * from pv_kad_onr_zk_aantek where fk_4koz_kad_identif=2650034670000
  ***************************************************/
CREATE OR REPLACE VIEW pv_kad_onr_zk_aantek
AS
  SELECT a.begindatum_aantek_kad_obj,
    a.kadaster_identif_aantek,
    a.aard_aantek_kad_obj,
    a.beschrijving_aantek_kad_obj,
    a.eindd_aantek_kad_obj,
    a.fk_4koz_kad_identif
    --    a.fk_5pes_sc_identif
  FROM kad_onrrnd_zk_aantek a
  WHERE a.beschrijving_aantek_kad_obj IS NOT NULL ;
  /***************************************************
  VIEW PV_ZAKELIJK_RECHT, BRON BRK
  select * from pv_zakelijk_recht limit 10;
  select * from pv_zakelijk_recht where fk_7koz_kad_identif=2650034670000
  ***************************************************/
CREATE OR REPLACE VIEW pv_zakelijk_recht
AS
  SELECT a.kadaster_identif,
    a.eindd_recht,
    a.indic_betrokken_in_splitsing,
    a.ingangsdatum_recht,
    a.fk_7koz_kad_identif,
    a.fk_8pes_sc_identif,
    a.ar_noemer,
    a.ar_teller,
    a.fk_2aard_recht_verkort_aand,
    ar.omschr arv_omschr,
    a.fk_3avr_aand
  FROM zak_recht a
  JOIN aard_recht_verkort ar
  ON ar.aand = a.fk_3avr_aand;
  /***************************************************
  VIEW PV_ZAKELIJK_RECHT_AANTEKENING, BRON BRK
  select * from pv_zakelijk_recht_aantekening limit 10;
  ***************************************************/
CREATE OR REPLACE VIEW pv_zakelijk_recht_aantekening
AS
  SELECT a.kadaster_identif_aantek_recht,
    a.aard_aantek_recht,
    a.begindatum_aantek_recht,
    a.beschrijving_aantek_recht,
    a.eindd_aantek_recht,
    a.fk_5zkr_kadaster_identif
    --        a.fk_6pes_sc_identif
  FROM zak_recht_aantek a;
  /***************************************************
  VIEW PV_APPARTEMENTSRECHT, BRON BRK
  select * from pv_appartementsrecht limit 10;
  ACTIE 27-3-2015: PARKEREN, TZT SAMEN MET B3P VERDER UITWERKEN
  ***************************************************/
CREATE OR REPLACE VIEW pv_appartementsrecht
AS
  SELECT b.ka_appartementsindex,
    b.ka_kad_gemeentecode,
    b.ka_perceelnummer,
    b.ka_sectie,
    b.sc_kad_identif,
    --    a.cu_aard_bebouwing,
    a.cu_aard_cultuur_onbebouwd,
    --    a.cu_meer_culturen,
    a.dat_beg_geldh,
    a.datum_einde_geldh,
    --    a.fk_10pes_sc_identif,
    --    a.ks_aard_bedrag,
    a.ks_bedrag,
    a.ks_koopjaar,
    a.ks_meer_onroerendgoed
    --    a.ks_transactiedatum,
    --    a.ks_valutasoort,
    --    a.lo_cultuur_bebouwd,
    --    a.lo_loc__omschr,
    --    a.lr_aand_aard_liproject,
    --    a.lr_aard_bedrag,
    --    a.lr_bedrag,
    --    a.lr_eindjaar,
    --    a.lr_valutasoort,
    --    a.typering
  FROM kad_onrrnd_zk a
  JOIN app_re b
  ON a.kad_identif = b.sc_kad_identif;
  /***************************************************
  VIEW PV_PERSOON, BRON RSGB
  select * from pv_persoon where sc_identif in ('NL.KAD.Persoon.133747084','NL.KAD.Persoon.133747052');
  ***************************************************/
CREATE OR REPLACE VIEW pv_persoon
AS
  SELECT a.sc_identif, a.clazz FROM prs a ;
  /***************************************************
  VIEW PV_NATUURLIJK_PERSOON, BRON RSGB
  select * from pv_natuurlijk_persoon where sc_identif in ('NL.KAD.Persoon.133747084','NL.KAD.Persoon.133747052');
  ACTIE 27-3-2015: WAAR WORDT BSN NUMMER OPGESLAGEN INDIEN DEZE DOOR KADASTER WORDT AANGELEVERD? B3 ZOEKT DIT UIT.
  ACTIE 27-3-2015: AFWIJKENDE AANTALLEN: B3 GAAT DIT NA
  select count(*) from pv_natuurlijk_persoon;
  select count(*) from NAT_PRS;
  240134
  select count(*) from ANDER_NAT_PRS;
  select count(*) from pv_ander_natuurlijk_persoon;
  35625
  select count(*) from ingeschr_nat_prs;
  select count(*) from pv_ingeschr_natuurlijk_persoon;
  204775
  select clazz, COUNT(*) from nat_prs group by clazz;
  waarden:
  "NULL" (HOE KOMT DIT?): 1537
  "INGESCHREVEN NATUURLIJK PERSOON" 203130
  "ANDER NATUURLIJK PERSOON" 35467
  ***************************************************/
CREATE OR REPLACE VIEW pv_natuurlijk_persoon
AS
  SELECT a.sc_identif,
    a.clazz,
    a.aand_naamgebruik,
    a.geslachtsaand,
    a.nm_adellijke_titel_predikaat,
    a.nm_geslachtsnaam,
    a.nm_voornamen,
    a.nm_voorvoegsel_geslachtsnaam,
    a.na_aanhef_aanschrijving,
    a.na_geslachtsnaam_aanschrijving,
    a.na_voorletters_aanschrijving,
    a.na_voornamen_aanschrijving,
    fk_2acd_code,
    b.fax_nummer,
    b.identif,
    b.kvk_nummer,
    b.naam,
    b.telefoonnummer
  FROM prs c
  JOIN subject b
  ON c.sc_identif = b.identif
  JOIN nat_prs a
  ON a.sc_identif = c.sc_identif;
  /***************************************************
  VIEW PV_NATUURLIJK_PERSOON, BRON RSGB
  select * from pv_ingeschr_natuurlijk_persoon where sc_identif in ('NL.KAD.Persoon.133747084','NL.KAD.Persoon.133747052');
  ACTIE 27-3-2015: KOLOMMEN OPNEMEN VOOR OVERLIJDEN GEREED
  ACTIE 27-03-2015: OVERLIJDENSDATUM EN OVERLIJDENSPLAATS ZIJN NIET GEVULD B3P NEEMT VRAAG MEE NAAR KADASTER
  ***************************************************/
CREATE OR REPLACE VIEW pv_ingeschr_natuurlijk_persoon
AS
  SELECT sc_identif,
    gb_geboortedatum,
    gb_geboorteplaats,
    ol_overlijdensdatum,
    ol_overlijdensplaats,
    va_loc_beschrijving
  FROM ingeschr_nat_prs;
  /***************************************************
  VIEW PV_NIET_INGEZETENE, BRON RSGB
  ***************************************************/
CREATE OR REPLACE VIEW pv_niet_ingezetene
AS
  SELECT sc_identif FROM niet_ingezetene;
  /***************************************************
  VIEW PV_ANDER_NATUURLIJK_PERSOON, BRON RSGB
  select * from pv_ander_natuurlijk_persoon limit 10;
  ***************************************************/
CREATE OR REPLACE VIEW pv_ander_natuurlijk_persoon
AS
  SELECT sc_identif, geboortedatum, overlijdensdatum FROM ander_nat_prs;
  /***************************************************
  VIEW PV_NIET_NATUURLIJK_PERSOON, BRON RSGB
  select * from niet_nat_prs where sc_identif in ('NL.KAD.Persoon.133747084','NL.KAD.Persoon.133747052');
  ***************************************************/
CREATE OR REPLACE VIEW pv_niet_natuurlijk_persoon
AS
  SELECT a.sc_identif,
    a.clazz,
    a.naam AS nnp_naam,
    -- a.datum_aanvang,
    -- a.datum_beeindiging,
    -- a.verkorte_naam,
    b.kvk_nummer,
    b.naam AS su_naam,
    b.telefoonnummer,
    b.typering,
    d.rechtsvorm,
    d.statutaire_zetel
  FROM prs c
  JOIN subject b
  ON c.sc_identif = b.identif
  JOIN niet_nat_prs a
  ON a.sc_identif = c.sc_identif
  LEFT JOIN ingeschr_niet_nat_prs d
  ON d.sc_identif = a.sc_identif;
  /***************************************************
  VIEW PV_BENOEMD_OBJ_KAD_ONR_ZK, BRON RSGB
  OPMERKING 31-3-2015: NIEUWE VIEW IVM KOPPELING BAG BRK
  select * from pv_benoemd_obj_kad_onr_zk where fk_nn_rh_koz_kad_identif ='2650034670000';
  fk_nn_lh_tgo_identif | fk_nn_rh_koz_kad_identif
  ----------------------+--------------------------
  0687010000018461     |            2650034670000
  (1 row)
  rsgb=>
  OPMERKING 31-3-2015, VRAAG AAN B3: IN EERSTE VELD ZIT EEN VOORLOOPNUL, IN DE TABEL VAN DE BAG BV BENOEMD_OBJ NIET.
  ***************************************************/
CREATE OR REPLACE VIEW pv_benoemd_obj_kad_onr_zk
AS
  SELECT a.fk_nn_lh_tgo_identif fk_nn_lh_tgo_identif,
    a.fk_nn_rh_koz_kad_identif
  FROM benoemd_obj_kad_onrrnd_zk a ;
  /***************************************************
  ALGEMENE OPMERKINGEN
  ACTIE 27-3-2015: ONDERZOEK CODE HYP4 OPNEMEN IN TABEL BRONDOCUMENT, VOORLOPIG GEEN ACTIE
  ACTIE 27-3-2015: nog maken: SAMENGESTELDE INFO VIEW VOOR PERSOON GEREED
  OPMERKING 27-3-2015: IS DE INFORMATIE IN TABEL BRONDOCUMENT RELEVANT?: NIET RELEVANT VOORLOPIG NIET OPNEMEN
  select * from brondocument where tabel ='BRONDOCUMENT' LIMIT 10;
  select * from brondocument where tabel ='ZAK_RECHT' AND tabel_identificatie='NL.KAD.Tenaamstelling.AKR1.216108'
  select * from brondocument where tabel ='KAD_ONRRND_ZAAK_AANTEK' LIMIT 10;
  select * from brondocument where tabel ='APP_RE' LIMIT 10;
  select * from brondocument where tabel ='KAD_PERCEEL' LIMIT 10;
  select * from brondocument where tabel ='KAD_PERCEEL' and tabel_identificatie='2650034670000' => Kadasterstukken
  OPMERKING 27-3-2015: IS DE INFORMATIE IN TABEL HERKOMST_METADATA RELEVANT, BIJV. MEENEMEN IN SNAPSHOTS OF RECORD COMFORTDATA OF NIET:
  MEENEMEN ZODRA NHR WORDT UITGEWERKT
  ACTIE 27-3-2015: Is de informatie in tabel KAD_ONRRND_ZK_AANTEK relevant? VIEW NOG MAKEN GEREED
  select * from kad_onrrnd_zk_aantek where fk_4koz_kad_identif=2650034670000 limit 20;
  ACTIE 27-3-2015: Is de informatie in tabel KAD_ONRRND_ZK_HIS_REL relevant? VIEW NOG MAKEN GEREED
  select * from kad_onrrnd_zk_his_rel;
  select * from niet_nat_prs where sc_identif in ('NL.KAD.Persoon.133747084','NL.KAD.Persoon.133747052');
  OPMERKING 27-3-2015: Check META... tabellen op relevantie RSGB gaat hier niet consistent mee om, opnieuw bekijken zodra RSGB3.0 wordt geimplementeerd
  OPMERKING 27-3-2015: WAAR WORDT BSN NUMMER OPGESLAGEN INDIEN DEZE DOOR KADASTER WORDT AANGELEVERD? NU: COMMENT OUT IN DATAMODEL. B3P ZOEKT DIT UIT
  OPMERKING 27-3-2015: TABEL AARD_VERKREGEN_RECHT IS LEEG, VOLGENS OVERZICHT B3 GEVULD, B3 HEEFT GECHECKT; VULLING NEGEREN INHOUD IS IDENTIEK AAN AARD_RECHT_VERKORT, VOORALSNOG GEEN VIEW NODIG GEREED
  ACTIE 27-3-2015: AARD_RECHT_VERKORT: 1 OP 1 RELATIE MET ZAK_RECHT, HIERIN VELDEN OPNEMEN; INTEGRAAL OPGENOMEN  IN VIEW PV_ZAKELIJK_RECHT. GEREED.
  ACTIE 27-3-2015: WELKE TABELLEN MISSEN WE NOG? KOPPELTABEL BAG - BRK? VIEW NOG MAKEN: PV_BENOEMD_OBJ_KAD_ONRRND_ZK, GEREED
  FUNCTIONEEL ONTWERP MAKEN VOOR ONTSLUITING VIA PV_INFOVIEWS, REKENING HOUDENDE MET VERTROUWELIJKHEDEN, IN BEHANDELING
  ***************************************************/
  /**************************************************
  Aangemaakte views worden voorzien van aliassen voor gebruik in benaming van indexen en keys. Synonymen worden in postgresql standaard niet ondersteund.
  **************************************************/
  COMMENT ON TABLE pv_adr_object_nummeraand
IS
  'aliased as aon, uses addresseerb_obj, openb_rmte, openb_rmte_gem_openb_rmte, gem_openb_rmte, wnplts, gemeente';
  COMMENT ON TABLE pv_ander_natuurlijk_persoon
IS
  'aliased as anp, uses ander_nat_prs';
  COMMENT ON TABLE pv_appartementsrecht
IS
  'aliased as apr, uses pv_appartementsrecht';
  COMMENT ON TABLE pv_benoemd_obj_kad_onr_zk
IS
  'aliased as bok, uses benoemd_obj_kad_onrrnd_zk';
  COMMENT ON TABLE pv_benoemd_object
IS
  'aliased as bob, uses benoemd_obj';
  COMMENT ON TABLE pv_benoemd_terrein
IS
  'aliased as bte, uses benoemd_terrein';
  COMMENT ON TABLE pv_gebouwd_obj_gebruiksdoel
IS
  'aliased as gog, uses gebouwd_obj_gebruiksdoel';
  COMMENT ON TABLE pv_gebouwd_object
IS
  'aliased as gob, uses gebouwd_obj';
  COMMENT ON TABLE pv_gemeente
IS
  'aliased as gem, uses gemeente';
  COMMENT ON TABLE pv_ingeschr_natuurlijk_persoon
IS
  'aliased as inp, uses ingeschr_nat_prs';
  COMMENT ON TABLE pv_kad_onr_zk_aantek
IS
  'aliased as koa, uses kad_onrrnd_zk_aantek';
  COMMENT ON TABLE pv_kad_onr_zk_his_rel
IS
  'aliased as koh, uses kad_onrrnd_zk_his_rel';
  COMMENT ON TABLE pv_kad_onroerende_zaak
IS
  'aliased as koz, uses kad_onrrnd_zk';
  COMMENT ON TABLE pv_kad_perceel
IS
  'aliased as kpe, uses kad_perceel, kad_onrrnd_zk';
  COMMENT ON TABLE pv_ligplaats
IS
  'aliased as lip, uses ligplaats, benoemd_terrein';
  COMMENT ON TABLE pv_ligplaats_nummeraand
IS
  'aliased as lin, uses ligplaats_nummeraand';
  COMMENT ON TABLE pv_natuurlijk_persoon
IS
  'aliased as npe, uses prs, subject, nat_prs';
  COMMENT ON TABLE pv_niet_ingezetene
IS
  'aliased as nig, uses niet_ingezetene';
  COMMENT ON TABLE pv_niet_natuurlijk_persoon
IS
  'aliased as nnp, uses subject, niet_nat_prs, ingeschr_niet_nat_prs';
  COMMENT ON TABLE pv_pand
IS
  'aliased as pnd, uses pand';
  COMMENT ON TABLE pv_persoon
IS
  'aliased as pso, uses prs';
  COMMENT ON TABLE pv_standplaats
IS
  'aliased as stp, uses standplaats, benoemd_terrein';
  COMMENT ON TABLE pv_standplaats_nummeraand
IS
  'aliased as stn, uses standplaats_nummeraand';
  COMMENT ON TABLE pv_verblijfsobj_nummeraand
IS
  'aliased as von, uses verblijfsobj_nummeraand';
  COMMENT ON TABLE pv_verblijfsobj_pand
IS
  'aliased as vop, uses verblijfsobj_pand';
  COMMENT ON TABLE pv_verblijfsobject
IS
  'aliased as vob, uses verblijfsobj, gebouwd_obj';
  COMMENT ON TABLE pv_woonplaats
IS
  'aliased as wpl, uses wnplts, gemeente';
  COMMENT ON TABLE pv_zakelijk_recht
IS
  'aliased as zre, uses zak_recht, aard_recht_verkort';
  COMMENT ON TABLE pv_zakelijk_recht_aantekening
IS
  'aliased as zra, uses zak_recht_aantek';
  --tabel functionaris wordt niet gevuld
  --create or replace view pv_functionaris as
  -- SELECT a.beperking_bev_in_euros,
  --    a.bev_met_andere_prsn,
  --    a.bv_beperking_in_geld,
  --    a.Bv_omschr_ovrg_beperkingen,
  --    a.bv_ovrg_volmacht,
  --    a.bv_soort_handeling,
  --    a.datum_toetr,
  --    a.datum_uittreding,
  --    a.fk_sc_lh_pes_sc_identif,
  --    a.fk_sc_rh_pes_sc_identif,
  --    a.functie,
  --    a.functionaristypering,
  --    a.indic_statutair_volmacht,
  --    a.ovrg_beperking_bev,
  --    a.soort_bev,
  --    a.volledig_beperkt_volmacht
  --   FROM functionaris a;
  --Geen nuttige query omdat vestiging niet vanuit BRK wordt
  --gevuld en de rest uit een tabel komt.
  --create or replace view pv_vestiging as
  -- SELECT a.adres_binnenland,
  --    a.adres_buitenland,
  --    a.emailadres,
  --    a.fax_nummer,
  --    a.kvk_nummer,
  --    a.naam AS sb_naam,
  --    a.pa_postadres_postcode,
  --    a.pa_postadrestype,
  --    a.pa_postbus__of_antwoordnummer,
  --    a.rn_bankrekeningnummer,
  --    a.rn_bic,
  --    a.rn_iban,
  --    a.telefoonnummer,
  --    a.typering AS sb_typ,
  --    a.vb_adres_buitenland_1,
  --    a.vb_adres_buitenland_2,
  --    a.vb_adres_buitenland_3,
  --    a.website_url,
  --    b.activiteit_omschr,
  --    b.datum_aanvang,
  --    b.datum_beeindiging,
  --    b.datum_voortzetting,
  --    b.fulltime_werkzame_mannen,
  --    b.fulltime_werkzame_vrouwen,
  --    b.naam AS vg_naam,
  --    b.parttime_werkzame_mannen,
  --    b.parttime_werkzame_vrouwen,
  --    b.sa_indic_hoofdactiviteit,
  --    b.sc_identif,
  --    b.toevoeging_adres,
  --    b.typering AS vg_typ,
  --    b.verkorte_naam
  --   FROM subject a
  --     JOIN vestg b ON a.identif::text = b.sc_identif::text;
  --Zowel woz-waarde als woz_obj worden niet vanuit
  --BRK of BAG gevuld
  --create or replace view pv_wozwaarde as
  -- SELECT a.dat_beg_geldh,
  --    a.datum_einde_geldh,
  --    a.gebruikscode,
  --    a.geom,
  --    a.grondoppervlakte,
  --    a.nummer,
  --    a.soort_obj_code,
  --    a.vastgestelde_waarde AS wo_vastg_w,
  --    a.waardepeildatum,
  --    b.status_beschikking,
  --    b.toestandspeildatum,
  --    b.vastgestelde_waarde AS ww_vastg_w
  --   FROM woz_waarde b
  --     JOIN woz_obj a ON b.fk_1woz_nummer = a.nummer;
