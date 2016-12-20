Capturing videos
****************

In order to capture videos there are at least two options which differ
*significantly*.

Frame output system + ``ffmpeg``
================================

This method consists on outputting an image to a file every frame using
the :ref:`frame-output-config` of
Gaia Sky to later gather them to create a video using a video encoder
software such as `ffmpeg <https://ffmpeg.org/>`__, which works on
Windows, Linux and OS X.

.. note:: Use ``F6`` to activate the frame output mode and start saving each frame as an image. Use ``F6`` again to deactivate it.

When the frame output system is active, each frame is saved as a ``JPEG`` image to disk. Refer to the
:ref:`frame-output-config` section to learn how to configure the frame output system.

Once you have the image frames you can convert them
into a video using the following command:

.. code:: bash

    $ ffmpeg -start_number [start_img_num] -i [prefix]%05d.png -vframes [num_images] -s 1280x720 -c:v libx264 -r 25 -preset [slower|veryslow|placebo] -pix_fmt + [out_video_filename].mp4

You need to obviously change the prefix and start number, choose the
right resolution, frame rate and preset and modify the output format if
you need to.

OpenGL context recorder
=======================

There are several available options to record OpenGL rendering to
videos, in all systems. Below are some of these listed. These methods,
however, will only record the OpenGL scene as it is displayed in the
screen and are limited to its resolution.

Linux
-----

-  `glc <https://github.com/nullkey/glc>`__/`glcs <https://github.com/lano1106/glcs>`__
   - Command-line interface applications. The documentation and user
   guides can be found in this
   `wiki <https://github.com/nullkey/glc/wiki>`__.
-  `Gamecaster <https://launchpad.net/gamecaster>`__ - Front end to
   ``glc``.
-  `Soul Capture <https://piga.orain.org/wiki/Soul_Capture>`__ - Front
   end to ``glc``.

Windows
-------

-  `FRAPS <http://www.fraps.com/>`__ - 3rd party Direct3D and OpenGL
   recording software.
-  `NVIDIA
   Shadowplay <http://www.geforce.com/geforce-experience/shadowplay>`__
   - Only for Geforce cards.
