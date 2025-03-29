create table if not exists dog
(
    id          serial primary key,
    name        text,
    description text,
    owner       text null
) ;