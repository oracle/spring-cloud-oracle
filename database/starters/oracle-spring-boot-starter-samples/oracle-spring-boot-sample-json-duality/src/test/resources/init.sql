create table students (
    id         varchar2(36) default sys_guid() primary key,
    first_name varchar2(50) not null,
    last_name  varchar2(50) not null,
    email      varchar2(100),
    major      varchar2(50) not null,
    credits    number(10),
    gpa        binary_double
);

create table lecture_halls (
    id         varchar2(36) default sys_guid() primary key,
    name       varchar2(50) not null
);

create table courses (
    id              varchar2(36) default sys_guid() primary key,
    lecture_hall_id varchar2(36) not null,
    name            varchar2(50) not null,
    description     varchar2(250) not null,
    credits         number check (credits between 0 and 10),
    constraint lecture_hall_fk foreign key (lecture_hall_id)
    references lecture_halls(id)
);

create table enrollments (
    id         varchar2(36) default sys_guid() primary key,
    student_id varchar2(36) not null,
    course_id  varchar2(36) not null,
    constraint student_fk foreign key (student_id)
    references students(id),
    constraint course_fk foreign key (course_id)
    references courses(id)
);

insert into students (first_name, last_name, email, major, credits, gpa)
values ('Alice', 'Smith', 'alice.smith@example.edu', 'Computer Science', 77, 3.86);
insert into lecture_halls (name)
values ('Hoffman Hall');
insert into courses (lecture_hall_id, name, description, credits)
values (
           (select id from lecture_halls where name = 'Hoffman Hall'),
           'Introduction to Computer Science',
           'A foundational course covering basic principles of computer science and programming.',
           4
       );
insert into courses (lecture_hall_id, name, description, credits)
values (
           (select id from lecture_halls where name = 'Hoffman Hall'),
           'Data Structures and Algorithms',
           'An in-depth study of various data structures and fundamental algorithms in computer science.',
           5
       );


create or replace json relational duality view
students_dv as
students @insert @update @delete {
    _id : id,
    first_name,
    last_name,
    email,
    major,
    gpa,
    credits,
    enrollments : enrollments @insert @update @delete
    {
        _id : id,
        course : courses @insert @update {
            _id : id,
            name,
            description,
            credits,
            lecture_hall: lecture_halls @insert @update {
                _id: id,
                name
            }
        }
    }
};

create or replace json relational duality view
courses_dv as
courses @insert @update @delete {
    _id : id,
    name,
    description,
    credits,
    lecture_hall: lecture_halls @insert @update {
        _id: id,
        name
    }
}
/

