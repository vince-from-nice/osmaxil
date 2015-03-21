select count(*) from planet_osm_line;
select count(*) from planet_osm_point;
select count(*) from planet_osm_polygon;

select count(distinct osm_id) from planet_osm_polygon;
select count(*) from planet_osm_polygon where building <> '';
select count(*) from planet_osm_polygon where building = 'yes';
select count(distinct osm_id) from planet_osm_polygon where osm_id < 0;

select * from planet_osm_polygon;
select * from planet_osm_polygon where osm_id < 0 order by osm_id;
select osm_id, name, building, ST_AsEWKT(ST_Transform(way, 4326)) from planet_osm_polygon;
select osm_id, name, building, ST_AsEWKT(ST_Transform(way, 4326)) from planet_osm_polygon where building <> '' LIMIT 100;
select osm_id, name, building, ST_AsEWKT(way) from planet_osm_polygon where building <> '' and name <> '' order by name LIMIT 100 ;

-- multipolygones CarpeDiem
select osm_id,name,building,ST_Area(way),ST_AsEWKT(way) from planet_osm_polygon where osm_id = -4296651996;
select osm_id,name,building,ST_Area(way),ST_AsEWKT(way) from planet_osm_polygon where osm_id = 4296767828;
select * from planet_osm_rels where id=4296651996;

-- multipolygones Napoleon
select osm_id,name,building,ST_Area(way),ST_AsEWKT(way) from planet_osm_polygon where osm_id = -4296652071;
select osm_id,name,building,ST_Area(way),ST_AsEWKT(way) from planet_osm_polygon where osm_id = 4296767866;
select * from planet_osm_rels where id=4296652071;

select osm_id,name,building,st_asewkt(way) from planet_osm_polygon 
where building <> '' and  ST_Contains(way, ST_Transform(ST_GeomFromText('POINT(2.38475269926 48.8458926819)', 4326), 900913));
select osm_id,name,building,st_asewkt(way) from planet_osm_polygon 
where building <> '' and  ST_Contains(way, ST_Transform(ST_GeomFromText('POINT(2.38475269926 48.8458926819)', 4326), 4326));

select(ST_Area(ST_GeomFromText('POLYGON((0 0, 2 0, 2 3, 0 3, 0 0))', 900913)));

select(ST_Area(ST_GeomFromText('POLYGON((
265489.77 6248795.93,
265514.62 6248796.47,
265515.49 6248757.45,
265490.65 6248757.21,
265490.64 6248759.15,
265490.39 6248770.86,
265489.77 6248795.93))', 900913)));

select(ST_Area(ST_GeomFromText('POLYGON((
265489.77 6248795.93,
265514.62 6248796.47,
265515.49 6248757.45,
265490.65 6248757.21,
265490.64 6248759.15,
265490.39 6248770.86,
265489.77 6248795.93))', 900913)));

