package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.data.galaxy.PointDataProvider;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.GalaxydataComponent;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public class MilkyWayReal extends AbstractPositionEntity implements I3DTextRenderable {
    float[] labelColour = new float[] { 1f, 1f, 1f, 1f };
    String model, transformName;
    Matrix4 coordinateSystem;

    public ModelComponent mc;

    public List<float[]> pointData, nebulaData;
    protected String provider;
    public GalaxydataComponent gc;

    /** Distance from origin at which this entity is fully visible with an alpha of 1f **/
    private float lowDist;
    /** Distance from origin at which this entity is first rendered, but its alpha value is 0f **/
    private float highDist;

    public MilkyWayReal() {
        super();
        localTransform = new Matrix4();
        lowDist = (float) (1e3 * Constants.PC_TO_U);
        highDist = (float) (5e3 * Constants.PC_TO_U);
    }

    public void initialize() {
        /** Load data **/
        PointDataProvider provider = new PointDataProvider();
        try {
            pointData = provider.loadData(gc.pointsource);
            nebulaData = provider.loadData(gc.nebulasource);
        } catch (Exception e) {
            Logger.error(e, getClass().getSimpleName());
        }
        mc.initialize();
        mc.env.set(new ColorAttribute(ColorAttribute.AmbientLight, cc[0], cc[1], cc[2], 1));

    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);

        // Set static coordinates to position
        coordinates.getEquatorialCartesianCoordinates(null, pos);

        // Initialise transform
        if (transformName != null) {
            Class<Coordinates> c = Coordinates.class;
            try {
                Method m = ClassReflection.getMethod(c, transformName);
                Matrix4d trf = (Matrix4d) m.invoke(null);

                coordinateSystem = trf.putIn(new Matrix4());

            } catch (ReflectionException e) {
                Logger.error(this.getClass().getName(), "Error getting/invoking method Coordinates." + transformName + "()");
            }
        } else {
            // Equatorial, nothing
        }

        // Model
        mc.doneLoading(manager, localTransform, null);
        Vector3 aux = new Vector3();
        Vector3 pos3 = pos.toVector3();

        // Transform all
        for (int i = 0; i < pointData.size(); i++) {
            float[] pointf = pointData.get(i);

            aux.set(pointf[0], pointf[2], pointf[1]);
            aux.scl(size).mul(coordinateSystem).add(pos3);
            pointf[0] = aux.x;
            pointf[1] = aux.y;
            pointf[2] = aux.z;
        }

        for (int i = 0; i < nebulaData.size(); i++) {
            float[] pointf = nebulaData.get(i);
            aux.set(pointf[0], pointf[2], pointf[1]);
            aux.scl(size).mul(coordinateSystem).add(pos3);
            pointf[0] = aux.x;
            pointf[1] = aux.y;
            pointf[2] = aux.z;
        }

    }

    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera, float opacity) {
        this.opacity = opacity * this.opacity;
        transform.set(parentTransform);

        // Update with translation/rotation/etc
        updateLocal(time, camera);

        if (children != null && camera.getDistance() * camera.getFovFactor() < highDist) {
            for (int i = 0; i < children.size; i++) {
                float childOpacity = 1 - this.opacity;
                SceneGraphNode child = children.get(i);
                child.update(time, transform, camera, childOpacity);
            }
        }
    }

    @Override
    public void update(ITimeFrameProvider time, Transform parentTransform, ICamera camera) {
        update(time, parentTransform, camera, 1f);
    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
        super.updateLocal(time, camera);

        // Update alpha
        this.opacity = MathUtilsd.lint((float) camera.getDistance() * camera.getFovFactor(), lowDist, highDist, 0, 1);

        // Directional light comes from up
        updateLocalTransform();

        if (mc != null) {
            Vector3 d = aux3f1.get();
            d.set(0, 1, 0);
            d.mul(coordinateSystem);

            mc.dlight.direction.set(d);
        }

    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        if ((float) camera.getDistance() * camera.getFovFactor() >= lowDist) {

            if (renderText()) {
                addToRender(this, RenderGroup.LABEL);
            }
            addToRender(this, RenderGroup.GALAXY);
        }

    }

    /**
     * Update the local transform with the transform and the rotations/scales necessary.
     * Override if your model contains more than just the position and size.
     */
    protected void updateLocalTransform() {
        // Scale + Rotate + Tilt + Translate 
        transform.getMatrix(localTransform).scl(size);
        localTransform.mul(coordinateSystem);
    }

    /**
     * Label rendering.
     */
    @Override
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font3d, BitmapFont font2d, ICamera camera) {
        Vector3d pos = aux3d1.get();
        textPosition(camera, pos);
        shader.setUniformf("a_viewAngle", 90f);
        shader.setUniformf("a_thOverFactor", 1f);
        render3DLabel(batch, shader, font3d, camera, text(), pos, textScale(), textSize(), textColour());
    }

    public void setTransformName(String transformName) {
        this.transformName = transformName;
    }

    @Override
    public boolean renderText() {
        return GaiaSky.instance.isOn(ComponentType.Labels);
    }

    /**
     * Sets the absolute size of this entity
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

    public void setLabelcolor(double[] labelcolor) {
        this.labelColour = GlobalResources.toFloatArray(labelcolor);

    }

    @Override
    public boolean isLabel() {
        return true;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setGalaxydata(GalaxydataComponent gc) {
        this.gc = gc;
    }

    /**
     * Sets the size of this entity in kilometres
     * @param size The diameter of the entity
     */
    public void setSize(Float size) {
        this.size = (float) (size * Constants.KM_TO_U);
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setModel(ModelComponent mc) {
        this.mc = mc;
    }
}
