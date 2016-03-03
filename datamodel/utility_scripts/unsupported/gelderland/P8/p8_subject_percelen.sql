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
CREATE OR REPLACE VIEW vw_p8_subject_percelen
			   AS
  SELECT z.pso_identif     AS subjectid,
    p.sc_kad_identif       AS kadperceelcode,
    p.ka_kad_gemeentecode  AS gemeente_code,
    p.ka_sectie            AS sectie,
    p.ka_perceelnummer     AS perceelnummer,
    CAST (NULL AS CHAR(1)) AS objectindexletter,
    p.grootte_perceel      AS oppervlakte,
    p.begrenzing_perceel   AS geom,
    CAST ( z.aandeel_teller
    ||'\'
    ||z.aandeel_noemer AS VARCHAR(10)) AS aandeel,
    z.rechtsvorm                       AS rechtsoort,
    CAST(p.dat_beg_geldh AS     TIMESTAMP) AS datum_ingang,
    CAST(p.datum_einde_geldh AS TIMESTAMP) AS datum_eind
  FROM pv_info_i_koz_zak_recht_SK z
  LEFT JOIN pv_map_i_kpe p
  ON p.sc_kad_identif = z.koz_identif;

 COMMENT ON VIEW vw_p8_subject_percelen
IS
  'Versie 0.1 september 2015 SK Provincie Gelderland';
*/

  CREATE TABLE pm_p8_subject_percelen_tmp AS
  SELECT * FROM vw_p8_subject_percelen; --where 1=2

-- Technische PK voor tooling
ALTER TABLE pm_p8_subject_percelen_tmp ADD column pm_p8_subject_percelen_tmp_id serial;
CREATE UNIQUE INDEX pk_pm_p8_subject_percelen_tmp ON pm_p8_subject_percelen_tmp USING btree (pm_p8_subject_percelen_tmp_id ASC);
  
  -- Extra indexen
  CREATE INDEX ix_subject_percelen_subjectid_tmp ON pm_p8_subject_percelen_tmp USING btree
    (
      subjectid ASC
    );
  
  CREATE INDEX ix_subject_percelen_kadperceelcode_tmp ON pm_p8_subject_percelen_tmp USING btree
    (
      kadperceelcode ASC
    );
  
  CREATE INDEX ix_subject_percelen_sectie_tmp ON pm_p8_subject_percelen_tmp USING btree
    (
      sectie ASC
    );
  
  CREATE INDEX ix_subject_percelen_perceelnummer_tmp ON pm_p8_subject_percelen_tmp USING btree
    (
      perceelnummer ASC
    );
  
  CREATE INDEX ix_subject_percelen_geom_tmp ON pm_p8_subject_percelen_tmp USING gist
    (
      geom
    );

-- Omzetten van TMP naar 'normaal'
drop table pm_p8_subject_percelen cascade;
ALTER TABLE pm_p8_subject_percelen_tmp  RENAME TO pm_p8_subject_percelen;

ALTER INDEX pk_pm_p8_subject_percelen_tmp RENAME TO pk_pm_p8_subject_percelen;
ALTER INDEX ix_subject_percelen_subjectid_tmp RENAME TO ix_subject_percelen_subjectid;
ALTER INDEX ix_subject_percelen_kadperceelcode_tmp RENAME TO ix_subject_percelen_kadperceelcode;
ALTER INDEX ix_subject_percelen_sectie_tmp RENAME TO ix_subject_percelen_sectie;
ALTER INDEX ix_subject_percelen_perceelnummer_tmp RENAME TO ix_subject_percelen_perceelnummer;
ALTER INDEX ix_subject_percelen_geom_tmp RENAME TO ix_subject_percelen_geom;

ALTER TABLE pm_p8_subject_percelen ADD CONSTRAINT pk_pm_p8_subject_percelen PRIMARY KEY USING INDEX pk_pm_p8_subject_percelen;


GRANT SELECT ON TABLE pm_p8_subject_percelen TO rsgb_lezer;
