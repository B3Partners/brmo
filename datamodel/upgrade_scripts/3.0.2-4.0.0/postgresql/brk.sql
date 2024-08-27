-- 
-- upgrade PostgreSQL BRK datamodel van 3.0.2 naar 4.0.0 
--

CREATE SCHEMA IF NOT EXISTS brk;

SET search_path = brk,public;

SET SCHEMA 'brk';

-- view vb_util_zk_recht_op_koz wordt vervangen met onderstaand SQL ten behoeve van de upgrade
CREATE OR REPLACE VIEW 
    vb_util_zk_recht_op_koz
            (
             identificatie,
             rustop_zak_recht
            )
AS 
SELECT  qry.identificatie,
        qry.rustop_zak_recht
FROM    ( 
            SELECT  r.identificatie,
                    r.rustop                               AS rustop_zak_recht
            FROM    recht r
            UNION ALL
            -- [BRMO-336] wanneer een zakelijkrecht een eigendomsrecht belast
            SELECT ribm.isbelastmet                        AS identificatie,
                   r.rustop                                AS rustop_zak_recht
            FROM recht r
            LEFT JOIN recht_isbelastmet ribm ON r.identificatie = ribm.zakelijkrecht
            UNION ALL
             -- [BRMO-351] wanneer een zakelijkrecht een ander zakelijkrecht belast
            SELECT ribm2.isbelastmet                        AS identificatie,
                   r.rustop                                AS rustop_zak_recht
            FROM recht r
            LEFT JOIN recht_isbelastmet ribm ON r.identificatie = ribm.zakelijkrecht
            LEFT JOIN recht_isbelastmet ribm2 ON ribm.isbelastmet = ribm2.zakelijkrecht     
          ) qry
  WHERE split_part( qry.identificatie, ':', 1) = 'NL.IMKAD.ZakelijkRecht';

COMMENT ON COLUMN onroerendezaak.begingeldigheid IS 'BRMO: metadata tbv archivering, de toestandsdatum van het bericht.';
COMMENT ON COLUMN onroerendezaak.eindegeldigheid IS 'BRMO: metadata tbv archivering, de toestandsdatum van het opvolgende bericht.';
COMMENT ON COLUMN recht.begingeldigheid IS 'BRMO: metadata tbv archivering, de toestandsdatum van het bericht.';

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_3.0.2_naar_4.0.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='4.0.0' WHERE naam='brmoversie';
