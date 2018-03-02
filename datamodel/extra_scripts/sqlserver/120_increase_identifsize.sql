ALTER TABLE ander_btnlnds_niet_nat_prs DROP CONSTRAINT fk_ann_sc;
ALTER TABLE ander_nat_prs DROP CONSTRAINT fk_anp_sc;
ALTER TABLE ouder_kind_rel DROP CONSTRAINT fk_ouder_kind_rel_sc_rh;
ALTER TABLE ouder_kind_rel DROP CONSTRAINT fk_ouder_kind_rel_sc_lh;
ALTER TABLE huw_ger_partn DROP CONSTRAINT fk_huw_ger_partn_sc_rh;
ALTER TABLE huw_ger_partn DROP CONSTRAINT fk_huw_ger_partn_sc_lh;
ALTER TABLE kad_onrrnd_zk DROP CONSTRAINT fk_koz_as_10;
ALTER TABLE kad_onrrnd_zk_aantek DROP CONSTRAINT fk_kza_as_5;
ALTER TABLE maatschapp_activiteit DROP CONSTRAINT fk_mac_as_4;
ALTER TABLE app_re DROP CONSTRAINT fk_apr_as_2;
ALTER TABLE vestg DROP CONSTRAINT fk_ves_as_19;
ALTER TABLE vestg DROP CONSTRAINT fk_ves_as_18;
ALTER TABLE vestg DROP CONSTRAINT fk_ves_as_17;
ALTER TABLE vestg_benoemd_obj DROP CONSTRAINT fk_ves_tgo_nn_lh;
ALTER TABLE ingeschr_niet_nat_prs DROP CONSTRAINT fk_inn_sc;
ALTER TABLE huishoudenrel DROP CONSTRAINT fk_huishoudenrel_sc_lh;
ALTER TABLE niet_ingezetene DROP CONSTRAINT fk_nin_sc;
ALTER TABLE ingezetene DROP CONSTRAINT fk_ing_sc;
ALTER TABLE ingeschr_nat_prs DROP CONSTRAINT fk_inp_sc;
ALTER TABLE nat_prs DROP CONSTRAINT fk_nps_sc;
ALTER TABLE functionaris DROP CONSTRAINT fk_functionaris_sc_lh;
ALTER TABLE functionaris DROP CONSTRAINT fk_functionaris_sc_rh;
ALTER TABLE zak_recht_aantek DROP CONSTRAINT fk_zra_as_6;
ALTER TABLE zak_recht DROP CONSTRAINT fk_zkr_as_8;
ALTER TABLE woz_belang DROP CONSTRAINT fk_woz_belang_sc_lh;
ALTER TABLE prs DROP CONSTRAINT fk_pes_sc;
ALTER TABLE niet_nat_prs DROP CONSTRAINT fk_nnp_sc;
ALTER TABLE vestg_naam DROP CONSTRAINT fk_ves1;
ALTER TABLE rsdoc_ingeschr_nat_prs DROP CONSTRAINT fk_rsd_inp_nn_rh;

-- prs_eigendom heeft een niet-benoemde FK
WHILE(EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE table_name='prs_eigendom' AND constraint_type='FOREIGN KEY'))
BEGIN
    DECLARE @sqlp NVARCHAR(MAX)
    SELECT TOP 1 @sqlp=('ALTER TABLE prs_eigendom DROP CONSTRAINT ' + CONSTRAINT_NAME )
    FROM information_schema.table_constraints WHERE table_name='prs_eigendom' AND constraint_type = 'FOREIGN KEY'
    -- PRINT N'Opruimen prs_eigendom FKs ' + @sqlp
    EXEC sp_executesql @sqlp
END

-- vestg_activiteit heeft niet-benoemde FK's
WHILE(EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE table_name='vestg_activiteit' AND constraint_type='FOREIGN KEY'))
BEGIN
    DECLARE @sqla NVARCHAR(MAX)
    SELECT TOP 1 @sqla=('ALTER TABLE vestg_activiteit DROP CONSTRAINT ' + constraint_name)
    FROM information_schema.table_constraints WHERE table_name='vestg_activiteit' AND constraint_type = 'FOREIGN KEY'
    -- PRINT N'Opruimen vestg_activiteit FKs ' + @sqla
    EXEC sp_executesql @sqla
END

GO

ALTER TABLE ander_nat_prs DROP CONSTRAINT ander_nat_prs_pk;
ALTER TABLE ander_btnlnds_niet_nat_prs DROP CONSTRAINT ander_btnlnds_niet_nat_prs_pk;
ALTER TABLE nat_prs DROP CONSTRAINT nat_prs_pk;
ALTER TABLE ingeschr_nat_prs DROP CONSTRAINT ingeschr_nat_prs_pk;
ALTER TABLE ingeschr_niet_nat_prs DROP CONSTRAINT ingeschr_niet_nat_prs_pk;
ALTER TABLE ingezetene DROP CONSTRAINT ingezetene_pk;
ALTER TABLE niet_ingezetene DROP CONSTRAINT niet_ingezetene_pk;
ALTER TABLE niet_nat_prs DROP CONSTRAINT niet_nat_prs_pk;
ALTER TABLE prs DROP CONSTRAINT prs_pk;
ALTER TABLE subject DROP CONSTRAINT subject_pk;
ALTER TABLE vestg DROP CONSTRAINT vestg_pk;

-- vestg_naam heeft een niet-benoemde PK
DECLARE @PrimaryKeyName sysname = (SELECT constraint_name FROM information_schema.table_constraints WHERE constraint_type = 'PRIMARY KEY' AND table_schema='dbo' AND table_name = 'vestg_naam')
IF @PrimaryKeyName IS NOT NULL
BEGIN
    DECLARE @SQL_PK NVARCHAR(MAX) = 'ALTER TABLE dbo.vestg_naam DROP CONSTRAINT ' + @PrimaryKeyName
    -- PRINT N'Opruimen vestg_naam PK ' + @SQL_PK
    EXEC sp_executesql @SQL_PK;
END

ALTER TABLE huishoudenrel DROP CONSTRAINT huishoudenrel_pk;
ALTER TABLE huw_ger_partn DROP CONSTRAINT huw_ger_partn_pk;
ALTER TABLE ouder_kind_rel DROP CONSTRAINT ouder_kind_rel_pk;
ALTER TABLE rsdoc_ingeschr_nat_prs DROP CONSTRAINT rsdoc_ingeschr_nat_prs_pk;
ALTER TABLE vestg_benoemd_obj DROP CONSTRAINT vestg_benoemd_obj_pk;
ALTER TABLE functionaris DROP CONSTRAINT functionaris_pk;

GO

ALTER TABLE ander_btnlnds_niet_nat_prs ALTER COLUMN sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE ander_nat_prs ALTER COLUMN sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE ingeschr_niet_nat_prs ALTER COLUMN sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE ingeschr_nat_prs ALTER COLUMN sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE ingezetene ALTER COLUMN sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE kad_onrrnd_zk ALTER COLUMN fk_10pes_sc_identif VARCHAR(255);
ALTER TABLE kad_onrrnd_zk_aantek ALTER COLUMN fk_5pes_sc_identif VARCHAR(255);
ALTER TABLE maatschapp_activiteit ALTER COLUMN fk_4pes_sc_identif VARCHAR(255);
ALTER TABLE nat_prs ALTER COLUMN sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE niet_ingezetene ALTER COLUMN sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE niet_nat_prs ALTER COLUMN sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE app_re ALTER COLUMN fk_2nnp_sc_identif VARCHAR(255);
ALTER TABLE prs ALTER COLUMN sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE subject ALTER COLUMN identif VARCHAR(255) NOT NULL;
ALTER TABLE vestg ALTER COLUMN sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE vestg ALTER COLUMN fk_18ves_sc_identif VARCHAR(255);
ALTER TABLE zak_recht ALTER COLUMN fk_8pes_sc_identif VARCHAR(255);
ALTER TABLE zak_recht_aantek ALTER COLUMN fk_6pes_sc_identif VARCHAR(255);
ALTER TABLE vestg_naam ALTER COLUMN fk_ves_sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE huishoudenrel ALTER COLUMN fk_sc_lh_inp_sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE huw_ger_partn ALTER COLUMN fk_sc_lh_inp_sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE huw_ger_partn ALTER COLUMN fk_sc_rh_inp_sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE ouder_kind_rel ALTER COLUMN fk_sc_lh_inp_sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE ouder_kind_rel ALTER COLUMN fk_sc_rh_inp_sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE rsdoc_ingeschr_nat_prs ALTER COLUMN fk_nn_rh_inp_sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE vestg_benoemd_obj ALTER COLUMN fk_nn_lh_ves_sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE functionaris ALTER COLUMN fk_sc_lh_pes_sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE functionaris ALTER COLUMN fk_sc_rh_pes_sc_identif VARCHAR(255) NOT NULL;
ALTER TABLE woz_belang ALTER COLUMN fk_sc_lh_sub_identif VARCHAR(255) NOT NULL;
ALTER TABLE vestg_activiteit ALTER COLUMN fk_vestg_nummer VARCHAR(255) NOT NULL;
ALTER TABLE prs_eigendom ALTER COLUMN fk_prs_sc_identif VARCHAR(255) NOT NULL;

GO

ALTER TABLE niet_nat_prs ADD CONSTRAINT niet_nat_prs_pk PRIMARY KEY clustered(sc_identif);
ALTER TABLE ander_btnlnds_niet_nat_prs ADD CONSTRAINT ander_btnlnds_niet_nat_prs_pk PRIMARY KEY clustered(sc_identif);
ALTER TABLE ander_nat_prs ADD CONSTRAINT ander_nat_prs_pk PRIMARY KEY clustered(sc_identif);
ALTER TABLE ingeschr_niet_nat_prs ADD CONSTRAINT ingeschr_niet_nat_prs_pk PRIMARY KEY clustered(sc_identif);
ALTER TABLE ingeschr_nat_prs ADD CONSTRAINT ingeschr_nat_prs_pk PRIMARY KEY clustered(sc_identif);
ALTER TABLE ingezetene ADD CONSTRAINT ingezetene_pk PRIMARY KEY clustered(sc_identif);
ALTER TABLE nat_prs ADD CONSTRAINT nat_prs_pk PRIMARY KEY clustered(sc_identif);
ALTER TABLE niet_ingezetene ADD CONSTRAINT niet_ingezetene_pk PRIMARY KEY clustered(sc_identif);
ALTER TABLE prs ADD CONSTRAINT prs_pk PRIMARY KEY clustered(sc_identif);
ALTER TABLE subject ADD CONSTRAINT subject_pk PRIMARY KEY clustered(identif);
ALTER TABLE vestg ADD CONSTRAINT vestg_pk PRIMARY KEY clustered(sc_identif);
ALTER TABLE vestg_naam ADD CONSTRAINT pk_vestg_naam PRIMARY KEY (naam, fk_ves_sc_identif);
ALTER TABLE huishoudenrel ADD CONSTRAINT huishoudenrel_pk PRIMARY KEY clustered(fk_sc_lh_inp_sc_identif,fk_sc_rh_hhd_nummer);
ALTER TABLE huw_ger_partn ADD CONSTRAINT huw_ger_partn_pk PRIMARY KEY clustered(fk_sc_lh_inp_sc_identif,fk_sc_rh_inp_sc_identif);
ALTER TABLE ouder_kind_rel ADD CONSTRAINT ouder_kind_rel_pk PRIMARY KEY clustered(fk_sc_lh_inp_sc_identif,fk_sc_rh_inp_sc_identif);
ALTER TABLE rsdoc_ingeschr_nat_prs ADD CONSTRAINT rsdoc_ingeschr_nat_prs_pk PRIMARY KEY clustered(fk_nn_lh_rsd_nummer,fk_nn_rh_inp_sc_identif);
ALTER TABLE vestg_benoemd_obj ADD CONSTRAINT vestg_benoemd_obj_pk PRIMARY KEY clustered(fk_nn_lh_ves_sc_identif,fk_nn_rh_tgo_identif);
ALTER TABLE functionaris ADD CONSTRAINT functionaris_pk PRIMARY KEY clustered(fk_sc_lh_pes_sc_identif,fk_sc_rh_pes_sc_identif);

GO

ALTER TABLE ander_btnlnds_niet_nat_prs ADD CONSTRAINT fk_ann_sc FOREIGN KEY (sc_identif) REFERENCES niet_nat_prs (sc_identif) ON DELETE no action;
ALTER TABLE ander_nat_prs ADD CONSTRAINT fk_anp_sc FOREIGN KEY (sc_identif) REFERENCES nat_prs (sc_identif) ON DELETE no action;
ALTER TABLE ingeschr_niet_nat_prs ADD CONSTRAINT fk_inn_sc FOREIGN KEY (sc_identif) REFERENCES niet_nat_prs (sc_identif) ON DELETE no action;
ALTER TABLE ingeschr_nat_prs ADD CONSTRAINT fk_inp_sc FOREIGN KEY (sc_identif) REFERENCES nat_prs (sc_identif) ON DELETE no action;
ALTER TABLE niet_ingezetene ADD CONSTRAINT fk_nin_sc FOREIGN KEY (sc_identif) REFERENCES ingeschr_nat_prs (sc_identif) ON DELETE no action;
ALTER TABLE prs ADD CONSTRAINT fk_pes_sc FOREIGN KEY (sc_identif) REFERENCES subject (identif) ON DELETE no action;
ALTER TABLE ouder_kind_rel ADD CONSTRAINT fk_ouder_kind_rel_sc_lh FOREIGN KEY (fk_sc_lh_inp_sc_identif) REFERENCES ingeschr_nat_prs (sc_identif) ON DELETE no action;
ALTER TABLE ouder_kind_rel ADD CONSTRAINT fk_ouder_kind_rel_sc_rh FOREIGN KEY (fk_sc_rh_inp_sc_identif) REFERENCES ingeschr_nat_prs (sc_identif) ON DELETE no action;
ALTER TABLE huw_ger_partn ADD CONSTRAINT fk_huw_ger_partn_sc_lh FOREIGN KEY (fk_sc_lh_inp_sc_identif) REFERENCES ingeschr_nat_prs (sc_identif) ON DELETE no action;
ALTER TABLE huw_ger_partn ADD CONSTRAINT fk_huw_ger_partn_sc_rh FOREIGN KEY (fk_sc_rh_inp_sc_identif) REFERENCES ingeschr_nat_prs (sc_identif) ON DELETE no action;
ALTER TABLE kad_onrrnd_zk ADD CONSTRAINT fk_koz_as_10 FOREIGN KEY (fk_10pes_sc_identif) REFERENCES prs (sc_identif) ON DELETE no action;
ALTER TABLE kad_onrrnd_zk_aantek ADD CONSTRAINT fk_kza_as_5 FOREIGN KEY (fk_5pes_sc_identif) REFERENCES prs (sc_identif) ON DELETE no action;
ALTER TABLE maatschapp_activiteit ADD CONSTRAINT fk_mac_as_4 FOREIGN KEY (fk_4pes_sc_identif) REFERENCES prs (sc_identif) ON DELETE no action;
ALTER TABLE app_re ADD CONSTRAINT fk_apr_as_2 FOREIGN KEY (fk_2nnp_sc_identif) REFERENCES niet_nat_prs (sc_identif) ON DELETE no action;
ALTER TABLE functionaris ADD CONSTRAINT fk_functionaris_sc_lh FOREIGN KEY (fk_sc_lh_pes_sc_identif) REFERENCES prs (sc_identif) ON DELETE no action;
ALTER TABLE functionaris ADD CONSTRAINT fk_functionaris_sc_rh FOREIGN KEY (fk_sc_rh_pes_sc_identif) REFERENCES prs (sc_identif) ON DELETE no action;
ALTER TABLE zak_recht_aantek ADD CONSTRAINT fk_zra_as_6 FOREIGN KEY (fk_6pes_sc_identif) REFERENCES prs (sc_identif) ON DELETE no action;
ALTER TABLE zak_recht ADD CONSTRAINT fk_zkr_as_8 FOREIGN KEY (fk_8pes_sc_identif) REFERENCES prs (sc_identif) ON DELETE no action;
ALTER TABLE woz_belang ADD CONSTRAINT fk_woz_belang_sc_lh FOREIGN KEY (fk_sc_lh_sub_identif) REFERENCES subject (identif) ON DELETE no action;
ALTER TABLE vestg ADD CONSTRAINT fk_ves_as_17 FOREIGN KEY (fk_17mac_kvk_nummer) REFERENCES maatschapp_activiteit (kvk_nummer) ON DELETE no action;
ALTER TABLE vestg ADD CONSTRAINT fk_ves_as_18 FOREIGN KEY (fk_18ves_sc_identif) REFERENCES vestg (sc_identif) ON DELETE no action;
ALTER TABLE vestg ADD CONSTRAINT fk_ves_as_19 FOREIGN KEY (fk_19mac_kvk_nummer) REFERENCES maatschapp_activiteit (kvk_nummer) ON DELETE no action;
ALTER TABLE vestg_naam ADD CONSTRAINT fk_ves1 FOREIGN KEY (fk_ves_sc_identif) REFERENCES vestg (sc_identif) ON DELETE no action;
ALTER TABLE vestg_activiteit ADD CONSTRAINT fkfk_vestg_nummer FOREIGN KEY (fk_vestg_nummer) REFERENCES vestg(sc_identif);
ALTER TABLE vestg_activiteit ADD CONSTRAINT fkfk_sbi_activiteit_code FOREIGN KEY (fk_sbi_activiteit_code) REFERENCES sbi_activiteit(sbi_code);
ALTER TABLE huishoudenrel ADD CONSTRAINT fk_huishoudenrel_sc_lh FOREIGN KEY (fk_sc_lh_inp_sc_identif) REFERENCES ingeschr_nat_prs (sc_identif) ON DELETE no action;
ALTER TABLE ingezetene ADD CONSTRAINT fk_ing_sc FOREIGN KEY (sc_identif) REFERENCES ingeschr_nat_prs (sc_identif) ON DELETE no action;
ALTER TABLE rsdoc_ingeschr_nat_prs ADD CONSTRAINT fk_rsd_inp_nn_rh FOREIGN KEY (fk_nn_rh_inp_sc_identif) REFERENCES ingeschr_nat_prs (sc_identif) ON DELETE no action;
ALTER TABLE nat_prs ADD CONSTRAINT fk_nps_sc FOREIGN KEY (sc_identif) REFERENCES prs (sc_identif) ON DELETE no action;
ALTER TABLE niet_nat_prs ADD CONSTRAINT fk_nnp_sc FOREIGN KEY (sc_identif) REFERENCES prs (sc_identif) ON DELETE no action;
ALTER TABLE prs_eigendom ADD CONSTRAINT fkfk_prs_sc_identif FOREIGN KEY (fk_prs_sc_identif) REFERENCES prs(sc_identif);
ALTER TABLE vestg_benoemd_obj ADD CONSTRAINT fk_ves_tgo_nn_lh FOREIGN KEY (fk_nn_lh_ves_sc_identif) REFERENCES vestg (sc_identif) ON DELETE no action;

GO
