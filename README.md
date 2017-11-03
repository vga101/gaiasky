![Gaia Sky](https://zah.uni-heidelberg.de/fileadmin/user_upload/gaia/gaiasky/img/GaiaSkyBanner.jpg)
--------------------------

[![Documentation Status](https://readthedocs.org/projects/gaia-sky/badge/?version=latest)](http://gaia-sky.readthedocs.io/en/latest/?badge=latest)
[![Circle CI](https://circleci.com/gh/langurmonkey/gaiasky.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/langurmonkey/gaiasky/tree/master)
[![License: MPL 2.0](https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg)](https://opensource.org/licenses/MPL-2.0)

[**Gaia Sky**](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky) is a real-time, 3D, astronomy visualisation software that
runs on Windows, Linux and MacOS. It is developed in the framework of
[ESA](http://www.esa.int/ESA)'s [Gaia mission](http://sci.esa.int/gaia) to chart about 1 billion stars of our Galaxy.
To get the latest up-to-date and most complete information,

*  Visit our [**home page**](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky)
*  Read the [**Documentation**](http://gaia-sky.readthedocs.io)
*  Submit a [**bug** or a **feature request**](https://github.com/langurmonkey/gaiasky/issues)
*  Follow development news at [@GaiaSkyDev](https://twitter.com/GaiaSkyDev)

This file contains the following sections:

1. Installation instructions and requirements
2. Configuration instructions
3. Running instructions
4. Copyright and licensing information
5. Contact information
6. Credits and acknowledgements

##  1. Installation instructions and requirements

### 1.1 Requirements

| **Operating system**  | Windows 7+ / MacOS X / Linux |
| :---: | :--- |
| **CPU** | Intel Core i3 3rd Generation or similar  |
| **GPU** | OpenGL 3.0 support / Intel HD 4000 / Nvidia GeForce 8400 GS, 500 MB GRAM |
| **Memory** | 3 GB RAM |
| **Hard drive**  | 230 MB of free disk space  |
| **Java**  | On Linux, you need the Java Runtime Environment 8+ installed (openJRE is fine) |

### 1.2 Installation and uninstallation

Depending on your system and your personal preferences the installation
procedure may vary. Below is a description of the various installation methods
available. You can download the packages [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/).

#### 1.2.1 Windows

Two windows installers are available for 32 and 64-bit systems [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/).

- `gaiasky_windows_<version>.exe` - 32 bit installer.
- `gaiasky_windows-x64_<version>.exe` - 64 bit installer.

Both versions will automatically install the JRE if it is not present
in the system.
To install Gaia Sky, just double click on the installer and
then follow the on-screen instructions. You will need to choose the
directory where the application is to be installed.

In order to **uninstall** the application you can use the Windows Control Panel or
you can use the provided uninstaller in the Gaia Sky folder.

#### 1.2.2 Linux

We provide 4 packages for linux systems. `deb`, `rpm`, an `aur` package and a linux installer. You can get them [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/).
The `aur` package can be installed using any `AUR` helper.

##### 1.2.2.1 DEB

This is the package for Debian-based distros (Debian, Ubuntu, Mint, SteamOS, etc.).
Download the `gaiasandbox_linux_<version>.deb` file and run the
following command. You will need root privileges to install a `deb` package in
your system.

```
sudo dpkg -i gaiasky_linux_<version>.deb
```

This will install the application in the `/opt/gaiasky/` folder
and it will create the necessary shortcuts.

In order to **uninstall**, just type:

```
sudo apt-get remove gaiasky
```
##### 1.2.2.2 AUR

We also provide an [AUR package](https://aur.archlinux.org/packages/gaiasky/) called `gaiasky`. You can install it easily with any tool capable of accessing `AUR`, for example `yaourt`.

```
yaourt -S gaiasky
```

##### 1.2.2.3 RPM

This is the package for RPM-based distributions (Red Hat, Fedora, Mandriva, SUSE, CentOS, etc.)
Download the `gaiasky_linux_<version>.rpm` file and run the
following command. You will need root privileges to install a `rpm` package in
your system.

```
sudo yum install gaiasky_linux_<version>.rpm
```

This will install the application in the `/opt/gaiasky/` folder
and it will create the necessary shortcuts.

In order to **uninstall**, just type:

```
sudo yum remove gaiasky-x86
```

##### 1.2.2.4 Linux installer

We also provide a Linux installer ([here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/)) which will trigger a graphical interface
where you can choose the installation location and some other settings.
Download the file `gaiasandbox_unix_<version>.sh` to your disk.
Then run the following to start the installation.

```
./gaiasky_unix_[version].sh
```

Follow the on-screen instructions to proceed with the installation.

In order to **uninstall**, just run the `uninstall` file in the
installation folder.

#### 1.2.3 macOS X

For macOS we provide a `gaiasky_macos_<version>.dmg` file
which is installed by unpacking into the Applications folder. Get it [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/). Once unpacked, you can run it by clicking on it.

#### 1.2.4 Compressed (TGZ) package

A `gaiasky-<version>.tgz` package file is also provided. It will work
in all systems but you need to unpack it yourself and create the desired
shortcuts.

In **Windows**, use an archiver software (7zip, iZArc, etc.) to unpack it.

In **Linux** and **macOS**, you can use:

```
tar -zxvf gaiasky-<version>.tgz
```

##  2. Running instructions

### 2.1 Running Gaia Sky

In order to run the program just click on the shortcut
provided in your operating system.

### 2.2 Running from source

First, clone the [GitHub](https://github.com/langurmonkey/gaiasky) repository:

```
git clone https://github.com/langurmonkey/gaiasky.git
cd gaiasky
```

Make sure you have at least `JDK8` installed.

The TGAS catalog files (Gaia data) are **not** in the repository, so if you want to use TGAS when running
from source you need to download
the `tar` file corresponding to your version — see table below.

As of version `1.5.0`, there are new GPU-bound catalogs which perform much better and can also be combined with the levels-of-detail structure to produce a good combo in terms of performance
and load times. Choose which catalog you want to use. Usually, the single file GPU version should work fine (tgas gpu), and has no culling, so all particles are visible at all times.

| **Catalog** | **Description** | **Location** | **Catalog file** |
|---------|-------------|----------|----------|
| [tgas lod (1.0.3)](http://wwwstaff.ari.uni-heidelberg.de/gaiasandbox/files/20161206_tgas_gaiasky_1.0.3.tar.gz)  | Levels of detail (lod) TGAS catalog. CPU-bound. | `gaiasky/android/assets/data/octree` | - |
| [tags lod (1.0.4)](http://wwwstaff.ari.uni-heidelberg.de/gaiasandbox/files/20161206_tgas_gaiasky_1.0.4.tar.gz)  | Levels of detail (lod) TGAS catalog. CPU-bound. | `gaiasky/android/assets/data/octree` | - |
| tags lod ([1.5.0](http://wwwstaff.ari.uni-heidelberg.de/gaiasandbox/files/20170731_tgas_lod_gaiasky_1.5.0.tar.gz), [1.5.1](http://wwwstaff.ari.uni-heidelberg.de/gaiasandbox/files/20171103_tgas_lod_gaiasky_1.5.1.tar.gz))  | Levels of detail (lod) TGAS catalog. GPU-bound. Version `1.5.1` contains a fix in proper motion and RAVE radial velocities.  | `gaiasky/android/assets/data/octree/tgas` | `data/catalog-tgas-hyg-lod.json` |
| tags gpu ([1.5.0](http://wwwstaff.ari.uni-heidelberg.de/gaiasandbox/files/20170731_tgas_gpu_gaiasky_1.5.0.tar.gz), [1.5.1](http://wwwstaff.ari.uni-heidelberg.de/gaiasandbox/files/20171103_tgas_gpu_gaiasky_1.5.1.tar.gz))  | TGAS catalog, GPU-bound. Version `1.5.1` contains a fix in proper motion and RAVE radial velocities.  | `gaiasky/android/assets/data/catalog` | `data/catalog-tgas-hyg.json` | 

For versions `1.0.x` just extract the package in the specified location. For versions `1.5.0+` you can choose whether you want to use the Levels of detail catalog (multiple files, uses an octree structure which culls particles outside the view frustum and hides particles which are far away according
to the view distance setting) or the regular catalog (single file, loaded once at startup, contains the full catalog, which is sent to GPU memory). Then, you need to point the key `data.json.catalog` in your `$HOME/.gaiasky/global.properties` file to the
file specified in the last column in the table.

Albeit **not recommended** for performance reasons, the legacy particle-based (CPU-bound) version of the catalog (version `1.0.4`) can still be used with newer versions. To do so, extract the package in `gaiasky/android/assets/data/octree/tgas` so that the `metadata.bin` file and the `particles` folder are directly within that folder and 
edit the configuration file so that `data.json.catalog` points to `data/catalog-tgas-hyg-lod-old.json`.


Finally, run Gaia Sky with:

```
gradlew desktop:run
```

Et voilà! Gaia Sky is running on your machine.

##  3. Documentation and help

The most up-to-date documentation of Gaia Sky is always in [gaia-sky.readthedocs.io](http://gaia-sky.readthedocs.io).

##  4. Copyright and licensing information

This software is published and distributed under the MPL 2.0
(Mozilla Public License 2.0). You can find the full license
text here https://github.com/langurmonkey/gaiasky/blob/master/LICENSE.md
or visiting https://opensource.org/licenses/MPL-2.0

##  5. Contact information

The main webpage of the project is
**[https://www.zah.uni-heidelberg.de/gaia/outreach/gaiasky](https://www.zah.uni-heidelberg.de/gaia/outreach/gaiasky)**. There you can find
the latest versions and the latest information on Gaia Sky.
