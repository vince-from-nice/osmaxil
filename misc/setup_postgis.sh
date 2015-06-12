#! /bin/bash

#FILE_PATH=/home/turman/Workspace/JOSM/bug-crash-with-too-big-id.osm
#FILE_PATH=/home/turman/Workspace/JOSM/bug-failure-with-multipolygon.osm
#FILE_PATH=/home/turman/Workspace/JOSM/test-paris-12-before-osmaxil.osm
#FILE_PATH=/home/turman/Workspace/JOSM/test-paris-12-parts.osm
FILE_PATH=/home/turman/Workspace/JOSM/live-paris-home.osm
#FILE_PATH=/home/turman/Temporary/GeoData/OSM/ile-de-france-latest.osm.pbf
#FILE_PATH=/home/turman/Temporary/GeoData/OSM/provence-alpes-cote-d-azur-latest.osm.pbf
#FILE_PATH=/home/turman/Temporary/GeoData/OSM/france-latest.osm.pbf

#DB_NAME=osm_apidb_paca
#DB_NAME=osm_apidb_idf
DB_NAME=osm_osm2pgsql_test
#DB_NAME=osm_osm2pgsql_paca
#DB_NAME=osm_osm2pgsql_idf
#DB_NAME=osm_osm2pgsql_france

#OSM2PGSQL=osm2pgsql
OSM2PGSQL=/home/turman/Software/osm2pgsql/bin/osm2pgsql

dropdb  $DB_NAME;
createdb $DB_NAME -E UTF-8

psql --username=postgres --dbname=$DB_NAME --command="CREATE EXTENSION hstore"
#psql --username=postgres --dbname=$DB_NAME --command="CREATE EXTENSION postgis"

psql --username=postgres --dbname=$DB_NAME --file=/usr/share/postgresql/9.1/contrib/postgis-1.5/postgis.sql
psql --username=postgres --dbname=$DB_NAME --file=/usr/share/postgresql/9.1/contrib/postgis-1.5/spatial_ref_sys.sql

#psql --username=postgres --dbname=$DB_NAME --command="ALTER TABLE geometry_columns OWNER TO gisuser"
#psql --username=postgres --dbname=$DB_NAME --command="ALTER TABLE spatial_ref_sys OWNER TO gisuser"

#psql -d $DB_NAME < /usr/share/doc/osmosis/examples/pgsnapshot_schema_0.6.sql
#osmosis --read-pbf file="$FILE_PATH" --write-apidb user="postgres" database="$DB_NAME"

$OSM2PGSQL -s -c -C 500 --number-processes=3 -d $DB_NAME $FILE_PATH

echo "Ok postgis setup is done"
