Scripting
*********

Gaia Sky offers the possibility to run ``Python`` scripts in the same
``JVM`` using `Jython <http://www.jython.org/>`__.

You can find some example scripts in the
`scripts <http://github.com/langurmonkey/gaiasky/tree/master/assets/scripts>`__
folder of the project.

.. hint:: Add your own scripts to the folder ``$HOME/.gaiasky/scripts`` so that Gaia Sky can run them.

An interface is provided in order to encapsulate some complex-behaviour
functions and to make scripting easier. This scripting interface is
described in the following section.

Important notes on the scripting engine
=======================================

The scripting engine spawns a new thread for each script. Even though the thread is given maximum priority, this system is 
non-deterministic by nature, so no one can guarantee when the thread will run and when the API calls will be issued. Simply put, 
the scripts are not run in sync with the main loop. Instead, API calls are (for the most part) queued up and run after the
current loop cycle, which updates the model and renders the frame. This prevents leaving the model in an inconsistent state by 
updating it from two threads at the same time.


The scripting API
=================

The scripting API is a set of methods which can be called to interact with Gaia Sky. The available methods differ depending on the version of Gaia Sky.

API documentation
-----------------

The only up-to-date API documentation for each version is in the interface header files themselves. Below is a list of links to the different APIs.

- `API Gaia Sky master (development branch) <https://github.com/langurmonkey/gaiasky/blob/master/core/src/gaia/cu9/ari/gaiaorbit/script/IScriptingInterface.java>`__
- `API Gaia Sky 2.0.0 <https://github.com/langurmonkey/gaiasky/blob/2.0.0/core/src/gaia/cu9/ari/gaiaorbit/script/IScriptingInterface.java>`__
- `API Gaia Sky 1.5.0 <https://github.com/langurmonkey/gaiasky/blob/1.5.0/core/src/gaia/cu9/ari/gaiaorbit/script/IScriptingInterface.java>`__
- `API Gaia Sky 1.0.4 <https://github.com/langurmonkey/gaiasky/blob/1.0.4/core/src/gaia/cu9/ari/gaiaorbit/script/IScriptingInterface.java>`__
- `API Gaia Sky 1.0.3 <https://github.com/langurmonkey/gaiasky/blob/1.0.3/core/src/gaia/cu9/ari/gaiaorbit/script/IScriptingInterface.java>`__
- `API Gaia Sky 1.0.2 <https://github.com/langurmonkey/gaiasky/blob/1.0.2/core/src/gaia/cu9/ari/gaiaorbit/script/IScriptingInterface.java>`__
- `API Gaia Sky 1.0.1 <https://github.com/langurmonkey/gaiasky/blob/1.0.1/core/src/gaia/cu9/ari/gaiaorbit/script/IScriptingInterface.java>`__
- `API Gaia Sky 1.0.0 <https://github.com/langurmonkey/gaiasky/blob/1.0.0/core/src/gaia/cu9/ari/gaiaorbit/script/IScriptingInterface.java>`__


Using the scripting API
-----------------------

In order to import the scripting interface package in your script, you
just need to import the default implementation
``EventScriptingInterface`` from the package ``gaia.cu9.ari.gaiaorbit.script``:

.. code:: python

    # Import scripting interface
    from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface

Then, we need to get the scripting interface instance before start using it.

.. code:: python

    gs = EventScriptingInterface.instance()

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
`folder <http://github.com/langurmonkey/gaiasky/tree/master/assets/scripts>`__ in the
Gaia Sky package.

How to run scripts
------------------

Each script is executed in its own thread in the virtual machine, and
runs alongside Gaia Sky. In order to run a script, follow the
procedure described in the :ref:`running-scripts` section.
