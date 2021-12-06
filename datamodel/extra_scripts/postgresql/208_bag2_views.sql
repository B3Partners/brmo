/*
 * Views BAG 2 data.
 */

set search_path = bag,public;

-- Vervangt mb_adres. Gemeentevelden zijn nog niet beschikbaar.
create or replace view vb_adres as
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
         left join v_woonplaats_actueel wp on (wp.identificatie = opr.ligtin);

comment on view vb_adres is 'volledig actueel adres zonder locatie';


-- Vervangt vb_ligplaats_adres. Gemeentevelden zijn nog niet beschikbaar.
create or replace view vb_ligplaats_adres as
select lp.objectid,
       true                      as ishoofdadres,
       lp.status,
       lp.identificatie,
       a.identificatienummeraanduiding,
       a.status                  as nummeraanduidingstatus,
       a.gemeente,
       a.woonplaats,
       a.straatnaam,
       a.huisnummer,
       a.huisletter,
       a.huisnummertoevoeging,
       a.postcode,
       st_centroid(lp.geometrie) as geometrie_centroide,
       lp.geometrie
from v_ligplaats_actueel lp
         join vb_adres a on lp.heeftalshoofdadres = a.identificatienummeraanduiding
union all
select lpa.objectid,
       false                      as ishoofdadres,
       lpa.status,
       lpa.identificatie,
       a.identificatienummeraanduiding,
       a.status                   as nummeraanduidingstatus,
       a.gemeente,
       a.woonplaats,
       a.straatnaam,
       a.huisnummer,
       a.huisletter,
       a.huisnummertoevoeging,
       a.postcode,
       st_centroid(lpa.geometrie) as geometrie_centroide,
       lpa.geometrie
from v_ligplaats_actueel lpa
         join ligplaats_nevenadres lpna on
    (lpna.identificatie = lpa.identificatie
        and lpna.voorkomenidentificatie = lpa.voorkomenidentificatie)
         join vb_adres a on
    lpa.heeftalshoofdadres = a.identificatienummeraanduiding;

comment on view vb_ligplaats_adres is 'ligplaats met adres en puntlocatie';


-- Vervangt vb_standplaats_adres. Gemeentevelden zijn nog niet beschikbaar.
create or replace view vb_standplaats_adres as
select sp.objectid,
       true                      as ishoofdadres,
       sp.status,
       sp.identificatie,
       a.identificatienummeraanduiding,
       a.status                  as nummeraanduidingstatus,
       a.gemeente,
       a.woonplaats,
       a.straatnaam,
       a.huisnummer,
       a.huisletter,
       a.huisnummertoevoeging,
       a.postcode,
       st_centroid(sp.geometrie) as geometrie_centroide,
       sp.geometrie
from v_standplaats_actueel sp
         join vb_adres a on sp.heeftalshoofdadres = a.identificatienummeraanduiding
union all
select spa.objectid,
       false                      as ishoofdadres,
       spa.status,
       spa.identificatie,
       a.identificatienummeraanduiding,
       a.status                   as nummeraanduidingstatus,
       a.gemeente,
       a.woonplaats,
       a.straatnaam,
       a.huisnummer,
       a.huisletter,
       a.huisnummertoevoeging,
       a.postcode,
       st_centroid(spa.geometrie) as geometrie_centroide,
       spa.geometrie
from v_standplaats_actueel spa
         join standplaats_nevenadres spna on
    (spna.identificatie = spa.identificatie
        and spna.voorkomenidentificatie = spa.voorkomenidentificatie)
         join vb_adres a on
    spa.heeftalshoofdadres = a.identificatienummeraanduiding;

comment on view vb_standplaats_adres is 'standplaats met adres en puntlocatie';


-- Vervangt vb_vbo_adres. Gemeentevelden zijn nog niet beschikbaar.
create or replace view vb_verblijfsobject_adres as
select vo.objectid,
       true                      as ishoofdadres,
       vo.status,
       vo.identificatie,
       a.identificatienummeraanduiding,
       a.status                  as nummeraanduidingstatus,
       a.gemeente,
       a.woonplaats,
       a.straatnaam,
       a.huisnummer,
       a.huisletter,
       a.huisnummertoevoeging,
       a.postcode,
       vbod.maaktdeeluitvan,
       array_to_string(
               (select array_agg(vg.gebruiksdoel)
                from verblijfsobject_gebruiksdoel vg
                where (vg.identificatie = vo.identificatie and vg.voorkomenidentificatie = vo.voorkomenidentificatie))
           , ', ')               as gebruiksdoelen,
       vo.oppervlakte,
       st_centroid(vo.geometrie) as geometrie_centroide,
       vo.geometrie
from v_verblijfsobject_actueel vo
         join vb_adres a on vo.heeftalshoofdadres = a.identificatienummeraanduiding
         join verblijfsobject_maaktdeeluitvan vbod
              on vo.identificatie = vbod.identificatie and vo.voorkomenidentificatie = vbod.voorkomenidentificatie
union all
select voa.objectid,
       false                      as ishoofdadres,
       voa.status,
       voa.identificatie,
       a.identificatienummeraanduiding,
       a.status                   as nummeraanduidingstatus,
       a.gemeente,
       a.woonplaats,
       a.straatnaam,
       a.huisnummer,
       a.huisletter,
       a.huisnummertoevoeging,
       a.postcode,
       vbod.maaktdeeluitvan,
       array_to_string(
               (select array_agg(vg.gebruiksdoel)
                from verblijfsobject_gebruiksdoel vg
                where (vg.identificatie = voa.identificatie and vg.voorkomenidentificatie = voa.voorkomenidentificatie))
           , ', ')                as gebruiksdoelen,
       voa.oppervlakte,
       st_centroid(voa.geometrie) as geometrie_centroide,
       voa.geometrie
from v_verblijfsobject_actueel voa
         join verblijfsobject_nevenadres vona on
    (vona.identificatie = voa.identificatie
        and vona.voorkomenidentificatie = voa.voorkomenidentificatie)
         join vb_adres a on
    voa.heeftalshoofdadres = a.identificatienummeraanduiding
         join verblijfsobject_maaktdeeluitvan vbod
              on voa.identificatie = vbod.identificatie and voa.voorkomenidentificatie = vbod.voorkomenidentificatie;

comment on view vb_verblijfsobject_adres is 'verblijfsobject met adres, pandverwijzing, gebruiksdoel en puntlocatie';


-- vervangt vb_benoemd_obj_adres alle objecten met een (neven)adres
create or replace view vb_adresseerbaar_object_geometrie as
select (row_number() over ())::integer as objectid,
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
       'ligplaats'                     as soort,
       null                            as maaktdeeluitvan,
       null                            as gebruiksdoelen,
       null                            as oppervlakte,
       vla.geometrie_centroide,
       vla.geometrie
from vb_ligplaats_adres vla
union all
select (row_number() over ())::integer as objectid,
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
       'standplaats'                   as soort,
       null                            as maaktdeeluitvan,
       null                            as gebruiksdoelen,
       null                            as oppervlakte,
       vsa.geometrie_centroide,
       vsa.geometrie
from vb_standplaats_adres vsa
union all
select (row_number() over ())::integer as objectid,
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
       'verblijfsobject'               as soort,
       vva.maaktdeeluitvan,
       vva.gebruiksdoelen,
       vva.oppervlakte,
       vva.geometrie_centroide,
       vva.geometrie
from vb_verblijfsobject_adres vva;

comment on
    view vb_adresseerbaar_object_geometrie is 'alle adresseerbare objecten (ligplaatst, standplaats, verblijfsobject) met adres, gebruiksdoel, pand en (afgeleide) geometrie.';
