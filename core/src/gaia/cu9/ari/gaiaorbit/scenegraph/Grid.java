package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.render.IAnnotationsRenderable;
import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.render.RenderingContext;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.ModelCache;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.g3d.MeshPartBuilder2;
import gaia.cu9.ari.gaiaorbit.util.g3d.ModelBuilder2;
import gaia.cu9.ari.gaiaorbit.util.gravwaves.RelativisticEffectsManager;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.vr.VRContext;

public class Grid extends AbstractPositionEntity implements IModelRenderable, IAnnotationsRenderable {
    private static final float ANNOTATIONS_ALPHA = 0.8f;

    private static final int divisionsU = 36;
    private static final int divisionsV = 18;

    private BitmapFont font;
    private String transformName;
    public ModelComponent mc;
    private Vector3 auxf;
    private Vector3d auxd;
    private float[] labelColor;

    public Grid() {
        super();
        localTransform = new Matrix4();
        auxf = new Vector3();
        auxd = new Vector3d();
    }

    @Override
    public void initialize() {
        mc = new ModelComponent();
        mc.initialize();
    }

    @Override
    public void doneLoading(AssetManager manager) {
        Material material = new Material(new BlendingAttribute(cc[3]), new ColorAttribute(ColorAttribute.Diffuse, cc[0], cc[1], cc[2], cc[3]));
        // Load model
        ModelBuilder2 modelBuilder = ModelCache.cache.mb;
        modelBuilder.begin();
        // create part
        MeshPartBuilder2 bPartBuilder = modelBuilder.part("sph", GL20.GL_LINES, Usage.Position, material);
        bPartBuilder.sphere(1, 1, 1, divisionsU, divisionsV);

        Model model = (modelBuilder.end());
        // Initialize transform
        localTransform.scl(size);
        if (transformName != null) {
            Class<Coordinates> c = Coordinates.class;
            try {
                Method m = ClassReflection.getMethod(c, transformName);
                Matrix4d trf = (Matrix4d) m.invoke(null);
                Matrix4 aux = new Matrix4();
                trf.putIn(aux);
                localTransform.mul(aux);
            } catch (ReflectionException e) {
                Logger.error(Grid.class.getName(), "Error getting/invoking method Coordinates." + transformName + "()");
            }
        } else {
            // Equatorial, nothing
        }
        mc.instance = new ModelInstance(model, this.localTransform);

        // Relativistic effects
        if (GlobalConf.runtime.RELATIVISTIC_ABERRATION)
            mc.rec.setUpRelativisticEffectsMaterial(mc.instance.materials);
        // Grav waves
        if (GlobalConf.runtime.GRAVITATIONAL_WAVES)
            mc.rec.setUpGravitationalWavesMaterial(mc.instance.materials);

        float pl = .5f;
        labelColor = new float[] { Math.min(1, cc[0] + pl), Math.min(1, cc[1] + pl), Math.min(1, cc[2] + pl), Math.min(1, cc[3] + pl) };

        font = GlobalResources.skin.getFont("grid-annotation");

    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        // Render group never changes
        // Add to toRender list
        addToRender(this, RenderGroup.MODEL_DEFAULT);
        addToRender(this, RenderGroup.FONT_ANNOTATION);
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
    }

    /**
     * Model rendering
     */
    @Override
    public void render(ModelBatch modelBatch, float alpha, double t, RenderingContext rc) {
        mc.touch();
        mc.setTransparencyColor(alpha * cc[3] * opacity);
        mc.updateRelativisticEffects(GaiaSky.instance.getICamera());
        modelBatch.render(mc.instance, mc.env);
    }

    /**
     * Occlusion rendering
     */
    @Override
    public void renderOpaque(ModelBatch modelBatch, float alpha, double t) {
    }

    /**
     * Annotation rendering
     */
    @Override
    public void render(SpriteBatch spriteBatch, ICamera camera, float alpha) {

        // Horizon
        float stepAngle = 360 / divisionsU;
        alpha *= ANNOTATIONS_ALPHA;

        font.setColor(labelColor[0], labelColor[1], labelColor[2], labelColor[3] * alpha);

        Vector3 vroffset = aux3f4.get();
        if (GlobalConf.runtime.OPENVR) {
            if (camera.getCurrent() instanceof NaturalCamera) {
                ((NaturalCamera) camera.getCurrent()).vroffset.put(vroffset);
                vroffset.scl(1 / VRContext.VROFFSET_FACTOR);
            }
        } else {
            vroffset.set(0, 0, 0);
        }

        for (int angle = 0; angle < 360; angle += stepAngle) {
            auxf.set(Coordinates.sphericalToCartesian(Math.toRadians(angle), 0, 1f, auxd).valuesf()).mul(localTransform).nor();
            effectsPos(auxf, camera);
            if (auxf.dot(camera.getCamera().direction.nor()) > 0) {
                auxf.add(camera.getCamera().position).scl(100).add(vroffset);
                camera.getCamera().project(auxf);
                font.draw(spriteBatch, Integer.toString(angle), auxf.x, auxf.y);
            }

        }
        // North-south line
        stepAngle = 180 / divisionsV;
        for (int angle = -90; angle <= 90; angle += stepAngle) {
            if (angle != 0) {
                auxf.set(Coordinates.sphericalToCartesian(0, Math.toRadians(angle), 1f, auxd).valuesf()).mul(localTransform).nor();
                effectsPos(auxf, camera);
                if (auxf.dot(camera.getCamera().direction.nor()) > 0) {
                    auxf.add(camera.getCamera().position).scl(100).add(vroffset);
                    camera.getCamera().project(auxf);
                    font.draw(spriteBatch, Integer.toString(angle), auxf.x, auxf.y);
                }
                auxf.set(Coordinates.sphericalToCartesian(0, Math.toRadians(-angle), -1f, auxd).valuesf()).mul(localTransform).nor();
                effectsPos(auxf, camera);
                if (auxf.dot(camera.getCamera().direction.nor()) > 0) {
                    auxf.add(camera.getCamera().position).scl(100).add(vroffset);
                    camera.getCamera().project(auxf);
                    font.draw(spriteBatch, Integer.toString(angle), auxf.x, auxf.y);
                }
            }
        }

    }

    private void effectsPos(Vector3 auxf, ICamera camera) {
        relativisticPos(auxf, camera);
        gravwavePos(auxf);
    }

    private void relativisticPos(Vector3 auxf, ICamera camera) {
        if (GlobalConf.runtime.RELATIVISTIC_ABERRATION) {
            auxd.set(auxf);
            GlobalResources.applyRelativisticAberration(auxd, camera);
            auxd.put(auxf);
        }
    }

    private void gravwavePos(Vector3 auxf) {
        if (GlobalConf.runtime.GRAVITATIONAL_WAVES) {
            auxd.set(auxf);
            RelativisticEffectsManager.getInstance().gravitationalWavePos(auxd);
            auxd.put(auxf);
        }
    }

    public void setTransformName(String transformName) {
        this.transformName = transformName;
    }

    @Override
    public boolean hasAtmosphere() {
        return false;
    }

}
