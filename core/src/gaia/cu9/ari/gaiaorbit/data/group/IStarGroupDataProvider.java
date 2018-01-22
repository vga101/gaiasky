package gaia.cu9.ari.gaiaorbit.data.group;

import java.util.Map;

/**
 * Data provider for a star group, which contains an index map with the names
 * and indices of the stars.
 * 
 * @author tsagrista
 *
 */
public interface IStarGroupDataProvider extends IParticleGroupDataProvider {
    public Map<Long, float[]> getColors();

    public void setParallaxOverError(double parallaxOverError);
}