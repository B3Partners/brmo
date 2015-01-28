
-- BAG koppeling
alter table benoemd_obj_kad_onrrnd_zk drop constraint fk_tgo_koz_nn_lh;

-- Referentielijst niet gevuld
alter table zak_recht drop constraint fk_zkr_rl_3;

-- Rechterkant mogelijk nog niet geinsert
alter table kad_onrrnd_zk_his_rel drop constraint fk_kad_onrrnd_zk_his_rel_sc_rh;

