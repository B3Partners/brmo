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
    reeks                 VARCHAR2(255),
    registercode          VARCHAR2(5),
    soortregister         VARCHAR2(16),
    tijdstipaanbieding    TIMESTAMP,
    tijdstipondertekening TIMESTAMP,
    tekeningingeschreven  NUMBER(1)
);
CREATE TABLE stukdeel
(
    identificatie               VARCHAR2(255) NOT NULL PRIMARY KEY,
    aard                        VARCHAR2(255),
    bedragtransactiesomlevering DECIMAL(9, 0),
    datumkenbaarheidpb          DATE,
    deelvan                     VARCHAR2(255) NOT NULL REFERENCES stuk (identificatie)
);
CREATE TABLE onroerendezaak
(
    identificatie                 VARCHAR2(255) NOT NULL PRIMARY KEY,
    akrkadastralegemeentecode     DECIMAL(4, 0),
    akrkadastralegemeente         VARCHAR2(5),
    kadastralegemeentecode        DECIMAL(4, 0),
    kadastralegemeente            VARCHAR2(30),
    sectie                        VARCHAR2(2),
    perceelnummer                 DECIMAL(5, 0),
    appartementsrechtvolgnummer   DECIMAL(4, 0),
    landinrichtingsrente_bedrag   DECIMAL(9, 0),
    landinrichtingsrente_jaar     INTEGER,
    aard_cultuur_onbebouwd        VARCHAR2(65),
    aard_cultuur_bebouwd          VARCHAR2(65),
    koopsom_bedrag                DECIMAL(9, 0),
    koopsom_koopjaar              INTEGER,
    koopsom_indicatiemeerobjecten NUMBER(1),
    toelichtingbewaarder          VARCHAR2(4000 BYTE),
    tijdstipontstaanobject        TIMESTAMP,
    oudstdigitaalbekend           TIMESTAMP,
    ontstaanuit                   VARCHAR2(255),
    begingeldigheid               DATE          NOT NULL
);
CREATE TABLE archief_onroerendezaak
(
    identificatie                 VARCHAR2(255) NOT NULL,
    akrkadastralegemeentecode     DECIMAL(4, 0),
    akrkadastralegemeente         VARCHAR2(5),
    kadastralegemeentecode        DECIMAL(4, 0),
    kadastralegemeente            VARCHAR2(30),
    sectie                        VARCHAR2(2),
    perceelnummer                 DECIMAL(5, 0),
    appartementsrechtvolgnummer   DECIMAL(4, 0),
    landinrichtingsrente_bedrag   DECIMAL(9, 0),
    landinrichtingsrente_jaar     INTEGER,
    aard_cultuur_onbebouwd        VARCHAR2(65),
    aard_cultuur_bebouwd          VARCHAR2(65),
    koopsom_bedrag                DECIMAL(9, 0),
    koopsom_koopjaar              INTEGER,
    koopsom_indicatiemeerobjecten NUMBER(1),
    toelichtingbewaarder          VARCHAR2(4000 BYTE),
    tijdstipontstaanobject        TIMESTAMP,
    oudstdigitaalbekend           TIMESTAMP,
    ontstaanuit                   VARCHAR2(255),
    begingeldigheid               DATE          NOT NULL,
    PRIMARY KEY (identificatie, begingeldigheid)
);
CREATE TABLE adres
(
    identificatie        VARCHAR2(255) PRIMARY KEY NOT NULL,
    huisnummer           NUMERIC(4, 0),
    huisletter           VARCHAR2(1),
    huisnummertoevoeging VARCHAR2(4),
    postbusnummer        NUMERIC(6, 0),
    postcode             VARCHAR2(6),
    openbareruimtenaam   VARCHAR2(80),
    woonplaatsnaam       VARCHAR2(80),
    openbareruimte       VARCHAR2(16),
    verblijfsobject      VARCHAR2(16),
    adresseerbaarobject  VARCHAR2(16),
    nummeraanduiding     VARCHAR2(16),
    standplaats          VARCHAR2(16),
    ligplaats            VARCHAR2(16),
    nevenadres           VARCHAR2(255),
    hoofdadres           VARCHAR2(255),
    koppelingswijze      VARCHAR2(29),
    buitenlandadres      VARCHAR2(200),
    buitenlandwoonplaats VARCHAR2(200),
    buitenlandregio      VARCHAR2(150),
    land                 VARCHAR2(40),
    onroerendezaak       VARCHAR2(255) REFERENCES onroerendezaak (identificatie)
);
CREATE TABLE persoon
(
    identificatie                 VARCHAR2(255) NOT NULL PRIMARY KEY,
    beschikkingsbevoegdheid       VARCHAR2(33),
    indicatieniettoonbarediakriet NUMBER(1),
    postlocatie                   VARCHAR2(255) REFERENCES adres (identificatie),
    woonlocatie                   VARCHAR2(255) REFERENCES adres (identificatie),
    soort                         VARCHAR2(21) CHECK ( soort IN ('natuurlijkpersoon', 'nietnatuurlijkpersoon') )
);
CREATE TABLE natuurlijkpersoon
(
    identificatie                        VARCHAR2(255) NOT NULL PRIMARY KEY,
    indicatieoverleden                   NUMBER(1),
    indicatieafschermingpersoonsgegevens NUMBER(1),
    betreft                              VARCHAR2(255),
    bsn                                  VARCHAR2(255),
    adellijketitelofpredicaat            VARCHAR2(10),
    aanduidingnaamgebruik                VARCHAR2(72),
    landwaarnaarvertrokken               VARCHAR2(40),
    geslachtsnaam                        VARCHAR2(200),
    voornamen                            VARCHAR2(200),
    voorvoegselsgeslachtsnaam            VARCHAR2(10),
    geslacht                             VARCHAR2(8),
    geboortedatum                        VARCHAR2(4),
    geboorteplaats                       VARCHAR2(80),
    geboorteland                         VARCHAR2(40),
    indicatiegeheim                      NUMBER(1),
    datumoverlijden                      VARCHAR2(4),
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
    betreft         VARCHAR2(255),
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
    bevoegdgezag     VARCHAR2(255)
);
CREATE TABLE onroerendezaakbeperking
(
    identificatie VARCHAR2(255) PRIMARY KEY NOT NULL,
    inonderzoek   NUMBER(1),
    beperkt       VARCHAR2(255) REFERENCES onroerendezaak (identificatie),
    leidttot      VARCHAR2(255) REFERENCES publiekrechtelijkebeperking (identificatie)
);
CREATE TABLE onroerendezaakfiliatie
(
    aard            VARCHAR2(65),
    betreft         VARCHAR2(255) REFERENCES onroerendezaak (identificatie) ON DELETE CASCADE,
    begingeldigheid DATE NOT NULL
);
CREATE TABLE archief_onroerendezaakfiliatie
(
    aard            VARCHAR2(65),
    betreft         VARCHAR2(255) NOT NULL,
    begingeldigheid DATE          NOT NULL,
    PRIMARY KEY (betreft, begingeldigheid)
);
CREATE TABLE perceel
(
    identifcatie           VARCHAR2(255) NOT NULL PRIMARY KEY,
    begrenzing_perceel     SDO_GEOMETRY  NOT NULL,
    kadastralegrootte      DECIMAL(9, 1),
    soortgrootte           VARCHAR2(100),
    perceelnummerrotatie   DECIMAL(3, 1),
    perceelnummer_deltax   DECIMAL(10, 10),
    perceelnummer_deltay   DECIMAL(10, 10),
    plaatscoordinaten      SDO_GEOMETRY  NOT NULL,
    meettariefverschuldigd NUMBER(1),
    begingeldigheid        DATE          NOT NULL
);
CREATE TABLE archief_perceel
(
    identificatie          VARCHAR2(255) NOT NULL,
    begrenzing_perceel     SDO_GEOMETRY  NOT NULL,
    kadastralegrootte      DECIMAL(9, 1),
    soortgrootte           VARCHAR2(100),
    perceelnummerrotatie   DECIMAL(3, 1),
    perceelnummer_deltax   DECIMAL(10, 10),
    perceelnummer_deltay   DECIMAL(10, 10),
    plaatscoordinaten      SDO_GEOMETRY  NOT NULL,
    meettariefverschuldigd NUMBER(1),
    begingeldigheid        DATE          NOT NULL,
    PRIMARY KEY (identificatie, begingeldigheid)
);
CREATE TABLE recht
(
    identificatie                          VARCHAR2(255) NOT NULL PRIMARY KEY,
    aard                                   VARCHAR2(255),
    toelichtingbewaarder                   VARCHAR2(4000 BYTE),
    isbelastmet                            VARCHAR2(255) REFERENCES recht (identificatie),
    isgebaseerdop                          VARCHAR2(255) REFERENCES stukdeel (identificatie),
    rustop                                 VARCHAR2(255) REFERENCES onroerendezaak (identificatie),
    isontstaanuit                          VARCHAR2(255) REFERENCES recht (identificatie),
    isbetrokkenbij                         VARCHAR2(255) REFERENCES recht (identificatie),
    isbestemdtot                           VARCHAR2(255) REFERENCES recht (identificatie),
    isbeperkttot                           VARCHAR2(255) REFERENCES recht (identificatie),
    soort                                  VARCHAR2(22),
    jaarlijksbedrag                        DECIMAL(9, 0),
    jaarlijksbedragbetreftmeerdere_oz      NUMBER(1),
    einddatumafkoop                        DATE,
    indicatieoudeonroerendezaakbetrokken   NUMBER(1),
    heefthoofdzaak                         VARCHAR2(255) REFERENCES onroerendezaak (identificatie),
    heeftverenigingvaneigenaren            VARCHAR2(255) REFERENCES nietnatuurlijkpersoon (identificatie),
    aandeel_teller                         DECIMAL(32, 0),
    aandeel_noemer                         DECIMAL(32, 0),
    burgerlijkestaattentijdevanverkrijging VARCHAR2(43),
    verkregennamenssamenwerkingsverband    VARCHAR2(26),
    betrokkenpartner                       VARCHAR2(255) REFERENCES natuurlijkpersoon (identificatie),
    geldtvoor                              VARCHAR2(255) REFERENCES recht (identificatie),
    betrokkensamenwerkingsverband          VARCHAR2(255) REFERENCES nietnatuurlijkpersoon (identificatie),
    betrokkengorzenenaanwassen             VARCHAR2(255) REFERENCES nietnatuurlijkpersoon (identificatie),
    tennamevan                             VARCHAR2(255) REFERENCES persoon (identificatie),
    omschrijving                           VARCHAR2(4000),
    einddatumrecht                         DATE,
    einddatum                              DATE,
    betreftgedeeltevanperceel              NUMBER(1),
    aantekeningrecht                       VARCHAR2(255) REFERENCES recht (identificatie),
    aantekeningkadastraalobject            VARCHAR2(255) REFERENCES onroerendezaak (identificatie),
    betrokkenpersoon                       VARCHAR2(255) REFERENCES persoon (identificatie),
    begingeldigheid                        DATE          NOT NULL
);
CREATE TABLE archief_recht
(
    identificatie                          VARCHAR2(255) NOT NULL,
    aard                                   VARCHAR2(255),
    toelichtingbewaarder                   VARCHAR2(4000),
    isbelastmet                            VARCHAR2(255) REFERENCES recht (identificatie),
    isgebaseerdop                          VARCHAR2(255) REFERENCES stukdeel (identificatie),
    rustop                                 VARCHAR2(255) REFERENCES onroerendezaak (identificatie),
    isontstaanuit                          VARCHAR2(255) REFERENCES recht (identificatie),
    isbetrokkenbij                         VARCHAR2(255) REFERENCES recht (identificatie),
    isbestemdtot                           VARCHAR2(255) REFERENCES recht (identificatie),
    isbeperkttot                           VARCHAR2(255) REFERENCES recht (identificatie),
    soort                                  VARCHAR2(22),
    jaarlijksbedrag                        DECIMAL(9, 0),
    jaarlijksbedragbetreftmeerdere_oz      NUMBER(1),
    einddatumafkoop                        DATE,
    indicatieoudeonroerendezaakbetrokken   NUMBER(1),
    heefthoofdzaak                         VARCHAR2(255) REFERENCES onroerendezaak (identificatie),
    heeftverenigingvaneigenaren            VARCHAR2(255) REFERENCES nietnatuurlijkpersoon (identificatie),
    aandeel_teller                         DECIMAL(32, 0),
    aandeel_noemer                         DECIMAL(32, 0),
    burgerlijkestaattentijdevanverkrijging VARCHAR2(43),
    verkregennamenssamenwerkingsverband    VARCHAR2(26),
    betrokkenpartner                       VARCHAR2(255) REFERENCES natuurlijkpersoon (identificatie),
    geldtvoor                              VARCHAR2(255) REFERENCES recht (identificatie),
    betrokkensamenwerkingsverband          VARCHAR2(255) REFERENCES nietnatuurlijkpersoon (identificatie),
    betrokkengorzenenaanwassen             VARCHAR2(255) REFERENCES nietnatuurlijkpersoon (identificatie),
    tennamevan                             VARCHAR2(255) REFERENCES persoon (identificatie),
    omschrijving                           VARCHAR2(4000 BYTE),
    einddatumrecht                         DATE,
    einddatum                              DATE,
    betreftgedeeltevanperceel              NUMBER(1),
    aantekeningrecht                       VARCHAR2(255) REFERENCES recht (identificatie),
    aantekeningkadastraalobject            VARCHAR2(255) REFERENCES onroerendezaak (identificatie),
    betrokkenpersoon                       VARCHAR2(255) REFERENCES persoon (identificatie),
    begingeldigheid                        DATE          NOT NULL,
    PRIMARY KEY (identificatie, begingeldigheid)
);
CREATE TABLE appartementsrecht
(
    identifcatie    VARCHAR2(255) NOT NULL PRIMARY KEY,
    hoofdsplitsing  VARCHAR2(255) NOT NULL REFERENCES recht (identificatie),
    begingeldigheid DATE          NOT NULL
);
CREATE TABLE archief_appartementsrecht
(
    identificatie   VARCHAR2(255) NOT NULL,
    hoofdsplitsing  VARCHAR2(255) NOT NULL REFERENCES recht (identificatie),
    begingeldigheid DATE          NOT NULL,
    PRIMARY KEY (identificatie, begingeldigheid)
);
