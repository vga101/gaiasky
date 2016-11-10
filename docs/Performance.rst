Performance
***********

The performance of the application may vary significantly depending on
the characteristics of your system. This chapter describes what are the
factors that have an impact in a greater or lesser degree in the
performance of the Gaia Sky and explains how to tweak them. It is
organised in two parts, namely GPU performance (graphics performance)
and CPU performance.

Graphics performance
====================

Refer to the :ref:`graphics-performance` chapter.


CPU performance
===============

The CPU also plays an obvious role in updating the scene state
(positions, orientations, etc.), managing the input and events,
executing the scripts and calling and running the rendering subsystem,
which streams all the texturing and geometrical information to the GPU
for rendering. This section describes what are the elements that can
cause a major impact in CPU performance and explains how to tune them.

Limiting magnitude
------------------

You can modify the magnitude limit by setting the property `data.limit.mag`
in the configuration file. This will prevent the loading of stars whose magnitude
is higher (they are fainter) than the specified magnitude, thus relieving the
CPU of some processing. Also, take a look at the
[data configuration](Configuration-files#data-properties) section.

.. _levels-of-detail:

Levels of Detail (LOD)
----------------------

These settings apply only when using a catalog with levels of detail
like ``TGAS``. We can configure whether we want smooth transitions between
the levels (fade-outs and fade-ins) and also the draw distance, which is
represented by a range slider. The left knob represents the view angle
above which octants are rendered.

The right knob only matters if ``Smooth LOD transitions`` is checked and sets a higher boundary for the
angle for the fade-out and fade-in of octant particles.

*  Set the knobs to the **right** to lower the draw distance and increase performance.
*  Set the knobs to the **left** to higher the draw distance at the expense of performance.

.. figure:: img/lodoctree.png
  :alt: Octree and levels of detail

  Octree and levels of detail. Image: `Wikipedia <https://en.wikipedia.org/wiki/Octree>`__.
