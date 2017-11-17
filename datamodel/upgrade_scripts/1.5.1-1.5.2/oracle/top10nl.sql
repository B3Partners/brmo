--
-- upgrade Oracle TOP10NL datamodel van 1.5.0 naar 1.5.1
--
alter table top10nl.Spoorbaandeel add (puntGeometrie MDSYS.SDO_GEOMETRY);