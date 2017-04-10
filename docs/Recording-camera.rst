Recording and playing camera paths
**********************************

Gaia Sky offers the possibility to record camera paths out of the
box in real time and later play them. These camera paths go to a
``text`` file in the ``temp`` folder of your system.

Camera path file format
=======================

The format of the file is pretty straightforward. It consists of a
``csv`` file with white spaces as delimiters, each row containing the
**state** of the camera and the **time**. The state of the camera
consists of 9 double-precision floating point numbers, 3 for the
**position** and 3 for the **direction** vector and 3 for the **up**
vector.

The reference system used is explained in the :ref:`reference-system` section. The units are :math:`1*10^{-9} m`.

The format of each row is as follows:

-  ``long`` - Time as defined by the ``getTime()`` function of
   ``java.util.Date`` (`here <https://docs.oracle.com/javase/8/docs/api/java/util/Date.html#getTime-->`__).
-  ``double x3`` - Position of the camera.
-  ``double x3`` - Direction vector of the camera.
-  ``double x3`` - Up vector of the camera.

Recording camera paths
======================

In order to **start recording** the camera path, click on the |rec-icon-gray| ``REC``
button next to the Camera section title in the GUI Controls window. The
``REC`` button will turn red |rec-icon-red|, which indicates the camera is being
recorded.

.. |rec-icon-gray| image:: img/ui/rec-icon-gray.png
.. |rec-icon-red| image:: img/ui/rec-icon-red.png

In order to **stop the recording** and write the file, click again on
the red ``REC`` button. The button will turn grey and a notification
will pop up indicating the location of the camera file. Camera files are
by default saved in the ``$HOME/.gaiasky/camera`` directory.

Playing camera paths
====================

In order to play a camera file, click on the |play-icon| ``PLAY``  icon next to the
``REC`` icon. This will prompt a list of available camera files in the
``$HOME/.gaiasky/camera`` folder.

.. |play-icon| image:: img/ui/play-icon.png

You can also combine the camera file playback with the frame output system to
save each frame to a ``JPEG`` image during playback. To do so, enable the **Activate frame output automatically**
checkbox in the preferences dialog as described in the :ref:`camera-recording-config` section.
