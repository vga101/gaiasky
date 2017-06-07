package gaia.cu9.ari.gaiaorbit.data.group;

import java.util.List;

public interface IParticleGroupDataProvider {
    /**
     * Loads the data as it is.
     * 
     * @param file
     * @return
     */
    public List<double[]> loadData(String file);

    /**
     * Loads the data applying a factor.
     * 
     * @param file
     * @param factor
     * @return
     */
    public List<double[]> loadData(String file, double factor);
}
