--- koppel nevenvestigingen met hoofdvestiging
create or replace view v_kvk_hoofd_nevenvestiging as 
select 			s.naam as hoofdvestigingnaam,
				coalesce(v.fk_15ond_kvk_nummer::text, v.fk_17mac_kvk_nummer::text) as kvknummer,
				array_to_string(array_agg(distinct s2.naam), ' | ')    as nevenvestigingen,
				count(v2.*)  								 		   as aantal_nevenvestigingen
from vestg v
join subject s on v.sc_identif = s.identif
join vestg v2 on coalesce(v.fk_15ond_kvk_nummer, v.fk_17mac_kvk_nummer) = coalesce(v2.fk_15ond_kvk_nummer, v2.fk_17mac_kvk_nummer)
join subject s2 on v2.sc_identif = s2.identif 
where v.hoofdvestiging = 'Ja' and v2.hoofdvestiging = 'Nee'
group by hoofdvestigingnaam, coalesce(v.fk_15ond_kvk_nummer::text, v.fk_17mac_kvk_nummer::text);