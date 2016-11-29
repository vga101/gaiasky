package gaia.cu9.ari.gaiaorbit.scenegraph.component;

import com.badlogic.gdx.math.Matrix4;

/**
 * Represents a generic matrix transformation
 * @author tsagrista
 *
 */
public interface ITransform {
    public void apply(Matrix4 mat);
}
