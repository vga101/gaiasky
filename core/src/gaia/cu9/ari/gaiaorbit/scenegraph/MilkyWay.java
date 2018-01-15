package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.render.RenderingContext;
import gaia.cu9.ari.gaiaorbit.render.system.FontRenderSystem;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public class MilkyWay extends Blob implements IModelRenderable, I3DTextRenderable {
    float[] labelColour = new float[] { 1f, 1f, 1f, 1f };
    ModelComponent mc;
    String model, transformName;
    Matrix4 coordinateSystem;

    public MilkyWay() {
        super();
        localTransform = new Matrix4();
        lowAngle = (float) Math.toRadians(60);
        highAngle = (float) Math.toRadians(75.51);
    }

    public void initialize() {
        mc.initialize();
        mc.env.set(new ColorAttribute(ColorAttribute.AmbientLight, cc[0], cc[1], cc[2], 1));
    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);

        // Set static coordinates to position
        coordinates.getEquatorialCartesianCoordinates(null, pos);

        // Initialize transform
        if (transformName != null) {
            Class<Coordinates> c = Coordinates.class;
            try {
                Method m = ClassReflection.getMethod(c, transformName);
                Matrix4d trf = (Matrix4d) m.invoke(null);
                coordinateSystem = new Matrix4();
                trf.putIn(coordinateSystem);
            } catch (ReflectionException e) {
                Logger.error(this.getClass().getName(), "Error getting/invoking method Coordinates." + transformName + "()");
            }
        } else {
            // Equatorial, nothing
        }
        // Model
        mc.doneLoading(manager, localTransform, null);
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        if (viewAngle <= highAngle) {
            addToRender(this, RenderGroup.MODEL_NORMAL);
            if (renderText()) {
                addToRender(this, RenderGroup.FONT_LABEL);
            }
        }
    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
        super.updateLocal(time, camera);
        // Directional light comes from up
        updateLocalTransform();
        if (mc != null) {
            Vector3 d = aux3f1.get();
            d.set(0, 1, 0);
            d.mul(coordinateSystem);

            mc.dlight.direction.set(d);
        }

    }

    /**
     * Update the local transform with the transform and the rotations/scales
     * necessary. Override if your model contains more than just the position
     * and size.
     */
    protected void updateLocalTransform() {
        // Scale + Rotate + Tilt + Translate
        transform.getMatrix(localTransform).scl(size);
        localTransform.mul(coordinateSystem);
    }

    /**
     * Model rendering.
     */
    @Override
    public void render(ModelBatch modelBatch, float alpha, double t, RenderingContext rc) {
        mc.touch();
        mc.setTransparency(alpha * cc[3] * opacity);
        modelBatch.render(mc.instance, mc.env);
    }

    /**
     * Label rendering.
     */
    @Override
    public void render(SpriteBatch batch, ShaderProgram shader, FontRenderSystem sys, RenderingContext rc, ICamera camera) {
        Vector3d pos = aux3d1.get();
        textPosition(camera, pos);
        shader.setUniformf("u_viewAngle", 90f);
        shader.setUniformf("u_thOverFactor", 1f);
        render3DLabel(batch, shader, sys.font3d, camera, rc, text(), pos, textScale(), textSize());
    }

    @Override
    public boolean hasAtmosphere() {
        return false;
    }

    public void setTransformName(String transformName) {
        this.transformName = transformName;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public boolean renderText() {
        return GaiaSky.instance.isOn(ComponentType.Labels);
    }

    /**
     * Sets the absolute size of this entity
     * 
     * @param size
     */
    public void setSize(Double size) {
        this.size = (float) (size * Constants.KM_TO_U);
    }

    @Override
    public float[] textColour() {
        return labelColour;
    }

    @Override
    public float textSize() {
        return (float) distToCamera * 3e-3f;
    }

    @Override
    public float textScale() {
        return 3f;
    }

    @Override
    public void textPosition(ICamera cam, Vector3d out) {
        transform.getTranslation(out);
    }

    @Override
    public String text() {
        return name;
    }

    @Override
    public void textDepthBuffer() {
        Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(false);
    }

    public void setModel(ModelComponent mc) {
        this.mc = mc;
    }

    public void setLabelcolor(double[] labelcolor) {
        this.labelColour = GlobalResources.toFloatArray(labelcolor);

    }

    @Override
    public boolean isLabel() {
        return true;
    }

}
