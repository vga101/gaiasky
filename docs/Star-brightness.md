The **star brightness** setting has an effect on the graphics performance because it will cause more or less stars to be rendered as quads instead of points, which means multiplying the number of vertices to send to the GPU. Quads are basically flat polygons to which a texture is applied (in this case their appearance is controlled by a shader). 

The star brightness can be increased or decreased from the `Star brightness` slider in the `Lighting` section of the [interface window](/ari-zah/gaiasandbox/wiki/User-interface#lighting).

You can also activate the **debug mode** (`CTRL + D`) to get some information on how many stars are currently being rendered as points and quads.

