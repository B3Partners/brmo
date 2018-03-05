-- https://discussie.kinggemeenten.nl/discussie/gemma/stuf-bg-310/mapping-soort-identiteitsbewijs-0204-met-reisdocumentsoort-0310

INSERT ALL
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('1','paspoort')
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('2','Europese identiteitskaart')
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('3','toeristenkaart')
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('4','gemeentelijke identiteitskaart')
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('5','verblijfsdocument van de Vreemdelingendienst')
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('6','vluchtelingenpaspoort')
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('7','vreemdelingenpaspoort')
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('8','paspoort met aantekening vergunning tot verblijf')
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('9','(electronisch) W-document')
SELECT 1 FROM DUAL;
