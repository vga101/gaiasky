![Gaia Sky](https://zah.uni-heidelberg.de/fileadmin/user_upload/gaia/gaiasky/img/GaiaSkyBanner.jpg)
-------------------------
--------------------------
[![Circle CI](https://circleci.com/gh/ari-zah/gaiasky/tree/master.svg?style=svg)](https://circleci.com/gh/ari-zah/gaiasky/tree/master)


The [**Gaia Sky**](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky) is a real-time, 3D, astronomy visualisation software that
runs on Windows, Linux and MacOS. It is developed in the framework of
[ESA](http://www.esa.int/ESA)'s [Gaia mission](http://sci.esa.int/gaia) to chart about 1 billion stars of our Galaxy.
To get the latest up-to-date and most complete information,
visit our **wiki** pages in <https://github.com/ari-zah/gaiasky/wiki>.

This file contains the following sections:

1. Installation instructions and requirements
2. Configuration instructions
3. Running instructions
4. Copyright and licensing information
5. Contact information
6. Credits and acknowledgements



######################################################
##  1. Installation instructions and requirements    #
######################################################

###1.1 Requirements

- **Minimum System Requirements** -

| **Operating system**  | Windows 7+ / MacOS X / Linux |
| :---: | :--- |
| **CPU** | Intel Core i3 3rd Generation or similar  |
| **GPU** | OpenGL 3.0 support / Intel HD 4000 / Nvidia GeForce 8400 GS |
| **Memory** | 4 GB RAM |
| **Hard drive**  | 150 MB of free space  |
| **Java**  | On Linux, you need the Java Runtime Environment 7+ installed (openJRE is fine) |




###1.2 Installation and uninstallation

Depending on your system and your personal preferences the installation
procedure may vary. Below is a description of the various installation methods
available. You can download the packages [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasandbox/downloads/).

####1.2.1 Windows

Two windows installers are available for 32 and 64-bit systems [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasandbox/downloads/).

- `gaiasky_windows_<version>.exe` - 32 bit installer.
- `gaiasky_windows-x64_<version>.exe` - 64 bit installer.

Both versions will automatically install the JRE if it is not present
in the system.
To install the Gaia Sky, just double click on the installer and
then follow the on-screen instructions. You will need to choose the
directory where the application is to be installed.

In order to **uninstall** the application you can use the Windows Control Panel or
you can use the provided uninstaller in the Gaia Sky folder.

####1.2.2 Linux

We provide 4 packages for linux systems. `deb`, `rpm`, an `aur` package and a linux installer. You can get them [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasandbox/downloads/).
The `aur` package can be installed using any `AUR` helper.

#####1.2.2.1 DEB

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
#####1.2.2.2 AUR

We also provide an [AUR package](https://aur.archlinux.org/packages/gaiasky/) called `gaiasky`. You can install it easily with any tool capable of accessing `AUR`, for example `yaourt`.

```
yaourt -S gaiasky
```



#####1.2.2.3 RPM

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

#####1.2.2.4 Linux installer

We also provide a Linux installer ([here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasandbox/downloads/)) which will trigger a graphical interface
where you can choose the installation location and some other settings.
Download the file `gaiasandbox_unix_<version>.sh` to your disk.
Then run the following to start the installation.

```
./gaiasky_unix_[version].sh
```

Follow the on-screen instructions to proceed with the installation.

In order to **uninstall**, just run the `uninstall` file in the
installation folder.

####1.2.3 OS X - Mac

For OS X we provide a `gaiasky_macos_<version>.dmg` file
which is installed by unpacking into the Applications folder. Get it [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasandbox/downloads/). Once unpacked, the
installer will come up, just follow its instructions.

####1.2.4 Compressed (TGZ) package

A `gaiasky-<version>.tgz` package file is also provided. It will work
in all systems but you need to unpack it yourself and create the desired
shortcuts.
In **Windows**, use an archiver software (7zip, iZArc, etc.) to unpack it.

In **Linux** and **OS X**, you can use:
```
tar -zxvf gaiasky-<version>.tgz
```




######################################################
##  2. Running instructions                          #
######################################################

###2.1 Running the Gaia Sky

In order to run the program just click on the shortcut
provided in your operating system.

###2.2 Running from code

In order to run from code you will need [gradle](https://gradle.org/) 2.10+
installed on your system. You can either follow the instructions on the
[gradle installation](https://docs.gradle.org/current/userguide/installation.html)
section or you can automatically install gradle in your linux
distro:
```
# debian/ubuntu
sudo apt-get install gradle
# arch/manjaro
sudo pacman -S gradle
```
First, clone the [GitHub](https://github.com/ari-zah/gaiasky) repository:
```
git clone https://github.com/ari-zah/gaiasky.git
cd gaiasky
gradle desktop:run
```
Et voilà! The Gaia Sky is running on your machine.


######################################################
##  3. Documentation and help                        #
######################################################

In order to get the full up-to-date documentation, visit our Wiki
pages in GitHub:

[https://github.com/ari-zah/gaiasky/wiki](https://github.com/ari-zah/gaiasky/wiki)

There you will find a thorough documentation of all the features of the
Gaia Sky. If you need to load your data into the sandbox or just
find out how to enable the 3D mode to experience it on your 3DTV, think no further,
the [Gaia Sky wiki](https://github.com/ari-zah/gaiasky/wiki) is the way to go.



######################################################
##  4. Copyright and licensing information           #
######################################################

This software is published and distributed under the LGPL
(Lesser General Public License) license. You can find the full license
text here https://github.com/ari-zah/gaiasky/blob/master/LICENSE
or visiting https://www.gnu.org/licenses/lgpl-3.0-standalone.html



######################################################
##  5. Contact information                           #
######################################################

The main webpage of the project is
http://www.zah.uni-heidelberg.de/gaia2/outreach/gaiasky. There you can find
the latest versions and the latest information on the Gaia Sky. You can also
visit our Github account to inspect the code, check the wiki or report bugs:
https://github.com/ari-zah/gaiasky

###5.1 Main designer and developer
- **Toni Sagrista Selles**
	- **E-mail**: tsagrista@ari.uni-heidelberg.de
	- **Personal webpage**: www.tonisagrista.com


###5.2 Contributors
- **Dr. Stefan Jordan**
	- **E-mail**: jordan@ari.uni-heidelberg.de
	- **Personal webpage**: www.stefan-jordan.de




######################################################
##  6. Credits and acknowledgements                  #
######################################################

The author would like to acknowledge the following people, or the
people behind the following technologies/resources:

- The [DLR](http://www.dlr.de/) for financing this project
- The [BWT](http://www.bmwi.de/), Bundesministerium für Wirtschaft und Technologie, also supporting this project
- My institution, [ARI](http://www.ari.uni-heidelberg.de)/[ZAH](http://www.zah.uni-heidelberg.de/)
- Dr. Martin Altmann for providing the Gaia orbit data
- [Libgdx](http://libgdx.badlogicgames.com)
- [libgdx-contribs-postprocessing](https://github.com/manuelbua/libgdx-contribs/tree/master/postprocessing)
- [HYG catalog](http://www.astronexus.com/hyg)
- [PgsLookAndFeel](http://www.pagosoft.com/projects/pgslookandfeel/)
- Mark Taylor's [STIL](http://www.star.bristol.ac.uk/~mbt/stil/) library
- The [Jython Project](http://www.jython.org/)
- [ernieyu](https://github.com/ernieyu/) for the Java Swing [range slider](https://github.com/ernieyu/Swing-range-slider)
- Nick Risinger for the artist's conception of the Milky Way
- Andreas Ressl and Georg Hammershmid for the star glow texture.
- Tom Patterson ([www.shadedrelief.com]()) for some textures
- Phil Stooke and Grant Hutchison ([http://www.classe.cornell.edu/~seb/celestia/hutchison/index-125.html]()) for some of the textures.
- Machuca+Arias+Caballero for the music track "Gaia-DR1".
- [Install4j](http://www.ej-technologies.com/products/install4j/overview.html) (multi-platform installer builder), for providing a free open source license
- Bitrock's [InstallBuilder](http://installbuilder.bitrock.com/) for providing a free open source license.
- And several online resources without which this would have not been possible

If you think I missed someone, please let me know.
