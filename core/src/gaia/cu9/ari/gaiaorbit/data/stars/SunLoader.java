package gaia.cu9.ari.gaiaorbit.data.stars;

import java.io.FileNotFoundException;

import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.data.ISceneGraphLoader;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

/**
 * Adds the sun manually
 * @author Toni Sagrista
 *
 */
public class SunLoader extends AbstractCatalogLoader implements ISceneGraphLoader {

    @Override
    public Array<? extends CelestialBody> loadData() throws FileNotFoundException {
        Array<Star> result = new Array<Star>(1);
        /** ADD SUN MANUALLY **/
        Star sun = new Star(new Vector3d(0, 0, 0), 4.83f, 4.83f, 0.656f, "Sol", System.currentTimeMillis());
        if (runFiltersAnd(sun)) {
            sun.initialize();
            result.add(sun);
        }
        return result;
    }

}
