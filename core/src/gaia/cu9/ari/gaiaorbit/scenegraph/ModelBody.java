package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ITransform;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * Abstract class with the basic functionality of bodies represented by a 3D
 * model.
 * 
 * @author Toni Sagrista
 *
 */
public abstract class ModelBody extends CelestialBody {
    protected static final double TH_ANGLE_POINT = Math.toRadians(0.30);

    /** The star closest to the camera at each moment **/
    public static CelestialBody closestCamStar = null;

    /**
     * Angle limit for rendering as point. If angle is any bigger, we render
     * with shader.
     */
    public double THRESHOLD_POINT() {
        return TH_ANGLE_POINT;
    }

    /** MODEL **/
    public ModelComponent mc;

    /** NAME FOR WIKIPEDIA **/
    public String wikiname;

    /** TRANSFORMATIONS - are applied each cycle **/
    public ITransform[] transformations;

    /** Multiplier for Loc view angle **/
    public float locVaMultiplier = 1f;
    /** ThOverFactor for Locs **/
    public float locThOverFactor = 1f;

    public ModelBody() {
        super();
        localTransform = new Matrix4();
        orientation = new Matrix4d();
    }

    public void initialize() {
        if (mc != null) {
            mc.initialize();
        }
        setColor2Data();
        setDerivedAttributes();
    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);
        if (mc != null) {
            mc.doneLoading(manager, localTransform, cc);
        }
    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
        super.updateLocal(time, camera);
        // Update light with global position
        if (mc != null) {
            mc.dlight.direction.set(transform.getTranslationf());
            if (closestCamStar != null) {
                mc.dlight.direction.sub(closestCamStar.transform.getTranslationf(aux3f1.get()));
                mc.dlight.color.set(closestCamStar.cc[0], closestCamStar.cc[1], closestCamStar.cc[2], 1.0f);
            } else {
                mc.dlight.direction.add((float) camera.getPos().x, (float) camera.getPos().y, (float) camera.getPos().z);
                mc.dlight.color.set(1f, 1f, 1f, 0f);
            }
        }
        updateLocalTransform();
    }

    /**
     * Update the local transform with the transform and the rotations/scales
     * necessary. Override if your model contains more than just the position
     * and size.
     */
    protected void updateLocalTransform() {
        setToLocalTransform(1, localTransform, true);
    }

    public void setToLocalTransform(float sizeFactor, Matrix4 localTransform, boolean forceUpdate) {
        if (sizeFactor != 1 || forceUpdate) {
            float[] trnsltn = transform.getTranslationf();
            localTransform.idt().translate(trnsltn[0], trnsltn[1], trnsltn[2]).scl(size * sizeFactor).rotate(0, 1, 0, (float) rc.ascendingNode).rotate(0, 0, 1, (float) (rc.inclination + rc.axialTilt)).rotate(0, 1, 0, (float) rc.angle);
            orientation.idt().rotate(0, 1, 0, (float) rc.ascendingNode).rotate(0, 0, 1, (float) (rc.inclination + rc.axialTilt));
        } else {
            localTransform.set(this.localTransform);
        }

        // Apply transformations
        if (transformations != null)
            for (ITransform tc : transformations)
                tc.apply(localTransform);
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        camera.checkClosest(this);
        if (GaiaSky.instance.isOn(ct)) {
            double thPoint = (THRESHOLD_POINT() * camera.getFovFactor());
            if (viewAngleApparent >= thPoint) {
                opacity = (float) MathUtilsd.lint(viewAngleApparent, thPoint, thPoint * 4, 0, 1);
                if (viewAngleApparent < THRESHOLD_QUAD() * camera.getFovFactor()) {
                    addToRender(this, RenderGroup.SHADER_F);
                } else {
                    addToRender(this, RenderGroup.MODEL_F);
                }

                if (renderText()) {
                    addToRender(this, RenderGroup.LABEL);
                }
            }
        }
    }

    @Override
    public float getInnerRad() {
        return .5f;
        //return .02f;
    }

    protected void setDerivedAttributes() {
        this.flux = (float) Math.pow(10, -absmag / 2.5f);
    }

    public void dispose() {
        super.dispose();
        mc.dispose();
    }

    @Override
    public void render(ModelBatch modelBatch, float alpha, float t) {
        mc.touch();
        mc.setTransparency(alpha * opacity);
        modelBatch.render(mc.instance, mc.env);
    }

    public boolean withinMagLimit() {
        return this.absmag <= GlobalConf.runtime.LIMIT_MAG_RUNTIME;
    }

    @Override
    protected float labelMax() {
        return 1e-4f;
    }

    public void setModel(ModelComponent mc) {
        this.mc = mc;
    }

    public float getFuzzyRenderSize(ICamera camera) {
        float thAngleQuad = (float) THRESHOLD_QUAD() * camera.getFovFactor();
        double size = 0f;
        if (viewAngle >= THRESHOLD_POINT() * camera.getFovFactor()) {
            size = Math.tan(thAngleQuad) * distToCamera * 2f;
        }
        return (float) size / camera.getFovFactor();
    }

    protected float getViewAnglePow() {
        return 1.14f;
    }

    protected float getThOverFactorScl() {
        return ct.contains(ComponentType.Moons) ? 2000f : 100f;
    }

    @Override
    public float textScale() {
        return Math.max(1f, labelSizeConcrete());
    }

    protected float labelSizeConcrete() {
        return (float) Math.pow(this.size * .6e1f, .001f);
    }

    public String getWikiname() {
        return wikiname;
    }

    public void setWikiname(String wikiname) {
        this.wikiname = wikiname;
    }

    public void setLocvamultiplier(Double val) {
        this.locVaMultiplier = val.floatValue();
    }

    public void setLocthoverfactor(Double val) {
        this.locThOverFactor = val.floatValue();
    }

    public void setTransformations(Object[] transformations) {
        this.transformations = new ITransform[transformations.length];
        for (int i = 0; i < transformations.length; i++)
            this.transformations[i] = (ITransform) transformations[i];
    }
}
