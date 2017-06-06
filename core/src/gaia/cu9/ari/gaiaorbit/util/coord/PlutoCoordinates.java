package gaia.cu9.ari.gaiaorbit.util.coord;

import java.util.Date;

import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

public class PlutoCoordinates implements IBodyCoordinates {
    @Override
    public void doneLoading(Object... params) {
    }

    @Override
    public Vector3d getEclipticSphericalCoordinates(Date date, Vector3d out) {
	AstroUtils.plutoEclipticCoordinates(date, out);
	// To internal units
	out.z *= Constants.KM_TO_U;
	return out;
    }

    @Override
    public Vector3d getEclipticCartesianCoordinates(Date date, Vector3d out) {
	getEclipticSphericalCoordinates(date, out);
	Coordinates.sphericalToCartesian(out, out);
	return out;
    }

    @Override
    public Vector3d getEquatorialCartesianCoordinates(Date date, Vector3d out) {
	getEclipticSphericalCoordinates(date, out);
	Coordinates.sphericalToCartesian(out, out);
	out.mul(Coordinates.equatorialToEcliptic());

	return out;
    }

}