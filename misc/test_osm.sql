select ST_Transform(ST_GeomFromText('POLYGON((2.398250920228302 48.84595995531564, 2.398278788054809 48.845923000438354, 2.398244390622222 48.84591201136309, 2.398161431620923 48.845885508035884, 2.398086502653111 48.84598726181667, 2.398184905895766 48.8460186431167, 2.398201090543588 48.846023804457246, 2.39820244529123 48.84602423647196, 2.398250920228302 48.84595995531564))', 4326), 900913);

select osm_id,name,building,ST_Area(way),ST_AsEWKT(way) from planet_osm_polygon where osm_id = -1746495;
select osm_id,name,building,ST_Area(way),ST_AsEWKT(way) from planet_osm_polygon where osm_id = -1197720;

select osm_id,name,building,ST_AsEWKT(way) from planet_osm_polygon where osm_id = -1746495;

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

-- Quartier de Picpus
select osm_id,name,building,ST_AsEWKT(ST_Transform(way, 4326)) from planet_osm_polygon where osm_id = -2171720;
-- Quartier de Bercy
select osm_id,name,building,ST_AsEWKT(ST_Transform(way, 4326)) from planet_osm_polygon where osm_id = -2171718;
-- Quartier du Bel-Air
select osm_id,name,building,ST_AsEWKT(ST_Transform(way, 4326)) from planet_osm_polygon where osm_id = -2171716;
-- Quartier des Quinze-Vingts
select osm_id,name,building,ST_AsEWKT(ST_Transform(way, 4326)) from planet_osm_polygon where osm_id = -2192616;

-- multipolygones CarpeDiem
select osm_id,name,building,ST_Area(way),ST_AsEWKT(way) from planet_osm_polygon where osm_id = -4296651996;
select osm_id,name,building,ST_Area(way),ST_AsEWKT(way) from planet_osm_polygon where osm_id = 4296767828;
select * from planet_osm_rels where id=4296651996;

-- multipolygones Napoleon
select osm_id,name,building,ST_Area(way),ST_AsEWKT(way) from planet_osm_polygon where osm_id = -4296652071;
select osm_id,name,building,ST_Area(way),ST_AsEWKT(way) from planet_osm_polygon where osm_id = 4296767866;
select * from planet_osm_rels where id=4296652071;

select osm_id,name,building,st_asewkt(way) from planet_osm_polygon where building <> '' and  ST_Contains(way, ST_Transform(ST_GeomFromText('POINT(2.38475269926 48.8458926819)', 4326), 900913));
select osm_id,name,building,st_asewkt(way) from planet_osm_polygon where building <> '' and  ST_Contains(way, ST_Transform(ST_GeomFromText('POINT(2.38475269926 48.8458926819)', 4326), 4326));
select osm_id,name,building,st_asewkt(way) from planet_osm_polygon where building <> '' and  ST_Contains(way, ST_Transform(ST_GeomFromText('POINT(2.416653424534161 48.84305412530941)', 4326), 900913));

select(ST_Area(ST_GeomFromText('POLYGON((0 0, 2 0, 2 3, 0 3, 0 0))', 900913)));

select(ST_Area(ST_GeomFromText('POLYGON((
265489.77 6248795.93,
265514.62 6248796.47,
265515.49 6248757.45,
265490.65 6248757.21,
265490.64 6248759.15,
265490.39 6248770.86,
265489.77 6248795.93))', 900913)));

select ST_Area(ST_Transform(way, 32633)) from planet_osm_polygon where osm_id = -1746495 ;
select ST_Area(ST_Transform(way, 32633)) from planet_osm_polygon where osm_id = -1197720 ;

select osm_id from planet_osm_polygon where building <> '' and  ST_Intersects(way, ST_Transform(ST_GeomFromText('', 4326), 4326);

select osm_id from planet_osm_polygon where building <> '' and  ST_Intersects(way, ST_GeomFromText('POLYGON((2.398250920228302 48.84595995531564, 2.398278788054809 48.845923000438354, 2.398244390622222 48.84591201136309, 2.398161431620923 48.845885508035884, 2.398086502653111 48.84598726181667, 2.398184905895766 48.8460186431167, 2.398201090543588 48.846023804457246, 2.39820244529123 48.84602423647196, 2.398250920228302 48.84595995531564))', 4326));
select osm_id from planet_osm_polygon where building <> '' and  ST_Intersects(way, ST_GeomFromText('POLYGON((48.84595995531564 2.398250920228302, 48.845923000438354 2.398278788054809, 48.84591201136309 2.398244390622222, 48.845885508035884 2.398161431620923, 48.84598726181667 2.398086502653111, 48.8460186431167 2.398184905895766, 48.846023804457246 2.398201090543588, 48.84602423647196 2.39820244529123, 48.84595995531564 2.398250920228302))', 4326));
