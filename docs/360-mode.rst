360 mode
********

Gaia Sky includes a 360 mode where the scene is rendered in all directions to a `cube map <https://en.wikipedia.org/wiki/Cube_mapping>`__.
This cube map is then projected onto a flat image using an `equirectangular projection <http://alexcpeterson.com/2015/08/25/converting-a-cube-map-to-a-sphericalequirectangular-map/>`__. The final image can be used
to create 360 videos with head tracking (see `here <https://www.youtube.com/watch?v=Bvsb8LZwkgc&t=33s>`__)

.. hint:: To activate the 360 mode, click on the |cubemap-icon| icon in the camera section of the controls window. Exit by clicking |cubemap-icon| again.

.. hint:: ``L-CTRL`` + ``3`` -- Can also be used to toggle the 360 mode.

Configuration
=============

Please, see the :ref:`360-mode-config` section.

Creating panorama images
========================

In order to create panorama images that can be viewed with a VR device or simply a 360 viewer, we need to take into consideration a few points.

*  Panoramas work best if their **aspect ratio is 2:1**, so a resolution of ``5300x2650`` or similar should work. (Refer to the :ref:`screenshots-configuration` section to learn how to take screenshots with an arbitrary resolution).
*  Some services (like Google) have strong constraints on image properties. For instance, they must be at least 14 megapixels and in `jpeg` format. Learn more `here <https://support.google.com/maps/answer/7012050?hl=en&ref_topic=6275604>`__.
*  Some **metadata** needs to be injected into the image file.

Injecting panorama metadata to 360 images
-----------------------------------------

To do so, we can use `ExifTool <http://owl.phy.queensu.ca/~phil/exiftool/>`__ in Linux, MacOS and Windows. To inject the metadata which describes a 360 degrees 4K image (3840x2160) we need to run the following command:

.. code:: bash

  exiftool -UsePanoramaViewer=True -ProjectionType=equirectangular -PoseHeadingDegrees=360.0 -CroppedAreaLeftPixels=0 -FullPanoWidthPixels=3840 -CroppedAreaImageHeightPixels=2160 -FullPanoHeightPixels=2160 -CroppedAreaImageWidthPixels=3840 -CroppedAreaTopPixels=0 -LargestValidInteriorRectLeft=0 -LargestValidInteriorRectTop=0 -LargestValidInteriorRectWidth=3840 -LargestValidInteriorRectHeight=2160 image_name.jpg 

Now we can enjoy our image in any 360 panorama viewer like Google Street View app or the Cardboard Camera!
Find some examples in this `album <https://goo.gl/photos/kn2MvugZHYcr5Fty8>`__.

.. figure:: img/screenshots/360/20161111_screenshot_00003.jpg
  :width: 100%

  Panorama image captured with Gaia Sky


Creating spherical (360) videos
===============================

First, you need to capture the 360 video. To do so, capture the images and use ``ffmpeg`` to encode them or capture the video directly using a screen recorder. See the :ref:`capture-videos` section for more information.
Once you have the ``.mp4`` video file, you must use the `spatial media <https://github.com/google/spatial-media>`__ project to inject the spherical metadata so that video players that support it can play it correctly.

First, clone the project.

.. code:: bash

  git clone https://github.com/google/spatial-media.git
  cd spatial-media/
  
Then, launch the GUI utility with the following command. Python 2.7 must be used to run the tool, so make sure to use that version.

.. code:: bash

  cd spatialmedia
  python2 gui.py
  
Finally, click on ``Open``, select your video file, select the ``My video is spherical (360)`` checkbox and click on ``Inject metadata``. You are done, your video can now be viewed using any 360 video player or even uploaded to `YouTube <https://youtube.com>`__.


.. |cubemap-icon| image:: img/ui/cubemap-icon.png
  