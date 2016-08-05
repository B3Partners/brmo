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
