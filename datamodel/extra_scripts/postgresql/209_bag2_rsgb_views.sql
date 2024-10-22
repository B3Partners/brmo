-- veranderd het search_path in de database naar het public schema om aan het public schema de nieuwe materialized views toe te voegen.
-- Via de BRMO GUI kunnen deze materialized views vervolgens automatisch ververst worden.
SET SCHEMA 'public';
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
select qry.objectid,
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
       qry.geometrie,
       qry.geometrie_centroide
from (select vla.ishoofdadres,
             vla.status,
             vla.objectid,
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
             vsa.objectid,
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
             vva.objectid,
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
