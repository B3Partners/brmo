INSERT INTO brmo_metadata (naam, waarde)
  SELECT 'update_gem_tabel',(SELECT waarde FROM brmo_metadata WHERE naam = 'brmoversie')
  WHERE NOT EXISTS (SELECT naam FROM brmo_metadata WHERE naam = 'update_gem_tabel');

/*
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


BEGIN TRANSACTION;
-- vervallen gemeenten per 1 juli 2015
UPDATE gemeente SET datum_einde_geldh = '2015-07-01', dat_beg_geldh = '2014-01-01' WHERE code IN (
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
*/




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

IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1945) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2016-01-01', 1945, 'Berg en Dal');
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1942) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2016-01-01', 1942, 'Gooise Meren');

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
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1948) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2017-01-01', 1948, 'Meierijstad');
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
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1949) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2017-01-01', 1949, 'Waadhoeke');
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1950) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2017-01-01', 1950, 'Westerwolde');
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1952) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2017-01-01', 1952, 'Midden-Groningen');
UPDATE brmo_metadata SET waarde = '2018' WHERE naam = 'update_gem_tabel';
COMMIT;


BEGIN TRANSACTION;
-- vervallen gemeenten per 1 jan 2019
-- Op 1 januari 2019 is het aantal gemeenten afgenomen met vijfentwintig, zodat het aantal gemeenten in Nederland 355 bedraagt.
-- zie: https://www.cbs.nl/nl-nl/onze-diensten/methoden/classificaties/overig/gemeentelijke-indelingen-per-jaar/indeling%20per%20jaar/gemeentelijke-indeling-op-1-januari-2019
UPDATE gemeente SET datum_einde_geldh = '2019-01-01', dat_beg_geldh = '2009-01-01' WHERE code IN (
-- Bedum (0005), Eemsmond (1651), De Marne (1663) en een deel van Winsum (0053) komen te vervallen
5, 1651, 1663, 53,
-- Ten Boer (0009) en Haren (0017) komen te vervallen
9, 17,
-- Grootegast (0015), Leek (0022), Marum (0025), Zuidhorn (0056) en een deel van Winsum (0053) komen te vervallen
15, 22, 25, 56,
-- 53,
-- Dongeradeel (0058), Kollumerland en Nieuwkruisland (0079) en Ferwerderadiel (1722) komen te vervallen
58, 79, 1722,
-- Geldermalsen (0236), Neerijnen (0304) en Lingewaal (0733) komen te vervallen en
236, 304, 733,
-- Haarlemmerliede en Spaarnwoude (0393) komt te vervallen
393,
-- Leerdam (0545), Vianen (0620) en Zederik (0707) komen te vervallen
545, 620, 707,
-- Noordwijkerhout (0576) komt te vervallen
576,
-- Oud-Beijerland (0584), Binnenmaas (0585), Korendijk (0588), Cromstrijen (0611) en Strijen (0617) komen te vervallen
584, 585, 588, 611, 617,
-- Giessenlanden (0689) en Molenwaard (1927) komen te vervallen
689, 1927,
-- Aalburg (0738), Werkendam (0870) en Woudrichem (0874) komen te vervallen
738, 870, 874,
-- Onderbanken (0881), Nuth (0951) en Schinnen (0962) komen te vervallen
881, 951, 962
);
INSERT INTO gemeente_archief SELECT * FROM gemeente WHERE code IN (
-- Bedum (0005), Eemsmond (1651), De Marne (1663) en een deel van Winsum (0053) komen te vervallen
5, 1651, 1663, 53,
-- Ten Boer (0009) en Haren (0017) komen te vervallen
9, 17,
-- Grootegast (0015), Leek (0022), Marum (0025), Zuidhorn (0056) en een deel van Winsum (0053) komen te vervallen
15, 22, 25, 56,
-- 53,
-- Dongeradeel (0058), Kollumerland en Nieuwkruisland (0079) en Ferwerderadiel (1722) komen te vervallen
58, 79, 1722,
-- Geldermalsen (0236), Neerijnen (0304) en Lingewaal (0733) komen te vervallen en
236, 304, 733,
-- Haarlemmerliede en Spaarnwoude (0393) komt te vervallen
393,
-- Leerdam (0545), Vianen (0620) en Zederik (0707) komen te vervallen
545, 620, 707,
-- Noordwijkerhout (0576) komt te vervallen
576,
-- Oud-Beijerland (0584), Binnenmaas (0585), Korendijk (0588), Cromstrijen (0611) en Strijen (0617) komen te vervallen
584, 585, 588, 611, 617,
-- Giessenlanden (0689) en Molenwaard (1927) komen te vervallen
689, 1927,
-- Aalburg (0738), Werkendam (0870) en Woudrichem (0874) komen te vervallen
738, 870, 874,
-- Onderbanken (0881), Nuth (0951) en Schinnen (0962) komen te vervallen
881, 951, 962
);
UPDATE wnplts SET fk_7gem_code=null WHERE fk_7gem_code IN (
-- Bedum (0005), Eemsmond (1651), De Marne (1663) en een deel van Winsum (0053) komen te vervallen
5, 1651, 1663, 53,
-- Ten Boer (0009) en Haren (0017) komen te vervallen
9, 17,
-- Grootegast (0015), Leek (0022), Marum (0025), Zuidhorn (0056) en een deel van Winsum (0053) komen te vervallen
15, 22, 25, 56,
-- 53,
-- Dongeradeel (0058), Kollumerland en Nieuwkruisland (0079) en Ferwerderadiel (1722) komen te vervallen
58, 79, 1722,
-- Geldermalsen (0236), Neerijnen (0304) en Lingewaal (0733) komen te vervallen en
236, 304, 733,
-- Haarlemmerliede en Spaarnwoude (0393) komt te vervallen
393,
-- Leerdam (0545), Vianen (0620) en Zederik (0707) komen te vervallen
545, 620, 707,
-- Noordwijkerhout (0576) komt te vervallen
576,
-- Oud-Beijerland (0584), Binnenmaas (0585), Korendijk (0588), Cromstrijen (0611) en Strijen (0617) komen te vervallen
584, 585, 588, 611, 617,
-- Giessenlanden (0689) en Molenwaard (1927) komen te vervallen
689, 1927,
-- Aalburg (0738), Werkendam (0870) en Woudrichem (0874) komen te vervallen
738, 870, 874,
-- Onderbanken (0881), Nuth (0951) en Schinnen (0962) komen te vervallen
881, 951, 962
);
DELETE FROM gemeente WHERE code IN (
-- Bedum (0005), Eemsmond (1651), De Marne (1663) en een deel van Winsum (0053) komen te vervallen
5, 1651, 1663, 53,
-- Ten Boer (0009) en Haren (0017) komen te vervallen
9, 17,
-- Grootegast (0015), Leek (0022), Marum (0025), Zuidhorn (0056) en een deel van Winsum (0053) komen te vervallen
15, 22, 25, 56,
-- 53,
-- Dongeradeel (0058), Kollumerland en Nieuwkruisland (0079) en Ferwerderadiel (1722) komen te vervallen
58, 79, 1722,
-- Geldermalsen (0236), Neerijnen (0304) en Lingewaal (0733) komen te vervallen en
236, 304, 733,
-- Haarlemmerliede en Spaarnwoude (0393) komt te vervallen
393,
-- Leerdam (0545), Vianen (0620) en Zederik (0707) komen te vervallen
545, 620, 707,
-- Noordwijkerhout (0576) komt te vervallen
576,
-- Oud-Beijerland (0584), Binnenmaas (0585), Korendijk (0588), Cromstrijen (0611) en Strijen (0617) komen te vervallen
584, 585, 588, 611, 617,
-- Giessenlanden (0689) en Molenwaard (1927) komen te vervallen
689, 1927,
-- Aalburg (0738), Werkendam (0870) en Woudrichem (0874) komen te vervallen
738, 870, 874,
-- Onderbanken (0881), Nuth (0951) en Schinnen (0962) komen te vervallen
881, 951, 962
);

IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1954) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2019-01-01', 1954, 'Beekdaelen');
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1959) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2019-01-01', 1959, 'Altena');
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1960) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2019-01-01', 1960, 'West Betuwe');
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1961) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2019-01-01', 1961, 'Vijfheerenlanden');
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1963) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2019-01-01', 1963, 'Hoeksche Waard');
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1966) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2019-01-01', 1966, 'Het Hogeland');
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1969) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2019-01-01', 1969, 'Westerkwartier');
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1970) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2019-01-01', 1970, 'Noardeast-Fryslân');
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1978) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2019-01-01', 1978, 'Molenlanden');

-- herstel onterecht verwijderde gemeente Oudewater die in bestand van 8 jan 2019 is geslopen (update_gem_tabel=2019)
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 589) INSERT INTO gemeente (code, naam) VALUES (589, 'Oudewater');
DELETE FROM gemeente_archief WHERE code = 589;

UPDATE brmo_metadata SET waarde = '2019.1' WHERE naam = 'update_gem_tabel';
COMMIT;

BEGIN TRANSACTION;
-- Samenvoeging van de gemeenten Appingedam (3), Delfzijl (10) en Loppersum (24) tot een nieuwe gemeente Eemsdelta (1979)
-- Opsplitsen (en opheffen) van gemeente Haaren (788)
UPDATE gemeente SET datum_einde_geldh = '2021-01-01', dat_beg_geldh = '2009-01-01' WHERE code IN (3, 10, 24, 788) AND dat_beg_geldh IS NULL;
INSERT INTO gemeente_archief SELECT * FROM gemeente WHERE code IN (3, 10, 24, 788);
UPDATE wnplts SET fk_7gem_code=null WHERE fk_7gem_code IN (3, 10, 24, 788);
DELETE FROM gemeente WHERE code IN (3, 10, 24, 788);

-- nieuw
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1979) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2021-01-01', 1979, 'Eemsdelta');

UPDATE brmo_metadata SET waarde = '2021.0' WHERE naam = 'update_gem_tabel';
COMMIT;

BEGIN TRANSACTION;
-- Samenvoegen van gemeente Beemster (370) met Purmerend (439)
-- Samenvoeging van de gemeenten Heerhugowaard (398) en Langedijk (416) tot een nieuwe gemeente Dijk en Waard (1980)
-- Samenvoeging van de gemeenten Landerd (1685) en Uden (856) tot een nieuwe gemeente Maashorst (1991)
-- Samenvoeging van de gemeenten Boxmeer (756), Cuijk (1684), Grave (786), Mill en Sint Hubert (815)
--      en Sint Anthonis (1702) tot een nieuwe gemeente Land van Cuijk (1982)
UPDATE gemeente SET datum_einde_geldh = '2022-01-01' WHERE code IN (370, 398, 416, 1685, 856, 756, 1684, 786, 1702);
UPDATE gemeente SET dat_beg_geldh = '2009-01-01'     WHERE code IN (370, 398, 416, 1685, 856, 756, 1684, 786, 1702) AND dat_beg_geldh IS NULL;

INSERT INTO gemeente_archief SELECT * FROM gemeente WHERE code IN (370, 398, 416, 1685, 856, 756, 1684, 786, 1702);
UPDATE wnplts SET fk_7gem_code=null WHERE fk_7gem_code         IN (370, 398, 416, 1685, 856, 756, 1684, 786, 1702);
DELETE FROM gemeente WHERE code                                IN (370, 398, 416, 1685, 856, 756, 1684, 786, 1702);

-- nieuw
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1980) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2022-01-01', 1980, 'Dijk en Waard');
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1991) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2022-01-01', 1991, 'Maashorst');
IF NOT EXISTS (SELECT * FROM gemeente WHERE code = 1982) INSERT INTO gemeente (dat_beg_geldh, code, naam) VALUES ('2022-01-01', 1982, 'Land van Cuijk');

UPDATE brmo_metadata SET waarde = '2022.0' WHERE naam = 'update_gem_tabel';
COMMIT;


BEGIN TRANSACTION;
-- Samenvoegen van gemeente Weesp (457) met Amsterdam (363)
UPDATE gemeente SET datum_einde_geldh = '2022-03-24' WHERE code IN (457);
UPDATE gemeente SET dat_beg_geldh = '2009-01-01'     WHERE code IN (457) AND dat_beg_geldh IS NULL;

INSERT INTO gemeente_archief SELECT * FROM gemeente WHERE code IN (457);
UPDATE wnplts SET fk_7gem_code=null WHERE fk_7gem_code         IN (457);
DELETE FROM gemeente WHERE code                                IN (457);

UPDATE brmo_metadata SET waarde = '2022.1' WHERE naam = 'update_gem_tabel';
COMMIT;