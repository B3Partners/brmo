--
-- upgrade Oracle TOP10NL datamodel van 1.5.1 naar 1.5.2
--
alter table top10nl.spoorbaandeel add (puntGeometrie MDSYS.SDO_GEOMETRY);
alter table top10nl.plantopografie add (geometrie MDSYS.SDO_GEOMETRY);
alter table top10nl.plantopografie add (naam VARCHAR2(255));

insert into user_sdo_geom_metadata values('spoorbaandeel', 'puntGeometrie', MDSYS.SDO_DIM_ARRAY(
	MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
	MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
	28992
);
CREATE INDEX spoorbaandeel_puntGeometrie_idx ON spoorbaandeel (puntGeometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX;



insert into user_sdo_geom_metadata values('plantopografie', 'geometrie', MDSYS.SDO_DIM_ARRAY(
	MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
	MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
	28992
);
CREATE INDEX plantopografie_geom_idx ON plantopografie (geometrie) INDEXTYPE IS MDSYS.SPATIAL_INDEX;
