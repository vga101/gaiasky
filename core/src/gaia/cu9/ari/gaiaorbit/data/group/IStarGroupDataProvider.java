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

    /**
     * The loader will only load stars for which pllx/pllx_error >
     * parallaxOverError
     * 
     * @param parallaxOverError
     *            The capping value
     */
    public void setParallaxOverError(double parallaxOverError);

    /**
     * Sets the zero point of the parallax as an addition to the parallax
     * values, in [mas]
     * 
     * @param parallaxZeroPoint
     *            The parallax zero point
     */
    public void setParallaxZeroPoint(double parallaxZeroPoint);
}