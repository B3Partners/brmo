/*
 * Views BAG 2 data.
 */

-- Vervangt mb_adres. Gemeentevelden zijn nog niet beschikbaar.
-- Gemeente-woonplaats relatie nog niet beschikbaar (BRMO-104)
create or replace view vb_adres as
select na.objectid,
       na.identificatie  as identificatienummeraanduiding,
       na.status,
       na.begingeldigheid,
       null              as gemeente,
       wp.naam           as woonplaats,
       opr.naam          as straatnaam,
       na.huisnummer,
       na.huisletter,
       na.huisnummertoevoeging,
       na.postcode,
       opr.identificatie as identificatieopenbareruimte,
       wp.identificatie  as identificatiewoonplaats,
       null              as gemeentecode
from v_nummeraanduiding_actueel na
         left join v_openbareruimte_actueel opr on (opr.identificatie = na.ligtaan)
         left join v_woonplaats_actueel wp on (wp.identificatie = opr.ligtin);

comment on table vb_adres is 'volledig actueel adres zonder locatie';


-- Vervangt vb_ligplaats_adres. Gemeentevelden zijn nog niet beschikbaar.
create or replace view vb_ligplaats_adres as
select lp.objectid,
       'true'                                 as ishoofdadres,
       lp.status,
       lp.identificatie,
       a.identificatienummeraanduiding,
       a.status                               as nummeraanduidingstatus,
       a.gemeente,
       a.woonplaats,
       a.straatnaam,
       a.huisnummer,
       a.huisletter,
       a.huisnummertoevoeging,
       a.postcode,
       sdo_geom.sdo_centroid(lp.geometrie, 2) as geometrie_centroide,
       lp.geometrie
from v_ligplaats_actueel lp
         join vb_adres a on lp.heeftalshoofdadres = a.identificatienummeraanduiding
union all
select lpa.objectid,
       'false'                                 as ishoofdadres,
       lpa.status,
       lpa.identificatie,
       a.identificatienummeraanduiding,
       a.status                                as nummeraanduidingstatus,
       a.gemeente,
       a.woonplaats,
       a.straatnaam,
       a.huisnummer,
       a.huisletter,
       a.huisnummertoevoeging,
       a.postcode,
       sdo_geom.sdo_centroid(lpa.geometrie, 2) as geometrie_centroide,
       lpa.geometrie
from v_ligplaats_actueel lpa
         join ligplaats_nevenadres lpna on
    (lpna.identificatie = lpa.identificatie
        and lpna.voorkomenidentificatie = lpa.voorkomenidentificatie)
         join vb_adres a on
    lpa.heeftalshoofdadres = a.identificatienummeraanduiding;

comment on table vb_ligplaats_adres is 'ligplaats met adres en puntlocatie';
delete from user_sdo_geom_metadata where table_name = 'VB_LIGPLAATS_ADRES';
insert into user_sdo_geom_metadata
values ('VB_LIGPLAATS_ADRES', 'GEOMETRIE_CENTROIDE', mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1),
                                                                         mdsys.sdo_dim_element('Y', 304000, 620000, .1)),
        28992);
insert into user_sdo_geom_metadata
values ('VB_LIGPLAATS_ADRES', 'GEOMETRIE', mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1),
                                                               mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);

-- Vervangt vb_standplaats_adres. Gemeentevelden zijn nog niet beschikbaar.
create or replace view vb_standplaats_adres as
select sp.objectid,
       'true'                                 as ishoofdadres,
       sp.status,
       sp.identificatie,
       a.identificatienummeraanduiding,
       a.status                               as nummeraanduidingstatus,
       a.gemeente,
       a.woonplaats,
       a.straatnaam,
       a.huisnummer,
       a.huisletter,
       a.huisnummertoevoeging,
       a.postcode,
       sdo_geom.sdo_centroid(sp.geometrie, 2) as geometrie_centroide,
       sp.geometrie
from v_standplaats_actueel sp
         join vb_adres a on sp.heeftalshoofdadres = a.identificatienummeraanduiding
union all
select spa.objectid,
       'false'                                 as ishoofdadres,
       spa.status,
       spa.identificatie,
       a.identificatienummeraanduiding,
       a.status                                as nummeraanduidingstatus,
       a.gemeente,
       a.woonplaats,
       a.straatnaam,
       a.huisnummer,
       a.huisletter,
       a.huisnummertoevoeging,
       a.postcode,
       sdo_geom.sdo_centroid(spa.geometrie, 2) as geometrie_centroide,
       spa.geometrie
from v_standplaats_actueel spa
         join standplaats_nevenadres spna on
    (spna.identificatie = spa.identificatie
        and spna.voorkomenidentificatie = spa.voorkomenidentificatie)
         join vb_adres a on
    spa.heeftalshoofdadres = a.identificatienummeraanduiding;

comment on table vb_standplaats_adres is 'standplaats met adres en puntlocatie';
delete from user_sdo_geom_metadata where table_name = 'VB_STANDPLAATS_ADRES';
insert into user_sdo_geom_metadata
values ('VB_STANDPLAATS_ADRES', 'GEOMETRIE_CENTROIDE',
        mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1),
                            mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);
insert into user_sdo_geom_metadata
values ('VB_STANDPLAATS_ADRES', 'GEOMETRIE', mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1),
                                                                 mdsys.sdo_dim_element('Y', 304000, 620000, .1)),
        28992);


-- Vervangt vb_vbo_adres. Gemeentevelden zijn nog niet beschikbaar.
create or replace view vb_verblijfsobject_adres as
select vo.objectid,
       'true'                                                                      as ishoofdadres,
       vo.status,
       vo.identificatie,
       a.identificatienummeraanduiding,
       a.status                                                                    as nummeraanduidingstatus,
       a.gemeente,
       a.woonplaats,
       a.straatnaam,
       a.huisnummer,
       a.huisletter,
       a.huisnummertoevoeging,
       a.postcode,
       vbod.maaktdeeluitvan,
       (select LISTAGG(vg.gebruiksdoel, ', ')
        from verblijfsobject_gebruiksdoel vg
        where (vg.identificatie = vo.identificatie and vg.voorkomenidentificatie =
                                                       vo.voorkomenidentificatie)) as gebruiksdoelen,
       sdo_geom.sdo_centroid(vo.geometrie, 2)                                      as geometrie_centroide,
       vo.geometrie
from v_verblijfsobject_actueel vo
         join vb_adres a on vo.heeftalshoofdadres = a.identificatienummeraanduiding
         join verblijfsobject_maaktdeeluitvan vbod
              on vo.identificatie = vbod.identificatie and vo.voorkomenidentificatie = vbod.voorkomenidentificatie
union all
select voa.objectid,
       'false'                                                                       as ishoofdadres,
       voa.status,
       voa.identificatie,
       a.identificatienummeraanduiding,
       a.status                                                                      as nummeraanduidingstatus,
       a.gemeente,
       a.woonplaats,
       a.straatnaam,
       a.huisnummer,
       a.huisletter,
       a.huisnummertoevoeging,
       a.postcode,
       vbod.maaktdeeluitvan,
       (select LISTAGG(vg.gebruiksdoel, ', ')
        from verblijfsobject_gebruiksdoel vg
        where (vg.identificatie = voa.identificatie and vg.voorkomenidentificatie =
                                                        voa.voorkomenidentificatie)) as gebruiksdoelen,
       sdo_geom.sdo_centroid(voa.geometrie, 2)                                       as geometrie_centroide,
       voa.geometrie
from v_verblijfsobject_actueel voa
         join verblijfsobject_nevenadres vona on
    (vona.identificatie = voa.identificatie
        and vona.voorkomenidentificatie = voa.voorkomenidentificatie)
         join vb_adres a on
    voa.heeftalshoofdadres = a.identificatienummeraanduiding
         join verblijfsobject_maaktdeeluitvan vbod
              on voa.identificatie = vbod.identificatie and voa.voorkomenidentificatie = vbod.voorkomenidentificatie;

comment on table vb_verblijfsobject_adres is 'verblijfsobject met adres, pandverwijzing, gebruiksdoel en puntlocatie';
delete from user_sdo_geom_metadata where table_name = 'VB_VERBLIJFSOBJECT_ADRES';
insert into user_sdo_geom_metadata
values ('VB_VERBLIJFSOBJECT_ADRES', 'GEOMETRIE_CENTROIDE',
        mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1),
                            mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);
insert into user_sdo_geom_metadata
values ('VB_VERBLIJFSOBJECT_ADRES', 'GEOMETRIE', mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1),
                                                                     mdsys.sdo_dim_element('Y', 304000, 620000, .1)),
        28992);


-- vervangt vb_benoemd_obj_adres alle objecten met een (neven)adres
create or replace view vb_adresseerbaar_object_geometrie as
select vla.objectid,
       vla.ishoofdadres,
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
       vla.geometrie_centroide,
       vla.geometrie
from vb_ligplaats_adres vla
union all
select vsa.objectid,
       vsa.ishoofdadres,
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
       vsa.geometrie_centroide,
       vsa.geometrie
from vb_standplaats_adres vsa
union all
select vva.objectid,
       vva.ishoofdadres,
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
       'verblijfsobject' as soort,
       vva.maaktdeeluitvan,
       vva.gebruiksdoelen,
       vva.geometrie_centroide,
       vva.geometrie
from vb_verblijfsobject_adres vva;

comment on table vb_adresseerbaar_object_geometrie is 'alle adresseerbare objecten (ligplaatst, standplaats, verblijfsobject) met adres, gebruiksdoel, pand en (afgeleide) geometrie.';
delete from user_sdo_geom_metadata where table_name = 'VB_ADRESSEERBAAR_OBJECT_GEOMETRIE';
insert into user_sdo_geom_metadata
values ('VB_ADRESSEERBAAR_OBJECT_GEOMETRIE', 'GEOMETRIE_CENTROIDE',
        mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1),
                            mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);
insert into user_sdo_geom_metadata
values ('VB_ADRESSEERBAAR_OBJECT_GEOMETRIE', 'GEOMETRIE',
        mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1),
                            mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);
