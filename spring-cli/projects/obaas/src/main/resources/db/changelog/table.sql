-- liquibase formatted sql

--changeset restservice:1
--preconditions onfail:restservice_ran onerror:restservice_ran
--precondition-sql-check expectedresult:0 select count(*) from mytable where 1=2
drop table mytable;

--changeset customer:2
create table mytable (
    a varchar2 (20),
    b varchar2 (40),
    c varchar2 (40),
    d varchar2(40)
) logging;

alter table mytable add constraint mytable_pk primary key (a) using index logging;

--rollback drop table mytable;