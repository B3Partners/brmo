WHENEVER OSERROR EXIT FAILURE;
WHENEVER SQLERROR EXIT SQL.SQLCODE;
SET DEFINE OFF;

-- ALTER system SET nls_length_semantics=CHAR scope=both;
ALTER session SET nls_length_semantics=CHAR;