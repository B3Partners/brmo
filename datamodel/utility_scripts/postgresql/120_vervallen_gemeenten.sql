INSERT INTO brmo_metadata (naam,waarde)
  SELECT 'update_gem_tabel',(SELECT waarde FROM brmo_metadata WHERE naam = 'brmoversie')
  WHERE NOT EXISTS (SELECT 1 FROM brmo_metadata WHERE naam = 'update_gem_tabel');

BEGIN TRANSACTION;
-- vervallen gemeenten per 1 jan 2014
-- https://www.cbs.nl/nl-nl/onze-diensten/methoden/classificaties/overig/gemeentelijke-indelingen-per-jaar/indeling%20per%20jaar/gemeentelijke-indeling-op-1-januari-2014
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
UPDATE wnplts SET fk_7gem_code=null WHERE fk_7gem_code IN (
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
--hebben we al: INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2014-01-01', 1921, 'De Friese Meren');
UPDATE brmo_metadata SET waarde = '2014' WHERE naam = 'update_gem_tabel';
COMMIT;


/*
BEGIN TRANSACTION;
-- vervallen gemeenten per 1 jan 2015
-- https://www.cbs.nl/nl-nl/onze-diensten/methoden/classificaties/overig/gemeentelijke-indelingen-per-jaar/indeling%20per%20jaar/gemeentelijke-indeling-op-1-januari-2015
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
INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2015-01-01', 1930, 'Nissewaard');
INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2015-01-01', 1931, 'Krimpenerwaard');
UPDATE brmo_metadata SET waarde = 'jan2015' WHERE naam = 'update_gem_tabel';
COMMIT;
*/

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
UPDATE wnplts SET fk_7gem_code=null WHERE fk_7gem_code IN (
-- De Friese Meren (1921) is hernoemt naar De Fryske Marren (1940)
  1921
);
DELETE FROM gemeente WHERE code IN (
-- De Friese Meren (1921) is hernoemt naar De Fryske Marren (1940)
  1921
);
INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2015-07-01', 1940, 'De Fryske Marren');
UPDATE brmo_metadata SET waarde = 'jul2015' WHERE naam = 'update_gem_tabel';
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
UPDATE wnplts SET fk_7gem_code=null WHERE fk_7gem_code IN (
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
INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2016-01-01', 1945, 'Berg en Dal');
INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2016-01-01', 1942, 'Gooise Meren');
UPDATE brmo_metadata SET waarde = '2016' WHERE naam = 'update_gem_tabel';
COMMIT;


BEGIN TRANSACTION;
-- vervallen gemeenten per 1 jan 2017
-- Op 1 januari 2017 is het aantal gemeenten afgenomen met drie
-- zie: https://www.cbs.nl/nl-nl/onze-diensten/methoden/classificaties/overig/gemeentelijke-indelingen-per-jaar/indeling%20per%20jaar/gemeentelijke-indeling-op-1-januari-2017
UPDATE gemeente SET datum_einde_geldh = '2017-01-01', dat_beg_geldh = '2009-01-01' WHERE code IN (
-- Schijndel (0844), Sint-Oedenrode (0846) en Veghel (0860) komen te vervallen
   844,846,860
);
INSERT INTO gemeente_archief SELECT * FROM gemeente WHERE code IN (
-- Schijndel (0844), Sint-Oedenrode (0846) en Veghel (0860) komen te vervallen
   844,846,860
);
UPDATE wnplts SET fk_7gem_code=null WHERE fk_7gem_code IN (
-- Schijndel (0844), Sint-Oedenrode (0846) en Veghel (0860) komen te vervallen
   844,846,860
);
DELETE FROM gemeente WHERE code IN (
-- Schijndel (0844), Sint-Oedenrode (0846) en Veghel (0860) komen te vervallen
   844,846,860
);
INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2017-01-01', 1948, 'Meierijstad');
UPDATE brmo_metadata SET waarde = '2017' WHERE naam = 'update_gem_tabel';
COMMIT;



BEGIN TRANSACTION;
-- vervallen gemeenten per 1 jan 2018
-- Op 1 januari 2018 is het aantal gemeenten afgenomen met elf
-- zie: https://www.cbs.nl/nl-nl/onze-diensten/methoden/classificaties/overig/gemeentelijke-indelingen-per-jaar/indeling%20per%20jaar/gemeentelijke-indeling-op-1-januari-2018
UPDATE gemeente SET datum_einde_geldh = '2018-01-01', dat_beg_geldh = '2009-01-01' WHERE code IN (
-- Bellingwedde (0007) en Vlagtwedde (0048) komen te vervallen
   07,48,
-- Hoogezand-Sappenmeer (0018), Slochteren (0040) en Menterwolde (1987) komen te vervallen
   18,40,1987,
-- het Bildt (0063), Franekeradeel (0070) en Menameradiel (1908) komen te vervallen
   63,70,1908,
-- Littenseradiel (0140) wordt heringedeeld in Leeuwarden, Súdwest-Fryslân en Waadhoeke.
   140,
-- Leeuwarderadeel (0081) gaat op in Leeuwarden
   81,
-- Rijnwaarden (0196) gaat op in Zevenaar
   196
);
INSERT INTO gemeente_archief SELECT * FROM gemeente WHERE code IN (
-- Bellingwedde (0007) en Vlagtwedde (0048) komen te vervallen
   07,48,
-- Hoogezand-Sappenmeer (0018), Slochteren (0040) en Menterwolde (1987) komen te vervallen
   18,40,1987,
-- het Bildt (0063), Franekeradeel (0070) en Menameradiel (1908) komen te vervallen
   63,70,1908,
-- Littenseradiel (0140) wordt heringedeeld in Leeuwarden, Súdwest-Fryslân en Waadhoeke.
   140,
-- Leeuwarderadeel (0081) gaat op in Leeuwarden
   81,
-- Rijnwaarden (0196) gaat op in Zevenaar
   196
);
UPDATE wnplts SET fk_7gem_code=null WHERE fk_7gem_code IN (
-- Bellingwedde (0007) en Vlagtwedde (0048) komen te vervallen
   07,48,
-- Hoogezand-Sappenmeer (0018), Slochteren (0040) en Menterwolde (1987) komen te vervallen
   18,40,1987,
-- het Bildt (0063), Franekeradeel (0070) en Menameradiel (1908) komen te vervallen
   63,70,1908,
-- Littenseradiel (0140) wordt heringedeeld in Leeuwarden, Súdwest-Fryslân en Waadhoeke.
   140,
-- Leeuwarderadeel (0081) gaat op in Leeuwarden
   81,
-- Rijnwaarden (0196) gaat op in Zevenaar
   196
);
DELETE FROM gemeente WHERE code IN (
-- Bellingwedde (0007) en Vlagtwedde (0048) komen te vervallen
   07,48,
-- Hoogezand-Sappenmeer (0018), Slochteren (0040) en Menterwolde (1987) komen te vervallen
   18,40,1987,
-- het Bildt (0063), Franekeradeel (0070) en Menameradiel (1908) komen te vervallen
   63,70,1908,
-- Littenseradiel (0140) wordt heringedeeld in Leeuwarden, Súdwest-Fryslân en Waadhoeke.
   140,
-- Leeuwarderadeel (0081) gaat op in Leeuwarden
   81,
-- Rijnwaarden (0196) gaat op in Zevenaar
   196
);
INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2017-01-01', 1949, 'Waadhoeke');
INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2017-01-01', 1950, 'Westerwolde');
INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2017-01-01', 1952, 'Midden-Groningen');
UPDATE brmo_metadata SET waarde = '2018' WHERE naam = 'update_gem_tabel';
COMMIT;
