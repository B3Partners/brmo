-- Via de BRMO GUI kunnen deze materialized views vervolgens automatisch ververst worden.

-- LET OP: de aanname is dat het BAG schema de naam "BAG" heeft, indien dat niet het geval is moet dat hieronder
-- aangepast worden op die plaatsen waar verwezen wordt naar het BAG schema, zoek naar "bag." om dat te vervangen
-- in de definities van mb_adres_bag en mb_adresseerbaar_object_geometrie_bag

-- Aangezien het in een Oracle database niet mogelijk is om op schema nivo permissies uit te delen dient dat per view
-- gedaan te worden in het BAG schema/door de BAG user. Dat kan met onderstaande procedure
-- waarbij de RSGB user nog moet aangepast (in onderstaande 'JENKINS_RSGB'):
--
-- Vooralsnog gaat het om de views:
--      v_nummeraanduiding_actueel
--      v_openbareruimte_actueel
--      v_woonplaats_actueel
--      vb_ligplaats_adres
--      vb_standplaats_adres
--      vb_verblijfsobject_adres
BEGIN
    FOR t IN (SELECT * FROM USER_VIEWS)
    LOOP
        EXECUTE IMMEDIATE 'GRANT SELECT ON ' || t.view_name || ' TO JENKINS_RSGB';
    END LOOP;
END;

-- Vervangt mb_adres. Gemeentevelden zijn nog niet beschikbaar.
-- Gemeente-woonplaats relatie nog niet beschikbaar (BRMO-104)
create materialized view mb_adres_bag build deferred refresh on demand as
select na.objectid,
       na.identificatie           as identificatienummeraanduiding,
       na.status,
       na.begingeldigheid,
       cast(null as varchar2(40)) as gemeente,
       wp.naam                    as woonplaats,
       opr.naam                   as straatnaam,
       na.huisnummer,
       na.huisletter,
       na.huisnummertoevoeging,
       na.postcode,
       opr.identificatie          as identificatieopenbareruimte,
       wp.identificatie           as identificatiewoonplaats,
       cast(null as NUMBER(4, 0)) as gemeentecode
from bag.v_nummeraanduiding_actueel na
         left join bag.v_openbareruimte_actueel opr on (opr.identificatie = na.ligtaan)
         left join bag.v_woonplaats_actueel wp on (wp.identificatie = opr.ligtin);

create unique index mb_adres_bag_identificatie on mb_adres_bag (identificatienummeraanduiding asc);
create unique index mb_adres_bag_objectid on mb_adres_bag (objectid asc);
comment on materialized view mb_adres_bag is 'volledig actueel adres zonder locatie';


-- maakt een materialized view van vb_adresseerbaar_object_geometrie tbv performance bij zeer grote datasets.
create materialized view mb_adresseerbaar_object_geometrie_bag build deferred refresh on demand as
select cast(ROWNUM as INTEGER) as objectid,
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
             cast(null as varchar2(40))   as gemeente,
             vla.woonplaats,
             vla.straatnaam,
             vla.huisnummer,
             vla.huisletter,
             vla.huisnummertoevoeging,
             vla.postcode,
             'ligplaats'                  as soort,
             cast(null as char(16))       as maaktdeeluitvan,
             cast(null as varchar2(4000)) as gebruiksdoelen,
             cast(null as NUMBER(10, 0))  as oppervlakte,
             vla.geometrie_centroide,
             vla.geometrie
      from bag.vb_ligplaats_adres vla
      union all
      select vsa.ishoofdadres,
             vsa.status,
             vsa.identificatie,
             vsa.identificatienummeraanduiding,
             vsa.nummeraanduidingstatus,
             cast(null as varchar2(40))   as gemeente,
             vsa.woonplaats,
             vsa.straatnaam,
             vsa.huisnummer,
             vsa.huisletter,
             vsa.huisnummertoevoeging,
             vsa.postcode,
             'standplaats'                as soort,
             cast(null as char(16))       as maaktdeeluitvan,
             cast(null as varchar2(4000)) as gebruiksdoelen,
             cast(null as NUMBER(10, 0))  as oppervlakte,
             vsa.geometrie_centroide,
             vsa.geometrie
      from bag.vb_standplaats_adres vsa
      union all
      select vva.ishoofdadres,
             vva.status,
             vva.identificatie,
             vva.identificatienummeraanduiding,
             vva.nummeraanduidingstatus,
             cast(null as varchar2(40)) as gemeente,
             vva.woonplaats,
             vva.straatnaam,
             vva.huisnummer,
             vva.huisletter,
             vva.huisnummertoevoeging,
             vva.postcode,
             'verblijfsobject'          as soort,
             vva.maaktdeeluitvan,
             vva.gebruiksdoelen,
             vva.oppervlakte,
             vva.geometrie_centroide,
             vva.geometrie
      from bag.vb_verblijfsobject_adres vva) qry;

comment on materialized view mb_adresseerbaar_object_geometrie_bag is 'alle adresseerbare objecten (ligplaatst, standplaats, verblijfsobject) met adres, gebruiksdoel, pand en (afgeleide) geometrie.';
delete
from user_sdo_geom_metadata
where table_name = 'MB_ADRESSEERBAAR_OBJECT_GEOMETRIE_BAG';
insert into user_sdo_geom_metadata
values ('MB_ADRESSEERBAAR_OBJECT_GEOMETRIE_BAG', 'GEOMETRIE_CENTROIDE',
        mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1),
                            mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);
insert into user_sdo_geom_metadata
values ('MB_ADRESSEERBAAR_OBJECT_GEOMETRIE_BAG', 'GEOMETRIE',
        mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1),
                            mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);

create index mb_adresseerbaar_object_geometrie_bag_geom on mb_adresseerbaar_object_geometrie_bag (geometrie) indextype is MDSYS.SPATIAL_INDEX;
create index mb_adresseerbaar_object_geometrie_bag_centroid on mb_adresseerbaar_object_geometrie_bag (geometrie_centroide) indextype is MDSYS.SPATIAL_INDEX;
create index mb_adresseerbaar_object_geometrie_bag_identificatie on mb_adresseerbaar_object_geometrie_bag (identificatienummeraanduiding);
create unique index mb_adresseerbaar_object_geometrie_bag_objectid on mb_adresseerbaar_object_geometrie_bag (objectid);


--Maakt dezelfde materialized view aan als mb_kad_onrrnd_zk_adres, maar de BAG gegevens worden uit mb_adresseerbaar_object_geometrie_bag gehaald.
create materialized view mb_kad_onrrnd_zk_adres_bag build deferred refresh on demand as
select cast(ROWNUM as INTEGER)                                                             as objectid,
       qry.identif                                                                         as koz_identif,
       koz.dat_beg_geldh                                                                   as begin_geldigheid,
       to_date(koz.dat_beg_geldh, 'YYYY-MM-DD')                                            as begin_geldigheid_datum,
       bok.fk_nn_lh_tgo_identif                                                            as benoemdobj_identif,
       qry.type,
       coalesce(qry.ka_sectie, '') || ' ' || coalesce(qry.ka_perceelnummer, '')            as aanduiding,
       coalesce(qry.ka_kad_gemeentecode, '') || ' ' || coalesce(qry.ka_sectie, '') || ' ' ||
       coalesce(qry.ka_perceelnummer, '') || ' ' || coalesce(qry.ka_appartementsindex, '') as aanduiding2,
       qry.ka_sectie                                                                       as sectie,
       qry.ka_perceelnummer                                                                as perceelnummer,
       qry.ka_appartementsindex                                                            as appartementsindex,
       qry.ka_kad_gemeentecode                                                             as gemeentecode,
       qry.aand_soort_grootte,
       qry.grootte_perceel,
       qry.oppervlakte_geom,
       qry.ka_deelperceelnummer                                                            as deelperceelnummer,
       qry.omschr_deelperceel,
       b.datum                                                                             as verkoop_datum,
       koz.cu_aard_cultuur_onbebouwd                                                       as aard_cultuur_onbebouwd,
       koz.ks_bedrag                                                                       as bedrag,
       koz.ks_koopjaar                                                                     as koopjaar,
       koz.ks_meer_onroerendgoed                                                           as meer_onroerendgoed,
       koz.ks_valutasoort                                                                  as valutasoort,
       koz.lo_loc__omschr                                                                  as loc_omschr,
       aant.aantekeningen                                                                  as aantekeningen,
       maogb.identificatienummeraanduiding,
       maogb.nummeraanduidingstatus,
       cast(null as varchar2(40))                                                          as gemeente,
       maogb.woonplaats,
       maogb.straatnaam,
       maogb.huisnummer,
       maogb.huisletter,
       maogb.huisnummertoevoeging,
       maogb.postcode,
       maogb.gebruiksdoelen,
       maogb.oppervlakte,
       qry.lon,
       qry.lat,
       qry.begrenzing_perceel
from (select p.sc_kad_identif                                                                    as identif,
             'perceel'                                                                           as type,
             p.ka_sectie,
             p.ka_perceelnummer,
             cast(null as CHARACTER VARYING(4))                                                  as ka_appartementsindex,
             p.ka_kad_gemeentecode,
             p.aand_soort_grootte,
             p.grootte_perceel,
             p.ka_deelperceelnummer,
             p.omschr_deelperceel,
             p.begrenzing_perceel                                                                as begrenzing_perceel,
             case
                 when p.begrenzing_perceel.get_gtype() is not null then sdo_geom.sdo_area(p.begrenzing_perceel, 0.1)
                 else null end                                                                   as oppervlakte_geom,
             case
                 when p.begrenzing_perceel.get_gtype() is not null then sdo_cs.transform(
                         sdo_geom.sdo_centroid(p.begrenzing_perceel, 0.1), 4326).sdo_point.x
                 else null end                                                                   as lon,
             case
                 when p.begrenzing_perceel.get_gtype() is not null then sdo_cs.transform(
                         sdo_geom.sdo_centroid(p.begrenzing_perceel, 0.1), 4326).sdo_point.y end as lat
      from kad_perceel p
      union all
      select ar.sc_kad_identif                                                                    as identif,
             'appartement'                                                                        as type,
             ar.ka_sectie,
             ar.ka_perceelnummer,
             ar.ka_appartementsindex,
             ar.ka_kad_gemeentecode,
             cast(null as CHARACTER VARYING(2))                                                   as aand_soort_grootte,
             cast(null as NUMERIC(8, 0))                                                          as grootte_perceel,
             cast(null as CHARACTER VARYING(4))                                                   as ka_deelperceelnummer,
             cast(null as CHARACTER VARYING(1120))                                                as omschr_deelperceel,
             kp.begrenzing_perceel                                                                as begrenzing_perceel,
             case
                 when kp.begrenzing_perceel.get_gtype() is not null then sdo_geom.sdo_area(kp.begrenzing_perceel, 0.1)
                 else null end                                                                    as oppervlakte_geom,
             case
                 when kp.begrenzing_perceel.get_gtype() is not null then sdo_cs.transform(
                         sdo_geom.sdo_centroid(kp.begrenzing_perceel, 0.1), 4326).sdo_point.x
                 else null end                                                                    as lon,
             case
                 when kp.begrenzing_perceel.get_gtype() is not null then sdo_cs.transform(
                         sdo_geom.sdo_centroid(kp.begrenzing_perceel, 0.1), 4326).sdo_point.y end as lat
      from mb_util_app_re_kad_perceel v
               join kad_perceel kp on cast(v.perceel_identif as NUMERIC) = kp.sc_kad_identif
               join app_re ar on cast(v.app_re_identif as NUMERIC) = ar.sc_kad_identif) qry
         join kad_onrrnd_zk koz on (koz.kad_identif = qry.identif)
         left join benoemd_obj_kad_onrrnd_zk bok on (bok.fk_nn_rh_koz_kad_identif = qry.identif)
         left join mb_adresseerbaar_object_geometrie_bag maogb on (((bok.fk_nn_lh_tgo_identif) = maogb.identificatie))
         left join (select bd.ref_id, max(bd.datum) as datum
                    from brondocument bd
                    where ((bd.omschrijving) = 'Akte van Koop en Verkoop')
                    group by bd.ref_id) b on (koz.kad_identif = b.ref_id)
         left join (select fk_4koz_kad_identif,
                           listagg('id: ' || coalesce(koza.kadaster_identif_aantek, '') || ', ' || 'aard: ' ||
                                   coalesce(koza.aard_aantek_kad_obj, '') || ', ' || 'begin: ' ||
                                   coalesce(koza.begindatum_aantek_kad_obj, '') || ', ' || 'beschrijving: ' ||
                                   coalesce(koza.beschrijving_aantek_kad_obj, '') || ', ' || 'eind: ' ||
                                   coalesce(koza.eindd_aantek_kad_obj, '') || ', ' || 'koz-id: ' ||
                                   coalesce(koza.fk_4koz_kad_identif, 0) || ', ' || 'subject-id: ' ||
                                   coalesce(koza.fk_5pes_sc_identif, '') || '; ', ' & ' on OVERFLOW TRUNCATE with COUNT)
                                   within group ( order by koza.fk_4koz_kad_identif ) as aantekeningen
                    from kad_onrrnd_zk_aantek koza
                    group by fk_4koz_kad_identif) aant on koz.kad_identif = aant.fk_4koz_kad_identif;

comment on materialized view mb_kad_onrrnd_zk_adres_bag is 'alle kadastrale onroerende zaken (perceel en appartementsrecht) met opgezochte verkoop datum, objectid voor geoserver/arcgis en BAG adres';
delete
from user_sdo_geom_metadata
where table_name = 'MB_ADRESSEERBAAR_OBJECT_GEOMETRIE_BAG';
insert into user_sdo_geom_metadata
values ('MB_ADRESSEERBAAR_OBJECT_GEOMETRIE_BAG', 'BEGRENZING_PERCEEL',
        mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1),
                            mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);

create index mb_kad_onrrnd_zk_adres_bag_begrenzing_perceel_idx on mb_kad_onrrnd_zk_adres_bag (begrenzing_perceel) indextype is MDSYS.SPATIAL_INDEX;
create index mb_kad_onrrnd_zk_adres_bag_identif on mb_kad_onrrnd_zk_adres_bag (koz_identif);
create unique index mb_kad_onrrnd_zk_adres_bag_objectid on mb_kad_onrrnd_zk_adres_bag (objectid);


--Maakt dezelfde materialized view aan als mb_koz_rechth, maar de BAG gegevens worden uit mb_kad_onrrnd_zk_adres_bag gehaald.
create materialized view mb_koz_rechth_bag build deferred refresh on demand as
select cast(ROWNUM as INTEGER)                                            as objectid,
       koz.koz_identif,
       koz.begin_geldigheid,
       to_date(koz.begin_geldigheid, 'YYYY-MM-DD')                        as begin_geldigheid_datum,
       koz.type,
       coalesce(koz.sectie, '') || ' ' || coalesce(koz.perceelnummer, '') as aanduiding,
       coalesce(koz.gemeentecode, '') || ' ' || coalesce(koz.sectie, '') || ' ' || coalesce(koz.perceelnummer, '') ||
       ' ' || coalesce(koz.appartementsindex, '')                         as aanduiding2,
       koz.sectie,
       koz.perceelnummer,
       koz.appartementsindex,
       koz.gemeentecode,
       koz.aand_soort_grootte,
       koz.grootte_perceel,
       koz.oppervlakte_geom                                               as oppervlakte_geom,
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
       zrr.omschr_aard_verkregenr_recht,
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
         right join mb_kad_onrrnd_zk_adres_bag koz on zrr.koz_identif = koz.koz_identif;

comment on materialized view mb_kad_onrrnd_zk_adres_bag is 'kadastrale percelen een appartementsrechten met rechten en rechthebbenden en objectid voor geoserver/arcgis';
delete
from user_sdo_geom_metadata
where table_name = 'MB_KOZ_RECHTH_BAG';
insert into user_sdo_geom_metadata
values ('MB_KOZ_RECHTH_BAG', 'BEGRENZING_PERCEEL',
        mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1),
                            mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);

create index mb_koz_rechth_bag_begrenzing_perceel_idx on mb_koz_rechth_bag (begrenzing_perceel) indextype is MDSYS.SPATIAL_INDEX;
create index mb_koz_rechth_bag_identif on mb_koz_rechth_bag (koz_identif);
create unique index mb_koz_rechth_bag_objectid on mb_koz_rechth_bag (objectid);


--Maakt dezelfde materialized view aan als mb_avg_koz_rechth, maar de BAG gegevens worden uit mb_kad_onrrnd_zk_adres_bag gehaald.
create materialized view mb_avg_koz_rechth_bag build deferred refresh on demand as
select cast(ROWNUM as INTEGER)                                            as objectid,
       koz.koz_identif                                                    as koz_identif,
       koz.begin_geldigheid,
       to_date(koz.begin_geldigheid, 'YYYY-MM-DD')                        as begin_geldigheid_datum,
       koz.type,
       coalesce(koz.sectie, '') || ' ' || coalesce(koz.perceelnummer, '') as aanduiding,
       coalesce(koz.gemeentecode, '') || ' ' || coalesce(koz.sectie, '') || ' ' || coalesce(koz.perceelnummer, '') ||
       ' ' || coalesce(koz.appartementsindex, '')                         as aanduiding2,
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
       zrr.omschr_aard_verkregenr_recht,
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
         right join mb_kad_onrrnd_zk_adres_bag koz on zrr.koz_identif = koz.koz_identif;


comment on materialized view mb_kad_onrrnd_zk_adres_bag is 'kadastrale percelen een appartementsrechten met rechten en rechthebbenden geschoond voor avg en objectid voor geoserver/arcgis';
delete
from user_sdo_geom_metadata
where table_name = 'MB_AVG_KOZ_RECHTH_BAG';
insert into user_sdo_geom_metadata
values ('MB_AVG_KOZ_RECHTH_BAG', 'BEGRENZING_PERCEEL',
        mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1),
                            mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);

create index mb_avg_koz_rechth_bag_begrenzing_perceel_idx on mb_avg_koz_rechth_bag (begrenzing_perceel) indextype is MDSYS.SPATIAL_INDEX;
create index mb_avg_koz_rechth_bag_identif on mb_avg_koz_rechth_bag (koz_identif);
create unique index mb_avg_koz_rechth_bag_objectid on mb_avg_koz_rechth_bag (objectid);
