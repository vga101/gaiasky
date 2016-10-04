package gaia.cu9.ari.gaiaorbit.render;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.bitfire.postprocessing.filters.Glow;
import com.bitfire.utils.ShaderLoader;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.render.system.AbstractRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.AbstractRenderSystem.RenderSystemRunnable;
import gaia.cu9.ari.gaiaorbit.render.system.FontRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.GalaxyRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.IRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.LineQuadRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.LineRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.ModelBatchRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.PixelRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.QuadRenderSystem;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.ds.Multilist;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.override.AtmosphereGroundShaderProvider;
import gaia.cu9.ari.gaiaorbit.util.override.AtmosphereShaderProvider;

/**
 * Renders a scenegraph.
 * 
 * @author Toni Sagrista
 *
 */
public class SceneGraphRenderer extends AbstractRenderer implements IProcessRenderer, IObserver {

    /** Contains the flags representing each type's visibility **/
    public static boolean[] visible;
    /** Contains the last update time of each of the flags **/
    public static long[] times;
    /** Alpha values for each type **/
    public static float[] alphas;

    public AbstractRenderSystem[] pixelRenderSystems;

    private ShaderProgram starShader, fontShader;

    private int maxTexSize;

    FrameBuffer depthfb;

    /** Render lists for all render groups **/
    public static Map<RenderGroup, Multilist<IRenderable>> render_lists;

    // Two model batches, for front (models), back and atmospheres
    private SpriteBatch spriteBatch, fontBatch;

    private List<IRenderSystem> renderProcesses;

    RenderSystemRunnable blendNoDepthRunnable, blendDepthRunnable;

    /** The particular current scene graph renderer **/
    private ISGR sgr;
    /**
     * Renderer vector, with 0 = normal, 1 = stereoscopic, 2 = FOV, 3 = cubemap
     **/
    private ISGR[] sgrs;

    final int SGR_DEFAULT_IDX = 0, SGR_STEREO_IDX = 1, SGR_FOV_IDX = 2, SGR_CUBEMAP_IDX = 3;

    public SceneGraphRenderer() {
        super();
    }

    @Override
    public void initialize(AssetManager manager) {
        IntBuffer intBuffer = BufferUtils.newIntBuffer(16);
        Gdx.gl20.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, intBuffer);
        maxTexSize = intBuffer.get();
        Logger.info(this.getClass().getSimpleName(), "Max texture size: " + maxTexSize + "^2 pixels");

        ShaderLoader.Pedantic = false;
        ShaderProgram.pedantic = false;
        starShader = new ShaderProgram(Gdx.files.internal("shader/star.vertex.glsl"), Gdx.files.internal("shader/star.fragment.glsl"));
        if (!starShader.isCompiled()) {
            Logger.error(new RuntimeException(), this.getClass().getName() + " - Star shader compilation failed:\n" + starShader.getLog());
        }

        fontShader = new ShaderProgram(Gdx.files.internal("shader/font.vertex.glsl"), Gdx.files.internal("shader/font.fragment.glsl"));
        if (!fontShader.isCompiled()) {
            Logger.error(new RuntimeException(), this.getClass().getName() + " - Font shader compilation failed:\n" + fontShader.getLog());
        }

        int numLists = GlobalConf.performance.MULTITHREADING ? GlobalConf.performance.NUMBER_THREADS() : 1;
        RenderGroup[] renderGroups = RenderGroup.values();
        render_lists = new HashMap<RenderGroup, Multilist<IRenderable>>(renderGroups.length);
        for (RenderGroup rg : renderGroups) {
            render_lists.put(rg, new Multilist<IRenderable>(numLists, 100));
        }

        ShaderProvider sp = new AtmosphereGroundShaderProvider(Gdx.files.internal("shader/default.vertex.glsl"), Gdx.files.internal("shader/default.fragment.glsl"));
        ShaderProvider spnormal = Constants.webgl ? sp : new AtmosphereGroundShaderProvider(Gdx.files.internal("shader/normal.vertex.glsl"), Gdx.files.internal("shader/normal.fragment.glsl"));
        ShaderProvider spatm = new AtmosphereShaderProvider(Gdx.files.internal("shader/atm.vertex.glsl"), Gdx.files.internal("shader/atm.fragment.glsl"));
        ShaderProvider spsurface = new DefaultShaderProvider(Gdx.files.internal("shader/default.vertex.glsl"), Gdx.files.internal("shader/starsurface.fragment.glsl"));

        RenderableSorter noSorter = new RenderableSorter() {
            @Override
            public void sort(Camera camera, Array<Renderable> renderables) {
                // Does nothing
            }
        };

        ModelBatch modelBatchB = new ModelBatch(sp, noSorter);
        ModelBatch modelBatchF = Constants.webgl ? modelBatchB : new ModelBatch(spnormal, noSorter);
        ModelBatch modelBatchAtm = new ModelBatch(spatm, noSorter);
        ModelBatch modelBatchS = new ModelBatch(spsurface, noSorter);

        // Sprites
        spriteBatch = GlobalResources.spriteBatch;
        spriteBatch.enableBlending();

        // Font batch
        fontBatch = new SpriteBatch(1000, fontShader);
        fontBatch.enableBlending();

        ComponentType[] comps = ComponentType.values();

        // Set reference
        visible = GlobalConf.scene.VISIBILITY;

        times = new long[comps.length];
        alphas = new float[comps.length];
        for (int i = 0; i < comps.length; i++) {
            times[i] = -20000l;
            alphas[i] = 1f;
        }

        /**
         * DEPTH BUFFER BITS
         */

        intBuffer.rewind();
        Gdx.gl.glGetIntegerv(GL20.GL_DEPTH_BITS, intBuffer);
        Logger.info(this.getClass().getSimpleName(), "Depth buffer size: " + intBuffer.get() + " bits");

        /**
         * INITIALIZE SGRs
         */
        sgrs = new ISGR[4];
        sgrs[SGR_DEFAULT_IDX] = new SGR();
        sgrs[SGR_STEREO_IDX] = new SGRStereoscopic();
        sgrs[SGR_FOV_IDX] = new SGRFov();
        sgrs[SGR_CUBEMAP_IDX] = new SGRCubemap();
        sgr = null;

        /**
         *
         * ======= INITIALIZE RENDER COMPONENTS =======
         *
         **/
        pixelRenderSystems = new AbstractRenderSystem[3];

        renderProcesses = new ArrayList<IRenderSystem>();

        blendNoDepthRunnable = new RenderSystemRunnable() {
            @Override
            public void run(AbstractRenderSystem renderSystem, List<IRenderable> renderables, ICamera camera) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
                Gdx.gl.glDepthMask(false);
            }
        };
        blendDepthRunnable = new RenderSystemRunnable() {
            @Override
            public void run(AbstractRenderSystem renderSystem, List<IRenderable> renderables, ICamera camera) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
                Gdx.gl.glDepthMask(true);
            }
        };

        int priority = 1;

        // MODEL BACK
        AbstractRenderSystem modelBackProc = new ModelBatchRenderSystem(RenderGroup.MODEL_B, priority++, alphas, modelBatchB, false);
        modelBackProc.setPreRunnable(blendNoDepthRunnable);
        modelBackProc.setPostRunnable(new RenderSystemRunnable() {
            @Override
            public void run(AbstractRenderSystem renderSystem, List<IRenderable> renderables, ICamera camera) {
                // This always goes at the back, clear depth buffer
                Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
            }
        });

        // POINTS
        AbstractRenderSystem pixelProc = new PixelRenderSystem(RenderGroup.POINT, 0, alphas);
        pixelProc.setPreRunnable(blendNoDepthRunnable);

        // ANNOTATIONS
        AbstractRenderSystem annotationsProc = new FontRenderSystem(RenderGroup.MODEL_B_ANNOT, priority++, alphas, spriteBatch);
        annotationsProc.setPreRunnable(blendNoDepthRunnable);
        annotationsProc.setPostRunnable(new RenderSystemRunnable() {
            @Override
            public void run(AbstractRenderSystem renderSystem, List<IRenderable> renderables, ICamera camera) {
                // This always goes at the back, clear depth buffer
                Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
            }
        });

        // SHADER STARS
        AbstractRenderSystem shaderBackProc = new QuadRenderSystem(RenderGroup.SHADER, priority++, alphas, starShader, true);
        shaderBackProc.setPreRunnable(blendNoDepthRunnable);
        shaderBackProc.setPostRunnable(new RenderSystemRunnable() {

            private float[] positions = new float[Glow.N * 2];
            private float[] viewAngles = new float[Glow.N];
            private float[] colors = new float[Glow.N * 3];
            private Vector3 auxv = new Vector3();

            @Override
            public void run(AbstractRenderSystem renderSystem, List<IRenderable> renderables, ICamera camera) {
                int size = renderables.size();
                if (PostProcessorFactory.instance.getPostProcessor().isLightScatterEnabled()) {
                    // Compute light positions for light scattering or light glow
                    int lightIndex = 0;
                    float angleEdgeDeg = camera.getAngleEdge() * MathUtils.radDeg;
                    for (int i = size - 1; i >= 0; i--) {
                        IRenderable s = renderables.get(i);
                        if (s instanceof Particle) {
                            Particle p = (Particle) s;
                            if (!Constants.webgl && lightIndex < Glow.N && GaiaSky.instance.cam.getDirection().angle(p.transform.position) < angleEdgeDeg) {
                                camera.getCamera().project(p.transform.getTranslationf(auxv));
                                // Here we **need** to use Gdx.graphics.getWidth/Height() because we use camera.project() which uses screen coordinates only
                                positions[lightIndex * 2] = auxv.x / Gdx.graphics.getWidth();
                                positions[lightIndex * 2 + 1] = auxv.y / Gdx.graphics.getHeight();
                                viewAngles[lightIndex] = p.viewAngleApparent;
                                colors[lightIndex * 3] = p.cc[0];
                                colors[lightIndex * 3 + 1] = p.cc[1];
                                colors[lightIndex * 3 + 2] = p.cc[2];
                                lightIndex++;
                            }
                        }
                    }
                    EventManager.instance.post(Events.LIGHT_POS_2D_UPDATED, lightIndex, positions, viewAngles, colors);
                } else {
                    EventManager.instance.post(Events.LIGHT_POS_2D_UPDATED, 0, positions, viewAngles, colors);
                }
            }

        });

        // LINES
        AbstractRenderSystem lineProc = getLineRenderSystem();

        // SHADER SSO
        AbstractRenderSystem shaderFrontProc = new QuadRenderSystem(RenderGroup.SHADER_F, priority++, alphas, starShader, false);
        shaderFrontProc.setPreRunnable(blendNoDepthRunnable);

        // GALAXY
        AbstractRenderSystem galaxyProc = new GalaxyRenderSystem(RenderGroup.GALAXY, priority++, alphas, modelBatchF);
        galaxyProc.setPreRunnable(blendNoDepthRunnable);

        // MODEL FRONT
        AbstractRenderSystem modelFrontProc = new ModelBatchRenderSystem(RenderGroup.MODEL_F, priority++, alphas, modelBatchF, false);
        modelFrontProc.setPreRunnable(blendDepthRunnable);

        // MODEL STARS
        AbstractRenderSystem modelStarsProc = new ModelBatchRenderSystem(RenderGroup.MODEL_S, priority++, alphas, modelBatchS, false);
        modelStarsProc.setPreRunnable(blendDepthRunnable);

        // LABELS
        AbstractRenderSystem labelsProc = new FontRenderSystem(RenderGroup.LABEL, priority++, alphas, fontBatch, fontShader);
        labelsProc.setPreRunnable(blendDepthRunnable);

        // MODEL ATMOSPHERE
        AbstractRenderSystem modelAtmProc = new ModelBatchRenderSystem(RenderGroup.MODEL_F_ATM, priority++, alphas, modelBatchAtm, true) {
            @Override
            protected float getAlpha(IRenderable s) {
                return alphas[ComponentType.Atmospheres.ordinal()] * (float) Math.pow(alphas[s.getComponentType().ordinal()], 2);
            }

            @Override
            protected boolean mustRender() {
                return alphas[ComponentType.Atmospheres.ordinal()] * alphas[ComponentType.Planets.ordinal()] > 0;
            }
        };
        modelAtmProc.setPreRunnable(blendDepthRunnable);

        // Add components to set
        renderProcesses.add(pixelProc);
        renderProcesses.add(modelBackProc);
        renderProcesses.add(annotationsProc);
        renderProcesses.add(shaderBackProc);
        renderProcesses.add(shaderFrontProc);
        renderProcesses.add(modelFrontProc);
        renderProcesses.add(lineProc);
        renderProcesses.add(galaxyProc);
        renderProcesses.add(modelStarsProc);
        renderProcesses.add(labelsProc);
        renderProcesses.add(modelAtmProc);

        EventManager.instance.subscribe(this, Events.TOGGLE_VISIBILITY_CMD, Events.PIXEL_RENDERER_UPDATE, Events.TOGGLE_STEREOSCOPIC_INFO, Events.CAMERA_MODE_CMD, Events.CUBEMAP360_CMD);

    }

    private void initSGR(ICamera camera) {
        if (camera.getNCameras() > 1) {
            // FOV mode
            sgr = sgrs[SGR_FOV_IDX];
        } else if (GlobalConf.program.STEREOSCOPIC_MODE) {
            // Stereoscopic mode
            sgr = sgrs[SGR_STEREO_IDX];
        } else if (GlobalConf.program.CUBEMAP360_MODE) {
            // 360 mode: cube map -> equirectangular map
            sgr = sgrs[SGR_CUBEMAP_IDX];
        } else {
            // Default mode
            sgr = sgrs[SGR_DEFAULT_IDX];
        }
    }

    @Override
    public void render(ICamera camera, float t, int rw, int rh, FrameBuffer fb, PostProcessBean ppb) {

        if (sgr == null)
            initSGR(camera);

        sgr.render(this, camera, t, rw, rh, fb, ppb);

        //        fb.getColorBufferTexture()
        //        if (fb != null) {
        //            int depthId = fb.getDepthBufferHandle();
        //            Gdx.gl.glBindFramebuffer(GL20.GL_FRAMEBUFFER, depthId);
        //            int texid = Gdx.gl.glGenTexture();
        //            Gdx.gl.glBindTexture(GL20.GL_TEXTURE_2D, texid);
        //            Gdx.gl.glTexImage2D(GL20.GL_TEXTURE_2D, 0, GL20.GL_RGB, rw, rh, 0, GL20.GL_RGB, GL20.GL_UNSIGNED_BYTE, null);
        //
        //            // Poor filtering. Needed !
        //            Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MAG_FILTER, GL20.GL_NEAREST);
        //            Gdx.gl.glTexParameteri(GL20.GL_TEXTURE_2D, GL20.GL_TEXTURE_MIN_FILTER, GL20.GL_NEAREST);
        //
        //        }
    }

    /**
     * Renders the scene
     * 
     * @param camera
     *            The camera to use.
     * @param t
     *            The time in seconds since the start.
     * @param rc
     *            The render context.
     */
    public void renderScene(ICamera camera, float t, RenderContext rc) {
        // Update time difference since last update
        for (ComponentType ct : ComponentType.values()) {
            alphas[ct.ordinal()] = calculateAlpha(ct, t);
        }

        EventManager.instance.post(Events.DEBUG1, "quads: " + (render_lists.get(RenderGroup.SHADER).size() + render_lists.get(RenderGroup.SHADER_F).size()) + ", points: " + render_lists.get(RenderGroup.POINT).size() + ", labels: " + render_lists.get(RenderGroup.LABEL).size());

        int size = renderProcesses.size();
        for (int i = 0; i < size; i++) {
            IRenderSystem process = renderProcesses.get(i);
            List<IRenderable> l = render_lists.get(process.getRenderGroup()).toList();
            process.render(l, camera, t, rc);
        }

    }

    /**
     * This must be called when all the rendering for the current frame has
     * finished.
     */
    public void clearLists() {
        for (RenderGroup rg : RenderGroup.values()) {
            render_lists.get(rg).clear();
        }
    }

    public String[] getRenderComponents() {
        ComponentType[] comps = ComponentType.values();
        String[] res = new String[comps.length];
        int i = 0;
        for (ComponentType comp : comps) {
            res[i++] = comp.getName();
        }
        return res;
    }

    public boolean isOn(ComponentType comp) {
        return visible[comp.ordinal()] || alphas[comp.ordinal()] > 0;
    }

    @Override
    public void notify(Events event, final Object... data) {
        switch (event) {
        case TOGGLE_VISIBILITY_CMD:
            int idx = ComponentType.getFromName((String) data[0]).ordinal();
            if (data.length == 3) {
                // We have the boolean
                visible[idx] = (boolean) data[2];
                times[idx] = (long) (GaiaSky.instance.getT() * 1000f);
            } else {
                // Only toggle
                visible[idx] = !visible[idx];
                times[idx] = (long) (GaiaSky.instance.getT() * 1000f);
            }
            break;

        case PIXEL_RENDERER_UPDATE:
            Gdx.app.postRunnable(new Runnable() {

                @Override
                public void run() {
                    AbstractRenderSystem.POINT_UPDATE_FLAG = true;
                    //                    updatePixelRenderSystem();
                }

            });
            break;
        case TOGGLE_STEREOSCOPIC_INFO:
            boolean stereo = (Boolean) data[0];
            if (stereo)
                sgr = sgrs[SGR_STEREO_IDX];
            else {
                sgr = sgrs[SGR_DEFAULT_IDX];
            }
            break;
        case CUBEMAP360_CMD:
            boolean cubemap = (Boolean) data[0];
            if (cubemap)
                sgr = sgrs[SGR_CUBEMAP_IDX];
            else
                sgr = sgrs[SGR_DEFAULT_IDX];
            break;
        case CAMERA_MODE_CMD:
            CameraMode cm = (CameraMode) data[0];
            if (cm.isGaiaFov())
                sgr = sgrs[SGR_FOV_IDX];
            else {
                if (GlobalConf.program.STEREOSCOPIC_MODE)
                    sgr = sgrs[SGR_STEREO_IDX];
                else if (GlobalConf.program.CUBEMAP360_MODE)
                    sgr = sgrs[SGR_CUBEMAP_IDX];
                else
                    sgr = sgrs[SGR_DEFAULT_IDX];

            }
            break;
        }
    }

    /**
     * Computes the alpha for the given component type.
     * 
     * @param type
     *            The component type.
     * @param now
     *            The current time in seconds.
     * @return The alpha value.
     */
    private float calculateAlpha(ComponentType type, float t) {
        long diff = (long) (t * 1000f) - times[type.ordinal()];
        if (diff > GlobalConf.scene.OBJECT_FADE_MS) {
            if (visible[type.ordinal()]) {
                alphas[type.ordinal()] = 1;
            } else {
                alphas[type.ordinal()] = 0;
            }
            return alphas[type.ordinal()];
        } else {
            return visible[type.ordinal()] ? MathUtilsd.lint(diff, 0, GlobalConf.scene.OBJECT_FADE_MS, 0, 1) : MathUtilsd.lint(diff, 0, GlobalConf.scene.OBJECT_FADE_MS, 1, 0);
        }
    }

    public void resize(final int w, final int h) {

        for (IRenderSystem rendSys : renderProcesses) {
            rendSys.resize(w, h);
        }
    }

    public void dispose() {
        for (ISGR sgr : sgrs) {
            if (sgr != null)
                sgr.dispose();
        }
    }

    private AbstractRenderSystem getLineRenderSystem() {
        AbstractRenderSystem sys = null;
        if (GlobalConf.scene.isNormalLineRenderer()) {
            // Normal
            sys = new LineRenderSystem(RenderGroup.LINE, 0, alphas);
            sys.setPreRunnable(blendDepthRunnable);
        } else {
            // Quad
            sys = new LineQuadRenderSystem(RenderGroup.LINE, 0, alphas);
            sys.setPreRunnable(blendDepthRunnable);
        }
        return sys;
    }

}
