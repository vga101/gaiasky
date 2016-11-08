The performance of the application may vary significantly depending on the characteristics of your system. This chapter describes what are the factors that have an impact in a greater or lesser degree in the performance of the Gaia Sandbox and explains how to tweak them.
It is organised in two parts, namely GPU performance (graphics performance) and CPU performance.

## Graphics performance
The Gaia Sandbox uses [OpenGL](https://www.opengl.org/) to render advanced graphics and thus its performance may be affected significatively by your graphics card. Below you can find some tips to improve the performance of the application by tewaking or deactivating some graphical effects.

* [Graphics quality](Configuration-interface#graphics-quality)
* [[Antialiasing]]
* [[Star brightness]]
* [[Model detail]]
* [[Bloom, lens flare and light glow|Post-processing:-bloom-lens-flare-and-light-glow]]
* [[Label factor]]

## CPU performance
The CPU also plays an obvious role in updating the scene state (positions, orientations, etc.), managing the input and events, executing the scripts and calling and running the rendering subsystem, which streams all the texturing and geometrical information to the GPU for rendering. This section describes what are the elements that can cause a major impact in CPU performance and explains how to tune them.

* [[Magnitude limit]]
