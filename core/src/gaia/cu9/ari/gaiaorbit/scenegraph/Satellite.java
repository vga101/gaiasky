package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.math.Matrix4;

import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public abstract class Satellite extends ModelBody {

    protected static final double TH_ANGLE_NONE = ModelBody.TH_ANGLE_POINT / 1e18;
    protected static final double TH_ANGLE_POINT = ModelBody.TH_ANGLE_POINT / 1e9;
    protected static final double TH_ANGLE_QUAD = ModelBody.TH_ANGLE_POINT / 8;

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
    public void initialize() {
        super.initialize();
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
        forceUpdatePosition(time, false);
    }

    /**
     * Default implementation, only sets the result of the coordinates call to
     * pos
     * 
     * @param time
     *            Time to get the coordinates
     * @param force
     *            Whether to force the update
     */
    protected void forceUpdatePosition(ITimeFrameProvider time, boolean force) {
        if (time.getDt() != 0 || force) {
            coordinates.getEquatorialCartesianCoordinates(time.getTime(), pos);
            // Convert to cartesian coordinates and put them in aux3 vector
            Vector3d aux3 = aux3d1.get();
            Coordinates.cartesianToSpherical(pos, aux3);
            posSph.set((float) (AstroUtils.TO_DEG * aux3.x), (float) (AstroUtils.TO_DEG * aux3.y));
        }
    }

    @Override
    protected void updateLocalTransform() {
        setToLocalTransform(1, localTransform, true);
    }

    /**
     * Sets the local transform of this satellite
     */
    public void setToLocalTransform(float sizeFactor, Matrix4 localTransform, boolean forceUpdate) {
        if (sizeFactor != 1 || forceUpdate) {
            transform.getMatrix(localTransform).scl(size * sizeFactor);
        } else {
            localTransform.set(this.localTransform);
        }

    }

    @Override
    protected float labelFactor() {
        return .5e1f;
    }

    @Override
    protected float labelMax() {
        return super.labelMax() * 2;
    }

    protected float getViewAnglePow() {
        return 1f;
    }

    protected float getThOverFactorScl() {
        return 5e3f;
    }

    public float getFuzzyRenderSize(ICamera camera) {
        float thAngleQuad = (float) THRESHOLD_QUAD() * camera.getFovFactor();
        double size = 0f;
        if (viewAngle >= THRESHOLD_POINT() * camera.getFovFactor()) {
            size = Math.tan(thAngleQuad) * distToCamera * 10f;
        }
        return (float) size / camera.getFovFactor();
    }

}
