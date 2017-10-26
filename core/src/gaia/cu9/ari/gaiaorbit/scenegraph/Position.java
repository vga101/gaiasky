package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.tree.IPosition;

public class Position implements IPosition {

    Vector3d pos;
    Vector3d vel;

    public Position(double x, double y, double z, double vx, double vy, double vz) {
        this.pos = new Vector3d(x, y, z);
        this.vel = new Vector3d(vx, vy, vz);
    }

    @Override
    public Vector3d getPosition() {
        return pos;
    }

    @Override
    public Vector3d getVelocity() {
        return vel;
    }

}
