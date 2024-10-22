-- Via de BRMO GUI kunnen deze materialized views vervolgens automatisch ververst worden.

-- LET OP: de aanname is dat het BAG schema de naam "JENKINS_BAG" heeft, indien dat niet het geval is moet dat hieronder
-- aangepast worden op die plaatsen waar verwezen wordt naar het BAG schema, zoek naar "jenkins_bag." om dat te vervangen
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
-- BEGIN
--     FOR t IN (SELECT * FROM USER_VIEWS)
--     LOOP
--         EXECUTE IMMEDIATE 'GRANT SELECT ON ' || t.view_name || ' TO JENKINS_RSGB';
--     END LOOP;
-- END;

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
from jenkins_bag.v_nummeraanduiding_actueel na
         left join jenkins_bag.v_openbareruimte_actueel opr on (opr.identificatie = na.ligtaan)
         left join jenkins_bag.v_woonplaats_actueel wp on (wp.identificatie = opr.ligtin);

create unique index mb_adres_bag_identificatie on mb_adres_bag (identificatienummeraanduiding asc);
create unique index mb_adres_bag_objectid on mb_adres_bag (objectid asc);
comment on materialized view mb_adres_bag is 'volledig actueel adres zonder locatie';


-- maakt een materialized view van vb_adresseerbaar_object_geometrie tbv performance bij zeer grote datasets.
create materialized view mb_adresseerbaar_object_geometrie_bag build deferred refresh on demand as
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
      from jenkins_bag.vb_ligplaats_adres vla
      union all
      select vsa.ishoofdadres,
             vsa.status,
             vsa.objectid,
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
      from jenkins_bag.vb_standplaats_adres vsa
      union all
      select vva.ishoofdadres,
             vva.status,
             vva.objectid,
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
      from jenkins_bag.vb_verblijfsobject_adres vva) qry;

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
