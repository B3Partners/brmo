--
-- upgrade PostgreSQL TOPNL datamodel van 1.5.0 naar 1.5.1
--

alter table top10nl.Spoorbaandeel add column puntGeometrie geometry (Point, 28992);