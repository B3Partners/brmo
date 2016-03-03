/****************************************************************
 ** Auteur	: S. Knoeff
 ** Versie 	: 1.1
 ** Datum	: 18-01-2016
 **
 ** Wijzigingen :
 ** Datum	Auteur		Soort
 ** xxxxxxxx	S.Knoeff	Initieel
 ** 18-01-2016	S.Knoeff	TMP tabel
 *****************************************************************/
/*
CREATE OR REPLACE VIEW vw_p8_kadastraal_perceel_overgegaan_in AS 
SELECT 	sc_kad_identif 			as kadperceelcode
, 	ka_kad_gemeentecode 		as gemeente_code
, 	ka_sectie 			as sectie
, 	ka_perceelnummer		as perceelnummer
,	cast (null as char(1))		as objectindexletter
,	-1				as objectindexnummer
, 	ka.grootte_perceel		as oppervlakte
, 	ka.cu_aard_cultuur_onbebouwd 	as aard
, 	ka.datum_einde_geldh		as datum_einde
FROM pv_kad_perceel_archief ka;
*/

  
  CREATE TABLE pm_p8_kadastraal_perceel_overgegaan_in_tmp AS
  SELECT * FROM vw_p8_subject_percelen; 

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