
    create table automatisch_proces (
        dtype varchar(255) not null,
        id  bigserial not null,
        cron_expressie varchar(255),
        lastrun timestamp,
        logfile text,
        samenvatting text,
        status varchar(255),
        primary key (id)
    );

    create table automatisch_proces_config (
        proces_id int8 not null,
        value text,
        config_key varchar(255),
        primary key (proces_id, config_key)
    );

    create table bericht (
        id  bigserial not null,
        br_orgineel_xml text,
        br_xml text,
        datum timestamp,
        db_xml text,
        job_id varchar(255),
        object_ref varchar(255),
        opmerking text,
        soort varchar(255),
        status varchar(255),
        status_datum timestamp,
        volgordenummer int4,
        xsl_version varchar(255),
        laadprocesid int8,
        primary key (id)
    );

    create table gebruiker_ (
        gebruikersnaam varchar(255) not null,
        wachtwoord varchar(255),
        primary key (gebruikersnaam)
    );

    create table gebruiker_groepen (
        gebruikersnaam varchar(255) not null,
        groep_ varchar(255) not null,
        primary key (gebruikersnaam, groep_)
    );

    create table groep_ (
        naam varchar(255) not null,
        beschrijving text,
        primary key (naam)
    );

    create table laadproces (
        id  bigserial not null,
        afgifteid varchar(255),
        afgiftereferentie varchar(255),
        artikelnummer varchar(255),
        beschikbaar_tot timestamp,
        bestand_datum timestamp,
        bestand_naam varchar(255),
        bestandsreferentie varchar(255),
        contact_email varchar(255),
        contractafgiftenummer numeric(19, 0),
        contractnummer varchar(255),
        gebied varchar(255),
        klantafgiftenummer numeric(19, 0),
        opmerking text,
        soort varchar(255),
        status varchar(255),
        status_datum timestamp,
        automatisch_proces int8,
        bestand_naam_hersteld character varying(255),
        primary key (id)
    );

    alter table automatisch_proces_config 
        add constraint FK39F3573E561B9F9B 
        foreign key (proces_id) 
        references automatisch_proces;

    alter table bericht 
        add constraint bericht_laadprocesid_fkey 
        foreign key (laadprocesid) 
        references laadproces;

    alter table gebruiker_groepen 
        add constraint FKD875A48FD741C965 
        foreign key (groep_) 
        references groep_;

    alter table gebruiker_groepen 
        add constraint FKD875A48F49E041F8 
        foreign key (gebruikersnaam) 
        references gebruiker_;

    alter table laadproces 
        add constraint FK8C420DCE3DA16A8 
        foreign key (automatisch_proces) 
        references automatisch_proces;

    CREATE TABLE job(
        jid bigserial NOT NULL,
        id BIGINT,
        br_xml TEXT,
        datum TIMESTAMP(6) WITHOUT TIME ZONE,
        object_ref CHARACTER VARYING(255),
        soort CHARACTER VARYING(255),
        volgordenummer INTEGER,
        PRIMARY KEY (jid)
    );

    CREATE TABLE nhr_laadproces (
        datum TIMESTAMP(6) WITHOUT TIME ZONE,
        laatst_geprobeerd TIMESTAMP(6) WITHOUT TIME ZONE,
        volgend_proberen TIMESTAMP(6) WITHOUT TIME ZONE,
        probeer_aantal INTEGER,
        kvk_nummer text not null,
        exception text,
        PRIMARY KEY (kvk_nummer)
    );

create index idx_bericht_job_id on bericht(job_id);
create index idx_bericht_object_ref on bericht(object_ref);
create index idx_bericht_laadprocesid on bericht(laadprocesid);
create index idx_bericht_soort on bericht (soort);
create index idx_bericht_status on bericht (status);
create unique index idx_bericht_refiddatumnr on bericht(object_ref,datum,volgordenummer);
create index idx_laadproces_soort on laadproces(soort);
create index idx_laadproces_contractnummer on laadproces(contractnummer);
create index idx_laadproces_contractafgiftenummer on laadproces(contractafgiftenummer);
create index idx_laadproces_klantafgiftenummer on laadproces(klantafgiftenummer);
create index idx_nhr_laadproces_volgend_proberen on nhr_laadproces(volgend_proberen);

CREATE TABLE brmo_metadata (
        naam CHARACTER VARYING(255) NOT NULL,
        waarde CHARACTER VARYING(255),
        CONSTRAINT brmo_metadata_pk PRIMARY KEY (naam)
);
COMMENT ON TABLE brmo_metadata IS 'BRMO metadata en versie gegevens';

-- brmo versienummer
INSERT INTO brmo_metadata (naam, waarde) VALUES ('brmoversie','${project.version}');

INSERT INTO groep_ VALUES ('Admin', 'Groep met toegang tot BRMO service');
INSERT INTO gebruiker_ VALUES ('brmo', '0109136bbd27819aec7b62c4711ddbea$100000$229427c57aaf1120ed38e4dd546248a669d27777ec2ef88c2b3d0854a17c75c9');
INSERT INTO gebruiker_groepen VALUES ('brmo', 'Admin');
