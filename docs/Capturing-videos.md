In order to capture videos there are at least two options which differ *significantly*.

### Frame output system + `ffmpeg`

This method consists on outputting an image to a file every frame using the [[frame output system|Configuration-interface#frame-output]] of Gaia Sky to later gather them to create a video using a video encoder software such as [`ffmpeg`](https://ffmpeg.org/), which works on Windows, Linux and OS X.

Once you have recorded the images using the [[frame output interface|Configuration-interface#frame-output]], you can convert them into a video using the following command:

``` bash
ffmpeg -start_number [start_img_num] -i [prefix]%05d.png -vframes [num_images] -s 1280x720 -c:v libx264 -r 25 -preset [slower|veryslow|placebo] -pix_fmt + [out_video_filename].mp4
```

You need to obviously change the prefix and start number, choose the right resolution, frame rate and preset and modify the output format if you need to.

### OpenGL context recorder

There are several available options to record OpenGL rendering to videos, in all systems. Below are some of these listed.
These methods, however, will only record the OpenGL scene as it is displayed in the screen and are limited to its resolution.

##### Linux
- [`glc`](https://github.com/nullkey/glc)/[`glcs`](https://github.com/lano1106/glcs) - Command-line interface applications. The documentation and user guides can be found in this [wiki](https://github.com/nullkey/glc/wiki).
- [Gamecaster](https://launchpad.net/gamecaster) - Front end to `glc`.
- [Soul Capture](https://piga.orain.org/wiki/Soul_Capture) - Front end to `glc`.

##### Windows
- [FRAPS](http://www.fraps.com/) - 3rd party Direct3D and OpenGL recording software.
- [NVIDIA Shadowplay](http://www.geforce.com/geforce-experience/shadowplay) - Only for Geforce cards.
