
    create table automatisch_proces (
        dtype varchar(255) not null,
        id numeric(19,0) identity not null,
        cron_expressie varchar(255) null,
        lastrun datetime null,
        logfile text null,
        samenvatting text null,
        status varchar(255) null,
        primary key (id)
    );

    create table automatisch_proces_config (
        proces_id numeric(19,0) not null,
        value text null,
        config_key varchar(255) not null,
        primary key (proces_id, config_key)
    );

    create table bericht (
        id numeric(19,0) identity not null,
        br_orgineel_xml text null,
        br_xml text null,
        datum datetime null,
        db_xml text null,
        job_id varchar(255) null,
        object_ref varchar(255) null,
        opmerking text null,
        soort varchar(255) null,
        status varchar(255) null,
        status_datum datetime null,
        volgordenummer int null,
        xsl_version varchar(255) null,
        laadprocesid numeric(19,0) null,
        primary key (id)
    );

    create table gebruiker_ (
        gebruikersnaam varchar(255) not null,
        wachtwoord varchar(255) null,
        primary key (gebruikersnaam)
    );

    create table gebruiker_groepen (
        gebruikersnaam varchar(255) not null,
        groep_ varchar(255) not null,
        primary key (gebruikersnaam, groep_)
    );

    create table groep_ (
        naam varchar(255) not null,
        beschrijving text null,
        primary key (naam)
    );

    create table laadproces (
        id numeric(19,0) identity not null,
        afgifteid varchar(255) null,
        afgiftereferentie varchar(255) null,
        artikelnummer varchar(255) null,
        beschikbaar_tot datetime null,
        bestand_datum datetime null,
        bestand_naam varchar(255) null,
        bestandsreferentie varchar(255) null,
        contact_email varchar(255) null,
        contractafgiftenummer numeric(19, 0) null,
        contractnummer varchar(255) null,
        gebied varchar(255) null,
        klantafgiftenummer numeric(19, 0) null,
        opmerking text null,
        soort varchar(255) null,
        status varchar(255) null,
        status_datum datetime null,
        automatisch_proces numeric(19,0) null,
        bestand_naam_hersteld varchar(255) null,
        primary key (id)
    );

GO

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

    CREATE TABLE job (
        jid numeric(19,0) identity not null,
        id numeric(19,0) null,
        br_xml text null,
        datum datetime null,
        object_ref varchar(255) null,
        soort varchar(255) null,
        volgordenummer int null,
        primary key (jid)
    );

GO

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

GO


CREATE TABLE brmo_metadata (
        naam VARCHAR(255) NOT NULL,
        waarde VARCHAR(255),
        PRIMARY KEY (naam)
);

GO

EXEC sys.sp_addextendedproperty @name=N'comment', @value=N'BRMO metadata en versie gegevens' , @level0type=N'SCHEMA',@level0name=N'dbo', @level1type=N'TABLE',@level1name=N'brmo_metadata'

GO

-- brmo versienummer
INSERT INTO brmo_metadata (naam, waarde) VALUES ('brmoversie','${project.version}');

INSERT INTO groep_ VALUES ('Admin', 'Groep met toegang tot BRMO service');
INSERT INTO gebruiker_ VALUES ('brmo', '6310227872580fec7d1262ab7ab3b4b3902a9f61');
INSERT INTO gebruiker_groepen VALUES ('brmo', 'Admin');
