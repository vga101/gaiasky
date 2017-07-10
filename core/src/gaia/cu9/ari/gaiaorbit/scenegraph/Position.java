package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.tree.IPosition;

public class Position implements IPosition {

    Vector3d pos;

    public Position(double x, double y, double z) {
        this.pos = new Vector3d(x, y, z);
    }

    public Position(Vector3d pos) {
        this.pos = new Vector3d(pos);
    }

    @Override
    public Vector3d getPosition() {
        return pos;
    }

}
