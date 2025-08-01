-- aanmaken van brmo-schema's in oracle
ALTER SESSION SET "_ORACLE_SCRIPT"=true;

-- STAGING
CREATE USER "JENKINS_STAGING" IDENTIFIED BY "jenkins_staging" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP";

-- QUOTAS
ALTER USER "JENKINS_STAGING" QUOTA UNLIMITED ON USERS;

-- ROLES
GRANT "CONNECT" TO "JENKINS_STAGING";
GRANT "RESOURCE" TO "JENKINS_STAGING";
ALTER USER "JENKINS_STAGING" DEFAULT ROLE "CONNECT","RESOURCE";

-- SYSTEM PRIVILEGES
GRANT CREATE TRIGGER TO "JENKINS_STAGING";
GRANT ALTER SESSION TO "JENKINS_STAGING";
GRANT CREATE MATERIALIZED VIEW TO "JENKINS_STAGING";
GRANT CREATE OPERATOR TO "JENKINS_STAGING";
GRANT CREATE VIEW TO "JENKINS_STAGING";
GRANT CREATE SESSION TO "JENKINS_STAGING";
GRANT CREATE TABLE TO "JENKINS_STAGING";
GRANT CREATE SEQUENCE TO "JENKINS_STAGING";
GRANT UNLIMITED TABLESPACE TO "JENKINS_STAGING";
GRANT CREATE PROCEDURE TO "JENKINS_STAGING";


-- RSGB
CREATE USER "JENKINS_RSGB" IDENTIFIED BY "jenkins_rsgb" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP";

-- QUOTAS
ALTER USER "JENKINS_RSGB" QUOTA UNLIMITED ON USERS;

-- ROLES
GRANT "CONNECT" TO "JENKINS_RSGB";
GRANT "RESOURCE" TO "JENKINS_RSGB";
ALTER USER "JENKINS_RSGB" DEFAULT ROLE "CONNECT","RESOURCE";

-- SYSTEM PRIVILEGES
GRANT CREATE TRIGGER TO "JENKINS_RSGB";
GRANT ALTER SESSION TO "JENKINS_RSGB";
GRANT CREATE MATERIALIZED VIEW TO "JENKINS_RSGB";
GRANT CREATE OPERATOR TO "JENKINS_RSGB";
GRANT CREATE VIEW TO "JENKINS_RSGB";
GRANT CREATE SESSION TO "JENKINS_RSGB";
GRANT CREATE TABLE TO "JENKINS_RSGB";
GRANT CREATE SEQUENCE TO "JENKINS_RSGB";
GRANT UNLIMITED TABLESPACE TO "JENKINS_RSGB";
GRANT CREATE PROCEDURE TO "JENKINS_RSGB";


-- RSGB BGT
CREATE USER "JENKINS_RSGBBGT" IDENTIFIED BY "jenkins_rsgbbgt" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP";

-- QUOTAS
ALTER USER "JENKINS_RSGBBGT" QUOTA UNLIMITED ON USERS;

-- ROLES
GRANT "CONNECT" TO "JENKINS_RSGBBGT";
GRANT "RESOURCE" TO "JENKINS_RSGBBGT";
ALTER USER "JENKINS_RSGBBGT" DEFAULT ROLE "CONNECT","RESOURCE";

-- SYSTEM PRIVILEGES
GRANT CREATE TRIGGER TO "JENKINS_RSGBBGT";
GRANT ALTER SESSION TO "JENKINS_RSGBBGT";
GRANT CREATE MATERIALIZED VIEW TO "JENKINS_RSGBBGT";
GRANT CREATE OPERATOR TO "JENKINS_RSGBBGT";
GRANT CREATE VIEW TO "JENKINS_RSGBBGT";
GRANT CREATE SESSION TO "JENKINS_RSGBBGT";
GRANT CREATE TABLE TO "JENKINS_RSGBBGT";
GRANT CREATE SEQUENCE TO "JENKINS_RSGBBGT";
GRANT UNLIMITED TABLESPACE TO "JENKINS_RSGBBGT";
GRANT CREATE PROCEDURE TO "JENKINS_RSGBBGT";

CREATE USER "JENKINS_BAG" IDENTIFIED BY "jenkins_bag" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP";

-- QUOTAS
ALTER USER "JENKINS_BAG" QUOTA UNLIMITED ON USERS;

-- ROLES
GRANT "CONNECT" TO "JENKINS_BAG";
GRANT "RESOURCE" TO "JENKINS_BAG";
ALTER USER "JENKINS_BAG" DEFAULT ROLE "CONNECT","RESOURCE";

-- SYSTEM PRIVILEGES
GRANT CREATE TRIGGER TO "JENKINS_BAG";
GRANT ALTER SESSION TO "JENKINS_BAG";
GRANT CREATE MATERIALIZED VIEW TO "JENKINS_BAG";
GRANT CREATE OPERATOR TO "JENKINS_BAG";
GRANT CREATE VIEW TO "JENKINS_BAG";
GRANT CREATE SESSION TO "JENKINS_BAG";
GRANT CREATE TABLE TO "JENKINS_BAG";
GRANT CREATE SEQUENCE TO "JENKINS_BAG";
GRANT UNLIMITED TABLESPACE TO "JENKINS_BAG";
GRANT CREATE PROCEDURE TO "JENKINS_BAG";


CREATE USER "JENKINS_BRK" IDENTIFIED BY "jenkins_brk" DEFAULT TABLESPACE "USERS" TEMPORARY TABLESPACE "TEMP";

-- QUOTAS
ALTER USER "JENKINS_BRK" QUOTA UNLIMITED ON USERS;

-- ROLES
GRANT "CONNECT" TO "JENKINS_BRK";
GRANT "RESOURCE" TO "JENKINS_BRK";
ALTER USER "JENKINS_BRK" DEFAULT ROLE "CONNECT","RESOURCE";

-- SYSTEM PRIVILEGES
GRANT CREATE TRIGGER TO "JENKINS_BRK";
GRANT ALTER SESSION TO "JENKINS_BRK";
GRANT CREATE MATERIALIZED VIEW TO "JENKINS_BRK";
GRANT CREATE OPERATOR TO "JENKINS_BRK";
GRANT CREATE VIEW TO "JENKINS_BRK";
GRANT CREATE SESSION TO "JENKINS_BRK";
GRANT CREATE TABLE TO "JENKINS_BRK";
GRANT CREATE SEQUENCE TO "JENKINS_BRK";
GRANT UNLIMITED TABLESPACE TO "JENKINS_BRK";
GRANT CREATE PROCEDURE TO "JENKINS_BRK";