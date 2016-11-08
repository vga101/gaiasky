Depending on your system and your personal preferences the installation
procedure may vary. Below is a description of the various installation methods
available.
You can download all packages [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/).

### Windows

Two windows installers are available for 32 and 64-bit systems. Get them [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/).

- `gaiasky_windows_<version>.exe` - 32 bit installer.
- `gaiasky_windows-x64_<version>.exe` - 64 bit installer.

To install the Gaia Sky, just double click on the installer and
then follow the on-screen instructions. You will need to choose the
directory where the application is to be installed.

In order to **uninstall** the application you can use the Windows Control Panel or
you can use the provided uninstaller in the Gaia Sky folder.

### Linux

We provide 3 packages for linux systems (`deb` for *Debian*, *Ubuntu* and derivatives, `rpm` for *RedHat*, *Fedora* and derivatives and a linux installer which works on all distros) plus an [`AUR` package](https://aur.archlinux.org/packages/gaiasky/) for *Arch* and derivatives.
Get the packages [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/).

#### deb

This is the package for Debian-based distros (*Debian*, *Ubuntu*, *Mint*, *SteamOS*, etc.).
[Download](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/) the `gaiasky_<version>.deb` file and run the
following command. You will need root privileges to install a `deb` package in
your system.

```
sudo dpkg -i gaiasky_<version>.deb
```

This will install the application in the `/opt/gaiasky/` folder
and it will create the necessary shortcuts and `.desktop` files. The package depends on
the `default-jre` package, which will be installed if it is not yet there.

In order to **uninstall**, just type:

```
sudo apt-get remove gaiasky
```

#### rpm

This is the package for RPM-based distributions (*Red Hat*, *Fedora*, *Mandriva*, *SUSE*, *CentOS*, etc.)
[Download](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/) the `gaiasky_linux_<version>.rpm` file and run the
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

#### Install from AUR

If you have *Arch*, *Manjaro*, *Antergos* or any other Arch Linux derivative, you can install the [package from AUR](https://aur.archlinux.org/packages/gaiasky/) using any tool able to install AUR software.
For example:
```
sudo yauort -S gaiasky
```
This will download the package and install it in your system. Fast and easy.

#### Unix/Linux installer

We also provide a [Unix/Linux installer](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/) that will trigger a graphical interface
where you can choose the installation location and some other settings.
Download the file `gaiasky_unix_<version>.sh` to your disk.
Then run the following to start the installation.

```
./gaiasky_unix_[version].sh
```

Follow the on-screen instructions to proceed with the installation.

In order to **uninstall**, just execute the `uninstall` file in the
installation folder.

### OS X - Mac

For OS X we provide a `gaiasky_macos_<version>.dmg` file [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/),
which is installed by unpacking into the Applications folder. Once unpacked, the
installer will come up, you just need to follow its instructions.

### Compressed (TGZ) package

A `gaiasky-[version].tgz` package file is also provided [here](https://zah.uni-heidelberg.de/gaia/outreach/gaiasky/downloads/). It will work
in all systems but you need to unpack it yourself and create the desired
shortcuts.
In **Windows**, use an archiver software (7zip, iZArc, etc.) to unpack it.

In **Linux** and **OS X**, you can use:
```
tar zxvf gaiasky-<version>.tgz
```
