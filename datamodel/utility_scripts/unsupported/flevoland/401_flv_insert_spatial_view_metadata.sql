--
-- voeg ruimtelijke views toe aan de USER_SDO_GEOM_METADATA view.
--
-- NB de gebruikte extent is heel Nederland, dit is sub-optimaal als de extent
-- van de data kleiner is.
-- NB de lijst views is mogelijk niet compleet, afhankelijk van installatie
--

-- V_????
INSERT INTO USER_SDO_GEOM_METADATA
VALUES('V_???', 'THE_GEOM_???', 
	MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);

