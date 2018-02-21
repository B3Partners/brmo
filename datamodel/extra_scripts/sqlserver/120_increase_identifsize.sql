alter table ander_btnlnds_niet_nat_prs drop constraint ander_btnlnds_niet_nat_prs_pk;
alter table ander_btnlnds_niet_nat_prs drop constraint fk_ann_sc;
alter table ander_nat_prs drop constraint ander_nat_prs_pk;
alter table ander_nat_prs drop constraint fk_anp_sc;
alter table ingeschr_niet_nat_prs drop constraint ingeschr_niet_nat_prs_pk;
alter table ingeschr_nat_prs drop constraint ingeschr_nat_prs_pk;
alter table ingezetene drop constraint ingezetene_pk;
alter table kad_onrrnd_zk drop constraint fk_koz_as_10;
alter table kad_onrrnd_zk_aantek drop constraint fk_kza_as_5;
alter table maatschapp_activiteit drop constraint fk_mac_as_4;
alter table nat_prs drop constraint nat_prs_pk;
alter table niet_ingezetene drop constraint niet_ingezetene_pk;
alter table niet_nat_prs drop constraint niet_nat_prs_pk;
alter table app_re drop constraint fk_apr_as_2;
alter table prs drop constraint prs_pk;
alter table subject drop constraint subject_pk;
alter table vestg drop constraint vestg_pk;
alter table vestg drop constraint fk_ves_as_18;


GO

alter table ander_btnlnds_niet_nat_prs alter column sc_identif varchar(255) not null;
alter table ander_nat_prs alter column sc_identif varchar(255) not null;
alter table ingeschr_niet_nat_prs alter column sc_identif varchar(255) not null;
alter table ingeschr_nat_prs alter column sc_identif varchar(255) not null;
alter table ingezetene alter column sc_identif varchar(255) not null;
alter table kad_onrrnd_zk alter column fk_10pes_sc_identif varchar(255);
alter table kad_onrrnd_zk_aantek alter column fk_5pes_sc_identif varchar(255);
alter table maatschapp_activiteit alter column fk_4pes_sc_identif varchar(255);
alter table nat_prs alter column sc_identif varchar(255) not null;
alter table niet_ingezetene alter column sc_identif varchar(255) not null;
alter table niet_nat_prs alter column sc_identif varchar(255) not null;
alter table app_re alter column fk_2nnp_sc_identif varchar(255);
alter table prs alter column sc_identif varchar(255) not null;
alter table subject alter column identif varchar(255) not null;
alter table vestg alter column sc_identif varchar(255) not null;
alter table vestg alter column fk_18ves_sc_identif varchar(255);

-- todo
alter table zak_recht alter column fk_8pes_sc_identif varchar(255);
alter table zak_recht_aantek alter column fk_6pes_sc_identif varchar(255);
alter table vestg_naam alter column fk_ves_sc_identif varchar(255);
alter table huishoudenrel alter column fk_sc_lh_inp_sc_identif varchar(255);
alter table huw_ger_partn alter column fk_sc_lh_inp_sc_identif varchar(255);
alter table huw_ger_partn alter column fk_sc_rh_inp_sc_identif varchar(255);
alter table ouder_kind_rel alter column fk_sc_lh_inp_sc_identif varchar(255);
alter table ouder_kind_rel alter column fk_sc_rh_inp_sc_identif varchar(255);
alter table rsdoc_ingeschr_nat_prs alter column fk_nn_rh_inp_sc_identif varchar(255);
alter table vestg_benoemd_obj alter column fk_nn_lh_ves_sc_identif varchar(255);

GO

alter table ander_btnlnds_niet_nat_prs add constraint ander_btnlnds_niet_nat_prs_pk primary key clustered(sc_identif);
alter table ander_btnlnds_niet_nat_prs add constraint fk_ann_sc foreign key (sc_identif) references niet_nat_prs (sc_identif) on delete no action;
alter table ander_nat_prs add constraint ander_nat_prs_pk primary key clustered(sc_identif);
alter table ander_nat_prs add constraint fk_anp_sc foreign key (sc_identif) references nat_prs (sc_identif) on delete no action;
alter table ingeschr_niet_nat_prs add constraint ingeschr_niet_nat_prs_pk primary key clustered(sc_identif);
alter table ingeschr_nat_prs add constraint ingeschr_nat_prs_pk primary key clustered(sc_identif);
alter table ingezetene add constraint ingezetene_pk primary key clustered(sc_identif);
alter table kad_onrrnd_zk add constraint fk_koz_as_10 foreign key (fk_10pes_sc_identif) references prs (sc_identif) on delete no action;
alter table kad_onrrnd_zk_aantek add constraint fk_kza_as_5 foreign key (fk_5pes_sc_identif) references prs (sc_identif) on delete no action;
alter table maatschapp_activiteit add constraint fk_mac_as_4 foreign key (fk_4pes_sc_identif) references prs (sc_identif) on delete no action;
alter table nat_prs add constraint nat_prs_pk primary key clustered(sc_identif);
alter table niet_ingezetene add constraint niet_ingezetene_pk primary key clustered(sc_identif);
alter table niet_nat_prs add constraint niet_nat_prs_pk primary key clustered(sc_identif);
alter table app_re add constraint fk_apr_as_2 foreign key (fk_2nnp_sc_identif) references niet_nat_prs (sc_identif) on delete no action;
alter table prs add constraint prs_pk primary key clustered(sc_identif);
alter table subject add constraint subject_pk primary key clustered(identif);
alter table vestg add constraint vestg_pk primary key clustered(sc_identif);
alter table vestg add constraint fk_ves_as_18 foreign key (fk_18ves_sc_identif) references vestg (sc_identif) on delete no action;



GO
