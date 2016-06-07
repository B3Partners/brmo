BEGIN TRANSACTION;
-- vervallen gemeenten per 1 jan 2014
-- http://www.cbs.nl/nl-NL/menu/methoden/classificaties/overzicht/gemeentelijke-indeling/2014/default.htm
-- Op 1 januari 2014 is het aantal gemeenten afgenomen met vijf, zodat het aantal gemeenten in Nederland 403 bedraagt.
UPDATE gemeente SET datum_einde_geldh = '2014-01-01', dat_beg_geldh = '2009-01-01' WHERE code IN (
-- Boskoop (0499) en Rijnwoude (1672)
   499, 1672,
-- Gaasterlân-Sleat (0653), Lemsterland (0082), Skasterlân (0051), Boarnsterhim1 (0055)
   653, 82, 51, 55
);
INSERT INTO gemeente_archief SELECT * FROM gemeente WHERE code IN (
-- Boskoop (0499) en Rijnwoude (1672)
   499, 1672,
-- Gaasterlân-Sleat (0653), Lemsterland (0082), Skasterlân (0051), Boarnsterhim1 (0055)
   653, 82, 51, 55
);
DELETE FROM gemeente WHERE code IN (
-- Boskoop (0499) en Rijnwoude (1672)
   499, 1672,
-- Gaasterlân-Sleat (0653), Lemsterland (0082), Skasterlân (0051), Boarnsterhim1 (0055)
   653, 82, 51, 55
);
COMMIT;




BEGIN TRANSACTION;
-- vervallen gemeenten per 1 jan 2015
-- http://www.cbs.nl/nl-NL/menu/methoden/classificaties/overzicht/gemeentelijke-indeling/2015/default.htm
-- Op 1 januari 2015 is het aantal gemeenten afgenomen met tien, zodat het aantal gemeenten in Nederland 393 bedraagt.
UPDATE gemeente SET datum_einde_geldh = '2015-01-01', dat_beg_geldh = '2009-01-01' WHERE code IN (
-- Graft-De Rijp (0365) & Schermer (0458) te vervallen
   365,458,
-- Millingen aan de Rijn (0265) & Ubbergen (0282) komen te vervallen
   265,282,
-- Bernisse (0568) & Spijkenisse (0612) komen te vervallen
   568,612,
-- Bergambacht (0491), Nederlek (0643), Ouderkerk (0644), Schoonhoven (0608) en Vlist (0623) komen te vervallen
   491,643,644,608,623,
-- Maasdonk (1671) komt te vervallen
   1671
);
INSERT INTO gemeente_archief SELECT * FROM gemeente WHERE code IN (
-- Graft-De Rijp (0365) & Schermer (0458) te vervallen
   365,458,
-- Millingen aan de Rijn (0265) & Ubbergen (0282) komen te vervallen
   265,282,
-- Bernisse (0568) & Spijkenisse (0612) komen te vervallen
   568,612,
-- Bergambacht (0491), Nederlek (0643), Ouderkerk (0644), Schoonhoven (0608) en Vlist (0623) komen te vervallen
   491,643,644,608,623,
-- Maasdonk (1671) komt te vervallen
   1671
);
DELETE FROM gemeente WHERE code IN (
-- Graft-De Rijp (0365) & Schermer (0458) te vervallen
   365,458,
-- Millingen aan de Rijn (0265) & Ubbergen (0282) komen te vervallen
   265,282,
-- Bernisse (0568) & Spijkenisse (0612) komen te vervallen
   568,612,
-- Bergambacht (0491), Nederlek (0643), Ouderkerk (0644), Schoonhoven (0608) en Vlist (0623) komen te vervallen
   491,643,644,608,623,
-- Maasdonk (1671) komt te vervallen
   1671
);
COMMIT;





BEGIN TRANSACTION;
-- vervallen gemeenten per 1 juli 2015
UPDATE gemeente SET datum_einde_geldh = '2015-07-01', dat_beg_geldh = '2009-01-01' WHERE code IN (
-- De Friese Meren (1921) is hernoemt naar De Fryske Marren (1940)
  1921
);
INSERT INTO gemeente_archief SELECT * FROM gemeente WHERE code IN (
-- De Friese Meren (1921) is hernoemt naar De Fryske Marren (1940)
  1921
);
DELETE FROM gemeente WHERE code IN (
-- De Friese Meren (1921) is hernoemt naar De Fryske Marren (1940)
  1921
);
COMMIT;





BEGIN TRANSACTION;
-- vervallen gemeenten per 1 jan 2016
-- Op 1 januari 2016 is het aantal gemeenten afgenomen met drie, zodat het aantal gemeenten in Nederland 390 bedraagt.
-- zie: http://www.cbs.nl/nl-NL/menu/methoden/classificaties/overzicht/gemeentelijke-indeling/2016/default.htm
UPDATE gemeente SET datum_einde_geldh = '2016-01-01', dat_beg_geldh = '2009-01-01' WHERE code IN (
-- Groesbeek (0241) is hernoemt naar Berg en Dal (1945)
   241,
-- Bussum (0381), Muiden (0424) & Naarden (0425) komen te vervallen
   381,424,425,
-- Zeevang (0478) komt te vervallen
   478
);
INSERT INTO gemeente_archief SELECT * FROM gemeente WHERE code IN (
-- Groesbeek (0241) is hernoemt naar Berg en Dal (1945)
   241,
-- Bussum (0381), Muiden (0424) & Naarden (0425) komen te vervallen
   381,424,425,
-- Zeevang (0478) komt te vervallen
   478
);
DELETE FROM gemeente WHERE code IN (
-- Groesbeek (0241) is hernoemt naar Berg en Dal (1945)
   241,
-- Bussum (0381), Muiden (0424) & Naarden (0425) komen te vervallen
   381,424,425,
-- Zeevang (0478) komt te vervallen
   478
);
COMMIT;
