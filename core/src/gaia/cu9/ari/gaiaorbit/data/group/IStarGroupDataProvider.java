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
     * <p>
     * The loader will only load stars for which the parallax error is
     * at most the percentage given here, in [0..1].
     * More specifically, the following must be met:
     * </p>
     * <code>pllx_err &lt; pllx * pllxErrFactor</code>
     * 
     * @param parallaxErrorFactor
     *            The percentage value of parallax errors with respect to parallax
     */
    public void setParallaxErrorFactor(double parallaxErrorFactor);

    /**
     * Whether to use an adaptive threshold, relaxing it for bright (appmag >= 13) stars to let more 
     * bright stars in.
     */
    public void setAdaptiveParallax(boolean adaptive);

    /**
     * Sets the zero point of the parallax as an addition to the parallax
     * values, in [mas]
     * 
     * @param parallaxZeroPoint
     *            The parallax zero point
     */
    public void setParallaxZeroPoint(double parallaxZeroPoint);

    /**
     * Sets the flag to apply magnitude and color corrections for extinction and
     * reddening
     * 
     * @param magCorrections
     *            Whether to apply the corrections
     */
    public void setMagCorrections(boolean magCorrections);
}