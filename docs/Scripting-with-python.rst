Scripting
*********

Gaia Sky offers the possibility to run ``Python`` scripts in the same
``JVM`` using `Jython <http://www.jython.org/>`__.

You can find some example scripts in the
`scripts <http://github.com/langurmonkey/gaiasky/tree/master/android/assets/scripts>`__
folder of the project.

.. hint:: Add your own scripts to the folder ``$HOME/.gaiasky/scripts`` so that Gaia Sky can run them.

An interface is provided in order to encapsulate some complex-behaviour
functions and to make scripting easier. This scripting interface is
described in the following section.

The scripting interface
=======================

The scripting interface is located in the package ``gaia.cu9.ari.gaiaorbit.script`` (see `here <http://langurmonkey.github.io/gaiasky/javadoc/gaia/cu9/ari/gaiaorbit/script/package-summary.html>`__).

Description of the functions
----------------------------

Before starting, have a look at the
`documentation of the scripting interface <http://langurmonkey.github.io/gaiasky/javadoc/gaia/cu9/ari/gaiaorbit/script/IScriptingInterface.html>`__,
which provides extensive descriptions of each function.

Using the scripting interface
-----------------------------

In order to import the scripting interface package in your script, you
just need to import the default implementation
``EventScriptingInterface`` from the package ``gaia.cu9.ari.gaiaorbit.script``:

.. code:: python

    # Import scripting interface
    from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface

Then, we need to create the scripting object before start using it.

.. code:: python

    gs = EventScriptingInterface()

Now, we can start executing functions.

.. code:: python

    # Disable input
    gs.disableInput()
    gs.cameraStop()
    gs.minimizeInterfaceWindow()

    # Welcome
    gs.setHeadlineMessage("Welcome to the Gaia Sky")
    gs.setSubheadMessage("Explore Gaia, the Solar System and the whole Galaxy!")
    [...]

More examples
-------------

You can find more examples by looking at the ``scripts``
`folder <http://github.com/langurmonkey/gaiasky/tree/master/android/assets/scripts>`__ in the
Gaia Sky package.

How to run scripts
------------------

Each script is executed in its own thread in the virtual machine, and
runs alongside Gaia Sky. In order to run a script, follow the
procedure described in the :ref:`running-scripts` section.
