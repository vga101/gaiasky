Running Gaia Sky
****************

If you installed the software using an installer or a package manager
system (``rpm``, ``deb``), you just need to use the standard running
procedures of your Operating System to run the application.

**Windows**

In windows, this means clicking on ``Start`` and then browsing the start
menu folder ``Gaia Sky``. You can run the executable from there.

**Linux**

Just type ``gaiasky`` in a terminal or use your favourite desktop
environment search engine to run the Gaia Sky launcher.

**OS X**

Locate the launcher using the main search.

**Code and pakcage**

However, if you are a maverick and do not like installers, you can also
run the Gaia Sky directly from the source code in ``GitHub`` or
using the ``tgz`` package.

Running from source
===================

Requirements
------------

If you want to compile the source code, you will need the following:

-  `JDK7 or
   above <http://www.oracle.com/technetwork/java/javase/downloads/index.html>`__
   (JDK8 recommended)

Please, be aware that only ``tags`` are guaranteed to work
(`here <https://github.com/langurmonkey/gaiasky/tags>`__). The ``master``
branch holds the development version and the configuration files are
possibly messed up and not ready to work out-of-the-box. So remember to
use a ``tag`` version if you want to run it right away from source.

Also, this guide is for **Unix-like systems only**. If you are working
on Windows, you will need `git for
windows <http://git-scm.com/download/win>`__ and `Power
Shell <http://en.wikipedia.org/wiki/Windows_PowerShell>`__, even though
it has not been tested.

First, clone the repository:

.. code-block:: bash

    $ git clone https://github.com/langurmonkey/gaiasky.git
    $ cd gaiasky

Compile and run
---------------

.. note:: The ``TGAS`` catalog files (Gaia data) are **not** in the repository, so if you want to use ``TGAS`` when running from source you either need to choose ``HYG`` in the Data tab of the configuration dialog or download the ``tar`` file `here <http://wwwstaff.ari.uni-heidelberg.de/gaiasandbox/files/20161206_tgas_gaiasky_1.0.3.tar.gz>`__ and extract it into the folder ``gaiasky/android/assets/octree``.

Please make sure to get the TGAS data files if you want to use them before running. To compile the code and run the desktop version of the application:

.. code-block:: bash

    $ gradlew desktop:run

Package Gaia Sky
----------------

To pack the application into a ``tar.gz`` file:

.. code-block:: bash

    $ gradlew desktop:createTar

In order to produce the desktop installers for the various systems you
need a licensed version of ``Install4j``.

.. code-block:: bash

    $ gradlew desktop:pack

These commands will compile and package the application into a
``gaiasky-[version]`` folder under the ``gaiasky/releases`` folder.

Running from downloaded package
===============================

If you prefer to run the application from the ``tar.gz`` package, follow the instructions below.

Linux
-----

In order to run the application on Linux, open the terminal, uncompress
the archive, give execution permissions to the ``run.sh`` file and then
run it.

.. code-block:: bash

    $ tar zxvf gaiasky-[version].tar.gz
    $ cd gaiasky-[version]/
    $ gaiasky

Windows
-------

In order to run the application on Windows, open a terminal window (type
``cmd`` in the start menu search box) and run the ``run.bat`` file.

.. code-block:: bash

    cd path_to_gaiasky_folder
    gaiasky.bat

OS X
----

To run the application on MacOS systems, follow the instructions in the
`Linux <#linux>`__ section.
