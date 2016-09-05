package gaia.cu9.ari.gaiaorbit;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

import gaia.cu9.ari.gaiaorbit.data.AssetBean;
import gaia.cu9.ari.gaiaorbit.data.GaiaAttitudeLoader;
import gaia.cu9.ari.gaiaorbit.data.GaiaAttitudeLoader.GaiaAttitudeLoaderParameter;
import gaia.cu9.ari.gaiaorbit.data.SGLoader;
import gaia.cu9.ari.gaiaorbit.data.SGLoader.SGLoaderParameter;
import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitData;
import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitDataLoader;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.FullGui;
import gaia.cu9.ari.gaiaorbit.interfce.GaiaControllerListener;
import gaia.cu9.ari.gaiaorbit.interfce.GaiaInputController;
import gaia.cu9.ari.gaiaorbit.interfce.IGui;
import gaia.cu9.ari.gaiaorbit.interfce.LoadingGui;
import gaia.cu9.ari.gaiaorbit.interfce.MobileGui;
import gaia.cu9.ari.gaiaorbit.render.AbstractRenderer;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.IMainRenderer;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.RenderType;
import gaia.cu9.ari.gaiaorbit.render.PostProcessorFactory;
import gaia.cu9.ari.gaiaorbit.render.SceneGraphRenderer;
import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.ModelCache;
import gaia.cu9.ari.gaiaorbit.util.MusicManager;
import gaia.cu9.ari.gaiaorbit.util.gaia.GaiaAttitudeServer;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.GlobalClock;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.time.RealTimeClock;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

/**
 * The main class. Holds all the entities manages the update/draw cycle as well
 * as the image rendering.
 * 
 * @author Toni Sagrista
 *
 */
public class GaiaSky implements ApplicationListener, IObserver, IMainRenderer {
    private static boolean LOADING = true;

    /** Attitude folder **/
    private static String ATTITUDE_FOLDER = "data/attitudexml/";

    /** Singleton instance **/
    public static GaiaSky instance;

    // Asset manager
    public AssetManager manager;

    /** This handles the input events **/
    private GaiaInputController inputController;

    // Camera
    public CameraManager cam;

    // Data load string
    private String dataLoadString;

    public ISceneGraph sg;
    private SceneGraphRenderer sgr;
    private IPostProcessor pp;

    // The frame number
    public long frames;

    // Frame buffer map
    private Map<String, FrameBuffer> fbmap;

    /**
     * The user interface
     */
    public IGui gui, loadingGui;

    /**
     * Time
     */
    public ITimeFrameProvider time;
    private ITimeFrameProvider clock, real;

    /**
     * Music
     */
    public Music music;

    /**
     * Camera recording or not?
     */
    private boolean camRecording = false;

    private boolean initialized = false;

    /**
     * Creates a GaiaSky instance.
     */
    public GaiaSky() {
        super();
        instance = this;
    }

    public void setSceneGraph(ISceneGraph sg) {
        this.sg = sg;
    }

    @Override
    public void create() {
        Gdx.app.setLogLevel(Application.LOG_INFO);

        fbmap = new HashMap<String, FrameBuffer>();

        // Disable all kinds of input
        EventManager.instance.post(Events.INPUT_ENABLED_CMD, false);

        if (!GlobalConf.initialized()) {
            Logger.error(new RuntimeException("FATAL: Global configuration not initlaized"));
            return;
        }

        // Initialize times
        clock = new GlobalClock(1, new Date());
        real = new RealTimeClock();
        time = GlobalConf.runtime.REAL_TIME ? real : clock;

        // Precompute some math functions
        MathUtilsd.initialize();

        // Initialize i18n
        I18n.initialize();

        // Initialize asset manager
        FileHandleResolver resolver = new InternalFileHandleResolver();
        manager = new AssetManager(resolver);
        manager.setLoader(ISceneGraph.class, new SGLoader(resolver));
        manager.setLoader(OrbitData.class, new OrbitDataLoader(resolver));
        manager.setLoader(GaiaAttitudeServer.class, new GaiaAttitudeLoader(resolver));

        // Init global resources
        GlobalResources.initialize(manager);

        // Initialize Cameras
        cam = new CameraManager(manager, CameraMode.Focus);

        // Set asset manager to asset bean
        AssetBean.setAssetManager(manager);

        // Initialize Gaia attitudes
        manager.load(ATTITUDE_FOLDER, GaiaAttitudeServer.class, new GaiaAttitudeLoaderParameter(GlobalConf.runtime.STRIPPED_FOV_MODE ? new String[] { "OPS_RSLS_0022916_rsls_nsl_gareq1_afterFirstSpinPhaseOptimization.2.xml" } : new String[] {}));

        /** LOAD SCENE GRAPH **/
        if (sg == null) {
            dataLoadString = GlobalConf.data.CATALOG_JSON_FILE + "," + GlobalConf.data.OBJECTS_JSON_FILE;
            manager.load(dataLoadString, ISceneGraph.class, new SGLoaderParameter(time, GlobalConf.performance.MULTITHREADING, GlobalConf.performance.NUMBER_THREADS()));
        }

        // Load scene graph
        if (Constants.desktop || Constants.webgl) {
            // Full GUI for desktop
            gui = new FullGui();
        } else if (Constants.mobile) {
            // Reduced GUI for android/iOS/...
            gui = new MobileGui();
        }
        gui.initialize(manager);

        // Tell the asset manager to load all the assets
        Set<AssetBean> assets = AssetBean.getAssets();
        for (AssetBean ab : assets) {
            ab.load(manager);
        }

        // Initialize loading screen
        loadingGui = new LoadingGui();
        loadingGui.initialize(manager);

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.glslversion", Gdx.gl.glGetString(GL20.GL_SHADING_LANGUAGE_VERSION)));
    }

    /**
     * Execute this when the models have finished loading. This sets the models
     * to their classes and removes the Loading message
     */
    private void doneLoading() {
        loadingGui.dispose();
        loadingGui = null;

        // Get attitude
        if (manager.isLoaded(ATTITUDE_FOLDER)) {
            GaiaAttitudeServer.instance = manager.get(ATTITUDE_FOLDER);
        }

        pp = PostProcessorFactory.instance.getPostProcessor();

        GlobalResources.doneLoading(manager);

        /**
         * GET SCENE GRAPH
         */
        if (manager.isLoaded(dataLoadString)) {
            sg = manager.get(dataLoadString);
        }

        /**
         * INITIALIZE RENDERER
         */
        AbstractRenderer.initialize(sg);
        sgr = new SceneGraphRenderer();
        sgr.initialize(manager);
        sgr.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // First time, set assets
        for (SceneGraphNode sgn : sg.getNodes()) {
            sgn.doneLoading(manager);
        }

        // Update whole tree to initialize positions
        OctreeNode.LOAD_ACTIVE = false;
        time.update(0.000000001f);
        sg.update(time, cam);
        time.update(0);
        OctreeNode.LOAD_ACTIVE = true;

        // Initialize input handlers
        InputMultiplexer inputMultiplexer = new InputMultiplexer();

        // Only for the Full GUI
        gui.setSceneGraph(sg);
        gui.setVisibilityToggles(ComponentType.values(), SceneGraphRenderer.visible);
        inputMultiplexer.addProcessor(gui.getGuiStage());

        // Initialize the GUI
        gui.doneLoading(manager);

        // Publish visibility
        EventManager.instance.post(Events.VISIBILITY_OF_COMPONENTS, new Object[] { SceneGraphRenderer.visible });

        inputController = new GaiaInputController(cam, gui);
        Controllers.addListener(new GaiaControllerListener(cam, gui));
        inputMultiplexer.addProcessor(inputController);

        Gdx.input.setInputProcessor(inputMultiplexer);

        EventManager.instance.post(Events.SCENE_GRAPH_LOADED, sg);

        AbstractPositionEntity focus = null;
        Vector3d newCameraPos = null;
        if (!Constants.focalplane) {
            focus = (AbstractPositionEntity) sg.getNode("Earth");
            EventManager.instance.post(Events.FOCUS_CHANGE_CMD, focus, true);
            EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.Focus);
            float dst = focus.size * 3;
            newCameraPos = focus.pos.cpy().add(0, dst / 5f, -dst);
            EventManager.instance.post(Events.CAMERA_POS_CMD, newCameraPos.values());

        } else {
            focus = (AbstractPositionEntity) sg.getNode("Gaia");
            EventManager.instance.post(Events.FOCUS_CHANGE_CMD, focus, true);
            EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.Gaia_FOV1and2);
        }

        // Update whole tree to reinitialize positions with the new camera
        // position
        time.update(0.0000000001f);
        sg.update(time, cam);
        sgr.clearLists();
        time.update(0);

        if (!Constants.focalplane) {
            Vector3d newCameraDir = focus.pos.cpy().sub(newCameraPos);
            EventManager.instance.post(Events.CAMERA_DIR_CMD, newCameraDir.values());
        }

        // Initialize time in GUI
        EventManager.instance.post(Events.TIME_CHANGE_INFO, time.getTime());

        // Subscribe to events
        EventManager.instance.subscribe(this, Events.TOGGLE_AMBIENT_LIGHT, Events.AMBIENT_LIGHT_CMD, Events.RECORD_CAMERA_CMD);

        // Re-enable input
        if (!GlobalConf.runtime.STRIPPED_FOV_MODE)
            EventManager.instance.post(Events.INPUT_ENABLED_CMD, true);

        // Set current date
        EventManager.instance.post(Events.TIME_CHANGE_CMD, new Date());

        if (Constants.focalplane) {
            // Activate time
            EventManager.instance.post(Events.TOGGLE_TIME_CMD, true, false);
        }

        // Hide interface if stereoscopic is on
        if (GlobalConf.program.STEREOSCOPIC_MODE) {
            EventManager.instance.post(Events.DISPLAY_GUI_CMD, I18n.bundle.get("notif.cleanmode"), false);
        }

        // Initialize frames
        frames = 0;

        initialized = true;

        // Run tutorial
        if (GlobalConf.program.DISPLAY_TUTORIAL) {
            EventManager.instance.post(Events.RUN_SCRIPT_PATH, "scripts/tutorial/tutorial-pointer.py");
            GlobalConf.program.DISPLAY_TUTORIAL = false;
        }

    }

    @Override
    public void dispose() {

        if (Constants.desktop)
            ConfInit.instance.persistGlobalConf(new File(System.getProperty("properties.file")));

        // Flush frames
        EventManager.instance.post(Events.FLUSH_FRAMES);

        // Dispose all
        gui.dispose();
        EventManager.instance.post(Events.DISPOSE);
        if (sg != null) {
            sg.dispose();
        }
        ModelCache.cache.dispose();

        // Renderer
        if (sgr != null)
            sgr.dispose();

        // Post processor
        if (pp != null)
            pp.dispose();

        // Dispose music manager
        MusicManager.dispose();
    }

    @Override
    public void render() {
        if (LOADING) {
            if (manager.update()) {
                doneLoading();

                LOADING = false;
            } else {
                // Display loading screen
                renderLoadingScreen();
            }
        } else {

            // Asynchronous load of textures and resources
            manager.update();

            if (!GlobalConf.runtime.UPDATE_PAUSE) {
                /**
                 * UPDATE
                 */
                update(Gdx.graphics.getDeltaTime());

                /**
                 * FRAME OUTPUT
                 */
                EventManager.instance.post(Events.RENDER_FRAME, this);

                /**
                 * SCREENSHOT OUTPUT - simple|redraw mode
                 */
                EventManager.instance.post(Events.RENDER_SCREENSHOT, this);

                /**
                 * SCREEN OUTPUT
                 */
                if (GlobalConf.screen.SCREEN_OUTPUT) {
                    /** RENDER THE SCENE **/
                    preRenderScene();
                    renderSgr(cam, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), null, pp.getPostProcessBean(RenderType.screen));

                    if (GlobalConf.runtime.DISPLAY_GUI) {
                        // Render the GUI, setting the viewport
                        gui.getGuiStage().getViewport().apply();
                        gui.render();
                    }

                }

                sgr.clearLists();
                // Number of frames
                frames++;
            }

        }

        EventManager.instance.post(Events.FPS_INFO, Gdx.graphics.getFramesPerSecond());
    }

    /**
     * Update method.
     * 
     * @param dt
     *            Delta time in seconds.
     */
    public void update(float dt) {
        if (GlobalConf.frame.RENDER_OUTPUT) {
            // If RENDER_OUTPUT is active, we need to set our dt according to
            // the fps
            dt = 1f / GlobalConf.frame.RENDER_TARGET_FPS;
        } else if (camRecording) {
            // If Camera is recording, we need to set our dt according to
            // the fps
            dt = 1f / GlobalConf.frame.CAMERA_REC_TARGET_FPS;
        } else {
            // Max time step is 0.1 seconds. Not in RENDER_OUTPUT MODE.
            dt = Math.min(dt, 0.1f);
        }

        gui.update(dt);
        EventManager.instance.post(Events.UPDATE_GUI, dt);

        float dtScene = dt;
        if (!GlobalConf.runtime.TIME_ON) {
            dtScene = 0;
        }
        // Update clock
        time.update(dtScene);

        // Update events
        EventManager.instance.dispatchDelayedMessages();

        // Update cameras
        cam.update(dt, time);

        // Update scene graph
        sg.update(time, cam);

    }

    public void preRenderScene() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    public void renderSgr(ICamera camera, int width, int height, FrameBuffer frameBuffer, PostProcessBean ppb) {
        sgr.render(camera, width, height, frameBuffer, ppb);
    }

    @Override
    public void resize(final int width, final int height) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                resizeImmediate(width, height);
            }
        });

    }

    public void resizeImmediate(final int width, final int height) {
        if (!initialized) {
            loadingGui.resize(width, height);
        } else {
            pp.resize(width, height);
            gui.resize(width, height);
            sgr.resize(width, height);
        }

        cam.updateAngleEdge(width, height);

        EventManager.instance.post(Events.SCREEN_RESIZE, width, height);
    }

    /**
     * Renders the loading screen
     */
    private void renderLoadingScreen() {
        loadingGui.update(Gdx.graphics.getDeltaTime());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        loadingGui.render();
    }

    @Override
    public void pause() {
        EventManager.instance.post(Events.FLUSH_FRAMES);
    }

    @Override
    public void resume() {
    }

    public List<CelestialBody> getFocusableEntities() {

        return sg.getFocusableObjects();
    }

    public SceneGraphNode findEntity(String name) {
        return sg.getNode(name);
    }

    public CelestialBody findFocusByName(String name) {
        return sg.findFocus(name);
    }

    public FrameBuffer getFrameBuffer(int w, int h) {
        String key = getKey(w, h);
        if (!fbmap.containsKey(key)) {
            FrameBuffer fb = new FrameBuffer(Format.RGB888, w, h, true);
            fbmap.put(key, fb);
        }
        return fbmap.get(key);
    }

    private String getKey(int w, int h) {
        return w + "x" + h;
    }

    public ICamera getICamera() {
        return cam.current;
    }

    public CameraManager getCameraManager() {
        return cam;
    }

    public IPostProcessor getPostProcessor() {
        return pp;
    }

    public boolean isOn(ComponentType comp) {
        return sgr.isOn(comp);
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case TOGGLE_AMBIENT_LIGHT:
            // TODO No better place to put this??
            ModelComponent.toggleAmbientLight((Boolean) data[1]);
            break;
        case AMBIENT_LIGHT_CMD:
            ModelComponent.setAmbientLight((float) data[0]);
            break;
        case RECORD_CAMERA_CMD:
            if (data != null) {
                camRecording = (Boolean) data[0];
            } else {
                camRecording = !camRecording;
            }
            break;
        default:
            break;
        }

    }

}
