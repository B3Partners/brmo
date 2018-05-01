--
-- upgrade Oracle RSGB datamodel van 1.5.2 naar 1.6.0
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
-- appartementsrecht aan bag adres
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

COMMENT ON VIEW v_app_re_adres IS 'appartementsrecht met bag adres';

-- alle kad_onrrnd_zk gekoppeld aan bag adres
CREATE OR REPLACE VIEW v_kad_onrrd_zk_adres AS
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
    JOIN benoemd_obj_kad_onrrnd_zk kpvbo ON (kpvbo.FK_NN_RH_KOZ_KAD_IDENTIF = kp.KAD_IDENTIF)
    LEFT JOIN verblijfsobj vbo ON (vbo.SC_IDENTIF = kpvbo.FK_NN_LH_TGO_IDENTIF)
    LEFT JOIN nummeraand na ON (na.SC_IDENTIF = vbo.FK_11NRA_SC_IDENTIF)
    LEFT JOIN addresseerb_obj_aand aoa ON (aoa.IDENTIF = na.SC_IDENTIF)
    LEFT JOIN gem_openb_rmte gor ON (gor.IDENTIFCODE = aoa.FK_7OPR_IDENTIFCODE)
    LEFT JOIN openb_rmte_wnplts oprw ON (oprw.FK_NN_LH_OPR_IDENTIFCODE = gor.IDENTIFCODE)
    LEFT JOIN wnplts wp ON (wp.IDENTIF = oprw.FK_NN_RH_WPL_IDENTIF);

COMMENT ON VIEW v_kad_onrrd_zk_adres IS 'onroerende zaak met bag adres';

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

-- data update voor issue #435
-- brk
UPDATE kad_onrrnd_zk_archief SET datum_einde_geldh = TO_CHAR(TO_DATE(datum_einde_geldh, 'DD-MM-YYYY'),'YYYY-MM-DD') WHERE REGEXP_LIKE(datum_einde_geldh, '\d{2}-\d{2}-\d{4}');
UPDATE kad_onrrnd_zk_aantek_archief SET eindd_aantek_kad_obj = TO_CHAR(TO_DATE(eindd_aantek_kad_obj, 'DD-MM-YYYY'),'YYYY-MM-DD') WHERE REGEXP_LIKE(eindd_aantek_kad_obj, '\d{2}-\d{2}-\d{4}');
-- bag
UPDATE addresseerb_obj_aand_archief SET dat_eind_geldh = TO_CHAR(TO_DATE(dat_eind_geldh, 'DD-MM-YYYY'),'YYYY-MM-DD') WHERE REGEXP_LIKE(dat_eind_geldh, '\d{2}-\d{2}-\d{4}');
UPDATE benoemd_terrein_archief SET datum_einde_geldh = TO_CHAR(TO_DATE(datum_einde_geldh, 'DD-MM-YYYY'),'YYYY-MM-DD') WHERE REGEXP_LIKE(datum_einde_geldh, '\d{2}-\d{2}-\d{4}');
UPDATE gebouwd_obj_archief SET datum_einde_geldh = TO_CHAR(TO_DATE(datum_einde_geldh, 'DD-MM-YYYY'),'YYYY-MM-DD') WHERE REGEXP_LIKE(datum_einde_geldh, '\d{2}-\d{2}-\d{4}');
UPDATE gem_openb_rmte_archief SET datum_einde_geldh = TO_CHAR(TO_DATE(datum_einde_geldh, 'DD-MM-YYYY'),'YYYY-MM-DD') WHERE REGEXP_LIKE(datum_einde_geldh, '\d{2}-\d{2}-\d{4}');
UPDATE pand_archief SET datum_einde_geldh = TO_CHAR(TO_DATE(datum_einde_geldh, 'DD-MM-YYYY'),'YYYY-MM-DD') WHERE REGEXP_LIKE(datum_einde_geldh, '\d{2}-\d{2}-\d{4}');
UPDATE wnplts_archief SET datum_einde_geldh = TO_CHAR(TO_DATE(datum_einde_geldh, 'DD-MM-YYYY'),'YYYY-MM-DD') WHERE REGEXP_LIKE(datum_einde_geldh, '\d{2}-\d{2}-\d{4}');


-- onderstaande dienen als laatste stappen van een upgrade uitgevoerd
INSERT INTO brmo_metadata (naam,waarde) SELECT 'upgrade_1.5.2_naar_1.6.0','vorige versie was '||waarde FROM brmo_metadata WHERE naam='brmoversie';
-- versienummer update
UPDATE brmo_metadata SET waarde='1.6.0' WHERE naam='brmoversie';
