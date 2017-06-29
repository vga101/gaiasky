package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.data.group.IParticleGroupDataProvider;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * This class represents a group of non-focusable particles, all with the same
 * luminosity. The contents of this group will be sent once to GPU memory and
 * stay there, so all particles get rendered directly in the GPU from the GPU
 * with no CPU intervention. This allows for much faster rendering. Use this for
 * large groups of particles.
 * 
 * @author tsagrista
 *
 */
public class ParticleGroup extends FadeNode implements I3DTextRenderable {
    float[] labelColour;
    Vector3d labelPosition;

    /**
     * Profile decay of the particles in the shader
     */
    public float profileDecay = 4.0f;

    protected String provider;

    public List<double[]> pointData;

    protected String datafile;

    public boolean inGpu;
    public int offset, count;

    /**
     * This flag indicates whether the mean position is already given by the
     * JSON injector
     */
    private boolean fixedMeanPosition = false;

    /**
     * Factor to apply to the data points, usually to normalise distances
     */
    protected Double factor = null;

    public ParticleGroup() {
        super();
        inGpu = false;
    }

    public void initialize() {
        /** Load data **/
        try {
            Class clazz = Class.forName(provider);
            IParticleGroupDataProvider provider = (IParticleGroupDataProvider) clazz.newInstance();

            if (factor == null)
                factor = 1d;

            pointData = provider.loadData(datafile, factor);

            if (!fixedMeanPosition) {
                // Mean position
                for (double[] point : pointData) {
                    pos.add(point[0], point[1], point[2]);
                }
                pos.scl(1d / pointData.size());
            }
        } catch (Exception e) {
            Logger.error(e, getClass().getSimpleName());
            pointData = null;
        }
    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);
    }

    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera, float opacity) {
        if (pointData != null) {
            super.update(time, parentTransform, camera, opacity);
        }
    }

    @Override
    public void update(ITimeFrameProvider time, Transform parentTransform, ICamera camera) {
        update(time, parentTransform, camera, 1f);
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        addToRender(this, RenderGroup.PARTICLE_GROUP);

        if (renderText()) {
            addToRender(this, RenderGroup.LABEL);
        }
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
        render3DLabel(batch, shader, font3d, camera, text(), pos, textScale(), textSize(), textColour(), this.opacity);
    }

    public void setLabelcolor(double[] labelcolor) {
        this.labelColour = GlobalResources.toFloatArray(labelcolor);
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public void setDatafile(String datafile) {
        this.datafile = datafile;
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
    }

    @Override
    public boolean renderText() {
        return GaiaSky.instance.isOn(ComponentType.Labels);
    }

    @Override
    public float[] textColour() {
        return labelColour;
    }

    @Override
    public float textSize() {
        return (float) distToCamera * 2e-3f;
    }

    @Override
    public float textScale() {
        return 1f;
    }

    @Override
    public void textPosition(ICamera cam, Vector3d out) {
        out.set(labelPosition).add(cam.getInversePos());
    }

    @Override
    public String text() {
        return name;
    }

    @Override
    public void textDepthBuffer() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);
    }

    @Override
    public boolean isLabel() {
        return true;
    }

    public void setLabelposition(double[] labelposition) {
        this.labelPosition = new Vector3d(labelposition[0] * Constants.PC_TO_U, labelposition[1] * Constants.PC_TO_U, labelposition[2] * Constants.PC_TO_U);
    }

    public void setFactor(Double factor) {
        this.factor = factor;
    }

    public void setProfiledecay(Double profiledecay) {
        this.profileDecay = profiledecay.floatValue();
    }

    public void setPosition(double[] pos) {
        super.setPosition(pos);
        this.fixedMeanPosition = true;
    }
}
