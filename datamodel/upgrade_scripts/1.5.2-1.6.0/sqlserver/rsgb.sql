--
-- upgrade SQLserver RSGB datamodel van 1.5.2 naar 1.6.0
--

-- 118
-- worden al in 114 verwijderd
-- alter table subject drop constraint fk_sub_as_13;
-- alter table subject drop constraint fk_sub_as_14;
-- alter table subject drop constraint fk_sub_as_15;
-- alter table subject drop constraint fk_sub_pa_as_4;
alter table subject drop constraint fk_sub_vb_4;

-- worden al in 114 verwijderd
-- alter table ingeschr_nat_prs drop constraint fk_inp_as_27;
-- alter table ingeschr_nat_prs drop constraint fk_inp_as_28;
-- alter table ingeschr_nat_prs drop constraint fk_inp_as_29;
-- alter table ingeschr_nat_prs drop constraint fk_inp_as_30;
-- alter table ingeschr_nat_prs drop constraint fk_inp_as_31;
alter table ingeschr_nat_prs drop constraint fk_inp_gb_2;
alter table ingeschr_nat_prs drop constraint fk_inp_ol_1;
alter table ingeschr_nat_prs drop constraint fk_inp_rl_17;
alter table ingeschr_nat_prs drop constraint fk_inp_rl_18;
-- alter table ingeschr_nat_prs drop constraint fk_inp_va_as_3;
-- alter table ingeschr_nat_prs drop constraint fk_inp_va_as_4;
-- alter table ingeschr_nat_prs drop constraint fk_inp_va_as_5;
-- alter table ingeschr_nat_prs drop constraint fk_inp_va_as_6;
-- alter table ingeschr_nat_prs drop constraint fk_inp_va_as_7;

GO

-- 119
insert into rsdocsoort (rsdoccode, rsdocomschr) values
('1','paspoort'),
('2','Europese identiteitskaart'),
('3','toeristenkaart'),
('4','gemeentelijke identiteitskaart'),
('5','verblijfsdocument van de Vreemdelingendienst'),
('6','vluchtelingenpaspoort'),
('7','vreemdelingenpaspoort'),
('8','paspoort met aantekening vergunning tot verblijf'),
('9','(electronisch) W-document');

GO

-- 120
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
-- ook in andere migratie scripts ed..
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


GO

-- issue #411
-- appartementsrecht aan bag adres
CREATE VIEW v_app_re_adres AS
  SELECT DISTINCT
    kp.sc_kad_identif,
    kpvbo.fk_nn_lh_tgo_identif AS kad_bag_koppeling_benobj,
    gor.naam_openb_rmte AS straat,
    aoa.huinummer AS huisnummer,
    aoa.huisletter,
    aoa.huinummertoevoeging AS toevoeging,
    aoa.postcode,
    wp.naam AS woonplaats
  FROM app_re kp
    LEFT JOIN benoemd_obj_kad_onrrnd_zk kpvbo on (kpvbo.FK_NN_RH_KOZ_KAD_IDENTIF = kp.SC_KAD_IDENTIF)
    LEFT JOIN verblijfsobj vbo on (vbo.SC_IDENTIF = kpvbo.FK_NN_LH_TGO_IDENTIF)
    LEFT JOIN nummeraand na on (na.SC_IDENTIF = vbo.FK_11NRA_SC_IDENTIF)
    LEFT JOIN addresseerb_obj_aand aoa on (aoa.IDENTIF = na.SC_IDENTIF)
    LEFT JOIN gem_openb_rmte gor on (gor.IDENTIFCODE = aoa.FK_7OPR_IDENTIFCODE)
    LEFT JOIN openb_rmte_wnplts oprw on (oprw.FK_NN_LH_OPR_IDENTIFCODE = gor.IDENTIFCODE)
    LEFT JOIN wnplts wp on (wp.IDENTIF = oprw.FK_NN_RH_WPL_IDENTIF);

GO

-- kad_onrrnd_zk gekoppeld aan bag adres
CREATE VIEW v_kad_onrrd_zk_adres AS
  SELECT DISTINCT
    kp.kad_identif,
    kpvbo.fk_nn_lh_tgo_identif AS kad_bag_koppeling_benobj,
    gor.naam_openb_rmte AS straat,
    aoa.huinummer AS huisnummer,
    aoa.huisletter,
    aoa.huinummertoevoeging AS toevoeging,
    aoa.postcode,
    wp.naam AS woonplaats
  FROM kad_onrrnd_zk kp
    LEFT JOIN benoemd_obj_kad_onrrnd_zk kpvbo on (kpvbo.FK_NN_RH_KOZ_KAD_IDENTIF = kp.KAD_IDENTIF)
    LEFT JOIN verblijfsobj vbo on (vbo.SC_IDENTIF = kpvbo.FK_NN_LH_TGO_IDENTIF)
    LEFT JOIN nummeraand na on (na.SC_IDENTIF = vbo.FK_11NRA_SC_IDENTIF)
    LEFT JOIN addresseerb_obj_aand aoa on (aoa.IDENTIF = na.SC_IDENTIF)
    LEFT JOIN gem_openb_rmte gor on (gor.IDENTIFCODE = aoa.FK_7OPR_IDENTIFCODE)
    LEFT JOIN openb_rmte_wnplts oprw on (oprw.FK_NN_LH_OPR_IDENTIFCODE = gor.IDENTIFCODE)
    LEFT JOIN wnplts wp on (wp.IDENTIF = oprw.FK_NN_RH_WPL_IDENTIF);

GO

-- 121
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (0,'Onbekend',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (1,'Nederlandse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (2,'Behandeld als Nederlander','20070401','20070401');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (27,'Slowaakse','19930101',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (28,'Tsjechische','19930101',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (29,'Burger van Bosnië-Herzegovina','19920406',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (30,'Georgische','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (31,'Turkmeense','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (32,'Tadzjiekse','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (33,'Oezbeekse','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (34,'Oekraïense','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (35,'Kirgizische','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (36,'Moldavische','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (37,'Kazachse','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (38,'Belarussische','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (39,'Azerbeidzjaanse','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (40,'Armeense','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (41,'Russische','19911231',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (42,'Sloveense','19920115',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (43,'Kroatische','19920115',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (44,'Letse','19910828',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (45,'Estische','19910828',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (46,'Litouwse','19910828',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (47,'Marshalleilandse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (48,'Myanmarese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (49,'Namibische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (50,'Albanese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (51,'Andorrese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (52,'Belgische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (53,'Bulgaarse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (54,'Deense',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (55,'Burger van de Bondsrepubliek Duitsland',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (56,'Finse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (57,'Franse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (58,'Jemenitische','19900522',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (59,'Griekse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (60,'Brits burger',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (61,'Hongaarse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (62,'Ierse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (63,'IJslandse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (64,'Italiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (65,'Joegoslavische',null,'20040201');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (66,'Liechtensteinse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (67,'Luxemburgse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (68,'Maltese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (69,'Monegaskische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (70,'Noorse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (71,'Oostenrijkse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (72,'Poolse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (73,'Portugese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (74,'Roemeense',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (75,'Burger van de Sovjet-Unie',null,'19911231');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (76,'San Marinese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (77,'Spaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (78,'Tsjecho-Slowaakse',null,'19930101');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (79,'Vaticaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (80,'Zweedse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (81,'Zwitserse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (82,'Oost-Duitse',null,'19901003');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (83,'Brits onderdaan',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (84,'Eritrese','19930528',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (85,'Brits overzees burger',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (86,'Macedonische','19930419',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (87,'Kosovaarse','20080615',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (100,'Algerijnse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (101,'Angolese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (104,'Burundese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (105,'Botswaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (106,'Burkinese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (108,'Centraal-Afrikaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (109,'Comorese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (110,'Burger van Congo',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (111,'Beninse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (112,'Egyptische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (113,'Equatoriaal-Guinese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (114,'Ethiopische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (115,'Djiboutiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (116,'Gabonese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (117,'Gambiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (118,'Ghanese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (119,'Guinese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (120,'Ivoriaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (121,'Kaapverdische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (122,'Kameroense',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (123,'Kenyaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (124,'Zaïrese',null,'19970715');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (125,'Lesothaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (126,'Liberiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (127,'Libische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (128,'Malagassische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (129,'Malawische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (130,'Malinese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (131,'Marokkaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (132,'Mauritaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (133,'Mauritiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (134,'Mozambikaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (135,'Swazische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (136,'Nigerese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (137,'Nigeriaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (138,'Ugandese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (139,'Guinee-Bissause',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (140,'Zuid-Afrikaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (142,'Zimbabwaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (143,'Rwandese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (144,'Burger van São Tomé en Principe',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (145,'Senegalese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (147,'Sierra Leoonse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (148,'Soedanese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (149,'Somalische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (151,'Tanzaniaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (152,'Togolese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (154,'Tsjadische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (155,'Tunesische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (156,'Zambiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (157,'Zuid-Soedanese','20110709',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (200,'Bahamaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (202,'Belizaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (204,'Canadese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (205,'Costa Ricaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (206,'Cubaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (207,'Dominicaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (208,'Salvadoraanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (211,'Guatemalaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (212,'Haïtiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (213,'Hondurese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (214,'Jamaicaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (216,'Mexicaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (218,'Nicaraguaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (219,'Panamese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (222,'Burger van Trinidad en Tobago',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (223,'Amerikaans burger',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (250,'Argentijnse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (251,'Barbadaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (252,'Boliviaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (253,'Braziliaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (254,'Chileense',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (255,'Colombiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (256,'Ecuadoraanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (259,'Guyaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (261,'Paraguayaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (262,'Peruaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (263,'Surinaamse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (264,'Uruguayaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (265,'Venezolaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (267,'Grenadaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (268,'Burger van Saint Kitts en Nevis',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (300,'Afghaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (301,'Bahreinse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (302,'Bhutaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (303,'Burmaanse',null,'19890618');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (304,'Bruneise',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (305,'Cambodjaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (306,'Sri Lankaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (307,'Chinese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (308,'Cyprische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (309,'Filipijnse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (310,'Taiwanese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (312,'Indiase',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (313,'Indonesische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (314,'Iraakse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (315,'Iraanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (316,'Israëlische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (317,'Japanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (318,'Noord-Jemenitische',null,'19900522');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (319,'Jordaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (320,'Koeweitse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (321,'Laotiaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (322,'Libanese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (324,'Maldivische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (325,'Maleisische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (326,'Mongolische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (327,'Omaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (328,'Nepalese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (329,'Noord-Koreaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (331,'Pakistaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (333,'Qatarese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (334,'Saoedi-Arabische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (335,'Singaporese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (336,'Syrische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (337,'Thaise',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (338,'Burger van de Verenigde Arabische Emiraten',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (339,'Turkse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (340,'Zuid-Jemenitische',null,'19900522');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (341,'Zuid-Koreaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (342,'Vietnamese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (345,'Bengalese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (400,'Australische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (401,'Papoea-Nieuw-Guinese',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (402,'Nieuw-Zeelandse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (404,'West-Samoaanse',null,'19970704');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (405,'Samoaanse','19970704',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (421,'Burger van Antigua en Barbuda',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (424,'Vanuatuaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (425,'Fijische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (429,'Burger van Britse afhankelijke gebieden',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (430,'Tongaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (431,'Nauruaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (432,'Palause','19941001',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (437,'Amerikaans onderdaan',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (442,'Salomonseilandse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (443,'Micronesische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (444,'Seychelse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (445,'Kiribatische',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (446,'Tuvaluaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (447,'Saint Luciaanse',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (448,'Burger van Dominica',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (449,'Burger van Saint Vincent en de Grenadines',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (450,'British National (overseas)','19870701',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (451,'Burger van Democratische Republiek Congo','19970517',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (452,'Burger van Timor Leste','20020520',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (453,'Burger van Servië en Montenegro','20030204','20060603');
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (454,'Servische','20060603',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (455,'Montenegrijnse','20060603',null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (499,'Staatloos',null,null);
INSERT INTO nation (code, omschr, begindatum_geldh, eindd_geldh) VALUES (500,'Vastgesteld niet-Nederlander','20070401','20070401');

GO

-- data update voor issue #435
-- brk 
-- 105 is 'dd-MM-yyyy' zie: https://docs.microsoft.com/en-us/sql/t-sql/functions/cast-and-convert-transact-sql?view=sql-server-2017
UPDATE kad_onrrnd_zk_archief SET datum_einde_geldh = FORMAT(CONVERT(DATETIME,datum_einde_geldh,105),'yyyy-MM-dd') WHERE datum_einde_geldh LIKE '[0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9]';
UPDATE kad_onrrnd_zk_aantek_archief SET eindd_aantek_kad_obj = FORMAT(CONVERT(DATETIME,eindd_aantek_kad_obj,105),'yyyy-MM-dd') WHERE eindd_aantek_kad_obj LIKE '[0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9]';

GO
--bag
UPDATE addresseerb_obj_aand_archief SET dat_eind_geldh = FORMAT(CONVERT(DATETIME,dat_eind_geldh,105),'yyyy-MM-dd') WHERE dat_eind_geldh LIKE '[0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9]';
UPDATE benoemd_terrein_archief SET datum_einde_geldh = FORMAT(CONVERT(DATETIME,datum_einde_geldh,105),'yyyy-MM-dd') WHERE datum_einde_geldh LIKE '[0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9]';
UPDATE gebouwd_obj_archief SET datum_einde_geldh = FORMAT(CONVERT(DATETIME,datum_einde_geldh,105),'yyyy-MM-dd') WHERE datum_einde_geldh LIKE '[0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9]';
UPDATE gem_openb_rmte_archief SET datum_einde_geldh = FORMAT(CONVERT(DATETIME,datum_einde_geldh,105),'yyyy-MM-dd') WHERE datum_einde_geldh LIKE '[0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9]';
UPDATE pand_archief SET datum_einde_geldh = FORMAT(CONVERT(DATETIME,datum_einde_geldh,105),'yyyy-MM-dd') WHERE datum_einde_geldh LIKE '[0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9]';
UPDATE wnplts_archief SET datum_einde_geldh = FORMAT(CONVERT(DATETIME,datum_einde_geldh,105),'yyyy-MM-dd') WHERE datum_einde_geldh LIKE '[0-9][0-9]-[0-9][0-9]-[0-9][0-9][0-9][0-9]';

GO
-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.5.2_naar_1.6.0','vorige versie was ' + waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.0' WHERE naam='brmoversie';

GO
