#! /bin/bash

#FILE_PATH=/home/turman/Temporary/ile-de-france-latest.osm.pbf
#FILE_PATH=/home/turman/Temporary/provence-alpes-cote-d-azur-latest.osm.pbf
FILE_PATH=/home/turman/Workspace/JOSM/test-paris-12.osm

#DB_NAME=osm_apidb_paca
#DB_NAME=osm_apidb_idf
#DB_NAME=osm_osm2pgsql_paca
#DB_NAME=osm_osm2pgsql_idf
DB_NAME=osm_osm2pgsql_test

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

~/Software/osm2pgsql/bin/osm2pgsql -c -C 1000 --number-processes=3  -d $DB_NAME $FILE_PATH
