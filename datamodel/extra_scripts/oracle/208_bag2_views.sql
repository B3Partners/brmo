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
       TRIM(COALESCE(opr.naam, '') || ' ' || COALESCE(TO_CHAR(na.huisnummer), '') || COALESCE(na.huisletter, '') || COALESCE(na.huisnummertoevoeging, '') || ', ' || COALESCE(na.postcode, '') || ' ' || COALESCE(wp.naam, '')) AS adres_totaal,
       opr.identificatie as identificatieopenbareruimte,
       wp.identificatie  as identificatiewoonplaats,
       null              as gemeentecode
from v_nummeraanduiding_actueel na
         left join v_openbareruimte_actueel opr on (opr.identificatie = na.ligtaan)
         left join v_woonplaats_actueel wp on (wp.identificatie = opr.ligtin)
where na.status <> 'Naamgeving ingetrokken';

comment on table vb_adres is 'Actuele gegevens van bestaande adressen zonder geometrie';


-- Vervangt vb_ligplaats_adres. Gemeentevelden zijn nog niet beschikbaar.
create or replace view vb_ligplaats_adres as
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
       qry.adres_totaal,
       qry.geometrie,
       qry.geometrie_centroide
from (select 'true'                                 as ishoofdadres,
             lp.status,
             a.objectid,
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
             a.adres_totaal,
             sdo_geom.sdo_centroid(lp.geometrie, 2) as geometrie_centroide,
             lp.geometrie
      from v_ligplaats_actueel lp
               join vb_adres a on lp.heeftalshoofdadres = a.identificatienummeraanduiding
      union all
      select 'false'                                 as ishoofdadres,
             lpa.status,
             a.objectid,
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
             a.adres_totaal,
             sdo_geom.sdo_centroid(lpa.geometrie, 2) as geometrie_centroide,
             lpa.geometrie
      from v_ligplaats_actueel lpa
               join ligplaats_nevenadres lpna on (lpna.identificatie = lpa.identificatie and
                                                  lpna.voorkomenidentificatie = lpa.voorkomenidentificatie)
               join vb_adres a on lpna.heeftalsnevenadres = a.identificatienummeraanduiding) qry
where qry.status <> 'Plaats ingetrokken';

comment on table vb_ligplaats_adres is 'Actuele gegevens van bestaande ligplaatsen met adres en puntlocatie';
delete from user_sdo_geom_metadata where table_name = 'VB_LIGPLAATS_ADRES';
insert into user_sdo_geom_metadata values ('VB_LIGPLAATS_ADRES', 'GEOMETRIE_CENTROIDE', mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1), mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);
insert into user_sdo_geom_metadata values ('VB_LIGPLAATS_ADRES', 'GEOMETRIE', mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1), mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);


-- Vervangt vb_standplaats_adres. Gemeentevelden zijn nog niet beschikbaar.
create or replace view vb_standplaats_adres as
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
       qry.adres_totaal,
       qry.geometrie,
       qry.geometrie_centroide
from (select 'true'                                 as ishoofdadres,
             sp.status,
             a.objectid,
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
             a.adres_totaal,
             sdo_geom.sdo_centroid(sp.geometrie, 2) as geometrie_centroide,
             sp.geometrie
      from v_standplaats_actueel sp
               join vb_adres a on sp.heeftalshoofdadres = a.identificatienummeraanduiding
      union all
      select 'false'                                 as ishoofdadres,
             spa.status,
             a.objectid,
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
             a.adres_totaal,
             sdo_geom.sdo_centroid(spa.geometrie, 2) as geometrie_centroide,
             spa.geometrie
      from v_standplaats_actueel spa
               join standplaats_nevenadres spna on (spna.identificatie = spa.identificatie and
                                                    spna.voorkomenidentificatie = spa.voorkomenidentificatie)
               join vb_adres a on spna.heeftalsnevenadres = a.identificatienummeraanduiding) qry
where qry.status <> 'Plaats ingetrokken';

comment on table vb_standplaats_adres is 'Actuele gegevens van bestaande standplaatsen met adres en puntlocatie';
delete from user_sdo_geom_metadata where table_name = 'VB_STANDPLAATS_ADRES';
insert into user_sdo_geom_metadata values ('VB_STANDPLAATS_ADRES', 'GEOMETRIE_CENTROIDE', mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1), mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);
insert into user_sdo_geom_metadata values ('VB_STANDPLAATS_ADRES', 'GEOMETRIE', mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1), mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);


-- Vervangt vb_vbo_adres. Gemeentevelden zijn nog niet beschikbaar.
create or replace view vb_verblijfsobject_adres as
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
       qry.adres_totaal,
       qry.maaktdeeluitvan,
       qry.gebruiksdoelen,
       qry.oppervlakte,
       qry.geometrie,
       qry.geometrie_centroide
from (select 'true'                                                                                                  as ishoofdadres,
             vo.status,
             a.objectid,
             vo.identificatie,
             a.identificatienummeraanduiding,
             a.status                                                                                                as nummeraanduidingstatus,
             a.gemeente,
             a.woonplaats,
             a.straatnaam,
             a.huisnummer,
             a.huisletter,
             a.huisnummertoevoeging,
             a.postcode,
             a.adres_totaal,
             (select listagg(vbod.maaktdeeluitvan, ', ')
              from verblijfsobject_maaktdeeluitvan vbod
              where (vbod.identificatie = vo.identificatie and vbod.voorkomenidentificatie =
                                                             vo.voorkomenidentificatie))                             as maaktdeeluitvan,
             (select listagg(vg.gebruiksdoel, ', ')
              from verblijfsobject_gebruiksdoel vg
              where (vg.identificatie = vo.identificatie and vg.voorkomenidentificatie =
                                                             vo.voorkomenidentificatie))                             as gebruiksdoelen,
             vo.oppervlakte,
             sdo_geom.sdo_centroid(vo.geometrie, 2)                                                                  as geometrie_centroide,
             vo.geometrie
      from v_verblijfsobject_actueel vo
               join vb_adres a on vo.heeftalshoofdadres = a.identificatienummeraanduiding
      union all
      select 'false'                                                          as ishoofdadres,
             voa.status,
             a.objectid,
             voa.identificatie,
             a.identificatienummeraanduiding,
             a.status                                                         as nummeraanduidingstatus,
             a.gemeente,
             a.woonplaats,
             a.straatnaam,
             a.huisnummer,
             a.huisletter,
             a.huisnummertoevoeging,
             a.postcode,
             a.adres_totaal,
             (select listagg(vbod.maaktdeeluitvan, ', ')
              from verblijfsobject_maaktdeeluitvan vbod
              where (vbod.identificatie = voa.identificatie and vbod.voorkomenidentificatie =
                                                             voa.voorkomenidentificatie))                             as maaktdeeluitvan,
             (select listagg(vg.gebruiksdoel, ', ')
              from verblijfsobject_gebruiksdoel vg
              where (vg.identificatie = voa.identificatie and
                     vg.voorkomenidentificatie = voa.voorkomenidentificatie)) as gebruiksdoelen,
             voa.oppervlakte,
             sdo_geom.sdo_centroid(voa.geometrie, 2)                          as geometrie_centroide,
             voa.geometrie
      from v_verblijfsobject_actueel voa
               join verblijfsobject_nevenadres vona on (vona.identificatie = voa.identificatie and
                                                        vona.voorkomenidentificatie = voa.voorkomenidentificatie)
               join vb_adres a on vona.heeftalsnevenadres = a.identificatienummeraanduiding) qry
where qry.status not in ('Niet gerealiseerd verblijfsobject', 'Verblijfsobject ingetrokken', 'Verblijfsobject ten onrechte opgevoerd');
--'Verblijfsobject buiten gebruik' wordt wel in de view meegenomen aangezien het verblijfsobject en het bijbehorende pand nog aanwezig zijn in de openbare ruimte.

comment on table vb_verblijfsobject_adres is 'Actuele gegevens van bestaande verblijfsobjecten en die nog gerealiseerd zullen worden met adres, pandverwijzing, gebruiksdoel en puntlocatie';
delete from user_sdo_geom_metadata where table_name = 'VB_VERBLIJFSOBJECT_ADRES';
insert into user_sdo_geom_metadata values ('VB_VERBLIJFSOBJECT_ADRES', 'GEOMETRIE_CENTROIDE', mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1), mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);
insert into user_sdo_geom_metadata values ('VB_VERBLIJFSOBJECT_ADRES', 'GEOMETRIE', mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1), mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);

create or replace view vb_pand as
select vpa.objectid,
       vpa.identificatie,
       vpa.voorkomenidentificatie,
       vpa.begingeldigheid,
       vpa.eindgeldigheid,
       vpa.tijdstipregistratie,
       vpa.eindregistratie,
       vpa.tijdstipinactief,
       vpa.tijdstipregistratielv,
       vpa.tijdstipeindregistratielv,
       vpa.tijdstipinactieflv,
       vpa.documentdatum,
       vpa.documentnummer,
       vpa.geconstateerd,
       vpa.status,
       vpa.oorspronkelijkbouwjaar,
       vpa.geometrie
from v_pand_actueel vpa
where vpa.status not in ('Niet gerealiseerd pand', 'Pand ten onrechte opgevoerd', 'Pand gesloopt');
--'Pand buiten gebruik' wordt wel in de view meegenomen aangezien het pand nog aanwezig is in de openbare ruimte.

comment on table vb_pand is 'Actuele gegevens van bestaande panden én die nog gerealiseerd zullen worden';
delete from user_sdo_geom_metadata where table_name = 'VB_PAND';
insert into user_sdo_geom_metadata values ('VB_PAND', 'GEOMETRIE', mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1), mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);

-- vervangt vb_benoemd_obj_adres alle objecten met een (neven)adres
create or replace view vb_adresseerbaar_object_geometrie as
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
      from vb_ligplaats_adres vla
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
      from vb_standplaats_adres vsa
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
             'verblijfsobject' as soort,
             vva.maaktdeeluitvan,
             vva.gebruiksdoelen,
             vva.oppervlakte,
             vva.geometrie_centroide,
             vva.geometrie
      from vb_verblijfsobject_adres vva) qry;

comment on table vb_adresseerbaar_object_geometrie is 'alle adresseerbare objecten (ligplaatst, standplaats, verblijfsobject) met adres, gebruiksdoel, pand en (afgeleide) geometrie.';
delete from user_sdo_geom_metadata where table_name = 'VB_ADRESSEERBAAR_OBJECT_GEOMETRIE';
insert into user_sdo_geom_metadata values ('VB_ADRESSEERBAAR_OBJECT_GEOMETRIE', 'GEOMETRIE_CENTROIDE', mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1), mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);
insert into user_sdo_geom_metadata values ('VB_ADRESSEERBAAR_OBJECT_GEOMETRIE', 'GEOMETRIE', mdsys.sdo_dim_array(mdsys.sdo_dim_element('X', 12000, 280000, .1), mdsys.sdo_dim_element('Y', 304000, 620000, .1)), 28992);
