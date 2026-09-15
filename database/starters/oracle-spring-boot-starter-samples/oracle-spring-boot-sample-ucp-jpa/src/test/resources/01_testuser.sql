-- Copyright (c) 2026, Oracle and/or its affiliates.
-- Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/.

whenever sqlerror exit sql.sqlcode
alter session set container=FREEPDB1;
create tablespace USERS datafile '/opt/oracle/oradata/FREE/FREEPDB1/users01.dbf' size 100M autoextend on next 100M maxsize unlimited;
create user TESTUSER identified by "testpwd" default tablespace USERS quota unlimited on USERS;
grant create session to TESTUSER;
grant DB_DEVELOPER_ROLE to TESTUSER;
prompt TESTUSER APP USER IS READY
exit;
