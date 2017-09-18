-- 
-- upgrade PostgreSQL RSGB datamodel van 1.4.5 naar 1.5.0 
--

-- pas view v_bd_app_re_all_kad_perceel aan om te filteren tegen actuele appartementsrechten issue#331
CREATE OR REPLACE VIEW v_bd_app_re_all_kad_perceel AS
WITH recursive related_app_re (
        app_re_identif, perceel_identif
    ) AS (
        SELECT b1.ref_id AS app_re_identif, b2.ref_id AS perceel_identif
        FROM brondocument b1
        JOIN brondocument b2
        ON b2.identificatie = b1.identificatie
        WHERE b2.omschrijving = 'betrokkenBij HoofdSplitsing'
          AND (b1.omschrijving = 'ontstaanUit HoofdSplitsing' OR  b1.omschrijving = 'ontstaanUit Ondersplitsing')
        GROUP BY b1.ref_id, b2.ref_id
        UNION
        SELECT vaa.app_re_identif, vap.perceel_identif
        FROM v_bd_app_re_app_re vaa
        JOIN related_app_re vap
        ON vaa.parent_app_re_identif = vap.app_re_identif
        GROUP BY vaa.app_re_identif, vap.perceel_identif
    )
SELECT app_re.sc_kad_identif::VARCHAR(50) AS app_re_identif, rar.perceel_identif
FROM related_app_re rar
LEFT JOIN app_re
ON app_re.sc_kad_identif::text = rar.app_re_identif;

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.4.5_naar_1.5.0','vorige versie was '||waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.5.0' WHERE naam='brmoversie';
