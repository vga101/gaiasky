Object labels or names in the Gaia Sandbox are rendered using a special `shader` which implements [distance field fonts](/libgdx/libgdx/wiki/Distance-field-fonts). This means that labels look great at all distances but it is costlier than the regular method.

The label factor basically determines the stars for which a label will be rendered if labels are active. It is a real number between 1 and 5, and it will be used to scale the `threshold point angle` (which determines the boundary between rendering as `points` or as `quads`) to select whether a label should be rendered or not.

The label is rendered if the formula below yields true.

```
viewAngle > threshold_angle_point / label_factor
```

Currently there is no GUI option for modifying the label factor, so you must directly edit the configuration file in the [[Scene properties|Configuration-files#scene-properties]] section of the [[Configuration files]] chapter.