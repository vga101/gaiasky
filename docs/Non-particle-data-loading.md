Most of the entities and celestial bodies that are not stars in the Gaia Sandbox scene are defined in a series of `json` files and are loaded using the [`JsonLoader`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/JsonLoader.java). The format is very flexible and loosely matches the underneath data model, which is a scene graph tree. Here are the subsections of this article:

- [[Top-level objects|Non-particle-data-loading#top-level-objects]]
- [[Planets, moons, asteroids, etc.|Non-particle-data-loading#planets-moons-asteroids-and-all-rigid-bodies]]
- [[Orbits|Non-particle-data-loading#orbits]]
- [[Grids and other special objects|Non-particle-data-loading#grids-and-other-special-objects]]




## Top-level objects

All objects in the `json` files must have at least the following 5 properties:
- `name`: The name of the object.
- `color`: The colour of the object. This will translate to the line colour in orbits, to the colour of the point for planets when they are far away and to the colour of the grid in grids.
- `ct`: The [`ComponentType`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/render/SceneGraphRenderer.java#L59). This is basically a `string` that will be matched to the entity type in `ComponentType` enum. Valid component types are `Stars`, `Planets`, `Moons`, `Satellites`, `Atmospheres`, `Constellations`, etc. 
- `impl`: The package and class name of the implementing class.
- `parent`: The name of the parent entity.

Additionally, different types of entities accept different additional parameters which are matched to the model using reflection. Here are some examples of these parameters:

- `size`: The size of the entity, usually the radius in `km`.
- `appmag`: The apparent magnitude.
- `absmag`: The absolute magnitude.

Below is an example of a simple entity, the equatorial grid:

``` json
{
	"name" : "Equatorial grid",
	"color" : [1.0, 0.0, 0.0, 0.5],
	"size" : 1.2e12,
	"ct" : "Equatorial",

	"parent" : "Universe", 
	"impl" : "gaia.cu9.ari.gaiaorbit.scenegraph.Grid"
}
```

## Planets, moons, asteroids and all rigid bodies

Planets, moons and asteroids all use the model object [`Planet`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/scenegraph/Planet.java). This provides a series of utilities that make their `json` specifications look similar.

### Coordinates
Within the `coordinates` object one specifies how to get the positional data of the entity given a time. This object contains a reference to the implementation class (which must implement [`IBodyCoordinates`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/util/coord/IBodyCoordinates.java)) and the necessary parameters to initialize it. There are currently a bunch of implementations that can be of use:

- `OrbitLintCoordinates` - The coordinates of the object are linearly interpolated using the data of its orbit, which is defined in a separated entity. See the [[Orbits|Non-particle-data-loading#orbits]] section for more info. The `name` of the orbit entity must be given. For instance, the Hygieia moon uses orbit coordinates.

  ``` json
  "coordinates" : {				
  	"impl" : "gaia.cu9.ari.gaiaorbit.util.coord.OrbitLintCoordinates",
  	"orbitname" : "Hygieia orbit"
  }
  ```
- `StaticCoordinates` - For entities that never move. A position is required. For instance, the Milky Way object uses static coordinates:

  ``` json
  "coordinates" : {			
  	"impl" : "gaia.cu9.ari.gaiaorbit.util.coord.StaticCoordinates",
  	"position" : [-2.1696166830918058e+17, -1.2574136144478805e+17, -1.8981686396725044e+16]
  }
  ```
- `AbstractVSOP87` - Used for the major planets, these coordinates implement the `VSOP87` algorithms. Only the implementation is needed. For instance, the Earth uses these coordinates.

  ``` json
  "coordinates" : {				
  	"impl" : "gaia.cu9.ari.gaiaorbit.util.coord.vsop87.EarthVSOP87"
  }
  ```
- `GaiaCoordinates` - Special coordinates for Gaia.
- `MoonAACoordinates` - Special coordinates for the moon using the algorithm described in the book Astronomical Algorithms by Jean Meeus.

### Rotation
The `rotation` object describes, as you may imagine, the rigid rotation of the body in question. A rotation is described by the following parameters:
- `period`: The rotation period in hours.
- `axialtilt`: The axial tilt is the angle between the equatorial plane of the body and its orbital plane. In degrees.
- `inclination`: The inclination is the angle between the orbital plane and the ecliptic. In degrees.
- `ascendingnode`: The ascending node in degrees.
- `meridianangle`: The meridian angle in degrees.

For instance, the rotation of Mars:
``` json
"rotation": {
	// In hours			
	"period" : 24.622962156,
	// Angle between equatorial plane and orbital plane
	"axialtilt" : 25.19,
	// Inclination of orbit plane with respect to ecliptic
	"inclination" : 1.850,
	"ascendingnode" : 47.68143,
	"meridianangle" : 176.630
}
```

### Model
This object describes the model which must be used to represent the entity. Models can have two origins:
- They may come from a **3D model file**. In this case, you just need to specify the file.

  ``` json
  "model": {
  	"args" : [true],
  	"model" : "data/models/gaia/gaia.g3db"
  }
  ```

- They may be **generated on the fly**. In this case, you need to specify the type of model, a series of parameters and the texture or textures.

  ``` json
  "model"	: {
  	"args" : [true],
  	"type" : "sphere",
  	"params" : {
  		"quality" : 180,
  		"diameter" : 1.0,
  		"flip" : false
  	},
	"texture" : {
		"base" : "data/tex/earth.jpg",
		"hires" : "data/tex/earth-8k.jpg",
		"specular" : "data/tex/earth-specular.jpg",
		"normal" : "data/tex/earth-normal-4k.jpg",
		"night" : "data/tex/earth-night-2k.jpg"
	}
  }
  ```
  - `type`: The type of model. Possible values are `sphere`, `disc`, `cylinder` and `ring`.
  - `params`: Parameters of the model. This depends on the type. The `quality` is the number of both horizontal and vertical divisions. The `diameter` is the diameter of the model and `flip` indicates whether the normals should be flipped to face outwards. The `ring` type also accepts `innerradius` and `outerradius`.
  - `texture`: Indicates the texture or textures to apply. The `base` texture is the one applied in normal conditions. The `hires` is applied when the camera is very close to the model. The `specular` is the specular map to produce specular reflections. The `normal` is a normal map to produce extra detail in the lighting. The `night` is the texture applied to the part of the model in the shade.

### Atmosphere
Planet atmospheres can also be defined using this object. The `atmosphere` object gets a number of physical quantities that are fed in the atmospheric scattering algorithm ([Sean O'Neil, GPU Gems](http://http.developer.nvidia.com/GPUGems2/gpugems2_chapter16.html)).
``` json
"atmosphere" : {
	"size" : 6600.0,
	"wavelengths" : [0.650, 0.570, 0.475],
	"m_Kr" : 0.0025,
	"m_Km" : 0.001,
	
	"params" : {
		"quality" : 180,
		// Atmosphere diameters are always 2
		"diameter" : 2.0,
		"flip" : true
	}
}
```

## Orbits
When we talk about orbits in this context we talk about orbit lines. In the Gaia Sandbox orbit lines may be created from two different sources. The sources are used by a class implementing the [`IOrbitDataProvider`](/ari-zah/gaiasandbox/blob/master/core/src/gaia/cu9/ari/gaiaorbit/data/orbit/IOrbitDataProvider.java) interface, which is also specified in ther `orbit` object.
- An **orbit data file**. In this case, the orbit data provider is `OrbitFileDataProvider`.
- The **orbital elements**, where the orbit data provider is `OrbitalParametersProvider`.

If the orbit is pre-sampled it comes from an **orbit data file**. In the Gaia Sandbox the orbits of all major planets are pre-sampled, as well as the orbit of Gaia. For instance, the orbit of **Venus**.

``` json
{
	"name" : "Venus orbit",
	"color" : [1.0, 1.0, 1.0, 0.55],
	"ct" : "Orbits",
		
	"parent" : "Sol", 
	"impl" : "gaia.cu9.ari.gaiaorbit.scenegraph.Orbit",
	"provider" : "gaia.cu9.ari.gaiaorbit.data.orbit.OrbitFileDataProvider",
			
	"orbit" : {
		"source" : "data/orb.VENUS.dat",
	}
}
```

If you prefer to define the orbit using the [orbital elements](http://en.wikipedia.org/wiki/Orbital_elements), you need to specify these parameters in the `orbit` object. For example, the orbit of **Phobos**.

``` json
{
	"name" : "Phobos orbit",
	"color" : [0.7, 0.7, 1.0, 0.4],
	"ct" : "Orbits",
	
	"parent" : "Mars", 
	"impl" : "gaia.cu9.ari.gaiaorbit.scenegraph.Orbit",
	"provider" : "gaia.cu9.ari.gaiaorbit.data.orbit.OrbitalParametersProvider",
		
	"orbit" : {
		// In days
		"period" : 0.31891023,
		// 2010 Jan 1 12:00
		"epoch" : 2455198,
		"semimajoraxis" : 9377.2,
		"eccentricity" : 0.0151,
		// Inclination of orbit with respect to the planet's Equator
		"inclination" : 1.082,
		"ascendingnode" : 16.946,
		"argofpericenter" : 157.116,
		"meananomaly" : 241.138
	}
}
```

## Grids and other special objects
There are a last family of objects which do not fall in any of the previous categories. These are grids and other objects such as the Milky Way (inner and outer parts). These objects usually have a special implementation and specific parameters, so they are a good example of how to implement new objects.

``` json
{
	"name" : "Galactic grid",
	"color" : [0.3, 0.5, 1.0, 0.5],
	"size" : 1.4e12,
	"ct" : Galactic,
	"transformName" : equatorialToGalactic,

	"parent" : "Universe", 
	"impl" : "gaia.cu9.ari.gaiaorbit.scenegraph.Grid"
}
```
For example, the grids accept a parameter `transformName`, which specifies the geometric transform to use. In the case of the galactic grid, we need to use the `equatorialToGalactic` transform to have the grid correctly positioned in the celestial sphere.