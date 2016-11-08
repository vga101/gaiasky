Configuration
*************

Gaia Sky can be configured using the provided Graphical User Interface. However, some functions are not represented in the GUI, so you may need to dive deep into the configuration files.

# Configuration using GUI

The configuration of the application can be done almost
entirely using the graphical interface that pops up
when the program is run. It should be pretty self-
explanatory, but here is a description of the most important items.

## Graphics: Resolution and mode

You can find the `Resolution and mode` configuration
under the `Graphics` tab. There you can switch between
full screen mode and windowed mode. In the case of full screen,
you can choose the resolution from a list of
supported resolutions in a drop down menu. If you choose
windowed mode, you can enter the resolution you want. You can
also choose whether the window should be resizable or not.
In order to switch from full screen mode to windowed
mode during the execution, use the key `F11`.

## Graphics quality

This setting governs the size of the textures, the complexity of the models and also the quality of the graphical effects (`light glow`, `lens flare`, etc.). Here are the differences.
-  `High` - Contains some high-resolution textures (4K) and specular and normal maps for most celestial bodies. Planets and moons have a high vertex count. Graphical effects use a large number of samples to get the best visuals.
-  `Normal` - Contains lower resolution textures (2K when available) and some specular and normal maps are deactivated. The graphical effects use a reasonable amount of quality for nice visuals without compromising the performance too much.
-  `Low` - Offers a noticeable performance gain on less powerful systems. Same textures and model quality as in the `Normal` setting. The `volumetric light` effect is turned off completely and the `lens flare` effect uses a low number of ghosts.


## Antialiasing

In the `Graphics` tab you can also find the antialiasing
configuration. Applying antialiasing removes the
jagged edges of the scene and makes it look better. However,
it does not come free of cost, and usually has a penalty
on the frames per second (FPS).
There are four main options, described below.

### No Antialiasing
If you choose this no antialiasing will be applied, and
therefore you will probably see jagged edges around models. This has no
penalty on either the CPU or the GPU.
If want you enable antialiasing with `override application settings`
in your graphics card driver configuration program, you can
leave the application antialiasing setting to off.  

### FXAA - Fast Approximate Antialiasing
This is a post-processing antialiasing which is very fast
and produces reasonably good results. It has some impact on the
FPS depending on how fast your graphics card is.
As it is a post-processing effect, this will work also when
you take screenshots or output the frames.
You can find a description of FXAA here:
http://en.wikipedia.org/wiki/Fast_approximate_anti-aliasing

### NFAA - Normal Field Antialiasing
This is yet another post-processing antialiasing technique. It is
based on generating a normal map to detect the edges for later
smoothing.
It may look better on some devices and the penalty in FPS is
small. It will also work for the screenshots and frame outputs.

### MSAA - Multi-Sample Antialiasing
As of version `1.0.1` MSAA is not offered anymore.
This is implemented by the graphics card and may not always be
available. You can choose the number of samples (from 2 to 16, from
worse to better) and it has a bigger cost on FPS than the
post-processing options. It also looks better.
However, this being reliant on a special multisample frame buffer
in the graphics card makes it not available for screenshots and
frame outputs.

## Line style
Whether to render lines with an advanced quad system or using simple `GL_LINES`. The former
will look better at the expense of requiring more processing power in the GPU.

## Vertical synchronization (V-sync)
This option limits the frames per second to match your monitor's
refresh rate and prevent screen tearing. It is recommended
to leave it enabled unless you want to test how many FPS you
can get or you want to fry your card.

## User interface
The `Interface` section allows the user to set the language and the
theme of the user interface.

One can select between a choice of languages using the language drop-down.
There are currently three visual themes available:

* `dark-orange`, black and orange theme.
* `dark-orange-large`, same as dark-orange, but with larger fonts.
* `dark-green`, black and green theme. The default since v0.800b.
* `light-blue`, a light theme with a blue tone.
* `HiDPI`, a version of the dark-orange theme to be used with high density (retina) screens, 4K monitors, etc.


## Performance and multithreading
In the 'Performance' tab you can enable and disable multithreading. Multithreading is still
an **experimental** feature, so use it at your own risk.
This allows the program to use more than one CPUs for the
processing.

### Levels of Detail (LOD)
These settings apply only when using a catalog with levels of detail like TGAS. We can configure whether we want smooth transitions between the levels (fade-outs and fade-ins) and also the draw distance, which is represented by a range slider. The left knob represents the view angle above which octants are rendered. The right knob only matters if `Smooth LOD transitions` is checked and sets a higher boundary for the angle for the fade-out and fade-in of octant particles.


## Controls
You can see the key associations in the `Controls` tab. Controls are not editable.
Check out the [[Controls]] documentation to know more.


## Screenshot configuration
You can take screenshots anytime when the application is running by
pressing `F5`.
There are two screenshot modes available:
* `Simple`, the classic screenshot of what is currently on screen, with the same resolution.
* `Advanced`, where you can define the resolution of the screenshots.

## Frame output
There is a feature in the Gaia Sky that enables the output
of every frame as an image. This is useful to produce videos. In order to
configure the frame output system, use the `Frame output` tab. There
you can select the output folder, the image prefix name, the output
image resolution (in case of `Advanced` mode) and the target frames per second. When the
program is in frame output mode, it does not run in real time but it
adjusts the internal clock to produce as many frames per second
as specified here. You have to take it into account when you later
use your favourite video encoder ([ffmpeg](https://www.ffmpeg.org/)) to convert the frame
images into a video.

## Camera recording
Here you can set the desired frames per second to capture the camera paths. If your device is not fast
enough in producing the specified frame rate, the application will slow down while recording so that enough
frames are captured. Same behaviour will be uploading during camera playback.

## Data
As of version `1.0.0` you can use the **Data** tab to select the catalogue to load. Gaia Sky ships with
two catalogues by default:
-   **TGAS** This is based on the Tycho-Gaia Astrometric Solution ([source](http://gaia.ari.uni-heidelberg.de) and contains a little over 600.000 stars. This
catalogue uses levels of detail which can be configured in the *Performance* tab.
-   **HYG** This is the Hipparcos, Gliese and Yale Bright Stars ([home page](http://www.astronexus.com/hyg), [GitHub repository](https://github.com/astronexus/HYG-Database)) and contains roughly some 100.000 stars.

## Gaia
Here you can choose the attitude of the satellite. You can either use the `real attitude` (takes a while to load but will ensure that Gaia points to where it should) and the `NSL`, which is an analytical implementation of the nominal attitude of the satellite. It behaves the same as the real thing, but the observation direction is not ensured.


## Check for new version
You can always check for a new version by clicking on this button.
By default, the application checks for a new version if more than
five days have passed since the last check. If a new version
is found, you will see the notice here together with a link to
the download.

## Do not show that again!
If you do not want this configuration dialogue to be displayed again
when you launch the Gaia Sky, tick this check box and
you are good to go.

# Configuration files

There is a configuration file which stores most of
the properties explained in the previous section and some
more. This section is devoted to these properties
that are not represented in the GUI but are still
configurable.
The configuration file is located in `$HOME/.gaiasky/global.properties`. Here
are some of the properties found in this file that are not
represented in the GUI.

### Graphics properties

- **graphics.render.time** -
This property gets a boolean (`true`|`false`) and indicates whether
a timestamp is to be added to screenshots and frames.


### Data properties

- **data.json.catalog** -
This property points to the `json` file where the catalog(s) to load are defined.
The properties `data.json.catalog.*` contain the default catalogs which are shipped with Gaia Sky
and are offered in the config dialog.

- **data.json.objects** -
Contains the `json` file where the definition of all the rest of the data is specified.
There are three child properties (`data.json.objects.gq.[0..2]`) which contain the
default graphics quality options.

- **data.limit.mag** -
This contains the limiting magnitude above which stars shall
not be loaded.

### Scene properties

- **scene.labelfactor** -
A real number in `[0..n]` that controls the number of star labels to display. The larger the number, the more stars will have a label.

- **scene.star.thresholdangle.quad** -
This property contains the view angle (in degrees) boundary above which the stars are rendered as [`quads`](https://www.opengl.org/wiki/Primitive#Quads). `Quads` are basically 4-vertex quadrilaterals, and they can be rendered as textures (images) or using `shaders`. They display more detail but are costlier in terms of GPU processing.

- **scene.star.thresholdangle.point** -
This property contains the view angle (in degrees) boundary above which the stars are rendered as [`points`](https://www.opengl.org/wiki/Primitive#Point_primitives). Points are single pixels, so they are not very resource demanding.

- **scene.star.thresholdangle.none** -
This property contains the view angle (in degrees) below which the stars are not rendered at all. Usually this is 0 unless you want to cull very distant stars.

- **scene.point.alpha.min** -
Contains the minimum alpha value (opacity) in `[0..1]` for the stars rendered as `points`. This should in any case be lower than `scene.point.alpha.max`.

- **scene.point.alpha.max** -
Contains the maximum alpha value (opacity) in `[0..1]` for the stars rendered as `points`. This should in any case be greater than `scene.point.alpha.min`.

- **scene.galaxy.3d** -
Contains a boolean. If set to true, the Milky Way will be rendered using a blending of a 2D image with a 3D distribution of stars and nebulae. Otherwise,
only the 2D image is used.

### Program wide properties

- **program.tutorial** -
This gets a boolean (`true`|`false`) indicating whether the tutorial
script should be automatically run at start up.

- **program.tutorial.script** -
This points to the tutorial script file.

- **program.debuginfo** -
If this property is set to true, some debug information will be
shown at the top right of the window. This contains information
such as the number of stars rendered as a quad, the number of stars
rendered as a point or the frames per second. This can be activated
in real time by pressing `CTRL` + `D`.

- **program.ui.theme** -
Specifies the GUI theme. Three themes are available: `bright-blue`, `dark-orange`, `dark-green`, `dark-orange-large` and `HiDPI`.
If you have a HiDPI screen (retina, 4K monitor) with a large dots per inch (DPI) number, you should use the `HiDPI` theme.
Since version `0.704b` you can also choose the theme by using the
[`Interface tab`](../Configuration-interface#Interface) in the [`Preferences dialog`](../Configuration-interface).
