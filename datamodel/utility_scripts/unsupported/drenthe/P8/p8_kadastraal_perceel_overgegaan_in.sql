CREATE OR REPLACE VIEW vw_p8_kad_perceel_over_in AS 
SELECT 	sc_kad_identif 			as kadperceelcode
, 	ka_kad_gemeentecode 		as gemeente_code
, 	ka_sectie 			as sectie
, 	ka_perceelnummer		as perceelnummer
,	cast (null as char(1))		as objectindexletter
,	-1				as objectindexnummer
, 	ka.grootte_perceel		as oppervlakte
, 	ka.cu_aard_cultuur_onbebouwd 	as aard
,   f_datum(KA.DAT_BEG_GELDH) as datum_ingang
,   f_datum(KA.DATUM_EINDE_GELDH)	as datum_einde
FROM pv_kad_perceel_archief ka;

  DROP TABLE pm_p8_kad_perceel_over_in;
  CREATE TABLE pm_p8_kad_perceel_over_in AS
  SELECT * FROM vw_p8_kad_perceel_over_in where 0=1; 
  
ALTER TABLE pm_p8_kad_perceel_over_in ADD pm_p8_kad_perceel_over_in_id integer generated always as identity;
insert into pm_p8_kad_perceel_over_in
(
   KADPERCEELCODE,
   GEMEENTE_CODE,
   SECTIE,
   PERCEELNUMMER,
   OBJECTINDEXLETTER,
   OBJECTINDEXNUMMER,
   OPPERVLAKTE,
   AARD,
   DATUM_INGANG,
   DATUM_EINDE
)
SELECT * FROM vw_p8_kad_perceel_over_in;
commit;

CREATE UNIQUE INDEX pk_pm_p8_kad_perceel_over_in ON pm_p8_kad_perceel_over_in (pm_p8_kad_perceel_over_in_id ASC);
ALTER TABLE pm_p8_kad_perceel_over_in ADD CONSTRAINT pk_pm_p8_kad_perceel_over_in PRIMARY KEY (pm_p8_kad_perceel_over_in_id);
  
  -- Extra indexen
  CREATE INDEX p8_kad_perceel_over_in_kpcode ON pm_p8_kad_perceel_over_in 
    (
      kadperceelcode ASC
    );
  
  CREATE INDEX p8_kad_perceel_over_in_sectie ON pm_p8_kad_perceel_over_in
    (
      sectie ASC
    );
  
  CREATE INDEX p8_kad_perceel_over_in_percnr ON pm_p8_kad_perceel_over_in
    (
      perceelnummer ASC
    );
  
  
  GRANT SELECT ON  vw_p8_kad_perceel_over_in TO brmo_rsgbuser;
  GRANT SELECT ON  pm_p8_kad_perceel_over_in TO brmo_rsgbuser;