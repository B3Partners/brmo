CREATE OR REPLACE VIEW v_p8_kad_perceel_over_in
                                    AS
  SELECT h.fk_sc_lh_koz_kad_identif AS kadperceelcode_over_in_crit,
    h.fk_sc_rh_koz_kad_identif      AS kadperceelcode_ontstaan_crit,
    h.aard                          AS aard_overgang,
    h.overgangsgrootte              AS overgangsgrootte,
    -- onroerende zaak
    kaz.cu_aard_cultuur_onbebouwd  AS aard,
    f_datum(kaz.dat_beg_geldh)     AS datum_ingang,
    f_datum(kaz.datum_einde_geldh) AS datum_einde,
    -- perceel info van actuele perceel
    ka.ka_kad_gemeentecode AS gemeente_code,
    ka.ka_sectie           AS sectie,
    ka.ka_perceelnummer    AS perceelnummer,
    -- CAST (NULL AS CHAR(1))      AS objectindexletter,
    -- -1                          AS objectindexnummer,
    ka.grootte_perceel AS oppervlakte
  FROM
    --pv_kad_onr_zk_his_rel h
    kad_onrrnd_zk_his_rel h
  LEFT JOIN kad_onrrnd_zk_archief kaz
  ON h.fk_sc_lh_koz_kad_identif = kaz.kad_identif
  LEFT JOIN kad_perceel ka
    -- kad_perceel_archief ka
  ON h.fk_sc_lh_koz_kad_identif = ka.sc_kad_identif
  ORDER BY h.fk_sc_lh_koz_kad_identif;
  COMMENT ON COLUMN v_p8_kad_perceel_over_in.kadperceelcode_over_in_crit
IS
  'zoek veld voor bepalen waarin is overgegaan';
  COMMENT ON COLUMN v_p8_kad_perceel_over_in.kadperceelcode_ontstaan_crit
IS
  'zoek veld voor bepalen waaruit is ontstaan';
  COMMENT ON TABLE v_p8_kad_perceel_over_in
IS
  'overgaan in en ontstaan uit bevragen met kad.identif op betreffende kolom [..over_in_crit | ..ontstaan_crit] levert records met relatie in tegeonoverliggende kolom';
  --
  -- materialized versie, verversing om 07:30
  --
  DROP MATERIALIZED VIEW vm_p8_kad_perceel_over_in;
CREATE MATERIALIZED VIEW vm_p8_kad_perceel_over_in REFRESH ON DEMAND START WITH TRUNC(SYSDATE) +(7.5/24) NEXT TRUNC(SYSDATE) +1+ (7.5/24)
AS
  SELECT * FROM v_p8_kad_perceel_over_in;
  COMMENT ON MATERIALIZED VIEW vm_p8_kad_perceel_over_in
IS
  'overgaan in en ontstaan uit bevragen met kad.identif op betreffende kolom [..over_in_crit | ..ontstaan_crit] levert records met relatie in tegeonoverliggende kolom';
  COMMENT ON COLUMN vm_p8_kad_perceel_over_in.kadperceelcode_over_in_crit
IS
  'zoek veld voor bepalen waarin is overgegaan';
  COMMENT ON COLUMN vm_p8_kad_perceel_over_in.kadperceelcode_ontstaan_crit
IS
  'zoek veld voor bepalen waaruit is ontstaan';
  CREATE INDEX vm_p8_kad_perc_ovr_in_idx ON vm_p8_kad_perceel_over_in
    (
      kadperceelcode_over_in_crit ASC
    );
  CREATE INDEX vm_p8_kad_perc_ovr_onts_idx ON vm_p8_kad_perceel_over_in
    (
      kadperceelcode_ontstaan_crit ASC
    );
