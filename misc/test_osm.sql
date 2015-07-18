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

-- Les Balcons de la Nation
select osm_id,name,building,ST_Area(way),ST_Area(ST_Transform(ST_Transform(way, 4326), 32633)),ST_AsEWKT(way) from planet_osm_polygon where osm_id = 128546153;
select ST_Area(ST_Transform(ST_GeomFromText('POLYGON((2.3938146 48.8456683, 2.3941176 48.8456482, 2.3940766 48.8453958, 2.3937724 48.8454183, 2.3938146 48.8456683))', 4326), 32633));

-- Find the point on building outer linestring which is the closest to another point
select ST_AsEWKT(ST_ExteriorRing(ST_Transform(way, 4326))) from planet_osm_polygon where osm_id = 128546153;
select ST_AsEWKT(line_interpolate_point(ST_ExteriorRing(ST_Transform(way, 4326)), line_locate_point(ST_ExteriorRing(ST_Transform(way, 4326)), ST_GeomFromText('POINT(2.3935059 48.8454880)', 4326)))) from planet_osm_polygon where osm_id = 128546153;

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

-- Trees
select * from planet_osm_nodes where tags is not null limit 100;
select * from planet_osm_nodes n where ((tags->'natural') = 'tree') LIMIT 100;
select osm_id, n.natural, ST_AsEWKT(way) from planet_osm_point n where n.natural = 'tree' LIMIT 100;
select count(osm_id) from planet_osm_point n where n.natural = 'tree';

select osm_id, n.natural, ST_AsEWKT(way) from planet_osm_point n where n.natural = 'tree' and osm_id = 2389577741;
-- "SRID=900913;POINT(808968.25 5418364.78)"
SELECT ST_Box2D(ST_GeomFromText('LINESTRING(1 2, 3 4, 5 6)'));
SELECT ST_AsEWKT(ST_Envelope(ST_GeomFromText('LINESTRING(1 2, 3 4, 5 6)')));
SELECT ST_AsEWKT(ST_MakeEnvelope(808958, 5418354, 808978, 5418374, 900913));

select osm_id, n.natural, ST_AsEWKT(way) from planet_osm_point n where n.natural = 'tree' and way && ST_MakeEnvelope(808958, 5418354, 808978, 5418374, 900913);

select osm_id, n.natural, ST_AsEWKT(way), ST_Distance(way, ST_GeomFromText('POINT(808968 5418364)', 900913)) as distance from planet_osm_point n 
where n.natural = 'tree' and way && ST_MakeEnvelope(808958, 5418354, 808978, 5418374, 900913);



