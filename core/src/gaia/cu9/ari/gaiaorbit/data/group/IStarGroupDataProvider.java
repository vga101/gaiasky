package gaia.cu9.ari.gaiaorbit.data.group;

import java.util.List;
import java.util.Map;

/**
 * Data provider for a star group, which contains an index map with the names
 * and indices of the stars.
 * 
 * @author tsagrista
 *
 */
public interface IStarGroupDataProvider extends IParticleGroupDataProvider {
    /**
     * Gets the index, a map from name/id to index
     * 
     * @return The name index
     */
    public Map<String, Integer> getIndex();

    /**
     * Gets the names list, in the same order as the particle data
     * 
     * @return The names array
     */
    public List<String> getNames();
}
