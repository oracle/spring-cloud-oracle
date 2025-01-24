-- liquibase formatted sql

--changeset springcliapp:1
--preconditions onfail:MARK_RAN onerror:MARK_RAN
--precondition-sql-check expectedresult:0 select count(*) from mytable where 1=2
drop table mytable;

--changeset springcliapp:2
create table mytable (
    a varchar2 (20),
    b varchar2 (40),
    c varchar2 (40),
    d varchar2(40)
) logging;

alter table mytable add constraint mytable_pk primary key (a) using index logging;

--rollback drop table mytable;