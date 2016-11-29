package gaia.cu9.ari.gaiaorbit.scenegraph.component;

import com.badlogic.gdx.math.Matrix4;

public class ScaleTransform implements ITransform {
    /** Scale **/
    float[] scale;

    public void apply(Matrix4 mat) {
        mat.scale(scale[0], scale[1], scale[2]);
    }

    public void setScale(double[] scale) {
        this.scale = new float[scale.length];
        for (int i = 0; i < scale.length; i++)
            this.scale[i] = (float) scale[i];
    }
}
