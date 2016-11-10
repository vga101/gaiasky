.. _properties-file:

The properties file
*******************

There is a configuration file which stores most of the properties
explained in the previous section and some more. This section is devoted
to these properties that are not represented in the GUI but are still
configurable. The configuration file is located in
``$HOME/.gaiasky/global.properties``. Here are some of the properties
found in this file that are not represented in the GUI.

Graphics properties
-------------------

-  **graphics.render.time** - This property gets a boolean
   (``true``\ \|\ ``false``) and indicates whether a timestamp is to be
   added to screenshots and frames.

.. _data-properties:

Data properties
---------------

-  **data.json.catalog** - This property points to the ``json`` file
   where the catalog(s) to load are defined. The properties
   ``data.json.catalog.*`` contain the default catalogs which are
   shipped with Gaia Sky and are offered in the config dialog.

-  **data.json.objects** - Contains the ``json`` file where the
   definition of all the rest of the data is specified. There are three
   child properties (``data.json.objects.gq.[0..2]``) which contain the
   default graphics quality options.

-  **data.limit.mag** - This contains the limiting magnitude above which
   stars shall not be loaded.

Scene properties
----------------

-  **scene.labelfactor** - A real number in ``[0..n]`` that controls the
   number of star labels to display. The larger the number, the more
   stars will have a label.

-  **scene.star.thresholdangle.quad** - This property contains the view
   angle (in degrees) boundary above which the stars are rendered as
   `quads <https://www.opengl.org/wiki/Primitive#Quads>`__.
   ``Quads`` are basically 4-vertex quadrilaterals, and they can be
   rendered as textures (images) or using ``shaders``. They display more
   detail but are costlier in terms of GPU processing.

-  **scene.star.thresholdangle.point** - This property contains the view
   angle (in degrees) boundary above which the stars are rendered as
   `points <https://www.opengl.org/wiki/Primitive#Point_primitives>`__.
   Points are single pixels, so they are not very resource demanding.

-  **scene.star.thresholdangle.none** - This property contains the view
   angle (in degrees) below which the stars are not rendered at all.
   Usually this is 0 unless you want to cull very distant stars.

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

-  **program.ui.theme** - Specifies the GUI theme. Three themes are
   available: ``bright-blue``, ``dark-orange``, ``dark-green``,
   ``dark-orange-large`` and ``HiDPI``. If you have a HiDPI screen
   (retina, 4K monitor) with a large dots per inch (DPI) number, you
   should use the ``HiDPI`` theme. Since version ``0.704b`` you can also
   choose the theme by using the
   :ref:`User Interface tab <user-interface-config>` in the
   :ref:`Preferences dialog <configuration>`.
