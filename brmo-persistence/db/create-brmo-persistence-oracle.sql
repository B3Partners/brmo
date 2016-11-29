
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
        bestand_datum timestamp,
        bestand_naam varchar2(255 char),
        contact_email varchar2(255 char),
        gebied varchar2(255 char),
        opmerking clob,
        soort varchar2(255 char),
        status varchar2(255 char),
        status_datum timestamp,
        automatisch_proces number(19,0),
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
