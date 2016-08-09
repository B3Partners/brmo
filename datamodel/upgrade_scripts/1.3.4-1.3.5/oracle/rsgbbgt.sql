-- upgrade RSGBBGT datamodel van 1.3.4 naar 1.3.5 (Oracle)

-- fix verkeerde data in openbareruimtelabel tabel (zie https://github.com/B3Partners/brmo/issues/192)
-- kopieer de data uit oprnm_pos_2_punt naar geom2d waar deze initieel geladen zou zijn
-- het is mogelijk dat hierbij data die werkelijk oprnm_pos_2_punt is in oprnm_pos_1_punt terecht komt
-- dat is alleen te verhelpen door de GML bestanden opnieuw te transformeren
update OPENBARERUIMTELABEL
   set GEOM2D = OPRNM_POS_2_PUNT
   where GEOM2D is null;
