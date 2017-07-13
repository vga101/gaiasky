.. _requirements-installation:

Requirements and Installation
*****************************

In the sections below is the information on the minimum hardware
requirements and on how to install the software.

System requirements
===================

Here are the minimum requirements to run this software:

+------------------------+----------------------------------------------------------------------------------+
| **Operating system**   | Windows 7+ / macOS X / Linux                                                     |
+========================+==================================================================================+
| **CPU**                | Intel Core i3 3rd Generation or similar                                          |
+------------------------+----------------------------------------------------------------------------------+
| **GPU**                | OpenGL 3.0 support / Intel HD 4000 / Nvidia GeForce 8400 GS, 512 MB GRAM         |
+------------------------+----------------------------------------------------------------------------------+
| **Memory**             | 3 GB RAM                                                                         |
+------------------------+----------------------------------------------------------------------------------+
| **Hard drive**         | 400 MB of free disk space                                                        |
+------------------------+----------------------------------------------------------------------------------+
| **Java**               | On Linux, you need the Java Runtime Environment 8+ installed (openJRE is fine)   |
+------------------------+----------------------------------------------------------------------------------+

Installation and uninstallation
===============================

Depending on your system and your personal preferences the installation
procedure may vary. Below is a description of the various installation
methods available.

*  `Download Gaia Sky <https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/>`__

Windows
-------

Two windows installers are available for 32 and 64-bit systems.

-  ``gaiasky_windows_<version>.exe`` -- 32 bit installer.
-  ``gaiasky_windows-x64_<version>.exe`` -- 64 bit installer.

To install the Gaia Sky, just double click on the installer and then
follow the on-screen instructions. You will need to choose the directory
where the application is to be installed.

In order to **uninstall** the application you can use the Windows
Control Panel or you can use the provided uninstaller in the Gaia Sky
folder.

Linux
-----

We provide 3 packages for linux systems (``deb`` for **Debian**, **Ubuntu**
and derivatives, ``rpm`` for **RedHat**, **Fedora** and derivatives and a
linux installer which works on all distros) plus an ``AUR``
`package <https://aur.archlinux.org/packages/gaiasky/>`__ for *Arch* and
derivatives.

**deb**

This is the package for Debian-based distros (**Debian**, **Ubuntu**,
**Mint**, **SteamOS**, etc.).
`Download <https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/>`__
the ``gaiasky_<version>.deb`` file and run the following command. You
will need root privileges to install a ``deb`` package in your system.

.. code-block:: bash

    $ dpkg -i gaiasky_<version>.deb

This will install the application in the ``/opt/gaiasky/`` folder and it
will create the necessary shortcuts and ``.desktop`` files. The package
depends on the ``default-jre`` package, which will be installed if it is
not yet there.

In order to **uninstall**, just type:

.. code-block:: bash

    $ apt-get remove gaiasky

**rpm**

This is the package for RPM-based distributions (**Red Hat**, **Fedora**,
**Mandriva**, **SUSE**, **CentOS**, etc.)
`Download <https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/>`__
the ``gaiasky_linux_<version>.rpm`` file and run the following command.
You will need root privileges to install a ``rpm`` package in your
system.

.. code-block:: bash

    $ yum install gaiasky_linux_<version>.rpm

This will install the application in the ``/opt/gaiasky/`` folder and it
will create the necessary shortcuts.

In order to **uninstall**, just type:

.. code-block:: bash

    $ yum remove gaiasky-x86

**Install from AUR**

If you have **Arch**, **Manjaro**, **Antergos** or any other Arch Linux
derivative, you can install the `package from
AUR <https://aur.archlinux.org/packages/gaiasky/>`__ using any tool able
to install AUR software. For example:

.. code-block:: bash

    $ yauort -S gaiasky

This will download the package and install it in the ``/opt/gaiasky/`` folder. It also
creates the necessary shortcuts.

**Unix/Linux installer**

We also provide a `Unix/Linux
installer <https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/>`__
that will trigger a graphical interface where you can choose the
installation location and some other settings. Download the file
``gaiasky_unix_<version>.sh`` to your disk. Then run the following to
start the installation.

.. code-block:: bash

    $ ./gaiasky_unix_[version].sh

Follow the on-screen instructions to proceed with the installation.

In order to **uninstall**, just execute the ``uninstall`` file in the
installation folder.

macOS X
-------

For macOS we provide a ``gaiasky_macos_<version>.dmg`` file
`here <https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/>`__,
which is installed by unpacking into the Applications folder. Once
unpacked, it is ready to run by simply clicking on it.

Compressed (TGZ) package
------------------------

A ``gaiasky-[version].tgz`` package file is also provided
`here <https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/>`__.
It will work in all systems but you need to unpack it yourself and
create the desired shortcuts. In **Windows**, use an archiver software
(7zip, iZArc, etc.) to unpack it.

In **Linux** and **OS X**, you can use:

.. code-block:: bash

    $ tar zxvf gaiasky-<version>.tgz

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
windows <http://git-scm.com/download/win>`__ and `Power
Shell <http://en.wikipedia.org/wiki/Windows_PowerShell>`__, even though
it has not been tested.

The TGAS catalog files (Gaia data) are **not** in the repository, so if you want to use TGAS when running
from source you need to download
the ``tar`` file corresponding to your version:

-  `tgas single file (version 1.0.3 or older) <http://wwwstaff.ari.uni-heidelberg.de/gaiasandbox/files/20161206_tgas_gaiasky_1.0.3.tar.gz>`__ 
-  `tags single file (version 1.0.4) <http://wwwstaff.ari.uni-heidelberg.de/gaiasandbox/files/20161206_tgas_gaiasky_1.0.4.tar.gz>`__ 
-  `tags gpu (version 1.5.0 or newer) <http://wwwstaff.ari.uni-heidelberg.de/gaiasandbox/files/20170713_tgas_gaiasky_1.5.0.tar.gz>`__ 

You must extract the contents tarball into the folder ``gaiasky/android/assets/data/octree`` (versions ``1.0.3`` and ``1.0.4``) or in the folder ``gaiasky/android/assets/data/catalog`` (version ``1.5.0``).


First, clone the repository:

.. code-block:: bash

    $ git clone https://github.com/langurmonkey/gaiasky.git
    $ cd gaiasky

Compiling and running
---------------------

To compile the code and run the desktop version of the application:

.. code-block:: bash

    $ gradlew desktop:run

Packaging Gaia Sky
------------------

To pack the application into a ``tar`` file:

.. code-block:: bash

    $ gradlew desktop:createTar

In order to produce the desktop installers for the various systems you
need a licensed version of ``Install4j``.

.. code-block:: bash

    $ gradlew desktop:pack

These commands will compile and package the application into a
``gaiasky-[version]`` folder under the ``gaiasky/releases`` folder.
