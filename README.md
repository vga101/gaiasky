![Gaia Sky](https://zah.uni-heidelberg.de/fileadmin/user_upload/gaia/gaiasky/img/GaiaSkyBanner.jpg)
--------------------------

[![Documentation Status](https://readthedocs.org/projects/gaia-sky/badge/?version=latest)](http://gaia-sky.readthedocs.io/en/latest/?badge=latest)
[![Circle CI](https://circleci.com/gh/langurmonkey/gaiasky.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/langurmonkey/gaiasky/tree/master)
[![License: MPL 2.0](https://img.shields.io/badge/License-MPL%202.0-brightgreen.svg)](https://opensource.org/licenses/MPL-2.0)
[![GitHub issues](https://img.shields.io/github/issues/langurmonkey/gaiasky.svg)](https://github.com/langurmonkey/gaiasky/issues)
[![GitHub forks](https://img.shields.io/github/forks/langurmonkey/gaiasky.svg)](https://github.com/langurmonkey/gaiasky/network)
[![GitHub tag](https://img.shields.io/github/tag/langurmonkey/gaiasky.svg)](https://github.com/langurmonkey/gaiasky/tags/)

[**Gaia Sky**](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky) is a real-time, 3D, astronomy visualisation software that
runs on Windows, Linux and macOS. It is developed in the framework of
[ESA](http://www.esa.int/ESA)'s [Gaia mission](http://sci.esa.int/gaia) to chart about 1 billion stars of our Galaxy.
To get the latest up-to-date and most complete information,

*  Visit our [**home page**](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky)
*  Read the [**Documentation**](http://gaia-sky.readthedocs.io)
*  Submit a [**bug** or a **feature request**](https://gitlab.com/langurmonkey/gaiasky/issues)
*  Follow development news at [@GaiaSky_Dev](https://twitter.com/GaiaSky_Dev)

This file contains the following sections:

1. [Installation instructions and requirements](#1-installation-instructions-and-requirements)
2. [Running instructions](#2-running-instructions)
3. [Documentation and help](#3-documentation-and-help)
4. [Copyright and licensing information](#4-copyright-and-licensing-information)
5. [Contact information](#5-contact-information)
6. [Credits and acknowledgements](#6-acknowledgements)
7. [Gaia Sky VR](#7-gaia-sky-vr)

##  1. Installation instructions and requirements

### 1.1 Requirements

| **Operating system**  | Linux / Windows 7+ / macOS |
| :---: | :--- |
| **CPU** | Intel Core i5 3rd Generation or similar  |
| **GPU** | Intel HD 4000, Nvidia GeForce 9800 GT, Radeon HD 5670 / 1 GB VRAM / OpenGL 3.0 |
| **Memory** | 4+ GB RAM |
| **Hard drive**  | 1 GB of free disk space  |

### 1.2 Installation and uninstallation

Depending on your system and your personal preferences the installation
procedure may vary. Below is a description of the various installation methods
available. You can download the packages [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/).

#### 1.2.1 Linux

We provide 4 packages for linux systems. `deb`, `rpm`, an `aur` package and a linux installer. You can get them [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/).
The `aur` package can be installed using any `AUR` helper.

##### 1.2.1.1 DEB

This is the package for Debian-based distros (Debian, Ubuntu, Mint, SteamOS, etc.).
Download the `gaiasandbox_linux_<version>.deb` file and run the
following command. You will need root privileges to install a `deb` package in
your system.

```
$  sudo dpkg -i gaiasky_linux_<version>.deb
```

This will install the application in the `/opt/gaiasky/` folder
and it will create the necessary shortcuts.

In order to **uninstall**, just type:

```
$  sudo apt-get remove gaiasky
```
##### 1.2.1.2 AUR

We also provide an [AUR package](https://aur.archlinux.org/packages/gaiasky/) called `gaiasky`. You can install it easily with any tool capable of accessing `AUR`, for example `yaourt`.

```
$  yaourt -S gaiasky
```
 
##### 1.2.1.3 RPM

This is the package for RPM-based distributions (Red Hat, Fedora, Mandriva, SUSE, CentOS, etc.)
Download the `gaiasky_linux_<version>.rpm` file and run the
following command. You will need root privileges to install a `rpm` package in
your system.

```
$  sudo yum install gaiasky_linux_<version>.rpm
```

This will install the application in the `/opt/gaiasky/` folder
and it will create the necessary shortcuts.

In order to **uninstall**, just type:

```
$  sudo yum remove gaiasky-x86
```

##### 1.2.1.4 Linux installer

We also provide a Linux installer ([here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/)) which will trigger a graphical interface
where you can choose the installation location and some other settings.
Download the file `gaiasandbox_unix_<version>.sh` to your disk.
Then run the following to start the installation.

```
$  ./gaiasky_unix_[version].sh
```

Follow the on-screen instructions to proceed with the installation.

In order to **uninstall**, just run the `uninstall` file in the
installation folder.

#### 1.2.2 Windows

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

#### 1.2.3 macOS

For macOS we provide a `gaiasky_macos_<version>.dmg` file
which is installed by double-clicking on it and following the on-screen instructions. Get it [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/). Once unpacked, you can run it by clicking on it.

#### 1.2.4 Compressed (TGZ) package

A `gaiasky-<version>.tgz` package file is also provided. It will work
in all systems but you need to unpack it yourself and create the desired
shortcuts.

In **Windows**, use an archiver software (7zip, iZArc, etc.) to unpack it.
When using the `tgz` package, uou need to install the [JRE8](http://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html) yourself.

In **Linux** and **macOS**, you can use:

```
$  tar -zxvf gaiasky-<version>.tgz
```

##  2. Running instructions

### 2.1 Running Gaia Sky

In order to run the program just click on the shortcut
provided in your operating system.

If Windows is your OS of choice, you first need to install [Git for Windows](https://git-scm.com/download/win).
Also, on Windows you will need to use the `gradlew.bat` script instead of the bash `gradlew`, so
make sure to substitute this in the commands below.

### 2.2 Running from source

First, clone the [GitLab](https://gitlab.com/langurmonkey/gaiasky) repository:

```
$  git clone https://gitlab.com/langurmonkey/gaiasky.git
$  cd gaiasky
```

Make sure you have at least `JDK8` installed.

### 2.3 Getting the data

The catalog files (TGAS or Gaia DR2) are **not** in the repository, so if you want to use these when running
from source you need to download
the `tar` file corresponding to your version -- see table below.

| **Catalog** | **Description** | **Extract location** | **Catalog file** |
|---------|-------------|----------|----------|
| TAGS lod ([1.5.0](http://gaia.ari.uni-heidelberg.de/gaiasky/files/catalogs/tgas/20170731_tgas_lod_gaiasky_1.5.0.tar.gz), [1.5.1](http://gaia.ari.uni-heidelberg.de/gaiasky/files/catalogs/tgas/20180416_tgas_lod_gaiasky_1.5.1.tar.gz))  | Levels of detail (lod) TGAS catalog. 700 K stars. Version `1.5.1` contains a fix in proper motion and RAVE radial velocities.  | `gaiasky/assets/data/octree/tgas` | [`data/catalog-tgas-hyg-lod.json`](http://gaia.ari.uni-heidelberg.de/gaiasky/files/catalogs/tgas/catalog-tgas-hyg-lod.json) |
| TGAS gpu ([1.5.0](http://gaia.ari.uni-heidelberg.de/gaiasky/files/catalogs/tgas/20170731_tgas_gpu_gaiasky_1.5.0.tar.gz), [1.5.1](http://gaia.ari.uni-heidelberg.de/gaiasky/files/catalogs/tgas/20180416_tgas_gpu_gaiasky_1.5.1.tar.gz))  | TGAS catalog. 700 K stars. Version `1.5.1` contains a fix in proper motion and RAVE radial velocities.  | `gaiasky/assets/data/catalog` | [`data/catalog-tgas-hyg.json`](http://gaia.ari.uni-heidelberg.de/gaiasky/files/catalogs/tgas/catalog-tgas-hyg.json), [`data/particles-tgas.json`](http://gaia.ari.uni-heidelberg.de/gaiasky/files/catalogs/tgas/particles-tgas.json) | 
| [Gaia DR2](http://gaia.ari.uni-heidelberg.de/gaiasky/files/catalogs/dr2/20180419/dr2-20-0.5.tar.gz)  | Gaia DR2 catalog, 9.5 M stars.  | `gaiasky/assets/data/octree/dr2` | [`data/catalog-dr2-default.json`](http://gaia.ari.uni-heidelberg.de/gaiasky/files/catalogs/dr2/catalog-dr2-default.json) | 

Find more catalogs to download [here](https://zah.uni-heidelberg.de/institutes/ari/gaia/outreach/gaiasky/downloads/#dr2catalogs).

First, choose the package corresponding to your Gaia Sky version and extract it into the specified **Extract location**. Then, download the catalog file(s) and put them in `gaiasky/assets/data` and you should be good to go. If you want to put the catalog files in another location, you need to update the path in the catalog files.

Then, you need to point the key `data.json.catalog` in your `$HOME/.gaiasky/global.properties` file to the
file specified in the last column in the table (**Catalog file**).

### 2.4 Running

Finally, run Gaia Sky (Linux, macOS) with:

```
$  gradlew core:run
```

On Windows, do:

```
.\gradlew.bat core:run
```

Et voilà ! The bleeding edge Gaia Sky is running in your machine.

In order to pull the latest version from the repository, just run the following from the `gaiasky` folder.

```
$  git pull
```

Remember that the master branch is the development branch and therefore intrinsically unstable. It is not guaranteed to always work.

##  3. Documentation and help

The most up-to-date documentation of Gaia Sky is always in [gaia-sky.readthedocs.io](http://gaia-sky.readthedocs.io).

##  4. Copyright and licensing information

This software is published and distributed under the MPL 2.0
(Mozilla Public License 2.0). You can find the full license
text here https://gitlab.com/langurmonkey/gaiasky/blob/master/LICENSE.md
or visiting https://opensource.org/licenses/MPL-2.0

##  5. Contact information

The main webpage of the project is
**[https://www.zah.uni-heidelberg.de/gaia/outreach/gaiasky](https://www.zah.uni-heidelberg.de/gaia/outreach/gaiasky)**. There you can find
the latest versions and the latest information on Gaia Sky.

##  6. Acknowledgements

The latest acknowledgements are always in the [ACKNOWLEDGEMENTS.md](https://gitlab.com/langurmonkey/gaiasky/blob/master/ACKNOWLEDGEMENTS.md) file.

##  7. Gaia Sky VR

There exists a development version of Gaia Sky which works with the VR headsets supporting OpenVR. More information on this is available in the [README.md file on the vr branch](https://gitlab.com/langurmonkey/gaiasky/blob/vr/README.md).
