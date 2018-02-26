--
-- upgrade Oracle RSGB datamodel van 1.5.2 naar 1.5.3
--

-- 118
alter table subject drop constraint fk_sub_vb_4;
alter table ingeschr_nat_prs drop constraint fk_inp_gb_2;
alter table ingeschr_nat_prs drop constraint fk_inp_ol_1;
alter table ingeschr_nat_prs drop constraint fk_inp_rl_17;
alter table ingeschr_nat_prs drop constraint fk_inp_rl_18;


-- 119
INSERT ALL
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('1','paspoort')
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('2','Europese identiteitskaart')
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('3','toeristenkaart')
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('4','gemeentelijke identiteitskaart')
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('5','verblijfsdocument van de Vreemdelingendienst')
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('6','vluchtelingenpaspoort')
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('7','vreemdelingenpaspoort')
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('8','paspoort met aantekening vergunning tot verblijf')
  INTO rsdocsoort (rsdoccode, rsdocomschr) VALUES ('9','(electronisch) W-document')
SELECT 1 FROM DUAL;

-- 120
ALTER TABLE ANDER_BTNLNDS_NIET_NAT_PRS MODIFY SC_IDENTIF VARCHAR2(255);
ALTER TABLE ANDER_NAT_PRS MODIFY SC_IDENTIF VARCHAR2(255);
ALTER TABLE APP_RE MODIFY FK_2NNP_SC_IDENTIF VARCHAR2(255);
ALTER TABLE INGESCHR_NIET_NAT_PRS MODIFY SC_IDENTIF VARCHAR2(255);
ALTER TABLE INGESCHR_NAT_PRS MODIFY SC_IDENTIF VARCHAR2(255);
ALTER TABLE INGEZETENE MODIFY SC_IDENTIF VARCHAR2(255);
ALTER TABLE KAD_ONRRND_ZK MODIFY FK_10PES_SC_IDENTIF VARCHAR2(255);
ALTER TABLE KAD_ONRRND_ZK_AANTEK MODIFY FK_5PES_SC_IDENTIF VARCHAR2(255);
ALTER TABLE MAATSCHAPP_ACTIVITEIT MODIFY FK_4PES_SC_IDENTIF VARCHAR2(255);
ALTER TABLE NAT_PRS MODIFY SC_IDENTIF VARCHAR2(255);
ALTER TABLE NIET_INGEZETENE MODIFY SC_IDENTIF VARCHAR2(255);
ALTER TABLE NIET_NAT_PRS MODIFY SC_IDENTIF VARCHAR2(255);
ALTER TABLE PRS MODIFY  SC_IDENTIF VARCHAR2(255);
ALTER TABLE SUBJECT MODIFY IDENTIF VARCHAR2(255);
ALTER TABLE VESTG MODIFY  SC_IDENTIF VARCHAR2(255);
ALTER TABLE VESTG MODIFY FK_18VES_SC_IDENTIF VARCHAR2(255);
ALTER TABLE ZAK_RECHT MODIFY FK_8PES_SC_IDENTIF VARCHAR2(255);
ALTER TABLE ZAK_RECHT_AANTEK MODIFY FK_6PES_SC_IDENTIF VARCHAR2(255);
ALTER TABLE VESTG_NAAM MODIFY FK_VES_SC_IDENTIF VARCHAR2(255);
ALTER TABLE HUISHOUDENREL MODIFY FK_SC_LH_INP_SC_IDENTIF VARCHAR2(255);
ALTER TABLE HUW_GER_PARTN MODIFY FK_SC_LH_INP_SC_IDENTIF VARCHAR2(255);
ALTER TABLE HUW_GER_PARTN MODIFY FK_SC_RH_INP_SC_IDENTIF VARCHAR2(255);
ALTER TABLE OUDER_KIND_REL MODIFY FK_SC_LH_INP_SC_IDENTIF VARCHAR2(255);
ALTER TABLE OUDER_KIND_REL MODIFY FK_SC_RH_INP_SC_IDENTIF VARCHAR2(255);
ALTER TABLE RSDOC_INGESCHR_NAT_PRS MODIFY FK_NN_RH_INP_SC_IDENTIF VARCHAR2(255);
ALTER TABLE VESTG_BENOEMD_OBJ MODIFY FK_NN_LH_VES_SC_IDENTIF VARCHAR2(255);

ALTER TABLE FUNCTIONARIS MODIFY (FK_SC_LH_PES_SC_IDENTIF VARCHAR2(255 CHAR) );
ALTER TABLE FUNCTIONARIS MODIFY (FK_SC_RH_PES_SC_IDENTIF VARCHAR2(255 CHAR) );
ALTER TABLE WOZ_BELANG MODIFY (FK_SC_LH_SUB_IDENTIF VARCHAR2(255) );
ALTER TABLE VESTG_ACTIVITEIT  MODIFY (FK_VESTG_NUMMER VARCHAR2(255) );
ALTER TABLE PRS_EIGENDOM  MODIFY (FK_PRS_SC_IDENTIF VARCHAR2(255) );

-- issue #411
-- appertementsrecht met bag adres
CREATE OR REPLACE VIEW v_app_re_adres AS
SELECT DISTINCT
        kp.sc_kad_identif,
        kpvbo.FK_NN_LH_TGO_IDENTIF AS kad_bag_koppeling_benobj,
        gor.naam_openb_rmte AS straat,
        aoa.huinummer AS huisnummer,
        aoa.huisletter,
        aoa.huinummertoevoeging AS toevoeging,
        aoa.postcode,
        wp.naam AS woonplaats
FROM app_re kp
JOIN benoemd_obj_kad_onrrnd_zk kpvbo ON (kpvbo.FK_NN_RH_KOZ_KAD_IDENTIF = kp.SC_KAD_IDENTIF)
LEFT JOIN verblijfsobj vbo ON (vbo.SC_IDENTIF = kpvbo.FK_NN_LH_TGO_IDENTIF)
LEFT JOIN nummeraand na ON (na.SC_IDENTIF = vbo.FK_11NRA_SC_IDENTIF)
LEFT JOIN addresseerb_obj_aand aoa ON (aoa.IDENTIF = na.SC_IDENTIF)
LEFT JOIN gem_openb_rmte gor ON (gor.IDENTIFCODE = aoa.FK_7OPR_IDENTIFCODE)
LEFT JOIN openb_rmte_wnplts oprw ON (oprw.FK_NN_LH_OPR_IDENTIFCODE = gor.IDENTIFCODE)
LEFT JOIN wnplts wp ON (wp.IDENTIF = oprw.FK_NN_RH_WPL_IDENTIF);

COMMENT ON VIEW v_app_re_adres
  IS 'appertementsrecht met bag adres';

-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.5.2_naar_1.5.3','vorige versie was '||waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.5.3' WHERE naam='brmoversie';
