--
-- upgrade SQLserver RSGB datamodel van 1.5.2 naar 1.5.3
--

ALTER TABLE ander_btnlnds_niet_nat_prs ALTER COLUMN sc_identif VARCHAR(255);
ALTER TABLE ander_nat_prs ALTER COLUMN sc_identif VARCHAR(255);
ALTER TABLE app_re ALTER COLUMN fk_2nnp_sc_identif VARCHAR(255);
ALTER TABLE ingeschr_niet_nat_prs ALTER COLUMN sc_identif VARCHAR(255);
ALTER TABLE ingeschr_nat_prs ALTER COLUMN sc_identif VARCHAR(255);
ALTER TABLE ingezetene ALTER COLUMN sc_identif VARCHAR(255);
ALTER TABLE kad_onrrnd_zk ALTER COLUMN fk_10pes_sc_identif VARCHAR(255);
ALTER TABLE kad_onrrnd_zk_aantek ALTER COLUMN fk_5pes_sc_identif VARCHAR(255);
ALTER TABLE maatschapp_activiteit ALTER COLUMN fk_4pes_sc_identif VARCHAR(255);
ALTER TABLE nat_prs ALTER COLUMN sc_identif VARCHAR(255);
ALTER TABLE niet_ingezetene ALTER COLUMN sc_identif VARCHAR(255);
ALTER TABLE niet_nat_prs ALTER COLUMN sc_identif VARCHAR(255);
ALTER TABLE prs ALTER COLUMN  sc_identif VARCHAR(255);
ALTER TABLE subject ALTER COLUMN identif VARCHAR(255);
ALTER TABLE vestg ALTER COLUMN  sc_identif VARCHAR(255);
ALTER TABLE vestg ALTER COLUMN fk_18ves_sc_identif VARCHAR(255);
ALTER TABLE zak_recht ALTER COLUMN fk_8pes_sc_identif VARCHAR(255);
ALTER TABLE zak_recht_aantek ALTER COLUMN fk_6pes_sc_identif VARCHAR(255);
ALTER TABLE vestg_naam ALTER COLUMN fk_ves_sc_identif VARCHAR(255);
ALTER TABLE huishoudenrel ALTER COLUMN fk_sc_lh_inp_sc_identif VARCHAR(255);
ALTER TABLE huw_ger_partn ALTER COLUMN fk_sc_lh_inp_sc_identif VARCHAR(255);
ALTER TABLE huw_ger_partn ALTER COLUMN fk_sc_rh_inp_sc_identif VARCHAR(255);
ALTER TABLE ouder_kind_rel ALTER COLUMN fk_sc_lh_inp_sc_identif VARCHAR(255);
ALTER TABLE ouder_kind_rel ALTER COLUMN fk_sc_rh_inp_sc_identif VARCHAR(255);
ALTER TABLE rsdoc_ingeschr_nat_prs ALTER COLUMN fk_nn_rh_inp_sc_identif VARCHAR(255);
ALTER TABLE vestg_benoemd_obj ALTER COLUMN fk_nn_lh_ves_sc_identif VARCHAR(255);

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.5.2_naar_1.5.3','vorige versie was '||waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.5.3' WHERE naam='brmoversie';
