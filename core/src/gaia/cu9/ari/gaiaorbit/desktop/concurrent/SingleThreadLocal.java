package gaia.cu9.ari.gaiaorbit.desktop.concurrent;

import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector2d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

/**
 * @deprecated No longer used
 * @author tsagrista
 *
 */
public abstract class SingleThreadLocal {

    public static class V3dThreadLocal implements IThreadLocal<Vector3d> {
        Vector3d value = new Vector3d();

        @Override
        public Vector3d get() {
            return value;
        }
    }

    public static class V3fThreadLocal implements IThreadLocal<Vector3> {
        Vector3 value = new Vector3();

        @Override
        public Vector3 get() {
            return value;
        }
    }

    public static class V2dThreadLocal implements IThreadLocal<Vector2d> {
        Vector2d value = new Vector2d();

        @Override
        public Vector2d get() {
            return value;
        }
    }

    public static class M4dThreadLocal implements IThreadLocal<Matrix4d> {
        Matrix4d value = new Matrix4d();

        @Override
        public Matrix4d get() {
            return value;
        }
    }
}
