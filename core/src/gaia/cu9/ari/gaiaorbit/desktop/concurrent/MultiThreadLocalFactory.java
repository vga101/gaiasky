package gaia.cu9.ari.gaiaorbit.desktop.concurrent;

import java.util.HashMap;

import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.desktop.concurrent.MultiThreadLocal.M4dThreadLocal;
import gaia.cu9.ari.gaiaorbit.desktop.concurrent.MultiThreadLocal.V2dThreadLocal;
import gaia.cu9.ari.gaiaorbit.desktop.concurrent.MultiThreadLocal.V3dThreadLocal;
import gaia.cu9.ari.gaiaorbit.desktop.concurrent.MultiThreadLocal.V3fThreadLocal;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector2d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

/**
 * @deprecated No longer used
 * @author tsagrista
 *
 */
public class MultiThreadLocalFactory extends ThreadLocalFactory {

    public MultiThreadLocalFactory() {
        super();
        map = new HashMap<Class, Class<? extends IThreadLocal>>();

        map.put(Vector3d.class, V3dThreadLocal.class);
        map.put(Vector3.class, V3fThreadLocal.class);
        map.put(Vector2d.class, V2dThreadLocal.class);
        map.put(Matrix4d.class, M4dThreadLocal.class);
    }

}
