/*
 * Views BAG 2 data.
 */

create or replace view vb2_adres as
select
    na.objectid,
    na.identificatie as identificatie_nummeraanduiding,
    na.status,
    na.begingeldigheid,
    null as gemeente, -- Gemeente-woonplaats relatie nog niet beschikbaar (BRMO-104)
    wp.naam as woonplaats,
    opr.naam as straatnaam,
    na.huisnummer,
    na.huisletter,
    na.huisnummertoevoeging,
    na.postcode,
    opr.identificatie as identificatie_openbareruimte,
    wp.identificatie as identificatie_woonplaats,
    null as gemeentecode -- Gemeente-woonplaats relatie nog niet beschikbaar (BRMO-104)
from v_nummeraanduiding_actueel na
         left join v_openbareruimte_actueel opr on (opr.identificatie = na.ligtaan)
         left join v_woonplaats_actueel wp on (wp.identificatie = opr.ligtin);
