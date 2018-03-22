package gaia.cu9.ari.gaiaorbit.util.coord;

import java.time.Instant;

import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

public class EclipticCoordinates extends OrbitLintCoordinates {
    @Override
    public Vector3d getEclipticSphericalCoordinates(Instant instant, Vector3d out) {
        return null;
    }

    @Override
    public Vector3d getEquatorialCartesianCoordinates(Instant instant, Vector3d out) {
        boolean inRange = data.loadPoint(out, instant);
        out.rotate(AstroUtils.obliquity(AstroUtils.getJulianDate(instant)), 0, 0, 1);//.mul(Coordinates.equatorialToEcliptic());
        return inRange ? out : null;
    }

}
