package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.ModelBatch;

import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.IAtmosphereRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.AtmosphereComponent;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public class Planet extends ModelBody implements IAtmosphereRenderable {

    private static final double TH_ANGLE_NONE = ModelBody.TH_ANGLE_POINT / 1e6;
    private static final double TH_ANGLE_POINT = ModelBody.TH_ANGLE_POINT / .5e5;
    private static final double TH_ANGLE_QUAD = ModelBody.TH_ANGLE_POINT * 2;

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

    private static AssetManager manager;

    ICamera camera;

    /** ATMOSPHERE **/
    AtmosphereComponent ac;

    public Planet() {
        super();
    }

    @Override
    public void initialize() {
        super.initialize();

    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);

        if (Planet.manager == null) {
            Planet.manager = manager;
        }

        // INITIALIZE ATMOSPHERE
        if (ac != null) {
            // Initialize atmosphere model
            ac.doneLoading(mc.instance.materials.first(), this.size);
        }

    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
        super.updateLocal(time, camera);
        this.camera = camera;
    }

    @Override
    protected void updateLocalTransform() {
        super.updateLocalTransform();
        if (ac != null) {
            ac.update(transform);
        }
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
        forceUpdateLocalValues(time, false);
    }

    protected void forceUpdateLocalValues(ITimeFrameProvider time, boolean force) {
        if (time.getDt() != 0 || force) {
            Vector3d aux3 = v3dpool.obtain();
            // Load this planet's spherical ecliptic coordinates into pos
            coordinates.getEquatorialCartesianCoordinates(time.getTime(), pos);

            // Convert to cartesian coordinates and put them in aux3 vector
            Coordinates.cartesianToSpherical(pos, aux3);
            posSph.set((float) (AstroUtils.TO_DEG * aux3.x), (float) (AstroUtils.TO_DEG * aux3.y));
            v3dpool.free(aux3);
            // Update angle
            rc.update(time);
        }
    }

    @Override
    public void render(Object... params) {
        Object first = params[0];
        if (!(first instanceof ModelBatch) || params.length == 2) {
            super.render(params);
        } else {
            // TODO fix this hack of byte parameter
            if (params.length > 2)
                // Atmosphere rendering
                render((ModelBatch) first, (Float) params[1], (Byte) params[2]);
        }
    }

    /**
     * Renders model
     */
    @Override
    public void render(ModelBatch modelBatch, float alpha) {
        compalpha = alpha;
        if (ac != null) {
            if (GlobalConf.scene.VISIBILITY[ComponentType.Atmospheres.ordinal()]) {
                ac.updateAtmosphericScatteringParams(mc.instance.materials.first(), alpha, true, transform, parent, rc);
            } else {
                ac.removeAtmosphericScattering(mc.instance.materials.first());
            }
        }
        mc.setTransparency(alpha * opacity);
        modelBatch.render(mc.instance, mc.env);
    }

    /**
     *  Renders atmosphere
     */
    @Override
    public void render(ModelBatch modelBatch, float alpha, byte b) {
        ac.updateAtmosphericScatteringParams(ac.mc.instance.materials.first(), alpha, false, transform, parent, rc);
        // Render atmosphere?
        modelBatch.render(ac.mc.instance, mc.env);
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        super.addToRenderLists(camera);
        // Add atmosphere to default render group if necessary
        if (ac != null && isInRender(this, RenderGroup.MODEL_F)) {
            addToRender(this, RenderGroup.MODEL_F_ATM);
        }

    }

    @Override
    public boolean hasAtmosphere() {
        return ac != null;
    }

    public void setAtmosphere(AtmosphereComponent ac) {
        this.ac = ac;
    }

    @Override
    protected float labelFactor() {
        return 1.5e1f;
    }

    public void dispose() {

    }

}