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

**macOS X**

Locate the launcher in your install directory (usually ``/Applications``) and double click on it.

**Code and pakcage**

However, if you are a maverick and do not like installers, you can also
run the Gaia Sky directly from the source code in ``GitHub`` or
using the ``tgz`` package.


.. _running-from-source:

Running from source
===================

Requirements
------------

If you want to compile the source code, you will need the following:

-  `JDK8 or
   above <http://www.oracle.com/technetwork/java/javase/downloads/index.html>`__

Please, be aware that only ``tags`` are guaranteed to work
(`here <https://github.com/langurmonkey/gaiasky/tags>`__). The ``master``
branch holds the development version and the configuration files are
possibly messed up and not ready to work out-of-the-box. So remember to
use a ``tag`` version if you want to run it right away from source.

Also, this guide is for **Unix-like systems only**. If you are working
on Windows, you will need `git for
windows <http://git-scm.com/download/win>`__, which contains a version of
MinGW (bash) packed with ``git``, ``vim`` and some other utils. All other
parts of the process should work the same under Windows systems.

First, clone the repository:

.. code-block:: bash

    $  git clone https://github.com/langurmonkey/gaiasky.git
    $  cd gaiasky

Getting the catalog data
------------------------

The TGAS catalog files (Gaia data) are **not** in the repository, so if
you want to use TGAS when running from source you need to download the
``tar`` file corresponding to your version --- see table below.

As of version ``1.5.0``, there are new GPU-bound catalogs which perform
much better and can also be combined with the levels-of-detail structure
to produce a good combo in terms of performance and load times. Choose
which catalog you want to use. Usually, the single file GPU version
should work fine (tgas GPU), and has no culling, so all particles are
visible at all times.

+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------------------------------------------------------------------+----------------------------------------------+--------------------------------------+
| **Catalog**                                                                                                                                                                                                                         | **Description**                                                                                    | **Location**                                 | **Catalog file**                     |
+=====================================================================================================================================================================================================================================+====================================================================================================+==============================================+======================================+
| `tgas LoD (1.0.3) <http://gaia.ari.uni-heidelberg.de/gaiasky/files/catalogs/tgas/20161206_tgas_gaiasky_1.0.3.tar.gz>`__                                                                                                                   | Levels of detail (lod) TGAS catalog. CPU-bound.                                                    | ``gaiasky/assets/data/octree``               | x                                    |
+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------------------------------------------------------------------+----------------------------------------------+--------------------------------------+
| `tags LoD (1.0.4) <http://gaia.ari.uni-heidelberg.de/gaiasky/files/catalogs/tgas/20161206_tgas_gaiasky_1.0.4.tar.gz>`__                                                                                                                   | Levels of detail (lod) TGAS catalog. CPU-bound.                                                    | ``gaiasky/assets/data/octree``               | x                                    |
+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------------------------------------------------------------------+----------------------------------------------+--------------------------------------+
| tags LoD (`1.5.0 <http://gaia.ari.uni-heidelberg.de/gaiasky/files/catalogs/tgas/20170731_tgas_lod_gaiasky_1.5.0.tar.gz>`__, `1.5.1 <http://gaia.ari.uni-heidelberg.de/gaiasky/files/catalogs/tgas/20180416_tgas_lod_gaiasky_1.5.1.tar.gz>`__)   | Levels of detail (lod) TGAS catalog. GPU-bound. Version ``1.5.1`` contains a pm fix and RAVE rv.   | ``gaiasky/assets/data/octree/tgas``          | ``data/catalog-tgas-hyg-lod.json``   |
+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------------------------------------------------------------------+----------------------------------------------+--------------------------------------+
| tags GPU (`1.5.0 <http://gaia.ari.uni-heidelberg.de/gaiasky/files/catalogs/tgas/20170731_tgas_gpu_gaiasky_1.5.0.tar.gz>`__, `1.5.1 <http://gaia.ari.uni-heidelberg.de/gaiasky/files/catalogs/tgas/20180416_tgas_gpu_gaiasky_1.5.1.tar.gz>`__)   | TGAS catalog, GPU-bound. Version ``1.5.1`` contains a pm fix and RAVE rv.                          | ``gaiasky/assets/data/catalog``              | ``data/catalog-tgas-hyg.json``       |
+-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------+----------------------------------------------------------------------------------------------------+----------------------------------------------+--------------------------------------+

For versions ``1.0.x`` just extract the package in the specified
location. For versions ``1.5.0+`` you can choose whether you want to use
the Levels of detail catalog (LoD, multiple files, uses an octree structure
which culls particles outside the view frustum and hides particles which
are far away according to the view distance setting) or the regular
catalog (single file, loaded once at startup, contains the full catalog,
which is sent to GPU memory). Then, you need to point the key
``data.json.catalog`` in your ``$HOME/.gaiasky/global.properties`` file
to the file specified in the last column in the table.

Albeit **not recommended** for performance reasons, the legacy
particle-based (CPU-bound) version of the catalog (version ``1.0.4``)
can still be used with newer versions. To do so, extract the package in
``gaiasky/assets/data/octree/tgas`` so that the ``metadata.bin``
file and the ``particles`` folder are directly within that folder and
edit the configuration file so that ``data.json.catalog`` points to
``data/catalog-tgas-hyg-lod-old.json``.

Compiling and running
---------------------

To compile the code and run Gaia Sky run the following.

.. code-block:: bash

    $  gradlew core:run
    
In order to pull the latest changes from the GitHub repository:

.. code-block:: bash

	$  git pull
	
Remember that the master branch is the development branch and therefore intrinsically unstable. It is not guaranteed to always work.


Packaging Gaia Sky
-----------------

Gaia Sky can be exported to a folder to be run as a standalone app with the following.

.. code-block:: bash

	$ gradlew core:dist
	
That will create a new folder called ``releases/gaiasky-[version].[revison]`` with the exported application. Run scripts
are provided with the name ``gaiasky`` (Linux, macOS) and ``gaiasky.cmd`` (Windows).

Also, to export Gaia Sky into a ``tar.gz`` archive file, run the following.

.. code-block:: bash

    $  gradlew core:createTar

In order to produce the desktop installers for the various systems you
need a licensed version of ``Install4j``. Then, you need to run:

.. code-block:: bash

    $  gradlew core:pack

These command will produce the different OS packages (``.exe``, ``.dmg``, ``.deb``, ``.rpm``, etc.) 
of Gaia Sky into ``releases/packages-[version].[revision]`` folder.

Running from downloaded package
===============================

If you prefer to run the application from the ``tar.gz`` package, follow the instructions below.

Linux
-----

In order to run the application on Linux, open the terminal, uncompress
the archive, give execution permissions to the ``gaiasky`` script and then
run it.

.. code-block:: bash

    $  tar zxvf gaiasky-[version].tar.gz
    $  cd gaiasky-[version]/
    $  gaiasky

Windows
-------

In order to run the application on Windows, open a terminal window (type
``cmd`` in the start menu search box) and run the ``gaiasky.cmd`` file.

.. code-block:: bash

    $  cd path_to_gaiasky_folder
    $  gaiasky.cmd

macOS X
-------

To run the application on macOS, follow the instructions in the
`Linux <#linux>`__ section.
