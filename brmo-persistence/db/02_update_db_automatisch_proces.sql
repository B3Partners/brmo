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


    alter table automatisch_proces_config
        add constraint FK39F3573E561B9F9B
        foreign key (proces_id)
        references automatisch_proces;
