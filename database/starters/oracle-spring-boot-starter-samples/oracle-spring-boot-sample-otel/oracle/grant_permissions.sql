-- Set as appropriate for your database.
alter session set container = freepdb1;

create user testuser identified by testpwd quota unlimited on users;
grant connect, resource to testuser;


create table testuser.ice_cream_flavors (
    id     number generated always as identity primary key,
    flavor varchar2(255)
);
