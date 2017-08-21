-- SRIDs
select * from spatial_ref_sys where srid = '900913';
select srtext from spatial_ref_sys where srid = '900913';
-- "+proj=merc +a=6378137 +b=6378137 +lat_ts=0.0 +lon_0=0.0 +x_0=0.0 +y_0=0 +units=m +k=1.0 +nadgrids=@null +no_defs"
-- "PROJCS["Popular Visualisation CRS / Mercator (deprecated)",GEOGCS["Popular Visualisation CRS",DATUM["Popular_Visualisation_Datum",SPHEROID["Popular Visualisation Sphere",6378137,0,AUTHORITY["EPSG","7059"]],TOWGS84[0,0,0,0,0,0,0],AUTHORITY["EPSG","6055"]],P (...)"


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

-- Tree sur la Promenade des Anglais
select osm_id, n.natural, ST_AsEWKT(way) from planet_osm_point n where n.natural = 'tree' and osm_id = 2389577741;
-- "SRID=900913;POINT(808968.25 5418364.78)"
SELECT ST_AsEWKT(ST_MakeEnvelope(808958, 5418354, 808978, 5418374, 900913));
select osm_id, n.natural, ST_AsEWKT(way) from planet_osm_point n where n.natural = 'tree' and way && ST_MakeEnvelope(808958, 5418354, 808978, 5418374, 900913);
-- Requête qui marche !!
select osm_id, n.natural, ST_AsEWKT(way), ST_Distance(way, ST_GeomFromText('POINT(808968 5418364)', 900913)) as distance from planet_osm_point n 
where n.natural = 'tree' and way && ST_MakeEnvelope(808958, 5418354, 808978, 5418374, 900913) ORDER BY distance;

-- Imported tree
SELECT ST_AsEWKT(ST_Transform(ST_GeomFromText('POINT (7.24701747 43.72702988)', 4326), 900913));
-- Postgis: POINT(806734.294530357 5423296.14707365)
-- Osmaxil: POINT (4867670.700143859 803507.514809089)
SELECT ST_AsEWKT(ST_Transform(ST_GeomFromText('POINT (7.24701747 43.72702988)', 4326), 3857));
-- Postgis: POINT(806734.294530357 5423296.14707365)
-- Osmaxil: POINT (4867670.700143859 808893.9985083668)
POINT (4867670.700143859 808893.9985083668)

-- Tree sur la Promenade des Anglais : id="2504921074" lat="43.6950068" lon="7.2670199"
SELECT ST_Transform(ST_GeomFromText('POINT(7.2670199 43.6950068)', 4326), 900913) as tree;
select osm_id, n.natural, ST_AsEWKT(way), ST_Distance(way, ST_Transform(ST_GeomFromText('POINT(7.2670199 43.6950068)', 4326), 900913)) as distance from planet_osm_point n 
where n.natural = 'tree' and way && ST_MakeEnvelope(808958, 5418354, 808978, 5418374, 900913) ORDER BY distance;

select osm_id, n.natural, ST_AsEWKT(way), ST_Distance(way, ST_Transform(ST_GeomFromText('POINT(7.2670199 43.6950068)', 4326), 900913)) as distance from planet_osm_point n 
where n.natural = 'tree' and way && ST_Buffer(ST_Transform(ST_GeomFromText('POINT(7.2670199 43.6950068)', 4326), 900913)), 20) ORDER BY distance;

-- ------------------------------------------------------
-- Point cloud
-- ------------------------------------------------------

select x, y, z, ST_AsEWKT(geom) from point_cloud_of_nice;
select z from point_cloud_of_nice;
select count(z) from point_cloud_of_nice;

INSERT INTO point_cloud_of_nice VALUES(1, ST_Transform(ST_GeomFromText('POINT(1038159.210 6290003.290 -2775.350)', 2154), 4326));

DROP TABLE point_cloud_of_nice;
CREATE TABLE point_cloud_of_nice
(
  x numeric(11,3),
  y numeric(11,3),
  z numeric(11,3)
)
WITH (
  OIDS=FALSE
);
 
COPY point_cloud_of_nice(x, y, z)
    FROM 'E:/Geodata/Local/Cities/Nice/MNS_2009_Nice/go_06_1038_6290_9.xyz'
    WITH DELIMITER AS ' ';
SELECT AddGeometryColumn ('point_cloud_of_nice', 'geom', 4326, 'POINT', 3);
UPDATE point_cloud_of_nice SET geom = ST_Transform(ST_GeomFromText('POINT('||x||' '||y||' '||z||')', 2154), 4326);

CREATE INDEX point_cloud_geom ON point_cloud_of_nice USING GIST (geom);

SELECT ST_Extent(geom) FROM point_cloud_of_nice;

SELECT count(*) FROM point_cloud_of_nice WHERE geom && ST_MakeEnvelope(7, 43, 8, 44);
SELECT x, y, z, ST_AsEWKT(geom) FROM point_cloud_of_nice WHERE geom && ST_MakeEnvelope(7.2450063676, 43.6912734721, 7.2480203658, 43.6929180872);
SELECT x, y, z, ST_AsEWKT(geom) FROM point_cloud_of_nice 
	WHERE ST_Intersects(geom, ST_GeomFromText('POLYGON((7.2454710513 43.691712862, 7.2452904098 43.6919726228, 7.2458982073 43.6921936046, 7.2450063676 43.6912734721, 7.2454710513 43.691712862))', 4326));

SELECT count(osm_id) from planet_osm_polygon;

select osm_id, name, building, ST_SRID(way), ST_AsEWKT(ST_Transform(way, 4326)) from planet_osm_polygon where building <> '' LIMIT 100 ;
select osm_id, name, building, ST_SRID(way), ST_AsEWKT(ST_Transform(way, 4326)) from planet_osm_polygon where building <> '' AND ST_Intersects(way, ST_Transform(ST_GeomFromText('POLYGON((4 43, 4 44, 5 44, 5 43, 4 43))', 4326), 3857)) LIMIT 100 ;

select count(*) from planet_osm_polygon where building <> '' AND ST_Intersects(way, ST_Transform(ST_GeomFromText('POLYGON((7.16667656102929 43.6274385733406, 7.16667656102929 43.7730641738773, 7.34539422876236 43.7730641738773, 7.34539422876236 43.6274385733406, 7.16667656102929 43.6274385733406))', 4326), 3857));
select count(*) from planet_osm_polygon where building <> '' AND ST_Intersects(way, ST_Transform(ST_GeomFromText('POLYGON((7.16667656102929 43.6274385733406, 7.16667656102929 43.7730641738773, 7.34539422876236 43.7730641738773, 7.34539422876236 43.6274385733406, 7.16667656102929 43.6274385733406))', 4326), 3857))
	and osm_id < 0;

select ST_Intersects(ST_GeomFromText('POINT(4.31924397581823 43.5020669989816)'), ST_GeomFromText('POLYGON((4 43, 4 44, 5 44, 5 43, 4 43))', 4326));
select ST_Intersects(ST_GeomFromText('POLYGON((4.31924397581823 43.5020669989816,4.31939399447068 43.5020840055454,4.31941896763558 43.501964959498,4.31926796083632 43.5019489954508,4.31924397581823 43.5020669989816))', 4326), ST_GeomFromText('POLYGON((4 43, 4 44, 5 44, 5 43, 4 43))', 4326));

SELECT osm_id, 1 from planet_osm_polygon WHERE building <> '' AND ST_Intersects(way, ST_Transform(ST_GeomFromText('POLYGON((7.16667656102929 43.6274385733406, 7.16667656102929 43.7730641738773, 7.34539422876236 43.7730641738773, 7.34539422876236 43.6274385733406, 7.16667656102929 43.6274385733406))', 4326), 3857));
SELECT osm_id, 1 from planet_osm_polygon WHERE building <> '' AND ST_Intersects(way, ST_Transform(ST_GeomFromText('POLYGON((7.16667656102929 43.6274385733406, 7.16667656102929 43.7730641738773, 7.34539422876236 43.7730641738773, 7.34539422876236 43.6274385733406, 7.16667656102929 43.6274385733406))', 4326), 3857)) AND ST_Disjoint(way, ST_Transform(ST_GeomFromText('POLYGON((7.16667656102929 43.6274385733406, 7.16667656102929 43.7730641738773, 7.34539422876236 43.7730641738773, 7.34539422876236 43.6274385733406, 7.16667656102929 43.6274385733406))', 4326), 3857))

SELECT osm_id, ST_AsEWKT(way) as geomAsWKT, 1 from planet_osm_polygon WHERE building <> '' AND ST_Intersects(way, ST_Transform(ST_GeomFromText('POLYGON((7.16667656102929 43.6274385733406, 7.16667656102929 43.7730641738773, 7.34539422876236 43.7730641738773, 7.34539422876236 43.6274385733406, 7.16667656102929 43.6274385733406))', 4326), 3857)) AND ST_Disjoint(way, ST_Transform(ST_GeomFromText('POINT(0 0)', 4326), 3857))

SELECT AddGeometryColumn ('gluar_polygon', 'geom', 4326, 'POLYGON', 2);
DELETE FROM gluar_polygon;
INSERT INTO gluar_polygon VALUES('includingArea01', ST_GeomFromText('POLYGON((7.16667656102929 43.6274385733406, 7.16667656102929 43.7730641738773, 7.34539422876236 43.7730641738773, 7.34539422876236 43.6274385733406, 7.16667656102929 43.6274385733406))', 4326));
INSERT INTO gluar_polygon VALUES('includingArea02', ST_GeomFromText('POLYGON((7.2680997849 43.6983558566, 7.272144556 43.6983558566, 7.272144556 43.6959511977, 7.2680997849 43.6959511977, 7.2680997849 43.6983558566))', 4326));
INSERT INTO gluar_polygon VALUES('includingArea02a', ST_Buffer(ST_GeomFromText('POLYGON((7.2680997849 43.6983558566, 7.272144556 43.6983558566, 7.272144556 43.6959511977, 7.2680997849 43.6959511977, 7.2680997849 43.6983558566))', 4326), -0.001));
INSERT INTO gluar_polygon VALUES('includingArea02b', ST_Scale(ST_GeomFromText('POLYGON((7.2680997849 43.6983558566, 7.272144556 43.6983558566, 7.272144556 43.6959511977, 7.2680997849 43.6959511977, 7.2680997849 43.6983558566))', 4326), 0.5, 0.5));
INSERT INTO gluar_polygon VALUES('includingArea03', ST_GeomFromText('POLYGON((7.2272623405 43.6885776021, 7.2323405743 43.6885776021, 7.2323405743 43.6859095683, 7.2272623405 43.6859095683, 7.2272623405 43.6885776021))', 4326));

SELECT ST_Translate(ST_Scale(ST_GeomFromText('POLYGON((7.2680997849 43.6983558566, 7.272144556 43.6983558566, 7.272144556 43.6959511977, 7.2680997849 43.6959511977, 7.2680997849 43.6983558566))', 4326), 0.5, 0.5), -0.5 * (ST_Xmin(geom)+ST_XMax(geom))/2 + ((ST_Xmin(geom)+ST_XMax(geom))/2), -0.5 * (ST_Ymin(geom)+ST_YMax(geom))/2 + ((ST_Ymin(geom)+ST_YMax(geom))/2)) from (select ST_GeomFromText('POLYGON((7.2680997849 43.6983558566, 7.272144556 43.6983558566, 7.272144556 43.6959511977, 7.2680997849 43.6959511977, 7.2680997849 43.6983558566))', 4326) as geom) a;
SELECT ST_AsEWKT(ST_Translate(ST_Scale(ST_GeomFromText('POLYGON((7.2680997849 43.6983558566, 7.272144556 43.6983558566, 7.272144556 43.6959511977, 7.2680997849 43.6959511977, 7.2680997849 43.6983558566))', 4326), 0.5, 0.5), -0.5 * (ST_Xmin(geom)+ST_XMax(geom))/2 + ((ST_Xmin(geom)+ST_XMax(geom))/2), -0.5 * (ST_Ymin(geom)+ST_YMax(geom))/2 + ((ST_Ymin(geom)+ST_YMax(geom))/2))) from (select ST_GeomFromText('POLYGON((7.2680997849 43.6983558566, 7.272144556 43.6983558566, 7.272144556 43.6959511977, 7.2680997849 43.6959511977, 7.2680997849 43.6983558566))', 4326) as geom) a;

INSERT INTO gluar_polygon VALUES('includingArea03c', 'SRID=4326;POLYGON((7.269110977675 43.697754691875,7.271133363225 43.697754691875,7.271133363225 43.696552362425,7.269110977675 43.696552362425,7.269110977675 43.697754691875))');
INSERT INTO gluar_polygon VALUES('includingArea03c', ST_Translate(ST_Scale(ST_GeomFromText('POLYGON((7.2680997849 43.6983558566, 7.272144556 43.6983558566, 7.272144556 43.6959511977, 7.2680997849 43.6959511977, 7.2680997849 43.6983558566))', 4326), 0.5, 05), -0.5 * (ST_Xmin(geom)+ST_XMax(geom))/2 + ((ST_Xmin(geom)+ST_XMax(geom))/2), -0.5 * (ST_Ymin(geom)+ST_YMax(geom))/2 + ((ST_Ymin(geom)+ST_YMax(geom))/2))) from (select ST_GeomFromText('POLYGON((7.2680997849 43.6983558566, 7.272144556 43.6983558566, 7.272144556 43.6959511977, 7.2680997849 43.6959511977, 7.2680997849 43.6983558566))', 4326) as geom) a;

SELECT x, y, z FROM point_cloud_of_nice, ST_Transform(ST_GeomFromText('POLYGON((4 43, 4 44, 5 44, 5 43, 4 43))', 3857), 4326) as geom2 
WHERE ST_Intersects(geom, ST_Translate(ST_Scale(geom2, 0.8, 0.8),
 -0.8*(ST_Xmin(geom2)+ST_XMax(geom2))/2 + ((ST_Xmin(geom2)+ST_XMax(geom2))/2), -0.8*(ST_Ymin(geom2)+ST_YMax(geom2))/2 + ((ST_Ymin(geom2)+ST_YMax(geom2))/2)))

SELECT x, y, z FROM point_cloud_of_nice, ST_Transform(ST_GeomFromText('POLYGON((804006.02 5416600.7,804018.6 5416615.94,804033.63 5416603.32,804034.41 5416604.24,804049.66 5416587.46,804047.21 5416585.62,804057.9 5416569.61,804043.32 5416559.29,804038.08 5416567.76,804038.97 5416568.38,804036.75 5416572.07,804035.86 5416571.45,804033.07 5416575.46,804030.29 5416579,804031.07 5416579.61,804024.95 5416586.69,804024.17 5416585.92,804021.16 5416588.85,804017.6 5416591.77,804017.93 5416592.39,804014.48 5416595.47,804013.82 5416594.85,804006.02 5416600.7))', 3857), 4326) as includingGeom 
WHERE ST_Intersects(geom, ST_Translate(ST_Scale(includingGeom, 0.8, 0.8),-0.8*(ST_Xmin(includingGeom)+ST_XMax(includingGeom))/2 + ((ST_Xmin(includingGeom)+ST_XMax(includingGeom))/2),-0.8*(ST_Ymin(includingGeom)+ST_YMax(includingGeom))/2 + ((ST_Ymin(includingGeom)+ST_YMax(includingGeom))/2)))

SELECT x, y, z FROM point_cloud_of_nice
WHERE ST_Intersects(geom, ST_Translate(ST_Scale(includingGeom, 0.8, 0.8),-0.8*(ST_Xmin(includingGeom)+ST_XMax(includingGeom))/2 + ((ST_Xmin(includingGeom)+ST_XMax(includingGeom))/2),-0.8*(ST_Ymin(includingGeom)+ST_YMax(includingGeom))/2 + ((ST_Ymin(includingGeom)+ST_YMax(includingGeom))/2)))

SELECT name, ST_AsEWKT(geom) as geomAsWKT, 1 from gluar_polygon;

INSERT INTO gluar_polygon VALUES('gluar01', ST_GeomFromText('POLYGON((7.2454710513 43.691712862, 7.2452904098 43.6919726228, 7.2458982073 43.6921936046, 7.2450063676 43.6912734721, 7.2454710513 43.691712862))', 4326));
INSERT INTO gluar_polygon VALUES('gluar02', ST_Transform(ST_GeomFromText('POLYGON((797864.99 5412635.33,797869.61 5412636.42,797870.94 5412630.98,797866.45 5412629.87,797864.99 5412635.33))', 3857), 4326));
SELECT name, ST_AsEWKT(geom) FROM gluar_polygon;

SELECT x, y, z FROM point_cloud_of_nice WHERE ST_Intersects(geom, ST_Transform(ST_GeomFromText('POLYGON((797864.99 5412635.33,797869.61 5412636.42,797870.94 5412630.98,797866.45 5412629.87,797864.99 5412635.33))', 3857), 4326))
SELECT x, y, z FROM point_cloud_of_nice WHERE ST_Intersects(geom, ST_Transform(ST_GeomFromText('POLYGON((809523.46 5418884.59,809534.15 5418886.44,809551.74 5418889.67,809550.07 5418894.14,809568.44 5418901.68,809579.34 5418875.51,809566.99 5418870.89,809564.76 5418869.81,809560.09 5418867.81,809557.97 5418873.5,809554.52 5418883.2,809535.48 5418876.58,809537.15 5418870.42,809552.74 5418875.97,809554.08 5418872.27,809530.92 5418864.42,809523.46 5418884.59))', 3857), 4326))
SELECT x, y, z FROM point_cloud_of_nice WHERE ST_Intersects(geom, ST_Transform(ST_GeomFromText('POLYGON((809050.69 5418683.97,809057.26 5418686.58,809061.6 5418688.28,809061.82 5418686.28,809070.39 5418688.12,809068.94 5418696.13,809104.23 5418710.6,809103.68 5418712.3,809115.37 5418716.92,809119.82 5418708.6,809121.26 5418705.68,809115.48 5418690.13,809112.03 5418684.12,809104.34 5418672.27,809105.9 5418671.03,809104.46 5418667.74,809103.34 5418663.95,809103.01 5418659.79,809103.34 5418657.33,809101.45 5418657.02,809100.11 5418659.49,809056.81 5418634.7,809055.48 5418637.47,809054.47 5418651.48,809055.59 5418653.02,809053.25 5418657.79,809052.36 5418664.11,809051.91 5418676.11,809050.69 5418683.97))', 3857), 4326))

SELECT * FROM planet_osm_ways WHERE id=142850664;

select ST_AsText(ST_Centroid(way)) from planet_osm_polygon where osm_id = 143754774;
select ST_X(ST_Centroid(way)), ST_Y(ST_Centroid(way)) from planet_osm_polygon where osm_id = 143754774;
select ST_X(ST_Centroid(way)) as x, ST_Y(ST_Centroid(way)) as y from planet_osm_polygon where osm_id = 143754774

select ST_X(center) as x, ST_Y(center) as y from (select ST_Transform(ST_Centroid(way), 2154) as center from planet_osm_polygon where osm_id = 143754774) a;
select ST_X(center) as x, ST_Y(center) as y from (select ST_Transform(ST_Centroid(way), 2154) as center from planet_osm_polygon where osm_id = 140247253) a;
select ST_X(center) as x, ST_Y(center) as y from (select ST_Transform(ST_Centroid(way), 2154) as center from planet_osm_polygon where osm_id = 142850777) a;
select osm_id,name,building,ST_Area(way),ST_AsEWKT(way) from planet_osm_polygon where osm_id = 142850777;

select * from apple_buildings where height > 0 limit 100;
select count(gid) from apple_buildings;
select count(gid) from apple_buildings where height > 0;

select * from planet_osm_polygon where osm_id = -1867273;
select * from planet_osm_rels where id = 1867273;
select ST_X(center) as x, ST_Y(center) as y from (select ST_Transform(ST_Centroid(way), 2154) as center from planet_osm_polygon where osm_id = -1867273) a;

select * from planet_osm_polygon where osm_id = -1848685;
select * from planet_osm_rels where id = 1848685;
select ST_X(center) as x, ST_Y(center) as y from (select ST_Transform(ST_Centroid(way), 2154) as center from planet_osm_polygon where osm_id = -1848685) a;

select * from planet_osm_polygon where osm_id = -175167;
select * from planet_osm_rels where id = 175167;
"{144458571,144458569}"
select ST_X(center) as x, ST_Y(center) as y from (select ST_Transform(ST_Centroid(way), 2154) as center from planet_osm_polygon where osm_id = -175167) a;
select ST_X(center) as x, ST_Y(center) as y from (select ST_Transform(ST_Centroid(way), 2154) as center from planet_osm_polygon where osm_id = -175167) a



