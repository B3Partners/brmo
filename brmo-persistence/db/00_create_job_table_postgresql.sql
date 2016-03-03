--DROP TABLE job;
CREATE TABLE
    job
    (
        jid bigserial NOT NULL,
        id BIGINT,
        br_xml TEXT,
        datum TIMESTAMP(6) WITHOUT TIME ZONE,
        object_ref CHARACTER VARYING(255),
        soort CHARACTER VARYING(255),
        volgordenummer INTEGER,
        PRIMARY KEY (jid)
    );
/*


--DROP TABLE automatisch_proces;
CREATE TABLE
    automatisch_proces
    (
        dtype CHARACTER VARYING(255) NOT NULL,
        id bigserial NOT NULL,
        cron_expressie CHARACTER VARYING(255),
        lastrun TIMESTAMP(6) WITHOUT TIME ZONE,
        logfile TEXT,
        samenvatting TEXT,
        status CHARACTER VARYING(255),
        PRIMARY KEY (id)
    );
--DROP TABLE automatisch_proces_config;
CREATE TABLE
    automatisch_proces_config
    (
        proces_id BIGINT NOT NULL,
        value TEXT,
        config_key CHARACTER VARYING(255) NOT NULL,
        PRIMARY KEY (proces_id, config_key)
    );
--DROP TABLE bericht;
CREATE TABLE
    bericht
    (
        id bigserial NOT NULL,
        br_orgineel_xml TEXT,
        br_xml TEXT,
        datum TIMESTAMP(6) WITHOUT TIME ZONE,
        db_xml TEXT,
        job_id CHARACTER VARYING(255),
        object_ref CHARACTER VARYING(255),
        opmerking TEXT,
        soort CHARACTER VARYING(255),
        status CHARACTER VARYING(255),
        status_datum TIMESTAMP(6) WITHOUT TIME ZONE,
        volgordenummer INTEGER,
        xsl_version CHARACTER VARYING(255),
        laadprocesid BIGINT,
        PRIMARY KEY (id)
    );
--DROP TABLE gebruiker_;
CREATE TABLE
    gebruiker_
    (
        gebruikersnaam CHARACTER VARYING(255) NOT NULL,
        wachtwoord CHARACTER VARYING(255),
        PRIMARY KEY (gebruikersnaam)
    );
--DROP TABLE gebruiker_groepen;
CREATE TABLE
    gebruiker_groepen
    (
        gebruikersnaam CHARACTER VARYING(255) NOT NULL,
        groep_ CHARACTER VARYING(255) NOT NULL,
        PRIMARY KEY (gebruikersnaam, groep_)
    );
--DROP TABLE groep_;
CREATE TABLE
    groep_
    (
        naam CHARACTER VARYING(255) NOT NULL,
        beschrijving TEXT,
        PRIMARY KEY (naam)
    );
--DROP TABLE laadproces;
CREATE TABLE
    laadproces
    (
        id bigserial NOT NULL,
        bestand_datum TIMESTAMP(6) WITHOUT TIME ZONE,
        bestand_naam CHARACTER VARYING(255),
        contact_email CHARACTER VARYING(255),
        gebied CHARACTER VARYING(255),
        opmerking TEXT,
        soort CHARACTER VARYING(255),
        status CHARACTER VARYING(255),
        status_datum TIMESTAMP(6) WITHOUT TIME ZONE,
        automatisch_proces BIGINT,
        PRIMARY KEY (id)
    );
ALTER TABLE
    automatisch_proces_config ADD CONSTRAINT auto_proces_id_fkey FOREIGN KEY (proces_id) REFERENCES
    automatisch_proces (id);
ALTER TABLE
    bericht ADD CONSTRAINT bericht_laadprocesid_fkey FOREIGN KEY (laadprocesid) REFERENCES
    laadproces (id);
ALTER TABLE
    gebruiker_groepen ADD CONSTRAINT gebruiker_groep_fkey FOREIGN KEY (groep_) REFERENCES groep_
    (naam);
ALTER TABLE
    gebruiker_groepen ADD CONSTRAINT gebruikers_naam_fkey FOREIGN KEY (gebruikersnaam) REFERENCES
    gebruiker_ (gebruikersnaam);
ALTER TABLE
    laadproces ADD CONSTRAINT laadproces_auto_fkey FOREIGN KEY (automatisch_proces) REFERENCES
    automatisch_proces (id);
	
*/
