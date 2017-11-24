# Osmaxil #

Osmaxil is a free software written in Java which allows automatic data imports into the OpenStreetMap database.

It provides different flows which can handle different kinds of imports and it is designed as an expandable program with the use of plugins. 

There are 4 types of flows:
* __elevator__ : this flow adds the height tag to existing OSM elements, the process is driven by existing elements
* __updater__ : this flow adds or updates tag(s) to existing OSM elements, the process is driven by imports
* __maker__ : this flow modifies or deletes existing OSM elements and create new ones, the process is driven by imports
* __remaker__ : this flow modifies or deletes existing OSM elements and create new ones, the process is driven by imports but new elements are added only in replacement of existing elements.

The flows works with OSM elements and for now two kinds of elements can be supported:
* __buildings__
* __vegetation__

Here are all the imports which has been performed for now:
* _Import with VegetationMaker in Grenoble_ : its data source is Open Data de Grenoble (http://data.metropolegrenoble.fr). It has already been applied on the live server on November 2017: 55k buildings of Montpellier has been updated with their building:height tag. More information are available on [the Wiki page](https://wiki.openstreetmap.org/wiki/Grenoble,_France/Trees_Import) dedicated to the import.
* _Import with BuildingElevator in Montpellier_ : its data source is Open Data de Montpellier Mediterranee Metropole (http://data.montpellier3m.fr). It has already been applied on the live server on November 2017: 55k buildings of Montpellier has been updated with their building:height tag. More information are available on [the Wiki page](https://wiki.openstreetmap.org/wiki/Montpellier,_France/Buildings_Heights_Import) dedicated to the import.
* _Import with BuildingElevator in Nice_ : its data source is OpenData portal of Nice Cote d'Azur (http://opendata.nicecotedazur.org). It has already been applied on the live server on September 2017: 52k buildings of Nice has been updated with their building:height tag. More information are available on [the Wiki page](https://wiki.openstreetmap.org/wiki/Nice,_France/Buildings_Heights_Import) dedicated to the import.
* _Import with VegetationMaker in Nice_ : its data source is the OpenData portal of Nice Cote d'Azur (http://opendata.nicecotedazur.org/site/). It has already been applied on the live server on August 2015: 29411 new trees has been added and 835 existing trees has been updated. More information are available on [the Wiki page](https://wiki.openstreetmap.org/wiki/Nice,_France/Trees_Import) dedicated to the import.
* _Import with BuildingRemaker in Paris_ : its data source is OpenData portal of Paris (http://opendata.paris.fr). It aims to provide a better building shape cutting (352k elements instead of 86k currently), it has been aborted but it should be restarted in the future.
* _Import with BuildingUpdater for Paris_ : its data source is OpenData portal of Paris (http://opendata.paris.fr). It has already been applied on the live server on April 2015: 49k parisian buildings has been updated with their building:levels tag. More information are available on [the Wiki page](http://wiki.openstreetmap.org/wiki/Paris,_France/Buildings_Heights_Import) dedicated to the import.
* _Import with BuildingUpdater in PSS_ : its data source is the database of the PSS association (http://www.pss-archi.eu). It contains informations (including height and floors) about 47k buildings all over France but it cannot be applied for now because the PSS association publishes their database under the CC-BY-ND-NC licence wich is incompatible with the ODbL licence. It could be changed in the future (I hope). 

## How to run ##

### Prerequesites ###

In order to run the program you need to have :
* Java
* Maven 
* PostGIS
* Osm2pgsql
* GDAL / OGR (for elevator flows only)

### Customize settings ###

All the settings are located in src/main/resources/settings.properties.

There's various settings such as parameters for OSM API or local PostGIS connections as well special settings for the plugins.

Plugins settings include for example which source label to set in the API changesets or the minimal matching score (see the section "How it works"). 

You also need to create you own password.properties file in src/main/resources which contains you private passwords (for OSM API and local PostGIS connection).

### Populate local PostGIS ###

The program use a local PostGIS database in order to match imports with existing OSM elements.

So before to launch the program you need to populate your local PostGIS instance with OSM data related to the area you want to update. 

For Paris plugins you should download the OSM data of the Ile-de-France region (http://download.geofabrik.de/europe/france/ile-de-france-latest.osm.pbf).

### Launch it !! ###

Once you data are loaded into your local PostGIS and your settings are well defined, you just have to launch the class *org.openstreetmap.osmaxil.Application.java*. 

There's no special argument for now.

## How it works ##

The process is divided in separate phases :
* Imports loading
* Element processing
* Element synchronization
* Statistics generation

### Elevator flow ###

TODO

### Maker flow ###

TODO

#### Updater flow ####

#### Imports loading ####

It loads all imports from a source whose type is depending on the actived plugin (for example the OpenDataParis plugin uses a CSV file).

For each loaded import, the program looks for matching OSM element. The notion of "matching" depends on the plugins but typically it's based on the geographic coordinates. For example, the OpenDataParis plugin looks for OSM buildings which *contains* (see the PostGIS function *ST_Contains()*) the coordinates of the imports.  

At the end of that phase all matching OSM elements are loaded into a map (see the ElementCache class) and linked to their matching imports (see the AbstractElement class).

#### Element processing ####

The goal of that phase is depending on the type of the plugin.

If it's an updater plugin, the processing phase tries to determine which matching imports are relevants.

First, for all matching OSM elements a matching score is calculated. The implemention of the method which calculates matching scores depends on the actived plugin. For example the OpenDataParis plugin defines scores by calculating a ratio (a float between 0.0 and 1.0) of the OSM building surface to the imported building surface.

At the end the *best* matching import must be determined for each OSM element. There's 2 methods to do that.

The old method was just looking for each OSM element which one has the biggest matching score.

The new method is more complex but more efficient: first all matching imports of the OSM element are regrouped by their tag value into different lists. Then for each list (ie. for each tag value) a *total* matching score is calculated by accumulating matching score of all imports of the list. The relevant tag value is the one which corresponds to the import list which has the biggest total score.

Why to do that ? 

Let's consider an OSM building which have been matched with 4 imported buildings :
- import building A has the tag building:levels=8 and a matching score of 0.42
- import building B has the tag building:levels=8 and a matching score of 0.45
- import building C has the tag building:levels=5 and a matching score of 0.15
- import building C has the tag building:levels=0 and a matching score of 0.08

The tag value (level=8) is the same for both method, BUT:
- with the old basic method the best matching score for the building is only 0.45 (score of the building B)
- with the new complex method the best matching score for the building is 0.87 (score of A + score of B) which reflects more correctly the predominance of the tag value of 8 levels.

#### Element synchronization ####

This phase eventually writes OSM elements. Depending on the type of the plugin (updater or remaker), *writing* means *updating* or *remaking*.

Furthermore, *writing* can be done directly to the OSM database via the OSM API, or it can be done inderectly by generating a XML files. Theses generated files can be considered as changes proposals: after that manual merges must done by a real humans.

The parameter **osmaxil.syncMode** defined in settings.xml can have the following values:
* no (no writings, but could be usefull for statistics) 
* api (direct writing with the OSM API)
* gen (indirect writing with generated XML files)

Note also that in the case of updating, a minimum matching score is defined in the settings.properties file for each updater plugin. OSM element can be updated only if it has a import whose matching score is bigger to that minimal score. If the matching score is enough then it tries to update one or more tag values. Depending on the plugin, update of the tag can be done only if the tag hasn't an original value yet. That way, the program will not destroy work which has been already done by other OSM contributors. That's the case with the OpenDataParis plugin.

## How to contribute ##

The source code is available on GitHub : https://github.com/vince-from-nice/osmaxil

Any suggestions or pull requests are welcome :)
