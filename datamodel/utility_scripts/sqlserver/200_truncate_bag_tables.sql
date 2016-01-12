--Pas op, zorg dat constraints tussen bag en andere
--BR gedropt zijn, anders gaat de gehele database
--leeg.
truncate addresseerb_obj_aand;
truncate benoemd_obj ;
truncate benoemd_terrein;
truncate gebouwd_obj;
truncate gebouwd_obj_gebruiksdoel;
truncate gem_openb_rmte;
truncate ligplaats;
truncate ligplaats_nummeraand;
truncate nummeraand;
truncate openb_rmte;
truncate openb_rmte_gem_openb_rmte;
truncate openb_rmte_wnplts;
truncate pand;
truncate standplaats;
truncate standplaats_nummeraand;
truncate verblijfsobj;
truncate verblijfsobj_nummeraand;
truncate verblijfsobj_pand;
truncate wnplts;