-- oracle

DROP TABLE ONBEGROEID_TERREINDEEL;

CREATE TABLE ONBEGROEID_TERREINDEEL (
  identif VARCHAR2(255 BYTE) NOT NULL,
  dat_beg_geldh date,
  datum_einde_geldh date,
  relve_hoogteligging NUMBER(1,0),
  bgt_status VARCHAR2(255 BYTE),
  plus_status VARCHAR2(255 BYTE),
  bgt_fysiekvoorkomen VARCHAR2(255 BYTE),
  optalud char(5),
  plus_fysiekvoorkomen VARCHAR2(255 BYTE),
  kruinlijn SDO_GEOMETRY,
  geom2d SDO_GEOMETRY,
  CONSTRAINT onbegroeid_terreindeel_pkey PRIMARY KEY (identif)
);

-- postgres

DROP TABLE bgttest.onbegroeid_terreindeel;

CREATE TABLE bgttest.onbegroeid_terreindeel
(
  identif character varying NOT NULL,
  dat_beg_geldh date,
  datum_einde_geldh date,
  relve_hoogteligging integer,
  bgt_status character varying,
  plus_status character varying,
  bgt_fysiekvoorkomen character varying,
  optalud boolean,
  plus_fysiekvoorkomen character varying,
  geom2d geometry(Polygon),
  kruinlijn geometry(LineString),
  CONSTRAINT onbegroeid_terreindeel_pkey PRIMARY KEY (identif)
)
WITH (
  OIDS=FALSE
);

-- sql server

DROP TABLE onbegroeid_terreindeel;
GO

CREATE TABLE onbegroeid_terreindeel (
  identif character varying(255) NOT NULL,
  dat_beg_geldh date,
  datum_einde_geldh date,
  relve_hoogteligging integer,
  bgt_status character varying(255),
  plus_status character varying(255),
  bgt_fysiekvoorkomen character varying(255),
  optalud char(5),
  plus_fysiekvoorkomen character varying(255),
  kruinlijn geometry,
  geom2d geometry,
  CONSTRAINT onbegroeid_terreindeel_pkey PRIMARY KEY (identif)
);
