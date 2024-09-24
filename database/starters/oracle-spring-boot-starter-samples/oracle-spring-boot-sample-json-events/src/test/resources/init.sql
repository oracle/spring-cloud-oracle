create table station (
    id           varchar2(36) default sys_guid() primary key,
    station_name varchar2(50) not null,
    latitude     number(10,8) not null,
    longitude    number(11,8) not null,
    elevation    number(16,2),
    constraint chk_latitude check (latitude between -90 and 90),
    constraint chk_longitude check (longitude between -180 and 180)
);


create table weather_sensor (
    id                varchar2(36) default sys_guid() primary key,
    station_id        varchar2(36) not null,
    relative_humidity number(5,2),
    temperature       number(5,2),
    uv_index          number(4,1),
    timestamp         timestamp default CURRENT_TIMESTAMP not null,
    constraint sensor_station_fk foreign key (station_id)
    references station(id)
);

create or replace json relational duality view
weather_sensor_dv as
weather_sensor @insert @update @delete {
    _id : id,
    relativeHumidity : relative_humidity,
    temperature,
    uvIndex : uv_index,
    timestamp,
    station: station {
        _id : id,
        stationName : station_name,
        latitude,
        longitude,
        elevation
    }
};

create or replace json relational duality view
station_dv as
station @insert @update @delete {
    _id : id,
    stationName : station_name,
    latitude,
    longitude,
    elevation
};

insert into station (id, station_name, latitude, longitude, elevation)
values ('ST001', 'Mount Hood Observatory', 45.3424092, -121.7824754, 11249);

insert into station (id, station_name, latitude, longitude, elevation)
values ('ST002', 'Astoria Research Center', 46.187580, -123.834114, 15.2);
