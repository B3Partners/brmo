CREATE OR REPLACE VIEW vw_p8_kadastraal_perceel_recht AS 
 SELECT 
	map.sc_kad_identif 		as kadperceelcode
,	zak.pso_identif			as subjectid
,	case when length(coalesce(naam_niet_natuurlijk_persoon,''))>0
	then naam_niet_natuurlijk_persoon
	else 
	trim(coalesce(geslachtsnaam,'') ||' '||coalesce(voorvoegsel,''))||', '||coalesce( voornamen,'') 
	end as subject_naam
,	soort_eigenaar			as subject_type
,	trim(substr(woonadres, length(woonadres)-instr(reverse(woonadres),' ')+1,length(woonadres)))	as subject_woonplaats
,	coalesce(to_char(aandeel_teller),'') || '/' || coalesce(to_char(aandeel_noemer),'') as aandeel
,	rechtsvorm				as recht_soort
,	cast( ingangsdatum_recht as date) 	as datum_ingang
,	cast(eindd_recht as date) 		as datum_eind
FROM pv_map_i_kpe  map 
 inner join PV_INFO_I_KOZ_ZAK_RECHT zak
 on map.sc_kad_identif = zak.koz_identif;
-- limit 100;

  DROP TABLE pm_p8_kadastraal_perceel_recht;
  CREATE TABLE pm_p8_kadastraal_perceel_recht AS
  SELECT * FROM vw_p8_kadastraal_perceel_recht where 1=2;

-- Technische PK voor tooling
ALTER TABLE pm_p8_kadastraal_perceel_recht ADD pm_p8_kad_perceel_recht_id integer generated always as identity;
insert into pm_p8_kadastraal_perceel_recht (
   KADPERCEELCODE,
   SUBJECTID,
   SUBJECT_NAAM,
   SUBJECT_TYPE,
   SUBJECT_WOONPLAATS,
   AANDEEL,
   RECHT_SOORT,
   DATUM_INGANG,
   DATUM_EIND
)
SELECT * FROM vw_p8_kadastraal_perceel_recht;

CREATE UNIQUE INDEX pk_pm_p8_kad_perceel_recht ON pm_p8_kadastraal_perceel_recht  (pm_p8_kad_perceel_recht_id ASC);
ALTER TABLE pm_p8_kadastraal_perceel_recht ADD CONSTRAINT pk_pm_p8_kad_perceel_recht PRIMARY KEY (pm_p8_kad_perceel_recht_id);
  
  -- Extra indexen
  CREATE INDEX ix_kad_perceel_recht_subjectid ON pm_p8_kadastraal_perceel_recht
    (
      subjectid ASC
    );
  
  CREATE INDEX ix_kad_perceel_rechten_kpcode ON pm_p8_kadastraal_perceel_recht
    (
      kadperceelcode ASC
    );
  
-- Rechten
   GRANT
  SELECT ON pm_p8_kadastraal_perceel_recht TO brmo_rsgbuser;
