--snijdt alle banden tussen BAG en andere BRs door
--zodat BAG verwijderd en weer opnieuw geladen kan worden
alter table addresseerb_obj_aand drop constraint fk_aoa_as_6;
alter table benoemd_obj_kad_onrrnd_zk drop constraint fk_tgo_koz_nn_lh;
alter table ander_nat_prs drop constraint fk_anp_as_3;
alter table begr_terr_dl drop constraint fk_btd_as_8;
alter table gem_openb_rmte drop constraint fk_gor_as_7;
alter table huishouden drop constraint fk_hhd_as_5;
alter table huishouden drop constraint fk_hhd_as_7;
alter table huishouden drop constraint fk_hhd_as_4;
alter table ingeschr_nat_prs drop constraint fk_inp_as_27;
alter table ingeschr_nat_prs drop constraint fk_inp_va_as_7;
alter table ingeschr_nat_prs drop constraint fk_inp_as_28;
alter table ingeschr_nat_prs drop constraint fk_inp_va_as_5;
alter table ingeschr_nat_prs drop constraint fk_inp_as_29;
alter table ingeschr_nat_prs drop constraint fk_inp_va_as_4;
alter table ingeschr_nat_prs drop constraint fk_inp_as_30;
alter table ingeschr_nat_prs drop constraint fk_inp_va_as_3;
alter table ingeschr_nat_prs drop constraint fk_inp_as_31;
alter table ingeschr_nat_prs drop constraint fk_inp_va_as_6;
alter table ingeschr_niet_nat_prs drop constraint fk_inn_as_8;
alter table locaand_adres drop constraint fk_locaand_adres_sc_lh;
alter table onbegr_terr_dl drop constraint fk_obt_as_8;
alter table ondersteunend_wegdeel drop constraint fk_owd_as_6;
alter table subject drop constraint fk_sub_as_14;
alter table subject drop constraint fk_sub_as_15;
alter table subject drop constraint fk_sub_as_13;
alter table subject drop constraint fk_sub_pa_as_4;
alter table vestg drop constraint fk_ves_as_16;
alter table vestg drop constraint fk_ves_as_20;
alter table vestg drop constraint fk_ves_sc;
alter table vestg_benoemd_obj drop constraint fk_ves_tgo_nn_rh;
alter table waterdeel drop constraint fk_wad_as_7;
alter table wegdeel drop constraint fk_wgd_as_8;
alter table woz_deelobj drop constraint fk_wdo_as_5;
alter table woz_deelobj drop constraint fk_wdo_as_4;

-- zet fk constraint om niet cascade delete naar woonplaats te doen
alter table openb_rmte_wnplts drop constraint fk_opr_wpl_nn_rh;
alter table openb_rmte_wnplts add constraint fk_opr_wpl_nn_rh foreign key (fk_nn_rh_wpl_identif) references wnplts (identif);
alter table wnplts drop constraint fk_wpl_as_7;
alter table wnplts add constraint fk_wpl_as_7 foreign key (fk_7gem_code) references gemeente (code);

-- Referentielijst niet gevuld
alter table zak_recht drop constraint fk_zkr_rl_3;

-- Rechterkant mogelijk nog niet geinsert
alter table kad_onrrnd_zk_his_rel drop constraint fk_kad_onrrnd_zk_his_rel_sc_rh;

