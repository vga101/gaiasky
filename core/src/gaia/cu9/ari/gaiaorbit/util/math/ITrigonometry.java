package gaia.cu9.ari.gaiaorbit.util.math;

/**
 * Trigonometry interface to enable multiple implementations
 * 
 * @author tsagrista
 *
 */
public interface ITrigonometry {
    public double sin(double angle);

    public double asin(double angle);

    public double cos(double angle);

    public double acos(double angle);

    public double tan(double angle);

    public double atan(double angle);

}
