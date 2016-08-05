/****************************************************************
 ** Auteur	: S. Knoeff
 ** Versie 	: 1.2
 ** Datum	: 07-06-2016
 **
 ** Wijzigingen :
 ** Datum	Auteur		Soort
 ** xxxxxxxx	S.Knoeff	Initieel
 ** 18-01-2016	S.Knoeff	TMP tabel
 ** 07-06-2016  S.Knoeff	Adres construeren uit va_loc_beschrijving
 *****************************************************************/
/*
create or replace view vw_p8_subject as
select 
p.sc_identif 				as subjectid, 
p.sc_identif 				as pk_id, 
p.clazz 				as type,
cast (
	case when trim(substring(va_loc_beschrijving, strpos(va_loc_beschrijving,',')+2,6)) ~ '[0-9][0-9][0-9][0-9][A-Z][A-Z]'
	then trim(substring(va_loc_beschrijving, strpos(va_loc_beschrijving,',')+2,6))
	else ''
	end
	as varchar(200)) 		as postcode,
cast (	
	case when strpos(va_loc_beschrijving,',') <> 0
	then trim(substring(va_loc_beschrijving, 0, strpos(va_loc_beschrijving,',')))
	else ''
	end as varchar(200))		as adres,	
cast (
	case when trim(substring(va_loc_beschrijving, strpos(va_loc_beschrijving,',')+2,6)) ~ '[0-9][0-9][0-9][0-9][A-Z][A-Z]' AND strpos(va_loc_beschrijving,',') <> 0
	then trim(substring(va_loc_beschrijving,strpos(va_loc_beschrijving,substring(va_loc_beschrijving, strpos(va_loc_beschrijving,',')+2,6))+6))
	else ''
	end as varchar(200)) 		as woonplaats,	
np.nm_voornamen 			as natuurlijk_subject_voornaam,
np.nm_voorvoegsel_geslachtsnaam 	as natuurlijk_subject_tussenvoegsel,
np.nm_geslachtsnaam 			as natuurlijk_subject_achternaam,
np.geslachtsaand 			as natuurlijk_subject_geslacht,
to_timestamp(case when np.clazz = 'INGESCHREVEN NATUURLIJK PERSOON'
	then inp.gb_geboortedatum 			
	else anp.geboortedatum
	end ) as natuurlijk_subject_geboorte_datum,
inp.gb_geboorteplaats 			as natuurlijk_subject_geboorte_plaats,
cast('' as varchar(200))		as natuurlijk_subject_geboorte_land,
to_timestamp(case when np.clazz = 'INGESCHREVEN NATUURLIJK PERSOON'
	then inp.ol_overlijdensdatum 		
	else anp.overlijdensdatum
	end ) as natuurlijk_subject_overlijdens_datum,
cast (case when inp.ol_overlijdensdatum is not null
	then true
	else false
	end as boolean )		as natuurlijk_subject_is_overleden,
nnp.kvk_nummer				as niet_natuurlijk_subject_kvk,
nnp.nnp_naam				as niet_natuurlijk_subject_bedrijfsnaam,
nnp.statutaire_zetel 			as niet_natuurlijk_subject_plaats,
nnp.rechtsvorm				as niet_natuurlijk_subject_rechtsvorm
from pv_persoon p
left join pv_natuurlijk_persoon np
 on p.sc_identif = np.sc_identif
	left join pv_ingeschr_natuurlijk_persoon inp
		on inp.sc_identif= np.sc_identif
	left join pv_ander_natuurlijk_persoon anp
		on anp.sc_identif= np.sc_identif
left join pv_niet_natuurlijk_persoon nnp
 on p.sc_identif = nnp.sc_identif;
*/


create table pm_p8_subject_tmp as select * from vw_p8_subject;

-- PK
CREATE UNIQUE INDEX pk_pm_p8_subject_pk_id_ix_tmp
   ON pm_p8_subject_tmp USING btree (pk_id ASC);

CREATE UNIQUE INDEX alternate_pk_p8_subject_id_ix_tmp
   ON pm_p8_subject_tmp USING btree (subjectid ASC);

ALTER TABLE pm_p8_subject_tmp
  CLUSTER ON pk_pm_p8_subject_pk_id_ix_tmp;

-- Herbenoemen TMP tabel
drop table pm_p8_subject cascade;
ALTER TABLE pm_p8_subject_tmp RENAME TO pm_p8_subject;

-- Hernoemen indexen
ALTER INDEX pk_pm_p8_subject_pk_id_ix_tmp RENAME TO pk_pm_p8_subject_pk_id_ix;
ALTER INDEX alternate_pk_p8_subject_id_ix_tmp RENAME TO alternate_pk_p8_subject_id_ix;

alter table  pm_p8_subject add constraint pk_p8_subject_id primary key using index pk_pm_p8_subject_pk_id_ix;


GRANT SELECT ON TABLE pm_p8_subject TO rsgb_lezer;
