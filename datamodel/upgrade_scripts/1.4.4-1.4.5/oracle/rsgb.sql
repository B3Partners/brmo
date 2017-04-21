-- 
-- upgrade Oracle RSGB datamodel van 1.4.4 naar 1.4.5 
--
-- versienummer update
UPDATE brmo_metadata SET waarde='1.4.5' WHERE naam='brmoversie';
--
-- aanpassing van de views:
-- 
-- - v_bd_app_re_app_re
-- - v_bd_kad_perceel_with_app_re
-- - v_bd_app_re_kad_perceel
--
-- zie: https://github.com/B3Partners/brmo/issues/315
CREATE OR REPLACE VIEW v_bd_app_re_app_re AS
 SELECT b1.ref_id AS app_re_identif,
    b2.ref_id AS parent_app_re_identif
   FROM brondocument b1
     JOIN brondocument b2 ON b2.identificatie = b1.identificatie
  WHERE (b2.omschrijving = 'betrokkenBij Ondersplitsing' OR  b2.omschrijving = 'ontstaanUit HoofdSplitsing') AND b1.omschrijving = 'ontstaanUit Ondersplitsing'
  GROUP BY b1.ref_id, b2.ref_id;

CREATE OR REPLACE VIEW v_bd_kad_perceel_with_app_re AS
 SELECT DISTINCT b2.ref_id AS perceel_identif
   FROM brondocument b1
     JOIN brondocument b2 ON b2.identificatie = b1.identificatie
  WHERE b2.omschrijving = 'betrokkenBij HoofdSplitsing' AND (b1.omschrijving = 'ontstaanUit HoofdSplitsing' OR b1.omschrijving = 'ontstaanUit Ondersplitsing');

CREATE OR REPLACE VIEW v_bd_app_re_kad_perceel AS
 SELECT b1.ref_id AS app_re_identif,
    b2.ref_id AS perceel_identif
   FROM brondocument b1
     JOIN brondocument b2 ON b2.identificatie = b1.identificatie
  WHERE b2.omschrijving = 'betrokkenBij HoofdSplitsing' AND (b1.omschrijving = 'ontstaanUit HoofdSplitsing' OR b1.omschrijving = 'ontstaanUit Ondersplitsing')
  GROUP BY b1.ref_id, b2.ref_id;
