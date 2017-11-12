#! /bin/bash

#FILE_PATH=/home/turman/Workspace/JOSM/bug-crash-with-too-big-id.osm
#FILE_PATH=/home/turman/Workspace/JOSM/bug-failure-with-multipolygon.osm
#FILE_PATH=/home/turman/Workspace/JOSM/test-paris-12-before-osmaxil.osm
#FILE_PATH=/home/turman/Workspace/JOSM/test-paris-12-parts.osm
#FILE_PATH=/home/turman/Workspace/JOSM/live-paris-home.osm
#FILE_PATH=/home/geodata/OpenStreetMap/Extracts/france-latest.osm.pbf
#FILE_PATH=/home/geodata/OpenStreetMap/Extracts/ile-de-france-latest.osm.pbf
#FILE_PATH=/home/geodata/OpenStreetMap/Extracts/provence-alpes-cote-d-azur-latest.osm.pbf
#FILE_PATH=/e/Geodata/Global/OpenStreetMap/provence-alpes-cote-d-azur-2017-08-01.osm.pbf
#FILE_PATH=/e/Geodata/Global/OpenStreetMap/languedoc-roussillon-2017-10-22.osm.pbf
FILE_PATH=/e/Geodata/Global/OpenStreetMap/rhone-alpes-2017-11-11.osm.pbf

#DB_NAME=osm_apidb_paca
#DB_NAME=osm_apidb_idf
#DB_NAME=osm2pgsql_test
#DB_NAME=osm2pgsql_paca
#DB_NAME=osm2pgsql_idf
#DB_NAME=osm2pgsql_languedoc
DB_NAME=osm2pgsql_rhone-alpes

#POSTGIS_PATH=/usr/share/postgresql/9.1/contrib/postgis-1.5/
POSTGIS_PATH="/e/Software/PostgreSQL 9.3/share/contrib/postgis-2.1/"

OSM2PGSQL_PATH=/e/Software/Osm2Pgsql
#OSM2PGSQL_PATH=/home/turman/Software/osm2pgsql/bin

dropdb --username=postgres $DB_NAME;
createdb --username=postgres -E UTF-8 $DB_NAME

psql --username=postgres --dbname=$DB_NAME --command="CREATE EXTENSION hstore"
#psql --username=postgres --dbname=$DB_NAME --command="CREATE EXTENSION postgis"

psql --username=postgres --dbname=$DB_NAME --file="$POSTGIS_PATH/postgis.sql"
psql --username=postgres --dbname=$DB_NAME --file="$POSTGIS_PATH/spatial_ref_sys.sql"

#psql --username=postgres --dbname=$DB_NAME --command="ALTER TABLE geometry_columns OWNER TO gisuser"
#psql --username=postgres --dbname=$DB_NAME --command="ALTER TABLE spatial_ref_sys OWNER TO gisuser"

#psql -d $DB_NAME < /usr/share/doc/osmosis/examples/pgsnapshot_schema_0.6.sql
#osmosis --read-pbf file="$FILE_PATH" --write-apidb user="postgres" database="$DB_NAME"

# TODO: try to use the --style $OSM2PGSQL_PATH/default.style (no need to copy osm2pgsql to c:/libs/share)
cd $OSM2PGSQL_PATH
./osm2pgsql --username=postgres -s -c -C 500 --number-processes=3 -d $DB_NAME $FILE_PATH

echo "Ok postgis setup is done"
