All post-processing algorithms (those algorithms that are applied to the image after it has been rendered) take a toll on the graphics card and can be disabled.

*  The **bloom** is not very taxing on the GPU.
*  The **lens flare** effect is a bit harder on the GPU, but most modern cards should be able to handle it with no problems.
*  The **light glow** effect is far more demanding, and disabling it can result in a significant performance gain in some GPUs. It samples the image around the principal light sources using a spiral pattern and applies a light glow texture which is rather large.

To disable these post-processing effects, find the controls in the UI window, as described in the [[lighting|User-interface#lighting]] section of the [[User interface]] chapter.
