package gaia.cu9.ari.gaiaorbit.scenegraph;

/**
 * Any entity which contains a proper motion
 * 
 * @author tsagrista
 *
 */
public interface IProperMotion {

    /**
     * Returns the mu alpha in mas/yr
     * 
     * @return The mu alpha in mas/yr
     */
    public double getMuAlpha();

    /**
     * Returns the mu delta in mas/yr
     * 
     * @return The mu delta in mas/yr
     */
    public double getMuDelta();

    /**
     * Returns the radial velocity in km/s
     * 
     * @return The radial velocity in km/s
     */
    public double getRadialVelocity();

}
