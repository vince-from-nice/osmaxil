# Osmaxil #

Osmaxil is a free software written in Java which allows automatic data imports into the OpenStreetMap database.

It is designed as an expandable program with different plugins which can handle different types of OSM elements.

For now it's focused on building with 2 plugins available :
* OpenDataParis (http://opendata.paris.fr)
* PSS database (http://www.pss-archi.eu)

These 2 plugins update existing buildings in the OSM database by setting their height and/or number of levels tag(s) value(s).

## How to run ##

### Prerequesites ###

In order to run the program you need to have :
* Java 6 or later
* PostGIS
* Osm2pgsql

### Customize settings ###

All the settings are located in src/main/resources/settings.properties.

There's various settings such as parameters for OSM API or local PostGIS connections as well special settings for the plugins.

Plugins settings include for example which source label to set in the API changesets or the minimal matching score (see the section "How it works"). 

You also need to create you own password.properties file in src/main/resources which contains you private passwords (for OSM API and local PostGIS connection).

### Populate local PostGIS ###

The program use a local PostGIS database in order to match imports with existing OSM elements.

So before to launch the program you need to populate your local PostGIS instance with OSM data related to the area you wan to update. 

For the OpenDataParis plugin you should download the OSM data of the region named Ile-de-France (http://download.geofabrik.de/europe/france/ile-de-france-latest.osm.pbf).

### Launch it !! ###

Once you data are loaded into your local PostGIS and your settings are well defined, you just have to launch the class *org.openstreetmap.osmaxil.Application.java*. 

There's no special argument for now.

## How it works ##

The process is divided in separate phases :
* Imports loading
* Element processing
* Element synchronization
* Statistics generation

### Imports loading ###

That phase is implemented by the class named services.ImportLoader.

It loads all imports from a source whose type is depending on the actived plugin (for example the OpenDataParis plugin uses a CSV file).

For each loaded import, the program looks for matching OSM element. The notion of "matching" depends on the plugins but typically it's based on the geographic coordinates. For example, the OpenDataParis plugin looks for OSM buildings which *contains* (see the PostGIS function *ST_Contains()*) the coordinates of the imports.  

At the end of that phase all matching OSM elements are loaded into a map (see the ElementCache class) and linked to their matching imports (see the AbstractElement class).

### Element processing ###

That phase is implemented by the class named services.ElementProcessor.

The goal of that phase is to determine which imports are relevants.

First, for all matching OSM elements a matching score is set for all their matching imports. 

The implemention of the method which calculates matching scores depends on the actived plugin. For example the OpenDataParis plugin defines scores by calculating a ratio (a float between 0.0 and 1.0) of the OSM building surface to the imported building surface.

At the end the *best* matching import must be determined for each OSM element. There's 2 methods to do that.

The old method was just looking for each OSM element which one has the biggest matching score.

The new method is more complex but more efficient: first all matching imports of the OSM element are regrouped by their tag value into different lists. Then for each list (ie. for each tag value) a *total* matching score is calculated by accumulating matching score of all imports of the list. The relevant tag value is the one which corresponds to the import list which has the biggest total score.

Why to do that ? 

Let's consider an OSM building which have been matched with 4 imported buildings :
- import building A has the tag building:levels=8 and a matching score of 0.42
- import building B has the tag building:levels=8 and a matching score of 0.35
- import building C has the tag building:levels=5 and a matching score of 0.15
- import building C has the tag building:levels=0 and a matching score of 0.08

The tag value (level=8) is the same for both method, BUT:
- with the old basic method the best matching score for the building is only 0.42 (score of the building A)
- with the new complex method the best matching score for the building is 0.77 (score of A + score of B) which reflects more correctly the predominance of the tag value of 8 levels.

### Element synchronization ###

That phase is implemented by the class named services.ElementSyncrhonizer.

It eventually writes OSM elements to the OSM API. 

For each plugin a minimum matching score is defined in the settings.properties file. OSM element can be updated only if it has a import whose matching score is bigger to that minimal score.

If the matching score is enough then it tries to update one or more tag values. Depending on the plugin, update of the tag can be done only if the tag hasn't an original value yet. That way, the program will not destroy work which has been already done by other OSM contributors. That's the case with the OpenDataParis plugin.

### Statistics generation ###

That phase is implemented by the class named services.StatsGenerator.

It crashes various statistics on the stdout such as :
* Number of matched elements
* Number of updatable elements
* Number of updated elements

It also displays all these statistics by matching score ranges.


## How to contribute ##

The source code is available on GitHub : https://github.com/vince-from-nice/osmaxil

Any suggestions or pull requests are welcome :)

