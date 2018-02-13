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
  FROM pv_info_i_koz_zak_recht z
  LEFT JOIN pv_map_i_kpe p
  ON p.sc_kad_identif = z.koz_identif;

 COMMENT ON table vw_p8_subject_percelen
IS
  'Versie 0.1 september 2015 SK Provincie Gelderland';

  DROP TABLE pm_p8_subject_percelen;
  CREATE TABLE pm_p8_subject_percelen AS
  SELECT * FROM vw_p8_subject_percelen where 1=2;

-- Technische PK voor tooling
ALTER TABLE pm_p8_subject_percelen ADD  pm_p8_subject_percelen_id integer generated always as identity;;
CREATE UNIQUE INDEX pk_pm_p8_subject_percelen ON pm_p8_subject_percelen (pm_p8_subject_percelen_id ASC);
ALTER TABLE pm_p8_subject_percelen ADD CONSTRAINT pk_pm_p8_subject_percelen PRIMARY KEY (pm_p8_subject_percelen_id);
  
  -- Extra indexen
  CREATE INDEX ix_subject_percelen_subjectid ON pm_p8_subject_percelen 
    (
      subjectid ASC
    );
  
  CREATE INDEX ix_subject_percelen_kpcode ON pm_p8_subject_percelen
    (
      kadperceelcode ASC
    );
  
  CREATE INDEX ix_subject_percelen_sectie ON pm_p8_subject_percelen
    (
      sectie ASC
    );
  
  CREATE INDEX ix_subject_percelen_perceelnr ON pm_p8_subject_percelen
    (
      perceelnummer ASC
    );
      
insert into user_sdo_geom_metadata values ( 'PM_P8_SUBJECT_PERCELEN', 'GEOM',
  sdo_dim_array(sdo_dim_element('X', 10000, 281000, 0.001), sdo_dim_element('Y', 304000, 623000, 0.001)), 28992);
  
  drop INDEX ix_subject_percelen_geom;  
  CREATE INDEX ix_subject_percelen_geom ON pm_p8_subject_percelen 
    (
      geom
    ) INDEXTYPE IS MDSYS.SPATIAL_INDEX
PARAMETERS('index_status=cleanup, layer_gtype=MULTIPOLYGON ');
  
  GRANT SELECT ON pm_p8_subject_percelen TO brmo_rsgbuser;
