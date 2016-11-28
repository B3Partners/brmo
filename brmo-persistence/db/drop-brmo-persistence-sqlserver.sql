
    alter table automatisch_proces_config 
        drop constraint FK39F3573E561B9F9B;

    alter table bericht 
        drop constraint bericht_laadprocesid_fkey;

    alter table gebruiker_groepen 
        drop constraint FKD875A48F49E041F8;

    alter table gebruiker_groepen 
        drop constraint FKD875A48FD741C965;

    alter table laadproces 
        drop constraint FK8C420DCE3DA16A8;

    drop table automatisch_proces;

    drop table automatisch_proces_config;

    drop table bericht;

    drop table job;

    drop table gebruiker_;

    drop table gebruiker_groepen;

    drop table groep_;

    drop table laadproces;

    drop table brmo_metadata;
