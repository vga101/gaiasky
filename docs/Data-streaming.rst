.. _data-streaming-lod:

Data streaming: Levels of detail
********************************

This section discusses the Levels of detail (LOD) datasets (from Gaia DR2 on) where not all data
fits into the CPU memory (RAM) and especially the GPU memory (VRAM).
In order to solve the issue, Gaia Sky implements a LOD structure based on the spatial distribution
of stars into an octree. The culling of the octree is determined using a draw distance setting, called
*alpha*. *Alpha* is actualy the minimum solid angle from the camera that an octant must have for it to
be observed and its stars to be rendered. Larger *alpha*s lead to less octants being observed, and smaller
*nu*s lead to more octants being observed.

Balancing the loading of data depends on several parameters:

- The maximum java heap memory (set to 4 Gb by default), let's call it *maxheap*.
- The available graphics memory (VRAM, video ram). It depends on your graphics card. Let's call it *VRAM*.
- The draw distance setting , *alpha*.
- The maximum number of loaded stars, 'nu'. This is in the configuration file (`~/.gaiasky/global.properties`) under
the key `scene.octree.maxstars`. The default value is balancing the 4 Gb of *maxheap* and the default data set.

So basically, a low *alpha* (below 50-60 degrees) means lots of observed octants and lots of stars. Setting *alpha* very
low causes Gaia Sky to try to load lots of data, eventually overflowing the heap space and creating an OutOfMemoryError. 
To mitigate that, one can also increase the *maxheap* setting (`gaiasky` script in the download package, `core/build.gradle`, `run` task
if running from source. The JVM argument is called `-Xmx`. More info).

Finally, there is the maximum number of loaded stars, *nu*. This is a number is set according to the *maxheap* setting. 
When the number of loaded stars is larger than *nu*, the loaded octants that have been
unobserved for the longest time will be unloaded and their memory structures will be freed (both in GPU and CPU). This poses a
problem if the draw distance setting is set so that the observed octants at a single moment contain more stars than than *nu*. That
is why high values for *alpha* are recommended. Usually, values between 60 and 80 are fine, depending on the dataset and the machine.