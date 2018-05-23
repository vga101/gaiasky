package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.scenegraph.camera.ICamera;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public class ModelObject extends ModelBody {

    private static final double TH_ANGLE_NONE = ModelBody.TH_ANGLE_POINT / 1e6;
    private static final double TH_ANGLE_POINT = ModelBody.TH_ANGLE_POINT / 2e4;
    private static final double TH_ANGLE_QUAD = ModelBody.TH_ANGLE_POINT / 2f;

    @Override
    public double THRESHOLD_NONE() {
        return TH_ANGLE_NONE;
    }

    @Override
    public double THRESHOLD_POINT() {
        return TH_ANGLE_POINT;
    }

    @Override
    public double THRESHOLD_QUAD() {
        return TH_ANGLE_QUAD;
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
        // Position is assumed to be updated elsewhere (script?)
        if (rc != null)
            rc.update(time);
    }

    @Override
    protected float labelFactor() {
        return 1.5e1f;
    }

}
