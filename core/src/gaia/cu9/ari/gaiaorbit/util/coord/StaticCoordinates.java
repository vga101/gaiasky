package gaia.cu9.ari.gaiaorbit.util.coord;

import java.util.Date;

import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

/**
 * A position that never changes
 * @author Toni Sagrista
 *
 */
public class StaticCoordinates implements IBodyCoordinates {

    double[] position;

    @Override
    public void doneLoading(Object... params) {
    }

    @Override
    public Vector3d getEclipticSphericalCoordinates(Date date, Vector3d out) {
        return out.set(position);
    }

    @Override
    public Vector3d getEclipticCartesianCoordinates(Date date, Vector3d out) {
        return out.set(position);
    }

    @Override
    public Vector3d getEquatorialCartesianCoordinates(Date date, Vector3d out) {
        return out.set(position);
    }

    public void setPosition(double[] position) {
        this.position = position;
        this.position[0] *= Constants.KM_TO_U;
        this.position[1] *= Constants.KM_TO_U;
        this.position[2] *= Constants.KM_TO_U;
    }

}
