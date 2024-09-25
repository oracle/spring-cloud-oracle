alter session set container=freepdb1;

-- You may wish to modify the unlimited tablespace grant as appropriate.
grant resource, connect, unlimited tablespace to TESTUSER;
grant aq_user_role to TESTUSER;
grant execute on dbms_aq to  TESTUSER;
grant execute on dbms_aqadm to TESTUSER;
grant select on gv_$session to TESTUSER;
grant select on v_$session to TESTUSER;
grant select on gv_$instance to TESTUSER;
grant select on gv_$listener_network to TESTUSER;
grant select on SYS.DBA_RSRC_PLAN_DIRECTIVES to TESTUSER;
grant select on gv_$pdbs to TESTUSER;
grant select on user_queue_partition_assignment_table to TESTUSER;
exec dbms_aqadm.GRANT_PRIV_FOR_RM_PLAN('TESTUSER');
commit;
