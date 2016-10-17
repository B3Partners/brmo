--
-- upgrade RSGB datamodel van 1.3.6 naar 1.4.0 (Oracle)
--
-- merge van de nieuwe waarden voor Aard Recht codelijst (issue#234)
MERGE INTO aard_recht_verkort USING dual ON (aand='23')
WHEN MATCHED THEN UPDATE SET omschr='Opstalrecht Nutsvoorzieningen op gedeelte van perceel'
WHEN NOT MATCHED THEN INSERT (aand, omschr) VALUES ('23','Opstalrecht Nutsvoorzieningen op gedeelte van perceel');

MERGE INTO aard_recht_verkort USING dual ON (aand='24')
WHEN MATCHED THEN UPDATE SET omschr='Zakelijk recht (als bedoeld in artikel 5, lid 3, onder b)'
WHEN NOT MATCHED THEN INSERT (aand, omschr) VALUES ('24','Zakelijk recht (als bedoeld in artikel 5, lid 3, onder b)');

MERGE INTO aard_verkregen_recht USING dual ON (aand='23')
WHEN MATCHED THEN UPDATE SET omschr_aard_verkregenr_recht='Opstalrecht Nutsvoorzieningen op gedeelte van perceel'
WHEN NOT MATCHED THEN INSERT (aand, omschr_aard_verkregenr_recht) VALUES ('23','Opstalrecht Nutsvoorzieningen op gedeelte van perceel');
  
MERGE INTO aard_verkregen_recht USING dual ON (aand='24')
WHEN MATCHED THEN UPDATE SET omschr_aard_verkregenr_recht='Zakelijk recht als bedoeld in artikel 5, lid 3, onder b, van de Belemmeringenwet Privaatrecht op gedeelte van perceel'
WHEN NOT MATCHED THEN INSERT (aand, omschr_aard_verkregenr_recht) VALUES ('24','Zakelijk recht als bedoeld in artikel 5, lid 3, onder b, van de Belemmeringenwet Privaatrecht op gedeelte van perceel');
