create or replace view vw_p8_subject as
select 
p.sc_identif 				as subjectid, 
p.sc_identif 				as pk_id, 
p.clazz 				as type,
cast (null as varchar(200)) 		as postcode,
cast (null as varchar(200))		as adres,	
cast (null as varchar(200)) 		as woonplaats,	
np.nm_voornamen 			as natuurlijk_subject_voornaam,
np.nm_voorvoegsel_geslachtsnaam 	as natuurlijk_subj_tussenvoegsel,
np.nm_geslachtsnaam 			as natuurlijk_subject_achternaam,
np.geslachtsaand 			as natuurlijk_subject_geslacht,
(case when np.clazz = 'INGESCHREVEN NATUURLIJK PERSOON'
then INP.GB_GEBOORTEDATUM else ANP.GEBOORTEDATUM
	/*then to_date(to_char(inp.gb_geboortedatum),'yyyymmdd')  			
	else to_date(to_char(anp.geboortedatum),'yyyymmdd') */
	end ) as natuurlijk_subj_geboorte_datum,
inp.gb_geboorteplaats 			as natuurlijk_subj_geboorteplaats,
cast('' as varchar(200))		as natuurlijk_subj_geboorte_land,
(case when np.clazz = 'INGESCHREVEN NATUURLIJK PERSOON'
then inp.ol_overlijdensdatum else anp.overlijdensdatum
	/*then to_date((inp.ol_overlijdensdatum),'yyyymmdd') 		
	else to_date((anp.overlijdensdatum), 'yyyymmdd')*/
	end ) as natuurlijk_subj_overl_datum,
 (case when inp.ol_overlijdensdatum is not null
	then 1
	else 0
	end) 	as natuurlijk_subj_is_overleden,
nnp.kvk_nummer				as niet_natuurlijk_subject_kvk,
nnp.nnp_naam				as niet_natuurl_subj_bedrijfsnaam,
nnp.statutaire_zetel 			as niet_natuurlijk_subject_plaats,
nnp.rechtsvorm				as niet_natuurl_subj_rechtsvorm
from pv_persoon p
left join pv_natuurlijk_persoon np
 on p.sc_identif = np.sc_identif
	left join pv_ingeschr_natuurlijk_persoon inp
		on inp.sc_identif= np.sc_identif
	left join pv_ander_natuurlijk_persoon anp
		on anp.sc_identif= np.sc_identif
left join pv_niet_natuurlijk_persoon nnp
 on p.sc_identif = nnp.sc_identif;

drop table pm_p8_subject;
create table pm_p8_subject as select * from vw_p8_subject;

select natuurlijk_subj_geboorte_datum,s.* from vw_p8_subject s where length(natuurlijk_subj_geboorte_datum) <8 order by 1;
-- PK
CREATE UNIQUE INDEX pk_pm_p8_subject_pk_id_ix
   ON pm_p8_subject (pk_id ASC);

CREATE UNIQUE INDEX alternate_pk_p8_subject_id_ix
   ON pm_p8_subject (subjectid ASC);

--ALTER TABLE pm_p8_subject  CLUSTER ON pk_pm_p8_subject_pk_id_ix;

alter table  pm_p8_subject add constraint pk_p8_subject_id primary key (pk_id);

GRANT SELECT ON pm_p8_subject TO brmo_rsgbuser;
