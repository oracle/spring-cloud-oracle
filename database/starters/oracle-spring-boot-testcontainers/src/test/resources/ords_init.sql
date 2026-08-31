-- Copyright (c) 2026, Oracle and/or its affiliates.
-- Licensed under the Universal Permissive License v 1.0 as shown at https://oss.oracle.com/licenses/upl/

WHENEVER SQLERROR EXIT SQL.SQLCODE;

ALTER SESSION SET CONTAINER = FREEPDB1;

CREATE USER ordsuser IDENTIFIED BY "Ords /@ Password1" QUOTA UNLIMITED ON users;
GRANT connect, pdb_dba TO ordsuser;

CREATE USER mongouser IDENTIFIED BY mongouserpwd QUOTA UNLIMITED ON users;
GRANT create session, create table, soda_app TO mongouser;

EXIT;
