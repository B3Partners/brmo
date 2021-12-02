-- opruimen van de gegevens van de WOZ uit de RSGB

-- brondocument tabel kan niet in zijn geheel gewist worden
-- omdat meerdere basisregistraties daar samenkomen
delete from brondocument where tabel in ('woz_waarde');

-- koppeltabellen
delete from locaand_openb_rmte where fk_sc_rh_woz_nummer in (select nummer from woz_obj);
delete from locaand_adres where fk_sc_rh_woz_nummer in (select nummer from woz_obj);
delete from herkomst_metadata where herkomst_br = 'woz';

-- vestiging en subject gegevens
delete from vestg_naam where fk_ves_sc_identif like 'WOZ.%';
delete from vestg_activiteit where fk_vestg_nummer like 'WOZ.%';
delete from vestg where sc_identif like 'WOZ.%';

delete from ingeschr_niet_nat_prs where sc_identif like 'WOZ.%';
delete from niet_nat_prs where sc_identif like 'WOZ.%';
delete from ander_nat_prs where sc_identif like 'WOZ.%';
delete from niet_ingezetene where sc_identif  like 'WOZ.%';
delete from ingeschr_nat_prs where sc_identif like 'WOZ.%';
delete from nat_prs where sc_identif like 'WOZ.%';
delete from prs where sc_identif like 'WOZ.%';
delete from subject where identif like 'WOZ.%';


-- WOZ specifieke tabellen wissen
truncate woz_waarde_archief cascade;
truncate woz_obj_archief cascade;
truncate woz_deelobj_archief cascade;
truncate woz_belang cascade;
truncate woz_waarde cascade;
truncate woz_omvat cascade;
truncate woz_deelobj cascade;
truncate woz_obj cascade;

-- NB voor opruimen in staging:
-- delete from laadproces where soort = 'woz';
-- delete from bericht where soort = 'woz';
