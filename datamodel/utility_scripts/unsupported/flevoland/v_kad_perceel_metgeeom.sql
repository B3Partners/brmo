--View die alleen de inhoud van de tabel kad_perceel toont waarbij de begrenzing_perceel niet NULL is
--Naam view: v_kad_perceel_metgeom
--Datum: 17/02/2016
--Auteur: Meine Toonen

select * from kad_perceel p where p.begrenzing_perceel.sdo_srid != 0 
