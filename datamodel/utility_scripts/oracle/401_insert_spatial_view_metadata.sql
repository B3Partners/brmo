--
-- voeg ruimtelijke views toe aan de USER_SDO_GEOM_METADATA view.
--
-- NB de gebruikte extent is heel Nederland, dit is sub-optimaal als de extent
-- van de data kleiner is.
-- NB de lijst views is mogelijk niet compleet, afhankelijk van installatie
--

-- V_ADRES
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_ADRES', 'THE_GEOM', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_ADRES_LIGPLAATS
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_ADRES_LIGPLAATS', 'THE_GEOM', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_ADRES_LIGPLAATS', 'CENTROIDE', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_ADRES_STANDPLAATS
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_ADRES_STANDPLAATS', 'THE_GEOM', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_ADRES_STANDPLAATS', 'CENTROIDE', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_ADRES_TOTAAL
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_ADRES_TOTAAL', 'THE_GEOM', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_BD_APP_RE_BIJ_PERCEEL
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_BD_APP_RE_BIJ_PERCEEL', 'BEGRENZING_PERCEEL', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_BD_KAD_PERCEEL_MET_APP
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_BD_KAD_PERCEEL_MET_APP', 'BEGRENZING_PERCEEL', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_BD_KAD_PERCEEL_MET_APP', 'PLAATSCOORDINATEN_PERCEEL', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_BD_APP_RE_AND_KAD_PERCEEL
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_BD_APP_RE_AND_KAD_PERCEEL', 'BEGRENZING_PERCEEL', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_BD_KAD_PERCEEL_MET_APP_VLAK
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_BD_KAD_PERCEEL_MET_APP_VLAK', 'BEGRENZING_PERCEEL', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_KAD_PERCEEL_EENVOUDIG
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_KAD_PERCEEL_EENVOUDIG', 'BEGRENZING_PERCEEL', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_KAD_PERCEEL_IN_EIGENDOM
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_KAD_PERCEEL_IN_EIGENDOM', 'BEGRENZING_PERCEEL', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_KAD_PERCEEL_ZR_ADRESSEN
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_KAD_PERCEEL_ZR_ADRESSEN', 'BEGRENZING_PERCEEL', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_LIGPLAATS_ALLES
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_LIGPLAATS_ALLES', 'THE_GEOM', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_LIGPLAATS
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_LIGPLAATS', 'GEOMETRIE', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_MAP_KAD_PERCEEL
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_MAP_KAD_PERCEEL', 'BEGRENZING_PERCEEL', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_PAND_GEBRUIK_NIET_INGEMETEN
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_PAND_GEBRUIK_NIET_INGEMETEN', 'THE_GEOM', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_PAND_IN_GEBRUIK
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_PAND_IN_GEBRUIK', 'THE_GEOM', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_STANDPLAATS_ALLES
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_STANDPLAATS_ALLES', 'THE_GEOM', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_STANDPLAATS
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_STANDPLAATS', 'GEOMETRIE', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_VERBLIJFSOBJECT
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_VERBLIJFSOBJECT', 'THE_GEOM', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_VERBLIJFSOBJECT_ALLES
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_VERBLIJFSOBJECT_ALLES', 'THE_GEOM', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- V_VERBLIJFSOBJECT_GEVORMD
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_VERBLIJFSOBJECT_GEVORMD', 'THE_GEOM', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- VM_KAD_EIGENARENKAART
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('VM_KAD_EIGENARENKAART', 'BEGRENZING_PERCEEL', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

-- adres vlakken
INSERT INTO USER_SDO_GEOM_METADATA 
VALUES('V_ADRES_PANDVLAK', 'THE_GEOM', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

INSERT INTO USER_SDO_GEOM_METADATA 
VALUES('V_ADRES_TOTAAL_VLAK', 'THE_GEOM', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

