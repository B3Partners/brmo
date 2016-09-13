CREATE OR REPLACE VIEW v_p8_subject
                      AS
  SELECT p.sc_identif AS subjectid,
    --p.sc_identif                    AS pk_id,
    p.clazz                         AS type,
    CAST (NULL AS VARCHAR(200))     AS postcode,
    CAST (NULL AS VARCHAR(200))     AS adres,
    CAST (NULL AS VARCHAR(200))     AS woonplaats,
    np.nm_voornamen                 AS natuurlijk_subject_voornaam,
    np.nm_voorvoegsel_geslachtsnaam AS natuurlijk_subj_tussenvoegsel,
    np.nm_geslachtsnaam             AS natuurlijk_subject_achternaam,
    np.geslachtsaand                AS natuurlijk_subject_geslacht,
    (
    CASE
      WHEN np.clazz = 'INGESCHREVEN NATUURLIJK PERSOON'
      THEN INP.GB_GEBOORTEDATUM
      ELSE ANP.GEBOORTEDATUM
        /*then to_date(to_char(inp.gb_geboortedatum),'yyyymmdd')
        else to_date(to_char(anp.geboortedatum),'yyyymmdd') */
    END )                    AS natuurlijk_subj_geboorte_datum,
    inp.gb_geboorteplaats    AS natuurlijk_subj_geboorteplaats,
    CAST('' AS VARCHAR(200)) AS natuurlijk_subj_geboorte_land,
    (
    CASE
      WHEN np.clazz = 'INGESCHREVEN NATUURLIJK PERSOON'
      THEN inp.ol_overlijdensdatum
      ELSE anp.overlijdensdatum
        /*then to_date((inp.ol_overlijdensdatum),'yyyymmdd')
        else to_date((anp.overlijdensdatum), 'yyyymmdd')*/
    END ) AS natuurlijk_subj_overl_datum,
    (
    CASE
      WHEN inp.ol_overlijdensdatum IS NOT NULL
      THEN 1
      ELSE 0
    END)                 AS natuurlijk_subj_is_overleden,
    nnp.kvk_nummer       AS niet_natuurlijk_subject_kvk,
    nnp.nnp_naam         AS niet_natuurl_subj_bedrijfsnaam,
    nnp.statutaire_zetel AS niet_natuurlijk_subject_plaats,
    nnp.rechtsvorm       AS niet_natuurl_subj_rechtsvorm
  FROM pv_persoon p
  LEFT JOIN pv_natuurlijk_persoon np
  ON p.sc_identif = np.sc_identif
  LEFT JOIN pv_ingeschr_natuurlijk_persoon inp
  ON inp.sc_identif= np.sc_identif
  LEFT JOIN pv_ander_natuurlijk_persoon anp
  ON anp.sc_identif= np.sc_identif
  LEFT JOIN pv_niet_natuurlijk_persoon nnp
  ON p.sc_identif = nnp.sc_identif;
  --
  -- materialized versie, verversing om 07:30
  --
  -- DROP MATERIALIZED VIEW vm_p8_subject;
CREATE MATERIALIZED VIEW vm_p8_subject REFRESH ON DEMAND START WITH TRUNC ( SYSDATE ) + ( 7.5/24 ) NEXT TRUNC ( SYSDATE ) +1+ ( 7.5/24 )
AS
  SELECT * FROM v_p8_subject;
  -- indexen
CREATE UNIQUE INDEX vm_p8_subject_id_idx ON vm_p8_subject ( subjectid ASC );