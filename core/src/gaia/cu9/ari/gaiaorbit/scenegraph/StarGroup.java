package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
import com.badlogic.gdx.utils.TimeUtils;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.data.group.IStarGroupDataProvider;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.render.ILineRenderable;
import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.render.IQuadRenderable;
import gaia.cu9.ari.gaiaorbit.render.system.LineRenderSystem;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.ModelCache;
import gaia.cu9.ari.gaiaorbit.util.Pair;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import net.jafama.FastMath;

/**
 * A particle group which additionally to the xyz position, supports color and
 * magnitude. id x y z pmx pmy pmz appmag absmag col size additional
 * 
 * @author tsagrista
 *
 */
public class StarGroup extends ParticleGroup implements ILineRenderable, IStarFocus, IQuadRenderable, IModelRenderable {

    public static final int SIZE = 19;
    /** INDICES **/
    public static final int I_X = 0;
    public static final int I_Y = 1;
    public static final int I_Z = 2;
    public static final int I_PMX = 3;
    public static final int I_PMY = 4;
    public static final int I_PMZ = 5;
    public static final int I_ID = 6;
    public static final int I_APPMAG = 7;
    public static final int I_ABSMAG = 8;
    public static final int I_COL = 9;
    public static final int I_SIZE = 10;
    public static final int I_HIP = 11;
    public static final int I_TYC1 = 12;
    public static final int I_TYC2 = 13;
    public static final int I_TYC3 = 14;
    public static final int I_MUALPHA = 15;
    public static final int I_MUDELTA = 16;
    public static final int I_RADVEL = 17;
    public static final int I_ADDITIONAL = 18;

    // Camera dx threshold
    private static final double CAM_DX_TH = 100 * Constants.AU_TO_U;
    // Min update time
    private static final double MIN_UPDATE_TIME_MS = 50;
    // Close up stars treated
    private static final int N_CLOSEUP_STARS = 250;
    // Fade in time to prevent pop-ins
    private static final long FADE_IN_MS = 1000;

    /**
     * The name index
     */
    Map<String, Integer> index;
    /**
     * Name of each particle
     */
    List<String> names;

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

    // Sorter daemon
    private SorterThread daemon;

    /** CLOSEST **/
    private Vector3d closestPos;
    private String closestName;
    private double closestDist;
    private double closestSize;
    private float[] closestCol;

    /** Star model **/
    private ModelComponent mc;
    private Matrix4 modelTransform;
    private double modelDist;

    /**
     * Opacity value to prevent pop-ins. Uses the last sort time and FADE_IN_MS
     * to compute an opacity in [0..1]
     **/
    private float popInOpacity;

    private Vector3d lastSortCameraPos;
    private Comparator<Integer> comp;

    public StarGroup() {
        super();
        comp = new StarGroupComparator();
        closestPos = new Vector3d();
        lastSortCameraPos = new Vector3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        closestCol = new float[4];
        EventManager.instance.subscribe(this, Events.CAMERA_MOTION_UPDATED, Events.DISPOSE);
    }

    public void initialize() {
        /** Load data **/
        try {
            Class clazz = Class.forName(provider);
            IStarGroupDataProvider provider = (IStarGroupDataProvider) clazz.newInstance();

            if (factor == null)
                factor = 1d;

            lastSortTime = -1;

            pointData = provider.loadData(datafile, factor);
            index = provider.getIndex();
            names = provider.getNames();

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
         * Index to scene graph
         */
        ISceneGraph sg = GaiaSky.instance.sg;
        if (index != null) {
            Set<String> keys = index.keySet();
            for (String key : keys) {
                sg.addToStringToNode(key, this);
                if (!key.toLowerCase().equals(key))
                    sg.addToStringToNode(key.toLowerCase(), this);
            }
        }

        initModel();

        /**
         * INITIALIZE DAEMON LOADER THREAD
         */
        daemon = new SorterThread(this);
        daemon.setDaemon(true);
        daemon.setName("daemon-star-group-sorter");
        daemon.setPriority(Thread.MIN_PRIORITY);
        daemon.start();
    }

    public void initModel() {
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

    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera, float opacity) {
        super.update(time, parentTransform, camera, opacity);

        // Update closest star
        double[] closestStar = pointData.get(active[0]);
        closestPos.set(closestStar[I_X], closestStar[I_Y], closestStar[I_Z]).sub(camera.getPos());
        closestDist = closestPos.len() - getRadius(active[0]);
        Color c = new Color();
        Color.abgr8888ToColor(c, (float) closestStar[I_COL]);
        closestCol[0] = c.r;
        closestCol[1] = c.g;
        closestCol[2] = c.b;
        closestCol[3] = c.a;
        closestSize = getSize(active[0]);
        closestName = String.valueOf((long) closestStar[I_ID]);
        camera.setClosestStar(this);

        // Model dist
        modelDist = 172.4643429 * getRadius(active[0]);

        // Pop in opacity
        long timeSinceLastSort = TimeUtils.millis() - lastSortTime;
        popInOpacity = MathUtilsd.lint(timeSinceLastSort, 0, FADE_IN_MS, 0, 1);
    }

    /**
     * Updates the additional information array, to use for sorting.
     * 
     * @param camera
     */
    public void updateAdditional(ICamera camera) {
        Vector3d cpos = camera.getPos();
        int n = pointData.size;
        for (int i = 0; i < n; i++) {
            double[] d = pointData.get(i);
            additional[i] = -(((d[I_SIZE] * Constants.STAR_SIZE_FACTOR) / cpos.dst(d[I_X], d[I_Y], d[I_Z])) / camera.getFovFactor()) * GlobalConf.scene.STAR_BRIGHTNESS;
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
        updateAdditional(camera);

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
        if (focusData != null && !focusRendered) {
            renderCloseupStar(1, focusIndex, camera, shader, mesh, thpointTimesFovfactor, thupOverFovfactor, thdownOverFovfactor, alph);
        }

    }

    private void renderCloseupStar(int i, int idx, ICamera camera, ShaderProgram shader, Mesh mesh, double thpointTimesFovfactor, double thupOverFovfactor, double thdownOverFovfactor, float alpha) {
        double[] star = pointData.get(idx);
        double size = getSize(idx);
        double radius = size * Constants.STAR_SIZE_FACTOR;
        Vector3d lpos = aux3d1.get().set(star[I_X], star[I_Y], star[I_Z]).add(camera.getInversePos());
        double distToCamera = lpos.len();
        double viewAngle = (radius / distToCamera) / camera.getFovFactor();
        double viewAngleApparent = viewAngle * GlobalConf.scene.STAR_BRIGHTNESS;
        Color c = new Color();
        Color.abgr8888ToColor(c, (float) star[I_COL]);
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
        computedSize *= GlobalConf.scene.STAR_BRIGHTNESS * 0.6;

        return computedSize;
    }

    /**
     * Model rendering
     */
    @Override
    public void render(ModelBatch modelBatch, float alpha, float t) {
        mc.touch();
        mc.setTransparency(alpha * (float) MathUtilsd.lint(closestDist, modelDist / 50f, modelDist, 1f, 0f));
        float[] col = closestCol;
        ((ColorAttribute) mc.env.get(ColorAttribute.AmbientLight)).color.set(col[0], col[1], col[2], 1f);
        ((FloatAttribute) mc.env.get(FloatAttribute.Shininess)).value = t;
        // Local transform
        mc.instance.transform.idt().translate((float) closestPos.x, (float) closestPos.y, (float) closestPos.z).scl((float) (getRadius(active[0]) * 2d));
        modelBatch.render(mc.instance, mc.env);
    }

    /**
     * Proper motion rendering
     */
    @Override
    public void render(LineRenderSystem renderer, ICamera camera, float alpha) {
        float thpointTimesFovfactor = (float) GlobalConf.scene.STAR_THRESHOLD_POINT * camera.getFovFactor();
        for (int i = N_CLOSEUP_STARS * 20; i >= 0; i--) {
            double[] star = pointData.get(active[i]);
            float radius = (float) (getSize(active[i]) * Constants.STAR_SIZE_FACTOR);
            Vector3d lpos = aux3d1.get().set(star[I_X], star[I_Y], star[I_Z]).sub(camera.getPos());
            float distToCamera = (float) lpos.len();
            float viewAngle = (float) (((radius / distToCamera) / camera.getFovFactor()) * GlobalConf.scene.STAR_BRIGHTNESS);
            if (viewAngle >= thpointTimesFovfactor / GlobalConf.scene.PM_NUM_FACTOR) {

                Vector3d p1 = aux3d1.get().set(star[I_X], star[I_Y], star[I_Z]).sub(camera.getPos());
                Vector3d ppm = aux3d2.get().set(star[I_PMX], star[I_PMY], star[I_PMZ]).scl(GlobalConf.scene.PM_LEN_FACTOR);
                Vector3d p2 = ppm.add(p1);

                // Mualpha -> red channel
                // Mudelta -> green channel
                // Radvel -> blue channel
                // Min value per channel = 0.2
                final double mumin = -80;
                final double mumax = 80;
                final double maxmin = mumax - mumin;
                renderer.addLine(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, (float) ((star[I_MUALPHA] - mumin) / maxmin) * 0.8f + 0.2f, (float) ((star[I_MUDELTA] - mumin) / maxmin) * 0.8f + 0.2f, (float) (star[I_RADVEL]) * 0.8f + 0.2f, alpha * this.opacity);
            }
        }

    }

    /**
     * Label rendering
     */
    @Override
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font3d, BitmapFont font2d, ICamera camera) {
        float thOverFactor = (float) (GlobalConf.scene.STAR_THRESHOLD_POINT / GlobalConf.scene.LABEL_NUMBER_FACTOR / camera.getFovFactor());
        float textScale = 1f;
        for (int i = 0; i < N_CLOSEUP_STARS; i++) {
            double[] star = pointData.get(active[i]);
            float radius = (float) getRadius(active[i]);
            Vector3d lpos = aux3d1.get().set(star[I_X], star[I_Y], star[I_Z]).sub(camera.getPos());
            float distToCamera = (float) lpos.len();
            float viewAngle = (float) (((radius / distToCamera) / camera.getFovFactor()) * GlobalConf.scene.STAR_BRIGHTNESS);

            if (viewAngle >= thOverFactor) {

                textPosition(camera, lpos, distToCamera, radius);
                shader.setUniformf("a_viewAngle", viewAngle);
                shader.setUniformf("a_viewAnglePow", 1f);
                shader.setUniformf("a_thOverFactor", thOverFactor);
                shader.setUniformf("a_thOverFactorScl", camera.getFovFactor());
                float textSize = (float) FastMath.tanh(viewAngle) * distToCamera * 1e5f;
                float alpha = Math.min((float) FastMath.atan(textSize / distToCamera), 1.e-3f);
                textSize = (float) FastMath.tan(alpha) * distToCamera;
                render3DLabel(batch, shader, font3d, camera, names.get(active[i]), lpos, textScale, textSize, textColour(), this.opacity);

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
        return focusData[I_SIZE];
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
        return (float) focusData[I_APPMAG];
    }

    public float getAbsmag() {
        return (float) focusData[I_ABSMAG];
    }

    public String getName() {
        if (focusData != null)
            return names.get(focusIndex);
        else
            return null;
    }

    public long getId() {
        if (focusData != null)
            return (long) focusData[I_ID];
        else
            return -1;
    }

    /**
     * Returns the size of the particle at index i
     * 
     * @param i
     *            The index
     * @return The size
     */
    public double getSize(int i) {
        return pointData.get(i)[I_SIZE];
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

    private class SorterThread extends Thread {
        public boolean awake;
        public boolean running;

        private StarGroup sg;
        public Vector3d currentCameraPos;

        public SorterThread(StarGroup sg) {
            super();
            this.awake = false;
            this.running = true;
            this.sg = sg;
        }

        public void stopExecution() {
            this.running = false;
        }

        @Override
        public void run() {
            while (running) {
                /** ----------- SLEEP UNTIL INTERRUPTED ----------- **/
                try {
                    awake = false;
                    Thread.sleep(Long.MAX_VALUE - 8);
                } catch (InterruptedException e) {
                    // New data!
                    awake = true;
                }

                sg.updateSorter(GaiaSky.instance.time, GaiaSky.instance.getICamera());
                sg.lastSortCameraPos.set(currentCameraPos);
            }
        }

    }

    @Override
    public void notify(Events event, Object... data) {
        // Super handles FOCUS_CHANGED event
        super.notify(event, data);
        switch (event) {
        case CAMERA_MOTION_UPDATED:
            final Vector3d currentCameraPos = (Vector3d) data[0];
            long t = TimeUtils.millis() - lastSortTime;
            if (!daemon.awake && this.opacity > 0 && (t > MIN_UPDATE_TIME_MS * 2 || (lastSortCameraPos.dst(currentCameraPos) > CAM_DX_TH && t > MIN_UPDATE_TIME_MS))) {
                // Update
                daemon.currentCameraPos = currentCameraPos;
                daemon.interrupt();
            }
            break;
        case DISPOSE:
            if (daemon != null)
                daemon.stopExecution();
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
        if (focusData != null && focusData[I_HIP] > 0)
            return (int) focusData[I_HIP];
        return -1;
    }

    @Override
    public String getTycho() {
        if (focusData != null && focusData[I_TYC1] > 0)
            return "TYC " + focusData[I_TYC1] + "-" + focusData[I_TYC1] + "-" + focusData[I_TYC1];
        return null;
    }

    @Override
    public long getCandidateId() {
        return (long) pointData.get(candidateFocusIndex)[I_ID];
    }

    @Override
    public String getCandidateName() {
        return String.valueOf((long) pointData.get(candidateFocusIndex)[I_ID]);
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

}
