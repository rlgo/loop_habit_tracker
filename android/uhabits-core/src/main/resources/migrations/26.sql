DROP TABLE HABITS;

create table Habits (
    id integer primary key autoincrement,
    archived integer,
    color integer,
    description text,
    freq_den integer,
    freq_num integer,
    highlight integer,
    name text,
    position integer,
    reminder_hour integer,
    reminder_min integer,
    reminder_days integer not null,
    type integer not null,
    target_type integer not null default,
    target_value real not null default 0,
    unit text not null default "",
    question text
);

update habits set color=19 where color=12;
update habits set color=17 where color=11;
update habits set color=15 where color=10;
update habits set color=14 where color=9;
update habits set color=13 where color=8;
update habits set color=10 where color=7;
update habits set color=9 where color=6;
update habits set color=8 where color=5;
update habits set color=7 where color=4;
update habits set color=5 where color=3;
update habits set color=4 where color=2;
update habits set color=0 where color<0 or color>19;

update Habits set question = description;

update Habits set description = "";
alter table habits add column uuid text;
update habits set uuid = lower(hex(randomblob(16) || id));

alter table habits add column enable_google_fit integer;

alter table habits add column calorie_burned TEXT;
alter table habits add column hydration TEXT;
alter table habits add column activity_duration TEXT;
