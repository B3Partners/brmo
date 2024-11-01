-- 
-- upgrade Oracle BRK datamodel van 3.0.2 naar 4.0.0 
--

WHENEVER SQLERROR EXIT SQL.SQLCODE
BEGIN
    EXECUTE IMMEDIATE 'CREATE TABLE brmo_metadata(naam VARCHAR2(255 CHAR) NOT NULL, waarde CLOB, PRIMARY KEY (naam))';
EXCEPTION
WHEN OTHERS THEN
IF
    SQLCODE = -955 THEN
    NULL;
ELSE RAISE;
END IF;
END;
/
MERGE INTO brmo_metadata USING DUAL ON (naam = 'brmoversie') WHEN NOT MATCHED THEN INSERT (naam) VALUES('brmoversie');

-- view vb_util_zk_recht_op_koz wordt vervangen met onderstaand SQL ten behoeve van de upgrade
CREATE OR REPLACE VIEW vb_util_zk_recht_op_koz
            (
             identificatie,
             rustop_zak_recht
                )
AS
SELECT qry.identificatie,
       qry.rustop_zak_recht
FROM (SELECT r.identificatie,
             r.rustop         AS rustop_zak_recht
      FROM   recht r 
      UNION ALL
      -- [BRMO-336] wanneer een zakelijkrecht een eigendomsrecht belast
      SELECT ribm.isbelastmet AS identificatie,
             r.rustop         AS rustop_zak_recht
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
WHERE SUBSTR(qry.identificatie, 1, INSTR(qry.identificatie, ':') - 1) = 'NL.IMKAD.ZakelijkRecht';

-- view vb_util_zk_recht wordt aangepast ihkv BRMO-380
CREATE OR REPLACE VIEW vb_util_zk_recht
            (
             zr_identif,
             ingangsdatum_recht,
             aandeel,
             ar_teller,
             ar_noemer,
             subject_identif,
             mandeligheid_identif,
             koz_identif,
             indic_betrokken_in_splitsing,
             omschr_aard_verkregenr_recht,
             fk_3avr_aand,
             aantekeningen
                )
AS
SELECT zakrecht.identificatie                                            AS zr_identif,
       zakrecht.begingeldigheid                                          AS ingangsdatum_recht,
       COALESCE(TO_CHAR(tenaamstelling.aandeel_teller), '0') || '/' ||
       COALESCE(TO_CHAR(tenaamstelling.aandeel_noemer), '0')             AS aandeel,
       tenaamstelling.aandeel_teller                                     AS ar_teller,
       tenaamstelling.aandeel_noemer                                     AS ar_noemer,
       -- BRMO-339: samenvoegen van de tennamevan (tenaamstelling) en de heeftverenigingvaneigenaren, zodat de grondpercelen zichtbaar zijn
       -- BRMO-340: samenvoegen van de tennamevan (tenaamstelling) op de zakelijke rechten die bestemd zijn tot een mandeligheid
       COALESCE(tenaamstelling.tennamevan, '') || COALESCE(vve.heeftverenigingvaneigenaren, '') ||
       COALESCE(tenaamstelling2.tennamevan, '')                          AS subject_identif,
       -- BRMO-340: toevoegen van mandeligheidsidentificatie, zodat het duidelijk is dat het een mandelige zaak betreft.
       mandeligheid.identificatie                                        AS mandeligheid_identif,
       vuzrok.rustop_zak_recht                                                   AS koz_identif,
       CASE WHEN (zakrecht.isbetrokkenbij is not NULL) THEN 1 ELSE 0 END AS indic_betrokken_in_splitsing,
       zakrecht.aard                                                     AS omschr_aard_verkregen_recht,
       zakrecht.aard                                                     AS fk_3avr_aand,
       (SELECT LISTAGG(
                       'id: ' || COALESCE(aantekening.identificatie, '') || ', '
                           || 'aard: ' || COALESCE(aantekening.aard, '') || ', '
                           || 'begin: ' || COALESCE(TO_CHAR(aantekening.begingeldigheid), '') || ', '
                           || 'beschrijving: ' || COALESCE(aantekening.omschrijving, '') || ', '
                           || 'eind: ' || COALESCE(TO_CHAR(aantekening.einddatum), '') || ', '
                           || 'koz-id: ' || COALESCE(aantekening.aantekeningkadastraalobject, '') || ', '
                           || 'subject-id: ' || COALESCE(aantekening.betrokkenpersoon, '') || '; ', ' & ' ON OVERFLOW
                       TRUNCATE WITH COUNT)
                       WITHIN GROUP ( ORDER BY aantekening.aantekeningkadastraalobject ) AS aantekeningen
        FROM recht aantekening
        WHERE aantekening.aantekeningkadastraalobject = zakrecht.rustop) AS aantekeningen
FROM recht zakrecht
         -- tenaamstelling
         LEFT JOIN recht tenaamstelling ON zakrecht.identificatie = tenaamstelling.van
    -- vereniging van eigenaren
         LEFT JOIN recht vve ON zakrecht.isbetrokkenbij = vve.identificatie
         LEFT JOIN vb_util_zk_recht_op_koz vuzrok ON zakrecht.identificatie = vuzrok.identificatie
    -- mandeligheid
         LEFT JOIN recht mandeligheid ON zakrecht.isbestemdtot = mandeligheid.identificatie
         LEFT JOIN vb_util_zk_recht_op_koz vuzrok2 ON mandeligheid.heefthoofdzaak = vuzrok2.rustop_zak_recht
         LEFT JOIN recht tenaamstelling2 ON vuzrok2.identificatie = tenaamstelling2.van
WHERE SUBSTR(zakrecht.identificatie, 1, INSTR(zakrecht.identificatie, ':') - 1) = 'NL.IMKAD.ZakelijkRecht';
COMMENT ON TABLE vb_util_zk_recht IS
    'commentaar view vb_util_zk_recht:
    zakelijk recht met opgezocht aard recht en berekend aandeel
        beschikbare kolommen:
    * zr_identif: natuurlijke id van zakelijk recht
    * ingangsdatum_recht: -
    * aandeel: samenvoeging van teller en noemer (1/2),
    * ar_teller: teller van aandeel,
    * ar_noemer: noemer van aandeel,
    * subject_identif: natuurlijk id van subject (natuurlijk of niet natuurlijk) welke rechthebbende is,
    * mandeligheid_identif: identificatie van een mandeligheid, een gemeenschappelijk eigendom van een onroerende zaak,
    * koz_identif: natuurlijk id van kadastrale onroerende zaak (perceel of appratementsrecht) dat gekoppeld is,
    * indic_betrokken_in_splitsing: -,
    * omschr_aard_verkregen_recht: tekstuele omschrijving aard recht,
    * fk_3avr_aand: code aard recht,
    * aantekeningen: samenvoeging van alle aantekening op dit recht';

COMMENT ON COLUMN onroerendezaak.begingeldigheid IS 'BRMO: metadata tbv archivering, de toestandsdatum van het bericht.';
COMMENT ON COLUMN onroerendezaak.eindegeldigheid IS 'BRMO: metadata tbv archivering, de toestandsdatum van het opvolgende bericht.';
COMMENT ON COLUMN recht.begingeldigheid IS 'BRMO: metadata tbv archivering, de toestandsdatum van het bericht.';

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_3.0.2_naar_4.0.0','vorige versie was ' || waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='4.0.0' WHERE naam='brmoversie';
