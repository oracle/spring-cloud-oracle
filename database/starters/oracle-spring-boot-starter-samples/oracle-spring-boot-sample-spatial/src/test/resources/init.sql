create table landmarks (
    id number primary key,
    name varchar2(200) not null,
    category varchar2(100) not null,
    geometry mdsys.sdo_geometry not null
);

insert into user_sdo_geom_metadata (table_name, column_name, diminfo, srid)
values (
    'LANDMARKS',
    'GEOMETRY',
    mdsys.sdo_dim_array(
        mdsys.sdo_dim_element('LONG', -180, 180, 0.005),
        mdsys.sdo_dim_element('LAT', -90, 90, 0.005)
    ),
    4326
);

create index landmarks_spatial_idx
on landmarks (geometry)
indextype is mdsys.spatial_index_v2;

insert into landmarks (id, name, category, geometry)
values (1, 'Ferry Building', 'MARKET', sdo_util.from_geojson('{"type":"Point","coordinates":[-122.3933,37.7955]}', null, 4326));

insert into landmarks (id, name, category, geometry)
values (2, 'Union Square', 'PLAZA', sdo_util.from_geojson('{"type":"Point","coordinates":[-122.4074,37.7879]}', null, 4326));

insert into landmarks (id, name, category, geometry)
values (3, 'Golden Gate Park', 'PARK', sdo_util.from_geojson('{"type":"Polygon","coordinates":[[[-122.511,37.771],[-122.454,37.771],[-122.454,37.768],[-122.511,37.768],[-122.511,37.771]]]}', null, 4326));

insert into landmarks (id, name, category, geometry)
values (4, 'Oracle Park', 'STADIUM', sdo_util.from_geojson('{"type":"Point","coordinates":[-122.3893,37.7786]}', null, 4326));

insert into landmarks (id, name, category, geometry)
values (5, 'Salesforce Tower', 'SKYSCRAPER', sdo_util.from_geojson('{"type":"Point","coordinates":[-122.3969,37.7897]}', null, 4326));

insert into landmarks (id, name, category, geometry)
values (6, 'Transamerica Pyramid', 'SKYSCRAPER', sdo_util.from_geojson('{"type":"Point","coordinates":[-122.4039,37.7952]}', null, 4326));

insert into landmarks (id, name, category, geometry)
values (7, 'Coit Tower', 'TOWER', sdo_util.from_geojson('{"type":"Point","coordinates":[-122.4058,37.8024]}', null, 4326));
