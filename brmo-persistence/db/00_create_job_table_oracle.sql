--DROP TABLE job;
    create sequence JOB_JID_SEQ;
    
CREATE TABLE
    job
    (
        jid number(19,0) NOT NULL,
	    id number(19,0),
        br_xml clob,
        datum timestamp,
        object_ref varchar2(255 char),
        soort varchar2(255 char),
        volgordenummer number(10,0),
        primary key (jid)
    );

