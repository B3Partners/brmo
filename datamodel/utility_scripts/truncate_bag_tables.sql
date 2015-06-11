--Pas op, zorg dat constraints tussen bag en andere
--BR gedropt zijn, anders gaat de gehele database
--leeg.
truncate addresseerb_obj_aand cascade;
truncate benoemd_obj  cascade;
truncate benoemd_terrein cascade;
truncate gebouwd_obj cascade;
truncate gebouwd_obj_gebruiksdoel cascade;
truncate gem_openb_rmte cascade;
truncate ligplaats cascade;
truncate ligplaats_nummeraand cascade;
truncate nummeraand cascade;
truncate openb_rmte cascade;
truncate openb_rmte_gem_openb_rmte cascade;
truncate openb_rmte_wnplts cascade;
truncate pand cascade;
truncate standplaats cascade;
truncate standplaats_nummeraand cascade;
truncate verblijfsobj cascade;
truncate verblijfsobj_nummeraand cascade;
truncate verblijfsobj_pand cascade;
truncate wnplts cascade;