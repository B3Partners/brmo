--
-- upgrade RSGB datamodel van 1.3.6 naar 1.4.0 (MS SQLserver)
--
-- merge van de nieuwe waarden voor Aard Recht codelijst (issue#234)
MERGE INTO aard_recht_verkort t USING (
    VALUES 
        ('23','Opstalrecht Nutsvoorzieningen op gedeelte van perceel'),
        ('24','Zakelijk recht (als bedoeld in artikel 5, lid 3, onder b)')
    ) AS src (code,txt) ON t.aand = src.code
WHEN MATCHED THEN UPDATE SET omschr = src.txt
WHEN NOT MATCHED THEN INSERT (aand,omschr) VALUES (src.code, src.txt);

MERGE INTO aard_verkregen_recht t USING (
    VALUES 
        ('23','Opstalrecht Nutsvoorzieningen op gedeelte van perceel'),
        ('24','Zakelijk recht als bedoeld in artikel 5, lid 3, onder b, van de Belemmeringenwet Privaatrecht op gedeelte van perceel')
    ) AS src (code,txt) ON t.aand = src.code
WHEN MATCHED THEN UPDATE SET omschr_aard_verkregenr_recht = src.txt
WHEN NOT MATCHED THEN INSERT (aand,omschr_aard_verkregenr_recht) VALUES (src.code, src.txt);
