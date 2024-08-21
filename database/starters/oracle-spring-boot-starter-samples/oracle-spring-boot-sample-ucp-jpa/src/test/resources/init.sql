create table if not exists student (
    id         varchar2(36) primary key,
    first_name varchar2(50) not null,
    last_name  varchar2(50) not null,
    email      varchar2(100),
    major      varchar2(20) not null,
    credits    number(10),
    gpa        number(10)
);
