-- veranderd het search_path in de database naar het public schema om aan het public schema de nieuwe materialized views toe te voegen.
-- Via de BRMO GUI kunnen deze materialized views vervolgens automatisch ververst worden.
set search_path = public,bag;

-- Vervangt mb_adres. Gemeentevelden zijn nog niet beschikbaar.
create materialized view mb_adres_bag as
select na.objectid,
       na.identificatie  as identificatienummeraanduiding,
       na.status,
       na.begingeldigheid,
       null              as gemeente,    -- Gemeente-woonplaats relatie nog niet beschikbaar (BRMO-104)
       wp.naam           as woonplaats,
       opr.naam          as straatnaam,
       na.huisnummer,
       na.huisletter,
       na.huisnummertoevoeging,
       na.postcode,
       opr.identificatie as identificatieopenbareruimte,
       wp.identificatie  as identificatiewoonplaats,
       null              as gemeentecode -- Gemeente-woonplaats relatie nog niet beschikbaar (BRMO-104)
from v_nummeraanduiding_actueel na
         left join v_openbareruimte_actueel opr on (opr.identificatie = na.ligtaan)
         left join v_woonplaats_actueel wp on (wp.identificatie = opr.ligtin)
with no data;

create index mb_adres_bag_identificatie on mb_adres_bag using btree (identificatienummeraanduiding);
create unique index mb_adres_bag_objectid on mb_adres_bag using btree (objectid);
comment on materialized view mb_adres_bag is 'volledig actueel adres zonder locatie';


-- maakt een materialized view van vb_adresseerbaar_object_geometrie tbv performance bij zeer grote datasets.
create materialized view mb_adresseerbaar_object_geometrie_bag as
select (row_number() over ())::integer as objectid,
       qry.ishoofdadres,
       qry.status,
       qry.identificatie,
       qry.identificatienummeraanduiding,
       qry.nummeraanduidingstatus,
       qry.gemeente,
       qry.woonplaats,
       qry.straatnaam,
       qry.huisnummer,
       qry.huisletter,
       qry.huisnummertoevoeging,
       qry.postcode,
       qry.soort,
       qry.maaktdeeluitvan,
       qry.gebruiksdoelen,
       qry.oppervlakte,
       qry.geometrie_centroide,
       qry.geometrie
from (select vla.ishoofdadres,
             vla.status,
             vla.identificatie,
             vla.identificatienummeraanduiding,
             vla.nummeraanduidingstatus,
             vla.gemeente,
             vla.woonplaats,
             vla.straatnaam,
             vla.huisnummer,
             vla.huisletter,
             vla.huisnummertoevoeging,
             vla.postcode,
             'ligplaats' as soort,
             null        as maaktdeeluitvan,
             null        as gebruiksdoelen,
             null        as oppervlakte,
             vla.geometrie_centroide,
             vla.geometrie
      from bag.vb_ligplaats_adres vla
      union all
      select vsa.ishoofdadres,
             vsa.status,
             vsa.identificatie,
             vsa.identificatienummeraanduiding,
             vsa.nummeraanduidingstatus,
             vsa.gemeente,
             vsa.woonplaats,
             vsa.straatnaam,
             vsa.huisnummer,
             vsa.huisletter,
             vsa.huisnummertoevoeging,
             vsa.postcode,
             'standplaats' as soort,
             null          as maaktdeeluitvan,
             null          as gebruiksdoelen,
             null          as oppervlakte,
             vsa.geometrie_centroide,
             vsa.geometrie
      from bag.vb_standplaats_adres vsa
      union all
      select vva.ishoofdadres,
             vva.status,
             vva.identificatie,
             vva.identificatienummeraanduiding,
             vva.nummeraanduidingstatus,
             vva.gemeente,
             vva.woonplaats,
             vva.straatnaam,
             vva.huisnummer,
             vva.huisletter,
             vva.huisnummertoevoeging,
             vva.postcode,
             'verblijfsobject'     as soort,
             vva.maaktdeeluitvan,
             vva.gebruiksdoelen,
             vva.oppervlakte::text as oppervlakte,
             vva.geometrie_centroide,
             vva.geometrie
      from bag.vb_verblijfsobject_adres vva) qry
with no data;

create index mb_adresseerbaar_object_geometrie_bag_geom on mb_adresseerbaar_object_geometrie_bag using gist (geometrie);
create index mb_adresseerbaar_object_geometrie_bag_centroid on mb_adresseerbaar_object_geometrie_bag using gist (geometrie_centroide);
create index mb_adresseerbaar_object_geometrie_bag_identificatie on mb_adresseerbaar_object_geometrie_bag using btree (identificatienummeraanduiding);
create unique index mb_adresseerbaar_object_geometrie_bag_objectid on mb_adresseerbaar_object_geometrie_bag using btree (objectid);

comment on
    materialized view mb_adresseerbaar_object_geometrie_bag is 'alle adresseerbare objecten (ligplaatst, standplaats, verblijfsobject) met adres, gebruiksdoel, pand en (afgeleide) geometrie.';

--Maakt dezelfde materialized view aan als mb_kad_onrrnd_zk_adres, maar de BAG gegevens worden uit mb_adresseerbaar_object_geometrie_bag gehaald.
create materialized view mb_kad_onrrnd_zk_adres_bag as
select (row_number() over ())::integer                                                    as objectid,
       qry.identif                                                                        as koz_identif,
       koz.dat_beg_geldh                                                                  as begin_geldigheid,
       to_date((koz.dat_beg_geldh)::text, 'YYYY-MM-DD'::text)                             as begin_geldigheid_datum,
       bok.fk_nn_lh_tgo_identif                                                           as benoemdobj_identif,
       qry.type,
       (((coalesce(qry.ka_sectie, ''::character varying))::text || ' '::text) ||
        (coalesce(qry.ka_perceelnummer, ''::character varying))::text)                    as aanduiding,
       (((((((coalesce(qry.ka_kad_gemeentecode, ''::character varying))::text || ' '::text) ||
            (coalesce(qry.ka_sectie, ''::character varying))::text) || ' '::text) ||
          (coalesce(qry.ka_perceelnummer, ''::character varying))::text) || ' '::text) ||
        (coalesce(qry.ka_appartementsindex, ''::character varying))::text)                as aanduiding2,
       qry.ka_sectie                                                                      as sectie,
       qry.ka_perceelnummer                                                               as perceelnummer,
       qry.ka_appartementsindex                                                           as appartementsindex,
       qry.ka_kad_gemeentecode                                                            as gemeentecode,
       qry.aand_soort_grootte,
       (qry.grootte_perceel)::integer                                                     as grootte_perceel,
       st_area(qry.begrenzing_perceel)                                                    as oppervlakte_geom,
       qry.ka_deelperceelnummer                                                           as deelperceelnummer,
       qry.omschr_deelperceel,
       b.datum                                                                            as verkoop_datum,
       koz.cu_aard_cultuur_onbebouwd                                                      as aard_cultuur_onbebouwd,
       (koz.ks_bedrag)::integer                                                           as bedrag,
       koz.ks_koopjaar                                                                    as koopjaar,
       koz.ks_meer_onroerendgoed                                                          as meer_onroerendgoed,
       koz.ks_valutasoort                                                                 as valutasoort,
       koz.lo_loc__omschr                                                                 as loc_omschr,
       array_to_string((select array_agg((((((((((((((((((((('id: '::text ||
                                                             (coalesce(koza.kadaster_identif_aantek, ''::character varying))::text) ||
                                                            ', '::text) || 'aard: '::text) ||
                                                          (coalesce(koza.aard_aantek_kad_obj, ''::character varying))::text) ||
                                                         ', '::text) || 'begin: '::text) ||
                                                       (coalesce(koza.begindatum_aantek_kad_obj, ''::character varying))::text) ||
                                                      ', '::text) || 'beschrijving: '::text) ||
                                                    (coalesce(koza.beschrijving_aantek_kad_obj, ''::character varying))::text) ||
                                                   ', '::text) || 'eind: '::text) ||
                                                 (coalesce(koza.eindd_aantek_kad_obj, ''::character varying))::text) ||
                                                ', '::text) || 'koz-id: '::text) ||
                                              coalesce(koza.fk_4koz_kad_identif, (0)::numeric(15, 0))) || ', '::text) ||
                                            'subject-id: '::text) ||
                                           (coalesce(koza.fk_5pes_sc_identif, ''::character varying))::text) ||
                                          '; '::text)) as array_agg
                        from kad_onrrnd_zk_aantek koza
                        where (koza.fk_4koz_kad_identif = koz.kad_identif)), ' & '::text) as aantekeningen,
       maogb.identificatienummeraanduiding,
       maogb.nummeraanduidingstatus,
       maogb.gemeente,
       maogb.woonplaats,
       maogb.straatnaam,
       maogb.huisnummer,
       maogb.huisletter,
       maogb.huisnummertoevoeging,
       maogb.postcode,
       maogb.gebruiksdoelen,
       maogb.oppervlakte,
       st_x(st_transform(st_setsrid(st_centroid(qry.begrenzing_perceel), 28992), 4326))   as lon,
       st_y(st_transform(st_setsrid(st_centroid(qry.begrenzing_perceel), 28992), 4326))   as lat,
       qry.begrenzing_perceel
from (((((select p.sc_kad_identif                 as identif,
                 'perceel'::character varying(11) as type,
                 p.ka_sectie,
                 p.ka_perceelnummer,
                 null::character varying(4)       as ka_appartementsindex,
                 p.ka_kad_gemeentecode,
                 p.aand_soort_grootte,
                 p.grootte_perceel,
                 p.ka_deelperceelnummer,
                 p.omschr_deelperceel,
                 p.begrenzing_perceel
          from kad_perceel p
          union all
          select ar.sc_kad_identif                    as identif,
                 'appartement'::character varying(11) as type,
                 ar.ka_sectie,
                 ar.ka_perceelnummer,
                 ar.ka_appartementsindex,
                 ar.ka_kad_gemeentecode,
                 null::character varying(2)           as aand_soort_grootte,
                 null::numeric(8, 0)                  as grootte_perceel,
                 null::character varying(4)           as ka_deelperceelnummer,
                 null::character varying(1120)        as omschr_deelperceel,
                 kp.begrenzing_perceel
          from ((mb_util_app_re_kad_perceel v
              join kad_perceel kp on ((v.perceel_identif = kp.sc_kad_identif)))
                   join app_re ar on (((v.app_re_identif)::numeric = ar.sc_kad_identif)))) qry
    join kad_onrrnd_zk koz on ((koz.kad_identif = qry.identif)))
    left join benoemd_obj_kad_onrrnd_zk bok on ((bok.fk_nn_rh_koz_kad_identif = qry.identif)))
    left join mb_adresseerbaar_object_geometrie_bag maogb on (((bok.fk_nn_lh_tgo_identif)::bpchar = maogb.identificatie)))
         left join (select brondocument.ref_id,
                           max(brondocument.datum) as datum
                    from brondocument
                    where ((brondocument.omschrijving)::text = 'Akte van Koop en Verkoop'::text)
                    group by brondocument.ref_id) b on (((koz.kad_identif)::text = (b.ref_id)::text)))
with no data;

comment on materialized view mb_kad_onrrnd_zk_adres_bag is 'alle kadastrale onroerende zaken (perceel en appartementsrecht) met opgezochte verkoop datum, objectid voor geoserver/arcgis en BAG adres';
create index mb_kad_onrrnd_zk_adres_bag_begrenzing_perceel_idx on mb_kad_onrrnd_zk_adres_bag using gist (begrenzing_perceel);
create index mb_kad_onrrnd_zk_adres_bag_identif on mb_kad_onrrnd_zk_adres_bag using btree (koz_identif);
create unique index mb_kad_onrrnd_zk_adres_bag_objectid on mb_kad_onrrnd_zk_adres_bag using btree (objectid);

--Maakt dezelfde materialized view aan als mb_koz_rechth, maar de BAG gegevens worden uit mb_kad_onrrnd_zk_adres_bag gehaald.
create materialized view mb_koz_rechth_bag as
select row_number() over ()::integer                                as objectid,
       koz.koz_identif,
       koz.begin_geldigheid,
       to_date(koz.begin_geldigheid::text, 'YYYY-MM-DD'::text)      as begin_geldigheid_datum,
       koz.type,
       (coalesce(koz.sectie, ''::character varying)::text || ' '::text) ||
       coalesce(koz.perceelnummer, ''::character varying)::text     as aanduiding,
       (((((coalesce(koz.gemeentecode, ''::character varying)::text || ' '::text) ||
           coalesce(koz.sectie, ''::character varying)::text) || ' '::text) ||
         coalesce(koz.perceelnummer, ''::character varying)::text) || ' '::text) ||
       coalesce(koz.appartementsindex, ''::character varying)::text as aanduiding2,
       koz.sectie,
       koz.perceelnummer,
       koz.appartementsindex,
       koz.gemeentecode,
       koz.aand_soort_grootte,
       koz.grootte_perceel,
       koz.oppervlakte_geom,
       koz.deelperceelnummer,
       koz.omschr_deelperceel,
       koz.verkoop_datum,
       koz.aard_cultuur_onbebouwd,
       koz.bedrag,
       koz.koopjaar,
       koz.meer_onroerendgoed,
       koz.valutasoort,
       koz.loc_omschr,
       zrr.zr_identif,
       zrr.ingangsdatum_recht,
       zrr.subject_identif,
       zrr.aandeel,
       zrr.omschr_aard_verkregen_recht,
       zrr.indic_betrokken_in_splitsing,
       zrr.soort,
       zrr.geslachtsnaam,
       zrr.voorvoegsel,
       zrr.voornamen,
       zrr.aand_naamgebruik,
       zrr.geslachtsaand,
       zrr.naam,
       zrr.woonadres,
       zrr.geboortedatum,
       zrr.geboorteplaats,
       zrr.overlijdensdatum,
       zrr.bsn,
       zrr.organisatie_naam,
       zrr.rechtsvorm,
       zrr.statutaire_zetel,
       zrr.rsin,
       zrr.kvk_nummer,
       zrr.aantekeningen,
       koz.gemeente,
       koz.woonplaats,
       koz.straatnaam,
       koz.huisnummer,
       koz.huisletter,
       koz.huisnummertoevoeging,
       koz.postcode,
       koz.lon,
       koz.lat,
       koz.begrenzing_perceel
from mb_zr_rechth zrr
         right join mb_kad_onrrnd_zk_adres_bag koz on zrr.koz_identif = koz.koz_identif
with no data;

comment on materialized view mb_kad_onrrnd_zk_adres_bag is 'kadastrale percelen een appartementsrechten met rechten en rechthebbenden en objectid voor geoserver/arcgis';
-- View indexes:
create index mb_koz_rechth_bag_begrenzing_perceel_idx on mb_koz_rechth_bag using gist (begrenzing_perceel);
create index mb_koz_rechth_bag_identif on mb_koz_rechth_bag using btree (koz_identif);
create unique index mb_koz_rechth_bag_objectid on mb_koz_rechth_bag using btree (objectid);


--Maakt dezelfde materialized view aan als mb_avg_koz_rechth, maar de BAG gegevens worden uit mb_kad_onrrnd_zk_adres_bag gehaald.
create materialized view mb_avg_koz_rechth_bag as
select row_number() over ()::integer                                as objectid,
       koz.koz_identif,
       koz.begin_geldigheid,
       to_date(koz.begin_geldigheid::text, 'YYYY-MM-DD'::text)      as begin_geldigheid_datum,
       koz.type,
       (coalesce(koz.sectie, ''::character varying)::text || ' '::text) ||
       coalesce(koz.perceelnummer, ''::character varying)::text     as aanduiding,
       (((((coalesce(koz.gemeentecode, ''::character varying)::text || ' '::text) ||
           coalesce(koz.sectie, ''::character varying)::text) || ' '::text) ||
         coalesce(koz.perceelnummer, ''::character varying)::text) || ' '::text) ||
       coalesce(koz.appartementsindex, ''::character varying)::text as aanduiding2,
       koz.sectie,
       koz.perceelnummer,
       koz.appartementsindex,
       koz.gemeentecode,
       koz.aand_soort_grootte,
       koz.grootte_perceel,
       koz.oppervlakte_geom,
       koz.deelperceelnummer,
       koz.omschr_deelperceel,
       koz.verkoop_datum,
       koz.aard_cultuur_onbebouwd,
       koz.bedrag,
       koz.koopjaar,
       koz.meer_onroerendgoed,
       koz.valutasoort,
       koz.loc_omschr,
       zrr.zr_identif,
       zrr.ingangsdatum_recht,
       zrr.subject_identif,
       zrr.aandeel,
       zrr.omschr_aard_verkregen_recht,
       zrr.indic_betrokken_in_splitsing,
       zrr.soort,
       zrr.geslachtsnaam,
       zrr.voorvoegsel,
       zrr.voornamen,
       zrr.aand_naamgebruik,
       zrr.geslachtsaand,
       zrr.naam,
       zrr.woonadres,
       zrr.geboortedatum,
       zrr.geboorteplaats,
       zrr.overlijdensdatum,
       zrr.bsn,
       zrr.organisatie_naam,
       zrr.rechtsvorm,
       zrr.statutaire_zetel,
       zrr.rsin,
       zrr.kvk_nummer,
       zrr.aantekeningen,
       koz.gemeente,
       koz.woonplaats,
       koz.straatnaam,
       koz.huisnummer,
       koz.huisletter,
       koz.huisnummertoevoeging,
       koz.postcode,
       koz.lon,
       koz.lat,
       koz.begrenzing_perceel
from mb_avg_zr_rechth zrr
         right join mb_kad_onrrnd_zk_adres_bag koz on zrr.koz_identif = koz.koz_identif
with no data;

comment on materialized view mb_kad_onrrnd_zk_adres_bag is 'kadastrale percelen een appartementsrechten met rechten en rechthebbenden geschoond voor avg en objectid voor geoserver/arcgis';
-- View indexes:
create index mb_avg_koz_rechth_bag_begrenzing_perceel_idx on mb_avg_koz_rechth_bag using gist (begrenzing_perceel);
create index mb_avg_koz_rechth_bag_identif on mb_avg_koz_rechth_bag using btree (koz_identif);
create unique index mb_avg_koz_rechth_bag_objectid on mb_avg_koz_rechth_bag using btree (objectid);


