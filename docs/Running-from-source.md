### Requirements
If you want to compile the source code, you will need the following:

- [JDK7 or above](http://www.oracle.com/technetwork/java/javase/downloads/index.html) (JDK8 recommended)

Please, be aware that only `tags` are guaranteed to work ([here](https://github.com/ari-zah/gaiasky/tags)). The `master` branch holds the development version and the configuration files are possibly messed up and not ready to work out-of-the-box. So remember to use a `tag` version if you want to run it right away from source.

Also, this guide is for **Unix-like systems only**. If you are working
on Windows, you will need [git for windows](http://git-scm.com/download/win) and [Power Shell](http://en.wikipedia.org/wiki/Windows_PowerShell), even though it has not been tested.

First, clone the repository:
```
git clone https://github.com/ari-zah/gaiasky.git
cd gaiasky
```

### Compile and run
To compile the code and run the desktop version of the application:
```
gradlew desktop:run
```
### Package the Gaia Sky
To pack the application into a ``tar`` file:

```
gradlew desktop:createTar
```

In order to produce the desktop installers for the various systems you need a licensed version of ``Install4j``.

```
gradlew desktop:pack
```

These commands will compile and package the application into a
`gaiasky-[version]` folder under the `gaiasky/releases`
folder.
