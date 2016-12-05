User Interface
**************

GUI window
==========

The Gaia Sky application has an on-screen user interface designed to be
easy to use. It is divided into five sections, `Time <#time>`__,
`Camera <#camera>`__, `Objects <#objects>`__, `Type
visibility <#type-visibility>`__, `Lighting <#lighting>`__ and `Gaia
scan <#gaia-scan>`__.

Time
----

You can play and pause the simulation using the ``PLAY/PAUSE`` button in
the ``Controls`` window to the left. You can also use ``SPACE`` to play
and pause the time. You can also change time warp, which is expressed as
a factor. Use ``,`` and ``.`` to divide by 2 and double the value of the
time warp.

Camera
------

In the camera options pane on the left you can select the type of
camera. This can also be done by using the ``NUMPAD 0-4`` keys.

There are five camera modes:

* ``Free mode`` -- The camera is not linked to any object and its velocity is exponential with respect to the distance to the origin (Sun).
* ``Focus mode`` -- The camera is linked to a focus object and it rotates and rolls with respect to it.
* ``Gaia scene`` -- Provides an outside view of the Gaia satellite. The camera can not be rotated or translated in this mode.
* ``Spacecraft``-- Take control of a spacecraft and navigate around at will.
* ``Gaia FOV`` -- The camera simulates either of the fields of view of Gaia, or both.

For more information on the camera modes, see the :ref:`camera-modes` section.

Additionally, there are a number of sliders for you to control different
parameters of the camera:

-  **Field of view**: Controls the field of view angle of the camera.
   The bigger it is, the larger the portion of the scene represented.
-  **Camera speed**: Controls the longitudinal speed of the camera.
-  **Rotation speed**: Controls the transversal speed of the camera, how
   fast it rotates around an object.
-  **Turn speed**: Controls the turning speed of the camera.

You can **lock the camera** to the focus when in focus mode. Doing so
links the reference system of the camera to that of the object and thus
it moves with it.

.. hint:: **Lock the camera** so that it stays at the same relative position to the focus object.

Finally, we can also **lock the orientation** of the camera to that of
the focus so that the same transformation matrix is applied to both.

.. hint:: **Lock the orientation** so that the camera also rotates with the focus.

Additionally, we can also enable the **crosshair**, which will mark the
currently focused object.

Objects
-------

There is a list of focus objects that can be selected from the
interface. When an object is selected the camera automatically centers
it in the view and you can rotate around it or zoom in and out. Objects
can also be selected by double-clicking on them directly in the view or
by using the search box provided above the list. You can also invoke a
search dialogue by pressing ``CTRL+F``.

Type visibility
---------------

Most graphical elements can be turned off and on using these toggles.
For example you can remove the stars from the display by clicking on the
``stars`` toggle. The object types available are the following:

-  Stars
-  Planets
-  Moons
-  Satellites, the spacecrafts
-  Asteroids
-  Labels, all the text labels
-  Equatorial grid
-  Ecliptic grid
-  Galactic grid
-  Orbits, the orbit lines
-  Atmospheres, the atmospheres of planets
-  Constellations, the constellation lines
-  Boundaries, the constellation boundaries
-  Milky way
-  Others

By checking the **proper motion vectors** checkbox we can enable the
representation of star proper motions if the currently loaded catalog
provides them. Once proper motions are activated, we can control the
number of displayed proper motions and their length by using the two
sliders that appear.

.. _interface-lighting:

Lighting
--------

Here are a few options to control the lighting of the scene:

-  **Star brightness**: Controls the brightness of stars.
-  **Star size**: Controls the size of point-like stars.
-  **Min. star opacity**: Sets a minimum opacity for the faintest stars.
-  **Ambient light**: Controls the amount of ambient light. This only
   affects the models such as the planets or satellites.
-  **Bloom effect**: Controls the bloom effect.
-  **Brightness**: Controls the brightness of the image.
-  **Contrast**: Controls the contrast of the image.
-  **Motion blur**: Enable or disable the motion blur effect.
-  **Lens flare**: Enable or disable the lens flare.
-  **Star glow**: Enable or disable star glows. If enabled, the stars
   are rendered using a glow texture in a post-processing step. This can
   have a performance hit on some older graphics cards.

Gaia scan
---------

You can also enable the real time computation of Gaia observation. To do
so, tick the ``Enable Gaia scan`` checkbox. Keep in mind that this
computation is done by interpolating the scan path and calculating what
stars fall into Gaia's both fields of view, so if you set the time pace
very high it is going to take a toll on the frames per second. Also, you
can choose to colour the stars observed by Gaia according to the number
of observations, where purple is 1 and red is 75. To do so, tick the
``Colour observed stars`` checkbox. Finally, you can decide to only
display the stars that have been observed by Gaia at least once. To do
so, tick the ``Show only observed stars`` checkbox.

Music
-----

Since version ``0.800b`` Gaia Sky also offers a music player in its
interface. By default it ships with only one *spacey* melody, but you
can add your own by dropping them in the folder ``$HOME/.gaiasky/music``.

.. hint:: Drop your ``mp3``, ``ogg`` or ``wav`` files in the folder ``$HOME/.gaiasky/music`` and these will be available during your Gaia Sky sessions to play.

.. _running-scripts:

Running scripts
===============

In order to run python scripts, click on the ``Run script...`` button at
the bottom of the GUI window. A new window will pop up allowing you to
select the script you want to run. Once you have selected it, the script
will be checked for errors. If no errors were found, you will be
notified in the box below and you'll be able to run the script right
away by clicking on the ``Run`` button. If the script contains errors,
you will be notified in the box below and you will not be able to run
the script until these errors are dealt with.

.. hint:: Add your own scripts to the folder ``$HOME/.gaiasky/scripts`` so that Gaia Sky can find them.

Preferences window
==================

You can launch the preferences window any time during the execution of
the program. To do so, click on the ``Preferences`` button at the bottom
of the GUI window. For a detailed description of the configuration
options refer to the :ref:`Configuration
Instructions <configuration>`.
