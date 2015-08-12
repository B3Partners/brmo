create table sbi_activiteit(
	omschr character varying(60),
	sbi_code character varying(6)
);
alter table sbi_activiteit add constraint sbi_activiteit_pk primary key(sbi_code);
comment on table sbi_activiteit is 'RSGB referentielijst SBI ACTIVITEIT';
comment on column sbi_activiteit.omschr is 'Referentielijst property SBI ACTIVITEIT.Omschrijving - Omschrijving';
comment on column sbi_activiteit.sbi_code is 'Referentielijst property SBI ACTIVITEIT.SBI code - SBI code';

create table aard_recht_verkort(
	aand character varying(4),
	omschr character varying(100)
);
alter table aard_recht_verkort add constraint aard_recht_verkort_pk primary key(aand);
comment on table aard_recht_verkort is 'RSGB referentielijst AARD RECHT VERKORT';
comment on column aard_recht_verkort.aand is 'Referentielijst property AARD RECHT VERKORT.Aanduiding aard recht verkort - Aanduiding aard recht verkort';
comment on column aard_recht_verkort.omschr is 'Referentielijst property AARD RECHT VERKORT.Omschrijving aard recht verkort - Omschrijving aard recht verkort';

create table aard_verkregen_recht(
	aand character varying(6),
	omschr_aard_verkregenr_recht character varying(200)
);
alter table aard_verkregen_recht add constraint aard_verkregen_recht_pk primary key(aand);
comment on table aard_verkregen_recht is 'RSGB referentielijst AARD VERKREGEN RECHT';
comment on column aard_verkregen_recht.aand is 'Referentielijst property AARD VERKREGEN RECHT.Aanduiding aard verkregen recht - Aanduiding aard verkregen recht';
comment on column aard_verkregen_recht.omschr_aard_verkregenr_recht is 'Referentielijst property AARD VERKREGEN RECHT.Omschrijving aard verkregenr recht - Omschrijving aard verkregenr recht';

create table academische_titel(
	code character varying(3),
	dat_beg_geldh_titel character varying(19),
	datum_einde_geldh_titel character varying(19),
	omschr character varying(80),
	positie_tov_naam character varying(1)
);
alter table academische_titel add constraint academische_titel_pk primary key(code);
comment on table academische_titel is 'RSGB referentielijst ACADEMISCHE TITEL';
comment on column academische_titel.code is 'Referentielijst property ACADEMISCHE TITEL.Academische titelcode - Academische titelcode';
comment on column academische_titel.dat_beg_geldh_titel is 'Referentielijst property ACADEMISCHE TITEL.Datum begin geldigheid titel - Datum begin geldigheid titel';
comment on column academische_titel.datum_einde_geldh_titel is 'Referentielijst property ACADEMISCHE TITEL.Datum einde geldigheid titel - Datum einde geldigheid titel';
comment on column academische_titel.omschr is 'Referentielijst property ACADEMISCHE TITEL.Omschrijving academische titel - Omschrijving academische titel';
comment on column academische_titel.positie_tov_naam is 'Referentielijst property ACADEMISCHE TITEL.Positie academische titel tov naam - Positie academische titel tov naam';

create table land(
	eindd character varying(19),
	ingangsdatum character varying(19),
	code decimal(4,0),
	code_iso character varying(2),
	naam character varying(40)
);
alter table land add constraint land_pk primary key(code_iso);
comment on table land is 'RSGB referentielijst LAND';
comment on column land.eindd is 'Referentielijst property LAND.Einddatum land - Einddatum land';
comment on column land.ingangsdatum is 'Referentielijst property LAND.Ingangsdatum land - Ingangsdatum land';
comment on column land.code is 'Referentielijst property LAND.Landcode - Landcode';
comment on column land.code_iso is 'Referentielijst property LAND.Landcode ISO - Landcode ISO';
comment on column land.naam is 'Referentielijst property LAND.Landnaam - Landnaam';

create table nation(
	begindatum_geldh character varying(19),
	eindd_geldh character varying(19),
	code decimal(4,0),
	omschr character varying(42)
);
alter table nation add constraint nation_pk primary key(code);
comment on table nation is 'RSGB referentielijst NATIONALITEIT';
comment on column nation.begindatum_geldh is 'Referentielijst property NATIONALITEIT.Begindatum geldigheid nationaliteit - Begindatum geldigheid nationaliteit';
comment on column nation.eindd_geldh is 'Referentielijst property NATIONALITEIT.Einddatum geldigheid nationaliteit - Einddatum geldigheid nationaliteit';
comment on column nation.code is 'Referentielijst property NATIONALITEIT.Nationaliteitcode - Nationaliteitcode';
comment on column nation.omschr is 'Referentielijst property NATIONALITEIT.Nationaliteitomschrijving - Nationaliteitomschrijving';

create table rsdocsoort(
	begindatum_geldh character varying(19),
	eindd_geldh character varying(19),
	rsdoccode character varying(2),
	rsdocomschr character varying(80)
);
alter table rsdocsoort add constraint rsdocsoort_pk primary key(rsdoccode);
comment on table rsdocsoort is 'RSGB referentielijst REISDOCUMENTSOORT';
comment on column rsdocsoort.begindatum_geldh is 'Referentielijst property REISDOCUMENTSOORT.Begindatum geldigheid reisdocumentsoort - Begindatum geldigheid reisdocumentsoort';
comment on column rsdocsoort.eindd_geldh is 'Referentielijst property REISDOCUMENTSOORT.Einddatum geldigheid reisdocumentsoort - Einddatum geldigheid reisdocumentsoort';
comment on column rsdocsoort.rsdoccode is 'Referentielijst property REISDOCUMENTSOORT.Reisdocumentcode - Reisdocumentcode';
comment on column rsdocsoort.rsdocomschr is 'Referentielijst property REISDOCUMENTSOORT.Reisdocumentomschrijving - Reisdocumentomschrijving';

create table addresseerb_obj_aand(
	dat_beg_geldh character varying(19),
	identif character varying(16),
	clazz character varying(255),
	dat_eind_geldh character varying(19),
	huisletter character varying(1),
	huinummer decimal(5,0),
	huinummertoevoeging character varying(4),
	postcode character varying(6),
	fk_6wpl_identif character varying(4),
	fk_7opr_identifcode character varying(16)
);
alter table addresseerb_obj_aand add constraint addresseerb_obj_aand_pk primary key(identif);
comment on table addresseerb_obj_aand is 'RSGB class ADRESSEERBAAR OBJECT AANDUIDING. Directe superclass van: NUMMERAANDUIDING, OVERIGE ADRESSEERBAAR OBJECT AANDUIDING';
comment on column addresseerb_obj_aand.identif is '[PK] AN16 - Identificatie adresseerbaar object aanduiding';
comment on column addresseerb_obj_aand.clazz is 'Aanduiding subclass';
comment on column addresseerb_obj_aand.dat_eind_geldh is 'OnvolledigeDatum - Datum einde geldigheid addresserbaar object aanduiding';
comment on column addresseerb_obj_aand.huisletter is 'AN1 - Huisletter';
comment on column addresseerb_obj_aand.huinummer is 'N5 - Huisnummer';
comment on column addresseerb_obj_aand.huinummertoevoeging is 'AN4 - Huisnummertoevoeging';
comment on column addresseerb_obj_aand.postcode is 'AN6 - Postcode';
comment on column addresseerb_obj_aand.fk_6wpl_identif is '[FK] AN4, FK naar wnplts.identif: "ligt in"';
comment on column addresseerb_obj_aand.fk_7opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode: "ligt aan"';

create table ander_btnlnds_niet_nat_prs(
	sc_identif character varying(32)
);
alter table ander_btnlnds_niet_nat_prs add constraint ander_btnlnds_niet_nat_prs_pk primary key(sc_identif);
comment on table ander_btnlnds_niet_nat_prs is 'RSGB class ANDER BUITENLANDS NIET-NATUURLIJK PERSOON. Subclass van: NIET-NATUURLIJK PERSOON -> PERSOON -> SUBJECT';
comment on column ander_btnlnds_niet_nat_prs.sc_identif is '[PK] AN32, FK naar niet_nat_prs.sc_identif - Identificatie';

create table ander_nat_prs(
	sc_identif character varying(32),
	geboortedatum decimal(8,0),
	overlijdensdatum decimal(8,0),
	fk_3aoa_identif character varying(16)
);
alter table ander_nat_prs add constraint ander_nat_prs_pk primary key(sc_identif);
comment on table ander_nat_prs is 'RSGB class ANDER NATUURLIJK PERSOON. Subclass van: NATUURLIJK PERSOON -> PERSOON -> SUBJECT';
comment on column ander_nat_prs.sc_identif is '[PK] AN32, FK naar nat_prs.sc_identif - Identificatie';
comment on column ander_nat_prs.geboortedatum is 'N8 - Geboortedatum';
comment on column ander_nat_prs.overlijdensdatum is 'N8 - Overlijdensdatum';
comment on column ander_nat_prs.fk_3aoa_identif is '[FK] AN16, FK naar addresseerb_obj_aand.identif: "heeft als bezoekadres"';

create table app_re(
	sc_kad_identif decimal(15,0),
	fk_2nnp_sc_identif character varying(32),
	ka_appartementsindex character varying(4),
	ka_kad_gemeentecode character varying(5),
	ka_perceelnummer character varying(15),
	ka_sectie character varying(255)
);
alter table app_re add constraint app_re_pk primary key(sc_kad_identif);
comment on table app_re is 'RSGB class APPARTEMENTSRECHT. Subclass van: KADASTRALE ONROERENDE ZAAK';
comment on column app_re.sc_kad_identif is '[PK] N15, FK naar kad_onrrnd_zk.kad_identif - Kadastrale identificatie';
comment on column app_re.fk_2nnp_sc_identif is '[FK] AN32, FK naar niet_nat_prs.sc_identif (is FK naar superclass SUBJECT): "maakt deel uit van appartementencomplex met als vereniging van eigenaars"';
comment on column app_re.ka_appartementsindex is 'Groepsattribuut Kadastrale aanduiding APPARTEMENTSRECHT.Appartementsindex - Appartementsindex';
comment on column app_re.ka_kad_gemeentecode is 'Groepsattribuut Kadastrale aanduiding APPARTEMENTSRECHT.Kadastrale gemeentecode - Kadastrale gemeentecode';
comment on column app_re.ka_perceelnummer is 'Groepsattribuut Kadastrale aanduiding APPARTEMENTSRECHT.Perceelnummer - Perceelnummer';
comment on column app_re.ka_sectie is 'Groepsattribuut Kadastrale aanduiding APPARTEMENTSRECHT.Sectie - Sectie';

create table begr_terr_dl(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	clazz character varying(255),
	datum_einde_geldh character varying(19),
	fysiek_voork_begr_terrein character varying(20),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	fk_8opr_identifcode character varying(16)
);
select addgeometrycolumn('begr_terr_dl', 'geom', 28992, 'MULTIPOLYGON', 2);
create index begr_terr_dl_geom_idx on begr_terr_dl USING GIST (geom);
select addgeometrycolumn('begr_terr_dl', 'kruinlijngeom', 28992, 'LINESTRING', 2);
create index begr_terr_dl_kruinlijngeom_idx on begr_terr_dl USING GIST (kruinlijngeom);
alter table begr_terr_dl add constraint begr_terr_dl_pk primary key(identif);
comment on table begr_terr_dl is 'RSGB class BEGROEID TERREINDEEL. Directe superclass van: BEGROEID TERREINVAKONDERDEEL';
comment on column begr_terr_dl.identif is '[PK] NEN3610ID - Identificatie begroeid terreindeel';
comment on column begr_terr_dl.clazz is 'Aanduiding subclass';
comment on column begr_terr_dl.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid begroeid terreindeel';
comment on column begr_terr_dl.fysiek_voork_begr_terrein is 'AN20 - Fysiek voorkomen begroeid terrein';
comment on column begr_terr_dl.geom is 'GM_Surface - Geometrie begroeid terreindeel';
comment on column begr_terr_dl.kruinlijngeom is 'GM_Curve - Kruinlijneometrie begroeid terreindeel';
comment on column begr_terr_dl.relve_hoogteligging is 'N1 - Relatieve hoogteligging begroeid terreindeel';
comment on column begr_terr_dl.status is 'A8 - Status begroeid terreindeel';
comment on column begr_terr_dl.fk_8opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode: "maakt deel uit van"';

create table begr_terreinvakonderd(
	sc_identif character varying(255),
	fk_1btd_identif character varying(255)
);
alter table begr_terreinvakonderd add constraint begr_terreinvakonderd_pk primary key(sc_identif);
comment on table begr_terreinvakonderd is 'RSGB class BEGROEID TERREINVAKONDERDEEL. Subclass van: BEGROEID TERREINDEEL';
comment on column begr_terreinvakonderd.sc_identif is '[PK] NEN3610ID, FK naar begr_terr_dl.identif - Identificatie begroeid terreindeel';
comment on column begr_terreinvakonderd.fk_1btd_identif is '[FK] NEN3610ID, FK naar begr_terr_dl.identif';

create table benoemd_obj(
	identif character varying(16),
	clazz character varying(255)
);
alter table benoemd_obj add constraint benoemd_obj_pk primary key(identif);
comment on table benoemd_obj is 'RSGB class BENOEMD OBJECT. Directe superclass van: BENOEMD TERREIN, GEBOUWD OBJECT';
comment on column benoemd_obj.identif is '[PK] AN16 - Benoemd object identificatie';
comment on column benoemd_obj.clazz is 'Aanduiding subclass';

create table benoemd_terrein(
	dat_beg_geldh character varying(19),
	sc_identif character varying(16),
	clazz character varying(255),
	datum_einde_geldh character varying(19)
);
select addgeometrycolumn('benoemd_terrein', 'geom', 28992, 'MULTIPOLYGON', 2);
create index benoemd_terrein_geom_idx on benoemd_terrein USING GIST (geom);
alter table benoemd_terrein add constraint benoemd_terrein_pk primary key(sc_identif);
comment on table benoemd_terrein is 'RSGB class BENOEMD TERREIN. Subclass van: BENOEMD OBJECT. Directe superclass van: LIGPLAATS, OVERIG TERREIN, STANDPLAATS';
comment on column benoemd_terrein.sc_identif is '[PK] AN16, FK naar benoemd_obj.identif - Benoemd object identificatie';
comment on column benoemd_terrein.clazz is 'Aanduiding subclass';
comment on column benoemd_terrein.geom is 'GM_Surface - Benoemd terrein geometrie';
comment on column benoemd_terrein.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid benoemd terrein';

create table brugconstructie_element(
	sc_identif character varying(255),
	type character varying(40)
);
alter table brugconstructie_element add constraint brugconstructie_element_pk primary key(sc_identif);
comment on table brugconstructie_element is 'RSGB class BRUGCONSTRUCTIE ELEMENT. Subclass van: KUNSTWERKDEEL';
comment on column brugconstructie_element.sc_identif is '[PK] NEN3610ID, FK naar kunstwerkdeel.identif - Identificatie kunstwerkdeel';
comment on column brugconstructie_element.type is 'AN40 - Type brugconstructie element';

create table buurt(
	code decimal(2,0),
	dat_beg_geldh character varying(19),
	naam character varying(40),
	datum_einde_geldh character varying(19),
	identif_imgeobrt character varying(255),
	relve_hoogteligging decimal(1,0),
	status character varying(8)
);
select addgeometrycolumn('buurt', 'geom', 28992, 'MULTIPOLYGON', 2);
create index buurt_geom_idx on buurt USING GIST (geom);
alter table buurt add constraint buurt_pk primary key(code);
comment on table buurt is 'RSGB class BUURT';
comment on column buurt.code is '[PK] N2 - Buurtcode';
comment on column buurt.geom is 'GM_Surface - Buurtgeometrie';
comment on column buurt.naam is 'AN40 - Buurtnaam';
comment on column buurt.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid buurt';
comment on column buurt.identif_imgeobrt is 'NEN3610ID - Identificatie IMGeoBRT';
comment on column buurt.relve_hoogteligging is 'N1 - Relatieve hoogteligging buurt';
comment on column buurt.status is 'A8 - Status buurt';

create table functioneel_gebied(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	naam character varying(40),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type character varying(255)
);
select addgeometrycolumn('functioneel_gebied', 'geom', 28992, 'MULTIPOLYGON', 2);
create index functioneel_gebied_geom_idx on functioneel_gebied USING GIST (geom);
alter table functioneel_gebied add constraint functioneel_gebied_pk primary key(identif);
comment on table functioneel_gebied is 'RSGB class FUNCTIONEEL GEBIED';
comment on column functioneel_gebied.identif is '[PK] NEN3610ID - Identificatie functioneel gebied';
comment on column functioneel_gebied.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid functioneel gebied';
comment on column functioneel_gebied.geom is 'GM_Surface - Geometrie functioneel gebied';
comment on column functioneel_gebied.naam is 'AN40 - Naam functioneel gebied';
comment on column functioneel_gebied.relve_hoogteligging is 'N1 - Relatieve hoogteligging functioneel gebied';
comment on column functioneel_gebied.status is 'A8 - Status functioneel gebied';
comment on column functioneel_gebied.type is '[geen RSGB type] - Type functioneel gebied';

create table gebouwd_obj(
	dat_beg_geldh character varying(19),
	sc_identif character varying(16),
	clazz character varying(255),
	bouwk_best_act character varying(255),
	bruto_inhoud decimal(6,0),
	datum_einde_geldh character varying(19),
	inwwijze_oppervlakte character varying(24),
	oppervlakte_obj decimal(6,0),
	status_voortgang_bouw integer
);
select addgeometrycolumn('gebouwd_obj', 'vlakgeom', 28992, 'MULTIPOLYGON', 2);
create index gebouwd_obj_vlakgeom_idx on gebouwd_obj USING GIST (vlakgeom);
select addgeometrycolumn('gebouwd_obj', 'puntgeom', 28992, 'POINT', 2);
create index gebouwd_obj_puntgeom_idx on gebouwd_obj USING GIST (puntgeom);
alter table gebouwd_obj add constraint gebouwd_obj_pk primary key(sc_identif);
comment on table gebouwd_obj is 'RSGB class GEBOUWD OBJECT. Subclass van: BENOEMD OBJECT. Directe superclass van: OVERIG GEBOUWD OBJECT, VERBLIJFSOBJECT';
comment on column gebouwd_obj.sc_identif is '[PK] AN16, FK naar benoemd_obj.identif - Benoemd object identificatie';
comment on column gebouwd_obj.clazz is 'Aanduiding subclass';
comment on column gebouwd_obj.bouwk_best_act is '[Enumeratie] - Bouwkundige bestemming actueel';
comment on column gebouwd_obj.bruto_inhoud is 'N6 - Bruto inhoud';
comment on column gebouwd_obj.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid gebouwd object';
comment on column gebouwd_obj.vlakgeom is 'GM_Surface - Gebouwd object vlakgeometrie';
comment on column gebouwd_obj.puntgeom is 'GM_Point - Gebouwd objectpuntgeometrie';
comment on column gebouwd_obj.inwwijze_oppervlakte is 'AN24 - Inwinningswijze oppervlakte';
comment on column gebouwd_obj.oppervlakte_obj is 'N6 - Oppervlakte (verblijfs)object';
comment on column gebouwd_obj.status_voortgang_bouw is 'long - Status voortgang bouw';

create table gebouwinstallatie(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type character varying(40)
);
select addgeometrycolumn('gebouwinstallatie', 'geom', 28992, 'MULTIPOLYGON', 2);
create index gebouwinstallatie_geom_idx on gebouwinstallatie USING GIST (geom);
alter table gebouwinstallatie add constraint gebouwinstallatie_pk primary key(identif);
comment on table gebouwinstallatie is 'RSGB class GEBOUWINSTALLATIE';
comment on column gebouwinstallatie.identif is '[PK] NEN3610ID - Identificatie gebouwinstallatie';
comment on column gebouwinstallatie.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid gebouwinstallatie';
comment on column gebouwinstallatie.geom is 'GM_Surface - Geometrie gebouwinstallatie';
comment on column gebouwinstallatie.relve_hoogteligging is 'N1 - Relatieve hoogteligging gebouwinstallatie';
comment on column gebouwinstallatie.status is 'A8 - Status gebouwinstallatie';
comment on column gebouwinstallatie.type is 'AN40 - Type gebouwinstallatie';

create table gemeente(
	dat_beg_geldh character varying(19),
	code decimal(4,0),
	datum_einde_geldh character varying(19),
	naam character varying(40),
	naam_nen character varying(24)
);
select addgeometrycolumn('gemeente', 'geom', 28992, 'MULTIPOLYGON', 2);
create index gemeente_geom_idx on gemeente USING GIST (geom);
alter table gemeente add constraint gemeente_pk primary key(code);
comment on table gemeente is 'RSGB class GEMEENTE';
comment on column gemeente.code is '[PK] N4 - Gemeentecode';
comment on column gemeente.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid gemeente';
comment on column gemeente.geom is 'GM_Surface - Gemeentegeometrie';
comment on column gemeente.naam is 'AN40 - Gemeentenaam';
comment on column gemeente.naam_nen is 'AN24 - Gemeentenaam NEN';

create table gem_openb_rmte(
	dat_beg_geldh character varying(19),
	identifcode character varying(16),
	datum_einde_geldh character varying(19),
	indic_geconst_openb_rmte character varying(1),
	naam_openb_rmte character varying(80),
	status_openb_rmte character varying(80),
	straatnaam character varying(24),
	type_openb_rmte character varying(40),
	fk_7gem_code decimal(4,0)
);
alter table gem_openb_rmte add constraint gem_openb_rmte_pk primary key(identifcode);
comment on table gem_openb_rmte is 'RSGB class GEMEENTELIJKE OPENBARE RUIMTE';
comment on column gem_openb_rmte.identifcode is '[PK] AN16 - Identificatiecode gemeentelijke openbare ruimte';
comment on column gem_openb_rmte.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid gemeentelijke openbare ruimte';
comment on column gem_openb_rmte.indic_geconst_openb_rmte is 'AN1 - Indicatie geconstateerde openbare ruimte';
comment on column gem_openb_rmte.naam_openb_rmte is 'AN80 - Naam openbare ruimte';
comment on column gem_openb_rmte.status_openb_rmte is 'AN80 - Status openbare ruimte';
comment on column gem_openb_rmte.straatnaam is 'AN24 - Straatnaam';
comment on column gem_openb_rmte.type_openb_rmte is 'AN40 - Type openbare ruimte';
comment on column gem_openb_rmte.fk_7gem_code is '[FK] N4, FK naar gemeente.code: "ligt in"';

create table huishouden(
	dat_beg_geldh character varying(19),
	nummer decimal(12,0),
	datum_einde_geldh character varying(19),
	grootte decimal(2,0),
	soort decimal(2,0),
	fk_4vbo_sc_identif character varying(16),
	fk_5lpl_sc_identif character varying(16),
	fk_7spl_sc_identif character varying(16)
);
alter table huishouden add constraint huishouden_pk primary key(nummer);
comment on table huishouden is 'RSGB class HUISHOUDEN';
comment on column huishouden.nummer is '[PK] N12 - Huishoudennummer';
comment on column huishouden.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid huishouden';
comment on column huishouden.grootte is 'N2 - Huishoudengrootte';
comment on column huishouden.soort is 'N2 - Huishoudensoort';
comment on column huishouden.fk_4vbo_sc_identif is '[FK] AN16, FK naar verblijfsobj.sc_identif (is FK naar superclass BENOEMD OBJECT): "is gehuisvest in"';
comment on column huishouden.fk_5lpl_sc_identif is '[FK] AN16, FK naar ligplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "is gehuisvest op"';
comment on column huishouden.fk_7spl_sc_identif is '[FK] AN16, FK naar standplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "is gehuisvest op"';

create table ingeschr_niet_nat_prs(
	sc_identif character varying(32),
	typering character varying(35),
	ovrg_privaatr_rechtsvorm character varying(200),
	publiekrechtelijke_rechtsvorm character varying(40),
	rechtstoestand character varying(30),
	rechtsvorm character varying(50),
	statutaire_zetel character varying(40),
	fk_7aoa_identif character varying(16)
);
alter table ingeschr_niet_nat_prs add constraint ingeschr_niet_nat_prs_pk primary key(sc_identif);
comment on table ingeschr_niet_nat_prs is 'RSGB class INGESCHREVEN NIET-NATUURLIJK PERSOON. Subclass van: NIET-NATUURLIJK PERSOON -> PERSOON -> SUBJECT';
comment on column ingeschr_niet_nat_prs.sc_identif is '[PK] AN32, FK naar niet_nat_prs.sc_identif - Identificatie';
comment on column ingeschr_niet_nat_prs.typering is 'AN35 - Ingeschreven niet-natuurlijk persoon typering';
comment on column ingeschr_niet_nat_prs.ovrg_privaatr_rechtsvorm is 'AN200 - Overige privaatrechtelijk rechtsvorm';
comment on column ingeschr_niet_nat_prs.publiekrechtelijke_rechtsvorm is 'AN40 - Publiekrechtelijke rechtsvorm';
comment on column ingeschr_niet_nat_prs.rechtstoestand is 'AN30 - Rechtstoestand';
comment on column ingeschr_niet_nat_prs.rechtsvorm is 'AN50 - Rechtsvorm';
comment on column ingeschr_niet_nat_prs.statutaire_zetel is 'AN40 - Statutaire zetel';
comment on column ingeschr_niet_nat_prs.fk_7aoa_identif is '[FK] AN16, FK naar addresseerb_obj_aand.identif: "heeft als bezoekadres"';

create table ingeschr_nat_prs(
	sc_identif character varying(32),
	clazz character varying(255),
	a_nummer decimal(10,0),
	btnlnds_rsdoc decimal(1,0),
	burgerlijke_staat decimal(1,0),
	dat_beg_geldh_verblijfpl character varying(19),
	datum_inschrijving_in_gemeente character varying(19),
	datum_opschorting_bijhouding character varying(19),
	datum_verkr_nation character varying(19),
	datum_verlies_nation character varying(19),
	datum_vertrek_uit_nederland character varying(19),
	datum_vestg_in_nederland character varying(19),
	gemeente_van_inschrijving decimal(4,0),
	handelingsbekwaam character varying(3),
	indic_geheim decimal(1,0),
	rechtstoestand character varying(22),
	reden_opschorting_bijhouding character varying(1),
	signalering_rsdoc decimal(1,0),
	fk_26lpl_sc_identif character varying(16),
	fk_27nra_sc_identif character varying(16),
	fk_28wpl_identif character varying(4),
	fk_29spl_sc_identif character varying(16),
	fk_30vbo_sc_identif character varying(16),
	fk_1rsd_nummer character varying(9),
	gb_geboortedatum decimal(8,0),
	fk_gb_lnd_code_iso character varying(2),
	gb_geboorteplaats character varying(40),
	nt_aand_bijzonder_nlschap character varying(1),
	fk_nt_nat_code decimal(4,0),
	nt_reden_verkr_nlse_nation decimal(3,0),
	nt_reden_verlies_nlse_nation decimal(3,0),
	fk_ol_lnd_code_iso character varying(2),
	ol_overlijdensdatum decimal(8,0),
	ol_overlijdensplaats character varying(40),
	va_adresherkomst character varying(1),
	va_loc_beschrijving character varying(255),
	fk_va_3_vbo_sc_identif character varying(16),
	fk_va_4_spl_sc_identif character varying(16),
	fk_va_5_nra_sc_identif character varying(16),
	fk_va_6_wpl_identif character varying(4),
	fk_va_7_lpl_sc_identif character varying(16),
	fk_3nat_code decimal(4,0),
	fk_16lnd_code_iso character varying(2),
	fk_17lnd_code_iso character varying(2)
);
alter table ingeschr_nat_prs add constraint ingeschr_nat_prs_pk primary key(sc_identif);
comment on table ingeschr_nat_prs is 'RSGB class INGESCHREVEN NATUURLIJK PERSOON. Subclass van: NATUURLIJK PERSOON -> PERSOON -> SUBJECT. Directe superclass van: INGEZETENE, NIET-INGEZETENE';
comment on column ingeschr_nat_prs.sc_identif is '[PK] AN32, FK naar nat_prs.sc_identif - Identificatie';
comment on column ingeschr_nat_prs.clazz is 'Aanduiding subclass';
comment on column ingeschr_nat_prs.a_nummer is 'N10 - A-nummer';
comment on column ingeschr_nat_prs.btnlnds_rsdoc is 'N1 - Buitenlands reisdocument';
comment on column ingeschr_nat_prs.burgerlijke_staat is 'N1 - Burgerlijke staat';
comment on column ingeschr_nat_prs.dat_beg_geldh_verblijfpl is 'OnvolledigeDatum - Datum begin geldigheid verblijfplaats';
comment on column ingeschr_nat_prs.datum_inschrijving_in_gemeente is 'OnvolledigeDatum - Datum inschrijving in gemeente';
comment on column ingeschr_nat_prs.datum_opschorting_bijhouding is 'OnvolledigeDatum - Datum opschorting bijhouding';
comment on column ingeschr_nat_prs.datum_verkr_nation is 'OnvolledigeDatum - Datum verkrijging nationaliteit';
comment on column ingeschr_nat_prs.datum_verlies_nation is 'OnvolledigeDatum - Datum verlies nationaliteit';
comment on column ingeschr_nat_prs.datum_vertrek_uit_nederland is 'OnvolledigeDatum - Datum vertrek uit Nederland';
comment on column ingeschr_nat_prs.datum_vestg_in_nederland is 'OnvolledigeDatum - Datum vestiging in Nederland';
comment on column ingeschr_nat_prs.gemeente_van_inschrijving is 'N4 - Gemeente van inschrijving';
comment on column ingeschr_nat_prs.handelingsbekwaam is 'AN3 - Handelingsbekwaam';
comment on column ingeschr_nat_prs.indic_geheim is 'N1 - Indicatie geheim';
comment on column ingeschr_nat_prs.rechtstoestand is 'AN22 - Rechtstoestand';
comment on column ingeschr_nat_prs.reden_opschorting_bijhouding is 'AN1 - Reden opschorting bijhouding';
comment on column ingeschr_nat_prs.signalering_rsdoc is 'N1 - Signalering reisdocument';
comment on column ingeschr_nat_prs.fk_26lpl_sc_identif is '[FK] AN16, FK naar ligplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "verblijft op"';
comment on column ingeschr_nat_prs.fk_27nra_sc_identif is '[FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "is ingeschreven op"';
comment on column ingeschr_nat_prs.fk_28wpl_identif is '[FK] AN4, FK naar wnplts.identif: "verblijft op locatie in"';
comment on column ingeschr_nat_prs.fk_29spl_sc_identif is '[FK] AN16, FK naar standplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "verblijft op"';
comment on column ingeschr_nat_prs.fk_30vbo_sc_identif is '[FK] AN16, FK naar verblijfsobj.sc_identif (is FK naar superclass BENOEMD OBJECT): "verblijft in"';
comment on column ingeschr_nat_prs.fk_1rsd_nummer is '[FK] AN9, FK naar rsdoc.nummer';
comment on column ingeschr_nat_prs.gb_geboortedatum is 'Groepsattribuut Geboorte INGESCHREVEN NATUURLIJK PERSOON.Geboortedatum - Geboortedatum';
comment on column ingeschr_nat_prs.fk_gb_lnd_code_iso is '[FK] A2, FK naar land.code_iso: "Groepsattribuut referentielijst Geboorteland"';
comment on column ingeschr_nat_prs.gb_geboorteplaats is 'Groepsattribuut Geboorte INGESCHREVEN NATUURLIJK PERSOON.Geboorteplaats - Geboorteplaats';
comment on column ingeschr_nat_prs.nt_aand_bijzonder_nlschap is 'Groepsattribuut Nederlandse nationaliteit INGESCHREVEN NATUURLIJK PERSOON.Aanduiding bijzonder Nederlanderschap - Aanduiding bijzonder Nederlanderschap';
comment on column ingeschr_nat_prs.fk_nt_nat_code is '[FK] N4, FK naar nation.code: "Groepsattribuut referentielijst Nationaliteit"';
comment on column ingeschr_nat_prs.nt_reden_verkr_nlse_nation is 'Groepsattribuut Nederlandse nationaliteit INGESCHREVEN NATUURLIJK PERSOON.Reden verkrijging Nederlandse nationaliteit - Reden verkrijging Nederlandse nationaliteit';
comment on column ingeschr_nat_prs.nt_reden_verlies_nlse_nation is 'Groepsattribuut Nederlandse nationaliteit INGESCHREVEN NATUURLIJK PERSOON.Reden verlies Nederlandse nationaliteit - Reden verlies Nederlandse nationaliteit';
comment on column ingeschr_nat_prs.fk_ol_lnd_code_iso is '[FK] A2, FK naar land.code_iso: "Groepsattribuut referentielijst Land overlijden"';
comment on column ingeschr_nat_prs.ol_overlijdensdatum is 'Groepsattribuut Overlijden INGESCHREVEN NATUURLIJK PERSOON.Overlijdensdatum - Overlijdensdatum';
comment on column ingeschr_nat_prs.ol_overlijdensplaats is 'Groepsattribuut Overlijden INGESCHREVEN NATUURLIJK PERSOON.Overlijdensplaats - Overlijdensplaats';
comment on column ingeschr_nat_prs.va_adresherkomst is 'Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.Adresherkomst - Adresherkomst';
comment on column ingeschr_nat_prs.va_loc_beschrijving is 'Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.Locatie beschrijving - Locatie beschrijving';
comment on column ingeschr_nat_prs.fk_va_3_vbo_sc_identif is '[FK] AN16, FK naar verblijfsobj.sc_identif (is FK naar superclass BENOEMD OBJECT): "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.verblijfsobject"';
comment on column ingeschr_nat_prs.fk_va_4_spl_sc_identif is '[FK] AN16, FK naar standplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.standplaats"';
comment on column ingeschr_nat_prs.fk_va_5_nra_sc_identif is '[FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.nummeraanduiding"';
comment on column ingeschr_nat_prs.fk_va_6_wpl_identif is '[FK] AN4, FK naar wnplts.identif: "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.woonplaats"';
comment on column ingeschr_nat_prs.fk_va_7_lpl_sc_identif is '[FK] AN16, FK naar ligplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "Groepsattribuut Verblijfadres INGESCHREVEN NATUURLIJK PERSOON.ligplaats"';
comment on column ingeschr_nat_prs.fk_3nat_code is '[FK] N4, FK naar nation.code: "Referentielijst INGESCHREVEN NATUURLIJK PERSOON.Buitenlandse nationaliteit"';
comment on column ingeschr_nat_prs.fk_16lnd_code_iso is '[FK] A2, FK naar land.code_iso: "Referentielijst INGESCHREVEN NATUURLIJK PERSOON.Land vanwaar ingeschreven"';
comment on column ingeschr_nat_prs.fk_17lnd_code_iso is '[FK] A2, FK naar land.code_iso: "Referentielijst INGESCHREVEN NATUURLIJK PERSOON.Land waarnaar vertrokken"';

create table ingezetene(
	sc_identif character varying(32),
	datum_verkr_verblijfstitel character varying(19),
	datum_verlies_verblijfstitel character varying(19),
	indic_blokkering character varying(1),
	indic_curateleregister decimal(1,0),
	indic_gezag_minderjarige character varying(2),
	fk_8vbt_aand decimal(2,0),
	ek_aand_euro_kiesr decimal(1,0),
	ek_verw_eindd_uitsl_euro_kiesr decimal(8,0),
	uk_aand_uitgesloten_kiesr character varying(1),
	uk_verw_eindd_uitsl_kiesr decimal(8,0)
);
alter table ingezetene add constraint ingezetene_pk primary key(sc_identif);
comment on table ingezetene is 'RSGB class INGEZETENE. Subclass van: INGESCHREVEN NATUURLIJK PERSOON -> NATUURLIJK PERSOON -> PERSOON -> SUBJECT';
comment on column ingezetene.sc_identif is '[PK] AN32, FK naar ingeschr_nat_prs.sc_identif - Identificatie';
comment on column ingezetene.datum_verkr_verblijfstitel is 'OnvolledigeDatum - Datum verkrijging verblijfstitel';
comment on column ingezetene.datum_verlies_verblijfstitel is 'OnvolledigeDatum - Datum verlies verblijfstitel';
comment on column ingezetene.indic_blokkering is 'AN1 - Indicatie blokkering';
comment on column ingezetene.indic_curateleregister is 'N1 - Indicatie curateleregister';
comment on column ingezetene.indic_gezag_minderjarige is 'AN2 - Indicatie gezag minderjarige';
comment on column ingezetene.fk_8vbt_aand is '[FK] N2, FK naar verblijfstitel.aand: "heeft"';
comment on column ingezetene.ek_aand_euro_kiesr is 'Groepsattribuut Europees kiesrecht INGEZETENE.Aanduiding Europees kiesrecht - Aanduiding Europees kiesrecht';
comment on column ingezetene.ek_verw_eindd_uitsl_euro_kiesr is 'Groepsattribuut Europees kiesrecht INGEZETENE.Verwachte einddatum uitsluiting Europees kiesrecht - Verwachte einddatum uitsluiting Europees kiesrecht';
comment on column ingezetene.uk_aand_uitgesloten_kiesr is 'Groepsattribuut Uitsluiting kiesrecht INGEZETENE.Aanduiding uitgesloten kiesrecht - Aanduiding uitgesloten kiesrecht';
comment on column ingezetene.uk_verw_eindd_uitsl_kiesr is 'Groepsattribuut Uitsluiting kiesrecht INGEZETENE.Verwachte einddatum uitsluiting kiesrecht - Verwachte einddatum uitsluiting kiesrecht';

create table inrichtingselement(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	typering character varying(40),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type character varying(30)
);
select addgeometrycolumn('inrichtingselement', 'geom', 28992, 'GEOMETRY', 2);
create index inrichtingselement_geom_idx on inrichtingselement USING GIST (geom);
alter table inrichtingselement add constraint inrichtingselement_pk primary key(identif);
comment on table inrichtingselement is 'RSGB class INRICHTINGSELEMENT';
comment on column inrichtingselement.identif is '[PK] NEN3610ID - Identificatie inrichtingselement';
comment on column inrichtingselement.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid inrichtingselement';
comment on column inrichtingselement.geom is 'PuntLijnVlak - Geometrie inrichtingselement';
comment on column inrichtingselement.typering is 'AN40 - Inrichtingselementtypering';
comment on column inrichtingselement.relve_hoogteligging is 'N1 - Relatieve hoogteligging inrichtingselement';
comment on column inrichtingselement.status is 'AN8 - Status inrichtingselement';
comment on column inrichtingselement.type is 'AN30 - Type inrichtingselement';

create table kad_perceel(
	sc_kad_identif decimal(15,0),
	aand_soort_grootte char(1),
	grootte_perceel decimal(8,0),
	omschr_deelperceel character varying(1120),
	fk_7kdp_sc_kad_identif decimal(15,0),
	ka_deelperceelnummer character varying(4),
	ka_kad_gemeentecode character varying(5),
	ka_perceelnummer character varying(5),
	ka_sectie character varying(255)
);
select addgeometrycolumn('kad_perceel', 'begrenzing_perceel', 28992, 'MULTIPOLYGON', 2);
create index kad_perceel_begrenzing_perceel_idx on kad_perceel USING GIST (begrenzing_perceel);
select addgeometrycolumn('kad_perceel', 'plaatscoordinaten_perceel', 28992, 'POINT', 2);
create index kad_perceel_plaatscoordinaten_perceel_idx on kad_perceel USING GIST (plaatscoordinaten_perceel);
alter table kad_perceel add constraint kad_perceel_pk primary key(sc_kad_identif);
comment on table kad_perceel is 'RSGB class KADASTRAAL PERCEEL. Subclass van: KADASTRALE ONROERENDE ZAAK';
comment on column kad_perceel.sc_kad_identif is '[PK] N15, FK naar kad_onrrnd_zk.kad_identif - Kadastrale identificatie';
comment on column kad_perceel.aand_soort_grootte is 'boolean - Aanduiding soort grootte';
comment on column kad_perceel.begrenzing_perceel is 'GM_Surface - Begrenzing perceel';
comment on column kad_perceel.grootte_perceel is 'N8 - Grootte perceel';
comment on column kad_perceel.omschr_deelperceel is 'AN1120 - Omschrijving deelperceel';
comment on column kad_perceel.plaatscoordinaten_perceel is 'GM_Point - Plaatscoördinaten perceel';
comment on column kad_perceel.fk_7kdp_sc_kad_identif is '[FK] N15, FK naar kad_perceel.sc_kad_identif (is FK naar superclass KADASTRALE ONROERENDE ZAAK): "ligt binnen"';
comment on column kad_perceel.ka_deelperceelnummer is 'Groepsattribuut Kadastrale aanduiding KADASTRAAL PERCEEL.Deelperceelnummer - Deelperceelnummer';
comment on column kad_perceel.ka_kad_gemeentecode is 'Groepsattribuut Kadastrale aanduiding KADASTRAAL PERCEEL.Kadastrale gemeentecode - Kadastrale gemeentecode';
comment on column kad_perceel.ka_perceelnummer is 'Groepsattribuut Kadastrale aanduiding KADASTRAAL PERCEEL.Perceelnummer - Perceelnummer';
comment on column kad_perceel.ka_sectie is 'Groepsattribuut Kadastrale aanduiding KADASTRAAL PERCEEL.Sectie - Sectie';

create table kad_gemeente(
	code character varying(5),
	indic_vervallen char(1),
	naam character varying(40)
);
alter table kad_gemeente add constraint kad_gemeente_pk primary key(code);
comment on table kad_gemeente is 'RSGB class KADASTRALE GEMEENTE';
comment on column kad_gemeente.code is '[PK] AN5 - Kadastrale gemeentecode';
comment on column kad_gemeente.indic_vervallen is 'boolean - Indicatie vervallen';
comment on column kad_gemeente.naam is 'AN40 - naam';

create table kad_onrrnd_zk(
	dat_beg_geldh character varying(19),
	kad_identif decimal(15,0),
	clazz character varying(255),
	datum_einde_geldh character varying(19),
	typering character varying(1),
	fk_7kdg_code character varying(5),
	fk_10pes_sc_identif character varying(32),
	cu_aard_bebouwing character varying(255),
	cu_aard_cultuur_onbebouwd character varying(65),
	cu_meer_culturen char(1),
	ks_aard_bedrag character varying(255),
	ks_bedrag decimal(9,0),
	ks_koopjaar integer,
	ks_meer_onroerendgoed char(1),
	ks_transactiedatum timestamp,
	ks_valutasoort character varying(255),
	lr_aand_aard_liproject character varying(1),
	lr_aard_bedrag character varying(255),
	lr_bedrag character varying(255),
	lr_eindjaar integer,
	lr_valutasoort character varying(3),
	lo_cultuur_bebouwd character varying(65),
	lo_loc__omschr character varying(40)
);
alter table kad_onrrnd_zk add constraint kad_onrrnd_zk_pk primary key(kad_identif);
comment on table kad_onrrnd_zk is 'RSGB class KADASTRALE ONROERENDE ZAAK. Directe superclass van: APPARTEMENTSRECHT, KADASTRAAL PERCEEL';
comment on column kad_onrrnd_zk.kad_identif is '[PK] N15 - Kadastrale identificatie';
comment on column kad_onrrnd_zk.clazz is 'Aanduiding subclass';
comment on column kad_onrrnd_zk.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid kadastrale onroerende zaak';
comment on column kad_onrrnd_zk.typering is 'AN1 - Kadastrale onroerende zaak typering';
comment on column kad_onrrnd_zk.fk_7kdg_code is '[FK] AN5, FK naar kad_gemeente.code: "ligt in"';
comment on column kad_onrrnd_zk.fk_10pes_sc_identif is '[FK] AN32, FK naar prs.sc_identif (is FK naar superclass SUBJECT): "heeft als voornaamste zakelijk gerechtigde"';
comment on column kad_onrrnd_zk.cu_aard_bebouwing is 'Groepsattribuut Cultuur onbebouwd KADASTRALE ONROERENDE ZAAK.Aard bebouwing - Aard bebouwing';
comment on column kad_onrrnd_zk.cu_aard_cultuur_onbebouwd is 'Groepsattribuut Cultuur onbebouwd KADASTRALE ONROERENDE ZAAK.Aard cultuur onbebouwd - Aard cultuur onbebouwd';
comment on column kad_onrrnd_zk.cu_meer_culturen is 'Groepsattribuut Cultuur onbebouwd KADASTRALE ONROERENDE ZAAK.Meer culturen - Meer culturen';
comment on column kad_onrrnd_zk.ks_aard_bedrag is 'Groepsattribuut Koopsom KADASTRALE ONROERENDE ZAAK.Aard bedrag - Aard bedrag';
comment on column kad_onrrnd_zk.ks_bedrag is 'Groepsattribuut Koopsom KADASTRALE ONROERENDE ZAAK.Bedrag - Bedrag';
comment on column kad_onrrnd_zk.ks_koopjaar is 'Groepsattribuut Koopsom KADASTRALE ONROERENDE ZAAK.Koopjaar - Koopjaar';
comment on column kad_onrrnd_zk.ks_meer_onroerendgoed is 'Groepsattribuut Koopsom KADASTRALE ONROERENDE ZAAK.Meer onroerendgoed - Meer onroerendgoed';
comment on column kad_onrrnd_zk.ks_transactiedatum is 'Groepsattribuut Koopsom KADASTRALE ONROERENDE ZAAK.Transactiedatum - Transactiedatum';
comment on column kad_onrrnd_zk.ks_valutasoort is 'Groepsattribuut Koopsom KADASTRALE ONROERENDE ZAAK.Valutasoort - Valutasoort';
comment on column kad_onrrnd_zk.lr_aand_aard_liproject is 'Groepsattribuut Landinrichtingsrente KADASTRALE ONROERENDE ZAAK.Aanduiding aard LIproject - Aanduiding aard LIproject';
comment on column kad_onrrnd_zk.lr_aard_bedrag is 'Groepsattribuut Landinrichtingsrente KADASTRALE ONROERENDE ZAAK.Aard bedrag - Aard bedrag';
comment on column kad_onrrnd_zk.lr_bedrag is 'Groepsattribuut Landinrichtingsrente KADASTRALE ONROERENDE ZAAK.Bedrag - Bedrag';
comment on column kad_onrrnd_zk.lr_eindjaar is 'Groepsattribuut Landinrichtingsrente KADASTRALE ONROERENDE ZAAK.Eindjaar - Eindjaar';
comment on column kad_onrrnd_zk.lr_valutasoort is 'Groepsattribuut Landinrichtingsrente KADASTRALE ONROERENDE ZAAK.Valutasoort - Valutasoort';
comment on column kad_onrrnd_zk.lo_cultuur_bebouwd is 'Groepsattribuut Locatie onroerende zaak KADASTRALE ONROERENDE ZAAK.Cultuur bebouwd - Cultuur bebouwd';
comment on column kad_onrrnd_zk.lo_loc__omschr is 'Groepsattribuut Locatie onroerende zaak KADASTRALE ONROERENDE ZAAK.Locatie- omschrijving - Locatie- omschrijving';

create table kad_onrrnd_zk_aantek(
	begindatum_aantek_kad_obj character varying(19),
	kadaster_identif_aantek character varying(255),
	aard_aantek_kad_obj character varying(255),
	beschrijving_aantek_kad_obj character varying(124),
	eindd_aantek_kad_obj character varying(19),
	fk_4koz_kad_identif decimal(15,0),
	fk_5pes_sc_identif character varying(32)
);
alter table kad_onrrnd_zk_aantek add constraint kad_onrrnd_zk_aantek_pk primary key(kadaster_identif_aantek);
comment on table kad_onrrnd_zk_aantek is 'RSGB class KADASTRALE ONROERENDE ZAAK AANTEKENING';
comment on column kad_onrrnd_zk_aantek.kadaster_identif_aantek is '[PK] AN255 - Kadaster identificatie aantekening';
comment on column kad_onrrnd_zk_aantek.aard_aantek_kad_obj is '[Enumeratie] - Aard aantekening kadastraal object';
comment on column kad_onrrnd_zk_aantek.beschrijving_aantek_kad_obj is 'AN124 - Beschrijving aantekening kadastraal object';
comment on column kad_onrrnd_zk_aantek.eindd_aantek_kad_obj is 'OnvolledigeDatum - Einddatum aantekening kadastraal object';
comment on column kad_onrrnd_zk_aantek.fk_4koz_kad_identif is '[FK] N15, FK naar kad_onrrnd_zk.kad_identif: "behoort bij"';
comment on column kad_onrrnd_zk_aantek.fk_5pes_sc_identif is '[FK] AN32, FK naar prs.sc_identif (is FK naar superclass SUBJECT): "heeft betrokken"';

create table kunstwerkdeel(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	clazz character varying(255),
	datum_einde_geldh character varying(19),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type_kunstwerk character varying(40)
);
select addgeometrycolumn('kunstwerkdeel', 'geom', 28992, 'GEOMETRY', 2);
create index kunstwerkdeel_geom_idx on kunstwerkdeel USING GIST (geom);
alter table kunstwerkdeel add constraint kunstwerkdeel_pk primary key(identif);
comment on table kunstwerkdeel is 'RSGB class KUNSTWERKDEEL. Directe superclass van: BRUGCONSTRUCTIE ELEMENT';
comment on column kunstwerkdeel.identif is '[PK] NEN3610ID - Identificatie kunstwerkdeel';
comment on column kunstwerkdeel.clazz is 'Aanduiding subclass';
comment on column kunstwerkdeel.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid kunstwerkdeel';
comment on column kunstwerkdeel.geom is 'LijnVlak - Geometrie kunstwerkdeel';
comment on column kunstwerkdeel.relve_hoogteligging is 'N1 - Relatieve hoogteligging kunstwerkdeel';
comment on column kunstwerkdeel.status is 'A8 - Status kunstwerkdeel';
comment on column kunstwerkdeel.type_kunstwerk is 'AN40 - Type kunstwerk';

create table ligplaats(
	sc_identif character varying(16),
	indic_geconst character varying(1),
	status character varying(80),
	fk_4nra_sc_identif character varying(16)
);
alter table ligplaats add constraint ligplaats_pk primary key(sc_identif);
comment on table ligplaats is 'RSGB class LIGPLAATS. Subclass van: BENOEMD TERREIN -> BENOEMD OBJECT';
comment on column ligplaats.sc_identif is '[PK] AN16, FK naar benoemd_terrein.sc_identif - Benoemd object identificatie';
comment on column ligplaats.indic_geconst is 'AN1 - Indicatie geconstateerde ligplaats';
comment on column ligplaats.status is 'AN80 - Ligplaatsstatus';
comment on column ligplaats.fk_4nra_sc_identif is '[FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "heeft als hoofdadres"';

create table maatschapp_activiteit(
	kvk_nummer decimal(8,0),
	datum_aanvang character varying(19),
	datum_einde_geldig character varying(19),
	fk_3ond_kvk_nummer decimal(8,0),
	fk_4pes_sc_identif character varying(32)
);
alter table maatschapp_activiteit add constraint maatschapp_activiteit_pk primary key(kvk_nummer);
comment on table maatschapp_activiteit is 'RSGB class MAATSCHAPPELIJKE ACTIVITEIT';
comment on column maatschapp_activiteit.kvk_nummer is '[PK] N8 - KvK-nummer';
comment on column maatschapp_activiteit.datum_aanvang is 'OnvolledigeDatum - Datum aanvang';
comment on column maatschapp_activiteit.datum_einde_geldig is 'OnvolledigeDatum - Datum einde geldig (beeindiging)';
comment on column maatschapp_activiteit.fk_3ond_kvk_nummer is '[FK] N8, FK naar ondrnmng.kvk_nummer: "manifesteert zich als"';
comment on column maatschapp_activiteit.fk_4pes_sc_identif is '[FK] AN32, FK naar prs.sc_identif (is FK naar superclass SUBJECT): "heeft als eigenaar"';

create table nat_prs(
	sc_identif character varying(32),
	clazz character varying(255),
	aand_naamgebruik character varying(1),
	geslachtsaand character varying(1),
	nm_adellijke_titel_predikaat character varying(10),
	nm_geslachtsnaam character varying(200),
	nm_voornamen character varying(200),
	nm_voorvoegsel_geslachtsnaam character varying(10),
	na_aanhef_aanschrijving character varying(50),
	na_geslachtsnaam_aanschrijving character varying(200),
	na_voorletters_aanschrijving character varying(20),
	na_voornamen_aanschrijving character varying(200),
	fk_2acd_code character varying(3)
);
alter table nat_prs add constraint nat_prs_pk primary key(sc_identif);
comment on table nat_prs is 'RSGB class NATUURLIJK PERSOON. Subclass van: PERSOON -> SUBJECT. Directe superclass van: ANDER NATUURLIJK PERSOON, INGESCHREVEN NATUURLIJK PERSOON';
comment on column nat_prs.sc_identif is '[PK] AN32, FK naar prs.sc_identif - Identificatie';
comment on column nat_prs.clazz is 'Aanduiding subclass';
comment on column nat_prs.aand_naamgebruik is 'AN1 - Aanduiding naamgebruik';
comment on column nat_prs.geslachtsaand is 'AN1 - Geslachtsaanduiding';
comment on column nat_prs.nm_adellijke_titel_predikaat is 'Groepsattribuut Naam NATUURLIJK PERSOON.Adellijke titel/ predikaat - Adellijke titel/ predikaat';
comment on column nat_prs.nm_geslachtsnaam is 'Groepsattribuut Naam NATUURLIJK PERSOON.Geslachtsnaam - Geslachtsnaam';
comment on column nat_prs.nm_voornamen is 'Groepsattribuut Naam NATUURLIJK PERSOON.Voornamen - Voornamen';
comment on column nat_prs.nm_voorvoegsel_geslachtsnaam is 'Groepsattribuut Naam NATUURLIJK PERSOON.Voorvoegsel geslachtsnaam - Voorvoegsel geslachtsnaam';
comment on column nat_prs.na_aanhef_aanschrijving is 'Groepsattribuut Naam aanschrijving NATUURLIJK PERSOON.Aanhef aanschrijving - Aanhef aanschrijving';
comment on column nat_prs.na_geslachtsnaam_aanschrijving is 'Groepsattribuut Naam aanschrijving NATUURLIJK PERSOON.Geslachtsnaam aanschrijving - Geslachtsnaam aanschrijving';
comment on column nat_prs.na_voorletters_aanschrijving is 'Groepsattribuut Naam aanschrijving NATUURLIJK PERSOON.Voorletters aanschrijving - Voorletters aanschrijving';
comment on column nat_prs.na_voornamen_aanschrijving is 'Groepsattribuut Naam aanschrijving NATUURLIJK PERSOON.Voornamen aanschrijving - Voornamen aanschrijving';
comment on column nat_prs.fk_2acd_code is '[FK] AN3, FK naar academische_titel.code: "Referentielijst NATUURLIJK PERSOON.Academische titel"';

create table niet_ingezetene(
	sc_identif character varying(32)
);
alter table niet_ingezetene add constraint niet_ingezetene_pk primary key(sc_identif);
comment on table niet_ingezetene is 'RSGB class NIET-INGEZETENE. Subclass van: INGESCHREVEN NATUURLIJK PERSOON -> NATUURLIJK PERSOON -> PERSOON -> SUBJECT';
comment on column niet_ingezetene.sc_identif is '[PK] AN32, FK naar ingeschr_nat_prs.sc_identif - Identificatie';

create table niet_nat_prs(
	sc_identif character varying(32),
	clazz character varying(255),
	naam character varying(500),
	datum_aanvang character varying(19),
	datum_beeindiging character varying(19),
	verkorte_naam character varying(45)
);
alter table niet_nat_prs add constraint niet_nat_prs_pk primary key(sc_identif);
comment on table niet_nat_prs is 'RSGB class NIET-NATUURLIJK PERSOON. Subclass van: PERSOON -> SUBJECT. Directe superclass van: ANDER BUITENLANDS NIET-NATUURLIJK PERSOON, INGESCHREVEN NIET-NATUURLIJK PERSOON';
comment on column niet_nat_prs.sc_identif is '[PK] AN32, FK naar prs.sc_identif - Identificatie';
comment on column niet_nat_prs.clazz is 'Aanduiding subclass';
comment on column niet_nat_prs.naam is 'AN500 - (Statutaire) Naam';
comment on column niet_nat_prs.datum_aanvang is 'OnvolledigeDatum - Datum aanvang';
comment on column niet_nat_prs.datum_beeindiging is 'OnvolledigeDatum - Datum beeindiging';
comment on column niet_nat_prs.verkorte_naam is 'AN45 - Verkorte naam';

create table nummeraand(
	sc_identif character varying(16),
	indic_geconst character varying(1),
	indic_hoofdadres char(1),
	status character varying(80)
);
alter table nummeraand add constraint nummeraand_pk primary key(sc_identif);
comment on table nummeraand is 'RSGB class NUMMERAANDUIDING. Subclass van: ADRESSEERBAAR OBJECT AANDUIDING';
comment on column nummeraand.sc_identif is '[PK] AN16, FK naar addresseerb_obj_aand.identif - Identificatie adresseerbaar object aanduiding';
comment on column nummeraand.indic_geconst is 'AN1 - Indicatie geconstateerde nummeraanduiding';
comment on column nummeraand.indic_hoofdadres is 'boolean - Indicatie hoofdadres';
comment on column nummeraand.status is 'AN80 - Nummeraanduidingstatus';

create table onbegr_terr_dl(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	clazz character varying(255),
	datum_einde_geldh character varying(19),
	fysiek_voork_onbegr_terrein character varying(20),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	fk_8opr_identifcode character varying(16)
);
select addgeometrycolumn('onbegr_terr_dl', 'geom', 28992, 'MULTIPOLYGON', 2);
create index onbegr_terr_dl_geom_idx on onbegr_terr_dl USING GIST (geom);
select addgeometrycolumn('onbegr_terr_dl', 'kruinlijngeom', 28992, 'LINESTRING', 2);
create index onbegr_terr_dl_kruinlijngeom_idx on onbegr_terr_dl USING GIST (kruinlijngeom);
alter table onbegr_terr_dl add constraint onbegr_terr_dl_pk primary key(identif);
comment on table onbegr_terr_dl is 'RSGB class ONBEGROEID TERREINDEEL. Directe superclass van: ONBEGROEID TERREINVAKONDERDEEL';
comment on column onbegr_terr_dl.identif is '[PK] NEN3610ID - Identificatie onbegroeid terreindeel';
comment on column onbegr_terr_dl.clazz is 'Aanduiding subclass';
comment on column onbegr_terr_dl.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid onbegroeid terreindeel';
comment on column onbegr_terr_dl.fysiek_voork_onbegr_terrein is 'AN20 - Fysiek voorkomen onbegroeid terrein';
comment on column onbegr_terr_dl.geom is 'GM_Surface - Geometrie onbegroeid terreindeel';
comment on column onbegr_terr_dl.kruinlijngeom is 'GM_Curve - Kruinlijngeometrie onbegroeid terreindeel';
comment on column onbegr_terr_dl.relve_hoogteligging is 'N1 - Relatieve hoogteligging onbegroeid terreindeel';
comment on column onbegr_terr_dl.status is 'A8 - Status onbegroeid terreindeel';
comment on column onbegr_terr_dl.fk_8opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode: "maakt deel uit van"';

create table onbegr_terreinvakonderd(
	sc_identif character varying(255),
	fk_1obt_identif character varying(255)
);
alter table onbegr_terreinvakonderd add constraint onbegr_terreinvakonderd_pk primary key(sc_identif);
comment on table onbegr_terreinvakonderd is 'RSGB class ONBEGROEID TERREINVAKONDERDEEL. Subclass van: ONBEGROEID TERREINDEEL';
comment on column onbegr_terreinvakonderd.sc_identif is '[PK] NEN3610ID, FK naar onbegr_terr_dl.identif - Identificatie onbegroeid terreindeel';
comment on column onbegr_terreinvakonderd.fk_1obt_identif is '[FK] NEN3610ID, FK naar onbegr_terr_dl.identif';

create table ondrnmng(
	kvk_nummer decimal(8,0),
	datum_aanvang character varying(19),
	datum_einde character varying(19),
	fk_4mac_kvk_nummer decimal(8,0),
	fk_1ond_kvk_nummer decimal(8,0)
);
alter table ondrnmng add constraint ondrnmng_pk primary key(kvk_nummer);
comment on table ondrnmng is 'RSGB class ONDERNEMING';
comment on column ondrnmng.kvk_nummer is '[PK] N8 - KvK-nummer';
comment on column ondrnmng.datum_aanvang is 'OnvolledigeDatum - Datum aanvang';
comment on column ondrnmng.datum_einde is 'OnvolledigeDatum - Datum einde';
comment on column ondrnmng.fk_4mac_kvk_nummer is '[FK] N8, FK naar maatschapp_activiteit.kvk_nummer: "is voortgezet door"';
comment on column ondrnmng.fk_1ond_kvk_nummer is '[FK] N8, FK naar ondrnmng.kvk_nummer';

create table ondersteunend_wegdeel(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	functie character varying(25),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	fk_6opr_identifcode character varying(16)
);
select addgeometrycolumn('ondersteunend_wegdeel', 'geom', 28992, 'MULTIPOLYGON', 2);
create index ondersteunend_wegdeel_geom_idx on ondersteunend_wegdeel USING GIST (geom);
alter table ondersteunend_wegdeel add constraint ondersteunend_wegdeel_pk primary key(identif);
comment on table ondersteunend_wegdeel is 'RSGB class ONDERSTEUNEND WEGDEEL';
comment on column ondersteunend_wegdeel.identif is '[PK] NEN3610ID - Identificatie ondersteunend wegdeel';
comment on column ondersteunend_wegdeel.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid ondersteunend wegdeel';
comment on column ondersteunend_wegdeel.functie is 'AN25 - Functie ondersteunend wegdeel';
comment on column ondersteunend_wegdeel.geom is 'GM_Surface - Geometrie ondersteunend wegdeel';
comment on column ondersteunend_wegdeel.relve_hoogteligging is 'N1 - Relatieve hoogteligging ondersteunend wegdeel';
comment on column ondersteunend_wegdeel.status is 'A8 - Status ondersteunend wegdeel';
comment on column ondersteunend_wegdeel.fk_6opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode: "maakt deel uit van"';

create table openb_rmte(
	identifcode character varying(16),
	huisnrrange_on_even_nummers character varying(11),
	huisnrrange_even_nummers character varying(11),
	huisnrrange_oneven_nummers character varying(11),
	identif_bgtopr character varying(255)
);
alter table openb_rmte add constraint openb_rmte_pk primary key(identifcode);
comment on table openb_rmte is 'RSGB class OPENBARE RUIMTE';
comment on column openb_rmte.identifcode is '[PK] AN16 - Identificatiecode openbare ruimte';
comment on column openb_rmte.huisnrrange_on_even_nummers is 'AN11 (NNNNN-NNNNN) - Huisnummerrange even en oneven nummers';
comment on column openb_rmte.huisnrrange_even_nummers is 'AN11 (NNNNN-NNNNN) - Huisnummerrange even nummers';
comment on column openb_rmte.huisnrrange_oneven_nummers is 'AN11 (NNNNN-NNNNN) - Huisnummerrange oneven nummers';
comment on column openb_rmte.identif_bgtopr is 'NEN3610ID - Identificatie BGTOPR';

create table ovrg_addresseerb_obj_aand(
	sc_identif character varying(16)
);
alter table ovrg_addresseerb_obj_aand add constraint ovrg_addresseerb_obj_aand_pk primary key(sc_identif);
comment on table ovrg_addresseerb_obj_aand is 'RSGB class OVERIGE ADRESSEERBAAR OBJECT AANDUIDING. Subclass van: ADRESSEERBAAR OBJECT AANDUIDING';
comment on column ovrg_addresseerb_obj_aand.sc_identif is '[PK] AN16, FK naar addresseerb_obj_aand.identif - Identificatie adresseerbaar object aanduiding';

create table overig_bouwwerk(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type character varying(40)
);
select addgeometrycolumn('overig_bouwwerk', 'geom', 28992, 'MULTIPOLYGON', 2);
create index overig_bouwwerk_geom_idx on overig_bouwwerk USING GIST (geom);
alter table overig_bouwwerk add constraint overig_bouwwerk_pk primary key(identif);
comment on table overig_bouwwerk is 'RSGB class OVERIG BOUWWERK';
comment on column overig_bouwwerk.identif is '[PK] NEN3610ID - Identificatie overig bouwwerk';
comment on column overig_bouwwerk.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid overig bouwwerk';
comment on column overig_bouwwerk.geom is 'GM_Surface - Geometrie overig bouwwerk';
comment on column overig_bouwwerk.relve_hoogteligging is 'N1 - Relatieve hoogteligging overig bouwwerk';
comment on column overig_bouwwerk.status is 'A8 - Status overig bouwwerk';
comment on column overig_bouwwerk.type is 'AN40 - Type overig bouwwerk';

create table overig_gebouwd_obj(
	sc_identif character varying(16),
	bouwjaar decimal(4,0),
	loc_aand character varying(40),
	type character varying(40),
	fk_4oao_sc_identif character varying(16),
	fk_5nra_sc_identif character varying(16),
	fk_6opr_identifcode character varying(16)
);
alter table overig_gebouwd_obj add constraint overig_gebouwd_obj_pk primary key(sc_identif);
comment on table overig_gebouwd_obj is 'RSGB class OVERIG GEBOUWD OBJECT. Subclass van: GEBOUWD OBJECT -> BENOEMD OBJECT';
comment on column overig_gebouwd_obj.sc_identif is '[PK] AN16, FK naar gebouwd_obj.sc_identif - Benoemd object identificatie';
comment on column overig_gebouwd_obj.bouwjaar is 'N4 - Bouwjaar';
comment on column overig_gebouwd_obj.loc_aand is 'AN40 - Overig gebouwd object locatie-aanduiding';
comment on column overig_gebouwd_obj.type is 'AN40 - Overig gebouwd object type';
comment on column overig_gebouwd_obj.fk_4oao_sc_identif is '[FK] AN16, FK naar ovrg_addresseerb_obj_aand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "heeft als officieel adres"';
comment on column overig_gebouwd_obj.fk_5nra_sc_identif is '[FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "heeft locatie-adres i.c.m."';
comment on column overig_gebouwd_obj.fk_6opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode: "heeft straatadres i.c.m."';

create table ovrg_scheiding(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type character varying(40)
);
select addgeometrycolumn('ovrg_scheiding', 'geom', 28992, 'GEOMETRY', 2);
create index ovrg_scheiding_geom_idx on ovrg_scheiding USING GIST (geom);
alter table ovrg_scheiding add constraint ovrg_scheiding_pk primary key(identif);
comment on table ovrg_scheiding is 'RSGB class OVERIGE SCHEIDING';
comment on column ovrg_scheiding.identif is '[PK] NEN3610ID - Identificatie overige scheiding';
comment on column ovrg_scheiding.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid overige scheiding';
comment on column ovrg_scheiding.geom is 'LijnVlak - Geometrie overige scheiding';
comment on column ovrg_scheiding.relve_hoogteligging is 'N1 - Relatieve hoogteligging overige scheiding';
comment on column ovrg_scheiding.status is 'A8 - Status overige scheiding';
comment on column ovrg_scheiding.type is 'AN40 - Type overige scheiding';

create table overig_terrein(
	sc_identif character varying(16),
	fk_2oao_sc_identif character varying(16)
);
alter table overig_terrein add constraint overig_terrein_pk primary key(sc_identif);
comment on table overig_terrein is 'RSGB class OVERIG TERREIN. Subclass van: BENOEMD TERREIN -> BENOEMD OBJECT';
comment on column overig_terrein.sc_identif is '[PK] AN16, FK naar benoemd_terrein.sc_identif - Benoemd object identificatie';
comment on column overig_terrein.fk_2oao_sc_identif is '[FK] AN16, FK naar ovrg_addresseerb_obj_aand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "heeft als officieel adres"';

create table pand(
	dat_beg_geldh character varying(19),
	identif character varying(16),
	bruto_inhoud decimal(6,0),
	datum_einde_geldh character varying(19),
	hoogste_bouwlaag decimal(3,0),
	identif_bgtpnd character varying(255),
	indic_geconstateerd character varying(1),
	inwwijze_geom_bovenaanzicht character varying(24),
	inwwijze_geom_maaiveld character varying(24),
	laagste_bouwlaag decimal(3,0),
	oorspronkelijk_bouwjaar decimal(4,0),
	oppervlakte decimal(6,0),
	status character varying(80),
	relve_hoogteligging decimal(1,0),
	status_voortgang_bouw character varying(24)
);
select addgeometrycolumn('pand', 'geom_bovenaanzicht', 28992, 'MULTIPOLYGON', 2);
create index pand_geom_bovenaanzicht_idx on pand USING GIST (geom_bovenaanzicht);
select addgeometrycolumn('pand', 'geom_maaiveld', 28992, 'MULTIPOLYGON', 2);
create index pand_geom_maaiveld_idx on pand USING GIST (geom_maaiveld);
alter table pand add constraint pand_pk primary key(identif);
comment on table pand is 'RSGB class PAND';
comment on column pand.identif is '[PK] AN16 - Pandidentificatie';
comment on column pand.bruto_inhoud is 'N6 - Bruto inhoud pand';
comment on column pand.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid pand';
comment on column pand.hoogste_bouwlaag is 'N3 - Hoogste bouwlaag pand';
comment on column pand.identif_bgtpnd is 'NEN3610ID - Identificatie BGTPND';
comment on column pand.indic_geconstateerd is 'AN1 - Indicatie geconstateerd pand';
comment on column pand.inwwijze_geom_bovenaanzicht is 'AN24 - Inwinningswijze geometrie bovenaanzicht';
comment on column pand.inwwijze_geom_maaiveld is 'AN24 - Inwinningswijze geometrie maaiveld';
comment on column pand.laagste_bouwlaag is 'N3 - Laagste bouwlaag pand';
comment on column pand.oorspronkelijk_bouwjaar is 'N4 - Oorspronkelijk bouwjaar pand';
comment on column pand.oppervlakte is 'N6 - Oppervlakte pand';
comment on column pand.geom_bovenaanzicht is 'GM_Surface - Pandgeometrie bovenaanzicht';
comment on column pand.geom_maaiveld is 'GM_MultiSurface - Pandgeometrie maaiveld';
comment on column pand.status is 'AN80 - Pandstatus';
comment on column pand.relve_hoogteligging is 'N1 - Relatieve hoogteligging pand';
comment on column pand.status_voortgang_bouw is 'AN24 - Status voortgang bouw';

create table prs(
	sc_identif character varying(32),
	clazz character varying(255)
);
alter table prs add constraint prs_pk primary key(sc_identif);
comment on table prs is 'RSGB class PERSOON. Subclass van: SUBJECT. Directe superclass van: NATUURLIJK PERSOON, NIET-NATUURLIJK PERSOON';
comment on column prs.sc_identif is '[PK] AN32, FK naar subject.identif - Identificatie';
comment on column prs.clazz is 'Aanduiding subclass';

create table rsdoc(
	nummer character varying(9),
	aand_inhouding_of_vermissing character varying(1),
	autoriteit_uitgifte character varying(6),
	datum_inhouding_of_vermissing decimal(8,0),
	datum_uitgifte decimal(8,0),
	eindd_geldh_document decimal(8,0),
	lengte_houder decimal(8,0),
	fk_7rds_rsdoccode character varying(2)
);
alter table rsdoc add constraint rsdoc_pk primary key(nummer);
comment on table rsdoc is 'RSGB class REISDOCUMENT';
comment on column rsdoc.nummer is '[PK] AN9 - Reisdocumentnummer';
comment on column rsdoc.aand_inhouding_of_vermissing is 'AN1 - Aanduiding inhouding of vermissing';
comment on column rsdoc.autoriteit_uitgifte is 'AN6 - Autoriteit uitgifte';
comment on column rsdoc.datum_inhouding_of_vermissing is 'N8 - Datum inhouding of vermissing';
comment on column rsdoc.datum_uitgifte is 'N8 - Datum uitgifte';
comment on column rsdoc.eindd_geldh_document is 'N8 - Einddatum geldigheid document';
comment on column rsdoc.lengte_houder is 'N8 - Lengte houder';
comment on column rsdoc.fk_7rds_rsdoccode is '[FK] A2, FK naar rsdocsoort.rsdoccode: "Referentielijst REISDOCUMENT.Soort"';

create table scheiding(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type character varying(40)
);
select addgeometrycolumn('scheiding', 'geom', 28992, 'GEOMETRY', 2);
create index scheiding_geom_idx on scheiding USING GIST (geom);
alter table scheiding add constraint scheiding_pk primary key(identif);
comment on table scheiding is 'RSGB class SCHEIDING';
comment on column scheiding.identif is '[PK] NEN3610ID - Identificatie scheiding';
comment on column scheiding.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid scheiding';
comment on column scheiding.geom is 'LijnVlak - Geometrie scheiding';
comment on column scheiding.relve_hoogteligging is 'N1 - Relatieve hoogteligging scheiding';
comment on column scheiding.status is 'A8 - Status scheiding';
comment on column scheiding.type is 'AN40 - Type scheiding';

create table spoor(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	functie character varying(25),
	relve_hoogteligging decimal(1,0),
	status character varying(8)
);
select addgeometrycolumn('spoor', 'geom', 28992, 'LINESTRING', 2);
create index spoor_geom_idx on spoor USING GIST (geom);
alter table spoor add constraint spoor_pk primary key(identif);
comment on table spoor is 'RSGB class SPOOR';
comment on column spoor.identif is '[PK] NEN3610ID - Identificatie spoor';
comment on column spoor.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid spoor';
comment on column spoor.functie is 'AN25 - Functie spoor';
comment on column spoor.geom is 'GM_Curve - Geometrie spoor';
comment on column spoor.relve_hoogteligging is 'N1 - Relatieve hoogteligging spoor';
comment on column spoor.status is 'A8 - Status spoor';

create table stadsdeel(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	relve_hoogteligging decimal(1,0),
	naam character varying(40),
	status character varying(8)
);
select addgeometrycolumn('stadsdeel', 'geom', 28992, 'MULTIPOLYGON', 2);
create index stadsdeel_geom_idx on stadsdeel USING GIST (geom);
alter table stadsdeel add constraint stadsdeel_pk primary key(identif);
comment on table stadsdeel is 'RSGB class STADSDEEL';
comment on column stadsdeel.identif is '[PK] NEN3610ID - Identificatie stadsdeel';
comment on column stadsdeel.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid stadsdeel';
comment on column stadsdeel.relve_hoogteligging is 'N1 - Relatieve hoogteligging stadsdeel';
comment on column stadsdeel.geom is 'GM_Surface - Stadsdeelgeometrie';
comment on column stadsdeel.naam is 'AN40 - Stadsdeelnaam';
comment on column stadsdeel.status is 'A8 - Status stadsdeel';

create table standplaats(
	sc_identif character varying(16),
	indic_geconst character varying(1),
	status character varying(80),
	fk_4nra_sc_identif character varying(16)
);
alter table standplaats add constraint standplaats_pk primary key(sc_identif);
comment on table standplaats is 'RSGB class STANDPLAATS. Subclass van: BENOEMD TERREIN -> BENOEMD OBJECT';
comment on column standplaats.sc_identif is '[PK] AN16, FK naar benoemd_terrein.sc_identif - Benoemd object identificatie';
comment on column standplaats.indic_geconst is 'AN1 - Indicatie geconstateerde standplaats';
comment on column standplaats.status is 'AN80 - Standplaatsstatus';
comment on column standplaats.fk_4nra_sc_identif is '[FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "heeft als hoofdadres"';

create table subject(
	identif character varying(32),
	clazz character varying(255),
	adres_binnenland character varying(257),
	adres_buitenland character varying(149),
	emailadres character varying(254),
	fax_nummer character varying(20),
	kvk_nummer decimal(8,0),
	naam character varying(500),
	typering character varying(50),
	telefoonnummer character varying(20),
	website_url character varying(200),
	fk_13wpl_identif character varying(4),
	fk_14aoa_identif character varying(16),
	fk_15aoa_identif character varying(16),
	pa_postadres_postcode character varying(6),
	pa_postadrestype character varying(1),
	pa_postbus__of_antwoordnummer decimal(5,0),
	fk_pa_4_wpl_identif character varying(4),
	rn_bankrekeningnummer decimal(10,0),
	rn_bic character varying(11),
	rn_iban character varying(34),
	vb_adres_buitenland_1 character varying(35),
	vb_adres_buitenland_2 character varying(35),
	vb_adres_buitenland_3 character varying(35),
	fk_vb_lnd_code_iso character varying(2)
);
alter table subject add constraint subject_pk primary key(identif);
comment on table subject is 'RSGB class SUBJECT. Directe superclass van: PERSOON, VESTIGING';
comment on column subject.identif is '[PK] AN32 - Identificatie';
comment on column subject.clazz is 'Aanduiding subclass';
comment on column subject.adres_binnenland is 'AN257 - Adres binnenland';
comment on column subject.adres_buitenland is 'AN149 - Adres buitenland';
comment on column subject.emailadres is 'AN254 - Emailadres';
comment on column subject.fax_nummer is 'AN20 - Fax-nummer';
comment on column subject.kvk_nummer is 'N8 - KvK-nummer';
comment on column subject.naam is 'AN500 - Naam';
comment on column subject.typering is 'AN50 - Subjecttypering';
comment on column subject.telefoonnummer is 'AN20 - Telefoonnummer';
comment on column subject.website_url is 'AN200 - Website-URL';
comment on column subject.fk_13wpl_identif is '[FK] AN4, FK naar wnplts.identif: "heeft als correspondentieadres"';
comment on column subject.fk_14aoa_identif is '[FK] AN16, FK naar addresseerb_obj_aand.identif: "heeft als factuuradres"';
comment on column subject.fk_15aoa_identif is '[FK] AN16, FK naar addresseerb_obj_aand.identif: "heeft als correspondentieadres"';
comment on column subject.pa_postadres_postcode is 'Groepsattribuut Postadres SUBJECT.Postadres postcode - Postadres postcode';
comment on column subject.pa_postadrestype is 'Groepsattribuut Postadres SUBJECT.Postadrestype - Postadrestype';
comment on column subject.pa_postbus__of_antwoordnummer is 'Groepsattribuut Postadres SUBJECT.Postbus- of antwoordnummer - Postbus- of antwoordnummer';
comment on column subject.fk_pa_4_wpl_identif is '[FK] AN4, FK naar wnplts.identif: "Groepsattribuut Postadres SUBJECT.woonplaats"';
comment on column subject.rn_bankrekeningnummer is 'Groepsattribuut Rekeningnummer SUBJECT.Bankrekeningnummer - Bankrekeningnummer';
comment on column subject.rn_bic is 'Groepsattribuut Rekeningnummer SUBJECT.BIC - BIC';
comment on column subject.rn_iban is 'Groepsattribuut Rekeningnummer SUBJECT.IBAN - IBAN';
comment on column subject.vb_adres_buitenland_1 is 'Groepsattribuut Verblijf buitenland SUBJECT.Adres buitenland 1 - Adres buitenland 1';
comment on column subject.vb_adres_buitenland_2 is 'Groepsattribuut Verblijf buitenland SUBJECT.Adres buitenland 2 - Adres buitenland 2';
comment on column subject.vb_adres_buitenland_3 is 'Groepsattribuut Verblijf buitenland SUBJECT.Adres buitenland 3 - Adres buitenland 3';
comment on column subject.fk_vb_lnd_code_iso is '[FK] A2, FK naar land.code_iso: "Groepsattribuut referentielijst Land verblijfadres"';

create table verblijfsobj(
	sc_identif character varying(16),
	aantal_kamers decimal(2,0),
	hoogste_bouwlaag decimal(3,0),
	indic_geconstateerd character varying(1),
	laagste_bouwlaag decimal(3,0),
	ontsluiting_verdieping character varying(3),
	soort_woonobj decimal(1,0),
	toegang_bouwlaag decimal(3,0),
	status character varying(80),
	fk_11nra_sc_identif character varying(16)
);
alter table verblijfsobj add constraint verblijfsobj_pk primary key(sc_identif);
comment on table verblijfsobj is 'RSGB class VERBLIJFSOBJECT. Subclass van: GEBOUWD OBJECT -> BENOEMD OBJECT';
comment on column verblijfsobj.sc_identif is '[PK] AN16, FK naar gebouwd_obj.sc_identif - Benoemd object identificatie';
comment on column verblijfsobj.aantal_kamers is 'N2 - Aantal kamers';
comment on column verblijfsobj.hoogste_bouwlaag is 'N3 - Hoogste bouwlaag verblijfsobject';
comment on column verblijfsobj.indic_geconstateerd is 'AN1 - Indicatie geconstateerd verblijfsobject';
comment on column verblijfsobj.laagste_bouwlaag is 'N3 - Laagste bouwlaag verblijfsobject';
comment on column verblijfsobj.ontsluiting_verdieping is 'AN3 - Ontsluiting verdieping';
comment on column verblijfsobj.soort_woonobj is 'N1 - Soort woonobject';
comment on column verblijfsobj.toegang_bouwlaag is 'N3 - Toegang bouwlaag verblijfsobject';
comment on column verblijfsobj.status is 'AN80 - Verblijfsobjectstatus';
comment on column verblijfsobj.fk_11nra_sc_identif is '[FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "heeft als hoofdadres"';

create table verblijfstitel(
	aand decimal(2,0),
	begindatum_geldh character varying(19),
	eindd_geldh character varying(19),
	omschr character varying(100)
);
alter table verblijfstitel add constraint verblijfstitel_pk primary key(aand);
comment on table verblijfstitel is 'RSGB class VERBLIJFSTITEL';
comment on column verblijfstitel.aand is '[PK] N2 - Aanduiding verblijfstitel';
comment on column verblijfstitel.eindd_geldh is 'OnvolledigeDatum - Einddatum geldigheid verblijfstitel';
comment on column verblijfstitel.omschr is 'AN100 - Verblijfstitelomschrijving';

create table vestg(
	sc_identif character varying(32),
	activiteit_omschr character varying(2000),
	datum_aanvang character varying(19),
	datum_beeindiging character varying(19),
	datum_voortzetting character varying(19),
	fulltime_werkzame_mannen decimal(5,0),
	fulltime_werkzame_vrouwen decimal(5,0),
	parttime_werkzame_mannen decimal(5,0),
	parttime_werkzame_vrouwen decimal(5,0),
	toevoeging_adres character varying(100),
	verkorte_naam character varying(45),
	typering character varying(26),
	fk_15ond_kvk_nummer decimal(8,0),
	fk_16tgo_identif character varying(16),
	fk_17mac_kvk_nummer decimal(8,0),
	fk_18ves_sc_identif character varying(32),
	fk_19mac_kvk_nummer decimal(8,0),
	fk_20aoa_identif character varying(16),
	sa_indic_hoofdactiviteit character varying(3),
	fk_sa_sbi_activiteit_sbi_code character varying(6)
);
alter table vestg add constraint vestg_pk primary key(sc_identif);
comment on table vestg is 'RSGB class VESTIGING. Subclass van: SUBJECT';
comment on column vestg.sc_identif is '[PK] AN32, FK naar subject.identif - Identificatie';
comment on column vestg.activiteit_omschr is 'AN2000 - Activiteit omschrijving';
comment on column vestg.datum_aanvang is 'OnvolledigeDatum - Datum aanvang';
comment on column vestg.datum_beeindiging is 'OnvolledigeDatum - Datum beeindiging';
comment on column vestg.datum_voortzetting is 'OnvolledigeDatum - Datum voortzetting';
comment on column vestg.fulltime_werkzame_mannen is 'N5 - Fulltime werkzame mannen';
comment on column vestg.fulltime_werkzame_vrouwen is 'N5 - Fulltime werkzame vrouwen';
comment on column vestg.parttime_werkzame_mannen is 'N5 - Parttime werkzame mannen';
comment on column vestg.parttime_werkzame_vrouwen is 'N5 - Parttime werkzame vrouwen';
comment on column vestg.toevoeging_adres is 'AN100 - Toevoeging adres';
comment on column vestg.verkorte_naam is 'AN45 - Verkorte naam';
comment on column vestg.typering is 'AN26 - Vestigingtypering';
comment on column vestg.fk_15ond_kvk_nummer is '[FK] N8, FK naar ondrnmng.kvk_nummer: "betreft uitoefening van activiteiten door"';
comment on column vestg.fk_16tgo_identif is '[FK] AN16, FK naar benoemd_obj.identif: "heeft hoofdlocatie in of op"';
comment on column vestg.fk_17mac_kvk_nummer is '[FK] N8, FK naar maatschapp_activiteit.kvk_nummer: "betreft uitoefening van activiteiten door"';
comment on column vestg.fk_18ves_sc_identif is '[FK] AN32, FK naar vestg.sc_identif (is FK naar superclass SUBJECT): "is samengevoegd met"';
comment on column vestg.fk_19mac_kvk_nummer is '[FK] N8, FK naar maatschapp_activiteit.kvk_nummer: "is hoofdvestiging van"';
comment on column vestg.fk_20aoa_identif is '[FK] AN16, FK naar addresseerb_obj_aand.identif: "heeft als locatie-adres"';
comment on column vestg.sa_indic_hoofdactiviteit is 'Groepsattribuut SBI activiteit VESTIGING.Indicatie hoofdactiviteit - Indicatie hoofdactiviteit';
comment on column vestg.fk_sa_sbi_activiteit_sbi_code is '[FK] AN6, FK naar sbi_activiteit.sbi_code: "Groepsattribuut referentielijst SBI activiteit"';

create table vrijstaand_vegetatie_obj(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type character varying(40)
);
select addgeometrycolumn('vrijstaand_vegetatie_obj', 'geom', 28992, 'MULTIPOLYGON', 2);
create index vrijstaand_vegetatie_obj_geom_idx on vrijstaand_vegetatie_obj USING GIST (geom);
alter table vrijstaand_vegetatie_obj add constraint vrijstaand_vegetatie_obj_pk primary key(identif);
comment on table vrijstaand_vegetatie_obj is 'RSGB class VRIJSTAAND VEGETATIE OBJECT';
comment on column vrijstaand_vegetatie_obj.identif is '[PK] NEN3610ID - Identificatie vrijstaand vegetatie object';
comment on column vrijstaand_vegetatie_obj.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid vrijstaand vegetatie object';
comment on column vrijstaand_vegetatie_obj.geom is 'GM_Surface - Geometrie vrijstaand vegetatie object';
comment on column vrijstaand_vegetatie_obj.relve_hoogteligging is 'N1 - Relatieve hoogteligging vrijstaand vegetatie object';
comment on column vrijstaand_vegetatie_obj.status is 'A8 - Status vrijstaand vegetatie object';
comment on column vrijstaand_vegetatie_obj.type is 'AN40 - Type vrijstaand vegetatie object';

create table waterdeel(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	clazz character varying(255),
	datum_einde_geldh character varying(19),
	droogvallend character varying(1),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type character varying(40),
	fk_7opr_identifcode character varying(16)
);
select addgeometrycolumn('waterdeel', 'geom', 28992, 'MULTIPOLYGON', 2);
create index waterdeel_geom_idx on waterdeel USING GIST (geom);
alter table waterdeel add constraint waterdeel_pk primary key(identif);
comment on table waterdeel is 'RSGB class WATERDEEL. Directe superclass van: WATERVAKONDERDEEL';
comment on column waterdeel.identif is '[PK] NEN3610ID - Identificatie waterdeel';
comment on column waterdeel.clazz is 'Aanduiding subclass';
comment on column waterdeel.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid waterdeel';
comment on column waterdeel.droogvallend is 'AN1 - Droogvallend';
comment on column waterdeel.geom is 'GM_Surface - Geometrie waterdeel';
comment on column waterdeel.relve_hoogteligging is 'N1 - Relatieve hoogteligging waterdeel';
comment on column waterdeel.status is 'A8 - Status waterdeel';
comment on column waterdeel.type is 'AN40 - Type waterdeel';
comment on column waterdeel.fk_7opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode: "maakt deel uit van"';

create table watervakonderdeel(
	sc_identif character varying(255),
	fk_1wad_identif character varying(255)
);
alter table watervakonderdeel add constraint watervakonderdeel_pk primary key(sc_identif);
comment on table watervakonderdeel is 'RSGB class WATERVAKONDERDEEL. Subclass van: WATERDEEL';
comment on column watervakonderdeel.sc_identif is '[PK] NEN3610ID, FK naar waterdeel.identif - Identificatie waterdeel';
comment on column watervakonderdeel.fk_1wad_identif is '[FK] NEN3610ID, FK naar waterdeel.identif';

create table waterschap(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	naam character varying(40)
);
select addgeometrycolumn('waterschap', 'geom', 28992, 'MULTIPOLYGON', 2);
create index waterschap_geom_idx on waterschap USING GIST (geom);
alter table waterschap add constraint waterschap_pk primary key(identif);
comment on table waterschap is 'RSGB class WATERSCHAP';
comment on column waterschap.identif is '[PK] NEN3610ID - Identificatie waterschap';
comment on column waterschap.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid waterschap';
comment on column waterschap.relve_hoogteligging is 'N1 - Relatieve hoogteligging waterschap';
comment on column waterschap.status is 'A8 - Status waterschap';
comment on column waterschap.geom is 'GM_Surface - Waterschapgeometrie';
comment on column waterschap.naam is 'AN40 - Waterschapnaam';

create table wegdeel(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	clazz character varying(255),
	datum_einde_geldh character varying(19),
	functie character varying(25),
	fysiek_voork character varying(20),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	fk_8opr_identifcode character varying(16)
);
select addgeometrycolumn('wegdeel', 'geom', 28992, 'MULTIPOLYGON', 2);
create index wegdeel_geom_idx on wegdeel USING GIST (geom);
alter table wegdeel add constraint wegdeel_pk primary key(identif);
comment on table wegdeel is 'RSGB class WEGDEEL. Directe superclass van: WEGVAKONDERDEEL';
comment on column wegdeel.identif is '[PK] NEN3610ID - Identificatie wegdeel';
comment on column wegdeel.clazz is 'Aanduiding subclass';
comment on column wegdeel.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid wegdeel';
comment on column wegdeel.functie is 'AN25 - Functie wegdeel';
comment on column wegdeel.fysiek_voork is 'AN20 - Fysiek voorkomen wegdeel';
comment on column wegdeel.geom is 'GM_Surface - Geometrie wegdeel';
comment on column wegdeel.relve_hoogteligging is 'N1 - Relatieve hoogteligging wegdeel';
comment on column wegdeel.status is 'A8 - Status wegdeel';
comment on column wegdeel.fk_8opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode: "maakt deel uit van"';

create table wegvakonderdeel(
	sc_identif character varying(255),
	fk_1wgd_identif character varying(255)
);
alter table wegvakonderdeel add constraint wegvakonderdeel_pk primary key(sc_identif);
comment on table wegvakonderdeel is 'RSGB class WEGVAKONDERDEEL. Subclass van: WEGDEEL';
comment on column wegvakonderdeel.sc_identif is '[PK] NEN3610ID, FK naar wegdeel.identif - Identificatie wegdeel';
comment on column wegvakonderdeel.fk_1wgd_identif is '[FK] NEN3610ID, FK naar wegdeel.identif';

create table wijk(
	dat_beg_geldh character varying(19),
	code decimal(2,0),
	datum_einde_geldh character varying(19),
	identif_imgeowyk character varying(255),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	naam character varying(40)
);
select addgeometrycolumn('wijk', 'geom', 28992, 'MULTIPOLYGON', 2);
create index wijk_geom_idx on wijk USING GIST (geom);
alter table wijk add constraint wijk_pk primary key(code);
comment on table wijk is 'RSGB class WIJK';
comment on column wijk.code is '[PK] N2 - Wijkcode';
comment on column wijk.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid wijk';
comment on column wijk.identif_imgeowyk is 'NEN3610ID - Identificatie IMGeoWYK';
comment on column wijk.relve_hoogteligging is 'N1 - Relatieve hoogteligging wijk';
comment on column wijk.status is 'A8 - Status wijk';
comment on column wijk.geom is 'GM_Surface - Wijkgeometrie';
comment on column wijk.naam is 'AN40 - Wijknaam';

create table wnplts(
	dat_beg_geldh character varying(19),
	identif character varying(4),
	datum_einde_geldh character varying(19),
	indic_geconst character varying(1),
	naam character varying(80),
	naam_nen character varying(24),
	status character varying(80),
	fk_7gem_code decimal(4,0)
);
select addgeometrycolumn('wnplts', 'geom', 28992, 'MULTIPOLYGON', 2);
create index wnplts_geom_idx on wnplts USING GIST (geom);
alter table wnplts add constraint wnplts_pk primary key(identif);
comment on table wnplts is 'RSGB class WOONPLAATS';
comment on column wnplts.identif is '[PK] AN4 - Woonplaatsidentificatie';
comment on column wnplts.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid woonplaats';
comment on column wnplts.indic_geconst is 'AN1 - Indicatie geconstateerde woonplaats';
comment on column wnplts.geom is 'GM_Surface - Woonplaatsgeometrie';
comment on column wnplts.naam is 'AN80 - Woonplaatsnaam';
comment on column wnplts.naam_nen is 'AN24 - Woonplaatsnaam NEN';
comment on column wnplts.status is 'AN80 - Woonplaatsstatus';
comment on column wnplts.fk_7gem_code is '[FK] N4, FK naar gemeente.code: "ligt in"';

create table woz_deelobj(
	dat_beg_geldh_deelobj decimal(8,0),
	nummer decimal(6,0),
	code character varying(4),
	datum_einde_geldh_deelobj decimal(8,0),
	status decimal(2,0),
	fk_4pnd_identif character varying(16),
	fk_5tgo_identif character varying(16),
	fk_6woz_nummer decimal(12,0)
);
alter table woz_deelobj add constraint woz_deelobj_pk primary key(nummer);
comment on table woz_deelobj is 'RSGB class WOZ-DEELOBJECT';
comment on column woz_deelobj.nummer is '[PK] N6 - Nummer WOZ-deelobject';
comment on column woz_deelobj.code is 'AN4 - Code WOZ-deelobject';
comment on column woz_deelobj.datum_einde_geldh_deelobj is 'N8 - Datum einde geldigheid deelobject';
comment on column woz_deelobj.status is 'N2 - Status WOZ-deelobject';
comment on column woz_deelobj.fk_4pnd_identif is '[FK] AN16, FK naar pand.identif: "bestaat uit"';
comment on column woz_deelobj.fk_5tgo_identif is '[FK] AN16, FK naar benoemd_obj.identif: "bestaat uit"';
comment on column woz_deelobj.fk_6woz_nummer is '[FK] N12, FK naar woz_obj.nummer: "is onderdeel van"';

create table woz_obj(
	dat_beg_geldh character varying(19),
	nummer decimal(12,0),
	datum_einde_geldh character varying(19),
	gebruikscode decimal(2,0),
	grondoppervlakte decimal(11,0),
	soort_obj_code decimal(4,0),
	status decimal(2,0),
	vastgestelde_waarde decimal(11,0),
	waardepeildatum date
);
select addgeometrycolumn('woz_obj', 'geom', 28992, 'MULTIPOLYGON', 2);
create index woz_obj_geom_idx on woz_obj USING GIST (geom);
alter table woz_obj add constraint woz_obj_pk primary key(nummer);
comment on table woz_obj is 'RSGB class WOZ-OBJECT';
comment on column woz_obj.nummer is '[PK] N12 - WOZ-objectnummer';
comment on column woz_obj.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid WOZ-object';
comment on column woz_obj.gebruikscode is 'N2 - Gebruikscode';
comment on column woz_obj.geom is 'GM_Surface - Geometrie WOZ-object';
comment on column woz_obj.grondoppervlakte is 'N11 - Grondoppervlakte';
comment on column woz_obj.soort_obj_code is 'N4 - Soort-object-code';
comment on column woz_obj.status is 'N2 - Status WOZ-object';
comment on column woz_obj.vastgestelde_waarde is 'N11 - Vastgestelde waarde';
comment on column woz_obj.waardepeildatum is 'Datum - Waardepeildatum';

create table woz_waarde(
	waardepeildatum decimal(8,0),
	status_beschikking decimal(2,0),
	toestandspeildatum decimal(8,0),
	vastgestelde_waarde decimal(11,0),
	fk_1woz_nummer decimal(12,0)
);
comment on table woz_waarde is 'RSGB class WOZ-WAARDE';
comment on column woz_waarde.status_beschikking is 'N2 - Status beschikking';
comment on column woz_waarde.toestandspeildatum is 'N8 - Toestandspeildatum';
comment on column woz_waarde.vastgestelde_waarde is 'N11 - Vastgestelde waarde';
comment on column woz_waarde.fk_1woz_nummer is '[FK] N12, FK naar woz_obj.nummer';

create table zak_recht(
	kadaster_identif character varying(255),
	eindd_recht character varying(255),
	indic_betrokken_in_splitsing character varying(255),
	ingangsdatum_recht character varying(19),
	fk_7koz_kad_identif decimal(15,0),
	fk_8pes_sc_identif character varying(32),
	ar_noemer decimal(8,0),
	ar_teller decimal(8,0),
	fk_2aard_recht_verkort_aand character varying(4),
	fk_3avr_aand character varying(6)
);
alter table zak_recht add constraint zak_recht_pk primary key(kadaster_identif);
comment on table zak_recht is 'RSGB class ZAKELIJK RECHT';
comment on column zak_recht.kadaster_identif is '[PK]  - Kadaster identificatie zakelijk recht';
comment on column zak_recht.eindd_recht is '[geen RSGB type] - Einddatum recht';
comment on column zak_recht.indic_betrokken_in_splitsing is '[geen RSGB type] - Indicatie betrokken in splitsing';
comment on column zak_recht.ingangsdatum_recht is 'OnvolledigeDatum - Ingangsdatum recht';
comment on column zak_recht.fk_7koz_kad_identif is '[FK] N15, FK naar kad_onrrnd_zk.kad_identif: "betreft"';
comment on column zak_recht.fk_8pes_sc_identif is '[FK] AN32, FK naar prs.sc_identif (is FK naar superclass SUBJECT): "heeft als gerechtigde"';
comment on column zak_recht.ar_noemer is 'Groepsattribuut Aandeel in recht ZAKELIJK RECHT.Noemer - Noemer';
comment on column zak_recht.ar_teller is 'Groepsattribuut Aandeel in recht ZAKELIJK RECHT.Teller - Teller';
comment on column zak_recht.fk_2aard_recht_verkort_aand is '[FK] AN4, FK naar aard_recht_verkort.aand: "Referentielijst ZAKELIJK RECHT.Aanduiding aard recht verkort"';
comment on column zak_recht.fk_3avr_aand is '[FK] AN6, FK naar aard_verkregen_recht.aand: "Referentielijst ZAKELIJK RECHT.Aanduiding aard verkregen recht"';

create table zak_recht_aantek(
	kadaster_identif_aantek_recht character varying(255),
	aard_aantek_recht character varying(255),
	begindatum_aantek_recht character varying(19),
	beschrijving_aantek_recht character varying(255),
	eindd_aantek_recht character varying(255),
	fk_5zkr_kadaster_identif character varying(255),
	fk_6pes_sc_identif character varying(32)
);
alter table zak_recht_aantek add constraint zak_recht_aantek_pk primary key(kadaster_identif_aantek_recht);
comment on table zak_recht_aantek is 'RSGB class ZAKELIJK RECHT AANTEKENING';
comment on column zak_recht_aantek.kadaster_identif_aantek_recht is '[PK]  - Kadaster identificatie aantekening recht';
comment on column zak_recht_aantek.aard_aantek_recht is '[geen RSGB type] - Aard aantekening recht';
comment on column zak_recht_aantek.begindatum_aantek_recht is 'OnvolledigeDatum - Begindatum aantekening recht';
comment on column zak_recht_aantek.beschrijving_aantek_recht is '[geen RSGB type] - Beschrijving aantekening recht';
comment on column zak_recht_aantek.eindd_aantek_recht is '[geen RSGB type] - Einddatum aantekening recht';
comment on column zak_recht_aantek.fk_5zkr_kadaster_identif is '[FK] , FK naar zak_recht.kadaster_identif: "behoort bij"';
comment on column zak_recht_aantek.fk_6pes_sc_identif is '[FK] AN32, FK naar prs.sc_identif (is FK naar superclass SUBJECT): "heeft betrokken"';

create table gebouwd_obj_gebruiksdoel(
	gebruiksdoel_gebouwd_obj character varying(80),
	fk_gbo_sc_identif character varying(16)
);
comment on table gebouwd_obj_gebruiksdoel is 'RSGB class voor een-op-meer kolom GEBOUWD OBJECT Gebruiksdoel gebouwd object';
comment on column gebouwd_obj_gebruiksdoel.gebruiksdoel_gebouwd_obj is 'AN80 - Gebruiksdoel gebouwd object';
comment on column gebouwd_obj_gebruiksdoel.fk_gbo_sc_identif is '[FK] AN16, FK naar gebouwd_obj.sc_identif (is FK naar superclass BENOEMD OBJECT)';

create table overig_terrein_gebruiksdoel(
	gebruiksdoel_overig_terrein character varying(80),
	fk_otr_sc_identif character varying(16)
);
comment on table overig_terrein_gebruiksdoel is 'RSGB class voor een-op-meer kolom OVERIG TERREIN Gebruiksdoel overig terrein';
comment on column overig_terrein_gebruiksdoel.gebruiksdoel_overig_terrein is 'AN80 - Gebruiksdoel overig terrein';
comment on column overig_terrein_gebruiksdoel.fk_otr_sc_identif is '[FK] AN16, FK naar overig_terrein.sc_identif (is FK naar superclass BENOEMD OBJECT)';

create table vestg_naam(
	naam character varying(500),
	fk_ves_sc_identif character varying(32)
);
comment on table vestg_naam is 'RSGB class voor een-op-meer kolom VESTIGING (Handels)naam';
comment on column vestg_naam.naam is 'AN500 - (Handels)naam';
comment on column vestg_naam.fk_ves_sc_identif is '[FK] AN32, FK naar vestg.sc_identif (is FK naar superclass SUBJECT)';

create table functionaris(
	fk_sc_lh_pes_sc_identif character varying(32),
	fk_sc_rh_pes_sc_identif character varying(32),
	beperking_bev_in_euros decimal(18,0),
	bv_beperking_in_geld decimal(18,0),
	bv_omschr_ovrg_beperkingen character varying(2000),
	bv_ovrg_volmacht character varying(3),
	bv_soort_handeling character varying(35),
	bev_met_andere_prsn character varying(3),
	datum_toetr character varying(19),
	datum_uittreding character varying(19),
	functie character varying(80),
	functionaristypering character varying(35),
	indic_statutair_volmacht character varying(3),
	ovrg_beperking_bev character varying(3),
	soort_bev character varying(30),
	volledig_beperkt_volmacht character varying(1)
);
alter table functionaris add constraint functionaris_pk primary key(fk_sc_lh_pes_sc_identif,fk_sc_rh_pes_sc_identif);
comment on table functionaris is 'RSGB superclass FUNCTIONARIS';
comment on column functionaris.fk_sc_lh_pes_sc_identif is '[FK] AN32, FK naar prs.sc_identif (is FK naar superclass SUBJECT)';
comment on column functionaris.fk_sc_rh_pes_sc_identif is '[FK] AN32, FK naar prs.sc_identif (is FK naar superclass SUBJECT)';
comment on column functionaris.beperking_bev_in_euros is 'N18 - Beperking bevoegdheid  in euros';
comment on column functionaris.bv_beperking_in_geld is 'Groepsattribuut Beperkte volmacht FUNCTIONARIS.Beperking in geld - Beperking in geld';
comment on column functionaris.bv_omschr_ovrg_beperkingen is 'Groepsattribuut Beperkte volmacht FUNCTIONARIS.Omschrijving overige beperkingen - Omschrijving overige beperkingen';
comment on column functionaris.bv_ovrg_volmacht is 'Groepsattribuut Beperkte volmacht FUNCTIONARIS.Overige volmacht - Overige volmacht';
comment on column functionaris.bv_soort_handeling is 'Groepsattribuut Beperkte volmacht FUNCTIONARIS.Soort handeling - Soort handeling';
comment on column functionaris.bev_met_andere_prsn is 'AN3 - Bevoegdheid met andere personen';
comment on column functionaris.datum_toetr is 'OnvolledigeDatum - Datum toetreding';
comment on column functionaris.datum_uittreding is 'OnvolledigeDatum - Datum uittreding';
comment on column functionaris.functie is 'AN80 - Functie';
comment on column functionaris.functionaristypering is 'AN35 - Functionaristypering';
comment on column functionaris.indic_statutair_volmacht is 'AN3 - Indicatie statutair volmacht';
comment on column functionaris.ovrg_beperking_bev is 'AN3 - Overige beperking bevoegdheid';
comment on column functionaris.soort_bev is 'AN30 - Soort Bevoegdheid';
comment on column functionaris.volledig_beperkt_volmacht is 'AN1 - Volledig Beperkt volmacht';

create table huishoudenrel(
	fk_sc_lh_inp_sc_identif character varying(32),
	fk_sc_rh_hhd_nummer decimal(12,0),
	huishoudenrelcode decimal(1,0)
);
alter table huishoudenrel add constraint huishoudenrel_pk primary key(fk_sc_lh_inp_sc_identif,fk_sc_rh_hhd_nummer);
comment on table huishoudenrel is 'RSGB superclass HUISHOUDENRELATIE';
comment on column huishoudenrel.fk_sc_lh_inp_sc_identif is '[FK] AN32, FK naar ingeschr_nat_prs.sc_identif (is FK naar superclass SUBJECT)';
comment on column huishoudenrel.fk_sc_rh_hhd_nummer is '[FK] N12, FK naar huishouden.nummer';
comment on column huishoudenrel.huishoudenrelcode is 'N1 - Huishoudenrelatiecode';

create table huw_ger_partn(
	fk_sc_lh_inp_sc_identif character varying(32),
	fk_sc_rh_inp_sc_identif character varying(32),
	hs_datum_aangaan decimal(8,0),
	fk_hs_lnd_code_iso character varying(2),
	hs_plaats character varying(40),
	ho_datum_ontb_huw_ger_partn decimal(8,0),
	fk_ho_lnd_code_iso character varying(2),
	ho_plaats_ontb_huw_ger_partn character varying(40),
	ho_reden_ontb_huw_ger_partn character varying(1),
	soort_verbintenis character varying(1)
);
alter table huw_ger_partn add constraint huw_ger_partn_pk primary key(fk_sc_lh_inp_sc_identif,fk_sc_rh_inp_sc_identif);
comment on table huw_ger_partn is 'RSGB superclass HUWELIJK/GEREGISTREERD PARTNERSCHAP';
comment on column huw_ger_partn.fk_sc_lh_inp_sc_identif is '[FK] AN32, FK naar ingeschr_nat_prs.sc_identif (is FK naar superclass SUBJECT)';
comment on column huw_ger_partn.fk_sc_rh_inp_sc_identif is '[FK] AN32, FK naar ingeschr_nat_prs.sc_identif (is FK naar superclass SUBJECT)';
comment on column huw_ger_partn.hs_datum_aangaan is 'Groepsattribuut Sluiting/aangaan HUWELIJK/GEREGISTREERD PARTNERSCHAP.Datum huwelijkssluiting/aangaan geregistreerd partnerschap - Datum huwelijkssluiting/aangaan geregistreerd partnerschap';
comment on column huw_ger_partn.fk_hs_lnd_code_iso is '[FK] A2, FK naar land.code_iso: "Groepsattribuut referentielijst Land huwelijkssluiting/aangaan geregistreerd partnerschap"';
comment on column huw_ger_partn.hs_plaats is 'Groepsattribuut Sluiting/aangaan HUWELIJK/GEREGISTREERD PARTNERSCHAP.Plaats huwelijkssluiting/aangaan geregistreerd partnerschap - Plaats huwelijkssluiting/aangaan geregistreerd partnerschap';
comment on column huw_ger_partn.ho_datum_ontb_huw_ger_partn is 'Groepsattribuut Ontbinding HUWELIJK/GEREGISTREERD PARTNERSCHAP.Datum ontbinding huwelijk/geregistreerd partnerschap - Datum ontbinding huwelijk/geregistreerd partnerschap';
comment on column huw_ger_partn.fk_ho_lnd_code_iso is '[FK] A2, FK naar land.code_iso: "Groepsattribuut referentielijst Land ontbinding huwelijk/geregistreerd partnerschap"';
comment on column huw_ger_partn.ho_plaats_ontb_huw_ger_partn is 'Groepsattribuut Ontbinding HUWELIJK/GEREGISTREERD PARTNERSCHAP.Plaats ontbinding huwelijk/geregistreerd partnerschap - Plaats ontbinding huwelijk/geregistreerd partnerschap';
comment on column huw_ger_partn.ho_reden_ontb_huw_ger_partn is 'Groepsattribuut Ontbinding HUWELIJK/GEREGISTREERD PARTNERSCHAP.Reden ontbinding huwelijk/geregistreerd partnerschap - Reden ontbinding huwelijk/geregistreerd partnerschap';
comment on column huw_ger_partn.soort_verbintenis is 'AN1 - Soort verbintenis';

create table kad_onrrnd_zk_his_rel(
	fk_sc_lh_koz_kad_identif decimal(15,0),
	fk_sc_rh_koz_kad_identif decimal(15,0),
	aard character varying(255),
	overgangsgrootte character varying(255)
);
alter table kad_onrrnd_zk_his_rel add constraint kad_onrrnd_zk_his_rel_pk primary key(fk_sc_lh_koz_kad_identif,fk_sc_rh_koz_kad_identif);
comment on table kad_onrrnd_zk_his_rel is 'RSGB superclass KADASTRALE ONROERENDE ZAAK HISTORIE RELATIE';
comment on column kad_onrrnd_zk_his_rel.fk_sc_lh_koz_kad_identif is '[FK] N15, FK naar kad_onrrnd_zk.kad_identif';
comment on column kad_onrrnd_zk_his_rel.fk_sc_rh_koz_kad_identif is '[FK] N15, FK naar kad_onrrnd_zk.kad_identif';
comment on column kad_onrrnd_zk_his_rel.aard is ' - Aard';
comment on column kad_onrrnd_zk_his_rel.overgangsgrootte is ' - Overgangsgrootte';

create table locaand_adres(
	fk_sc_lh_aoa_identif character varying(16),
	fk_sc_rh_woz_nummer decimal(12,0),
	locomschr character varying(40)
);
alter table locaand_adres add constraint locaand_adres_pk primary key(fk_sc_lh_aoa_identif,fk_sc_rh_woz_nummer);
comment on table locaand_adres is 'RSGB superclass LOCATIEAANDUIDING ADRES';
comment on column locaand_adres.fk_sc_lh_aoa_identif is '[FK] AN16, FK naar addresseerb_obj_aand.identif';
comment on column locaand_adres.fk_sc_rh_woz_nummer is '[FK] N12, FK naar woz_obj.nummer';
comment on column locaand_adres.locomschr is 'AN40 - Locatieomschrijving';

create table locaand_openb_rmte(
	fk_sc_lh_opr_identifcode character varying(16),
	fk_sc_rh_woz_nummer decimal(12,0),
	locomschr character varying(40)
);
alter table locaand_openb_rmte add constraint locaand_openb_rmte_pk primary key(fk_sc_lh_opr_identifcode,fk_sc_rh_woz_nummer);
comment on table locaand_openb_rmte is 'RSGB superclass LOCATIEAANDUIDING OPENBARE RUIMTE';
comment on column locaand_openb_rmte.fk_sc_lh_opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode';
comment on column locaand_openb_rmte.fk_sc_rh_woz_nummer is '[FK] N12, FK naar woz_obj.nummer';
comment on column locaand_openb_rmte.locomschr is 'AN40 - Locatieomschrijving';

create table ouder_kind_rel(
	fk_sc_lh_inp_sc_identif character varying(32),
	fk_sc_rh_inp_sc_identif character varying(32),
	datum_einde_fam_recht_betr character varying(19),
	datum_ingang_fam_recht_betr decimal(8,0),
	ouder_aand character varying(6)
);
alter table ouder_kind_rel add constraint ouder_kind_rel_pk primary key(fk_sc_lh_inp_sc_identif,fk_sc_rh_inp_sc_identif);
comment on table ouder_kind_rel is 'RSGB superclass OUDER-KIND-RELATIE';
comment on column ouder_kind_rel.fk_sc_lh_inp_sc_identif is '[FK] AN32, FK naar ingeschr_nat_prs.sc_identif (is FK naar superclass SUBJECT)';
comment on column ouder_kind_rel.fk_sc_rh_inp_sc_identif is '[FK] AN32, FK naar ingeschr_nat_prs.sc_identif (is FK naar superclass SUBJECT)';
comment on column ouder_kind_rel.datum_einde_fam_recht_betr is 'OnvolledigeDatum - Datum einde familierechtelijke betrekking';
comment on column ouder_kind_rel.datum_ingang_fam_recht_betr is 'N8 - Datum ingang familierechtelijke betrekking';
comment on column ouder_kind_rel.ouder_aand is 'AN6 - Ouder-aanduiding';

create table woz_belang(
	fk_sc_lh_sub_identif character varying(32),
	fk_sc_rh_woz_nummer decimal(12,0),
	aand_eigenaargebruiker character varying(1)
);
alter table woz_belang add constraint woz_belang_pk primary key(fk_sc_lh_sub_identif,fk_sc_rh_woz_nummer);
comment on table woz_belang is 'RSGB superclass WOZ-BELANG';
comment on column woz_belang.fk_sc_lh_sub_identif is '[FK] AN32, FK naar subject.identif';
comment on column woz_belang.fk_sc_rh_woz_nummer is '[FK] N12, FK naar woz_obj.nummer';
comment on column woz_belang.aand_eigenaargebruiker is 'AN1 - Aanduiding eigenaar/gebruiker';

create table app_re_kad_perceel(
	fk_nn_lh_apr_sc_kad_identif decimal(15,0),
	fk_nn_lh_apr_sc_dat_beg_geldh character varying(19),
	fk_nn_rh_kdp_sc_kad_identif decimal(15,0)
);
alter table app_re_kad_perceel add constraint app_re_kad_perceel_pk primary key(fk_nn_lh_apr_sc_kad_identif,fk_nn_rh_kdp_sc_kad_identif);
comment on table app_re_kad_perceel is 'N - N relatie: APPARTEMENTSRECHT "maakt deel uit van appartementencomplex dat staat op" KADASTRAAL PERCEEL';
comment on column app_re_kad_perceel.fk_nn_lh_apr_sc_kad_identif is '[FK] N15, FK naar app_re.sc_kad_identif (is FK naar superclass KADASTRALE ONROERENDE ZAAK)';
comment on column app_re_kad_perceel.fk_nn_rh_kdp_sc_kad_identif is '[FK] N15, FK naar kad_perceel.sc_kad_identif (is FK naar superclass KADASTRALE ONROERENDE ZAAK)';

create table benoemd_obj_kad_onrrnd_zk(
	fk_nn_lh_tgo_identif character varying(16),
	fk_nn_rh_koz_kad_identif decimal(15,0)
);
alter table benoemd_obj_kad_onrrnd_zk add constraint benoemd_obj_kad_onrrnd_zk_pk primary key(fk_nn_lh_tgo_identif,fk_nn_rh_koz_kad_identif);
comment on table benoemd_obj_kad_onrrnd_zk is 'N - N relatie: BENOEMD OBJECT "staat op of heeft ruimtelijke overlap met" KADASTRALE ONROERENDE ZAAK';
comment on column benoemd_obj_kad_onrrnd_zk.fk_nn_lh_tgo_identif is '[FK] AN16, FK naar benoemd_obj.identif';
comment on column benoemd_obj_kad_onrrnd_zk.fk_nn_rh_koz_kad_identif is '[FK] N15, FK naar kad_onrrnd_zk.kad_identif';

create table benoemd_terrein_benoemd_terrei(
	fk_nn_lh_btr_sc_identif character varying(16),
	fk_nn_lh_btr_dat_beg_geldh character varying(19),
	fk_nn_rh_btr_sc_identif character varying(16)
);
alter table benoemd_terrein_benoemd_terrei add constraint benoemd_terrein_benoemd_ter_pk primary key(fk_nn_lh_btr_sc_identif,fk_nn_rh_btr_sc_identif);
comment on table benoemd_terrein_benoemd_terrei is 'N - N relatie: BENOEMD TERREIN "is ontstaan uit / overgegaan in" BENOEMD TERREIN';
comment on column benoemd_terrein_benoemd_terrei.fk_nn_lh_btr_sc_identif is '[FK] AN16, FK naar benoemd_terrein.sc_identif (is FK naar superclass BENOEMD OBJECT)';
comment on column benoemd_terrein_benoemd_terrei.fk_nn_rh_btr_sc_identif is '[FK] AN16, FK naar benoemd_terrein.sc_identif (is FK naar superclass BENOEMD OBJECT)';

create table gemeente_gemeente(
	fk_nn_lh_gem_code decimal(4,0),
	fk_nn_lh_gem_dat_beg_geldh character varying(19),
	fk_nn_rh_gem_code decimal(4,0)
);
alter table gemeente_gemeente add constraint gemeente_gemeente_pk primary key(fk_nn_lh_gem_code,fk_nn_rh_gem_code);
comment on table gemeente_gemeente is 'N - N relatie: GEMEENTE "is overgegaan in" GEMEENTE';
comment on column gemeente_gemeente.fk_nn_lh_gem_code is '[FK] N4, FK naar gemeente.code';
comment on column gemeente_gemeente.fk_nn_rh_gem_code is '[FK] N4, FK naar gemeente.code';

create table kad_onrrnd_zk_kad_onrrnd_zk(
	fk_nn_lh_koz_kad_identif decimal(15,0),
	fk_nn_lh_koz_dat_beg_geldh character varying(19),
	fk_nn_rh_koz_kad_identif decimal(15,0)
);
alter table kad_onrrnd_zk_kad_onrrnd_zk add constraint kad_onrrnd_zk_kad_onrrnd_zk_pk primary key(fk_nn_lh_koz_kad_identif,fk_nn_rh_koz_kad_identif);
comment on table kad_onrrnd_zk_kad_onrrnd_zk is 'N - N relatie: KADASTRALE ONROERENDE ZAAK "is hoofdperceel bij mandelige" KADASTRALE ONROERENDE ZAAK';
comment on column kad_onrrnd_zk_kad_onrrnd_zk.fk_nn_lh_koz_kad_identif is '[FK] N15, FK naar kad_onrrnd_zk.kad_identif';
comment on column kad_onrrnd_zk_kad_onrrnd_zk.fk_nn_rh_koz_kad_identif is '[FK] N15, FK naar kad_onrrnd_zk.kad_identif';

create table ligplaats_nummeraand(
	fk_nn_lh_lpl_sc_identif character varying(16),
	fk_nn_lh_lpl_sc_dat_beg_geldh character varying(19),
	fk_nn_rh_nra_sc_identif character varying(16)
);
alter table ligplaats_nummeraand add constraint ligplaats_nummeraand_pk primary key(fk_nn_lh_lpl_sc_identif,fk_nn_rh_nra_sc_identif);
comment on table ligplaats_nummeraand is 'N - N relatie: LIGPLAATS "heeft als nevenadressen" NUMMERAANDUIDING';
comment on column ligplaats_nummeraand.fk_nn_lh_lpl_sc_identif is '[FK] AN16, FK naar ligplaats.sc_identif (is FK naar superclass BENOEMD OBJECT)';
comment on column ligplaats_nummeraand.fk_nn_rh_nra_sc_identif is '[FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING)';

create table openb_rmte_gem_openb_rmte(
	fk_nn_lh_opr_identifcode character varying(16),
	fk_nn_rh_gor_identifcode character varying(16)
);
alter table openb_rmte_gem_openb_rmte add constraint openb_rmte_gem_openb_rmte_pk primary key(fk_nn_lh_opr_identifcode,fk_nn_rh_gor_identifcode);
comment on table openb_rmte_gem_openb_rmte is 'N - N relatie: OPENBARE RUIMTE "maakt deel uit van" GEMEENTELIJKE OPENBARE RUIMTE';
comment on column openb_rmte_gem_openb_rmte.fk_nn_lh_opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode';
comment on column openb_rmte_gem_openb_rmte.fk_nn_rh_gor_identifcode is '[FK] AN16, FK naar gem_openb_rmte.identifcode';

create table openb_rmte_wnplts(
	fk_nn_lh_opr_identifcode character varying(16),
	fk_nn_rh_wpl_identif character varying(4)
);
alter table openb_rmte_wnplts add constraint openb_rmte_wnplts_pk primary key(fk_nn_lh_opr_identifcode,fk_nn_rh_wpl_identif);
comment on table openb_rmte_wnplts is 'N - N relatie: OPENBARE RUIMTE "ligt in" WOONPLAATS';
comment on column openb_rmte_wnplts.fk_nn_lh_opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode';
comment on column openb_rmte_wnplts.fk_nn_rh_wpl_identif is '[FK] AN4, FK naar wnplts.identif';

create table rsdoc_ingeschr_nat_prs(
	fk_nn_lh_rsd_nummer character varying(9),
	fk_nn_rh_inp_sc_identif character varying(32)
);
alter table rsdoc_ingeschr_nat_prs add constraint rsdoc_ingeschr_nat_prs_pk primary key(fk_nn_lh_rsd_nummer,fk_nn_rh_inp_sc_identif);
comment on table rsdoc_ingeschr_nat_prs is 'N - N relatie: REISDOCUMENT "heeft als houder" INGESCHREVEN NATUURLIJK PERSOON';
comment on column rsdoc_ingeschr_nat_prs.fk_nn_lh_rsd_nummer is '[FK] AN9, FK naar rsdoc.nummer';
comment on column rsdoc_ingeschr_nat_prs.fk_nn_rh_inp_sc_identif is '[FK] AN32, FK naar ingeschr_nat_prs.sc_identif (is FK naar superclass SUBJECT)';

create table standplaats_nummeraand(
	fk_nn_lh_spl_sc_identif character varying(16),
	fk_nn_lh_spl_sc_dat_beg_geldh character varying(19),
	fk_nn_rh_nra_sc_identif character varying(16)
);
alter table standplaats_nummeraand add constraint standplaats_nummeraand_pk primary key(fk_nn_lh_spl_sc_identif,fk_nn_rh_nra_sc_identif);
comment on table standplaats_nummeraand is 'N - N relatie: STANDPLAATS "heeft als nevenadressen" NUMMERAANDUIDING';
comment on column standplaats_nummeraand.fk_nn_lh_spl_sc_identif is '[FK] AN16, FK naar standplaats.sc_identif (is FK naar superclass BENOEMD OBJECT)';
comment on column standplaats_nummeraand.fk_nn_rh_nra_sc_identif is '[FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING)';

create table verblijfsobj_pand(
	fk_nn_lh_vbo_sc_identif character varying(16),
	fk_nn_lh_vbo_sc_dat_beg_geldh character varying(19),
	fk_nn_rh_pnd_identif character varying(16)
);
alter table verblijfsobj_pand add constraint verblijfsobj_pand_pk primary key(fk_nn_lh_vbo_sc_identif,fk_nn_rh_pnd_identif);
comment on table verblijfsobj_pand is 'N - N relatie: VERBLIJFSOBJECT "maakt deel uit van" PAND';
comment on column verblijfsobj_pand.fk_nn_lh_vbo_sc_identif is '[FK] AN16, FK naar verblijfsobj.sc_identif (is FK naar superclass BENOEMD OBJECT)';
comment on column verblijfsobj_pand.fk_nn_rh_pnd_identif is '[FK] AN16, FK naar pand.identif';

create table verblijfsobj_nummeraand(
	fk_nn_lh_vbo_sc_identif character varying(16),
	fk_nn_lh_vbo_sc_dat_beg_geldh character varying(19),
	fk_nn_rh_nra_sc_identif character varying(16)
);
alter table verblijfsobj_nummeraand add constraint verblijfsobj_nummeraand_pk primary key(fk_nn_lh_vbo_sc_identif,fk_nn_rh_nra_sc_identif);
comment on table verblijfsobj_nummeraand is 'N - N relatie: VERBLIJFSOBJECT "heeft als nevenadres(sen)" NUMMERAANDUIDING';
comment on column verblijfsobj_nummeraand.fk_nn_lh_vbo_sc_identif is '[FK] AN16, FK naar verblijfsobj.sc_identif (is FK naar superclass BENOEMD OBJECT)';
comment on column verblijfsobj_nummeraand.fk_nn_rh_nra_sc_identif is '[FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING)';

create table vestg_benoemd_obj(
	fk_nn_lh_ves_sc_identif character varying(32),
	fk_nn_rh_tgo_identif character varying(16)
);
alter table vestg_benoemd_obj add constraint vestg_benoemd_obj_pk primary key(fk_nn_lh_ves_sc_identif,fk_nn_rh_tgo_identif);
comment on table vestg_benoemd_obj is 'N - N relatie: VESTIGING "heeft nevenlocatie in of op" BENOEMD OBJECT';
comment on column vestg_benoemd_obj.fk_nn_lh_ves_sc_identif is '[FK] AN32, FK naar vestg.sc_identif (is FK naar superclass SUBJECT)';
comment on column vestg_benoemd_obj.fk_nn_rh_tgo_identif is '[FK] AN16, FK naar benoemd_obj.identif';

create table meta_enumeratie_waardes(
	naam character varying(255),
	waarde character varying(255)
);
alter table meta_enumeratie_waardes add constraint meta_enumeratie_waardes_pk primary key(naam,waarde);
comment on table meta_enumeratie_waardes is 'RSGB class meta_enumeratie_waardes';
comment on column meta_enumeratie_waardes.naam is 'naam van de enum - naam';
comment on column meta_enumeratie_waardes.waarde is 'waarde van de enum - waarde';

create table meta_enumeratie(
	tabel character varying(255),
	kolom character varying(255),
	enumeratie character varying(255)
);
alter table meta_enumeratie add constraint meta_enumeratie_pk primary key(tabel,kolom);
comment on table meta_enumeratie is 'RSGB class meta_enumeratie';
comment on column meta_enumeratie.tabel is '[PK] tabel - tabel';
comment on column meta_enumeratie.kolom is '[PK] kolom - kolom';
comment on column meta_enumeratie.enumeratie is 'enumeratie - enumeratie';

create table meta_referentielijsten(
	tabel character varying(255),
	kolom character varying(255),
	referentielijst character varying(255)
);
alter table meta_referentielijsten add constraint meta_referentielijsten_pk primary key(tabel,kolom);
comment on table meta_referentielijsten is 'RSGB class meta_referentielijsten';
comment on column meta_referentielijsten.tabel is '[PK] tabel - tabel';
comment on column meta_referentielijsten.kolom is '[PK] kolom - kolom';
comment on column meta_referentielijsten.referentielijst is 'referentielijst - referentielijst';


-- Foreign keys voor tabel addresseerb_obj_aand
alter table addresseerb_obj_aand add constraint fk_aoa_as_6 foreign key (fk_6wpl_identif) references wnplts (identif) on delete cascade;
alter table addresseerb_obj_aand add constraint fk_aoa_as_7 foreign key (fk_7opr_identifcode) references openb_rmte (identifcode) on delete cascade;

-- Foreign keys voor tabel ander_btnlnds_niet_nat_prs
alter table ander_btnlnds_niet_nat_prs add constraint fk_ann_sc foreign key (sc_identif) references niet_nat_prs (sc_identif) on delete cascade;

-- Foreign keys voor tabel ander_nat_prs
alter table ander_nat_prs add constraint fk_anp_sc foreign key (sc_identif) references nat_prs (sc_identif) on delete cascade;
alter table ander_nat_prs add constraint fk_anp_as_3 foreign key (fk_3aoa_identif) references addresseerb_obj_aand (identif) on delete cascade;

-- Foreign keys voor tabel app_re
alter table app_re add constraint fk_apr_sc foreign key (sc_kad_identif) references kad_onrrnd_zk (kad_identif) on delete cascade;
alter table app_re add constraint fk_apr_as_2 foreign key (fk_2nnp_sc_identif) references niet_nat_prs (sc_identif) on delete cascade;

-- Foreign keys voor tabel begr_terr_dl
alter table begr_terr_dl add constraint fk_btd_as_8 foreign key (fk_8opr_identifcode) references openb_rmte (identifcode) on delete cascade;

-- Foreign keys voor tabel begr_terreinvakonderd
alter table begr_terreinvakonderd add constraint fk_btv_sc foreign key (sc_identif) references begr_terr_dl (identif) on delete cascade;
alter table begr_terreinvakonderd add constraint fk_btv_1n_1 foreign key (fk_1btd_identif) references begr_terr_dl (identif) on delete cascade;

-- Foreign keys voor tabel benoemd_terrein
alter table benoemd_terrein add constraint fk_btr_sc foreign key (sc_identif) references benoemd_obj (identif) on delete cascade;

-- Foreign keys voor tabel brugconstructie_element
alter table brugconstructie_element add constraint fk_bce_sc foreign key (sc_identif) references kunstwerkdeel (identif) on delete cascade;

-- Foreign keys voor tabel gebouwd_obj
alter table gebouwd_obj add constraint fk_gbo_sc foreign key (sc_identif) references benoemd_obj (identif) on delete cascade;

-- Foreign keys voor tabel gem_openb_rmte
alter table gem_openb_rmte add constraint fk_gor_as_7 foreign key (fk_7gem_code) references gemeente (code) on delete cascade;

-- Foreign keys voor tabel huishouden
alter table huishouden add constraint fk_hhd_as_4 foreign key (fk_4vbo_sc_identif) references verblijfsobj (sc_identif) on delete cascade;
alter table huishouden add constraint fk_hhd_as_5 foreign key (fk_5lpl_sc_identif) references ligplaats (sc_identif) on delete cascade;
alter table huishouden add constraint fk_hhd_as_7 foreign key (fk_7spl_sc_identif) references standplaats (sc_identif) on delete cascade;

-- Foreign keys voor tabel ingeschr_niet_nat_prs
alter table ingeschr_niet_nat_prs add constraint fk_inn_sc foreign key (sc_identif) references niet_nat_prs (sc_identif) on delete cascade;
alter table ingeschr_niet_nat_prs add constraint fk_inn_as_7 foreign key (fk_7aoa_identif) references addresseerb_obj_aand (identif) on delete cascade;

-- Foreign keys voor tabel ingeschr_nat_prs
alter table ingeschr_nat_prs add constraint fk_inp_sc foreign key (sc_identif) references nat_prs (sc_identif) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_as_26 foreign key (fk_26lpl_sc_identif) references ligplaats (sc_identif) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_as_27 foreign key (fk_27nra_sc_identif) references nummeraand (sc_identif) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_as_28 foreign key (fk_28wpl_identif) references wnplts (identif) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_as_29 foreign key (fk_29spl_sc_identif) references standplaats (sc_identif) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_as_30 foreign key (fk_30vbo_sc_identif) references verblijfsobj (sc_identif) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_1n_1 foreign key (fk_1rsd_nummer) references rsdoc (nummer) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_gb_2 foreign key (fk_gb_lnd_code_iso) references land (code_iso) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_nt_2 foreign key (fk_nt_nat_code) references nation (code) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_ol_1 foreign key (fk_ol_lnd_code_iso) references land (code_iso) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_va_as_3 foreign key (fk_va_3_vbo_sc_identif) references verblijfsobj (sc_identif) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_va_as_4 foreign key (fk_va_4_spl_sc_identif) references standplaats (sc_identif) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_va_as_5 foreign key (fk_va_5_nra_sc_identif) references nummeraand (sc_identif) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_va_as_6 foreign key (fk_va_6_wpl_identif) references wnplts (identif) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_va_as_7 foreign key (fk_va_7_lpl_sc_identif) references ligplaats (sc_identif) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_rl_3 foreign key (fk_3nat_code) references nation (code) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_rl_16 foreign key (fk_16lnd_code_iso) references land (code_iso) on delete cascade;
alter table ingeschr_nat_prs add constraint fk_inp_rl_17 foreign key (fk_17lnd_code_iso) references land (code_iso) on delete cascade;

-- Foreign keys voor tabel ingezetene
alter table ingezetene add constraint fk_ing_sc foreign key (sc_identif) references ingeschr_nat_prs (sc_identif) on delete cascade;
alter table ingezetene add constraint fk_ing_as_8 foreign key (fk_8vbt_aand) references verblijfstitel (aand) on delete cascade;

-- Foreign keys voor tabel kad_perceel
alter table kad_perceel add constraint fk_kdp_sc foreign key (sc_kad_identif) references kad_onrrnd_zk (kad_identif) on delete cascade;
alter table kad_perceel add constraint fk_kdp_as_7 foreign key (fk_7kdp_sc_kad_identif) references kad_perceel (sc_kad_identif) on delete cascade;

-- Foreign keys voor tabel kad_onrrnd_zk
alter table kad_onrrnd_zk add constraint fk_koz_as_7 foreign key (fk_7kdg_code) references kad_gemeente (code) on delete cascade;
alter table kad_onrrnd_zk add constraint fk_koz_as_10 foreign key (fk_10pes_sc_identif) references prs (sc_identif) on delete cascade;

-- Foreign keys voor tabel kad_onrrnd_zk_aantek
alter table kad_onrrnd_zk_aantek add constraint fk_kza_as_4 foreign key (fk_4koz_kad_identif) references kad_onrrnd_zk (kad_identif) on delete cascade;
alter table kad_onrrnd_zk_aantek add constraint fk_kza_as_5 foreign key (fk_5pes_sc_identif) references prs (sc_identif) on delete cascade;

-- Foreign keys voor tabel ligplaats
alter table ligplaats add constraint fk_lpl_sc foreign key (sc_identif) references benoemd_terrein (sc_identif) on delete cascade;
alter table ligplaats add constraint fk_lpl_as_4 foreign key (fk_4nra_sc_identif) references nummeraand (sc_identif) on delete cascade;

-- Foreign keys voor tabel maatschapp_activiteit
alter table maatschapp_activiteit add constraint fk_mac_as_3 foreign key (fk_3ond_kvk_nummer) references ondrnmng (kvk_nummer) on delete cascade;
alter table maatschapp_activiteit add constraint fk_mac_as_4 foreign key (fk_4pes_sc_identif) references prs (sc_identif) on delete cascade;

-- Foreign keys voor tabel nat_prs
alter table nat_prs add constraint fk_nps_sc foreign key (sc_identif) references prs (sc_identif) on delete cascade;
alter table nat_prs add constraint fk_nps_rl_2 foreign key (fk_2acd_code) references academische_titel (code) on delete cascade;

-- Foreign keys voor tabel niet_ingezetene
alter table niet_ingezetene add constraint fk_nin_sc foreign key (sc_identif) references ingeschr_nat_prs (sc_identif) on delete cascade;

-- Foreign keys voor tabel niet_nat_prs
alter table niet_nat_prs add constraint fk_nnp_sc foreign key (sc_identif) references prs (sc_identif) on delete cascade;

-- Foreign keys voor tabel nummeraand
alter table nummeraand add constraint fk_nra_sc foreign key (sc_identif) references addresseerb_obj_aand (identif) on delete cascade;

-- Foreign keys voor tabel onbegr_terr_dl
alter table onbegr_terr_dl add constraint fk_obt_as_8 foreign key (fk_8opr_identifcode) references openb_rmte (identifcode) on delete cascade;

-- Foreign keys voor tabel onbegr_terreinvakonderd
alter table onbegr_terreinvakonderd add constraint fk_otv_sc foreign key (sc_identif) references onbegr_terr_dl (identif) on delete cascade;
alter table onbegr_terreinvakonderd add constraint fk_otv_1n_1 foreign key (fk_1obt_identif) references onbegr_terr_dl (identif) on delete cascade;

-- Foreign keys voor tabel ondrnmng
alter table ondrnmng add constraint fk_ond_as_4 foreign key (fk_4mac_kvk_nummer) references maatschapp_activiteit (kvk_nummer) on delete cascade;
alter table ondrnmng add constraint fk_ond_1n_1 foreign key (fk_1ond_kvk_nummer) references ondrnmng (kvk_nummer) on delete cascade;

-- Foreign keys voor tabel ondersteunend_wegdeel
alter table ondersteunend_wegdeel add constraint fk_owd_as_6 foreign key (fk_6opr_identifcode) references openb_rmte (identifcode) on delete cascade;

-- Foreign keys voor tabel ovrg_addresseerb_obj_aand
alter table ovrg_addresseerb_obj_aand add constraint fk_oao_sc foreign key (sc_identif) references addresseerb_obj_aand (identif) on delete cascade;

-- Foreign keys voor tabel overig_gebouwd_obj
alter table overig_gebouwd_obj add constraint fk_ogo_sc foreign key (sc_identif) references gebouwd_obj (sc_identif) on delete cascade;
alter table overig_gebouwd_obj add constraint fk_ogo_as_4 foreign key (fk_4oao_sc_identif) references ovrg_addresseerb_obj_aand (sc_identif) on delete cascade;
alter table overig_gebouwd_obj add constraint fk_ogo_as_5 foreign key (fk_5nra_sc_identif) references nummeraand (sc_identif) on delete cascade;
alter table overig_gebouwd_obj add constraint fk_ogo_as_6 foreign key (fk_6opr_identifcode) references openb_rmte (identifcode) on delete cascade;

-- Foreign keys voor tabel overig_terrein
alter table overig_terrein add constraint fk_otr_sc foreign key (sc_identif) references benoemd_terrein (sc_identif) on delete cascade;
alter table overig_terrein add constraint fk_otr_as_2 foreign key (fk_2oao_sc_identif) references ovrg_addresseerb_obj_aand (sc_identif) on delete cascade;

-- Foreign keys voor tabel prs
alter table prs add constraint fk_pes_sc foreign key (sc_identif) references subject (identif) on delete cascade;

-- Foreign keys voor tabel rsdoc
alter table rsdoc add constraint fk_rsd_rl_7 foreign key (fk_7rds_rsdoccode) references rsdocsoort (rsdoccode) on delete cascade;

-- Foreign keys voor tabel standplaats
alter table standplaats add constraint fk_spl_sc foreign key (sc_identif) references benoemd_terrein (sc_identif) on delete cascade;
alter table standplaats add constraint fk_spl_as_4 foreign key (fk_4nra_sc_identif) references nummeraand (sc_identif) on delete cascade;

-- Foreign keys voor tabel subject
alter table subject add constraint fk_sub_as_13 foreign key (fk_13wpl_identif) references wnplts (identif) on delete cascade;
alter table subject add constraint fk_sub_as_14 foreign key (fk_14aoa_identif) references addresseerb_obj_aand (identif) on delete cascade;
alter table subject add constraint fk_sub_as_15 foreign key (fk_15aoa_identif) references addresseerb_obj_aand (identif) on delete cascade;
alter table subject add constraint fk_sub_pa_as_4 foreign key (fk_pa_4_wpl_identif) references wnplts (identif) on delete cascade;
alter table subject add constraint fk_sub_vb_4 foreign key (fk_vb_lnd_code_iso) references land (code_iso) on delete cascade;

-- Foreign keys voor tabel verblijfsobj
alter table verblijfsobj add constraint fk_vbo_sc foreign key (sc_identif) references gebouwd_obj (sc_identif) on delete cascade;
alter table verblijfsobj add constraint fk_vbo_as_11 foreign key (fk_11nra_sc_identif) references nummeraand (sc_identif) on delete cascade;

-- Foreign keys voor tabel vestg
alter table vestg add constraint fk_ves_sc foreign key (sc_identif) references subject (identif) on delete cascade;
alter table vestg add constraint fk_ves_as_15 foreign key (fk_15ond_kvk_nummer) references ondrnmng (kvk_nummer) on delete cascade;
alter table vestg add constraint fk_ves_as_16 foreign key (fk_16tgo_identif) references benoemd_obj (identif) on delete cascade;
alter table vestg add constraint fk_ves_as_17 foreign key (fk_17mac_kvk_nummer) references maatschapp_activiteit (kvk_nummer) on delete cascade;
alter table vestg add constraint fk_ves_as_18 foreign key (fk_18ves_sc_identif) references vestg (sc_identif) on delete cascade;
alter table vestg add constraint fk_ves_as_19 foreign key (fk_19mac_kvk_nummer) references maatschapp_activiteit (kvk_nummer) on delete cascade;
alter table vestg add constraint fk_ves_as_20 foreign key (fk_20aoa_identif) references addresseerb_obj_aand (identif) on delete cascade;
alter table vestg add constraint fk_ves_sa_2 foreign key (fk_sa_sbi_activiteit_sbi_code) references sbi_activiteit (sbi_code) on delete cascade;

-- Foreign keys voor tabel waterdeel
alter table waterdeel add constraint fk_wad_as_7 foreign key (fk_7opr_identifcode) references openb_rmte (identifcode) on delete cascade;

-- Foreign keys voor tabel watervakonderdeel
alter table watervakonderdeel add constraint fk_wav_sc foreign key (sc_identif) references waterdeel (identif) on delete cascade;
alter table watervakonderdeel add constraint fk_wav_1n_1 foreign key (fk_1wad_identif) references waterdeel (identif) on delete cascade;

-- Foreign keys voor tabel wegdeel
alter table wegdeel add constraint fk_wgd_as_8 foreign key (fk_8opr_identifcode) references openb_rmte (identifcode) on delete cascade;

-- Foreign keys voor tabel wegvakonderdeel
alter table wegvakonderdeel add constraint fk_wvd_sc foreign key (sc_identif) references wegdeel (identif) on delete cascade;
alter table wegvakonderdeel add constraint fk_wvd_1n_1 foreign key (fk_1wgd_identif) references wegdeel (identif) on delete cascade;

-- Foreign keys voor tabel wnplts
alter table wnplts add constraint fk_wpl_as_7 foreign key (fk_7gem_code) references gemeente (code) on delete cascade;

-- Foreign keys voor tabel woz_deelobj
alter table woz_deelobj add constraint fk_wdo_as_4 foreign key (fk_4pnd_identif) references pand (identif) on delete cascade;
alter table woz_deelobj add constraint fk_wdo_as_5 foreign key (fk_5tgo_identif) references benoemd_obj (identif) on delete cascade;
alter table woz_deelobj add constraint fk_wdo_as_6 foreign key (fk_6woz_nummer) references woz_obj (nummer) on delete cascade;

-- Foreign keys voor tabel woz_waarde
alter table woz_waarde add constraint fk_wrd_1n_1 foreign key (fk_1woz_nummer) references woz_obj (nummer) on delete cascade;

-- Foreign keys voor tabel zak_recht
alter table zak_recht add constraint fk_zkr_as_7 foreign key (fk_7koz_kad_identif) references kad_onrrnd_zk (kad_identif) on delete cascade;
alter table zak_recht add constraint fk_zkr_as_8 foreign key (fk_8pes_sc_identif) references prs (sc_identif) on delete cascade;
alter table zak_recht add constraint fk_zkr_rl_2 foreign key (fk_2aard_recht_verkort_aand) references aard_recht_verkort (aand) on delete cascade;
alter table zak_recht add constraint fk_zkr_rl_3 foreign key (fk_3avr_aand) references aard_verkregen_recht (aand) on delete cascade;

-- Foreign keys voor tabel zak_recht_aantek
alter table zak_recht_aantek add constraint fk_zra_as_5 foreign key (fk_5zkr_kadaster_identif) references zak_recht (kadaster_identif) on delete cascade;
alter table zak_recht_aantek add constraint fk_zra_as_6 foreign key (fk_6pes_sc_identif) references prs (sc_identif) on delete cascade;

-- Foreign keys voor tabel gebouwd_obj_gebruiksdoel
alter table gebouwd_obj_gebruiksdoel add constraint fk_gbo7 foreign key (fk_gbo_sc_identif) references gebouwd_obj (sc_identif) on delete cascade;

-- Foreign keys voor tabel overig_terrein_gebruiksdoel
alter table overig_terrein_gebruiksdoel add constraint fk_otr1 foreign key (fk_otr_sc_identif) references overig_terrein (sc_identif) on delete cascade;

-- Foreign keys voor tabel vestg_naam
alter table vestg_naam add constraint fk_ves1 foreign key (fk_ves_sc_identif) references vestg (sc_identif) on delete cascade;

-- Foreign keys voor tabel functionaris
alter table functionaris add constraint fk_functionaris_sc_lh foreign key (fk_sc_lh_pes_sc_identif) references prs (sc_identif) on delete cascade;
alter table functionaris add constraint fk_functionaris_sc_rh foreign key (fk_sc_rh_pes_sc_identif) references prs (sc_identif) on delete cascade;

-- Foreign keys voor tabel huishoudenrel
alter table huishoudenrel add constraint fk_huishoudenrel_sc_lh foreign key (fk_sc_lh_inp_sc_identif) references ingeschr_nat_prs (sc_identif) on delete cascade;
alter table huishoudenrel add constraint fk_huishoudenrel_sc_rh foreign key (fk_sc_rh_hhd_nummer) references huishouden (nummer) on delete cascade;

-- Foreign keys voor tabel huw_ger_partn
alter table huw_ger_partn add constraint fk_huw_ger_partn_sc_lh foreign key (fk_sc_lh_inp_sc_identif) references ingeschr_nat_prs (sc_identif) on delete cascade;
alter table huw_ger_partn add constraint fk_huw_ger_partn_sc_rh foreign key (fk_sc_rh_inp_sc_identif) references ingeschr_nat_prs (sc_identif) on delete cascade;
alter table huw_ger_partn add constraint fk_huw_ger_partn_hs_2 foreign key (fk_hs_lnd_code_iso) references land (code_iso) on delete cascade;
alter table huw_ger_partn add constraint fk_huw_ger_partn_ho_2 foreign key (fk_ho_lnd_code_iso) references land (code_iso) on delete cascade;

-- Foreign keys voor tabel kad_onrrnd_zk_his_rel
alter table kad_onrrnd_zk_his_rel add constraint fk_kad_onrrnd_zk_his_rel_sc_lh foreign key (fk_sc_lh_koz_kad_identif) references kad_onrrnd_zk (kad_identif) on delete cascade;
alter table kad_onrrnd_zk_his_rel add constraint fk_kad_onrrnd_zk_his_rel_sc_rh foreign key (fk_sc_rh_koz_kad_identif) references kad_onrrnd_zk (kad_identif) on delete cascade;

-- Foreign keys voor tabel locaand_adres
alter table locaand_adres add constraint fk_locaand_adres_sc_lh foreign key (fk_sc_lh_aoa_identif) references addresseerb_obj_aand (identif) on delete cascade;
alter table locaand_adres add constraint fk_locaand_adres_sc_rh foreign key (fk_sc_rh_woz_nummer) references woz_obj (nummer) on delete cascade;

-- Foreign keys voor tabel locaand_openb_rmte
alter table locaand_openb_rmte add constraint fk_locaand_openb_rmte_sc_lh foreign key (fk_sc_lh_opr_identifcode) references openb_rmte (identifcode) on delete cascade;
alter table locaand_openb_rmte add constraint fk_locaand_openb_rmte_sc_rh foreign key (fk_sc_rh_woz_nummer) references woz_obj (nummer) on delete cascade;

-- Foreign keys voor tabel ouder_kind_rel
alter table ouder_kind_rel add constraint fk_ouder_kind_rel_sc_lh foreign key (fk_sc_lh_inp_sc_identif) references ingeschr_nat_prs (sc_identif) on delete cascade;
alter table ouder_kind_rel add constraint fk_ouder_kind_rel_sc_rh foreign key (fk_sc_rh_inp_sc_identif) references ingeschr_nat_prs (sc_identif) on delete cascade;

-- Foreign keys voor tabel woz_belang
alter table woz_belang add constraint fk_woz_belang_sc_lh foreign key (fk_sc_lh_sub_identif) references subject (identif) on delete cascade;
alter table woz_belang add constraint fk_woz_belang_sc_rh foreign key (fk_sc_rh_woz_nummer) references woz_obj (nummer) on delete cascade;

-- Foreign keys voor tabel app_re_kad_perceel
alter table app_re_kad_perceel add constraint fk_apr_kdp_nn_lh foreign key (fk_nn_lh_apr_sc_kad_identif) references app_re (sc_kad_identif) on delete cascade;
alter table app_re_kad_perceel add constraint fk_apr_kdp_nn_rh foreign key (fk_nn_rh_kdp_sc_kad_identif) references kad_perceel (sc_kad_identif) on delete cascade;

-- Foreign keys voor tabel benoemd_obj_kad_onrrnd_zk
alter table benoemd_obj_kad_onrrnd_zk add constraint fk_tgo_koz_nn_lh foreign key (fk_nn_lh_tgo_identif) references benoemd_obj (identif) on delete cascade;
alter table benoemd_obj_kad_onrrnd_zk add constraint fk_tgo_koz_nn_rh foreign key (fk_nn_rh_koz_kad_identif) references kad_onrrnd_zk (kad_identif) on delete cascade;

-- Foreign keys voor tabel benoemd_terrein_benoemd_terrei
alter table benoemd_terrein_benoemd_terrei add constraint fk_btr_btr_nn_lh foreign key (fk_nn_lh_btr_sc_identif) references benoemd_terrein (sc_identif) on delete cascade;
alter table benoemd_terrein_benoemd_terrei add constraint fk_btr_btr_nn_rh foreign key (fk_nn_rh_btr_sc_identif) references benoemd_terrein (sc_identif) on delete cascade;

-- Foreign keys voor tabel gemeente_gemeente
alter table gemeente_gemeente add constraint fk_gem_gem_nn_lh foreign key (fk_nn_lh_gem_code) references gemeente (code) on delete cascade;
alter table gemeente_gemeente add constraint fk_gem_gem_nn_rh foreign key (fk_nn_rh_gem_code) references gemeente (code) on delete cascade;

-- Foreign keys voor tabel kad_onrrnd_zk_kad_onrrnd_zk
alter table kad_onrrnd_zk_kad_onrrnd_zk add constraint fk_koz_koz_nn_lh foreign key (fk_nn_lh_koz_kad_identif) references kad_onrrnd_zk (kad_identif) on delete cascade;
alter table kad_onrrnd_zk_kad_onrrnd_zk add constraint fk_koz_koz_nn_rh foreign key (fk_nn_rh_koz_kad_identif) references kad_onrrnd_zk (kad_identif) on delete cascade;

-- Foreign keys voor tabel ligplaats_nummeraand
alter table ligplaats_nummeraand add constraint fk_lpl_nra_nn_lh foreign key (fk_nn_lh_lpl_sc_identif) references ligplaats (sc_identif) on delete cascade;
alter table ligplaats_nummeraand add constraint fk_lpl_nra_nn_rh foreign key (fk_nn_rh_nra_sc_identif) references nummeraand (sc_identif) on delete cascade;

-- Foreign keys voor tabel openb_rmte_gem_openb_rmte
alter table openb_rmte_gem_openb_rmte add constraint fk_opr_gor_nn_lh foreign key (fk_nn_lh_opr_identifcode) references openb_rmte (identifcode) on delete cascade;
alter table openb_rmte_gem_openb_rmte add constraint fk_opr_gor_nn_rh foreign key (fk_nn_rh_gor_identifcode) references gem_openb_rmte (identifcode) on delete cascade;

-- Foreign keys voor tabel openb_rmte_wnplts
alter table openb_rmte_wnplts add constraint fk_opr_wpl_nn_lh foreign key (fk_nn_lh_opr_identifcode) references openb_rmte (identifcode) on delete cascade;
alter table openb_rmte_wnplts add constraint fk_opr_wpl_nn_rh foreign key (fk_nn_rh_wpl_identif) references wnplts (identif) on delete cascade;

-- Foreign keys voor tabel rsdoc_ingeschr_nat_prs
alter table rsdoc_ingeschr_nat_prs add constraint fk_rsd_inp_nn_lh foreign key (fk_nn_lh_rsd_nummer) references rsdoc (nummer) on delete cascade;
alter table rsdoc_ingeschr_nat_prs add constraint fk_rsd_inp_nn_rh foreign key (fk_nn_rh_inp_sc_identif) references ingeschr_nat_prs (sc_identif) on delete cascade;

-- Foreign keys voor tabel standplaats_nummeraand
alter table standplaats_nummeraand add constraint fk_spl_nra_nn_lh foreign key (fk_nn_lh_spl_sc_identif) references standplaats (sc_identif) on delete cascade;
alter table standplaats_nummeraand add constraint fk_spl_nra_nn_rh foreign key (fk_nn_rh_nra_sc_identif) references nummeraand (sc_identif) on delete cascade;

-- Foreign keys voor tabel verblijfsobj_pand
alter table verblijfsobj_pand add constraint fk_vbo_pnd_nn_lh foreign key (fk_nn_lh_vbo_sc_identif) references verblijfsobj (sc_identif) on delete cascade;
alter table verblijfsobj_pand add constraint fk_vbo_pnd_nn_rh foreign key (fk_nn_rh_pnd_identif) references pand (identif) on delete cascade;

-- Foreign keys voor tabel verblijfsobj_nummeraand
alter table verblijfsobj_nummeraand add constraint fk_vbo_nra_nn_lh foreign key (fk_nn_lh_vbo_sc_identif) references verblijfsobj (sc_identif) on delete cascade;
alter table verblijfsobj_nummeraand add constraint fk_vbo_nra_nn_rh foreign key (fk_nn_rh_nra_sc_identif) references nummeraand (sc_identif) on delete cascade;

-- Foreign keys voor tabel vestg_benoemd_obj
alter table vestg_benoemd_obj add constraint fk_ves_tgo_nn_lh foreign key (fk_nn_lh_ves_sc_identif) references vestg (sc_identif) on delete cascade;
alter table vestg_benoemd_obj add constraint fk_ves_tgo_nn_rh foreign key (fk_nn_rh_tgo_identif) references benoemd_obj (identif) on delete cascade;

-- Archief tabellen 

create table addresseerb_obj_aand_archief(
	dat_beg_geldh character varying(19),
	identif character varying(16),
	clazz character varying(255),
	dat_eind_geldh character varying(19),
	huisletter character varying(1),
	huinummer decimal(5,0),
	huinummertoevoeging character varying(4),
	postcode character varying(6),
	fk_6wpl_identif character varying(4),
	fk_7opr_identifcode character varying(16)
);
alter table addresseerb_obj_aand_archief add constraint ar_addresseerb_obj_aand_pk primary key(dat_beg_geldh,identif);
comment on table addresseerb_obj_aand_archief is 'RSGB class ADRESSEERBAAR OBJECT AANDUIDING. Directe superclass van: NUMMERAANDUIDING, OVERIGE ADRESSEERBAAR OBJECT AANDUIDING';
comment on column addresseerb_obj_aand_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid addresserbaar object aanduiding';
comment on column addresseerb_obj_aand_archief.identif is '[PK] AN16 - Identificatie adresseerbaar object aanduiding';
comment on column addresseerb_obj_aand_archief.clazz is 'Aanduiding subclass';
comment on column addresseerb_obj_aand_archief.dat_eind_geldh is 'OnvolledigeDatum - Datum einde geldigheid addresserbaar object aanduiding';
comment on column addresseerb_obj_aand_archief.huisletter is 'AN1 - Huisletter';
comment on column addresseerb_obj_aand_archief.huinummer is 'N5 - Huisnummer';
comment on column addresseerb_obj_aand_archief.huinummertoevoeging is 'AN4 - Huisnummertoevoeging';
comment on column addresseerb_obj_aand_archief.postcode is 'AN6 - Postcode';
comment on column addresseerb_obj_aand_archief.fk_6wpl_identif is '[FK] AN4, FK naar wnplts.identif: "ligt in"';
comment on column addresseerb_obj_aand_archief.fk_7opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode: "ligt aan"';

create table app_re_archief(
	sc_dat_beg_geldh character varying(19),
	sc_kad_identif decimal(15,0),
	fk_2nnp_sc_identif character varying(32),
	ka_appartementsindex character varying(4),
	ka_kad_gemeentecode character varying(5),
	ka_perceelnummer character varying(15),
	ka_sectie character varying(255)
);
alter table app_re_archief add constraint ar_app_re_pk primary key(sc_dat_beg_geldh,sc_kad_identif);
comment on table app_re_archief is 'RSGB class APPARTEMENTSRECHT. Subclass van: KADASTRALE ONROERENDE ZAAK';
comment on column app_re_archief.sc_dat_beg_geldh is '[PK] OnvolledigeDatum, FK naar kad_onrrnd_zk.dat_beg_geldh - Datum begin geldigheid kadastrale onroerende zaak';
comment on column app_re_archief.sc_kad_identif is '[PK] N15, FK naar kad_onrrnd_zk.kad_identif - Kadastrale identificatie';
comment on column app_re_archief.fk_2nnp_sc_identif is '[FK] AN32, FK naar niet_nat_prs.sc_identif (is FK naar superclass SUBJECT): "maakt deel uit van appartementencomplex met als vereniging van eigenaars"';
comment on column app_re_archief.ka_appartementsindex is 'Groepsattribuut Kadastrale aanduiding APPARTEMENTSRECHT.Appartementsindex - Appartementsindex';
comment on column app_re_archief.ka_kad_gemeentecode is 'Groepsattribuut Kadastrale aanduiding APPARTEMENTSRECHT.Kadastrale gemeentecode - Kadastrale gemeentecode';
comment on column app_re_archief.ka_perceelnummer is 'Groepsattribuut Kadastrale aanduiding APPARTEMENTSRECHT.Perceelnummer - Perceelnummer';
comment on column app_re_archief.ka_sectie is 'Groepsattribuut Kadastrale aanduiding APPARTEMENTSRECHT.Sectie - Sectie';

create table begr_terr_dl_archief(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	clazz character varying(255),
	datum_einde_geldh character varying(19),
	fysiek_voork_begr_terrein character varying(20),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	fk_8opr_identifcode character varying(16)
);
select addgeometrycolumn('begr_terr_dl_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index begr_terr_dl_archief_geom_idx on begr_terr_dl_archief USING GIST (geom);
select addgeometrycolumn('begr_terr_dl_archief', 'kruinlijngeom', 28992, 'LINESTRING', 2);
create index begr_terr_dl_archief_kruinlijngeom_idx on begr_terr_dl_archief USING GIST (kruinlijngeom);
alter table begr_terr_dl_archief add constraint ar_begr_terr_dl_pk primary key(dat_beg_geldh,identif);
comment on table begr_terr_dl_archief is 'RSGB class BEGROEID TERREINDEEL. Directe superclass van: BEGROEID TERREINVAKONDERDEEL';
comment on column begr_terr_dl_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid begroeid terreindeel';
comment on column begr_terr_dl_archief.identif is '[PK] NEN3610ID - Identificatie begroeid terreindeel';
comment on column begr_terr_dl_archief.clazz is 'Aanduiding subclass';
comment on column begr_terr_dl_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid begroeid terreindeel';
comment on column begr_terr_dl_archief.fysiek_voork_begr_terrein is 'AN20 - Fysiek voorkomen begroeid terrein';
comment on column begr_terr_dl_archief.geom is 'GM_Surface - Geometrie begroeid terreindeel';
comment on column begr_terr_dl_archief.kruinlijngeom is 'GM_Curve - Kruinlijneometrie begroeid terreindeel';
comment on column begr_terr_dl_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging begroeid terreindeel';
comment on column begr_terr_dl_archief.status is 'A8 - Status begroeid terreindeel';
comment on column begr_terr_dl_archief.fk_8opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode: "maakt deel uit van"';

create table begr_terreinvakonderd_archief(
	sc_dat_beg_geldh character varying(19),
	sc_identif character varying(255),
	fk_1btd_identif character varying(255)
);
alter table begr_terreinvakonderd_archief add constraint ar_begr_terreinvakonderd_pk primary key(sc_dat_beg_geldh,sc_identif);
comment on table begr_terreinvakonderd_archief is 'RSGB class BEGROEID TERREINVAKONDERDEEL. Subclass van: BEGROEID TERREINDEEL';
comment on column begr_terreinvakonderd_archief.sc_dat_beg_geldh is '[PK] OnvolledigeDatum, FK naar begr_terr_dl.dat_beg_geldh - Datum begin geldigheid begroeid terreindeel';
comment on column begr_terreinvakonderd_archief.sc_identif is '[PK] NEN3610ID, FK naar begr_terr_dl.identif - Identificatie begroeid terreindeel';
comment on column begr_terreinvakonderd_archief.fk_1btd_identif is '[FK] NEN3610ID, FK naar begr_terr_dl.identif';

create table benoemd_terrein_archief(
	dat_beg_geldh character varying(19),
	sc_identif character varying(16),
	clazz character varying(255),
	datum_einde_geldh character varying(19)
);
select addgeometrycolumn('benoemd_terrein_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index benoemd_terrein_archief_geom_idx on benoemd_terrein_archief USING GIST (geom);
alter table benoemd_terrein_archief add constraint ar_benoemd_terrein_pk primary key(dat_beg_geldh,sc_identif);
comment on table benoemd_terrein_archief is 'RSGB class BENOEMD TERREIN. Subclass van: BENOEMD OBJECT. Directe superclass van: LIGPLAATS, OVERIG TERREIN, STANDPLAATS';
comment on column benoemd_terrein_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid benoemd terrein';
comment on column benoemd_terrein_archief.sc_identif is '[PK] AN16, FK naar benoemd_obj.identif - Benoemd object identificatie';
comment on column benoemd_terrein_archief.clazz is 'Aanduiding subclass';
comment on column benoemd_terrein_archief.geom is 'GM_Surface - Benoemd terrein geometrie';
comment on column benoemd_terrein_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid benoemd terrein';

create table brugconstructie_elemen_archief(
	sc_dat_beg_geldh character varying(19),
	sc_identif character varying(255),
	type character varying(40)
);
alter table brugconstructie_elemen_archief add constraint ar_brugconstructie_element_pk primary key(sc_dat_beg_geldh,sc_identif);
comment on table brugconstructie_elemen_archief is 'RSGB class BRUGCONSTRUCTIE ELEMENT. Subclass van: KUNSTWERKDEEL';
comment on column brugconstructie_elemen_archief.sc_dat_beg_geldh is '[PK] OnvolledigeDatum, FK naar kunstwerkdeel.dat_beg_geldh - Datum begin geldigheid kunstwerkdeel';
comment on column brugconstructie_elemen_archief.sc_identif is '[PK] NEN3610ID, FK naar kunstwerkdeel.identif - Identificatie kunstwerkdeel';
comment on column brugconstructie_elemen_archief.type is 'AN40 - Type brugconstructie element';

create table buurt_archief(
	code decimal(2,0),
	dat_beg_geldh character varying(19),
	naam character varying(40),
	datum_einde_geldh character varying(19),
	identif_imgeobrt character varying(255),
	relve_hoogteligging decimal(1,0),
	status character varying(8)
);
select addgeometrycolumn('buurt_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index buurt_archief_geom_idx on buurt_archief USING GIST (geom);
alter table buurt_archief add constraint ar_buurt_pk primary key(code,dat_beg_geldh);
comment on table buurt_archief is 'RSGB class BUURT';
comment on column buurt_archief.code is '[PK] N2 - Buurtcode';
comment on column buurt_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid buurt';
comment on column buurt_archief.geom is 'GM_Surface - Buurtgeometrie';
comment on column buurt_archief.naam is 'AN40 - Buurtnaam';
comment on column buurt_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid buurt';
comment on column buurt_archief.identif_imgeobrt is 'NEN3610ID - Identificatie IMGeoBRT';
comment on column buurt_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging buurt';
comment on column buurt_archief.status is 'A8 - Status buurt';

create table functioneel_gebied_archief(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	naam character varying(40),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type character varying(255)
);
select addgeometrycolumn('functioneel_gebied_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index functioneel_gebied_archief_geom_idx on functioneel_gebied_archief USING GIST (geom);
alter table functioneel_gebied_archief add constraint ar_functioneel_gebied_pk primary key(dat_beg_geldh,identif);
comment on table functioneel_gebied_archief is 'RSGB class FUNCTIONEEL GEBIED';
comment on column functioneel_gebied_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid functioneel gebied';
comment on column functioneel_gebied_archief.identif is '[PK] NEN3610ID - Identificatie functioneel gebied';
comment on column functioneel_gebied_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid functioneel gebied';
comment on column functioneel_gebied_archief.geom is 'GM_Surface - Geometrie functioneel gebied';
comment on column functioneel_gebied_archief.naam is 'AN40 - Naam functioneel gebied';
comment on column functioneel_gebied_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging functioneel gebied';
comment on column functioneel_gebied_archief.status is 'A8 - Status functioneel gebied';
comment on column functioneel_gebied_archief.type is '[geen RSGB type] - Type functioneel gebied';

create table gebouwd_obj_archief(
	dat_beg_geldh character varying(19),
	sc_identif character varying(16),
	clazz character varying(255),
	bouwk_best_act character varying(255),
	bruto_inhoud decimal(6,0),
	datum_einde_geldh character varying(19),
	inwwijze_oppervlakte character varying(24),
	oppervlakte_obj decimal(6,0),
	status_voortgang_bouw integer
);
select addgeometrycolumn('gebouwd_obj_archief', 'vlakgeom', 28992, 'MULTIPOLYGON', 2);
create index gebouwd_obj_archief_vlakgeom_idx on gebouwd_obj_archief USING GIST (vlakgeom);
select addgeometrycolumn('gebouwd_obj_archief', 'puntgeom', 28992, 'POINT', 2);
create index gebouwd_obj_archief_puntgeom_idx on gebouwd_obj_archief USING GIST (puntgeom);
alter table gebouwd_obj_archief add constraint ar_gebouwd_obj_pk primary key(dat_beg_geldh,sc_identif);
comment on table gebouwd_obj_archief is 'RSGB class GEBOUWD OBJECT. Subclass van: BENOEMD OBJECT. Directe superclass van: OVERIG GEBOUWD OBJECT, VERBLIJFSOBJECT';
comment on column gebouwd_obj_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid gebouwd object';
comment on column gebouwd_obj_archief.sc_identif is '[PK] AN16, FK naar benoemd_obj.identif - Benoemd object identificatie';
comment on column gebouwd_obj_archief.clazz is 'Aanduiding subclass';
comment on column gebouwd_obj_archief.bouwk_best_act is '[Enumeratie] - Bouwkundige bestemming actueel';
comment on column gebouwd_obj_archief.bruto_inhoud is 'N6 - Bruto inhoud';
comment on column gebouwd_obj_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid gebouwd object';
comment on column gebouwd_obj_archief.vlakgeom is 'GM_Surface - Gebouwd object vlakgeometrie';
comment on column gebouwd_obj_archief.puntgeom is 'GM_Point - Gebouwd objectpuntgeometrie';
comment on column gebouwd_obj_archief.inwwijze_oppervlakte is 'AN24 - Inwinningswijze oppervlakte';
comment on column gebouwd_obj_archief.oppervlakte_obj is 'N6 - Oppervlakte (verblijfs)object';
comment on column gebouwd_obj_archief.status_voortgang_bouw is 'long - Status voortgang bouw';

create table gebouwinstallatie_archief(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type character varying(40)
);
select addgeometrycolumn('gebouwinstallatie_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index gebouwinstallatie_archief_geom_idx on gebouwinstallatie_archief USING GIST (geom);
alter table gebouwinstallatie_archief add constraint ar_gebouwinstallatie_pk primary key(dat_beg_geldh,identif);
comment on table gebouwinstallatie_archief is 'RSGB class GEBOUWINSTALLATIE';
comment on column gebouwinstallatie_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid gebouwinstallatie';
comment on column gebouwinstallatie_archief.identif is '[PK] NEN3610ID - Identificatie gebouwinstallatie';
comment on column gebouwinstallatie_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid gebouwinstallatie';
comment on column gebouwinstallatie_archief.geom is 'GM_Surface - Geometrie gebouwinstallatie';
comment on column gebouwinstallatie_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging gebouwinstallatie';
comment on column gebouwinstallatie_archief.status is 'A8 - Status gebouwinstallatie';
comment on column gebouwinstallatie_archief.type is 'AN40 - Type gebouwinstallatie';

create table gemeente_archief(
	dat_beg_geldh character varying(19),
	code decimal(4,0),
	datum_einde_geldh character varying(19),
	naam character varying(40),
	naam_nen character varying(24)
);
select addgeometrycolumn('gemeente_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index gemeente_archief_geom_idx on gemeente_archief USING GIST (geom);
alter table gemeente_archief add constraint ar_gemeente_pk primary key(dat_beg_geldh,code);
comment on table gemeente_archief is 'RSGB class GEMEENTE';
comment on column gemeente_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid gemeente';
comment on column gemeente_archief.code is '[PK] N4 - Gemeentecode';
comment on column gemeente_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid gemeente';
comment on column gemeente_archief.geom is 'GM_Surface - Gemeentegeometrie';
comment on column gemeente_archief.naam is 'AN40 - Gemeentenaam';
comment on column gemeente_archief.naam_nen is 'AN24 - Gemeentenaam NEN';

create table gem_openb_rmte_archief(
	dat_beg_geldh character varying(19),
	identifcode character varying(16),
	datum_einde_geldh character varying(19),
	indic_geconst_openb_rmte character varying(1),
	naam_openb_rmte character varying(80),
	status_openb_rmte character varying(80),
	straatnaam character varying(24),
	type_openb_rmte character varying(40),
	fk_7gem_code decimal(4,0)
);
alter table gem_openb_rmte_archief add constraint ar_gem_openb_rmte_pk primary key(dat_beg_geldh,identifcode);
comment on table gem_openb_rmte_archief is 'RSGB class GEMEENTELIJKE OPENBARE RUIMTE';
comment on column gem_openb_rmte_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid gemeentelijke openbare ruimte';
comment on column gem_openb_rmte_archief.identifcode is '[PK] AN16 - Identificatiecode gemeentelijke openbare ruimte';
comment on column gem_openb_rmte_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid gemeentelijke openbare ruimte';
comment on column gem_openb_rmte_archief.indic_geconst_openb_rmte is 'AN1 - Indicatie geconstateerde openbare ruimte';
comment on column gem_openb_rmte_archief.naam_openb_rmte is 'AN80 - Naam openbare ruimte';
comment on column gem_openb_rmte_archief.status_openb_rmte is 'AN80 - Status openbare ruimte';
comment on column gem_openb_rmte_archief.straatnaam is 'AN24 - Straatnaam';
comment on column gem_openb_rmte_archief.type_openb_rmte is 'AN40 - Type openbare ruimte';
comment on column gem_openb_rmte_archief.fk_7gem_code is '[FK] N4, FK naar gemeente.code: "ligt in"';

create table huishouden_archief(
	dat_beg_geldh character varying(19),
	nummer decimal(12,0),
	datum_einde_geldh character varying(19),
	grootte decimal(2,0),
	soort decimal(2,0),
	fk_4vbo_sc_identif character varying(16),
	fk_5lpl_sc_identif character varying(16),
	fk_7spl_sc_identif character varying(16)
);
alter table huishouden_archief add constraint ar_huishouden_pk primary key(dat_beg_geldh,nummer);
comment on table huishouden_archief is 'RSGB class HUISHOUDEN';
comment on column huishouden_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid huishouden';
comment on column huishouden_archief.nummer is '[PK] N12 - Huishoudennummer';
comment on column huishouden_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid huishouden';
comment on column huishouden_archief.grootte is 'N2 - Huishoudengrootte';
comment on column huishouden_archief.soort is 'N2 - Huishoudensoort';
comment on column huishouden_archief.fk_4vbo_sc_identif is '[FK] AN16, FK naar verblijfsobj.sc_identif (is FK naar superclass BENOEMD OBJECT): "is gehuisvest in"';
comment on column huishouden_archief.fk_5lpl_sc_identif is '[FK] AN16, FK naar ligplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "is gehuisvest op"';
comment on column huishouden_archief.fk_7spl_sc_identif is '[FK] AN16, FK naar standplaats.sc_identif (is FK naar superclass BENOEMD OBJECT): "is gehuisvest op"';

create table inrichtingselement_archief(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	typering character varying(40),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type character varying(30)
);
select addgeometrycolumn('inrichtingselement_archief', 'geom', 28992, 'GEOMETRY', 2);
create index inrichtingselement_archief_geom_idx on inrichtingselement_archief USING GIST (geom);
alter table inrichtingselement_archief add constraint ar_inrichtingselement_pk primary key(dat_beg_geldh,identif);
comment on table inrichtingselement_archief is 'RSGB class INRICHTINGSELEMENT';
comment on column inrichtingselement_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid inrichtingselement';
comment on column inrichtingselement_archief.identif is '[PK] NEN3610ID - Identificatie inrichtingselement';
comment on column inrichtingselement_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid inrichtingselement';
comment on column inrichtingselement_archief.geom is 'PuntLijnVlak - Geometrie inrichtingselement';
comment on column inrichtingselement_archief.typering is 'AN40 - Inrichtingselementtypering';
comment on column inrichtingselement_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging inrichtingselement';
comment on column inrichtingselement_archief.status is 'AN8 - Status inrichtingselement';
comment on column inrichtingselement_archief.type is 'AN30 - Type inrichtingselement';

create table kad_perceel_archief(
	sc_dat_beg_geldh character varying(19),
	sc_kad_identif decimal(15,0),
	aand_soort_grootte char(1),
	grootte_perceel decimal(8,0),
	omschr_deelperceel character varying(1120),
	fk_7kdp_sc_kad_identif decimal(15,0),
	ka_deelperceelnummer character varying(4),
	ka_kad_gemeentecode character varying(5),
	ka_perceelnummer character varying(5),
	ka_sectie character varying(255)
);
select addgeometrycolumn('kad_perceel_archief', 'begrenzing_perceel', 28992, 'MULTIPOLYGON', 2);
create index kad_perceel_archief_begrenzing_perceel_idx on kad_perceel_archief USING GIST (begrenzing_perceel);
select addgeometrycolumn('kad_perceel_archief', 'plaatscoordinaten_perceel', 28992, 'POINT', 2);
create index kad_perceel_archief_plaatscoordinaten_perceel_idx on kad_perceel_archief USING GIST (plaatscoordinaten_perceel);
alter table kad_perceel_archief add constraint ar_kad_perceel_pk primary key(sc_dat_beg_geldh,sc_kad_identif);
comment on table kad_perceel_archief is 'RSGB class KADASTRAAL PERCEEL. Subclass van: KADASTRALE ONROERENDE ZAAK';
comment on column kad_perceel_archief.sc_dat_beg_geldh is '[PK] OnvolledigeDatum, FK naar kad_onrrnd_zk.dat_beg_geldh - Datum begin geldigheid kadastrale onroerende zaak';
comment on column kad_perceel_archief.sc_kad_identif is '[PK] N15, FK naar kad_onrrnd_zk.kad_identif - Kadastrale identificatie';
comment on column kad_perceel_archief.aand_soort_grootte is 'boolean - Aanduiding soort grootte';
comment on column kad_perceel_archief.begrenzing_perceel is 'GM_Surface - Begrenzing perceel';
comment on column kad_perceel_archief.grootte_perceel is 'N8 - Grootte perceel';
comment on column kad_perceel_archief.omschr_deelperceel is 'AN1120 - Omschrijving deelperceel';
comment on column kad_perceel_archief.plaatscoordinaten_perceel is 'GM_Point - Plaatscoördinaten perceel';
comment on column kad_perceel_archief.fk_7kdp_sc_kad_identif is '[FK] N15, FK naar kad_perceel.sc_kad_identif (is FK naar superclass KADASTRALE ONROERENDE ZAAK): "ligt binnen"';
comment on column kad_perceel_archief.ka_deelperceelnummer is 'Groepsattribuut Kadastrale aanduiding KADASTRAAL PERCEEL.Deelperceelnummer - Deelperceelnummer';
comment on column kad_perceel_archief.ka_kad_gemeentecode is 'Groepsattribuut Kadastrale aanduiding KADASTRAAL PERCEEL.Kadastrale gemeentecode - Kadastrale gemeentecode';
comment on column kad_perceel_archief.ka_perceelnummer is 'Groepsattribuut Kadastrale aanduiding KADASTRAAL PERCEEL.Perceelnummer - Perceelnummer';
comment on column kad_perceel_archief.ka_sectie is 'Groepsattribuut Kadastrale aanduiding KADASTRAAL PERCEEL.Sectie - Sectie';

create table kad_onrrnd_zk_archief(
	dat_beg_geldh character varying(19),
	kad_identif decimal(15,0),
	clazz character varying(255),
	datum_einde_geldh character varying(19),
	typering character varying(1),
	fk_7kdg_code character varying(5),
	fk_10pes_sc_identif character varying(32),
	cu_aard_bebouwing character varying(255),
	cu_aard_cultuur_onbebouwd character varying(65),
	cu_meer_culturen char(1),
	ks_aard_bedrag character varying(255),
	ks_bedrag decimal(9,0),
	ks_koopjaar integer,
	ks_meer_onroerendgoed char(1),
	ks_transactiedatum timestamp,
	ks_valutasoort character varying(255),
	lr_aand_aard_liproject character varying(1),
	lr_aard_bedrag character varying(255),
	lr_bedrag character varying(255),
	lr_eindjaar integer,
	lr_valutasoort character varying(3),
	lo_cultuur_bebouwd character varying(65),
	lo_loc__omschr character varying(40)
);
alter table kad_onrrnd_zk_archief add constraint ar_kad_onrrnd_zk_pk primary key(dat_beg_geldh,kad_identif);
comment on table kad_onrrnd_zk_archief is 'RSGB class KADASTRALE ONROERENDE ZAAK. Directe superclass van: APPARTEMENTSRECHT, KADASTRAAL PERCEEL';
comment on column kad_onrrnd_zk_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid kadastrale onroerende zaak';
comment on column kad_onrrnd_zk_archief.kad_identif is '[PK] N15 - Kadastrale identificatie';
comment on column kad_onrrnd_zk_archief.clazz is 'Aanduiding subclass';
comment on column kad_onrrnd_zk_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid kadastrale onroerende zaak';
comment on column kad_onrrnd_zk_archief.typering is 'AN1 - Kadastrale onroerende zaak typering';
comment on column kad_onrrnd_zk_archief.fk_7kdg_code is '[FK] AN5, FK naar kad_gemeente.code: "ligt in"';
comment on column kad_onrrnd_zk_archief.fk_10pes_sc_identif is '[FK] AN32, FK naar prs.sc_identif (is FK naar superclass SUBJECT): "heeft als voornaamste zakelijk gerechtigde"';
comment on column kad_onrrnd_zk_archief.cu_aard_bebouwing is 'Groepsattribuut Cultuur onbebouwd KADASTRALE ONROERENDE ZAAK.Aard bebouwing - Aard bebouwing';
comment on column kad_onrrnd_zk_archief.cu_aard_cultuur_onbebouwd is 'Groepsattribuut Cultuur onbebouwd KADASTRALE ONROERENDE ZAAK.Aard cultuur onbebouwd - Aard cultuur onbebouwd';
comment on column kad_onrrnd_zk_archief.cu_meer_culturen is 'Groepsattribuut Cultuur onbebouwd KADASTRALE ONROERENDE ZAAK.Meer culturen - Meer culturen';
comment on column kad_onrrnd_zk_archief.ks_aard_bedrag is 'Groepsattribuut Koopsom KADASTRALE ONROERENDE ZAAK.Aard bedrag - Aard bedrag';
comment on column kad_onrrnd_zk_archief.ks_bedrag is 'Groepsattribuut Koopsom KADASTRALE ONROERENDE ZAAK.Bedrag - Bedrag';
comment on column kad_onrrnd_zk_archief.ks_koopjaar is 'Groepsattribuut Koopsom KADASTRALE ONROERENDE ZAAK.Koopjaar - Koopjaar';
comment on column kad_onrrnd_zk_archief.ks_meer_onroerendgoed is 'Groepsattribuut Koopsom KADASTRALE ONROERENDE ZAAK.Meer onroerendgoed - Meer onroerendgoed';
comment on column kad_onrrnd_zk_archief.ks_transactiedatum is 'Groepsattribuut Koopsom KADASTRALE ONROERENDE ZAAK.Transactiedatum - Transactiedatum';
comment on column kad_onrrnd_zk_archief.ks_valutasoort is 'Groepsattribuut Koopsom KADASTRALE ONROERENDE ZAAK.Valutasoort - Valutasoort';
comment on column kad_onrrnd_zk_archief.lr_aand_aard_liproject is 'Groepsattribuut Landinrichtingsrente KADASTRALE ONROERENDE ZAAK.Aanduiding aard LIproject - Aanduiding aard LIproject';
comment on column kad_onrrnd_zk_archief.lr_aard_bedrag is 'Groepsattribuut Landinrichtingsrente KADASTRALE ONROERENDE ZAAK.Aard bedrag - Aard bedrag';
comment on column kad_onrrnd_zk_archief.lr_bedrag is 'Groepsattribuut Landinrichtingsrente KADASTRALE ONROERENDE ZAAK.Bedrag - Bedrag';
comment on column kad_onrrnd_zk_archief.lr_eindjaar is 'Groepsattribuut Landinrichtingsrente KADASTRALE ONROERENDE ZAAK.Eindjaar - Eindjaar';
comment on column kad_onrrnd_zk_archief.lr_valutasoort is 'Groepsattribuut Landinrichtingsrente KADASTRALE ONROERENDE ZAAK.Valutasoort - Valutasoort';
comment on column kad_onrrnd_zk_archief.lo_cultuur_bebouwd is 'Groepsattribuut Locatie onroerende zaak KADASTRALE ONROERENDE ZAAK.Cultuur bebouwd - Cultuur bebouwd';
comment on column kad_onrrnd_zk_archief.lo_loc__omschr is 'Groepsattribuut Locatie onroerende zaak KADASTRALE ONROERENDE ZAAK.Locatie- omschrijving - Locatie- omschrijving';

create table kad_onrrnd_zk_aantek_archief(
	begindatum_aantek_kad_obj character varying(19),
	kadaster_identif_aantek character varying(255),
	aard_aantek_kad_obj character varying(255),
	beschrijving_aantek_kad_obj character varying(124),
	eindd_aantek_kad_obj character varying(19),
	fk_4koz_kad_identif decimal(15,0),
	fk_5pes_sc_identif character varying(32)
);
alter table kad_onrrnd_zk_aantek_archief add constraint ar_kad_onrrnd_zk_aantek_pk primary key(begindatum_aantek_kad_obj,kadaster_identif_aantek);
comment on table kad_onrrnd_zk_aantek_archief is 'RSGB class KADASTRALE ONROERENDE ZAAK AANTEKENING';
comment on column kad_onrrnd_zk_aantek_archief.begindatum_aantek_kad_obj is '[PK] OnvolledigeDatum - Begindatum aantekening kadastraal object';
comment on column kad_onrrnd_zk_aantek_archief.kadaster_identif_aantek is '[PK] AN255 - Kadaster identificatie aantekening';
comment on column kad_onrrnd_zk_aantek_archief.aard_aantek_kad_obj is '[Enumeratie] - Aard aantekening kadastraal object';
comment on column kad_onrrnd_zk_aantek_archief.beschrijving_aantek_kad_obj is 'AN124 - Beschrijving aantekening kadastraal object';
comment on column kad_onrrnd_zk_aantek_archief.eindd_aantek_kad_obj is 'OnvolledigeDatum - Einddatum aantekening kadastraal object';
comment on column kad_onrrnd_zk_aantek_archief.fk_4koz_kad_identif is '[FK] N15, FK naar kad_onrrnd_zk.kad_identif: "behoort bij"';
comment on column kad_onrrnd_zk_aantek_archief.fk_5pes_sc_identif is '[FK] AN32, FK naar prs.sc_identif (is FK naar superclass SUBJECT): "heeft betrokken"';

create table kunstwerkdeel_archief(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	clazz character varying(255),
	datum_einde_geldh character varying(19),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type_kunstwerk character varying(40)
);
select addgeometrycolumn('kunstwerkdeel_archief', 'geom', 28992, 'GEOMETRY', 2);
create index kunstwerkdeel_archief_geom_idx on kunstwerkdeel_archief USING GIST (geom);
alter table kunstwerkdeel_archief add constraint ar_kunstwerkdeel_pk primary key(dat_beg_geldh,identif);
comment on table kunstwerkdeel_archief is 'RSGB class KUNSTWERKDEEL. Directe superclass van: BRUGCONSTRUCTIE ELEMENT';
comment on column kunstwerkdeel_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid kunstwerkdeel';
comment on column kunstwerkdeel_archief.identif is '[PK] NEN3610ID - Identificatie kunstwerkdeel';
comment on column kunstwerkdeel_archief.clazz is 'Aanduiding subclass';
comment on column kunstwerkdeel_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid kunstwerkdeel';
comment on column kunstwerkdeel_archief.geom is 'LijnVlak - Geometrie kunstwerkdeel';
comment on column kunstwerkdeel_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging kunstwerkdeel';
comment on column kunstwerkdeel_archief.status is 'A8 - Status kunstwerkdeel';
comment on column kunstwerkdeel_archief.type_kunstwerk is 'AN40 - Type kunstwerk';

create table ligplaats_archief(
	sc_dat_beg_geldh character varying(19),
	sc_identif character varying(16),
	indic_geconst character varying(1),
	status character varying(80),
	fk_4nra_sc_identif character varying(16)
);
alter table ligplaats_archief add constraint ar_ligplaats_pk primary key(sc_dat_beg_geldh,sc_identif);
comment on table ligplaats_archief is 'RSGB class LIGPLAATS. Subclass van: BENOEMD TERREIN -> BENOEMD OBJECT';
comment on column ligplaats_archief.sc_dat_beg_geldh is '[PK] OnvolledigeDatum, FK naar benoemd_terrein.dat_beg_geldh - Datum begin geldigheid benoemd terrein';
comment on column ligplaats_archief.sc_identif is '[PK] AN16, FK naar benoemd_terrein.sc_identif - Benoemd object identificatie';
comment on column ligplaats_archief.indic_geconst is 'AN1 - Indicatie geconstateerde ligplaats';
comment on column ligplaats_archief.status is 'AN80 - Ligplaatsstatus';
comment on column ligplaats_archief.fk_4nra_sc_identif is '[FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "heeft als hoofdadres"';

create table nummeraand_archief(
	sc_dat_beg_geldh character varying(19),
	sc_identif character varying(16),
	indic_geconst character varying(1),
	indic_hoofdadres char(1),
	status character varying(80)
);
alter table nummeraand_archief add constraint ar_nummeraand_pk primary key(sc_dat_beg_geldh,sc_identif);
comment on table nummeraand_archief is 'RSGB class NUMMERAANDUIDING. Subclass van: ADRESSEERBAAR OBJECT AANDUIDING';
comment on column nummeraand_archief.sc_dat_beg_geldh is '[PK] OnvolledigeDatum, FK naar addresseerb_obj_aand.dat_beg_geldh - Datum begin geldigheid addresserbaar object aanduiding';
comment on column nummeraand_archief.sc_identif is '[PK] AN16, FK naar addresseerb_obj_aand.identif - Identificatie adresseerbaar object aanduiding';
comment on column nummeraand_archief.indic_geconst is 'AN1 - Indicatie geconstateerde nummeraanduiding';
comment on column nummeraand_archief.indic_hoofdadres is 'boolean - Indicatie hoofdadres';
comment on column nummeraand_archief.status is 'AN80 - Nummeraanduidingstatus';

create table onbegr_terr_dl_archief(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	clazz character varying(255),
	datum_einde_geldh character varying(19),
	fysiek_voork_onbegr_terrein character varying(20),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	fk_8opr_identifcode character varying(16)
);
select addgeometrycolumn('onbegr_terr_dl_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index onbegr_terr_dl_archief_geom_idx on onbegr_terr_dl_archief USING GIST (geom);
select addgeometrycolumn('onbegr_terr_dl_archief', 'kruinlijngeom', 28992, 'LINESTRING', 2);
create index onbegr_terr_dl_archief_kruinlijngeom_idx on onbegr_terr_dl_archief USING GIST (kruinlijngeom);
alter table onbegr_terr_dl_archief add constraint ar_onbegr_terr_dl_pk primary key(dat_beg_geldh,identif);
comment on table onbegr_terr_dl_archief is 'RSGB class ONBEGROEID TERREINDEEL. Directe superclass van: ONBEGROEID TERREINVAKONDERDEEL';
comment on column onbegr_terr_dl_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid onbegroeid terreindeel';
comment on column onbegr_terr_dl_archief.identif is '[PK] NEN3610ID - Identificatie onbegroeid terreindeel';
comment on column onbegr_terr_dl_archief.clazz is 'Aanduiding subclass';
comment on column onbegr_terr_dl_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid onbegroeid terreindeel';
comment on column onbegr_terr_dl_archief.fysiek_voork_onbegr_terrein is 'AN20 - Fysiek voorkomen onbegroeid terrein';
comment on column onbegr_terr_dl_archief.geom is 'GM_Surface - Geometrie onbegroeid terreindeel';
comment on column onbegr_terr_dl_archief.kruinlijngeom is 'GM_Curve - Kruinlijngeometrie onbegroeid terreindeel';
comment on column onbegr_terr_dl_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging onbegroeid terreindeel';
comment on column onbegr_terr_dl_archief.status is 'A8 - Status onbegroeid terreindeel';
comment on column onbegr_terr_dl_archief.fk_8opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode: "maakt deel uit van"';

create table onbegr_terreinvakonder_archief(
	sc_dat_beg_geldh character varying(19),
	sc_identif character varying(255),
	fk_1obt_identif character varying(255)
);
alter table onbegr_terreinvakonder_archief add constraint ar_onbegr_terreinvakonderd_pk primary key(sc_dat_beg_geldh,sc_identif);
comment on table onbegr_terreinvakonder_archief is 'RSGB class ONBEGROEID TERREINVAKONDERDEEL. Subclass van: ONBEGROEID TERREINDEEL';
comment on column onbegr_terreinvakonder_archief.sc_dat_beg_geldh is '[PK] OnvolledigeDatum, FK naar onbegr_terr_dl.dat_beg_geldh - Datum begin geldigheid onbegroeid terreindeel';
comment on column onbegr_terreinvakonder_archief.sc_identif is '[PK] NEN3610ID, FK naar onbegr_terr_dl.identif - Identificatie onbegroeid terreindeel';
comment on column onbegr_terreinvakonder_archief.fk_1obt_identif is '[FK] NEN3610ID, FK naar onbegr_terr_dl.identif';

create table ondersteunend_wegdeel_archief(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	functie character varying(25),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	fk_6opr_identifcode character varying(16)
);
select addgeometrycolumn('ondersteunend_wegdeel_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index ondersteunend_wegdeel_archief_geom_idx on ondersteunend_wegdeel_archief USING GIST (geom);
alter table ondersteunend_wegdeel_archief add constraint ar_ondersteunend_wegdeel_pk primary key(dat_beg_geldh,identif);
comment on table ondersteunend_wegdeel_archief is 'RSGB class ONDERSTEUNEND WEGDEEL';
comment on column ondersteunend_wegdeel_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid ondersteunend wegdeel';
comment on column ondersteunend_wegdeel_archief.identif is '[PK] NEN3610ID - Identificatie ondersteunend wegdeel';
comment on column ondersteunend_wegdeel_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid ondersteunend wegdeel';
comment on column ondersteunend_wegdeel_archief.functie is 'AN25 - Functie ondersteunend wegdeel';
comment on column ondersteunend_wegdeel_archief.geom is 'GM_Surface - Geometrie ondersteunend wegdeel';
comment on column ondersteunend_wegdeel_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging ondersteunend wegdeel';
comment on column ondersteunend_wegdeel_archief.status is 'A8 - Status ondersteunend wegdeel';
comment on column ondersteunend_wegdeel_archief.fk_6opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode: "maakt deel uit van"';

create table ovrg_addresseerb_obj_a_archief(
	sc_dat_beg_geldh character varying(19),
	sc_identif character varying(16)
);
alter table ovrg_addresseerb_obj_a_archief add constraint ar_ovrg_addresseerb_obj_aan_pk primary key(sc_dat_beg_geldh,sc_identif);
comment on table ovrg_addresseerb_obj_a_archief is 'RSGB class OVERIGE ADRESSEERBAAR OBJECT AANDUIDING. Subclass van: ADRESSEERBAAR OBJECT AANDUIDING';
comment on column ovrg_addresseerb_obj_a_archief.sc_dat_beg_geldh is '[PK] OnvolledigeDatum, FK naar addresseerb_obj_aand.dat_beg_geldh - Datum begin geldigheid addresserbaar object aanduiding';
comment on column ovrg_addresseerb_obj_a_archief.sc_identif is '[PK] AN16, FK naar addresseerb_obj_aand.identif - Identificatie adresseerbaar object aanduiding';

create table overig_bouwwerk_archief(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type character varying(40)
);
select addgeometrycolumn('overig_bouwwerk_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index overig_bouwwerk_archief_geom_idx on overig_bouwwerk_archief USING GIST (geom);
alter table overig_bouwwerk_archief add constraint ar_overig_bouwwerk_pk primary key(dat_beg_geldh,identif);
comment on table overig_bouwwerk_archief is 'RSGB class OVERIG BOUWWERK';
comment on column overig_bouwwerk_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid overig bouwwerk';
comment on column overig_bouwwerk_archief.identif is '[PK] NEN3610ID - Identificatie overig bouwwerk';
comment on column overig_bouwwerk_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid overig bouwwerk';
comment on column overig_bouwwerk_archief.geom is 'GM_Surface - Geometrie overig bouwwerk';
comment on column overig_bouwwerk_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging overig bouwwerk';
comment on column overig_bouwwerk_archief.status is 'A8 - Status overig bouwwerk';
comment on column overig_bouwwerk_archief.type is 'AN40 - Type overig bouwwerk';

create table overig_gebouwd_obj_archief(
	sc_dat_beg_geldh character varying(19),
	sc_identif character varying(16),
	bouwjaar decimal(4,0),
	loc_aand character varying(40),
	type character varying(40),
	fk_4oao_sc_identif character varying(16),
	fk_5nra_sc_identif character varying(16),
	fk_6opr_identifcode character varying(16)
);
alter table overig_gebouwd_obj_archief add constraint ar_overig_gebouwd_obj_pk primary key(sc_dat_beg_geldh,sc_identif);
comment on table overig_gebouwd_obj_archief is 'RSGB class OVERIG GEBOUWD OBJECT. Subclass van: GEBOUWD OBJECT -> BENOEMD OBJECT';
comment on column overig_gebouwd_obj_archief.sc_dat_beg_geldh is '[PK] OnvolledigeDatum, FK naar gebouwd_obj.dat_beg_geldh - Datum begin geldigheid gebouwd object';
comment on column overig_gebouwd_obj_archief.sc_identif is '[PK] AN16, FK naar gebouwd_obj.sc_identif - Benoemd object identificatie';
comment on column overig_gebouwd_obj_archief.bouwjaar is 'N4 - Bouwjaar';
comment on column overig_gebouwd_obj_archief.loc_aand is 'AN40 - Overig gebouwd object locatie-aanduiding';
comment on column overig_gebouwd_obj_archief.type is 'AN40 - Overig gebouwd object type';
comment on column overig_gebouwd_obj_archief.fk_4oao_sc_identif is '[FK] AN16, FK naar ovrg_addresseerb_obj_aand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "heeft als officieel adres"';
comment on column overig_gebouwd_obj_archief.fk_5nra_sc_identif is '[FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "heeft locatie-adres i.c.m."';
comment on column overig_gebouwd_obj_archief.fk_6opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode: "heeft straatadres i.c.m."';

create table ovrg_scheiding_archief(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type character varying(40)
);
select addgeometrycolumn('ovrg_scheiding_archief', 'geom', 28992, 'GEOMETRY', 2);
create index ovrg_scheiding_archief_geom_idx on ovrg_scheiding_archief USING GIST (geom);
alter table ovrg_scheiding_archief add constraint ar_ovrg_scheiding_pk primary key(dat_beg_geldh,identif);
comment on table ovrg_scheiding_archief is 'RSGB class OVERIGE SCHEIDING';
comment on column ovrg_scheiding_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid overige scheiding';
comment on column ovrg_scheiding_archief.identif is '[PK] NEN3610ID - Identificatie overige scheiding';
comment on column ovrg_scheiding_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid overige scheiding';
comment on column ovrg_scheiding_archief.geom is 'LijnVlak - Geometrie overige scheiding';
comment on column ovrg_scheiding_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging overige scheiding';
comment on column ovrg_scheiding_archief.status is 'A8 - Status overige scheiding';
comment on column ovrg_scheiding_archief.type is 'AN40 - Type overige scheiding';

create table overig_terrein_archief(
	sc_dat_beg_geldh character varying(19),
	sc_identif character varying(16),
	fk_2oao_sc_identif character varying(16)
);
alter table overig_terrein_archief add constraint ar_overig_terrein_pk primary key(sc_dat_beg_geldh,sc_identif);
comment on table overig_terrein_archief is 'RSGB class OVERIG TERREIN. Subclass van: BENOEMD TERREIN -> BENOEMD OBJECT';
comment on column overig_terrein_archief.sc_dat_beg_geldh is '[PK] OnvolledigeDatum, FK naar benoemd_terrein.dat_beg_geldh - Datum begin geldigheid benoemd terrein';
comment on column overig_terrein_archief.sc_identif is '[PK] AN16, FK naar benoemd_terrein.sc_identif - Benoemd object identificatie';
comment on column overig_terrein_archief.fk_2oao_sc_identif is '[FK] AN16, FK naar ovrg_addresseerb_obj_aand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "heeft als officieel adres"';

create table pand_archief(
	dat_beg_geldh character varying(19),
	identif character varying(16),
	bruto_inhoud decimal(6,0),
	datum_einde_geldh character varying(19),
	hoogste_bouwlaag decimal(3,0),
	identif_bgtpnd character varying(255),
	indic_geconstateerd character varying(1),
	inwwijze_geom_bovenaanzicht character varying(24),
	inwwijze_geom_maaiveld character varying(24),
	laagste_bouwlaag decimal(3,0),
	oorspronkelijk_bouwjaar decimal(4,0),
	oppervlakte decimal(6,0),
	status character varying(80),
	relve_hoogteligging decimal(1,0),
	status_voortgang_bouw character varying(24)
);
select addgeometrycolumn('pand_archief', 'geom_bovenaanzicht', 28992, 'MULTIPOLYGON', 2);
create index pand_archief_geom_bovenaanzicht_idx on pand_archief USING GIST (geom_bovenaanzicht);
select addgeometrycolumn('pand_archief', 'geom_maaiveld', 28992, 'MULTIPOLYGON', 2);
create index pand_archief_geom_maaiveld_idx on pand_archief USING GIST (geom_maaiveld);
alter table pand_archief add constraint ar_pand_pk primary key(dat_beg_geldh,identif);
comment on table pand_archief is 'RSGB class PAND';
comment on column pand_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid pand';
comment on column pand_archief.identif is '[PK] AN16 - Pandidentificatie';
comment on column pand_archief.bruto_inhoud is 'N6 - Bruto inhoud pand';
comment on column pand_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid pand';
comment on column pand_archief.hoogste_bouwlaag is 'N3 - Hoogste bouwlaag pand';
comment on column pand_archief.identif_bgtpnd is 'NEN3610ID - Identificatie BGTPND';
comment on column pand_archief.indic_geconstateerd is 'AN1 - Indicatie geconstateerd pand';
comment on column pand_archief.inwwijze_geom_bovenaanzicht is 'AN24 - Inwinningswijze geometrie bovenaanzicht';
comment on column pand_archief.inwwijze_geom_maaiveld is 'AN24 - Inwinningswijze geometrie maaiveld';
comment on column pand_archief.laagste_bouwlaag is 'N3 - Laagste bouwlaag pand';
comment on column pand_archief.oorspronkelijk_bouwjaar is 'N4 - Oorspronkelijk bouwjaar pand';
comment on column pand_archief.oppervlakte is 'N6 - Oppervlakte pand';
comment on column pand_archief.geom_bovenaanzicht is 'GM_Surface - Pandgeometrie bovenaanzicht';
comment on column pand_archief.geom_maaiveld is 'GM_MultiSurface - Pandgeometrie maaiveld';
comment on column pand_archief.status is 'AN80 - Pandstatus';
comment on column pand_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging pand';
comment on column pand_archief.status_voortgang_bouw is 'AN24 - Status voortgang bouw';

create table scheiding_archief(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type character varying(40)
);
select addgeometrycolumn('scheiding_archief', 'geom', 28992, 'GEOMETRY', 2);
create index scheiding_archief_geom_idx on scheiding_archief USING GIST (geom);
alter table scheiding_archief add constraint ar_scheiding_pk primary key(dat_beg_geldh,identif);
comment on table scheiding_archief is 'RSGB class SCHEIDING';
comment on column scheiding_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid scheiding';
comment on column scheiding_archief.identif is '[PK] NEN3610ID - Identificatie scheiding';
comment on column scheiding_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid scheiding';
comment on column scheiding_archief.geom is 'LijnVlak - Geometrie scheiding';
comment on column scheiding_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging scheiding';
comment on column scheiding_archief.status is 'A8 - Status scheiding';
comment on column scheiding_archief.type is 'AN40 - Type scheiding';

create table spoor_archief(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	functie character varying(25),
	relve_hoogteligging decimal(1,0),
	status character varying(8)
);
select addgeometrycolumn('spoor_archief', 'geom', 28992, 'LINESTRING', 2);
create index spoor_archief_geom_idx on spoor_archief USING GIST (geom);
alter table spoor_archief add constraint ar_spoor_pk primary key(dat_beg_geldh,identif);
comment on table spoor_archief is 'RSGB class SPOOR';
comment on column spoor_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid spoor';
comment on column spoor_archief.identif is '[PK] NEN3610ID - Identificatie spoor';
comment on column spoor_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid spoor';
comment on column spoor_archief.functie is 'AN25 - Functie spoor';
comment on column spoor_archief.geom is 'GM_Curve - Geometrie spoor';
comment on column spoor_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging spoor';
comment on column spoor_archief.status is 'A8 - Status spoor';

create table stadsdeel_archief(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	relve_hoogteligging decimal(1,0),
	naam character varying(40),
	status character varying(8)
);
select addgeometrycolumn('stadsdeel_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index stadsdeel_archief_geom_idx on stadsdeel_archief USING GIST (geom);
alter table stadsdeel_archief add constraint ar_stadsdeel_pk primary key(dat_beg_geldh,identif);
comment on table stadsdeel_archief is 'RSGB class STADSDEEL';
comment on column stadsdeel_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid stadsdeel';
comment on column stadsdeel_archief.identif is '[PK] NEN3610ID - Identificatie stadsdeel';
comment on column stadsdeel_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid stadsdeel';
comment on column stadsdeel_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging stadsdeel';
comment on column stadsdeel_archief.geom is 'GM_Surface - Stadsdeelgeometrie';
comment on column stadsdeel_archief.naam is 'AN40 - Stadsdeelnaam';
comment on column stadsdeel_archief.status is 'A8 - Status stadsdeel';

create table standplaats_archief(
	sc_dat_beg_geldh character varying(19),
	sc_identif character varying(16),
	indic_geconst character varying(1),
	status character varying(80),
	fk_4nra_sc_identif character varying(16)
);
alter table standplaats_archief add constraint ar_standplaats_pk primary key(sc_dat_beg_geldh,sc_identif);
comment on table standplaats_archief is 'RSGB class STANDPLAATS. Subclass van: BENOEMD TERREIN -> BENOEMD OBJECT';
comment on column standplaats_archief.sc_dat_beg_geldh is '[PK] OnvolledigeDatum, FK naar benoemd_terrein.dat_beg_geldh - Datum begin geldigheid benoemd terrein';
comment on column standplaats_archief.sc_identif is '[PK] AN16, FK naar benoemd_terrein.sc_identif - Benoemd object identificatie';
comment on column standplaats_archief.indic_geconst is 'AN1 - Indicatie geconstateerde standplaats';
comment on column standplaats_archief.status is 'AN80 - Standplaatsstatus';
comment on column standplaats_archief.fk_4nra_sc_identif is '[FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "heeft als hoofdadres"';

create table verblijfsobj_archief(
	sc_dat_beg_geldh character varying(19),
	sc_identif character varying(16),
	aantal_kamers decimal(2,0),
	hoogste_bouwlaag decimal(3,0),
	indic_geconstateerd character varying(1),
	laagste_bouwlaag decimal(3,0),
	ontsluiting_verdieping character varying(3),
	soort_woonobj decimal(1,0),
	toegang_bouwlaag decimal(3,0),
	status character varying(80),
	fk_11nra_sc_identif character varying(16)
);
alter table verblijfsobj_archief add constraint ar_verblijfsobj_pk primary key(sc_dat_beg_geldh,sc_identif);
comment on table verblijfsobj_archief is 'RSGB class VERBLIJFSOBJECT. Subclass van: GEBOUWD OBJECT -> BENOEMD OBJECT';
comment on column verblijfsobj_archief.sc_dat_beg_geldh is '[PK] OnvolledigeDatum, FK naar gebouwd_obj.dat_beg_geldh - Datum begin geldigheid gebouwd object';
comment on column verblijfsobj_archief.sc_identif is '[PK] AN16, FK naar gebouwd_obj.sc_identif - Benoemd object identificatie';
comment on column verblijfsobj_archief.aantal_kamers is 'N2 - Aantal kamers';
comment on column verblijfsobj_archief.hoogste_bouwlaag is 'N3 - Hoogste bouwlaag verblijfsobject';
comment on column verblijfsobj_archief.indic_geconstateerd is 'AN1 - Indicatie geconstateerd verblijfsobject';
comment on column verblijfsobj_archief.laagste_bouwlaag is 'N3 - Laagste bouwlaag verblijfsobject';
comment on column verblijfsobj_archief.ontsluiting_verdieping is 'AN3 - Ontsluiting verdieping';
comment on column verblijfsobj_archief.soort_woonobj is 'N1 - Soort woonobject';
comment on column verblijfsobj_archief.toegang_bouwlaag is 'N3 - Toegang bouwlaag verblijfsobject';
comment on column verblijfsobj_archief.status is 'AN80 - Verblijfsobjectstatus';
comment on column verblijfsobj_archief.fk_11nra_sc_identif is '[FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING): "heeft als hoofdadres"';

create table verblijfstitel_archief(
	aand decimal(2,0),
	begindatum_geldh character varying(19),
	eindd_geldh character varying(19),
	omschr character varying(100)
);
alter table verblijfstitel_archief add constraint ar_verblijfstitel_pk primary key(aand,begindatum_geldh);
comment on table verblijfstitel_archief is 'RSGB class VERBLIJFSTITEL';
comment on column verblijfstitel_archief.aand is '[PK] N2 - Aanduiding verblijfstitel';
comment on column verblijfstitel_archief.begindatum_geldh is '[PK] OnvolledigeDatum - Begindatum geldigheid verblijfstitel';
comment on column verblijfstitel_archief.eindd_geldh is 'OnvolledigeDatum - Einddatum geldigheid verblijfstitel';
comment on column verblijfstitel_archief.omschr is 'AN100 - Verblijfstitelomschrijving';

create table vrijstaand_vegetatie_o_archief(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type character varying(40)
);
select addgeometrycolumn('vrijstaand_vegetatie_o_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index vrijstaand_vegetatie_o_archief_geom_idx on vrijstaand_vegetatie_o_archief USING GIST (geom);
alter table vrijstaand_vegetatie_o_archief add constraint ar_vrijstaand_vegetatie_obj_pk primary key(dat_beg_geldh,identif);
comment on table vrijstaand_vegetatie_o_archief is 'RSGB class VRIJSTAAND VEGETATIE OBJECT';
comment on column vrijstaand_vegetatie_o_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid vrijstaand vegetatie object';
comment on column vrijstaand_vegetatie_o_archief.identif is '[PK] NEN3610ID - Identificatie vrijstaand vegetatie object';
comment on column vrijstaand_vegetatie_o_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid vrijstaand vegetatie object';
comment on column vrijstaand_vegetatie_o_archief.geom is 'GM_Surface - Geometrie vrijstaand vegetatie object';
comment on column vrijstaand_vegetatie_o_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging vrijstaand vegetatie object';
comment on column vrijstaand_vegetatie_o_archief.status is 'A8 - Status vrijstaand vegetatie object';
comment on column vrijstaand_vegetatie_o_archief.type is 'AN40 - Type vrijstaand vegetatie object';

create table waterdeel_archief(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	clazz character varying(255),
	datum_einde_geldh character varying(19),
	droogvallend character varying(1),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	type character varying(40),
	fk_7opr_identifcode character varying(16)
);
select addgeometrycolumn('waterdeel_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index waterdeel_archief_geom_idx on waterdeel_archief USING GIST (geom);
alter table waterdeel_archief add constraint ar_waterdeel_pk primary key(dat_beg_geldh,identif);
comment on table waterdeel_archief is 'RSGB class WATERDEEL. Directe superclass van: WATERVAKONDERDEEL';
comment on column waterdeel_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid waterdeel';
comment on column waterdeel_archief.identif is '[PK] NEN3610ID - Identificatie waterdeel';
comment on column waterdeel_archief.clazz is 'Aanduiding subclass';
comment on column waterdeel_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid waterdeel';
comment on column waterdeel_archief.droogvallend is 'AN1 - Droogvallend';
comment on column waterdeel_archief.geom is 'GM_Surface - Geometrie waterdeel';
comment on column waterdeel_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging waterdeel';
comment on column waterdeel_archief.status is 'A8 - Status waterdeel';
comment on column waterdeel_archief.type is 'AN40 - Type waterdeel';
comment on column waterdeel_archief.fk_7opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode: "maakt deel uit van"';

create table watervakonderdeel_archief(
	sc_dat_beg_geldh character varying(19),
	sc_identif character varying(255),
	fk_1wad_identif character varying(255)
);
alter table watervakonderdeel_archief add constraint ar_watervakonderdeel_pk primary key(sc_dat_beg_geldh,sc_identif);
comment on table watervakonderdeel_archief is 'RSGB class WATERVAKONDERDEEL. Subclass van: WATERDEEL';
comment on column watervakonderdeel_archief.sc_dat_beg_geldh is '[PK] OnvolledigeDatum, FK naar waterdeel.dat_beg_geldh - Datum begin geldigheid waterdeel';
comment on column watervakonderdeel_archief.sc_identif is '[PK] NEN3610ID, FK naar waterdeel.identif - Identificatie waterdeel';
comment on column watervakonderdeel_archief.fk_1wad_identif is '[FK] NEN3610ID, FK naar waterdeel.identif';

create table waterschap_archief(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	datum_einde_geldh character varying(19),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	naam character varying(40)
);
select addgeometrycolumn('waterschap_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index waterschap_archief_geom_idx on waterschap_archief USING GIST (geom);
alter table waterschap_archief add constraint ar_waterschap_pk primary key(dat_beg_geldh,identif);
comment on table waterschap_archief is 'RSGB class WATERSCHAP';
comment on column waterschap_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid waterschap';
comment on column waterschap_archief.identif is '[PK] NEN3610ID - Identificatie waterschap';
comment on column waterschap_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid waterschap';
comment on column waterschap_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging waterschap';
comment on column waterschap_archief.status is 'A8 - Status waterschap';
comment on column waterschap_archief.geom is 'GM_Surface - Waterschapgeometrie';
comment on column waterschap_archief.naam is 'AN40 - Waterschapnaam';

create table wegdeel_archief(
	dat_beg_geldh character varying(19),
	identif character varying(255),
	clazz character varying(255),
	datum_einde_geldh character varying(19),
	functie character varying(25),
	fysiek_voork character varying(20),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	fk_8opr_identifcode character varying(16)
);
select addgeometrycolumn('wegdeel_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index wegdeel_archief_geom_idx on wegdeel_archief USING GIST (geom);
alter table wegdeel_archief add constraint ar_wegdeel_pk primary key(dat_beg_geldh,identif);
comment on table wegdeel_archief is 'RSGB class WEGDEEL. Directe superclass van: WEGVAKONDERDEEL';
comment on column wegdeel_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid wegdeel';
comment on column wegdeel_archief.identif is '[PK] NEN3610ID - Identificatie wegdeel';
comment on column wegdeel_archief.clazz is 'Aanduiding subclass';
comment on column wegdeel_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid wegdeel';
comment on column wegdeel_archief.functie is 'AN25 - Functie wegdeel';
comment on column wegdeel_archief.fysiek_voork is 'AN20 - Fysiek voorkomen wegdeel';
comment on column wegdeel_archief.geom is 'GM_Surface - Geometrie wegdeel';
comment on column wegdeel_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging wegdeel';
comment on column wegdeel_archief.status is 'A8 - Status wegdeel';
comment on column wegdeel_archief.fk_8opr_identifcode is '[FK] AN16, FK naar openb_rmte.identifcode: "maakt deel uit van"';

create table wegvakonderdeel_archief(
	sc_dat_beg_geldh character varying(19),
	sc_identif character varying(255),
	fk_1wgd_identif character varying(255)
);
alter table wegvakonderdeel_archief add constraint ar_wegvakonderdeel_pk primary key(sc_dat_beg_geldh,sc_identif);
comment on table wegvakonderdeel_archief is 'RSGB class WEGVAKONDERDEEL. Subclass van: WEGDEEL';
comment on column wegvakonderdeel_archief.sc_dat_beg_geldh is '[PK] OnvolledigeDatum, FK naar wegdeel.dat_beg_geldh - Datum begin geldigheid wegdeel';
comment on column wegvakonderdeel_archief.sc_identif is '[PK] NEN3610ID, FK naar wegdeel.identif - Identificatie wegdeel';
comment on column wegvakonderdeel_archief.fk_1wgd_identif is '[FK] NEN3610ID, FK naar wegdeel.identif';

create table wijk_archief(
	dat_beg_geldh character varying(19),
	code decimal(2,0),
	datum_einde_geldh character varying(19),
	identif_imgeowyk character varying(255),
	relve_hoogteligging decimal(1,0),
	status character varying(8),
	naam character varying(40)
);
select addgeometrycolumn('wijk_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index wijk_archief_geom_idx on wijk_archief USING GIST (geom);
alter table wijk_archief add constraint ar_wijk_pk primary key(dat_beg_geldh,code);
comment on table wijk_archief is 'RSGB class WIJK';
comment on column wijk_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid wijk';
comment on column wijk_archief.code is '[PK] N2 - Wijkcode';
comment on column wijk_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid wijk';
comment on column wijk_archief.identif_imgeowyk is 'NEN3610ID - Identificatie IMGeoWYK';
comment on column wijk_archief.relve_hoogteligging is 'N1 - Relatieve hoogteligging wijk';
comment on column wijk_archief.status is 'A8 - Status wijk';
comment on column wijk_archief.geom is 'GM_Surface - Wijkgeometrie';
comment on column wijk_archief.naam is 'AN40 - Wijknaam';

create table wnplts_archief(
	dat_beg_geldh character varying(19),
	identif character varying(4),
	datum_einde_geldh character varying(19),
	indic_geconst character varying(1),
	naam character varying(80),
	naam_nen character varying(24),
	status character varying(80),
	fk_7gem_code decimal(4,0)
);
select addgeometrycolumn('wnplts_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index wnplts_archief_geom_idx on wnplts_archief USING GIST (geom);
alter table wnplts_archief add constraint ar_wnplts_pk primary key(dat_beg_geldh,identif);
comment on table wnplts_archief is 'RSGB class WOONPLAATS';
comment on column wnplts_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid woonplaats';
comment on column wnplts_archief.identif is '[PK] AN4 - Woonplaatsidentificatie';
comment on column wnplts_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid woonplaats';
comment on column wnplts_archief.indic_geconst is 'AN1 - Indicatie geconstateerde woonplaats';
comment on column wnplts_archief.geom is 'GM_Surface - Woonplaatsgeometrie';
comment on column wnplts_archief.naam is 'AN80 - Woonplaatsnaam';
comment on column wnplts_archief.naam_nen is 'AN24 - Woonplaatsnaam NEN';
comment on column wnplts_archief.status is 'AN80 - Woonplaatsstatus';
comment on column wnplts_archief.fk_7gem_code is '[FK] N4, FK naar gemeente.code: "ligt in"';

create table woz_deelobj_archief(
	dat_beg_geldh_deelobj decimal(8,0),
	nummer decimal(6,0),
	code character varying(4),
	datum_einde_geldh_deelobj decimal(8,0),
	status decimal(2,0),
	fk_4pnd_identif character varying(16),
	fk_5tgo_identif character varying(16),
	fk_6woz_nummer decimal(12,0)
);
alter table woz_deelobj_archief add constraint ar_woz_deelobj_pk primary key(dat_beg_geldh_deelobj,nummer);
comment on table woz_deelobj_archief is 'RSGB class WOZ-DEELOBJECT';
comment on column woz_deelobj_archief.dat_beg_geldh_deelobj is '[PK] N8 - Datum begin geldigheid deelobject';
comment on column woz_deelobj_archief.nummer is '[PK] N6 - Nummer WOZ-deelobject';
comment on column woz_deelobj_archief.code is 'AN4 - Code WOZ-deelobject';
comment on column woz_deelobj_archief.datum_einde_geldh_deelobj is 'N8 - Datum einde geldigheid deelobject';
comment on column woz_deelobj_archief.status is 'N2 - Status WOZ-deelobject';
comment on column woz_deelobj_archief.fk_4pnd_identif is '[FK] AN16, FK naar pand.identif: "bestaat uit"';
comment on column woz_deelobj_archief.fk_5tgo_identif is '[FK] AN16, FK naar benoemd_obj.identif: "bestaat uit"';
comment on column woz_deelobj_archief.fk_6woz_nummer is '[FK] N12, FK naar woz_obj.nummer: "is onderdeel van"';

create table woz_obj_archief(
	dat_beg_geldh character varying(19),
	nummer decimal(12,0),
	datum_einde_geldh character varying(19),
	gebruikscode decimal(2,0),
	grondoppervlakte decimal(11,0),
	soort_obj_code decimal(4,0),
	status decimal(2,0),
	vastgestelde_waarde decimal(11,0),
	waardepeildatum date
);
select addgeometrycolumn('woz_obj_archief', 'geom', 28992, 'MULTIPOLYGON', 2);
create index woz_obj_archief_geom_idx on woz_obj_archief USING GIST (geom);
alter table woz_obj_archief add constraint ar_woz_obj_pk primary key(dat_beg_geldh,nummer);
comment on table woz_obj_archief is 'RSGB class WOZ-OBJECT';
comment on column woz_obj_archief.dat_beg_geldh is '[PK] OnvolledigeDatum - Datum begin geldigheid WOZ-object';
comment on column woz_obj_archief.nummer is '[PK] N12 - WOZ-objectnummer';
comment on column woz_obj_archief.datum_einde_geldh is 'OnvolledigeDatum - Datum einde geldigheid WOZ-object';
comment on column woz_obj_archief.gebruikscode is 'N2 - Gebruikscode';
comment on column woz_obj_archief.geom is 'GM_Surface - Geometrie WOZ-object';
comment on column woz_obj_archief.grondoppervlakte is 'N11 - Grondoppervlakte';
comment on column woz_obj_archief.soort_obj_code is 'N4 - Soort-object-code';
comment on column woz_obj_archief.status is 'N2 - Status WOZ-object';
comment on column woz_obj_archief.vastgestelde_waarde is 'N11 - Vastgestelde waarde';
comment on column woz_obj_archief.waardepeildatum is 'Datum - Waardepeildatum';

create table woz_waarde_archief(
	waardepeildatum decimal(8,0),
	status_beschikking decimal(2,0),
	toestandspeildatum decimal(8,0),
	vastgestelde_waarde decimal(11,0),
	fk_1woz_nummer decimal(12,0)
);
alter table woz_waarde_archief add constraint ar_woz_waarde_pk primary key(waardepeildatum);
comment on table woz_waarde_archief is 'RSGB class WOZ-WAARDE';
comment on column woz_waarde_archief.waardepeildatum is '[PK] N8 - Waardepeildatum';
comment on column woz_waarde_archief.status_beschikking is 'N2 - Status beschikking';
comment on column woz_waarde_archief.toestandspeildatum is 'N8 - Toestandspeildatum';
comment on column woz_waarde_archief.vastgestelde_waarde is 'N11 - Vastgestelde waarde';
comment on column woz_waarde_archief.fk_1woz_nummer is '[FK] N12, FK naar woz_obj.nummer';

create table app_re_kad_perceel_archief(
	fk_nn_lh_apr_sc_kad_identif decimal(15,0),
	fk_nn_lh_apr_sc_dat_beg_geldh character varying(19),
	fk_nn_rh_kdp_sc_kad_identif decimal(15,0)
);
alter table app_re_kad_perceel_archief add constraint ar_app_re_kad_perceel_pk primary key(fk_nn_lh_apr_sc_kad_identif,fk_nn_lh_apr_sc_dat_beg_geldh,fk_nn_rh_kdp_sc_kad_identif);
comment on table app_re_kad_perceel_archief is 'N - N relatie: APPARTEMENTSRECHT "maakt deel uit van appartementencomplex dat staat op" KADASTRAAL PERCEEL';
comment on column app_re_kad_perceel_archief.fk_nn_lh_apr_sc_kad_identif is '[FK] N15, FK naar app_re.sc_kad_identif (is FK naar superclass KADASTRALE ONROERENDE ZAAK)';
comment on column app_re_kad_perceel_archief.fk_nn_lh_apr_sc_dat_beg_geldh is '[FK] OnvolledigeDatum, FK naar app_re.sc_dat_beg_geldh (is FK naar superclass KADASTRALE ONROERENDE ZAAK)';
comment on column app_re_kad_perceel_archief.fk_nn_rh_kdp_sc_kad_identif is '[FK] N15, FK naar kad_perceel.sc_kad_identif (is FK naar superclass KADASTRALE ONROERENDE ZAAK)';

create table benoemd_terrein_benoem_archief(
	fk_nn_lh_btr_sc_identif character varying(16),
	fk_nn_lh_btr_dat_beg_geldh character varying(19),
	fk_nn_rh_btr_sc_identif character varying(16)
);
alter table benoemd_terrein_benoem_archief add constraint ar_benoemd_terrein_benoemd__pk primary key(fk_nn_lh_btr_sc_identif,fk_nn_lh_btr_dat_beg_geldh,fk_nn_rh_btr_sc_identif);
comment on table benoemd_terrein_benoem_archief is 'N - N relatie: BENOEMD TERREIN "is ontstaan uit / overgegaan in" BENOEMD TERREIN';
comment on column benoemd_terrein_benoem_archief.fk_nn_lh_btr_sc_identif is '[FK] AN16, FK naar benoemd_terrein.sc_identif (is FK naar superclass BENOEMD OBJECT)';
comment on column benoemd_terrein_benoem_archief.fk_nn_lh_btr_dat_beg_geldh is '[FK] OnvolledigeDatum, FK naar benoemd_terrein.dat_beg_geldh';
comment on column benoemd_terrein_benoem_archief.fk_nn_rh_btr_sc_identif is '[FK] AN16, FK naar benoemd_terrein.sc_identif (is FK naar superclass BENOEMD OBJECT)';

create table gemeente_gemeente_archief(
	fk_nn_lh_gem_code decimal(4,0),
	fk_nn_lh_gem_dat_beg_geldh character varying(19),
	fk_nn_rh_gem_code decimal(4,0)
);
alter table gemeente_gemeente_archief add constraint ar_gemeente_gemeente_pk primary key(fk_nn_lh_gem_code,fk_nn_lh_gem_dat_beg_geldh,fk_nn_rh_gem_code);
comment on table gemeente_gemeente_archief is 'N - N relatie: GEMEENTE "is overgegaan in" GEMEENTE';
comment on column gemeente_gemeente_archief.fk_nn_lh_gem_code is '[FK] N4, FK naar gemeente.code';
comment on column gemeente_gemeente_archief.fk_nn_lh_gem_dat_beg_geldh is '[FK] OnvolledigeDatum, FK naar gemeente.dat_beg_geldh';
comment on column gemeente_gemeente_archief.fk_nn_rh_gem_code is '[FK] N4, FK naar gemeente.code';

create table kad_onrrnd_zk_kad_onrr_archief(
	fk_nn_lh_koz_kad_identif decimal(15,0),
	fk_nn_lh_koz_dat_beg_geldh character varying(19),
	fk_nn_rh_koz_kad_identif decimal(15,0)
);
alter table kad_onrrnd_zk_kad_onrr_archief add constraint ar_kad_onrrnd_zk_kad_onrrnd_pk primary key(fk_nn_lh_koz_kad_identif,fk_nn_lh_koz_dat_beg_geldh,fk_nn_rh_koz_kad_identif);
comment on table kad_onrrnd_zk_kad_onrr_archief is 'N - N relatie: KADASTRALE ONROERENDE ZAAK "is hoofdperceel bij mandelige" KADASTRALE ONROERENDE ZAAK';
comment on column kad_onrrnd_zk_kad_onrr_archief.fk_nn_lh_koz_kad_identif is '[FK] N15, FK naar kad_onrrnd_zk.kad_identif';
comment on column kad_onrrnd_zk_kad_onrr_archief.fk_nn_lh_koz_dat_beg_geldh is '[FK] OnvolledigeDatum, FK naar kad_onrrnd_zk.dat_beg_geldh';
comment on column kad_onrrnd_zk_kad_onrr_archief.fk_nn_rh_koz_kad_identif is '[FK] N15, FK naar kad_onrrnd_zk.kad_identif';

create table ligplaats_nummeraand_archief(
	fk_nn_lh_lpl_sc_identif character varying(16),
	fk_nn_lh_lpl_sc_dat_beg_geldh character varying(19),
	fk_nn_rh_nra_sc_identif character varying(16)
);
alter table ligplaats_nummeraand_archief add constraint ar_ligplaats_nummeraand_pk primary key(fk_nn_lh_lpl_sc_identif,fk_nn_lh_lpl_sc_dat_beg_geldh,fk_nn_rh_nra_sc_identif);
comment on table ligplaats_nummeraand_archief is 'N - N relatie: LIGPLAATS "heeft als nevenadressen" NUMMERAANDUIDING';
comment on column ligplaats_nummeraand_archief.fk_nn_lh_lpl_sc_identif is '[FK] AN16, FK naar ligplaats.sc_identif (is FK naar superclass BENOEMD OBJECT)';
comment on column ligplaats_nummeraand_archief.fk_nn_lh_lpl_sc_dat_beg_geldh is '[FK] OnvolledigeDatum, FK naar ligplaats.sc_dat_beg_geldh (is FK naar superclass BENOEMD TERREIN)';
comment on column ligplaats_nummeraand_archief.fk_nn_rh_nra_sc_identif is '[FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING)';

create table standplaats_nummeraand_archief(
	fk_nn_lh_spl_sc_identif character varying(16),
	fk_nn_lh_spl_sc_dat_beg_geldh character varying(19),
	fk_nn_rh_nra_sc_identif character varying(16)
);
alter table standplaats_nummeraand_archief add constraint ar_standplaats_nummeraand_pk primary key(fk_nn_lh_spl_sc_identif,fk_nn_lh_spl_sc_dat_beg_geldh,fk_nn_rh_nra_sc_identif);
comment on table standplaats_nummeraand_archief is 'N - N relatie: STANDPLAATS "heeft als nevenadressen" NUMMERAANDUIDING';
comment on column standplaats_nummeraand_archief.fk_nn_lh_spl_sc_identif is '[FK] AN16, FK naar standplaats.sc_identif (is FK naar superclass BENOEMD OBJECT)';
comment on column standplaats_nummeraand_archief.fk_nn_lh_spl_sc_dat_beg_geldh is '[FK] OnvolledigeDatum, FK naar standplaats.sc_dat_beg_geldh (is FK naar superclass BENOEMD TERREIN)';
comment on column standplaats_nummeraand_archief.fk_nn_rh_nra_sc_identif is '[FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING)';

create table verblijfsobj_pand_archief(
	fk_nn_lh_vbo_sc_identif character varying(16),
	fk_nn_lh_vbo_sc_dat_beg_geldh character varying(19),
	fk_nn_rh_pnd_identif character varying(16)
);
alter table verblijfsobj_pand_archief add constraint ar_verblijfsobj_pand_pk primary key(fk_nn_lh_vbo_sc_identif,fk_nn_lh_vbo_sc_dat_beg_geldh,fk_nn_rh_pnd_identif);
comment on table verblijfsobj_pand_archief is 'N - N relatie: VERBLIJFSOBJECT "maakt deel uit van" PAND';
comment on column verblijfsobj_pand_archief.fk_nn_lh_vbo_sc_identif is '[FK] AN16, FK naar verblijfsobj.sc_identif (is FK naar superclass BENOEMD OBJECT)';
comment on column verblijfsobj_pand_archief.fk_nn_lh_vbo_sc_dat_beg_geldh is '[FK] OnvolledigeDatum, FK naar verblijfsobj.sc_dat_beg_geldh (is FK naar superclass GEBOUWD OBJECT)';
comment on column verblijfsobj_pand_archief.fk_nn_rh_pnd_identif is '[FK] AN16, FK naar pand.identif';

create table verblijfsobj_nummeraan_archief(
	fk_nn_lh_vbo_sc_identif character varying(16),
	fk_nn_lh_vbo_sc_dat_beg_geldh character varying(19),
	fk_nn_rh_nra_sc_identif character varying(16)
);
alter table verblijfsobj_nummeraan_archief add constraint ar_verblijfsobj_nummeraand_pk primary key(fk_nn_lh_vbo_sc_identif,fk_nn_lh_vbo_sc_dat_beg_geldh,fk_nn_rh_nra_sc_identif);
comment on table verblijfsobj_nummeraan_archief is 'N - N relatie: VERBLIJFSOBJECT "heeft als nevenadres(sen)" NUMMERAANDUIDING';
comment on column verblijfsobj_nummeraan_archief.fk_nn_lh_vbo_sc_identif is '[FK] AN16, FK naar verblijfsobj.sc_identif (is FK naar superclass BENOEMD OBJECT)';
comment on column verblijfsobj_nummeraan_archief.fk_nn_lh_vbo_sc_dat_beg_geldh is '[FK] OnvolledigeDatum, FK naar verblijfsobj.sc_dat_beg_geldh (is FK naar superclass GEBOUWD OBJECT)';
comment on column verblijfsobj_nummeraan_archief.fk_nn_rh_nra_sc_identif is '[FK] AN16, FK naar nummeraand.sc_identif (is FK naar superclass ADRESSEERBAAR OBJECT AANDUIDING)';

insert into meta_enumeratie_waardes (naam,waarde) values ('Aard aantekening kadastraal object','koopovereenkomst waarvan de inschrijving resulteert in een koperbescherming volgens artikel 7:3 BW');
insert into meta_enumeratie_waardes (naam,waarde) values ('Aard aantekening kadastraal object','koopovereenkomst of voorovereenkomst tot koop waarvan de inschrijving resulteert in koperbescherming volgens artikel 10 Wvg');
insert into meta_enumeratie_waardes (naam,waarde) values ('Aard aantekening kadastraal object','vervallen van de koopovereenkomst of voorovereenkomst tot koop die zijn ingeschreven op grond van artikel 7:3 of artikel 10 Wvg');
insert into meta_enumeratie_waardes (naam,waarde) values ('Aard aantekening kadastraal object','voorwaardelijke verkrijging');
insert into meta_enumeratie_waardes (naam,waarde) values ('Aard aantekening kadastraal object','erfdienstbaarheid');
insert into meta_enumeratie_waardes (naam,waarde) values ('Aard aantekening kadastraal object','kwalitatieve verplichting alsin art 6:25 BW');
insert into meta_enumeratie_waardes (naam,waarde) values ('Aard aantekening kadastraal object','onderbewindstelling van een grondstuk');
insert into meta_enumeratie_waardes (naam,waarde) values ('Aard aantekening kadastraal object','publiekrechtelijke beperking');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','doeleinden voor wonen');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','eensgezinswoning');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','bejaardenwoning');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','recreatiewoning');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','meergezinswoning');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','dienstwoning');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','zorgwoonverblijf');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','aanleunwoonverblijf');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','bejaardenwoonverblijf (in bejaardenoord, centrale keuken)');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','jongerenwooneenheid');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','gehandicaptenwooneenheid');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','doeleinden voor niet-wonen');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','doeleinden voor handel, horeca en bedrijf');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','detailhandel');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','cafe/bar/restaurant');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','hotel/logies');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','kantoor');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','opslag en distributie');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','fabricage en productie');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','onderhoud en reparatie');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','laboratoria');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','overige doeleinden voor niet-wonen');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','doeleinden voor cultuur');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','wijk-/buurt-/verenigingsactiviteiten');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','congres');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','theater en concert');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','musea');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','expositie');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','bioscoop');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','bibliotheek');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','overige doeleinden voor cultuur');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','doeleinden voor recreatie');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','sport buiten');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','sport binnen');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','recreatie');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','zwembad');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','dierenverzorging');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','natuur en landschap');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','overige doeleinden voor recreatie');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','doeleinden voor agrarisch bedrijf');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','akkerbouw');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','veeteelt');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','tuinbouw');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','gemengd bedrijf');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','overige doeleinden voor agrarisch bedrijf');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','doeleinden voor onderwijs');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','kinderopvang');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','basisschool');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','algemeen voortgezet onderwijs');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','hoger beroepsonderwijs');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','academisch onderwijs');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','bijzonder onderwijs');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','vrijetijds onderwijs');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','overige doeleinden voor onderwijs');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','doeleinden voor gezondheidszorg');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','ziekenhuis');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','polikliniek');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','praktijkruimte');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','verpleegtehuis');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','verzorgingstehuis en bejaardentehuis');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','dagverblijf');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','wijkverzorging');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','psychiatrische inrichting');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','overige doeleinden voor gezondheidszorg');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','doeleinden voor verkeer');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','stalling (fietsen/auto''s)');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','wegverkeer');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','spoorwegverkeer');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','luchtvaart');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','scheepvaart');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','overige doeleinden voor verkeer');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','doeleinden voor nutsvoorzieningen');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','waternuts doeleinden');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','gas');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','elektriciteit');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','CAI');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','telecommunicatie');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','waterschaps en waterverdediging');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','overige doeleinden voor nutsvoorzieningen');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','andere doeleinden van openbaar nut');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','gemeentehuis');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','politie');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','brandweer');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','gevangenis/gesticht');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','begraafplaats/crematorium');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','godsdienst (kerk, klooster e.d.)');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','defensie');
insert into meta_enumeratie_waardes (naam,waarde) values ('Bouwkundige bestemming actueel GEBOUWD OBJECT','overige andere doeleinden van openbaar nut');
insert into meta_enumeratie_waardes (naam,waarde) values ('Huishoudensoort','institutioneel huishouden');
insert into meta_enumeratie_waardes (naam,waarde) values ('Huishoudensoort','alleenstaand (inclusief andere personen die in hetzelfde object wonen, maar een eigen huishouding voeren)');
insert into meta_enumeratie_waardes (naam,waarde) values ('Huishoudensoort','2 personen, vaste partners, geen thuiswonende kinderen');
insert into meta_enumeratie_waardes (naam,waarde) values ('Huishoudensoort','2 personen, vaste partners, een of meer thuiswonende kinderen');
insert into meta_enumeratie_waardes (naam,waarde) values ('Huishoudensoort','eenoudergezin, ouder met een of meer thuiswonende kinderen');
insert into meta_enumeratie_waardes (naam,waarde) values ('Huishoudensoort','overig particulier huishouden (samenwoning van personen die geen partnerrelatie onderhouden of een ouder-kindrelatie hebben, maar wel gezamenlijk een huishouding voeren)');
insert into meta_enumeratie (tabel,kolom,enumeratie) values ('gebouwd_obj','bouwk_best_act','Bouwkundige bestemming actueel GEBOUWD OBJECT');
insert into meta_enumeratie (tabel,kolom,enumeratie) values ('kad_onrrnd_zk_aantek','aard_aantek_kad_obj','Aard aantekening kadastraal object');
insert into meta_referentielijsten (tabel,kolom,referentielijst) values ('ingeschr_nat_prs','btnlndse_nation','NATIONALITEIT');
insert into meta_referentielijsten (tabel,kolom,referentielijst) values ('ingeschr_nat_prs','land_vanwaar_ingeschr','LAND');
insert into meta_referentielijsten (tabel,kolom,referentielijst) values ('ingeschr_nat_prs','land_waarnaar_vertrokken','LAND');
insert into meta_referentielijsten (tabel,kolom,referentielijst) values ('nat_prs','academische_titel','ACADEMISCHE TITEL');
insert into meta_referentielijsten (tabel,kolom,referentielijst) values ('rsdoc','soort','REISDOCUMENTSOORT');
insert into meta_referentielijsten (tabel,kolom,referentielijst) values ('zak_recht','aand_aard_recht_verkort','AARD RECHT VERKORT');
insert into meta_referentielijsten (tabel,kolom,referentielijst) values ('zak_recht','aand_aard_verkregen_recht','AARD VERKREGEN RECHT');

-- Handmatige scripts

-- Script: metagegevens_brondocument.sql


-- Een brondocument wordt niet in de originele tabel opgenomen omdat dit een
-- 0..n relatie kan zijn en niet altijd een 0..1. 

-- In deze tabel wordt verwezen naar de tabel waarop het metagegeven brondocument
-- van toepassing is. Voor de identificatie van de rij van de tabel naar waar
-- wordt verwezen is een enkele kolom gebruikt: composite keys worden niet ondersteund.
-- Composite keys worden toch voornamelijk toegepast bij een combinate met datum
-- begin geldigheid.

create table brondocument (
  tabel varchar(30),
  tabel_identificatie varchar(50),
  identificatie varchar(50),
  gemeente integer,
  omschrijving varchar(40),
  datum date,
  ref_id varchar(50),
  primary key(tabel,tabel_identificatie,identificatie)
);
  
-- Script: woz_waarde.sql

alter table woz_waarde add constraint woz_waarde_pk primary key (fk_1woz_nummer);

alter table woz_waarde_archief drop constraint ar_woz_waarde_pk;
alter table woz_waarde_archief add constraint woz_waarde_archief_pk primary key(waardepeildatum,fk_1woz_nummer);
-- Script: drop_constraints.sql


-- BAG koppeling
alter table benoemd_obj_kad_onrrnd_zk drop constraint fk_tgo_koz_nn_lh;

-- Referentielijst niet gevuld
alter table zak_recht drop constraint fk_zkr_rl_3;

-- Rechterkant mogelijk nog niet geinsert
alter table kad_onrrnd_zk_his_rel drop constraint fk_kad_onrrnd_zk_his_rel_sc_rh;

-- Script: herkomst_metadata.sql

create table herkomst_metadata (
	tabel character varying(255),
	kolom character varying(255),
	waarde character varying(255),
	herkomst_br character varying(255),
	datum timestamp without time zone,
	primary key (tabel, kolom, waarde, herkomst_br, datum)
) with (
  	OIDS = FALSE
);
-- Script: bag_views.sql

/*
Views for visualizing the BAG data in RSGB format.
Based on Gouda views
*/


/*
    VERBLIJFSOBJ status
    VERBLIJFSOBJ_PAND pand id
    GEBOUWD_OBJ geometrie en oppervlak
    verblijfsobj_nummeraand koppeling
    NUMMERAAND hoofdadres
    addresseerb_obj_aand huisnummer
    gem_openb_rmte straat
    gemeente
    VERBLIJFSOBJGEBRUIKSDOEL doel

*/
CREATE VIEW
    V_VERBLIJFSOBJECT_ALLES
    (
        FID,
        PAND_ID,
        GEMEENTE_NAAM,
        openbareruimtenaam,
        STRAATNAAM,
        HUINUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        --GEBRUIKSDOEL,
        STATUS,
        OPPERVLAKTE,
        THE_GEOM
    ) AS
SELECT
    vbo.sc_IDENTIF            AS fid,
    fkpand.fk_nn_rh_pnd_identif    AS pand_id,
    gem.naam               AS gemeente_naam,
    geor.naam_openb_rmte AS openbareruimtenaam, -- straatnaam,
    geor.straatnaam as straatnaam,
    addrobj.huinummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    --doel.gebruiksdoel,
    vbo.status,
    gobj.oppervlakte_obj AS oppervlakte,
    gobj.puntgeom        AS the_geom
FROM
    VERBLIJFSOBJ vbo
JOIN
    VERBLIJFSOBJ_PAND fkpand
ON
    (
        fkpand.fk_nn_lh_vbo_sc_identif = vbo.sc_identif )
JOIN
    GEBOUWD_OBJ gobj
ON
    (
        gobj.sc_identif = vbo.sc_identif )
JOIN
    verblijfsobj_nummeraand vna
ON
    (
        vna.fk_nn_lh_vbo_sc_identif = vbo.sc_identif )
JOIN
    NUMMERAAND na
ON
    (
        na.sc_identif = vna.fk_nn_rh_nra_sc_identif )
JOIN
    addresseerb_obj_aand addrobj
ON
    (
        addrobj.identif = na.sc_identif )
JOIN
    gem_openb_rmte geor 
ON
    (
        geor.identifcode = addrobj.fk_7opr_identifcode )
JOIN
    gemeente gem
ON
    (
        geor.fk_7gem_code = gem.code )
/*JOIN
    VERBLIJFSOBJGEBRUIKSDOEL doel
ON
    (
        doel.verblijfsobj = vbo.IDENTIF
    AND doel.VBO_DATUM_BEGIN = vbo.DATUM_BEGIN_GELDH )*/
WHERE
    --vbo.DATUM_EIND_GELDH IS NULL
--vna.DATUM_EIND_GELDH IS NULL
 --na.DATUM_EIND_GELDH IS NULL
addrobj.dat_eind_geldh IS NULL
AND geor.DATUM_EINDE_GELDH IS NULL
AND gem.DATUM_EINDE_GELDH IS NULL
AND gobj.DATUM_EINDE_GELDH IS NULL;
------


-- V_VERBLIJFSOBJECT_GEVORMD
CREATE VIEW
    V_VERBLIJFSOBJECT_GEVORMD
    (
        FID,
        PAND_ID,
        GEMEENTE_NAAM,
        STRAATNAAM,
        HUINUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        --GEBRUIKSDOEL,
        STATUS,
        OPPERVLAKTE,
        THE_GEOM
    ) AS
SELECT
    FID,
    PAND_ID,
    GEMEENTE_NAAM,
    STRAATNAAm,
    HUINUMMER,
    HUISLETTER,
    HUISNUMMER_TOEV,
    POSTCODE,
    --GEBRUIKSDOEL,
    STATUS,
    OPPERVLAKTE,
    THE_GEOM
FROM
    V_VERBLIJFSOBJECT_ALLES
WHERE
    status = 'Verblijfsobject gevormd';


-- V_VERBLIJFSOBJECT

CREATE VIEW
    V_VERBLIJFSOBJECT
    (
        FID,
        PAND_ID,
        GEMEENTE_NAAM,
        STRAATNAAM,
        HUINUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        --GEBRUIKSDOEL,
        STATUS,
        OPPERVLAKTE,
        THE_GEOM
    ) AS
SELECT
     FID,
    PAND_ID,
    GEMEENTE_NAAM,
    STRAATNAAm,
    HUINUMMER,
    HUISLETTER,
    HUISNUMMER_TOEV,
    POSTCODE,
    --GEBRUIKSDOEL,
    STATUS,
    OPPERVLAKTE,
    THE_GEOM
FROM
    V_VERBLIJFSOBJECT_ALLES
WHERE
    status = 'Verblijfsobject in gebruik (niet ingemeten)'
OR  status = 'Verblijfsobject in gebruik';


--V_PAND_IN_GEBRUIK

CREATE VIEW
    V_PAND_IN_GEBRUIK
    (
        FID,
        EIND_DATUM_GELDIG,
        BEGIN_DATUM_GELDIG,
        STATUS,
        BOUWJAAR,
        THE_GEOM
    ) AS
SELECT
    p.IDENTIF           AS fid,
    p.DATUM_EINDE_GELDH AS eind_datum_geldig,
    p.DAT_BEG_GELDH AS begin_datum_geldig,
    p.STATUS,
    p.OORSPRONKELIJK_BOUWJAAR AS bouwjaar,
    p.GEOM_BOVENAANZICHT      AS the_geom
FROM
    PAND p
WHERE
    status IN ('Sloopvergunning verleend',
               'Pand in gebruik (niet ingemeten)',
               'Pand in gebruik',
               'Bouw gestart')
AND datum_einde_geldh IS NULL;

--  V_PAND_GEBRUIK_NIET_INGEMETEN
CREATE VIEW
    V_PAND_GEBRUIK_NIET_INGEMETEN
    (
        FID,
        BEGIN_DATUM_GELDIG,
        STATUS,
        BOUWJAAR,
        THE_GEOM
    ) AS
SELECT
    p.IDENTIF           AS fid,
    p.DAT_BEG_GELDH AS begin_datum_geldig,
    p.STATUS,
    p.OORSPRONKELIJK_BOUWJAAR AS bouwjaar,
    p.GEOM_BOVENAANZICHT      AS the_geom
FROM
    PAND p
WHERE
    status = 'Pand in gebruik (niet ingemeten)'
AND datum_einde_geldh IS NULL;



-- v_standplaats
CREATE VIEW
    V_STANDPLAATS
    (
        sc_identif,
        STATUS,
        fk_4nra_sc_identif,
        DATUM_BEGIN_GELDH,
        GEOMETRIE
    ) AS
SELECT
    sp.sc_identif,
    sp.STATUS,
    sp.fk_4nra_sc_identif,
    bt.DAT_BEG_GELDH,
    bt.GEOM AS geometrie
FROM
    standplaats sp
LEFT JOIN
    benoemd_terrein bt
ON
    (
        sp.sc_identif = bt.sc_identif);

-- V_LIGPLAATS
CREATE VIEW
    V_LIGPLAATS
    (
        sc_identif,
        STATUS,
        fk_4nra_sc_identif,
        DAT_BEG_GELDH,
        GEOMETRIE
    ) AS
SELECT
    lp.sc_identif,
    lp.STATUS,
    lp.fk_4nra_sc_identif,
    bt.DAT_BEG_GELDH,
    bt.GEOM AS geometrie
FROM
    ligplaats lp
LEFT JOIN
    benoemd_terrein bt
ON
    (
        lp.sc_identif = bt.sc_identif)
;



-- v_adres
/*
volledige adressenlijst
standplaats en ligplaats via benoemd_terrein, waarbij centroide van polygon wordt genomen
plus veblijfsobjec via punt object van gebouwd_obj
*/
CREATE VIEW
    V_ADRES
    (
        FID,
        GEMEENTE,
        STRAAT,
        HUISNUMMER,
        HUISLETTER,
        HUISNUMMER_TOEV,
        POSTCODE,
        STATUS,
        OPPERVLAKTE,
        THE_GEOM
    ) AS
SELECT
    vbo.sc_identif    AS fid,
    gem.naam          AS gemeente,
    geor.straatnaam   AS straat,
    addrobj.huinummer AS huisnummer,
    addrobj.huisletter,
    addrobj.huinummertoevoeging AS huisnummer_toev,
    addrobj.postcode,
    vbo.status,
    gobj.OPPERVLAKTE_OBJ || ' m2' AS oppervlakte,
    gobj.puntgeom                 AS the_geom
FROM
    VERBLIJFSOBJ vbo
JOIN
    GEBOUWD_OBJ gobj
ON
    (
        gobj.sc_identif = vbo.sc_identif )
JOIN
    verblijfsobj_nummeraand vna
ON
    (
        vna.fk_nn_lh_vbo_sc_identif = vbo.sc_identif )
JOIN
    NUMMERAAND na
ON
    (
        na.sc_identif = vna.fk_nn_rh_nra_sc_identif )
JOIN
    addresseerb_obj_aand addrobj
ON
    (
        addrobj.identif = na.sc_identif )
JOIN
    openb_rmte opr
ON
    (
        opr.identifcode = addrobj.fk_7opr_identifcode )
JOIN
    openb_rmte_gem_openb_rmte gmopr
ON
    (
        gmopr.fk_nn_lh_opr_identifcode = opr.identifcode )
JOIN
    gem_openb_rmte geor
ON
    (
        geor.identifcode = gmopr.fk_nn_rh_gor_identifcode )
JOIN
    gemeente gem
ON
    (
        geor.fk_7gem_code = gem.code )
WHERE
    addrobj.DAT_EIND_geldh IS NULL
AND geor.DATUM_EINDE_GELDH IS NULL
AND gem.DATUM_EINDE_GELDH IS NULL
AND gobj.DATUM_EINDE_GELDH IS NULL
AND (
        vbo.status = 'Verblijfsobject in gebruik (niet ingemeten)'
    OR  vbo.status = 'Verblijfsobject in gebruik')
AND na.STATUS ='Naamgeving uitgegeven';
-- Script: brk_views.sql


create view v_map_kad_perceel as
select
    p.sc_kad_identif,
    p.begrenzing_perceel,
    p.ka_sectie || ' ' || p.ka_perceelnummer AS aanduiding,
    p.grootte_perceel,
    z.ks_koopjaar,
    z.ks_bedrag,
    z.cu_aard_cultuur_onbebouwd
from kad_perceel p
join kad_onrrnd_zk z on (z.kad_identif = p.sc_kad_identif);

create table prs_eigendom (
    fk_prs_sc_identif varchar(32), 
    primary key (fk_prs_sc_identif), 
    foreign key (fk_prs_sc_identif) references prs(sc_identif)
);

create or replace view v_kad_perceel_in_eigendom as
select 
    p.begrenzing_perceel,
     p.sc_kad_identif,
    p.aanduiding,
    p.grootte_perceel,
    p.ks_koopjaar,
    p.ks_bedrag,
    p.cu_aard_cultuur_onbebouwd,
    nnprs.naam
from v_map_kad_perceel p
join zak_recht zr on (zr.fk_7koz_kad_identif = p.sc_kad_identif)
join prs_eigendom prs_e on (prs_e.fk_prs_sc_identif = zr.fk_8pes_sc_identif)
left join niet_nat_prs nnprs on (nnprs.sc_identif = prs_e.fk_prs_sc_identif);

create or replace view v_kad_perceel_adres as 
select distinct 
        kp.sc_kad_identif,
        kpvbo.FK_NN_LH_TGO_IDENTIF as kad_bag_koppeling_benobj,
        gor.naam_openb_rmte as straat,
        aoa.huinummer as huisnummer,
        aoa.huisletter,
        aoa.huinummertoevoeging as toevoeging,
        aoa.postcode,
        wp.naam as woonplaats
from kad_perceel kp
left join benoemd_obj_kad_onrrnd_zk kpvbo on (kpvbo.FK_NN_RH_KOZ_KAD_IDENTIF = kp.SC_KAD_IDENTIF)
left join verblijfsobj vbo on (vbo.SC_IDENTIF = kpvbo.FK_NN_LH_TGO_IDENTIF)
left join nummeraand na on (na.SC_IDENTIF = vbo.FK_11NRA_SC_IDENTIF)
left join addresseerb_obj_aand aoa on (aoa.IDENTIF = na.SC_IDENTIF)
left join gem_openb_rmte gor on (gor.IDENTIFCODE = aoa.FK_7OPR_IDENTIFCODE)
left join wnplts wp on (wp.IDENTIF = aoa.FK_6WPL_IDENTIF);

create or replace view v_kad_perceel_eenvoudig as
select
        p.sc_kad_identif,
        p.begrenzing_perceel,
        p.ka_sectie || ' ' || p.ka_perceelnummer AS aanduiding,
        p.grootte_perceel,   
        p_adr.kad_bag_koppeling_benobj,             
        p_adr.straat,
        p_adr.huisnummer,
        p_adr.huisletter,
        p_adr.toevoeging,
        p_adr.postcode,
        p_adr.woonplaats
from kad_perceel p
join v_kad_perceel_adres p_adr on (p_adr.sc_kad_identif = p.sc_kad_identif);

create or replace view v_kad_perceel_zak_recht as
  select 
    p.sc_kad_identif as Kadaster_identificatie,
    zr.AR_TELLER  as Aandeel_teller,
    zr.AR_NOEMER as Aandeel_noemer,
    zr.FK_3AVR_AAND as Aard_recht_aand,
--    ark.omschr as Aard_recht_omschrijving_verkort, XXX referentielijst niet gevuld
--    ar.omschr_aard_verkregenr_recht as Aard_recht_omschrijving, XXX referentielijst niet gevuld
    case when np.sc_identif is not null then 'Natuurlijk persoon' else 'Niet natuurlijk persoon' end as soort_eigenaar,
    np.NM_GESLACHTSNAAM as Geslachtsnaam,
    np.NM_VOORVOEGSEL_GESLACHTSNAAM as Voorvoegsel,
    np.NM_VOORNAMEN as Voornamen,
    np.GESLACHTSAAND as Geslacht,
    inp.VA_LOC_BESCHRIJVING as Woonadres,
    inp.GB_GEBOORTEDATUM as Geboortedatum,
--    inp.GB_GEBOORTELAND as Code_geboorteland, XXX in XSL conversie naar 2-letterige ISO code
    inp.GB_GEBOORTEPLAATS as Geboorteplaats,
    inp.OL_OVERLIJDENSDATUM as Overlijdensdatum,
    nnp.NAAM as Naam_niet_natuurlijk_persoon,
    innp.RECHTSVORM as Rechtsvorm,
    innp.STATUTAIRE_ZETEL as Statutaire_zetel,
    innp_subject.kvk_nummer
  from kad_perceel p
  join zak_recht zr on (zr.FK_7KOZ_KAD_IDENTIF = p.sc_kad_identif)
  left join aard_recht_verkort ark on (zr.FK_3AVR_AAND = ark.AAND)
  left join aard_verkregen_recht ar on (zr.FK_3AVR_AAND = ar.AAND)
  left join nat_prs np on (np.SC_IDENTIF = zr.FK_8PES_SC_IDENTIF)
  left join ingeschr_nat_prs inp on (inp.SC_IDENTIF = np.SC_IDENTIF)
  left join niet_nat_prs nnp on (nnp.sc_identif = zr.FK_8PES_SC_IDENTIF)
  left join ingeschr_niet_nat_prs innp on (innp.SC_IDENTIF = nnp.sc_identif)
  left join subject innp_subject on (innp_subject.identif = innp.sc_identif)
  where np.NM_GESLACHTSNAAM is not null or nnp.NAAM is not null;
  
create or replace view v_kad_perceel_zr_adressen as 
select 
  kp.SC_KAD_IDENTIF,
  kp.BEGRENZING_PERCEEL,
  kp.AANDUIDING,
  kp.GROOTTE_PERCEEL,
  kp.STRAAT,
  kp.HUISNUMMER,
  kp.HUISLETTER,
  kp.TOEVOEGING,
  kp.POSTCODE,
  kp.WOONPLAATS,
  zr.AANDEEL_TELLER,
  zr.AANDEEL_NOEMER,
  zr.AARD_RECHT_AAND,
  zr.SOORT_EIGENAAR,
  zr.GESLACHTSNAAM,
  zr.VOORVOEGSEL,
  zr.VOORNAMEN,
  zr.GESLACHT,
  zr.WOONADRES,
  zr.GEBOORTEDATUM,
  zr.GEBOORTEPLAATS,
  zr.OVERLIJDENSDATUM,
  zr.NAAM_NIET_NATUURLIJK_PERSOON,
  zr.RECHTSVORM,
  zr.STATUTAIRE_ZETEL,
  zr.KVK_NUMMER
from v_kad_perceel_eenvoudig kp
join v_kad_perceel_zak_recht zr on (zr.KADASTER_IDENTIFICATIE = kp.sc_kad_identif);

create or replace view kad_perceel_app_rechten as
select 
 kpe.SC_KAD_IDENTIF as perceel_identificatie,
-- kpe.KA_SECTIE || ' ' || kpe.KA_PERCEELNUMMER as perceelnr,
 kpe.aanduiding,
 kpe.straat, kpe.huisnummer, kpe.toevoeging, kpe.huisletter,
 kpe.straat || ' ' || kpe.huisnummer || ' ' || kpe.huisletter || ' ' || kpe.toevoeging || ' ' || kpe.postcode as adres,
-- zr.kadaster_identif as links_zak_recht,
 zr.FK_3AVR_AAND as complex_zak_recht_aard_aand,
-- zr.FK2_PERSOON as links_zak_recht_persoon,

--    case when np1.PK_PERSOON is not null then 'Natuurlijk persoon' else 'Niet natuurlijk persoon' end as l_soort_eigenaar,
    case when np1.sc_identif is not null then np1.NM_GESLACHTSNAAM || ', ' || np1.NM_VOORNAMEN || ' ' || np1.NM_VOORVOEGSEL_GESLACHTSNAAM else nnp1.NAAM end as perceel_zak_recht_naam,
--    nnp1.NAAM as l_nnp,
    
-- bd1.identificatie as brondocument, 
-- zr2.kadaster_identif as rechts_zak_recht,
 zr2.FK_3AVR_AAND as app_re_zak_recht_aard_aand,
-- zr2.FK2_PERSOON as rechts_zak_recht_persoon,

--    case when np2.PK_PERSOON is not null then 'Natuurlijk persoon' else 'Niet natuurlijk persoon' end as r_soort_eigenaar,
    case when np2.sc_identif is not null then np2.NM_GESLACHTSNAAM || ', ' || np2.NM_VOORNAMEN || ' ' || np2.NM_VOORVOEGSEL_GESLACHTSNAAM else nnp2.NAAM end as app_re_zak_recht_naam,
--    nnp2.NAAM as r_nnp,

ar.SC_KAD_IDENTIF as app_re_identificatie,
 ar.KA_APPARTEMENTSINDEX::int as appartementsindex --,
-- ar.FK1_NIET_NAT_PERSOON as app_re_vve,
-- ar_vve_nnp.naam as app_re_vve_naam,
-- ar_vve_innp.rechtsvorm as app_re_vve_rechtsvorm,
-- ar_vve_innp.rsin as app_re_vve_rsin
from v_kad_perceel_eenvoudig kpe
join zak_recht zr on (zr.FK_7KOZ_KAD_IDENTIF = kpe.SC_KAD_IDENTIF)

  left join nat_prs np1 on (np1.SC_IDENTIF = zr.FK_8PES_SC_IDENTIF)
  left join ingeschr_nat_prs inp1 on (inp1.SC_IDENTIF = np1.SC_IDENTIF)
  left join niet_nat_prs nnp1 on (nnp1.sc_identif = zr.FK_8PES_SC_IDENTIF)
  left join ingeschr_niet_nat_prs innp1 on (innp1.sc_identif = nnp1.sc_identif)

join brondocument bd1 on (bd1.tabel = 'ZAK_RECHT' and bd1.tabel_identificatie = zr.kadaster_identif)
join brondocument bd2 on (bd2.tabel = 'ZAK_RECHT' and bd2.tabel_identificatie <> zr.kadaster_identif and bd2.identificatie = bd1.identificatie)
join zak_recht zr2 on (zr2.kadaster_identif = bd2.tabel_identificatie)


  left join nat_prs np2 on (np2.SC_IDENTIF = zr2.FK_8PES_SC_IDENTIF)
  left join ingeschr_nat_prs inp2 on (inp2.SC_IDENTIF = np2.SC_IDENTIF)
  left join niet_nat_prs nnp2 on (nnp2.sc_identif = zr2.FK_8PES_SC_IDENTIF)
  left join ingeschr_niet_nat_prs innp2 on (innp2.sc_identif = nnp2.sc_identif)

join app_re ar on (ar.SC_KAD_IDENTIF = zr2.FK_7KOZ_KAD_IDENTIF)
join niet_nat_prs ar_vve_nnp on (ar_vve_nnp.sc_identif = ar.FK_2NNP_SC_IDENTIF)
join INGESCHR_NIET_NAT_PRS ar_vve_innp on (ar_vve_innp.sc_identif = ar_vve_nnp.sc_identif)
where bd1.omschrijving like 'betrokkenBij%'
and zr2.FK_8PES_SC_IDENTIF is not null
order by kpe.SC_KAD_IDENTIF, kpe.straat, kpe.huisnummer, kpe.toevoeging, kpe.huisletter,  KA_APPARTEMENTSINDEX::int;

  
-- Script: brondocument_ref_id.sql

--ALTER TABLE brondocument
--  ADD COLUMN ref_id character varying(50);
-- Script: brondocument_indices.sql

create index brondocument_tabel_idx on brondocument(tabel);
create index brondocument_tabel_identif_idx on brondocument(tabel_identificatie);
create index brondocument_identificatie_idx on brondocument(identificatie);
CREATE INDEX BRONDOCUMENT_OMSCHRIJVING_IDX ON BRONDOCUMENT(OMSCHRIJVING);

CREATE INDEX brondocument_ref_id  ON brondocument (ref_id);

-- Script: appartements_rechten.sql

-- selecteer parent en child app_re's die een ondersplitsing zijn of zijn geworden


CREATE OR REPLACE VIEW v_bd_app_re_app_re AS 
 SELECT b1.ref_id AS app_re_identif,
    b2.ref_id AS parent_app_re_identif
   FROM brondocument b1
     JOIN brondocument b2 ON b2.identificatie = b1.identificatie
  WHERE b2.omschrijving = 'betrokkenBij Ondersplitsing' AND b1.omschrijving = 'ontstaanUit Ondersplitsing'
  GROUP BY b1.ref_id, b2.ref_id;

-- recursieve query om alle appartementsrechten te vinden bij percelen
CREATE OR REPLACE VIEW v_bd_app_re_all_kad_perceel AS 
with recursive related_app_re (app_re_identif, perceel_identif) as (
SELECT b1.ref_id AS app_re_identif,
    b2.ref_id AS perceel_identif
   FROM brondocument b1
     JOIN brondocument b2 ON b2.identificatie = b1.identificatie
  WHERE b2.omschrijving = 'betrokkenBij HoofdSplitsing' AND b1.omschrijving = 'ontstaanUit HoofdSplitsing'  GROUP BY b1.ref_id, b2.ref_id

union


 SELECT vaa.app_re_identif,
    vap.perceel_identif
   FROM v_bd_app_re_app_re vaa
     JOIN related_app_re vap ON vaa.parent_app_re_identif = vap.app_re_identif
  GROUP BY vaa.app_re_identif, vap.perceel_identif
)

select 
rar.*
from related_app_re rar;


-- Haalt alle percelen ids op met 1 of meer app_re (dient als basis voor de view voor de kaart)

CREATE OR REPLACE VIEW v_bd_kad_perceel_with_app_re AS 
 SELECT DISTINCT b2.ref_id AS perceel_identif
   FROM brondocument b1
     JOIN brondocument b2 ON b2.identificatie = b1.identificatie
  WHERE b2.omschrijving = 'betrokkenBij HoofdSplitsing' AND b1.omschrijving = 'ontstaanUit HoofdSplitsing';

-- view om kaart te maken met percelen die 1 of meerdere appartementen hebben

CREATE OR REPLACE VIEW v_bd_kad_perceel_met_app AS 
 SELECT v.perceel_identif,
    kp.sc_kad_identif,
    kp.aand_soort_grootte,
    kp.grootte_perceel,
    kp.omschr_deelperceel,
    kp.fk_7kdp_sc_kad_identif,
    kp.ka_deelperceelnummer,
    kp.ka_kad_gemeentecode,
    kp.ka_perceelnummer,
    kp.ka_sectie,
    kp.begrenzing_perceel,
    kp.plaatscoordinaten_perceel
   FROM v_bd_kad_perceel_with_app_re v
     JOIN kad_perceel kp ON v.perceel_identif::numeric = kp.sc_kad_identif;



-- view om app_re' s bij percelen op te zoeken
CREATE OR REPLACE VIEW v_bd_app_re_bij_perceel AS 
 SELECT ar.sc_kad_identif,
    ar.fk_2nnp_sc_identif,
    ar.ka_appartementsindex,
    ar.ka_kad_gemeentecode,
    ar.ka_perceelnummer,
    ar.ka_sectie,
    kp.begrenzing_perceel
   FROM v_bd_app_re_all_kad_perceel v
     JOIN kad_perceel kp ON v.perceel_identif::numeric = kp.sc_kad_identif
     JOIN app_re ar ON v.app_re_identif::numeric = ar.sc_kad_identif;

-- gebruikersdoel_primary_key.sql

ALTER TABLE gebouwd_obj_gebruiksdoel
  ADD CONSTRAINT pk_geb_obj_gebr_doel PRIMARY KEY (gebruiksdoel_gebouwd_obj, fk_gbo_sc_identif);
