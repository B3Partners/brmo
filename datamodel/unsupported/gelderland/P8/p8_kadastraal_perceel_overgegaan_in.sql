/****************************************************************
 ** Auteur	: S. Knoeff
 ** Versie 	: 2.0
 ** Datum	: 19-05-2016
 **
 ** Wijzigingen :
 ** Datum	Auteur		Soort
 ** xxxxxxxx	S.Knoeff	Initieel
 ** 18-01-2016	S.Knoeff	TMP tabel
 ** 12-04-2016	S.Knoeff	pm_p8_kadastraal_perceel_overgegaan_in werd opgebouwd uit subject, dat is niet correct
 ** 19-05-2016	S.Knoeff	Nieuw opgezette view
 *****************************************************************/
/*
CREATE OR REPLACE VIEW vw_p8_kadastraal_perceel_overgegaan_in AS 
with cte_complex
  as
  (
  select perceel_identif from v_bd_app_re_all_kad_perceel
  )                                       
SELECT 	kadperceelcode
,	kadperceelcode_overgegaan_in
, 	gemeente_code
, 	sectie
, 	perceelnummer
,	objectindexletter
,	objectindexnummer
, 	oppervlakte
, 	aard
, 	datum_einde
From
(SELECT 	distinct kpa.sc_kad_identif		as kadperceelcode
,	kp.sc_kad_identif				as kadperceelcode_overgegaan_in
, 	kp.ka_kad_gemeentecode 				as gemeente_code
, 	kp.ka_sectie 					as sectie
, 	kp.ka_perceelnummer				as perceelnummer
,  CAST (case when cc.perceel_identif is null and kp.omschr_deelperceel is null
	then 'G' 
	when cc.perceel_identif is not null and kp.omschr_deelperceel is null
	then 'C'
	when kp.omschr_deelperceel is not null -- Deel bepalen aan hand van aanwezigheid omschrijving
	then 'D'
	else 'O' 				
	end   AS CHAR(1))              			AS objectindexletter
,	-1						as objectindexnummer
, 	kp.grootte_perceel				as oppervlakte
, 	kp.cu_aard_bebouwing 	 			as aard
, 	kpa.datum_einde_geldh				as datum_einde
, 	RANK () over (partition by kpa.sc_kad_identif ORDER BY kpa.dat_beg_geldh desc ) as ranking_nummer
  FROM pv_kad_perceel_archief kpa
  inner join pv_kad_perceel kp
  on st_within( kp.plaatscoordinaten_perceel,kpa.begrenzing_perceel)
  and kpa.sc_kad_identif <>  kp.sc_kad_identif

   -- Complex
  left join  cte_complex cc
  on cc.perceel_identif = kp.kad_identif::text

) poi 
where ranking_nummer=1 
*/

  CREATE TABLE pm_p8_kadastraal_perceel_overgegaan_in_tmp AS
  SELECT * FROM vw_p8_kadastraal_perceel_overgegaan_in; 

-- Technische PK voor tooling
ALTER TABLE pm_p8_kadastraal_perceel_overgegaan_in_tmp ADD column pm_p8_kadastraal_perceel_overgegaan_in_id serial;
CREATE UNIQUE INDEX pk_pm_p8_kadastraal_perceel_overgegaan_in_tmp ON pm_p8_kadastraal_perceel_overgegaan_in_tmp USING btree (pm_p8_kadastraal_perceel_overgegaan_in_id ASC);
  
  -- Extra indexen
  CREATE INDEX p8_kadastraal_perceel_overgegaan_inkadperceelcode_tmp ON pm_p8_kadastraal_perceel_overgegaan_in_tmp USING btree
    (
      kadperceelcode ASC
    );
  
  CREATE INDEX p8_kadastraal_perceel_overgegaan_insectie_tmp ON pm_p8_kadastraal_perceel_overgegaan_in_tmp USING btree
    (
      sectie ASC
    );
  
  CREATE INDEX p8_kadastraal_perceel_overgegaan_inperceelnummer_tmp ON pm_p8_kadastraal_perceel_overgegaan_in_tmp USING btree
    (
      perceelnummer ASC
    );

-- Omzetten van TMP naar 'normaal'
  DROP TABLE pm_p8_kadastraal_perceel_overgegaan_in;
  ALTER TABLE pm_p8_kadastraal_perceel_overgegaan_in_tmp RENAME TO pm_p8_kadastraal_perceel_overgegaan_in;

ALTER INDEX pk_pm_p8_kadastraal_perceel_overgegaan_in_tmp RENAME TO pk_pm_p8_kadastraal_perceel_overgegaan_in;
ALTER INDEX p8_kadastraal_perceel_overgegaan_inkadperceelcode_tmp RENAME TO p8_kadastraal_perceel_overgegaan_inkadperceelcode;
ALTER INDEX p8_kadastraal_perceel_overgegaan_insectie_tmp RENAME TO p8_kadastraal_perceel_overgegaan_insectie;
ALTER INDEX p8_kadastraal_perceel_overgegaan_inperceelnummer_tmp RENAME TO p8_kadastraal_perceel_overgegaan_inperceelnummer;

ALTER TABLE pm_p8_kadastraal_perceel_overgegaan_in ADD CONSTRAINT pk_pm_p8_kadastraal_perceel_overgegaan_in PRIMARY KEY USING INDEX pk_pm_p8_kadastraal_perceel_overgegaan_in;


GRANT SELECT ON TABLE pm_p8_kadastraal_perceel_overgegaan_in TO rsgb_lezer;
