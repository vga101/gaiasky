Gaia Sky uses a flexible data loading mechanism where the correspondences between data loader and files are defined in a couple of `JSON` files which are specified in the `global.properties` configuration file in `$HOME/.gaiasky/`. The two main files are the catalog file (usually `data/catalog-*.json`) and the objects file (`data/data-low.json`, `data/data-normal.json` and `data/data-high.json` are the default options, which provide the low, normal and high graphics quality settings). See the [[Configuration files]] section for more information on this.

### catalog-*.json example file

``` json
{ "data" : [
	{
		"loader": "gaia.cu9.ari.gaiaorbit.data.stars.HYGBinaryLoader",
		"files": [ "data/hygxyz.bin" ]
	}
]}
```

### data-*.json example file

``` json
{ "data" : [
	{
		"loader": "gaia.cu9.ari.gaiaorbit.data.JsonLoader",
		"files": [ "data/planets-normal.json",
					"data/moons-normal.json",
					"data/satellites.json",
					"data/asteroids.json",
					"data/orbits_planet.json",
					"data/orbits_moon.json",
					"data/orbits_asteroid.json",
					"data/orbits_satellite.json",
					"data/extra-low.json",
					"data/locations.json",
					"data/locations_earth.json",
					"data/locations_moon.json"]
	},
	{
		"loader": "gaia.cu9.ari.gaiaorbit.data.constel.ConstellationsLoader",
		"files": [ "data/constel_hip.csv" ]
	},
	{
		"loader": "gaia.cu9.ari.gaiaorbit.data.constel.ConstelBoundariesLoader",
		"files": [ "data/boundaries.csv" ]
	}
]}
```

As you see the format in both files is based on specifying `Java` `"loader"` classes that will load the list of files under the `"files"` property. The format should be pretty self-explanatory, but here are some rules:

![Gaia Sky data loading diagram](http://www.zah.uni-heidelberg.de/fileadmin/user_upload/gaia/gaiasky/img/diagrams/gs_top_level.png "Gaia Sandbox data loading diagram")

- The **`"data"`** property contains a list of `Java` classes that implement the [`ISceneGraphLoader`](https://github.com/ari-zah/gaiasky/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/ISceneGraphLoader.java) interface. Each one of these will load a different kind of data; the [`JSONLoader`](https://github.com/ari-zah/gaiasky/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/JsonLoader.java) loads non-catalog data (planets, satellites, orbits, etc.), the [`STILCatalogLoader`](https://github.com/ari-zah/gaiasky/blob/master/desktop/src/gaia/cu9/ari/gaiaorbit/data/stars/STILCatalogLoader.java) loads `VOTables`, `FITS`, `CSV` and other files through the [`STIL`](http://www.star.bristol.ac.uk/~mbt/stil/) library, [`ConstellationsLoader`](https://github.com/ari-zah/gaiasky/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/ConstellationsLoader.java) and [`ConstellationsBoundariesLoader`](https://github.com/ari-zah/gaiasky/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/ConstelBoundariesLoader.java) load constellation data and constellation boundary data respectively and so on.
- Then, for each one of these data loaders a **list of files** is defined. This list will be passed to the loader, which will try to load these files and add them to the scene graph.

Information on the default providers can be found in the following sections:

- [[Particle data: loading catalogs|particle-data-loading]]
- [[Non-particle data: planets, orbits, grids, etc.|non-particle-data-loading]]
- [[Loading from scripts]]
