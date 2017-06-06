package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.util.List;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Matrix4;

import gaia.cu9.ari.gaiaorbit.data.galaxy.SDSSDataProvider;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public class SDSS extends AbstractPositionEntity implements IRenderable {
    float[] labelColour = new float[] { 1f, 1f, 1f, 1f };
    String model, transformName;
    Matrix4 coordinateSystem;

    public List<double[]> pointData;
    protected String provider;
    public String file;

    /**
     * Distance from origin at which this entity is fully visible with an alpha
     * of 1f
     **/
    private float lowDist;
    /**
     * Distance from origin at which this entity is first rendered, but its
     * alpha value is 0f
     **/
    private float highDist;

    public SDSS() {
	super();
	localTransform = new Matrix4();
	lowDist = (float) (5e4 * Constants.PC_TO_U);
	highDist = (float) (5e5 * Constants.PC_TO_U);
    }

    public void initialize() {
	/** Load data **/
	SDSSDataProvider provider = new SDSSDataProvider();
	try {
	    pointData = provider.loadData(file);
	} catch (Exception e) {
	    Logger.error(e, getClass().getSimpleName());
	}
    }

    @Override
    public void doneLoading(AssetManager manager) {
	super.doneLoading(manager);
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

    }

    @Override
    protected void addToRenderLists(ICamera camera) {
	if ((float) camera.getDistance() * camera.getFovFactor() >= lowDist) {
	    addToRender(this, RenderGroup.SDSS);
	}

    }

    /**
     * Update the local transform with the transform and the rotations/scales
     * necessary. Override if your model contains more than just the position
     * and size.
     */
    protected void updateLocalTransform() {
	// Scale + Rotate + Tilt + Translate
    }

    public void setTransformName(String transformName) {
	this.transformName = transformName;
    }

    /**
     * Sets the absolute size of this entity
     * 
     * @param size
     */
    public void setSize(Double size) {
	this.size = (float) (size * Constants.KM_TO_U);
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

    public void setFile(String file) {
	this.file = file;
    }

    /**
     * Sets the size of this entity in kilometres
     * 
     * @param size
     *            The diameter of the entity
     */
    public void setSize(Float size) {
	this.size = (float) (size * Constants.KM_TO_U);
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
    }

}
