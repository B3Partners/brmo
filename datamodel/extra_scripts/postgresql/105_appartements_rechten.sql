-- selecteer parent en child app_re's die een ondersplitsing zijn of zijn geworden

CREATE OR REPLACE VIEW v_bd_app_re_app_re AS 
 SELECT b1.ref_id AS app_re_identif,
    b2.ref_id AS parent_app_re_identif
   FROM brondocument b1
     JOIN brondocument b2 ON b2.identificatie = b1.identificatie
  WHERE (b2.omschrijving = 'betrokkenBij Ondersplitsing' OR  b2.omschrijving = 'ontstaanUit HoofdSplitsing') AND b1.omschrijving = 'ontstaanUit Ondersplitsing'
  GROUP BY b1.ref_id, b2.ref_id;

-- recursieve query om alle actuele appartementsrechten te vinden bij percelen
CREATE OR REPLACE VIEW v_bd_app_re_all_kad_perceel AS 

with recursive related_app_re (app_re_identif, perceel_identif) as (
SELECT b1.ref_id AS app_re_identif,
    b2.ref_id AS perceel_identif
   FROM brondocument b1
     JOIN brondocument b2 ON b2.identificatie = b1.identificatie
  WHERE b2.omschrijving = 'betrokkenBij HoofdSplitsing' AND (b1.omschrijving = 'ontstaanUit HoofdSplitsing' OR b1.omschrijving = 'ontstaanUit Ondersplitsing')  GROUP BY b1.ref_id, b2.ref_id


union


 SELECT vaa.app_re_identif,
    vap.perceel_identif
   FROM v_bd_app_re_app_re vaa
     JOIN related_app_re vap ON vaa.parent_app_re_identif = vap.app_re_identif
  GROUP BY vaa.app_re_identif, vap.perceel_identif
)

select 
app_re.sc_kad_identif::varchar(50) as app_re_identif, rar.perceel_identif
from related_app_re rar
left join app_re 
on app_re.sc_kad_identif::text = rar.app_re_identif;


-- Haalt alle percelen ids op met 1 of meer app_re (dient als basis voor de view voor de kaart)

CREATE OR REPLACE VIEW v_bd_kad_perceel_with_app_re AS 
 SELECT DISTINCT b2.ref_id AS perceel_identif
   FROM brondocument b1
     JOIN brondocument b2 ON b2.identificatie = b1.identificatie
  WHERE b2.omschrijving = 'betrokkenBij HoofdSplitsing' AND (b1.omschrijving = 'ontstaanUit HoofdSplitsing' OR b1.omschrijving = 'ontstaanUit Ondersplitsing');

-- view om kaart te maken met percelen die 1 of meerdere appartementen hebben

CREATE OR REPLACE VIEW v_bd_kad_perceel_met_app AS 
 SELECT v.perceel_identif,
    kp.sc_kad_identif,
    kp.aand_soort_grootte,
    kp.grootte_perceel,
    kp.omschr_deelperceel,
    kp.fk_7kdp_sc_kad_identif,
    kp.ka_deelperceelnummer,
    kp.ka_kad_gemeentecode,
    kp.ka_perceelnummer,
    kp.ka_sectie,
    kp.begrenzing_perceel,
    kp.plaatscoordinaten_perceel
   FROM v_bd_kad_perceel_with_app_re v
     JOIN kad_perceel kp ON v.perceel_identif = kp.sc_kad_identif::varchar;

-- view om vlakken kaart te maken met percelen die 1 of meerdere appartementen hebben
CREATE OR REPLACE VIEW v_bd_kad_perceel_met_app_vlak AS 
 SELECT 
    (row_number() OVER ())::integer AS ObjectID,
    v.perceel_identif,
    kp.sc_kad_identif,
    kp.aand_soort_grootte,
    kp.grootte_perceel,
    kp.omschr_deelperceel,
    kp.fk_7kdp_sc_kad_identif,
    kp.ka_deelperceelnummer,
    kp.ka_kad_gemeentecode,
    kp.ka_perceelnummer,
    kp.ka_sectie,
    kp.begrenzing_perceel
   FROM v_bd_kad_perceel_with_app_re v
     JOIN kad_perceel kp ON v.perceel_identif = kp.sc_kad_identif::varchar;
     

-- view om appartementsrechten bij percelen op te zoeken
CREATE OR REPLACE VIEW v_bd_app_re_bij_perceel AS 
 SELECT 
    (row_number() OVER ())::integer AS ObjectID,
    ar.sc_kad_identif,
    ar.fk_2nnp_sc_identif,
    ar.ka_appartementsindex,
    ar.ka_kad_gemeentecode,
    ar.ka_perceelnummer,
    ar.ka_sectie,
    kp.begrenzing_perceel
   FROM v_bd_app_re_all_kad_perceel v
     JOIN kad_perceel kp ON v.perceel_identif = kp.sc_kad_identif::varchar
     JOIN app_re ar ON v.app_re_identif = ar.sc_kad_identif::varchar;



