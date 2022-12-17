alter system set processes=500 scope=spfile;
alter system set nls_length_semantics='CHAR' scope=BOTH;
shutdown immediate;
