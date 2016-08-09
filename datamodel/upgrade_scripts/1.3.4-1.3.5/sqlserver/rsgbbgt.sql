-- upgrade RSGBBGT datamodel van 1.3.4 naar 1.3.5 (MS SQL server)

-- fix verkeerde data in openbareruimtelabel tabel (zie https://github.com/B3Partners/brmo/issues/192)
-- kopieer de data uit oprnm_pos_2_punt naar geom2d waar deze initieel geladen zou zijn
-- het is mogelijk dat hierbij data die werkelijk oprnm_pos_2_punt is in oprnm_pos_1_punt terecht komt
-- dat is alleen te verhelpen door de GML bestanden opnieuw te transformeren
UPDATE openbareruimtelabel
   SET geom2d = oprnm_pos_2_punt
   WHERE geom2d is null;
