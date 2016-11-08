Antialiasing is a term to refer to a number of techniques for **reducing jagged edges**, stairstep-like lines that should be smooth. It reduces the jagged appearance of lines and edges, but it also makes the image smoother. The result are generally better looking images, even though this depends on the resolution display device.

There are several groups of antialiasing techniques, some of them implemented in the Gaia Sandbox and available for you to choose from the [[preferences dialog|Configuration-interface]]. They all come at a cost, which may vary depending on your system.

Name | Type | Description
-----|------|------------
**No Antialiasing** | No antialiasing | This has no cost since it does not apply any antialiasing technique.
**FXAA** | Post-processing | This has a mild performance cost and produces reasonably good results. If you have a good graphics card, this is super-fast.
**NFAA** | Post-processing | Based on the creation of a normal map to identify edges, this is slightly costlier than FXAA but it may produce better results in some devices.
**MSAAx2** | MSAA | MSAA is implemented in the graphics card itself and comes at a greater cost than post-processing techniques since it multi-samples the scene and uses its geometry to antialias it. This version uses two samples per pixel.
**MSAAx4** | MSAA | Version of MSAA that uses four samples per pixel, therefore it is costlier than MSAAx2.
**MSAAx8** | MSAA | Version of MSAA that uses eight samples per pixel, therefore it is costlier than MSAAx4.
**MSAAx16** | MSAA | Version of MSAA that uses sixteen samples per pixel, therefore it is costlier than MSAAx8.

```
Since version 1.0.1 the MSAA options have been removed due
to the lack of support for multisampling frame buffers in libgdx.
```

Here are some sample images.

Name | Image
-----|------
**No Antialiasing** | ![NOAA](http://www.zah.uni-heidelberg.de/fileadmin/user_upload/gaia/gaiasandbox/img/aa/NOAA.png)
**FXAA** | ![FXAA](http://www.zah.uni-heidelberg.de/fileadmin/user_upload/gaia/gaiasandbox/img/aa/FXAA.png)
**NFAA** | ![NFAA](http://www.zah.uni-heidelberg.de/fileadmin/user_upload/gaia/gaiasandbox/img/aa/NFAA.png)
**MSAAx2** | ![MSAAx2](http://www.zah.uni-heidelberg.de/fileadmin/user_upload/gaia/gaiasandbox/img/aa/MSAAx2.png)
**MSAAx4** | ![MSAAx4](http://www.zah.uni-heidelberg.de/fileadmin/user_upload/gaia/gaiasandbox/img/aa/MSAAx4.png)
**MSAAx8** | ![MSAAx8](http://www.zah.uni-heidelberg.de/fileadmin/user_upload/gaia/gaiasandbox/img/aa/MSAAx8.png)
**MSAAx16** | ![MSAAx16](http://www.zah.uni-heidelberg.de/fileadmin/user_upload/gaia/gaiasandbox/img/aa/MSAAx16.png)

Some graphics drivers allow you to override the anti-aliasing settings of applications with some default configuration (usually MSAA or FXAA). You can also use this feature with the Gaia Sandbox.
