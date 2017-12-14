package gaia.cu9.ari.gaiaorbit.util.coord;

import java.util.Date;

import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitData;
import gaia.cu9.ari.gaiaorbit.scenegraph.HeliotropicOrbit;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

public class GaiaCoordinates extends AbstractOrbitCoordinates {
    OrbitData data;

    @Override
    public void doneLoading(Object... params) {
        orbitname = "Gaia orbit";
        orbit = (HeliotropicOrbit) ((ISceneGraph) params[0]).getNode("Gaia orbit");
        data = orbit.orbitData;
    }

    @Override
    public Vector3d getEclipticCartesianCoordinates(Date date, Vector3d out) {
        return null;
    }

    @Override
    public Vector3d getEclipticSphericalCoordinates(Date date, Vector3d out) {
        return null;
    }

    @Override
    public Vector3d getEquatorialCartesianCoordinates(Date date, Vector3d out) {
        boolean inRange = data.loadPoint(out, date);
        // Rotate by solar longitude, and convert to equatorial.
        out.rotate(AstroUtils.getSunLongitude(date) + 180, 0, 1, 0).mul(Coordinates.eclToEq());
        return inRange ? out : null;
    }

}
