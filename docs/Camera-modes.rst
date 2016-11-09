.. _camera-modes:

Camera modes
************

Gaia Sky offers five basic camera modes.

Focus mode
==========

This is the default mode. In this mode the camera movement is locked to a focus object, which can be selected by double clicking or by using the find dialog (``CTRL_LEFT`` + ``F``).

*  :ref:`Mouse controls in focus mode <mouse-focus-mode>`
*  :ref:`Gamepad controls in focus mode <gamepad-focus-mode>`

.. hint:: ``NUMPAD_0`` -- Enter the Focus mode

Free mode
=========

This mode does not lock the camera to a focus object but it lets it roam free in space.

*  :ref:`Mouse controls in free mode <mouse-free-mode>`
*  :ref:`Gamepad controls in free mode <gamepad-free-mode>`

.. hint:: ``NUMPAD_1`` -- Enter the Free mode

Gaia scene mode
===============

In this mode the camera can not be controlled. It provides a view of the Gaia satellite from the outside.

.. hint:: ``NUMPAD_2`` -- Enter the Gaia scene mode

Spacecraft mode
===============

In this mode you take control of a spacecraft. In the spacecraft mode, the ``GUI`` changes completely. The Options window disappears and
a new user interface is shown in its place at the bottm left of the screen.

*  **Attitude indicator** -- It is shown as a ball with the horizon and other marks. It represents the current orientation of the spacecraft with respect to the equatorial system.

  *  |pointer| -- Indicates the direction the spacecraft is currently headed to.
  *  |greencross| -- Indicates direction of the current velocity vector, if any.
  *  |redcross| -- Indicates inverse direction of the current velocity vector, if any.

*  **Engine Power** -- Current power of the engine. It is a multiplier in steps of powers of ten. Low engine power levels allow for Solar System or planetary travel, whereas high engine power levels are suitable for galactic and intergalactic exploration.
*  |stabilise| -- Stabilises the yaw, pitch and roll angles. If rotation is applied during the stabilisation, the stabilisation is cancelled.
*  |stop| -- Stops the spacecraft until its velocity with respect to the Sun is 0. If thrust is applied during the stopping, the stopping is cancelled.
*  |exit| -- Return to the focus mode.


.. |redcross| image:: img/sc/ai-antivel.png
.. |greencross| image:: img/sc/ai-vel.png
.. |pointer| image:: img/sc/ai-pointer.png
.. |stabilise| image:: img/sc/icon_stabilise.jpg
.. |stop| image:: img/sc/icon_stop.jpg
.. |exit| image:: img/sc/icon_exit.jpg

*  :ref:`Gamepad controls in spacecraft mode <gamepad-focus-mode>`

.. hint:: ```NUMPAD_3`` -- Enter the Spacecraft mode

.. figure:: img/sc/sc-mode.jpg
  :alt: Spacecraft mode, with the various controls at the bottom left.
  :width: 100%

  Spacecraft mode, with the various controls at the bottom left.


Field of View mode
==================

This mode simulates the Gaia fields of view. You can select FoV1, FoV2 or both.

.. hint:: ``NUMPAD_4`` -- Enter Field of View 1 mode

          ``NUMPAD_5`` -- Enter Field of View 2 mode

          ``NUMPAD_6`` -- Enter Field of View 1 and 2 mode
