-- 
-- upgrade Oracle RSGB datamodel van 2.2.2 naar 2.3.0 
--

WHENEVER SQLERROR EXIT SQL.SQLCODE

CREATE OR REPLACE VIEW vb_util_app_re_splitsing AS
    SELECT
        b1.ref_id   AS child_identif,
        MIN(b2.ref_id) AS parent_identif
    FROM
        brondocument b1
        JOIN brondocument b2 ON b2.identificatie = b1.identificatie
    WHERE
        ( b2.omschrijving = 'betrokkenBij Ondersplitsing'
          OR b2.omschrijving = 'betrokkenBij HoofdSplitsing'
          OR  b2.omschrijving = 'betrokkenBij SplitsingAfkoopErfpacht')
        AND ( b1.omschrijving = 'ontstaanUit Ondersplitsing'
              OR b1.omschrijving = 'ontstaanUit HoofdSplitsing'
              OR  b1.omschrijving = 'ontstaanUit SplitsingAfkoopErfpacht')
    GROUP BY
        b1.ref_id;


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_2.2.2_naar_2.3.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='2.3.0' WHERE naam='brmoversie';
