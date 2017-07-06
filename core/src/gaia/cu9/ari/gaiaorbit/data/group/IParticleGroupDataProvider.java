package gaia.cu9.ari.gaiaorbit.data.group;

import com.badlogic.gdx.utils.Array;

/**
 * Data provider for a particle group.
 * 
 * @author tsagrista
 *
 */
public interface IParticleGroupDataProvider {
    /**
     * Loads the data as it is.
     * 
     * @param file
     * @return
     */
    public Array<double[]> loadData(String file);

    /**
     * Loads the data applying a factor.
     * 
     * @param file
     * @param factor
     * @return
     */
    public Array<double[]> loadData(String file, double factor);
}
