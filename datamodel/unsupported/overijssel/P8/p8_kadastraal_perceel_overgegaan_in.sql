CREATE OR REPLACE VIEW v_p8_kad_perceel_over_in
                                   AS
  WITH cte_complex                 AS
        ( SELECT perceel_identif FROM v_bd_app_re_all_kad_perceel )                                   
  SELECT 
    ROWNUM                         AS oid, -- genereer een unieke id
    h.fk_sc_lh_koz_kad_identif     AS kadperceelcode_over_in_crit,
    h.fk_sc_rh_koz_kad_identif     AS kadperceelcode_ontstaan_crit,
    h.aard                         AS aard_overgang,
    h.overgangsgrootte             AS overgangsgrootte,
    -- onroerende zaak
    F_DATUM (kaz.dat_beg_geldh)    AS datum_ingang,
    F_DATUM (kaz.datum_einde_geldh)AS datum_einde,
    -- perceel info van actuele perceel
    ka.ka_kad_gemeentecode         AS gemeente_code,
    ka.ka_sectie                   AS sectie,
    ka.ka_perceelnummer            AS perceelnummer,
    CAST (
      CASE
        WHEN cc.perceel_identif IS NULL
          AND ka.omschr_deelperceel IS NULL
        THEN 'G'
        WHEN cc.perceel_identif IS NOT NULL
          AND ka.omschr_deelperceel IS NULL
        THEN 'C'
        WHEN ka.omschr_deelperceel IS NOT NULL -- Deel bepalen aan hand van aanwezigheid omschrijving
        THEN 'D'
        ELSE 'O'
      END AS CHAR(1))              AS objectindexletter,
    -1                             AS objectindexnummer,
    ka.grootte_perceel             AS oppervlakte
  FROM
    kad_onrrnd_zk_his_rel h
  LEFT JOIN kad_onrrnd_zk_archief kaz
    ON h.fk_sc_lh_koz_kad_identif = kaz.kad_identif
  LEFT JOIN kad_perceel ka
    ON h.fk_sc_lh_koz_kad_identif = ka.sc_kad_identif
  -- Complex
  LEFT JOIN cte_complex cc
    ON cc.perceel_identif = h.fk_sc_lh_koz_kad_identif
  ORDER BY h.fk_sc_lh_koz_kad_identif;
  
  COMMENT ON COLUMN v_p8_kad_perceel_over_in.kadperceelcode_over_in_crit
IS
  'zoek veld voor bepalen waarin is overgegaan - wordt';
  COMMENT ON COLUMN v_p8_kad_perceel_over_in.kadperceelcode_ontstaan_crit
IS
  'zoek veld voor bepalen waaruit is ontstaan - was';
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

-- indexen
CREATE UNIQUE INDEX m_p8_kad_perc_overin_oid_idx ON vm_p8_kad_perceel_over_in ( oid ASC );
CREATE INDEX vm_p8_kad_perc_ovr_in_idx ON vm_p8_kad_perceel_over_in( kadperceelcode_over_in_crit ASC );
CREATE INDEX vm_p8_kad_perc_ovr_onts_idx ON vm_p8_kad_perceel_over_in( kadperceelcode_ontstaan_crit ASC );
