create table sensor_data (
    id                varchar2(36) default sys_guid() primary key,
    station_id        varchar2(36) not null,
    relative_humidity number(5,2),
    temperature       number(5,2),
    uv_index          number(4,1),
    timestamp         timestamp default CURRENT_TIMESTAMP not null,
    constraint sensor_station_fk foreign key (station_id)
    references weather_station(id)
);

create table weather_station (
    id                varchar2(36) default sys_guid() primary key,
    station_name      varchar2(50) not null,
    latitude NUMBER(10,8) NOT NULL,
    longitude NUMBER(11,8) NOT NULL,
    elevation NUMBER(6,2),
    constraint chk_latitude check (latitude between -90 and 90),
    constraint chk_longitude check (longitude between -180 and 180)
);

create or replace json relational duality view
sensor_data_dv as
sensor_data @insert @update @delete {
    _id : id,
    stationId : station_id,
    relativeHumidity : reliative_humidity,
    temperature,
    uvIndex : uv_index,
    timestamp,
    station: weather_station {
        _id : id,
        stationName : station_name,
        latitude,
        longitude,
        elevation
    }
}
/

create or replace json relational duality view
weather_station_dv as
weather_station @insert @update @delete {
    _id : id,
    stationName : station_name,
    latitude,
    longitude,
    elevation
}
/


{
    "_id": "ST001",
    "stationName": "Mount Hood Observatory",
    "latitude": 45.3424092,
    "longitude": -121.7824754,
    "elevation": 11249
  },
  {
    "_id": "ST002",
    "stationName": "Astoria Coastal Research Center",
    "latitude": 34.0522,
    "longitude": -118.2437,
    "elevation": 15.2
  },
  {
    "_id": "ST003",
    "stationName": "Oregon Desert Monitoring Station",
    "latitude": 36.1699,
    "longitude": -115.1398,
    "elevation": 620.3
  },

