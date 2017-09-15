package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.render.IQuadRenderable;
import gaia.cu9.ari.gaiaorbit.render.RenderingContext;
import gaia.cu9.ari.gaiaorbit.render.system.FontRenderSystem;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.ComponentTypes;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.ModelCache;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.g3d.MeshPartBuilder2;
import gaia.cu9.ari.gaiaorbit.util.g3d.ModelBuilder2;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import net.jafama.FastMath;

public class StarCluster extends AbstractPositionEntity implements IModelRenderable, I3DTextRenderable, IQuadRenderable {

    private static final double TH_ANGLE = Math.toRadians(0.5);
    private static ModelComponent mc;
    private static Matrix4 modelTransform;
    private static Texture clusterTex;

    // Label and model colors
    private static float[] col = new float[] { 0.9f, 0.9f, 0.2f, 1.0f };

    protected Vector3d pm;
    protected Vector3 pmSph;

    protected float[] labelcolor;

    // Distance of this cluster to Sol, in internal units
    protected double dist;

    // Radius of this cluster in degrees
    protected double raddeg;

    // Number of stars of this cluster
    protected int nstars;

    public StarCluster(String name, String parentName, Vector3d pos, Vector3d pm, Vector3d posSph, Vector3 pmSph, double raddeg, int nstars) {
        super();
        this.parentName = parentName;
        this.name = name.replace("_", " ");
        this.pos = pos;
        this.posSph.set((float) posSph.x, (float) posSph.y);
        this.pm = pm;
        this.pmSph = pmSph;
        this.dist = posSph.z;
        this.raddeg = raddeg;
        this.nstars = nstars;
        this.labelcolor = new float[] { col[0], col[1], col[2], 8.0f };
    }

    public static void initModel() {
        if (mc == null) {
            Material mat = new Material(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA), new ColorAttribute(ColorAttribute.Diffuse, col[0], col[1], col[2], col[3]));
            ModelBuilder2 modelBuilder = ModelCache.cache.mb;
            modelBuilder.begin();
            // create part
            MeshPartBuilder2 bPartBuilder = modelBuilder.part("sph", GL20.GL_LINES, Usage.Position, mat);
            bPartBuilder.icosphere(1, 3, false, true);

            Model model = (modelBuilder.end());

            modelTransform = new Matrix4();
            mc = new ModelComponent(false);
            mc.dlight = new DirectionalLight();
            mc.dlight.set(1, 1, 1, 1, 1, 1);
            mc.env = new Environment();
            mc.env.add(mc.dlight);
            mc.env.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
            mc.env.set(new FloatAttribute(FloatAttribute.Shininess, 0.2f));
            mc.instance = new ModelInstance(model, modelTransform);
        }

        clusterTex = new Texture(Gdx.files.internal("data/tex/cluster-tex.png"), true);
        clusterTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
    }

    public void initialize() {
        this.ct = new ComponentTypes(ComponentType.Clusters.ordinal());
        // Compute size from distance and radius, convert to units
        this.size = (float) (Math.tan(Math.toRadians(this.raddeg)) * this.dist * 2);

        this.localTransform = new Matrix4();

    }

    @Override
    public void doneLoading(AssetManager manager) {
        initModel();
    }

    /**
     * Updates the local transform matrix.
     * 
     * @param time
     */
    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
        // Update pos, local transform
        this.transform.translate(pos);
        Vector3d pmv = aux3d1.get().set(pm).scl(AstroUtils.getMsSince(time.getTime(), AstroUtils.JD_J2015_5) * Constants.MS_TO_Y);
        this.transform.position.add(pmv);

        this.localTransform.idt().translate(this.transform.position.put(aux3f1.get())).scl(this.size);

        Vector3d aux = aux3d1.get();
        this.distToCamera = (float) transform.getTranslation(aux).len();
        this.viewAngle = (float) FastMath.atan(size / distToCamera);
        this.viewAngleApparent = this.viewAngle / camera.getFovFactor();
        if (!copy) {
            addToRenderLists(camera);
        }

        this.opacity *= 0.1f;
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        if (GaiaSky.instance.isOn(ct) && this.opacity > 0) {
            if (this.viewAngleApparent > TH_ANGLE) {
                addToRender(this, RenderGroup.MODEL_FB);
                addToRender(this, RenderGroup.LABEL);
            } else {
                addToRender(this, RenderGroup.BILLBOARD_SPRITE);
            }
        }
    }

    /**
     * Model rendering
     */
    @Override
    public void render(ModelBatch modelBatch, float alpha, double t) {
        mc.touch();
        mc.setTransparency(alpha * opacity);
        mc.instance.transform.set(this.localTransform);
        modelBatch.render(mc.instance, mc.env);

    }

    /**
     * Billboard quad rendering
     */
    @Override
    public void render(ShaderProgram shader, float alpha, boolean colorTransit, Mesh mesh, ICamera camera) {

        // Bind texture
        if (clusterTex != null) {
            clusterTex.bind(0);
            shader.setUniformi("u_texture0", 0);
        }

        Vector3 aux = aux3f1.get();
        shader.setUniformf("u_pos", transform.getTranslationf(aux));
        shader.setUniformf("u_size", size);
        shader.setUniformf("u_color", col[0], col[1], col[2], col[3] * alpha * opacity);
        // Sprite.render
        mesh.render(shader, GL20.GL_TRIANGLES, 0, 6);
    }

    /**
     * Label rendering
     */
    @Override
    public void render(SpriteBatch batch, ShaderProgram shader, FontRenderSystem sys, RenderingContext rc, ICamera camera) {
        Vector3d pos = aux3d1.get();
        textPosition(camera, pos);
        shader.setUniformf("u_viewAngle", (float) this.viewAngle * 500f);
        shader.setUniformf("u_viewAnglePow", 1f);
        shader.setUniformf("u_thOverFactor", 1f);
        shader.setUniformf("u_thOverFactorScl", 1f);

        render3DLabel(batch, shader, sys.font3d, camera, rc, text(), pos, textScale(), textSize());
    }

    @Override
    public boolean hasAtmosphere() {
        return false;
    }

    @Override
    public boolean renderText() {
        return name != null && GaiaSky.instance.isOn(ComponentType.Labels) && this.opacity > 0;
    }

    @Override
    public float[] textColour() {
        return labelcolor;
    }

    @Override
    public float textSize() {
        return (float) distToCamera * 1e-3f;
    }

    @Override
    public float textScale() {
        return 1;
    }

    @Override
    public void textPosition(ICamera cam, Vector3d out) {
        transform.getTranslation(out);
        double len = out.len();
        out.clamp(0, len - getRadius()).scl(0.9f);

        Vector3d aux = aux3d2.get();
        aux.set(cam.getUp());

        aux.crs(out).nor();

        float dist = -0.015f * (float) out.len();

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
    public boolean isLabel() {
        return true;
    }

}
