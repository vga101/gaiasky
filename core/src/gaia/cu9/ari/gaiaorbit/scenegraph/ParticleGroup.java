package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.data.group.IParticleGroupDataProvider;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
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
public class ParticleGroup extends AbstractPositionEntity implements I3DTextRenderable {
    float[] labelColour;
    Vector3d labelPosition;

    /**
     * Fade in low and high limits
     */
    private Vector2 fadeIn;

    /**
     * Fade out low and high limits
     */
    private Vector2 fadeOut;

    /**
     * The current distance at each cycle, in internal units
     */
    private double currentDistance;

    protected String provider;

    public List<double[]> pointData;

    protected String datafile;

    private float labelAlpha;

    public boolean inGpu;

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
	    this.opacity = opacity * this.opacity;
	    transform.set(parentTransform);
	    this.currentDistance = camera.getDistance() * camera.getFovFactor();

	    // Update with translation/rotation/etc
	    updateLocal(time, camera);

	    if (children != null && (fadeIn == null || fadeIn != null && currentDistance < fadeIn.y)
		    && (fadeOut == null || fadeOut != null && currentDistance > fadeOut.x)) {
		for (int i = 0; i < children.size; i++) {
		    float childOpacity = 1 - this.opacity;
		    SceneGraphNode child = children.get(i);
		    child.update(time, transform, camera, childOpacity);
		}
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
	this.opacity = 1;
	if (fadeIn != null)
	    this.opacity *= MathUtilsd.lint((float) this.currentDistance, fadeIn.x, fadeIn.y, 0, 1);
	if (fadeOut != null)
	    this.opacity *= MathUtilsd.lint((float) this.currentDistance, fadeOut.x, fadeOut.y, 1, 0);

	// Label alpha
	this.labelColour[3] = this.labelAlpha * this.opacity;

    }

    @Override
    protected void addToRenderLists(ICamera camera) {
	if ((fadeIn == null || fadeIn != null && currentDistance > fadeIn.x)
		&& (fadeOut == null || fadeOut != null && currentDistance < fadeOut.y)) {
	    addToRender(this, RenderGroup.POINT_GROUP);

	    if (renderText()) {
		addToRender(this, RenderGroup.LABEL);
	    }
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
	render3DLabel(batch, shader, font3d, camera, text(), pos, textScale(), textSize(), textColour());
    }

    public void setLabelcolor(double[] labelcolor) {
	this.labelColour = GlobalResources.toFloatArray(labelcolor);
	this.labelAlpha = this.labelColour[3];
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

    public void setFadein(double[] fadein) {
	fadeIn = new Vector2((float) (fadein[0] * Constants.PC_TO_U), (float) (fadein[1] * Constants.PC_TO_U));
    }

    public void setFadeout(double[] fadeout) {
	fadeOut = new Vector2((float) (fadeout[0] * Constants.PC_TO_U), (float) (fadeout[1] * Constants.PC_TO_U));
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
	this.labelPosition = new Vector3d(labelposition[0] * Constants.PC_TO_U, labelposition[1] * Constants.PC_TO_U,
		labelposition[2] * Constants.PC_TO_U);
    }

    public void setFactor(Double factor) {
	this.factor = factor;
    }
}
