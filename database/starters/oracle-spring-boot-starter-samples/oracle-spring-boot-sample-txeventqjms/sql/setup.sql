-- This must be executed as SYS
create user testuser identified by Welcome12345;
grant resource, connect, unlimited tablespace to testuser;
grant aq_user_role to testuser;
grant execute on dbms_aq to testuser;
grant execute on dbms_aqadm to testuser;
grant execute ON dbms_aqin TO testuser;
grant execute ON dbms_aqjms TO testuser;
grant execute on dbms_teqk to testuser;
commit;

-- create the TEQ
begin
    dbms_aqadm.create_transactional_event_queue(
            queue_name         => 'TESTUSER.MY_TXEVENTQ',
        -- when mutiple_consumers is true, this will create a pub/sub "topic" - the default is false
            multiple_consumers => false
    );

    -- start the TEQ
    dbms_aqadm.start_queue(
            queue_name         => 'TESTUSER.MY_TXEVENTQ'
    );
end;
/
