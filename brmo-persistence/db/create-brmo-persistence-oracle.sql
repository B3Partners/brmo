
    create table automatisch_proces (
        dtype varchar2(255 char) not null,
        id number(19,0) not null,
        cron_expressie varchar2(255 char),
        lastrun timestamp,
        logfile clob,
        samenvatting clob,
        status varchar2(255 char),
        primary key (id)
    );

    create table automatisch_proces_config (
        proces_id number(19,0) not null,
        value clob,
        config_key varchar2(255 char),
        primary key (proces_id, config_key)
    );

    create table bericht (
        id number(19,0) not null,
        br_orgineel_xml clob,
        br_xml clob,
        datum timestamp,
        db_xml clob,
        job_id varchar2(255 char),
        object_ref varchar2(255 char),
        opmerking clob,
        soort varchar2(255 char),
        status varchar2(255 char),
        status_datum timestamp,
        volgordenummer number(10,0),
        xsl_version varchar2(255 char),
        laadprocesid number(19,0),
        primary key (id)
    );

    create table gebruiker_ (
        gebruikersnaam varchar2(255 char) not null,
        wachtwoord varchar2(255 char),
        primary key (gebruikersnaam)
    );

    create table gebruiker_groepen (
        gebruikersnaam varchar2(255 char) not null,
        groep_ varchar2(255 char) not null,
        primary key (gebruikersnaam, groep_)
    );

    create table groep_ (
        naam varchar2(255 char) not null,
        beschrijving clob,
        primary key (naam)
    );

    create table laadproces (
        id number(19,0) not null,
        afgifteid varchar2(255 char),
        afgiftereferentie varchar2(255 char),
        artikelnummer varchar2(255 char),
        beschikbaar_tot timestamp,
        bestand_datum timestamp,
        bestand_naam varchar2(255 char),
        bestandsreferentie varchar2(255 char),
        contact_email varchar2(255 char),
        contractafgiftenummer number(19, 0),
        contractnummer varchar2(255 char),
        gebied varchar2(255 char),
        klantafgiftenummer number(19, 0),
        opmerking clob,
        soort varchar2(255 char),
        status varchar2(255 char),
        status_datum timestamp,
        automatisch_proces number(19,0),
        bestand_naam_hersteld varchar2(255 char),
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

    create sequence automatischproces_id_seq;

    create sequence bericht_id_seq;

    create sequence laadproces_id_seq;

    CREATE TABLE  job  (
        jid number(19,0) NOT NULL,
        id number(19,0),
        br_xml clob,
        datum timestamp,
        object_ref varchar2(255 char),
        soort varchar2(255 char),
        volgordenummer number(10,0),
        primary key (jid)
    );

    create sequence JOB_JID_SEQ;

    create table nhr_laadproces (
        datum timestamp,
        laatst_geprobeerd timestamp,
        volgend_proberen timestamp,
        probeer_aantal number(19, 0),
        kvk_nummer varchar2(255 char) not null,
        exception clob,
        primary key (kvk_nummer)
    );



-- create triggers om (conditioneel) een id uit de sequence te halen
-- fix voor issue #86
CREATE OR REPLACE TRIGGER LAADPROCES_INSERT_TRIGGER
        BEFORE INSERT ON LAADPROCES
        FOR EACH ROW
BEGIN
    IF :new.ID IS NULL THEN
                SELECT LAADPROCES_ID_SEQ.nextval INTO :new.ID FROM DUAL;
    END IF;
END;
/


CREATE OR REPLACE TRIGGER BERICHT_INSERT_TRIGGER
        BEFORE INSERT ON BERICHT
        FOR EACH ROW
BEGIN
    IF :new.ID IS NULL THEN
                SELECT BERICHT_ID_SEQ.nextval INTO :new.ID FROM DUAL;
    END IF;
END;
/


CREATE OR REPLACE TRIGGER JOB_INSERT_TRIGGER
        BEFORE INSERT ON JOB
        FOR EACH ROW
BEGIN
    IF :new.JID IS NULL THEN
                SELECT JOB_JID_SEQ.nextval INTO :new.JID FROM DUAL;
    END IF;
END;
/


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


CREATE TABLE BRMO_METADATA (
        NAAM VARCHAR2(255 CHAR) NOT NULL,
        WAARDE VARCHAR2(255 CHAR),
        PRIMARY KEY (NAAM)
);
COMMENT ON TABLE BRMO_METADATA IS 'BRMO metadata en versie gegevens';

-- brmo versienummer
INSERT INTO brmo_metadata (naam, waarde) VALUES ('brmoversie','${project.version}');

INSERT INTO groep_ VALUES ('Admin', 'Groep met toegang tot BRMO service');
INSERT INTO gebruiker_ VALUES ('brmo', '0109136bbd27819aec7b62c4711ddbea$100000$229427c57aaf1120ed38e4dd546248a669d27777ec2ef88c2b3d0854a17c75c9');
INSERT INTO gebruiker_groepen VALUES ('brmo', 'Admin');
