-- liquibase formatted sql

-- changeset restservice:1 runAlways:true
truncate table mytable;

insert into mytable (a,b,c,d) 
values ('1','2','3','4');
commit;

--rollback DELETE FROM mytable;