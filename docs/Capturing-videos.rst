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

Once you have the image frames you can encode a video using a ``ffmpeg`` preset (slow, veryslow, fast, etc.) with the following command:

.. code:: bash

    $ ffmpeg --framerate 60 -start_number [start_img_num] -i [prefix]%05d.jpg -vframes [num_images] -s 1280x720 -c:v libx264 -preset [slower|veryslow|placebo] [out_video_filename].mp4

Please note that if you don't want scaling, the ``--framerate`` and ``-s`` resolution settings must match the settings defined in the frame output system preferences in Gaia Sky.
You can also use a constant rate factor ``-crf`` setting:

.. code:: bash

	$ ffmpeg -framerate 60 -start_number [start_img_num] -i [prefix]%05d.jpg  -vframes [num_images] -s 1280x720 -c:v libx264 -pix_fmt yuv420p -crf 23 [out_video_filename].mp4

You need to obviously change the prefix and start number, if any, choose the
right resolution, frame rate and preset and modify the output format if
you need to. 

``ffmpeg`` is quite a complex command which provides a lot of options, so for more information please refer
to the official `ffmpeg documentation <http://ffmpeg.org/ffmpeg.html>`__.

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
