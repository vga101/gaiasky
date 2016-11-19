package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.render.IQuadRenderable;
import gaia.cu9.ari.gaiaorbit.render.PostProcessorFactory;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.RotationComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.color.ColourUtils;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * Represents any celestial body.
 * 
 * @author Toni Sagrista
 *
 */
public abstract class CelestialBody extends AbstractPositionEntity implements I3DTextRenderable, IQuadRenderable, IModelRenderable {
    private static float[] labelColour = new float[] { 1, 1, 1, 1 };

    /**
     * radius/distance limit for rendering at all. If angle is smaller than this
     * quantity, no rendering happens.
     */
    public abstract double THRESHOLD_NONE();

    /**
     * radius/distance limit for rendering as shader. If angle is any bigger, we
     * render as a model.
     */
    public abstract double THRESHOLD_QUAD();

    /**
     * radius/distance limit for rendering as point. If angle is any bigger, we
     * render with shader.
     */
    public abstract double THRESHOLD_POINT();

    public float TH_OVER_FACTOR;

    /** Absolute magnitude, m = -2.5 log10(flux), with the flux at 10 pc **/
    public float absmag;
    /** Apparent magnitude, m = -2.5 log10(flux) **/
    public float appmag;
    /** Red, green and blue colors and their revamped cousins **/
    public float[] ccPale;
    /** Colour for stars that have been observed by Gaia **/
    public float[] ccTransit;
    /**
     * The B-V color index, calculated as the magnitude in B minus the magnitude
     * in V
     **/
    public float colorbv;
    /** The one-dimensional flux **/
    public float flux;
    /** Holds information about the rotation of the body **/
    public RotationComponent rc;

    /** Number of times this body has been observed by Gaia **/
    public int transits = 0;
    /** Last observations increase in ms **/
    public long lastTransitIncrease = 0;

    public float compalpha;

    public CelestialBody() {
        super();
        TH_OVER_FACTOR = (float) (THRESHOLD_POINT() / GlobalConf.scene.LABEL_NUMBER_FACTOR);
    }

    /**
     * Overrides the update adding the magnitude limit thingy.
     */
    @Override
    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera) {
        if (appmag <= GlobalConf.runtime.LIMIT_MAG_RUNTIME) {
            super.update(time, parentTransform, camera);
        }
    }

    @Override
    public void render(Object... params) {
        Object first = params[0];
        if (first instanceof ShaderProgram) {
            // QUAD - SHADER
            render((ShaderProgram) first, (Float) params[1], (Boolean) params[2], (Mesh) params[3], (ICamera) params[4]);
        } else if (first instanceof SpriteBatch) {
            // LABEL
            render((SpriteBatch) first, (ShaderProgram) params[1], (BitmapFont) params[2], (BitmapFont) params[3], (ICamera) params[4]);
        } else if (first instanceof ModelBatch) {
            // Normal model
            render((ModelBatch) first, (Float) params[1], (Float) params[2]);
        }
    }

    float smoothstep(float edge0, float edge1, float x) {
        // Scale, bias and saturate x to 0..1 range
        x = MathUtilsd.clamp((x - edge0) / (edge1 - edge0), 0.0f, 1.0f);
        // Evaluate polynomial
        return x * x * (3f - 2f * x);
    }

    float step(float edge, float x) {
        if (x < edge)
            return 0f;
        else
            return 1f;
    }

    float core(float distance_center, float inner_rad) {
        float core = 1.0f - step(inner_rad / 5.0f, distance_center);
        float core_glow = smoothstep(inner_rad / 2.0f, inner_rad / 5.0f, distance_center);
        return core_glow + core;
    }

    /**
     * Shader render, for planets and stars.
     */
    @Override
    public void render(ShaderProgram shader, float alpha, boolean colorTransit, Mesh mesh, ICamera camera) {
        compalpha = alpha;

        float size = getFuzzyRenderSize(camera);

        Vector3 aux = aux3f1.get();
        shader.setUniformf("u_pos", transform.getTranslationf(aux));
        shader.setUniformf("u_size", size);

        float[] col = colorTransit ? ccTransit : ccPale;
        shader.setUniformf("u_color", col[0], col[1], col[2], alpha * opacity);
        shader.setUniformf("u_inner_rad", getInnerRad());
        shader.setUniformf("u_distance", distToCamera);
        shader.setUniformf("u_apparent_angle", viewAngleApparent);
        shader.setUniformf("u_thpoint", (float) THRESHOLD_POINT() * camera.getFovFactor());

        // Whether light scattering is enabled or not
        shader.setUniformi("u_lightScattering", (this instanceof Star && PostProcessorFactory.instance.getPostProcessor().isLightScatterEnabled()) ? 1 : 0);

        shader.setUniformf("u_radius", getRadius());

        // Sprite.render
        mesh.render(shader, GL20.GL_TRIANGLES, 0, 6);
    }

    public float getFuzzyRenderSize(ICamera camera) {
        float thAngleQuad = (float) THRESHOLD_QUAD() * camera.getFovFactor();
        double size = 0f;
        if (viewAngle >= THRESHOLD_POINT() * camera.getFovFactor()) {
            if (viewAngle < thAngleQuad) {
                float tanThShaderOverlapDist = (float) Math.tan(thAngleQuad) * distToCamera;
                size = tanThShaderOverlapDist;
            } else {
                size = this.size;
            }
        }
        return (float) size / camera.getFovFactor();
    }

    /**
     * Label rendering.
     */
    @Override
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font3d, BitmapFont font2d, ICamera camera) {
        if (camera.getCurrent() instanceof FovCamera) {
            render2DLabel(batch, shader, font2d, camera, text(), pos);
        } else {
            //render2DLabel(batch, shader, font, camera, text(), transform.position);
            // 3D distance font
            Vector3d pos = aux3d1.get();
            textPosition(camera, pos);
            shader.setUniformf("a_viewAngle", (float) viewAngleApparent);
            shader.setUniformf("a_viewAnglePow", getViewAnglePow());
            shader.setUniformf("a_thOverFactor", TH_OVER_FACTOR / camera.getFovFactor());
            shader.setUniformf("a_thOverFactorScl", getThOverFactorScl());

            if (camera.isFocus((CelestialBody) this)) {
                //System.out.println(labelSizeConcrete());
            }

            render3DLabel(batch, shader, font3d, camera, text(), pos, textScale(), textSize(), textColour());
        }

    }

    protected float getViewAnglePow() {
        return 1f;
    }

    protected float getThOverFactorScl() {
        return 1f;
    }

    protected void setColor2Data() {
        final float plus = .1f;
        ccPale = new float[] { Math.min(1, cc[0] + plus), Math.min(1, cc[1] + plus), Math.min(1, cc[2] + plus) };
        ccTransit = new float[] { ccPale[0], ccPale[1], ccPale[2], cc[3] };
    }

    public abstract float getInnerRad();

    public void setMag(Double mag) {
        this.absmag = mag.floatValue();
        this.appmag = mag.floatValue();
    }

    public void setAbsmag(Double absmag) {
        this.absmag = absmag.floatValue();
    }

    public void setAppmag(Double appmag) {
        this.appmag = appmag.floatValue();
    }

    public Vector2 getPositionSph() {
        return posSph;
    }

    /**
     * Adds all the children that are focusable objects to the list.
     * 
     * @param list
     */
    public void addFocusableObjects(List<CelestialBody> list) {
        list.add(this);
        super.addFocusableObjects(list);
    }

    public float getViewAngle() {
        return viewAngle;
    }

    /**
     * Sets the size of this entity in kilometers
     * 
     * @param size
     */
    public void setSize(Double size) {
        // Size gives us the radius, and we want the diameter
        this.size = (float) (size * 2 * Constants.KM_TO_U);
    }

    public boolean isStar() {
        return false;
    }

    /**
     * Sets the rotation period in hours
     */
    public void setRotation(RotationComponent rc) {
        this.rc = rc;
    }

    public boolean withinMagLimit() {
        return this.appmag <= GlobalConf.runtime.LIMIT_MAG_RUNTIME;
    }

    @Override
    public <T extends SceneGraphNode> T getSimpleCopy() {
        CelestialBody copy = (CelestialBody) super.getSimpleCopy();
        copy.absmag = this.absmag;
        copy.appmag = this.appmag;
        copy.colorbv = this.colorbv;
        copy.flux = this.flux;
        copy.rc = this.rc;
        return (T) copy;
    }

    /**
     * Updates the transit number of this body if visible is true and it is a
     * new transit. It also updates the colour if needed.
     * 
     * @param visible
     * @param time
     */
    protected void updateTransitNumber(boolean visible, ITimeFrameProvider time, FovCamera fcamera) {
        if (GlobalConf.scene.COMPUTE_GAIA_SCAN && visible && timeCondition(time)) {
            // Update observations. Add if forward time, subtract if backward time
            transits = Math.max(0, transits + (int) Math.signum(time.getDt()));
            lastTransitIncrease = time.getTime().getTime();
            // Update transit colour
            ColourUtils.long_rainbow(ColourUtils.normalize(transits, 0, 30), this.ccTransit);
        }
    }

    protected boolean timeCondition(ITimeFrameProvider time) {
        // 95 seconds minimum since last increase, this ensures we are not increasing more than once in the same transit
        if (time.getDt() < 0 && lastTransitIncrease - time.getTime().getTime() < 0) {
            lastTransitIncrease = time.getTime().getTime();
            return true;
        } else if (time.getDt() > 0 && time.getTime().getTime() - lastTransitIncrease < 0) {
            lastTransitIncrease = time.getTime().getTime();
            return true;
        } else {
            return (time.getDt() > 0 && time.getTime().getTime() - lastTransitIncrease > 90000) || (time.getDt() < 0 && lastTransitIncrease - time.getTime().getTime() > 90000);
        }
    }

    @Override
    public boolean renderText() {
        return name != null && GaiaSky.instance.isOn(ComponentType.Labels) && Math.pow(viewAngleApparent, getViewAnglePow()) >= (TH_OVER_FACTOR * getThOverFactorScl());
    }

    @Override
    public float[] textColour() {
        return labelColour;
    }

    @Override
    public float textScale() {
        return (float) Math.atan(labelMax()) * labelFactor() * 4e2f;
    }

    @Override
    public float textSize() {
        return (float) (Math.min(labelSizeConcrete() / Math.pow(distToCamera, 1.05f), labelMax()) * distToCamera * labelFactor());
    }

    protected float labelSizeConcrete() {
        return this.size;
    }

    protected abstract float labelFactor();

    protected abstract float labelMax();

    @Override
    public void textPosition(ICamera cam, Vector3d out) {
        transform.getTranslation(out);
        double len = out.len();
        out.clamp(0, len - getRadius()).scl(0.8f);

        Vector3d aux = aux3d2.get();
        aux.set(cam.getUp());

        aux.crs(out).nor();

        float dist = (float) Math.min(-0.03f * out.len(), getRadius());

        aux.add(cam.getUp()).nor().scl(dist);

        out.add(aux);

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
    public boolean hasAtmosphere() {
        return false;
    }

    @Override
    public boolean isLabel() {
        return true;
    }

    public double getPmX() {
        return 0;
    }

    public double getPmY() {
        return 0;
    }

    public double getPmZ() {
        return 0;
    }

}
