.. _capture-videos:

Capturing videos
****************

In order to capture videos there are at least two options which differ
*significantly*.

Frame output system + ``ffmpeg``
================================

The frame output system enables automatic saving of every frame
to an image file to disk with an arbitrary resolution and a user-defined
frame rate. The image files can later be encoded into a video using
video encoder software such as `ffmpeg <https://ffmpeg.org/>`__.

.. note:: Use ``F6`` to activate the frame output mode and start saving each frame as an image. Use ``F6`` again to deactivate it. When the frame output mode is active, the icon |frameoutput| is displayed at the top-right corner of the screen.

When the frame output system is active, each frame is saved as a ``JPG`` or ``PNG`` image to disk. Refer to the
:ref:`frame-output-config` section to learn how to configure the frame output system.

Once you have the image frames you can encode a video using a ``ffmpeg`` preset (slow, veryslow, fast, etc.) with the following command:

.. code:: bash

    $ ffmpeg -framerate 60 -start_number [start_img_num] -i [prefix]%05d.jpg -vframes [num_images] -s 1280x720 -c:v libx264 -preset [slower|veryslow|placebo] -r 60  [out_video_filename].mp4

Please note that if you don't want scaling, the ``--framerate`` input framerate, ``-r`` output framerate and ``-s`` resolution settings must match the settings defined in the frame output system preferences in Gaia Sky.
You can also use a constant rate factor ``-crf`` setting:

.. code:: bash

	$ ffmpeg -framerate 60 -start_number [start_img_num] -i [prefix]%05d.jpg  -vframes [num_images] -s 1280x720 -c:v libx264 -pix_fmt yuv420p -crf 23 -r 60  [out_video_filename].mp4

You need to obviously change the prefix and start number, if any, choose the
right resolution, frame rate and preset and modify the output format if
you need to. 

``ffmpeg`` is quite a complex command which provides a lot of options, so for more information please refer
to the official `ffmpeg documentation <http://ffmpeg.org/documentation.html>`__. 
Also, `here <https://en.wikibooks.org/wiki/FFMPEG_An_Intermediate_Guide/image_sequence>`__ is a good resource on 
encoding videos from image sequences with ``ffmpeg``.

OpenGL/Screen recorders
=======================

There are several available options to record the screen or OpenGL 
context, in all systems. Below are some of these listed. These methods,
however, will only record the scene as it is displayed in the
screen and are limited to its window resolution.

Linux
-----

-  `OBS Studio <https://obsproject.com/>`__ - Amazing open source streaming solution.
-  `glc <https://github.com/nullkey/glc>`__/`glcs <https://github.com/lano1106/glcs>`__
   - Command-line interface applications. The documentation and user
   guides can be found in this `wiki <https://github.com/nullkey/glc/wiki>`__.
-  `Simple Screen Recorder <http://www.maartenbaert.be/simplescreenrecorder/>`__ - The name says it all.
-  `Gamecaster <https://launchpad.net/gamecaster>`__ - Front end to
   ``glc``.
-  `Soul Capture <https://piga.orain.org/wiki/Soul_Capture>`__ - Front
   end to ``glc``.

Windows
-------

-  `OBS Studio <https://obsproject.com/>`__ - Amazing open source streaming solution.
-  `FRAPS <http://www.fraps.com/>`__ - 3rd party Direct3D and OpenGL
   recording software.
-  `NVIDIA
   Shadowplay <http://www.geforce.com/geforce-experience/shadowplay>`__
   - Only for Geforce cards.
   

.. |frameoutput| image:: img/ui/frameoutput.png
