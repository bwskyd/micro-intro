create table if not exists songs_t (
    id serial primary key,
    name_c character varying(100) not null,
    artist_c character varying(100) not null,
    album_c character varying(100) not null,
    duration_c character varying(5) not null,
    year_c character varying(4) default null
);