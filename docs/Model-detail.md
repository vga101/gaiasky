Some models (mainly spherical planets, planetoids, moons and asteroids) are automatically generated when the Gaia Sandbox is initialising and accept parameters which tell the loader how many vertices the model should have. These parameters are set in the `json` data files and can have an impact on devices with low-end graphics processors. Let's see an example:

``` json
"model"	: {
		"args" : [true],
		"type" : "sphere",
		"params" : {
			"quality" : 150,
			"diameter" : 1.0,
			"flip" : false
			},
		"texture" : {
			"base" : "data/tex/neptune.jpg",
			}
	}
```

The `quality` parameter specifies here the number of both vertical and horizontal divisions that the sphere will have. This number is reduced in the `Android` port of the Gaia Sandbox to something between 30 and 50 depending on the object.

Additionally, some other models, such as that of the Gaia spacecraft, come from a binary model file `.g3db`. These models are created using a 3D modelling software and then exported to either `.g3db` (binary) or `.g3dj` (JSON) using [`fbx-conv`](https://github.com/libgdx/fbx-conv). You can create your own low-resolution models and export them to the right format. Then you just need to point the `json` data file to the right low-res model file. The attribute's name is `model`.

``` json
"model"	: {
		"args" : [true],
		"model" : "data/models/gaia/gaia.g3db"
	}
```