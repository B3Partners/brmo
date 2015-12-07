--
-- maak alle ruimtelijke indexen.
-- tbv. update/migratie BRMO 1.2.10 naar 1.2.11
--
-- (her-)genereer dit script eventueel met:
-- grep 'MDSYS.SPATIAL_INDEX' ../../generated_scripts/datamodel_oracle.sql > create_spatial_indexes.sql
--
CREATE INDEX begr_terr_dl_geom1_idx ON begr_terr_dl (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX begr_terr_dl_kruinlijngeo2_idx ON begr_terr_dl (kruinlijngeom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=LINE');
CREATE INDEX benoemd_terrein_geom1_idx ON benoemd_terrein (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX buurt_geom1_idx ON buurt (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX functioneel_gebied_geom1_idx ON functioneel_gebied (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX gebouwd_obj_vlakgeom1_idx ON gebouwd_obj (vlakgeom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX gebouwd_obj_puntgeom2_idx ON gebouwd_obj (puntgeom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=POINT');
CREATE INDEX gebouwinstallatie_geom1_idx ON gebouwinstallatie (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX gemeente_geom1_idx ON gemeente (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX inrichtingselement_geom1_idx ON inrichtingselement (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=COLLECTION');
CREATE INDEX kad_perceel_begrenzing_pe1_idx ON kad_perceel (begrenzing_perceel) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX kad_perceel_plaatscoordin2_idx ON kad_perceel (plaatscoordinaten_perceel) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=POINT');
CREATE INDEX kunstwerkdeel_geom1_idx ON kunstwerkdeel (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=COLLECTION');
CREATE INDEX onbegr_terr_dl_geom1_idx ON onbegr_terr_dl (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX onbegr_terr_dl_kruinlijng2_idx ON onbegr_terr_dl (kruinlijngeom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=LINE');
CREATE INDEX ondersteunend_wegdeel_geo1_idx ON ondersteunend_wegdeel (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX overig_bouwwerk_geom1_idx ON overig_bouwwerk (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX ovrg_scheiding_geom1_idx ON ovrg_scheiding (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=COLLECTION');
CREATE INDEX pand_geom_bovenaanzicht1_idx ON pand (geom_bovenaanzicht) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX pand_geom_maaiveld2_idx ON pand (geom_maaiveld) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX scheiding_geom1_idx ON scheiding (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=COLLECTION');
CREATE INDEX spoor_geom1_idx ON spoor (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=LINE');
CREATE INDEX stadsdeel_geom1_idx ON stadsdeel (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX vrijstaand_vegetatie_obj_1_idx ON vrijstaand_vegetatie_obj (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX waterdeel_geom1_idx ON waterdeel (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX waterschap_geom1_idx ON waterschap (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX wegdeel_geom1_idx ON wegdeel (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX wijk_geom1_idx ON wijk (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX wnplts_geom1_idx ON wnplts (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX woz_obj_geom1_idx ON woz_obj (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX begr_terr_dl_archief_geom1_idx ON begr_terr_dl_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX begr_terr_dl_archief_krui2_idx ON begr_terr_dl_archief (kruinlijngeom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=LINE');
CREATE INDEX benoemd_terrein_archief_g1_idx ON benoemd_terrein_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX buurt_archief_geom1_idx ON buurt_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX functioneel_gebied_archie1_idx ON functioneel_gebied_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX gebouwd_obj_archief_vlakg1_idx ON gebouwd_obj_archief (vlakgeom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX gebouwd_obj_archief_puntg2_idx ON gebouwd_obj_archief (puntgeom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=POINT');
CREATE INDEX gebouwinstallatie_archief1_idx ON gebouwinstallatie_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX gemeente_archief_geom1_idx ON gemeente_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX inrichtingselement_archie1_idx ON inrichtingselement_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=COLLECTION');
CREATE INDEX kad_perceel_archief_begre1_idx ON kad_perceel_archief (begrenzing_perceel) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX kad_perceel_archief_plaat2_idx ON kad_perceel_archief (plaatscoordinaten_perceel) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=POINT');
CREATE INDEX kunstwerkdeel_archief_geo1_idx ON kunstwerkdeel_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=COLLECTION');
CREATE INDEX onbegr_terr_dl_archief_ge1_idx ON onbegr_terr_dl_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX onbegr_terr_dl_archief_kr2_idx ON onbegr_terr_dl_archief (kruinlijngeom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=LINE');
CREATE INDEX ondersteunend_wegdeel_arc1_idx ON ondersteunend_wegdeel_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX overig_bouwwerk_archief_g1_idx ON overig_bouwwerk_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX ovrg_scheiding_archief_ge1_idx ON ovrg_scheiding_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=COLLECTION');
CREATE INDEX pand_archief_geom_bovenaa1_idx ON pand_archief (geom_bovenaanzicht) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX pand_archief_geom_maaivel2_idx ON pand_archief (geom_maaiveld) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX scheiding_archief_geom1_idx ON scheiding_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=COLLECTION');
CREATE INDEX spoor_archief_geom1_idx ON spoor_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=LINE');
CREATE INDEX stadsdeel_archief_geom1_idx ON stadsdeel_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX vrijstaand_vegetatie_o_ar1_idx ON vrijstaand_vegetatie_o_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX waterdeel_archief_geom1_idx ON waterdeel_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX waterschap_archief_geom1_idx ON waterschap_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX wegdeel_archief_geom1_idx ON wegdeel_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX wijk_archief_geom1_idx ON wijk_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX wnplts_archief_geom1_idx ON wnplts_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX woz_obj_archief_geom1_idx ON woz_obj_archief (geom) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
