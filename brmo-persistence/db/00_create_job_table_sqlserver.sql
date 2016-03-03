--DROP TABLE job;
CREATE TABLE
    job
    (
        jid numeric(19,0) identity not null,
        id numeric(19,0) null,
        br_xml text null,
        datum datetime null,
        object_ref varchar(255) null,
        soort varchar(255) null,
        volgordenummer int null,
        primary key (jid)
    );
