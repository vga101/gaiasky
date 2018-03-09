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
public abstract class MultiThreadLocal {

    public static class V3dThreadLocal implements IThreadLocal<Vector3d> {
        ThreadLocal<Vector3d> tl = new ThreadLocal<Vector3d>() {
            @Override
            protected Vector3d initialValue() {
                return new Vector3d();
            }
        };

        @Override
        public Vector3d get() {
            return tl.get();
        }
    }

    public static class V3fThreadLocal implements IThreadLocal<Vector3> {
        ThreadLocal<Vector3> tl = new ThreadLocal<Vector3>() {
            @Override
            protected Vector3 initialValue() {
                return new Vector3();
            }
        };

        @Override
        public Vector3 get() {
            return tl.get();
        }
    }

    public static class V2dThreadLocal implements IThreadLocal<Vector2d> {
        ThreadLocal<Vector2d> tl = new ThreadLocal<Vector2d>() {
            @Override
            protected Vector2d initialValue() {
                return new Vector2d();
            }
        };

        @Override
        public Vector2d get() {
            return tl.get();
        }
    }

    public static class M4dThreadLocal implements IThreadLocal<Matrix4d> {
        ThreadLocal<Matrix4d> tl = new ThreadLocal<Matrix4d>() {
            @Override
            protected Matrix4d initialValue() {
                return new Matrix4d();
            }
        };

        @Override
        public Matrix4d get() {
            return tl.get();
        }
    }
}
