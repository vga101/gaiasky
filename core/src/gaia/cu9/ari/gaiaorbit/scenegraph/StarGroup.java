package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.data.group.IStarGroupDataProvider;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.render.ILineRenderable;
import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.render.IQuadRenderable;
import gaia.cu9.ari.gaiaorbit.render.RenderingContext;
import gaia.cu9.ari.gaiaorbit.render.system.LineRenderSystem;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.ModelCache;
import gaia.cu9.ari.gaiaorbit.util.Pair;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;
import net.jafama.FastMath;

/**
 * A particle group which additionally to the xyz position, supports color and
 * magnitude. id x y z pmx pmy pmz appmag absmag col size additional
 * 
 * @author tsagrista
 *
 */
public class StarGroup extends ParticleGroup implements ILineRenderable, IStarFocus, IQuadRenderable, IModelRenderable {

    /**
     * Contains info on one star
     */
    public static class StarBean extends ParticleBean {
        private static final long serialVersionUID = 1L;

        public static final int SIZE = 17;
        /** INDICES **/

        /** Stored doubles **/
        public static final int I_X = 0;
        public static final int I_Y = 1;
        public static final int I_Z = 2;
        public static final int I_PMX = 3;
        public static final int I_PMY = 4;
        public static final int I_PMZ = 5;
        public static final int I_MUALPHA = 6;
        public static final int I_MUDELTA = 7;
        public static final int I_RADVEL = 8;

        /** Stored as float **/
        public static final int I_APPMAG = 9;
        public static final int I_ABSMAG = 10;
        public static final int I_COL = 11;
        public static final int I_SIZE = 12;

        /** Stored as int **/
        public static final int I_HIP = 13;
        public static final int I_TYC1 = 14;
        public static final int I_TYC2 = 15;
        public static final int I_TYC3 = 16;

        public Long id;
        public transient OctreeNode octant;
        public String name;

        public StarBean(double[] data, Long id, String name) {
            super(data);
            this.id = id;
            this.name = name;
            this.octant = null;
        }

        public Vector3d pos(Vector3d aux) {
            return aux.set(x(), y(), z());
        }

        public double x() {
            return data[I_X];
        }

        public double y() {
            return data[I_Y];
        }

        public double z() {
            return data[I_Z];
        }

        public double pmx() {
            return data[I_PMX];
        }

        public double pmy() {
            return data[I_PMY];
        }

        public double pmz() {
            return data[I_PMZ];
        }

        public double appmag() {
            return data[I_APPMAG];
        }

        public double absmag() {
            return data[I_ABSMAG];
        }

        public double col() {
            return data[I_COL];
        }

        public double size() {
            return data[I_SIZE];
        }

        public double radius() {
            return size() * Constants.STAR_SIZE_FACTOR;
        }

        public int hip() {
            return (int) data[I_HIP];
        }

        public int tyc1() {
            return (int) data[I_TYC1];
        }

        public int tyc2() {
            return (int) data[I_TYC2];
        }

        public int tyc3() {
            return (int) data[I_TYC3];
        }

        public String tyc() {
            return tyc1() + "-" + tyc2() + "-" + tyc3();
        }

        public double mualpha() {
            return data[I_MUALPHA];
        }

        public double mudelta() {
            return data[I_MUDELTA];
        }

        public double radvel() {
            return data[I_RADVEL];
        }

    }

    // Camera dx threshold
    private static final double CAM_DX_TH = 100 * Constants.AU_TO_U;
    // Min update time
    private static final double MIN_UPDATE_TIME_MS = 50;
    // Fade in time to prevent pop-ins
    private static final long FADE_IN_MS = 1000;
    // Sequence id
    private static long idseq = 0;
    /** Star model **/
    private static ModelComponent mc;
    private static Matrix4 modelTransform;

    /** Epoch as julian days **/
    private double epoch_jd = AstroUtils.JD_J2015_5;
    /** Current computed epoch time **/
    private double currDeltaYears = 0;

    private static void initModel() {
        if (mc == null) {
            Texture tex = new Texture(Gdx.files.internal(GlobalConf.TEXTURES_FOLDER + "star.jpg"));
            Texture lut = new Texture(Gdx.files.internal(GlobalConf.TEXTURES_FOLDER + "lut.jpg"));
            tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);

            Map<String, Object> params = new TreeMap<String, Object>();
            params.put("quality", 120l);
            params.put("diameter", 1d);
            params.put("flip", false);

            Pair<Model, Map<String, Material>> pair = ModelCache.cache.getModel("sphere", params, Usage.Position | Usage.Normal | Usage.TextureCoordinates);
            Model model = pair.getFirst();
            Material mat = pair.getSecond().get("base");
            mat.clear();
            mat.set(new TextureAttribute(TextureAttribute.Diffuse, tex));
            mat.set(new TextureAttribute(TextureAttribute.Normal, lut));
            // Only to activate view vector (camera position)
            mat.set(new TextureAttribute(TextureAttribute.Specular, lut));
            mat.set(new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA));
            modelTransform = new Matrix4();
            mc = new ModelComponent(false);
            mc.env = new Environment();
            mc.env.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
            mc.env.set(new FloatAttribute(FloatAttribute.Shininess, 0f));
            mc.instance = new ModelInstance(model, modelTransform);
        }
    }

    /**
     * Thread pool executor
     */
    private static ThreadPoolExecutor pool;
    private static BlockingQueue<Runnable> workQueue;
    static {
        workQueue = new LinkedBlockingQueue<Runnable>();
        int nthreads = !GlobalConf.performance.MULTITHREADING ? 1 : GlobalConf.performance.NUMBER_THREADS();
        pool = new ThreadPoolExecutor(nthreads, nthreads, 5, TimeUnit.SECONDS, workQueue);
    }

    /**
     * The name index
     */
    Map<String, Integer> index;

    /**
     * Additional values
     */
    double[] additional;

    /**
     * Aux array to know whether a star has been rendered in the last cycle
     */
    public byte[] renderedLastCycle;

    // Indices list buffer 1
    Integer[] indices1;
    // Indices list buffer 2
    Integer[] indices2;
    // Active indices list
    Integer[] active;
    // Background indices list (the one we sort)
    Integer[] background;

    // Close up stars treated
    private int N_CLOSEUP_STARS;

    // Updater task
    private UpdaterTask updaterTask;

    /** CLOSEST **/
    private Vector3d closestPos, closestPm;
    private String closestName;
    private double closestDist;
    private double closestSize;
    private float[] closestCol;

    private double modelDist;

    /**
     * Opacity value to prevent pop-ins. Uses the last sort time and FADE_IN_MS
     * to compute an opacity in [0..1]
     **/
    private float popInOpacity;

    private Vector3d lastSortCameraPos;

    /** Comparator **/
    private Comparator<Integer> comp;

    // Is it updating?
    private volatile boolean updating = false;

    public StarGroup() {
        super();
        id = idseq++;
        comp = new StarGroupComparator();
        closestPos = new Vector3d();
        closestPm = new Vector3d();
        lastSortCameraPos = new Vector3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        closestCol = new float[4];
        lastSortTime = -1;
        EventManager.instance.subscribe(this, Events.CAMERA_MOTION_UPDATED, Events.DISPOSE);
    }

    @SuppressWarnings("unchecked")
    public void initialize() {
        /** Load data **/
        try {
            Class<?> clazz = Class.forName(provider);
            IStarGroupDataProvider provider = (IStarGroupDataProvider) clazz.newInstance();

            if (factor == null)
                factor = 1d;

            // Set data, generate index
            Array<StarBean> l = (Array<StarBean>) provider.loadData(datafile, factor);
            this.setData(l);

        } catch (Exception e) {
            Logger.error(e, getClass().getSimpleName());
            pointData = null;
        }

        computeFixedMeanPosition();
    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);
        initModel();

        // Additional
        additional = new double[pointData.size];

        // Initialise indices list with natural order
        indices1 = new Integer[pointData.size];
        indices2 = new Integer[pointData.size];
        for (int i = 0; i < pointData.size; i++) {
            indices1[i] = i;
            indices2[i] = i;
        }
        active = indices1;
        background = indices2;

        /**
         * INIT UPDATER TASK
         */
        updaterTask = new UpdaterTask(this);
    }

    /**
     * Returns the data list
     * 
     * @return The data list
     */
    @SuppressWarnings("unchecked")
    public Array<StarBean> data() {
        return (Array<StarBean>) pointData;
    }

    public void setData(Array<StarBean> pointData, Map<String, Integer> index) {
        this.pointData = pointData;
        this.N_CLOSEUP_STARS = Math.min(250, pointData.size);
        this.index = index;
    }

    public void setData(Array<StarBean> pointData) {
        setData(pointData, true);
    }

    public void setData(Array<StarBean> pointData, boolean regenerateIndex) {
        this.pointData = pointData;
        this.N_CLOSEUP_STARS = Math.min(250, pointData.size);
        if (regenerateIndex)
            regenerateIndex();
    }

    public void regenerateIndex() {
        index = generateIndex(data());
    }

    public HashMap<String, Integer> generateIndex(Array<StarBean> pointData) {
        HashMap<String, Integer> index = new HashMap<String, Integer>();
        int n = pointData.size;
        for (int i = 0; i < n; i++) {
            StarBean sb = pointData.get(i);
            String lcname = sb.name.toLowerCase();
            index.put(lcname, i);
            String lcid = sb.id.toString().toLowerCase();
            if (sb.id > 0 && !lcid.equals(lcname)) {
                index.put(lcid, i);
            }
            if (sb.hip() > 0) {
                String lchip = "hip " + sb.hip();
                if (!lchip.equals(lcname))
                    index.put(lchip, i);
            }
            if (sb.tyc1() > 0) {
                String lctyc = "tyc " + sb.tyc();
                if (!lctyc.equals(lcname))
                    index.put(lctyc, i);
            }
        }
        return index;
    }

    public void computeFixedMeanPosition() {
        if (!fixedMeanPosition) {
            // Mean position
            for (StarBean point : data()) {
                pos.add(point.x(), point.y(), point.z());
            }
            pos.scl(1d / pointData.size);
        }
    }

    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera, float opacity) {
        // Delta years
        currDeltaYears = AstroUtils.getMsSince(time.getTime(), epoch_jd) * Constants.MS_TO_Y;

        super.update(time, parentTransform, camera, opacity);

        // Update closest star
        StarBean closestStar = (StarBean) pointData.get(active[0]);

        closestPm.set(closestStar.pmx(), closestStar.pmy(), closestStar.pmz()).scl(currDeltaYears);
        closestPos.set(closestStar.x(), closestStar.y(), closestStar.z()).sub(camera.getPos()).add(closestPm);
        closestDist = closestPos.len() - getRadius(active[0]);
        Color c = new Color();
        Color.abgr8888ToColor(c, (float) closestStar.col());
        closestCol[0] = c.r;
        closestCol[1] = c.g;
        closestCol[2] = c.b;
        closestCol[3] = c.a;
        closestSize = getSize(active[0]);
        closestName = closestStar.name;
        camera.setClosestStar(this);

        // Model dist
        modelDist = 172.4643429 * getRadius(active[0]);

        // Pop in opacity
        long timeSinceLastSort = TimeUtils.millis() - lastSortTime;
        popInOpacity = MathUtilsd.lint(timeSinceLastSort, 0, FADE_IN_MS, 0, 1);

    }

    /**
     * Updates the parameters of the focus, if the focus is active in this group
     * 
     * @param time
     *            The time frame provider
     * @param camera
     *            The current camera
     */
    public void updateFocus(ITimeFrameProvider time, ICamera camera) {
        StarBean focus = (StarBean) pointData.get(focusIndex);
        Vector3d aux = this.fetchPosition(focus, camera.getPos(), aux3d1.get());

        this.focusPosition.set(aux).add(camera.getPos());
        this.focusDistToCamera = aux.len();
        this.focusSize = getFocusSize();
        this.focusViewAngle = (float) ((getRadius() / this.focusDistToCamera) / camera.getFovFactor());
        this.focusViewAngleApparent = this.focusViewAngle * GlobalConf.scene.STAR_BRIGHTNESS;
    }

    /**
     * Updates the additional information array, to use for sorting.
     * 
     * @param camera
     */
    public void updateAdditional(ITimeFrameProvider time, ICamera camera) {
        Vector3d cpos = camera.getPos();
        double deltaYears = AstroUtils.getMsSince(time.getTime(), epoch_jd) * Constants.MS_TO_Y;
        int n = pointData.size;
        for (int i = 0; i < n; i++) {
            StarBean d = (StarBean) pointData.get(i);

            // Pm
            Vector3d dx = aux3d2.get().set(d.pmx(), d.pmy(), d.pmz()).scl(deltaYears);
            // Pos
            Vector3d x = aux3d1.get().set(d.x(), d.y(), d.z()).add(dx);

            additional[i] = -(((d.size() * Constants.STAR_SIZE_FACTOR) / cpos.dst(x.x, x.y, x.z)) / camera.getFovFactor()) * GlobalConf.scene.STAR_BRIGHTNESS;
        }
    }

    /**
     * Sorts the list of particles using the apparent magnitude (view angle)
     * criterion. This should be called only when the camera frustum is
     * significantly changed. By no means should this be called every cycle.
     * Sorting happens always in the working buffer, which is not the active
     * buffer. After sorting, buffers are swapped.
     */
    public void updateSorter(ITimeFrameProvider time, ICamera camera) {
        // Prepare metadata to sort
        updateAdditional(time, camera);

        // Sort background list of indices
        Arrays.sort(background, comp);
        // Swap indices lists
        swapBuffers();
        // Update last sort call
        super.updateSorter(time, camera);

    }

    private void swapBuffers() {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                if (active == indices1) {
                    active = indices2;
                    background = indices1;
                } else {
                    active = indices1;
                    background = indices2;
                }

            }

        });
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        addToRender(this, RenderGroup.STAR_GROUP);
        addToRender(this, RenderGroup.SHADER_STAR);
        addToRender(this, RenderGroup.LINE);
        addToRender(this, RenderGroup.MODEL_S);
        if (renderText()) {
            addToRender(this, RenderGroup.LABEL);
        }
    }

    /**
     * Quad rendering
     */
    @Override
    public void render(ShaderProgram shader, float alpha, boolean colorTransit, Mesh mesh, ICamera camera) {
        double thpointTimesFovfactor = GlobalConf.scene.STAR_THRESHOLD_POINT * camera.getFovFactor() * .5e-1f;
        double thupOverFovfactor = Constants.THRESHOLD_UP / camera.getFovFactor();
        double thdownOverFovfactor = Constants.THRESHOLD_DOWN / camera.getFovFactor();
        double innerRad = 0.006 + GlobalConf.scene.STAR_POINT_SIZE * 0.008;
        float alph = alpha * this.opacity;

        /** GENERAL UNIFORMS **/
        shader.setUniformf("u_thpoint", (float) GlobalConf.scene.STAR_THRESHOLD_POINT * camera.getFovFactor());
        // Light glow always disabled with star groups
        shader.setUniformi("u_lightScattering", 0);
        shader.setUniformf("u_inner_rad", (float) innerRad);

        /** RENDER ACTUAL STARS **/
        boolean focusRendered = false;
        for (int i = 0; i < N_CLOSEUP_STARS; i++) {
            renderCloseupStar(i, active[i], camera, shader, mesh, thpointTimesFovfactor, thupOverFovfactor, thdownOverFovfactor, alph * this.popInOpacity);
            focusRendered = focusRendered || active[i] == focusIndex;
        }
        if (focus != null && !focusRendered) {
            renderCloseupStar(1, focusIndex, camera, shader, mesh, thpointTimesFovfactor, thupOverFovfactor, thdownOverFovfactor, alph);
        }

    }

    private void renderCloseupStar(int i, int idx, ICamera camera, ShaderProgram shader, Mesh mesh, double thpointTimesFovfactor, double thupOverFovfactor, double thdownOverFovfactor, float alpha) {
        StarBean star = (StarBean) pointData.get(idx);
        double size = getSize(idx);
        double radius = size * Constants.STAR_SIZE_FACTOR;
        Vector3d lpos = fetchPosition(star, camera.getPos(), aux3d1.get());
        double distToCamera = lpos.len();
        double viewAngle = (radius / distToCamera) / camera.getFovFactor();
        double viewAngleApparent = viewAngle * GlobalConf.scene.STAR_BRIGHTNESS;
        Color c = new Color();
        Color.abgr8888ToColor(c, (float) star.col());
        if (viewAngle >= thpointTimesFovfactor) {
            double ssize = getFuzzyRenderSize(camera, size, radius, distToCamera, viewAngle, thdownOverFovfactor, thupOverFovfactor);

            Vector3 pos = lpos.put(aux3f3.get());
            shader.setUniformf("u_pos", pos);
            shader.setUniformf("u_size", (float) ssize);

            shader.setUniformf("u_color", c.r, c.g, c.b, alpha);
            shader.setUniformf("u_distance", (float) distToCamera);
            shader.setUniformf("u_apparent_angle", (float) viewAngleApparent);
            shader.setUniformf("u_radius", (float) radius);

            // Sprite.render
            mesh.render(shader, GL20.GL_TRIANGLES, 0, 6);

        }
    }

    public double getFuzzyRenderSize(ICamera camera, double size, double radius, double distToCamera, double viewAngle, double thdown, double thup) {
        double computedSize = size;
        if (viewAngle > thdown) {
            double dist = distToCamera;
            if (viewAngle > thup) {
                dist = radius / Constants.THRESHOLD_UP;
            }
            computedSize = (size * (dist / radius) * Constants.THRESHOLD_DOWN);
        }
        computedSize *= GlobalConf.scene.STAR_BRIGHTNESS * 0.4;

        return computedSize;
    }

    /**
     * Model rendering
     */
    @Override
    public void render(ModelBatch modelBatch, float alpha, double t) {
        mc.touch();
        float opct = (float) MathUtilsd.lint(closestDist, modelDist / 50f, modelDist, 1f, 0f);
        if (alpha * opct > 0) {
            mc.setTransparency(alpha * opct);
            float[] col = closestCol;
            ((ColorAttribute) mc.env.get(ColorAttribute.AmbientLight)).color.set(col[0], col[1], col[2], 1f);
            ((FloatAttribute) mc.env.get(FloatAttribute.Shininess)).value = (float) t;
            // Local transform
            mc.instance.transform.idt().translate((float) closestPos.x, (float) closestPos.y, (float) closestPos.z).scl((float) (getRadius(active[0]) * 2d));
            modelBatch.render(mc.instance, mc.env);
        }
    }

    /**
     * Proper motion rendering
     */
    @Override
    public void render(LineRenderSystem renderer, ICamera camera, float alpha) {
        float thpointTimesFovfactor = (float) GlobalConf.scene.STAR_THRESHOLD_POINT * camera.getFovFactor();
        int n = Math.min(N_CLOSEUP_STARS * 20, pointData.size);
        for (int i = n - 1; i >= 0; i--) {
            StarBean star = (StarBean) pointData.get(active[i]);
            float radius = (float) (getSize(active[i]) * Constants.STAR_SIZE_FACTOR);
            // Position
            Vector3d lpos = fetchPosition(star, camera.getPos(), aux3d1.get());
            // Proper motion
            Vector3d pm = aux3d2.get().set(star.pmx(), star.pmy(), star.pmz()).scl(currDeltaYears);
            // Rest of attributes
            float distToCamera = (float) lpos.len();
            float viewAngle = (float) (((radius / distToCamera) / camera.getFovFactor()) * GlobalConf.scene.STAR_BRIGHTNESS);
            if (viewAngle >= thpointTimesFovfactor / GlobalConf.scene.PM_NUM_FACTOR) {

                Vector3d p1 = aux3d1.get().set(star.x() + pm.x, star.y() + pm.y, star.z() + pm.z).sub(camera.getPos());
                Vector3d ppm = aux3d2.get().set(star.pmx(), star.pmy(), star.pmz()).scl(GlobalConf.scene.PM_LEN_FACTOR);
                Vector3d p2 = ppm.add(p1);

                // Mualpha -> red channel
                // Mudelta -> green channel
                // Radvel -> blue channel
                // Min value per channel = 0.2
                final double mumin = -80;
                final double mumax = 80;
                final double maxmin = mumax - mumin;
                renderer.addLine(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, (float) ((star.mualpha() - mumin) / maxmin) * 0.8f + 0.2f, (float) ((star.mudelta() - mumin) / maxmin) * 0.8f + 0.2f, (float) (star.radvel()) * 0.8f + 0.2f, alpha * this.opacity);
            }
        }

    }

    /**
     * Label rendering
     */
    @Override
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font3d, BitmapFont font2d, RenderingContext rc, ICamera camera) {
        float thOverFactor = (float) (GlobalConf.scene.STAR_THRESHOLD_POINT / GlobalConf.scene.LABEL_NUMBER_FACTOR / camera.getFovFactor());
        float textScale = 1f;
        for (int i = 0; i < N_CLOSEUP_STARS; i++) {
            StarBean star = (StarBean) pointData.get(active[i]);
            float radius = (float) getRadius(active[i]);
            Vector3d lpos = fetchPosition(star, camera.getPos(), aux3d1.get());
            float distToCamera = (float) lpos.len();
            float viewAngle = (float) (((radius / distToCamera) / camera.getFovFactor()) * GlobalConf.scene.STAR_BRIGHTNESS);

            if (viewAngle >= thOverFactor) {

                textPosition(camera, lpos, distToCamera, radius);
                shader.setUniformf("u_viewAngle", viewAngle);
                shader.setUniformf("u_viewAnglePow", 1f);
                shader.setUniformf("u_thOverFactor", thOverFactor);
                shader.setUniformf("u_thOverFactorScl", camera.getFovFactor());
                float textSize = (float) FastMath.tanh(viewAngle) * distToCamera * 1e5f;
                float alpha = Math.min((float) FastMath.atan(textSize / distToCamera), 1.e-3f);
                textSize = (float) FastMath.tan(alpha) * distToCamera;
                render3DLabel(batch, shader, font3d, camera, star.name, lpos, textScale, textSize, textColour());

            }
        }

    }

    public void textPosition(ICamera cam, Vector3d out, float len, float rad) {
        out.clamp(0, len - rad);

        Vector3d aux = aux3d2.get();
        aux.set(cam.getUp());

        aux.crs(out).nor();

        float dist = -0.02f * (float) out.len();

        aux.add(cam.getUp()).nor().scl(dist);

        out.add(aux);

    }

    public double getFocusSize() {
        return focus.data[StarBean.I_SIZE];
    }

    // Radius in stars is different!
    public double getRadius() {
        return getSize() * Constants.STAR_SIZE_FACTOR;
    }

    // Radius in stars is different!
    public double getRadius(int i) {
        return getSize(i) * Constants.STAR_SIZE_FACTOR;
    }

    public float getAppmag() {
        return (float) focus.data[StarBean.I_APPMAG];
    }

    public float getAbsmag() {
        return (float) focus.data[StarBean.I_ABSMAG];
    }

    public String getName() {
        if (focus != null)
            return ((StarBean) focus).name;
        else
            return null;
    }

    public long getId() {
        if (focus != null)
            return ((StarBean) focus).id;
        else
            return -1;
    }

    @Override
    public double getMuAlpha() {
        if (focus != null)
            return focus.data[StarBean.I_MUALPHA];
        else
            return 0;
    }

    @Override
    public double getMuDelta() {
        if (focus != null)
            return focus.data[StarBean.I_MUDELTA];
        else
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
        return ((StarBean) pointData.get(i)).size();
    }

    /**
     * Uses whatever precomputed value is in index 8 to compare the values
     * 
     * @author tsagrista
     *
     */
    private class StarGroupComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer i1, Integer i2) {
            return Double.compare(additional[i1], additional[i2]);
        }
    }

    public class UpdaterTask implements Runnable {

        private StarGroup sg;

        public UpdaterTask(StarGroup sg) {
            this.sg = sg;
        }

        @Override
        public void run() {
            sg.updateSorter(GaiaSky.instance.time, GaiaSky.instance.getICamera());
            updating = false;
        }

    }

    @Override
    public void notify(Events event, Object... data) {
        // Super handles FOCUS_CHANGED event
        super.notify(event, data);
        switch (event) {
        case CAMERA_MOTION_UPDATED:
            if (updaterTask != null) {
                final Vector3d currentCameraPos = (Vector3d) data[0];
                long t = TimeUtils.millis() - lastSortTime;
                if (!updating && !workQueue.contains(updaterTask) && this.opacity > 0 && (t > MIN_UPDATE_TIME_MS * 2 || (lastSortCameraPos.dst(currentCameraPos) > CAM_DX_TH && t > MIN_UPDATE_TIME_MS))) {
                    updating = true;
                    pool.execute(updaterTask);
                }
            }
            break;
        case DISPOSE:
            dispose();
            break;
        default:
            break;
        }

    }

    @Override
    public int getCatalogSource() {
        return 1;
    }

    @Override
    public int getHip() {
        if (focus != null && focus.data[StarBean.I_HIP] > 0)
            return (int) focus.data[StarBean.I_HIP];
        return -1;
    }

    @Override
    public String getTycho() {
        if (focus != null && focus.data[StarBean.I_TYC1] > 0)
            return (int) focus.data[StarBean.I_TYC1] + "-" + (int) focus.data[StarBean.I_TYC2] + "-" + (int) focus.data[StarBean.I_TYC3];
        return null;
    }

    @Override
    public long getCandidateId() {
        return ((StarBean) pointData.get(candidateFocusIndex)).id;
    }

    @Override
    public String getCandidateName() {
        return ((StarBean) pointData.get(candidateFocusIndex)).name;
    }

    @Override
    public double getCandidateViewAngleApparent() {
        if (candidateFocusIndex >= 0) {
            StarBean candidate = (StarBean) pointData.get(candidateFocusIndex);
            Vector3d aux = candidate.pos(aux3d1.get());
            ICamera camera = GaiaSky.instance.getICamera();
            double va = (float) ((candidate.radius() / aux.sub(camera.getPos()).len()) / camera.getFovFactor());
            return va * GlobalConf.scene.STAR_BRIGHTNESS;
        } else {
            return -1;
        }
    }

    @Override
    public double getClosestDist() {
        return this.closestDist;
    }

    @Override
    public String getClosestName() {
        return this.closestName;
    }

    @Override
    public double getClosestSize() {
        return this.closestSize;
    }

    @Override
    public Vector3d getClosestPos(Vector3d out) {
        return out.set(this.closestPos);
    }

    @Override
    public float[] getClosestCol() {
        return this.closestCol;
    }

    @Override
    public boolean hasAtmosphere() {
        return false;
    }

    @Override
    public IFocus getFocus(String name) {
        Integer idx = index.get(name);
        if (idx != null)
            candidateFocusIndex = idx;
        return this;
    }

    @Override
    public int getStarCount() {
        return pointData.size;
    }

    @Override
    public void dispose() {
        // Dispose of GPU data
        EventManager.instance.post(Events.DISPOSE_STAR_GROUP_GPU_MESH, this.offset);
    }

    @Override
    protected Vector3d fetchPosition(ParticleBean pb, Vector3d campos, Vector3d dest) {
        StarBean sb = (StarBean) pb;
        Vector3d pm = aux3d2.get().set(sb.pmx(), sb.pmy(), sb.pmz()).scl(currDeltaYears);
        return dest.set(pb.data[0], pb.data[1], pb.data[2]).sub(campos).add(pm);
    }

}
