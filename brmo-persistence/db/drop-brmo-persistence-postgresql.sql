
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

    drop table automatisch_proces cascade;

    drop table automatisch_proces_config cascade;

    drop table bericht cascade;

    drop table job cascade;

    drop table gebruiker_ cascade;

    drop table gebruiker_groepen cascade;

    drop table groep_ cascade;

    drop table laadproces cascade;

    drop table nhr_laadproces cascade;

    drop table brmo_metadata cascade;
