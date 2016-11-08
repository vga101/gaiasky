The scripting interface is located in the package `sandbox.script`.

### Description of the functions

Before starting, have a look at the [`javadoc` documentation](http://ari-zah.github.io/gaiasky/javadoc/sandbox/script/IScriptingInterface.html), which provides extensive descriptions of each function.

### Using the scripting interface

In order to import the scripting interface package in your script, you just need to import the default implementation `EventScriptingInterface` from the package `sandbox.script`:

``` python
# Import scripting interface
from gaia.cu9.ari.gaiaorbit.script import EventScriptingInterface
```

Then, we need to create the scripting object before start using it.

``` python
gs = EventScriptingInterface()
```

Now, we can start executing functions.

``` python
# Disable input
gs.disableInput()
gs.cameraStop()
gs.minimizeInterfaceWindow()

# Welcome
gs.setHeadlineMessage("Welcome to the Gaia Sandbox")
gs.setSubheadMessage("Explore Gaia, the Solar System and the whole Galaxy!")
[...]
```

### More examples

You can find more examples by looking at the [`scripts` folder](/ari-zah/gaiasandbox/tree/master/android/assets/scripts) in the Gaia Sandbox package.