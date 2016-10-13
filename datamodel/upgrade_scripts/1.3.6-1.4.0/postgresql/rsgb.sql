--
-- upgrade RSGB datamodel van 1.3.6 naar 1.4.0 (PostgreSQL)
--
-- upsert van de nieuwe waarden voor Aard Recht codelijst (issue#234)
WITH new_values (id, txt) AS (VALUES
        ('23','Opstalrecht Nutsvoorzieningen op gedeelte van perceel'),
        ('24','Zakelijk recht (als bedoeld in artikel 5, lid 3, onder b)')
    ), upsert AS (
        UPDATE aard_recht_verkort m SET omschr = nv.txt
        FROM new_values nv WHERE  m.aand = nv.id RETURNING m.* 
    )
INSERT INTO aard_recht_verkort (aand, omschr) SELECT id, txt FROM new_values WHERE NOT EXISTS (SELECT 1 FROM upsert up WHERE up.aand = new_values.id);
                  
WITH new_values (id, txt) AS (VALUES
        ('23','Opstalrecht Nutsvoorzieningen op gedeelte van perceel'),
        ('24','Zakelijk recht als bedoeld in artikel 5, lid 3, onder b, van de Belemmeringenwet Privaatrecht op gedeelte van perceel')
    ), upsert AS (
        UPDATE aard_verkregen_recht m SET omschr_aard_verkregenr_recht = nv.txt
        FROM new_values nv WHERE  m.aand = nv.id RETURNING m.* 
    )
INSERT INTO aard_verkregen_recht (aand, omschr_aard_verkregenr_recht) SELECT id, txt FROM new_values WHERE NOT EXISTS (SELECT 1 FROM upsert up WHERE up.aand = new_values.id);