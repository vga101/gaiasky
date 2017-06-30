package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.data.group.IParticleGroupDataProvider;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.RotationComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.Quaterniond;
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
public class ParticleGroup extends FadeNode implements I3DTextRenderable, IFocus, IObserver {

    /**
     * List that contains the point data
     */
    public Array<double[]> pointData;

    /**
     * Fully qualified name of data provider class
     */
    protected String provider;

    /**
     * Path of data file
     */
    protected String datafile;

    /**
     * Colour of label
     */
    float[] labelColour;

    /**
     * Position of label
     */
    Vector3d labelPosition;

    /**
     * Profile decay of the particles in the shader
     */
    public float profileDecay = 4.0f;

    /**
     * Are the data of this group in the GPU memory?
     */
    public boolean inGpu;

    // Offset and count for this group
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

    /**
     * Index of the particle acting as focus. Negative if we have no focus here.
     */
    int focusIndex;

    /**
     * Position of the current focus
     */
    Vector3d focusPosition;

    /**
     * Focus attributes
     */
    double focusDistToCamera, focusViewAngle, focusViewAngleApparent, focusSize;

    /**
     * Reference to the current focus data
     */
    double[] focusData;

    public ParticleGroup() {
        super();
        inGpu = false;
        focusIndex = -1;
        focusPosition = new Vector3d();
        EventManager.instance.subscribe(this, Events.FOCUS_CHANGED);
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
                pos.scl(1d / pointData.size);
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

            if (focusIndex >= 0) {
                updateFocus(time, parentTransform, camera);
            }
        }
    }

    @Override
    public void update(ITimeFrameProvider time, Transform parentTransform, ICamera camera) {
        update(time, parentTransform, camera, 1f);
    }

    /**
     * Updates the parameters of the focus, if the focus is active in this group
     * 
     * @param time
     *            The time frame provider
     * @param parentTransform
     *            The parent transform
     * @param camera
     *            The current camera
     */
    public void updateFocus(ITimeFrameProvider time, Transform parentTransform, ICamera camera) {

        Vector3d aux = aux3d1.get().set(this.focusPosition);
        this.focusDistToCamera = aux.sub(camera.getPos()).len();
        this.focusSize = getFocusSize();
        this.focusViewAngle = (float) ((getRadius() / this.focusDistToCamera) / camera.getFovFactor());
        this.focusViewAngleApparent = this.viewAngle * GlobalConf.scene.STAR_BRIGHTNESS;
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

    /**
     * LABEL
     */

    /**
     * Sets the label color
     * 
     * @param labelcolor
     */
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

    /**
     * FOCUS
     */

    /**
     * Default size if not in data, 1e5 km
     * 
     * @return The size
     */
    public double getFocusSize() {
        return 1e5 * Constants.KM_TO_U;
    }

    /**
     * Returns the id
     */
    public long getId() {
        return 123l;
    }

    /**
     * Returns name of focus
     */
    public String getName() {
        return name;
    }

    public boolean isActive() {
        return focusIndex >= 0;
    }

    /**
     * Returns position of focus
     */
    public void setPosition(double[] pos) {
        super.setPosition(pos);
        this.fixedMeanPosition = true;
    }

    /**
     * Adds all the children that are focusable objects to the list.
     * 
     * @param list
     */
    public void addFocusableObjects(Array<IFocus> list) {
        list.add(this);
        super.addFocusableObjects(list);
    }

    // Myself!
    public AbstractPositionEntity getComputedAncestor() {
        return this;
    }

    // Myself?
    public SceneGraphNode getFirstStarAncestor() {
        return this;
    }

    // The focus position
    public Vector3d getAbsolutePosition(Vector3d aux) {
        return aux.set(focusPosition);
    }

    // Same position
    public Vector3d getPredictedPosition(Vector3d aux, ITimeFrameProvider time, ICamera camera, boolean force) {
        return getAbsolutePosition(aux);
    }

    // Spherical position for focus info, will be computed
    public Vector2 getPosSph() {
        return null;
    }

    // Focus dist to camera
    public double getDistToCamera() {
        return focusDistToCamera;
    }

    // Focus view angle
    public double getViewAngle() {
        return focusViewAngle;
    }

    // Focus apparent view angle
    public double getViewAngleApparent() {
        return focusViewAngleApparent;
    }

    // Focus size
    public double getSize() {
        return focusSize;
    }

    public float getAppmag() {
        return 0;
    }

    public float getAbsmag() {
        return 0;
    }

    /**
     * Returns the size of the particle at index i
     * 
     * @param i
     *            The index
     * @return The size
     */
    public double getSize(int i) {
        return getFocusSize();
    }

    // Half the size
    public double getRadius() {
        return getSize() / 2d;
    }

    @Override
    public boolean withinMagLimit() {
        return true;
    }

    @Override
    public RotationComponent getRotationComponent() {
        return null;
    }

    @Override
    public Quaterniond getOrientationQuaternion() {
        return null;
    }

    public void addHit(int screenX, int screenY, int w, int h, int pxdist, NaturalCamera camera, Array<IFocus> hits) {
        int n = pointData.size;
        if (this.opacity > 0)
            for (int i = 0; i < n; i++) {
                double[] vals = pointData.get(i);
                Vector3 pos = aux3f1.get();
                Vector3d aux = aux3d1.get().set(vals[0], vals[1], vals[2]);
                Vector3d posd = aux.add(camera.posinv);
                pos.set(posd.valuesf());

                if (camera.direction.dot(posd) > 0) {
                    // The star is in front of us
                    // Diminish the size of the star
                    // when we are close by
                    double dist = posd.len();
                    double angle = getSize(i) / 2 / dist / camera.getFovFactor();

                    PerspectiveCamera pcamera;
                    if (GlobalConf.program.STEREOSCOPIC_MODE) {
                        if (screenX < Gdx.graphics.getWidth() / 2f) {
                            pcamera = camera.getCameraStereoLeft();
                            pcamera.update();
                        } else {
                            pcamera = camera.getCameraStereoRight();
                            pcamera.update();
                        }
                    } else {
                        pcamera = camera.camera;
                    }

                    angle = (float) Math.toDegrees(angle * camera.fovFactor) * (40f / pcamera.fieldOfView);
                    double pixelSize = Math.max(pxdist, ((angle * pcamera.viewportHeight) / pcamera.fieldOfView) / 2);
                    pcamera.project(pos);
                    pos.y = pcamera.viewportHeight - pos.y;
                    if (GlobalConf.program.STEREOSCOPIC_MODE) {
                        pos.x /= 2;
                    }

                    // Check click distance
                    if (pos.dst(screenX % pcamera.viewportWidth, screenY, pos.z) <= pixelSize) {
                        //Hit
                        focusIndex = i;
                        updateFocusDataPos();
                        hits.add(this);
                        return;
                    }
                }
            }
        focusIndex = -1;
        updateFocusDataPos();
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case FOCUS_CHANGED:
            if (data[0] instanceof String) {
                focusIndex = ((String) data[0]).equals(this.getName()) ? focusIndex : -1;
            } else {
                focusIndex = data[0] == this ? focusIndex : -1;
            }
            updateFocusDataPos();
            break;
        default:
            break;
        }

    }

    private void updateFocusDataPos() {
        if (focusIndex < 0) {
            focusData = null;
        } else {
            focusData = pointData.get(focusIndex);
            focusPosition.set(focusData[0], focusData[1], focusData[2]);
        }
    }
}
