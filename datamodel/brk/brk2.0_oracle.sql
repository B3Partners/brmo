-- https://docs.oracle.com/en/database/oracle/oracle-database/19/refrn/NLS_LENGTH_SEMANTICS.html#GUID-221B0A5E-A17A-4CBC-8309-3A79508466F9
ALTER SESSION SET NLS_LENGTH_SEMANTICS='CHAR';
SET DEFINE OFF;
WHENEVER SQLERROR EXIT sql.sqlcode;
CREATE TABLE stuk
(
    identificatie         VARCHAR2(255) NOT NULL PRIMARY KEY,
    toelichtingbewaarder  VARCHAR2(4000 BYTE),
    portefeuillenummer    VARCHAR2(16),
    deel                  VARCHAR2(5),
    nummer                VARCHAR2(5),
    reeks                 VARCHAR2(19),
    registercode          VARCHAR2(5),
    soortregister         VARCHAR2(16),
    tijdstipaanbieding    TIMESTAMP,
    tijdstipondertekening TIMESTAMP,
    tekeningingeschreven  NUMBER(1, 0) CHECK (tekeningingeschreven in (0, 1))
);

CREATE TABLE stukdeel
(
    identificatie               VARCHAR2(255) NOT NULL PRIMARY KEY,
    aard                        VARCHAR2(255),
    bedragtransactiesomlevering DECIMAL(20, 2),
    valutatransactiesomlevering VARCHAR2(42),
    datumkenbaarheidpb          DATE,
    deelvan                     VARCHAR2(255) NOT NULL REFERENCES stuk (identificatie)
);

CREATE TABLE onroerendezaak
(
    identificatie                 VARCHAR2(255) NOT NULL PRIMARY KEY,
    begingeldigheid               DATE          NOT NULL,
    eindegeldigheid               DATE,
    akrkadastralegemeentecode     DECIMAL(4, 0),
    akrkadastralegemeente         VARCHAR2(5),
    kadastralegemeentecode        DECIMAL(4, 0),
    kadastralegemeente            VARCHAR2(30),
    sectie                        VARCHAR2(2),
    perceelnummer                 DECIMAL(5, 0),
    appartementsrechtvolgnummer   DECIMAL(4, 0),
    landinrichtingsrente_bedrag   DECIMAL(20, 2),
    landinrichtingsrente_valuta   VARCHAR(42),
    landinrichtingsrente_jaar     INTEGER,
    aard_cultuur_onbebouwd        VARCHAR2(65),
    aard_cultuur_bebouwd          VARCHAR2(65),
    koopsom_bedrag                DECIMAL(20, 2),
    koopsom_valuta                VARCHAR(42),
    koopsom_koopjaar              INTEGER,
    koopsom_indicatiemeerobjecten NUMBER(1, 0) CHECK (koopsom_indicatiemeerobjecten in (0, 1)),
    toelichtingbewaarder          VARCHAR2(4000 BYTE),
    tijdstipontstaanobject        TIMESTAMP,
    oudstdigitaalbekend           TIMESTAMP
);
CREATE TABLE onroerendezaak_archief
(
    identificatie                 VARCHAR2(255) NOT NULL,
    begingeldigheid               DATE          NOT NULL,
    eindegeldigheid               DATE          NOT NULL,
    akrkadastralegemeentecode     DECIMAL(4, 0),
    akrkadastralegemeente         VARCHAR2(5),
    kadastralegemeentecode        DECIMAL(4, 0),
    kadastralegemeente            VARCHAR2(30),
    sectie                        VARCHAR2(2),
    perceelnummer                 DECIMAL(5, 0),
    appartementsrechtvolgnummer   DECIMAL(4, 0),
    landinrichtingsrente_bedrag   DECIMAL(20, 2),
    landinrichtingsrente_valuta   VARCHAR2(42),
    landinrichtingsrente_jaar     INTEGER,
    aard_cultuur_onbebouwd        VARCHAR2(65),
    aard_cultuur_bebouwd          VARCHAR2(65),
    koopsom_bedrag                DECIMAL(20, 2),
    koopsom_valuta                VARCHAR2(42),
    koopsom_koopjaar              INTEGER,
    koopsom_indicatiemeerobjecten NUMBER(1, 0) CHECK (koopsom_indicatiemeerobjecten in (0, 1)),
    toelichtingbewaarder          VARCHAR2(4000 BYTE),
    tijdstipontstaanobject        TIMESTAMP,
    oudstdigitaalbekend           TIMESTAMP,
    ontstaanuit                   VARCHAR2(255),
    PRIMARY KEY (identificatie, begingeldigheid)
);
CREATE TABLE adres
(
    identificatie        VARCHAR2(255) PRIMARY KEY NOT NULL,
    huisnummer           NUMERIC(5, 0),
    huisletter           VARCHAR2(1),
    huisnummertoevoeging VARCHAR2(4),
    postbusnummer        NUMERIC(6, 0),
    postcode             VARCHAR2(6),
    openbareruimtenaam   VARCHAR2(80),
    woonplaatsnaam       VARCHAR2(80),
    nummeraanduiding     VARCHAR2(16),
    adresseerbaarobject  VARCHAR2(16),
    verblijfsobject      VARCHAR2(16),
    standplaats          VARCHAR2(16),
    ligplaats            VARCHAR2(16),
    hoofdadres           VARCHAR2(16),
    nevenadres           VARCHAR2(16),
    buitenlandadres      VARCHAR2(200),
    buitenlandwoonplaats VARCHAR2(200),
    buitenlandregio      VARCHAR2(150),
    land                 VARCHAR2(40)
);

CREATE TABLE objectlocatie
(
    heeft           VARCHAR2(255) REFERENCES onroerendezaak (identificatie),
    betreft         VARCHAR2(255) REFERENCES adres (identificatie),
    koppelingswijze VARCHAR(29),
    PRIMARY KEY (heeft, betreft)
);

CREATE TABLE objectlocatie_archief
(
    heeft           VARCHAR2(255) NOT NULL,
    betreft         VARCHAR2(255) NOT NULL,
    koppelingswijze VARCHAR(29),
    begingeldigheid DATE          NOT NULL,
    PRIMARY KEY (heeft, betreft, begingeldigheid)
);

CREATE TABLE persoon
(
    identificatie                 VARCHAR2(255) NOT NULL PRIMARY KEY,
    beschikkingsbevoegdheid       VARCHAR2(33),
    indicatieniettoonbarediakriet NUMBER(1, 0) CHECK (indicatieniettoonbarediakriet in (0, 1)),
    postlocatie                   VARCHAR2(255) REFERENCES adres (identificatie),
    woonlocatie                   VARCHAR2(255) REFERENCES adres (identificatie),
    soort                         VARCHAR2(21) CHECK ( soort IN ('natuurlijkpersoon', 'nietnatuurlijkpersoon') )
);
CREATE TABLE natuurlijkpersoon
(
    identificatie                        VARCHAR2(255) NOT NULL PRIMARY KEY,
    indicatieoverleden                   NUMBER(1, 0) CHECK (indicatieoverleden in (0, 1)),
    indicatieafschermingpersoonsgegevens NUMBER(1, 0) CHECK (indicatieafschermingpersoonsgegevens in (0, 1)),
    bsn                                  VARCHAR2(255),
    adellijketitelofpredicaat            VARCHAR2(10),
    aanduidingnaamgebruik                VARCHAR2(72),
    landwaarnaarvertrokken               VARCHAR2(40),
    geslachtsnaam                        VARCHAR2(200),
    voornamen                            VARCHAR2(200),
    voorvoegselsgeslachtsnaam            VARCHAR2(10),
    geslacht                             VARCHAR2(8),
    geboortedatum                        VARCHAR2(10),
    geboorteplaats                       VARCHAR2(80),
    geboorteland                         VARCHAR2(40),
    indicatiegeheim                      NUMBER(1, 0) CHECK (indicatiegeheim in (0, 1)),
    datumoverlijden                      VARCHAR2(10),
    partnergeslachtsnaam                 VARCHAR2(200),
    partnervoornamen                     VARCHAR2(200),
    partnervoorvoegselsgeslachtsnaam     VARCHAR2(10)
);
CREATE TABLE nietnatuurlijkpersoon
(
    identificatie   VARCHAR2(255) NOT NULL PRIMARY KEY,
    statutairenaam  VARCHAR2(200),
    rechtsvorm      VARCHAR2(52),
    statutairezetel VARCHAR2(40),
    rsin            VARCHAR2(9),
    kvknummer       VARCHAR2(8)
);
CREATE TABLE publiekrechtelijkebeperking
(
    identificatie    VARCHAR2(255) PRIMARY KEY NOT NULL,
    grondslag        VARCHAR2(255),
    datuminwerking   DATE,
    datumbeeindiging DATE,
    isgebaseerdop    VARCHAR2(255) REFERENCES stukdeel (identificatie),
    bevoegdgezag     VARCHAR2(255) REFERENCES nietnatuurlijkpersoon (identificatie)
);


CREATE TABLE onroerendezaakbeperking
(
    inonderzoek NUMBER(1, 0) CHECK (inonderzoek in (0, 1)),
    beperkt     VARCHAR2(255) REFERENCES onroerendezaak (identificatie),
    leidttot    VARCHAR2(255) REFERENCES publiekrechtelijkebeperking (identificatie),
    PRIMARY KEY (beperkt, leidttot)
);

CREATE TABLE onroerendezaakbeperking_archief
(
    inonderzoek     NUMBER(1, 0) CHECK (inonderzoek in (0, 1)),
    beperkt         VARCHAR2(255) NOT NULL,
    leidttot        VARCHAR2(255) REFERENCES publiekrechtelijkebeperking (identificatie),
    begingeldigheid DATE          NOT NULL,
    PRIMARY KEY (beperkt, leidttot, begingeldigheid)
);

CREATE TABLE onroerendezaakfiliatie
(
    aard            VARCHAR2(65) NOT NULL,
    onroerendezaak  VARCHAR2(255),
    betreft         VARCHAR2(255),
    PRIMARY KEY (aard, onroerendezaak, betreft)
);

CREATE TABLE perceel
(
    identificatie          VARCHAR2(255) NOT NULL PRIMARY KEY,
    begrenzing_perceel     SDO_GEOMETRY  NOT NULL,
    kadastralegrootte      DECIMAL(9, 1),
    soortgrootte           VARCHAR2(100),
    perceelnummerrotatie   DECIMAL(3, 1),
    perceelnummer_deltax   DECIMAL(20, 10),
    perceelnummer_deltay   DECIMAL(20, 10),
    plaatscoordinaten      SDO_GEOMETRY  NOT NULL,
    meettariefverschuldigd NUMBER(1, 0) CHECK (meettariefverschuldigd in (0, 1))
);
CREATE TABLE perceel_archief
(
    identificatie          VARCHAR2(255) NOT NULL,
    begingeldigheid        DATE          NOT NULL,
    begrenzing_perceel     SDO_GEOMETRY  NOT NULL,
    kadastralegrootte      DECIMAL(9, 1),
    soortgrootte           VARCHAR2(100),
    perceelnummerrotatie   DECIMAL(3, 1),
    perceelnummer_deltax   DECIMAL(20, 10),
    perceelnummer_deltay   DECIMAL(20, 10),
    plaatscoordinaten      SDO_GEOMETRY  NOT NULL,
    meettariefverschuldigd NUMBER(1, 0) CHECK (meettariefverschuldigd in (0, 1)),
    PRIMARY KEY (identificatie, begingeldigheid)
);
CREATE TABLE recht
(
    identificatie                          VARCHAR2(255) NOT NULL PRIMARY KEY,
    aard                                   VARCHAR2(255),
    toelichtingbewaarder                   VARCHAR2(4000 BYTE),
    isgebaseerdop                          VARCHAR2(255) REFERENCES stukdeel (identificatie),
    isgebaseerdop2                         VARCHAR2(255) REFERENCES stukdeel (identificatie),
    betreft                                VARCHAR2(255) REFERENCES recht (identificatie),
    rustop                                 VARCHAR2(255) REFERENCES onroerendezaak (identificatie),
    isontstaanuit                          VARCHAR2(255) REFERENCES recht (identificatie),
    isbetrokkenbij                         VARCHAR2(255) REFERENCES recht (identificatie),
    isbestemdtot                           VARCHAR2(255) REFERENCES recht (identificatie),
    soort                                  VARCHAR2(22),
    jaarlijksbedrag                        DECIMAL(20, 2),
    jaarlijksbedrag_valuta                 VARCHAR(42),
    jaarlijksbedragbetreftmeerdere_oz      NUMBER(1, 0) CHECK (jaarlijksbedragbetreftmeerdere_oz in (0, 1)),
    einddatumafkoop                        DATE,
    indicatieoudeonroerendezaakbetrokken   NUMBER(1, 0) CHECK (indicatieoudeonroerendezaakbetrokken in (0, 1)),
    heefthoofdzaak                         VARCHAR2(255) REFERENCES onroerendezaak (identificatie),
    heeftverenigingvaneigenaren            VARCHAR2(255) REFERENCES nietnatuurlijkpersoon (identificatie),
    aandeel_teller                         DECIMAL(32, 0),
    aandeel_noemer                         DECIMAL(32, 0),
    burgerlijkestaattentijdevanverkrijging VARCHAR2(43),
    verkregennamenssamenwerkingsverband    VARCHAR2(26),
    van                                    VARCHAR2(255) REFERENCES recht (identificatie),
    betrokkenpartner                       VARCHAR2(255) REFERENCES natuurlijkpersoon (identificatie),
    geldtvoor                              VARCHAR2(255) REFERENCES recht (identificatie),
    betrokkensamenwerkingsverband          VARCHAR2(255) REFERENCES nietnatuurlijkpersoon (identificatie),
    betrokkengorzenenaanwassen             VARCHAR2(255) REFERENCES nietnatuurlijkpersoon (identificatie),
    tennamevan                             VARCHAR2(255) REFERENCES persoon (identificatie),
    omschrijving                           VARCHAR2(4000),
    einddatumrecht                         DATE,
    einddatum                              DATE,
    betreftgedeeltevanperceel              NUMBER(1, 0) CHECK (betreftgedeeltevanperceel in (0, 1)),
    aantekeningkadastraalobject            VARCHAR2(255) REFERENCES onroerendezaak (identificatie),
    betrokkenpersoon                       VARCHAR2(255) REFERENCES persoon (identificatie),
    begingeldigheid                        DATE          NOT NULL
);

-- koppeltabellen voor 1:n (n>1) recht:recht relaties
CREATE TABLE recht_aantekeningrecht
(
    aantekening    VARCHAR2(255) REFERENCES recht (identificatie),
    tenaamstelling VARCHAR2(255) REFERENCES recht (identificatie),
    PRIMARY KEY (aantekening, tenaamstelling)
);
CREATE TABLE recht_isbelastmet
(
    zakelijkrecht VARCHAR2(255) REFERENCES recht (identificatie),
    isbelastmet   VARCHAR2(255) REFERENCES recht (identificatie),
    PRIMARY KEY (zakelijkrecht, isbelastmet)
);
CREATE TABLE recht_isbeperkttot
(
    zakelijkrecht  VARCHAR2(255) REFERENCES recht (identificatie),
    tenaamstelling VARCHAR2(255) NOT NULL,
    PRIMARY KEY (zakelijkrecht, tenaamstelling)
);

CREATE TABLE recht_archief
(
    identificatie                          VARCHAR2(255) NOT NULL,
    aard                                   VARCHAR2(255),
    toelichtingbewaarder                   VARCHAR2(4000),
    -- stukdeel referentie
    -- REFERENCES stukdeel (identificatie)
    isgebaseerdop                          VARCHAR2(255),
    -- REFERENCES stukdeel (identificatie)
    isgebaseerdop2                         VARCHAR2(255),
    -- zakelijke recht referentie
    -- relatie Recht:Erfpachtcanon/Recht:betreft/Recht-ref:ZakelijkRechtRef
    -- REFERENCES recht (identificatie)
    betreft                                VARCHAR2(255),
    -- OZ referentie
    -- REFERENCES onroerendezaak (identificatie)
    rustop                                 VARCHAR2(255),
    -- een splitsing ref
    -- REFERENCES recht (identificatie)
    isontstaanuit                          VARCHAR2(255),
    -- een splitsing ref
    -- REFERENCES recht (identificatie)
    isbetrokkenbij                         VARCHAR2(255),
    -- Mandeligheid ref
    -- REFERENCES recht (identificatie)
    isbestemdtot                           VARCHAR2(255),
    soort                                  VARCHAR2(22),
    jaarlijksbedrag                        DECIMAL(20, 2),
    jaarlijksbedrag_valuta                 VARCHAR2(42),
    jaarlijksbedragbetreftmeerdere_oz      NUMBER(1, 0) CHECK (jaarlijksbedragbetreftmeerdere_oz in (0, 1)),
    einddatumafkoop                        DATE,
    indicatieoudeonroerendezaakbetrokken   NUMBER(1, 0) CHECK (indicatieoudeonroerendezaakbetrokken in (0, 1)),
    -- REFERENCES onroerendezaak (identificatie)
    heefthoofdzaak                         VARCHAR2(255),
    -- NNP verwijzing
    -- REFERENCES nietnatuurlijkpersoon (identificatie)
    heeftverenigingvaneigenaren            VARCHAR2(255),
    aandeel_teller                         DECIMAL(32, 0),
    aandeel_noemer                         DECIMAL(32, 0),
    burgerlijkestaattentijdevanverkrijging VARCHAR2(43),
    verkregennamenssamenwerkingsverband    VARCHAR2(26),
    -- relatie Recht:Tenaamstelling/Recht:van/Recht-ref:ZakelijkRechtRef
    -- REFERENCES recht (identificatie)
    van                                    VARCHAR2(255),
    -- NP verwijzing
    -- REFERENCES natuurlijkpersoon (identificatie)
    betrokkenpartner                       VARCHAR2(255),
    -- GezamenlijkAandeelRef
    -- REFERENCES recht (identificatie)
    geldtvoor                              VARCHAR2(255),
    -- NNP verwijzing
    -- REFERENCES nietnatuurlijkpersoon (identificatie)
    betrokkensamenwerkingsverband          VARCHAR2(255),
    -- NNP verwijzing
    -- REFERENCES nietnatuurlijkpersoon (identificatie)
    betrokkengorzenenaanwassen             VARCHAR2(255),
    -- NNP of NP verwijzing
    -- REFERENCES persoon (identificatie)
    tennamevan                             VARCHAR2(255),
    omschrijving                           VARCHAR2(4000),
    einddatumrecht                         DATE,
    einddatum                              DATE,
    betreftgedeeltevanperceel              NUMBER(1, 0) CHECK (betreftgedeeltevanperceel in (0, 1)),
    -- REFERENCES onroerendezaak (identificatie)
    aantekeningkadastraalobject            VARCHAR2(255),
    betrokkenpersoon                       VARCHAR2(255),
    begingeldigheid                        DATE          NOT NULL,
    eindegeldigheid                        DATE          NOT NULL,
    PRIMARY KEY (identificatie, begingeldigheid)
);

CREATE TABLE recht_aantekeningrecht_archief
(
    aantekening     VARCHAR(255) NOT NULL,
    tenaamstelling  VARCHAR(255) NOT NULL,
    begingeldigheid DATE         NOT NULL,
    PRIMARY KEY (aantekening, tenaamstelling, begingeldigheid)
);
CREATE TABLE recht_isbelastmet_archief
(
    zakelijkrecht   VARCHAR(255) NOT NULL,
    isbelastmet     VARCHAR(255) NOT NULL,
    begingeldigheid DATE         NOT NULL,
    PRIMARY KEY (zakelijkrecht, isbelastmet, begingeldigheid)
);
CREATE TABLE recht_isbeperkttot_archief
(
    zakelijkrecht   VARCHAR(255) NOT NULL,
    tenaamstelling  VARCHAR(255) NOT NULL,
    begingeldigheid DATE         NOT NULL,
    PRIMARY KEY (zakelijkrecht, tenaamstelling, begingeldigheid)
);

CREATE TABLE appartementsrecht
(
    identificatie  VARCHAR2(255) NOT NULL PRIMARY KEY,
    hoofdsplitsing VARCHAR2(255) NOT NULL
    -- begingeldigheid DATE          NOT NULL
);
CREATE TABLE appartementsrecht_archief
(
    identificatie   VARCHAR2(255) NOT NULL,
    begingeldigheid DATE          NOT NULL,
    hoofdsplitsing  VARCHAR2(255) NOT NULL REFERENCES recht (identificatie),
    PRIMARY KEY (identificatie, begingeldigheid)
);


CREATE TABLE brmo_metadata
(
    naam   VARCHAR2(255 CHAR) NOT NULL PRIMARY KEY,
    waarde VARCHAR2(255 CHAR)
);

-- brmo versienummer
INSERT INTO brmo_metadata (naam, waarde)
VALUES ('brmoversie', '${project.version}');

INSERT INTO user_sdo_geom_metadata
VALUES ('PERCEEL', 'BEGRENZING_PERCEEL', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
                                                             MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
INSERT INTO user_sdo_geom_metadata
VALUES ('PERCEEL', 'PLAATSCOORDINATEN', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
                                                            MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)), 28992);
INSERT INTO user_sdo_geom_metadata
VALUES ('PERCEEL_ARCHIEF', 'BEGRENZING_PERCEEL', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
                                                                     MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
        28992);
INSERT INTO user_sdo_geom_metadata
VALUES ('PERCEEL_ARCHIEF', 'PLAATSCOORDINATEN', MDSYS.SDO_DIM_ARRAY(MDSYS.SDO_DIM_ELEMENT('X', 12000, 280000, .1),
                                                                    MDSYS.SDO_DIM_ELEMENT('Y', 304000, 620000, .1)),
        28992);

CREATE INDEX perceel_begrenzing_perceel ON perceel (begrenzing_perceel) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX perceel_plaatscoordinaten ON perceel (plaatscoordinaten) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=POINT');
CREATE INDEX perceel_archief_begrenzing_perceel ON perceel_archief (begrenzing_perceel) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=MULTIPOLYGON');
CREATE INDEX perceel_archief_plaatscoordinaten ON perceel_archief (plaatscoordinaten) INDEXTYPE IS MDSYS.SPATIAL_INDEX PARAMETERS ( 'LAYER_GTYPE=POINT');
