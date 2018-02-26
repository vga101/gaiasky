.. _properties-file:

The configuration file
**********************

There is a configuration file which stores most of the configration settings
of Gaia Sky. This section is devoted
to these seetings that are not represented in the GUI but are still
configurable. The configuration file is located in
``$HOME/.gaiasky/global.properties``. The file is annotated with
comments specifying the function of most properties. However, here is an 
explanation of some of the properties
found in this file that are not represented in the GUI.

Graphics properties
-------------------

-  **graphics.render.time** - This property gets a boolean
   (``true``\ \|\ ``false``) and indicates whether a timestamp is to be
   added to screenshots and frames.
  
-  **graphics.screen.resizable** - Whether the window (if in windowed mode) is resiable or not. Defaults to true.

.. _data-properties:

Data properties
---------------

-  **data.json.catalog** - This property points to the ``json`` file
   where the catalog(s) to load are defined. Unless you want to load your
   own catalogs, this should either be ``data/catalog-tgas-hyg.json`` for the
   GPU-bound non-LOD version, or ``data/catalog-tgas-hyg-lod.json``, for the 
   LOD version of TGAS.

-  **data.json.objects** - Contains the ``json`` file where the
   definition of all the rest of the data is specified.

-  **data.limit.mag** - This contains the limiting magnitude above which
   stars shall not be loaded. Not all data loaders implement this. It is
   now deprecated.

Scene properties
----------------

-  **scene.labelfactor** - A real number in ``[0..n]`` that controls the
   number of star labels to display. The larger the number, the more
   stars will have a label.

-  **scene.star.threshold.quad** - This property contains the view
   angle (in degrees) boundary above which the stars are rendered as
   `quads <https://www.opengl.org/wiki/Primitive#Quads>`__.
   ``Quads`` are basically 4-vertex quadrilaterals, and they can be
   rendered as textures (images) or using ``shaders``. They display more
   detail but are costlier in terms of GPU processing. Do not touch unless
   you know what you are doing.

-  **scene.star.threshold.point** - This property contains the view
   angle (in degrees) boundary above which the stars are rendered as
   `points <https://www.opengl.org/wiki/Primitive#Point_primitives>`__.
   Points are single pixels, so they are not very resource demanding. Do not touch unless
   you know what you are doing.

-  **scene.star.threshold.none** - This property contains the view
   angle (in degrees) below which the stars are not rendered at all.
   Usually this is 0 unless you want to cull very distant stars. Do not touch unless
   you know what you are doing.

-  **scene.point.alpha.min** - Contains the minimum alpha value
   (opacity) in ``[0..1]`` for the stars rendered as ``points``. This
   should in any case be lower than ``scene.point.alpha.max``.

-  **scene.point.alpha.max** - Contains the maximum alpha value
   (opacity) in ``[0..1]`` for the stars rendered as ``points``. This
   should in any case be greater than ``scene.point.alpha.min``.

-  **scene.galaxy.3d** - Contains a boolean. If set to true, the Milky
   Way will be rendered using a blending of a 2D image with a 3D
   distribution of stars and nebulae. Otherwise, only the 2D image is
   used.

Program wide properties
-----------------------

-  **program.tutorial** - This gets a boolean (``true``\ \|\ ``false``)
   indicating whether the tutorial script should be automatically run at
   start up.

-  **program.tutorial.script** - This points to the tutorial script
   file.

-  **program.debuginfo** - If this property is set to true, some debug
   information will be shown at the top right of the window. This
   contains information such as the number of stars rendered as a quad,
   the number of stars rendered as a point or the frames per second.
   This can be activated in real time by pressing ``CTRL`` + ``D``.
