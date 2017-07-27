package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.InputStream;

import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.scenegraph.ParticleGroup.ParticleBean;

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
     *            The file to load
     * @return Array of particle beans
     */
    public Array<? extends ParticleBean> loadData(String file);

    /**
     * Loads the data applying a factor.
     * 
     * @param file
     *            The file to load
     * @param factor
     *            Factor to apply to the positions
     * @return Array of particle beans
     */
    public Array<? extends ParticleBean> loadData(String file, double factor);

    /**
     * Loads the data applying a factor.
     * 
     * @param is
     *            Input stream to load the data from
     * @param factor
     *            Factor to apply to the positions
     * @return Array of particle beans
     */
    public Array<? extends ParticleBean> loadData(InputStream is, double factor);
}
