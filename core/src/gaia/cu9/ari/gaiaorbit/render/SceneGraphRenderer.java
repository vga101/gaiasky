package gaia.cu9.ari.gaiaorbit.render;

import java.nio.IntBuffer;
import java.util.HashMap;
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
import gaia.cu9.ari.gaiaorbit.assets.AtmosphereGroundShaderProviderLoader.AtmosphereGroundShaderProviderParameter;
import gaia.cu9.ari.gaiaorbit.assets.AtmosphereShaderProviderLoader.AtmosphereShaderProviderParameter;
import gaia.cu9.ari.gaiaorbit.assets.DefaultShaderProviderLoader.DefaultShaderProviderParameter;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.render.system.AbstractRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.AbstractRenderSystem.RenderSystemRunnable;
import gaia.cu9.ari.gaiaorbit.render.system.BillboardSpriteRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.BillboardStarRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.FontRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.IRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.LineQuadRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.LineRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.MilkyWayRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.ModelBatchRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.ParticleGroupRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.PixelRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.ShapeRenderSystem;
import gaia.cu9.ari.gaiaorbit.render.system.StarGroupRenderSystem;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.ComponentTypes;
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
    public static ComponentTypes visible;
    /** Contains the last update time of each of the flags **/
    public static long[] times;
    /** Alpha values for each type **/
    public static float[] alphas;

    public AbstractRenderSystem[] pixelRenderSystems;

    private ShaderProgram starShader, galaxyShader, fontShader, spriteShader;

    private int maxTexSize;

    FrameBuffer depthfb;

    /** Render lists for all render groups **/
    public static Map<RenderGroup, Multilist<IRenderable>> render_lists;

    // Two model batches, for front (models), back and atmospheres
    private SpriteBatch spriteBatch, fontBatch;

    private Array<IRenderSystem> renderProcesses;

    RenderSystemRunnable blendNoDepthRunnable, blendDepthRunnable;

    /** The particular current scene graph renderer **/
    private ISGR sgr;
    /**
     * Renderers vector, with 0 = normal, 1 = stereoscopic, 2 = FOV, 3 = cubemap
     **/
    private ISGR[] sgrs;
    // Indexes
    final int SGR_DEFAULT_IDX = 0, SGR_STEREO_IDX = 1, SGR_FOV_IDX = 2, SGR_CUBEMAP_IDX = 3;

    public SceneGraphRenderer() {
        super();
    }

    @Override
    public void initialize(AssetManager manager) {
        ShaderLoader.Pedantic = false;
        ShaderProgram.pedantic = false;

        /** LOAD SHADER PROGRAMS WITH ASSET MANAGER **/
        manager.load("shader/star.vertex.glsl", ShaderProgram.class);
        manager.load("shader/gal.vertex.glsl", ShaderProgram.class);
        manager.load("shader/font.vertex.glsl", ShaderProgram.class);
        manager.load("shader/sprite.vertex.glsl", ShaderProgram.class);
        manager.load("atmgrounddefault", AtmosphereGroundShaderProvider.class, new AtmosphereGroundShaderProviderParameter("shader/default.vertex.glsl", "shader/default.fragment.glsl"));
        manager.load("spsurface", DefaultShaderProvider.class, new DefaultShaderProviderParameter("shader/default.vertex.glsl", "shader/starsurface.fragment.glsl"));
        manager.load("spbeam", DefaultShaderProvider.class, new DefaultShaderProviderParameter("shader/default.vertex.glsl", "shader/beam.fragment.glsl"));
        manager.load("atm", AtmosphereShaderProvider.class, new AtmosphereShaderProviderParameter("shader/atm.vertex.glsl", "shader/atm.fragment.glsl"));
        if (!Constants.webgl) {
            manager.load("atmground", AtmosphereGroundShaderProvider.class, new AtmosphereGroundShaderProviderParameter("shader/normal.vertex.glsl", "shader/normal.fragment.glsl"));
        }

        pixelRenderSystems = new AbstractRenderSystem[3];

        renderProcesses = new Array<IRenderSystem>();

        blendNoDepthRunnable = new RenderSystemRunnable() {
            @Override
            public void run(AbstractRenderSystem renderSystem, Array<IRenderable> renderables, ICamera camera) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
                Gdx.gl.glDepthMask(false);
            }
        };
        blendDepthRunnable = new RenderSystemRunnable() {
            @Override
            public void run(AbstractRenderSystem renderSystem, Array<IRenderable> renderables, ICamera camera) {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
                Gdx.gl.glDepthMask(true);
            }
        };

    }

    public void doneLoading(AssetManager manager) {
        IntBuffer intBuffer = BufferUtils.newIntBuffer(16);
        Gdx.gl20.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, intBuffer);
        maxTexSize = intBuffer.get();
        Logger.info(this.getClass().getSimpleName(), "Max texture size: " + maxTexSize + "^2 pixels");

        /**
         * STAR SHADER
         */
        starShader = manager.get("shader/star.vertex.glsl");
        if (!starShader.isCompiled()) {
            Logger.error(new RuntimeException(), this.getClass().getName() + " - Star shader compilation failed:\n" + starShader.getLog());
        }
        /**
         * GALAXY SHADER
         */
        galaxyShader = manager.get("shader/gal.vertex.glsl");
        if (!galaxyShader.isCompiled()) {
            Logger.error(new RuntimeException(), this.getClass().getName() + " - Galaxy shader compilation failed:\n" + galaxyShader.getLog());
        }

        /**
         * FONT SHADER
         */
        fontShader = manager.get("shader/font.vertex.glsl");
        if (!fontShader.isCompiled()) {
            Logger.error(new RuntimeException(), this.getClass().getName() + " - Font shader compilation failed:\n" + fontShader.getLog());
        }

        /**
         * SPRITE SHADER
         */
        spriteShader = manager.get("shader/sprite.vertex.glsl");
        if (!spriteShader.isCompiled()) {
            Logger.error(new RuntimeException(), this.getClass().getName() + " - Sprite shader compilation failed:\n" + spriteShader.getLog());
        }

        int numLists = GlobalConf.performance.MULTITHREADING ? GlobalConf.performance.NUMBER_THREADS() : 1;
        RenderGroup[] renderGroups = RenderGroup.values();
        render_lists = new HashMap<RenderGroup, Multilist<IRenderable>>(renderGroups.length);
        for (RenderGroup rg : renderGroups) {
            render_lists.put(rg, new Multilist<IRenderable>(numLists, 40000));
        }

        ShaderProvider sp = manager.get("atmgrounddefault");
        ShaderProvider spnormal = Constants.webgl ? sp : manager.get("atmground");
        ShaderProvider spatm = manager.get("atm");
        ShaderProvider spsurface = manager.get("spsurface");
        ShaderProvider spbeam = manager.get("spbeam");

        RenderableSorter noSorter = new RenderableSorter() {
            @Override
            public void sort(Camera camera, Array<Renderable> renderables) {
                // Does nothing
            }
        };

        ModelBatch modelBatchFB = new ModelBatch(sp, noSorter);
        ModelBatch modelBatchF = Constants.webgl ? new ModelBatch(sp, noSorter) : new ModelBatch(spnormal, noSorter);
        ModelBatch modelBatchAtm = new ModelBatch(spatm, noSorter);
        ModelBatch modelBatchS = new ModelBatch(spsurface, noSorter);
        ModelBatch modelBatchBeam = new ModelBatch(spbeam, noSorter);

        // Sprites
        spriteBatch = GlobalResources.spriteBatch;
        spriteBatch.enableBlending();

        // Font batch
        fontBatch = new SpriteBatch(1000, fontShader);
        fontBatch.enableBlending();

        ComponentType[] comps = ComponentType.values();

        // Set reference
        visible = new ComponentTypes();
        for (int i = 0; i < GlobalConf.scene.VISIBILITY.length; i++) {
            if (GlobalConf.scene.VISIBILITY[i]) {
                visible.set(ComponentType.values()[i].ordinal());
            }

        }

        times = new long[comps.length];
        alphas = new float[comps.length];
        for (int i = 0; i < comps.length; i++) {
            times[i] = -20000l;
            alphas[i] = 1f;
        }

        /**
         * DEPTH BUFFER BITS
         */

        intBuffer.clear();
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

        int priority = 0;

        // POINTS
        AbstractRenderSystem pixelStarProc = new PixelRenderSystem(RenderGroup.POINT_STAR, priority++, alphas, ComponentType.Stars);
        pixelStarProc.setPreRunnable(blendNoDepthRunnable);

        // MODEL FRONT-BACK - NO CULL FACE
        AbstractRenderSystem modelFrontBackProc = new ModelBatchRenderSystem(RenderGroup.MODEL_FB, priority++, alphas, modelBatchFB, false);
        modelFrontBackProc.setPreRunnable(blendNoDepthRunnable);
        modelFrontBackProc.setPostRunnable(new RenderSystemRunnable() {
            @Override
            public void run(AbstractRenderSystem renderSystem, Array<IRenderable> renderables, ICamera camera) {
                // This always goes at the back, clear depth buffer
                Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
            }
        });

        // VOLUMETRIC CLOUDS
        //        AbstractRenderSystem cloudsProc = new VolumeCloudsRenderSystem(priority++, alphas);
        //        cloudsProc.setPreRunnable(blendNoDepthRunnable);

        // ANNOTATIONS
        AbstractRenderSystem annotationsProc = new FontRenderSystem(RenderGroup.MODEL_B_ANNOT, priority++, alphas, spriteBatch);
        annotationsProc.setPreRunnable(blendNoDepthRunnable);
        annotationsProc.setPostRunnable(new RenderSystemRunnable() {
            @Override
            public void run(AbstractRenderSystem renderSystem, Array<IRenderable> renderables, ICamera camera) {
                // This always goes at the back, clear depth buffer
                Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
            }
        });

        // BILLBOARD STARS
        AbstractRenderSystem billboardStarsProc = new BillboardStarRenderSystem(RenderGroup.BILLBOARD_STAR, priority++, alphas, starShader, true, "img/star_glow_s.png", ComponentType.Stars.ordinal());
        billboardStarsProc.setPreRunnable(blendNoDepthRunnable);
        billboardStarsProc.setPostRunnable(new RenderSystemRunnable() {

            private float[] positions = new float[Glow.N * 2];
            private float[] viewAngles = new float[Glow.N];
            private float[] colors = new float[Glow.N * 3];
            private Vector3 auxv = new Vector3();
            private Vector3 auxv2 = new Vector3();

            @Override
            public void run(AbstractRenderSystem renderSystem, Array<IRenderable> renderables, ICamera camera) {
                int size = renderables.size;
                if (PostProcessorFactory.instance.getPostProcessor().isLightScatterEnabled() && Particle.renderOn) {
                    // Compute light positions for light scattering or light
                    // glow
                    int lightIndex = 0;
                    float angleEdgeDeg = camera.getAngleEdge() * MathUtils.radDeg;
                    for (int i = size - 1; i >= 0; i--) {
                        IRenderable s = renderables.get(i);
                        if (s instanceof Particle) {
                            Particle p = (Particle) s;
                            if (!Constants.webgl && lightIndex < Glow.N && (GlobalConf.program.CUBEMAP360_MODE || GaiaSky.instance.cam.getDirection().angle(p.transform.position) < angleEdgeDeg)) {
                                Vector3 pos3 = p.transform.getTranslationf(auxv);
                                pos3.sub(camera.getShift().put(auxv2));
                                camera.getCamera().project(pos3);
                                // Here we **need** to use
                                // Gdx.graphics.getWidth/Height() because we use
                                // camera.project() which uses screen
                                // coordinates only
                                positions[lightIndex * 2] = auxv.x / Gdx.graphics.getWidth();
                                positions[lightIndex * 2 + 1] = auxv.y / Gdx.graphics.getHeight();
                                viewAngles[lightIndex] = (float) p.viewAngleApparent;
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

        // BILLBOARD GALAXIES
        AbstractRenderSystem billboardGalaxiesProc = new BillboardStarRenderSystem(RenderGroup.BILLBOARD_GAL, priority++, alphas, galaxyShader, true, "img/static.jpg", ComponentType.Galaxies.ordinal());
        billboardGalaxiesProc.setPreRunnable(blendNoDepthRunnable);

        // BILLBOARD SPRITES
        AbstractRenderSystem billboardSpritesProc = new BillboardSpriteRenderSystem(RenderGroup.BILLBOARD_SPRITE, priority++, alphas, spriteShader, ComponentType.Others.ordinal());
        billboardSpritesProc.setPreRunnable(blendNoDepthRunnable);

        // LINES
        AbstractRenderSystem lineProc = getLineRenderSystem();

        // MODEL FRONT
        AbstractRenderSystem modelFrontProc = new ModelBatchRenderSystem(RenderGroup.MODEL_F, priority++, alphas, modelBatchF, false);
        modelFrontProc.setPreRunnable(blendDepthRunnable);

        // MODEL BEAM
        AbstractRenderSystem modelBeamProc = new ModelBatchRenderSystem(RenderGroup.MODEL_BEAM, priority++, alphas, modelBatchBeam, false);
        modelBeamProc.setPreRunnable(blendDepthRunnable);

        // GALAXY
        AbstractRenderSystem galaxyProc = new MilkyWayRenderSystem(RenderGroup.GALAXY, priority++, alphas, modelBatchFB);
        galaxyProc.setPreRunnable(blendNoDepthRunnable);

        // PARTICLE GROUP
        AbstractRenderSystem particleGroupProc = new ParticleGroupRenderSystem(RenderGroup.PARTICLE_GROUP, priority++, alphas);
        particleGroupProc.setPreRunnable(blendNoDepthRunnable);

        // STAR GROUP
        AbstractRenderSystem starGroupProc = new StarGroupRenderSystem(RenderGroup.STAR_GROUP, priority++, alphas);
        starGroupProc.setPreRunnable(blendNoDepthRunnable);

        // MODEL STARS
        AbstractRenderSystem modelStarsProc = new ModelBatchRenderSystem(RenderGroup.MODEL_S, priority++, alphas, modelBatchS, false);
        modelStarsProc.setPreRunnable(blendDepthRunnable);

        // LABELS
        AbstractRenderSystem labelsProc = new FontRenderSystem(RenderGroup.LABEL, priority++, alphas, fontBatch, fontShader);
        labelsProc.setPreRunnable(blendNoDepthRunnable);

        // BILLBOARD SSO
        AbstractRenderSystem billboardSSOProc = new BillboardStarRenderSystem(RenderGroup.BILLBOARD_SSO, priority++, alphas, starShader, false, "img/sso.png", -1);
        billboardSSOProc.setPreRunnable(blendDepthRunnable);

        // MODEL ATMOSPHERE
        AbstractRenderSystem modelAtmProc = new ModelBatchRenderSystem(RenderGroup.MODEL_F_ATM, priority++, alphas, modelBatchAtm, true) {
            @Override
            public float getAlpha(IRenderable s) {
                return alphas[ComponentType.Atmospheres.ordinal()] * (float) Math.pow(alphas[s.getComponentType().getFirstOrdinal()], 2);
            }

            @Override
            protected boolean mustRender() {
                return alphas[ComponentType.Atmospheres.ordinal()] * alphas[ComponentType.Planets.ordinal()] > 0;
            }
        };
        modelAtmProc.setPreRunnable(blendDepthRunnable);
        modelAtmProc.setPostRunnable(new RenderSystemRunnable() {
            @Override
            public void run(AbstractRenderSystem renderSystem, Array<IRenderable> renderables, ICamera camera) {
                // Clear depth buffer before rendering things up close
                Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
            }
        });

        // SHAPES
        AbstractRenderSystem shapeProc = new ShapeRenderSystem(RenderGroup.SHAPE, priority++, alphas);
        shapeProc.setPreRunnable(blendNoDepthRunnable);

        // MODEL CLOSE UP
        //        AbstractRenderSystem modelCloseUpProc = new ModelBatchRenderSystem(RenderGroup.MODEL_CLOSEUP, priority++, alphas, modelBatchCloseUp, false);
        //        modelCloseUpProc.setPreRunnable(blendDepthRunnable);

        // Add components to set
        renderProcesses.add(modelFrontBackProc);
        renderProcesses.add(pixelStarProc);
        renderProcesses.add(starGroupProc);
        // renderProcesses.add(cloudsProc);
        renderProcesses.add(annotationsProc);
        renderProcesses.add(particleGroupProc);

        // Billboards for galaxies and stars
        renderProcesses.add(billboardGalaxiesProc);
        renderProcesses.add(billboardStarsProc);
        renderProcesses.add(galaxyProc);

        // Billboard for sprites
        renderProcesses.add(billboardSpritesProc);

        renderProcesses.add(modelFrontProc);

        renderProcesses.add(modelBeamProc);
        renderProcesses.add(lineProc);
        renderProcesses.add(labelsProc);
        renderProcesses.add(billboardSSOProc);

        renderProcesses.add(modelStarsProc);
        renderProcesses.add(modelAtmProc);
        renderProcesses.add(shapeProc);
        // renderProcesses.add(modelCloseUpProc);

        EventManager.instance.subscribe(this, Events.TOGGLE_VISIBILITY_CMD, Events.PIXEL_RENDERER_UPDATE, Events.LINE_RENDERER_UPDATE, Events.TOGGLE_STEREOSCOPIC_INFO, Events.CAMERA_MODE_CMD, Events.CUBEMAP360_CMD);

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
    public void render(ICamera camera, double t, int rw, int rh, FrameBuffer fb, PostProcessBean ppb) {
        if (sgr == null)
            initSGR(camera);

        sgr.render(this, camera, t, rw, rh, fb, ppb);
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
    public void renderScene(ICamera camera, double t, RenderingContext rc) {
        // Update time difference since last update
        for (ComponentType ct : ComponentType.values()) {
            alphas[ct.ordinal()] = calculateAlpha(ct, t);
        }

        int size = renderProcesses.size;
        for (int i = 0; i < size; i++) {
            IRenderSystem process = renderProcesses.get(i);
            // If we have no render group, this means all the info is already in
            // the render system. No lists needed
            if (process.getRenderGroup() != null) {
                Array<IRenderable> l = render_lists.get(process.getRenderGroup()).toList();
                process.render(l, camera, t, rc);
            } else {
                process.render(null, camera, t, rc);
            }
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
        return visible.get(comp.ordinal()) || alphas[comp.ordinal()] > 0;
    }

    /**
     * TODO Make this faster!
     * 
     * @param comp
     * @return
     */
    public boolean isOn(ComponentTypes comp) {
        boolean allon = comp.allSetLike(visible);

        if (!allon) {
            allon = true;
            for (int i = comp.nextSetBit(0); i >= 0; i = comp.nextSetBit(i + 1)) {
                // operate on index i here
                allon = allon && alphas[i] > 0;
                if (i == Integer.MAX_VALUE) {
                    break; // or (i+1) would overflow
                }
            }
        }
        return allon;
    }

    public boolean isOn(int ordinal) {
        return visible.get(ordinal) || alphas[ordinal] > 0;
    }

    @Override
    public void notify(Events event, final Object... data) {
        switch (event) {
        case TOGGLE_VISIBILITY_CMD:
            ComponentType ct = ComponentType.getFromKey((String) data[0]);
            int idx = ct.ordinal();
            if (data.length == 3) {
                // We have the boolean
                if ((boolean) data[2])
                    visible.set(ct.ordinal());
                else
                    visible.clear(ct.ordinal());
                times[idx] = (long) (GaiaSky.instance.getT() * 1000f);
            } else {
                // Only toggle
                visible.flip(ct.ordinal());
                times[idx] = (long) (GaiaSky.instance.getT() * 1000f);
            }
            break;

        case PIXEL_RENDERER_UPDATE:
            Gdx.app.postRunnable(new Runnable() {

                @Override
                public void run() {
                    AbstractRenderSystem.POINT_UPDATE_FLAG = true;
                    // updatePixelRenderSystem();
                }

            });
            break;
        case LINE_RENDERER_UPDATE:
            Gdx.app.postRunnable(new Runnable() {

                @Override
                public void run() {
                    updateLineRenderSystem();
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
        default:
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
    private float calculateAlpha(ComponentType type, double t) {
        int ordinal = type.ordinal();
        long diff = (long) (t * 1000f) - times[ordinal];
        if (diff > GlobalConf.scene.OBJECT_FADE_MS) {
            if (visible.get(ordinal)) {
                alphas[ordinal] = 1;
            } else {
                alphas[ordinal] = 0;
            }
            return alphas[ordinal];
        } else {
            return visible.get(ordinal) ? MathUtilsd.lint(diff, 0, GlobalConf.scene.OBJECT_FADE_MS, 0, 1) : MathUtilsd.lint(diff, 0, GlobalConf.scene.OBJECT_FADE_MS, 1, 0);
        }
    }

    public void resize(final int w, final int h) {
        resize(w, h, false);
    }

    public void resize(final int w, final int h, boolean resizeRenderSys) {
        if (resizeRenderSys)
            resizeRenderSystems(w, h);

        for (ISGR sgr : sgrs) {
            sgr.resize(w, h);
        }
    }

    public void resizeRenderSystems(final int w, final int h) {
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

    public void updateLineRenderSystem() {
        LineRenderSystem current = null;
        for (IRenderSystem proc : renderProcesses) {
            if (proc instanceof LineRenderSystem) {
                current = (LineRenderSystem) proc;
            }
        }
        final int idx = renderProcesses.indexOf(current, true);
        if ((current instanceof LineQuadRenderSystem && GlobalConf.scene.isNormalLineRenderer()) || (!(current instanceof LineQuadRenderSystem) && !GlobalConf.scene.isNormalLineRenderer())) {
            renderProcesses.removeIndex(idx);
            AbstractRenderSystem lineSys = getLineRenderSystem();
            renderProcesses.insert(idx, lineSys);
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
