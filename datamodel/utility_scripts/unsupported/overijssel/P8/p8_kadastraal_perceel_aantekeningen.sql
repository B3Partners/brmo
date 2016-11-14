CREATE OR REPLACE VIEW v_p8_kadastraal_perceel_aant
                                           AS
  SELECT 
    ROWNUM                                 AS oid,
    map.kadperceelcode                     AS kadperceelcode ,
    zak.pso_identif                        AS aantekening_op, --> 'recht' 'perceel' 'subject'
    aard_aantek_recht                      AS aantekening_aard,
    -- kadaster_identif_aantek_recht
    CASE
      WHEN map.objectindexletter='A'
      THEN 'Appartementsrecht'
      ELSE 'Perceel'
    END                                    AS aantekening_recht_type , --> 'Perceel','Appartementsrecht'
    F_DATUM (AANT.BEGINDATUM_AANTEK_RECHT) AS datum_ingang,
    F_DATUM (aant.eindd_aantek_recht)      AS datum_eind,
    CASE
      WHEN LENGTH(COALESCE(naam_niet_natuurlijk_persoon,''))>0
      THEN naam_niet_natuurlijk_persoon
      ELSE trim(COALESCE(geslachtsnaam,'')
        ||' '
        ||COALESCE(voorvoegsel,''))
        ||', '
        ||COALESCE( voornamen,'')
    END                                                                                          AS subject_naam ,
    soort_eigenaar                                                                               AS subject_type ,
    zak.pso_identif                                                                              AS subjectid ,
    trim(SUBSTR(woonadres, LENGTH(woonadres)-instr(reverse(woonadres),' ')+1,LENGTH(woonadres))) AS subject_woonplaats
  FROM v_p8_kadastraal_perceel map -- pv_map_i_kpe
  INNER JOIN pv_info_i_koz_zak_recht zak
  ON map.kadperceelcode = zak.koz_identif
  INNER JOIN pv_info_i_koz_zak_recht_aant aant
  ON aant.koz_identif = zak.koz_identif;
  
--
-- materialized versie, verversing om 07:30
--
DROP MATERIALIZED VIEW vm_p8_kadastraal_perceel_aant;
CREATE MATERIALIZED VIEW vm_p8_kadastraal_perceel_aant REFRESH ON DEMAND START WITH TRUNC ( SYSDATE ) + ( 7.5/24 ) NEXT TRUNC ( SYSDATE ) +1+ ( 7.5/24 )
AS
  SELECT * FROM v_p8_kadastraal_perceel_aant;

-- indexen
CREATE UNIQUE INDEX vm_p8_kad_perc_aant_oid_idx ON vm_p8_kadastraal_perceel_aant ( oid ASC );
CREATE INDEX kad_perceel_aant_kpcode_idx ON vm_p8_kadastraal_perceel_aant ( kadperceelcode ASC) ;
CREATE INDEX kad_perceel_aant_subj_idx ON vm_p8_kadastraal_perceel_aant ( subjectid ASC) ;