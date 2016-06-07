package gaia.cu9.ari.gaiaorbit.render;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.utils.DefaultShaderProvider;
import com.badlogic.gdx.graphics.g3d.utils.RenderableSorter;
import com.badlogic.gdx.graphics.g3d.utils.ShaderProvider;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.BufferUtils;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bitfire.utils.ShaderLoader;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.render.system.AbstractRenderSystem;
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
import gaia.cu9.ari.gaiaorbit.scenegraph.NaturalCamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.MyPools;
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

    private RenderContext rc;

    private int maxTexSize;

    /** Render lists for all render groups **/
    public static Map<RenderGroup, Multilist<IRenderable>> render_lists;

    // Two model batches, for front (models), back and atmospheres
    private SpriteBatch spriteBatch, fontBatch;

    private List<IRenderSystem> renderProcesses;

    /** Viewport to use in steoeroscopic mode **/
    private Viewport stretchViewport;
    /** Viewport to use in normal mode **/
    private Viewport extendViewport;

    /** Frame buffers for 3D mode (screen, screenshot, frame output) **/
    Map<Integer, FrameBuffer> fb3D;

    Runnable blendNoDepthRunnable, blendDepthRunnable;

    public SceneGraphRenderer() {
        super();
    }

    @Override
    public void initialize(AssetManager manager) {
        IntBuffer intBuffer = BufferUtils.newIntBuffer(16);
        Gdx.gl20.glGetIntegerv(GL20.GL_MAX_TEXTURE_SIZE, intBuffer);
        maxTexSize = intBuffer.get();
        Logger.debug(this.getClass().getSimpleName(), "Max texture size: " + maxTexSize);

        ShaderLoader.Pedantic = false;
        ShaderProgram.pedantic = false;
        starShader = new ShaderProgram(Gdx.files.internal("shader/star.vertex.glsl"), Gdx.files.internal("shader/star.rays.fragment.glsl"));
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

        // Render context
        rc = new RenderContext();

        ComponentType[] comps = ComponentType.values();

        // Set reference
        visible = GlobalConf.scene.VISIBILITY;

        times = new long[comps.length];
        alphas = new float[comps.length];
        for (int i = 0; i < comps.length; i++) {
            alphas[i] = 1f;
        }

        /**
         *
         * ======= INITIALIZE RENDER COMPONENTS =======
         *
         **/
        pixelRenderSystems = new AbstractRenderSystem[3];

        renderProcesses = new ArrayList<IRenderSystem>();

        blendNoDepthRunnable = new Runnable() {
            @Override
            public void run() {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glDisable(GL20.GL_DEPTH_TEST);
                Gdx.gl.glDepthMask(false);
            }
        };
        blendDepthRunnable = new Runnable() {
            @Override
            public void run() {
                Gdx.gl.glEnable(GL20.GL_BLEND);
                Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
                Gdx.gl.glDepthMask(true);
            }
        };

        int priority = 1;

        // POINTS
        AbstractRenderSystem pixelProc = new PixelRenderSystem(RenderGroup.POINT, 0, alphas);
        pixelProc.setPreRunnable(blendNoDepthRunnable);

        // MODEL BACK
        AbstractRenderSystem modelBackProc = new ModelBatchRenderSystem(RenderGroup.MODEL_B, priority++, alphas, modelBatchB, false);
        modelBackProc.setPreRunnable(blendNoDepthRunnable);
        modelBackProc.setPostRunnable(new Runnable() {
            @Override
            public void run() {
                // This always goes at the back, clear depth buffer
                Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
            }
        });

        // ANNOTATIONS
        AbstractRenderSystem annotationsProc = new FontRenderSystem(RenderGroup.MODEL_B_ANNOT, priority++, alphas, spriteBatch);
        annotationsProc.setPreRunnable(blendNoDepthRunnable);
        annotationsProc.setPostRunnable(new Runnable() {
            @Override
            public void run() {
                // This always goes at the back, clear depth buffer
                Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
            }
        });

        // SHADER STARS
        AbstractRenderSystem shaderBackProc = new QuadRenderSystem(RenderGroup.SHADER, priority++, alphas, starShader, true);
        shaderBackProc.setPreRunnable(blendNoDepthRunnable);

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

        // INIT VIEWPORTS
        stretchViewport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        extendViewport = new ExtendViewport(200, 200);

        // INIT FRAME BUFFER FOR 3D MODE
        fb3D = new HashMap<Integer, FrameBuffer>();
        fb3D.put(getKey(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight()), new FrameBuffer(Format.RGB888, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight(), true));

        EventManager.instance.subscribe(this, Events.TOGGLE_VISIBILITY_CMD, Events.PIXEL_RENDERER_UPDATE, Events.SCREENSHOT_SIZE_UDPATE, Events.FRAME_SIZE_UDPATE);

    }

    private int getKey(int w, int h) {
        return w * 31 + h;
    }

    private boolean postprocessCapture(PostProcessBean ppb, FrameBuffer fb, int rw, int rh) {
        boolean postproc = ppb.capture();
        if (postproc) {
            rc.ppb = ppb;
        } else {
            rc.ppb = null;
        }
        rc.fb = fb;
        rc.w = rw;
        rc.h = rh;
        return postproc;
    }

    private void postprocessRender(PostProcessBean ppb, FrameBuffer fb, boolean postproc, ICamera camera) {
        ppb.render(fb);

        // Render camera
        if (fb != null && postproc) {
            fb.begin();
        }
        camera.render();
        if (fb != null && postproc) {
            fb.end();
        }
    }

    @Override
    public void render(ICamera camera, int rw, int rh, FrameBuffer fb, PostProcessBean ppb) {

        // Prepare render context
        boolean postproc;

        if (camera.getNCameras() > 1) {
            postproc = postprocessCapture(ppb, fb, rw, rh);

            /** FIELD OF VIEW CAMERA **/

            CameraMode aux = camera.getMode();

            camera.updateMode(CameraMode.Gaia_FOV1, false);

            renderScene(camera, rc);

            camera.updateMode(CameraMode.Gaia_FOV2, false);

            renderScene(camera, rc);

            camera.updateMode(aux, false);

            postprocessRender(ppb, fb, postproc, camera);

        } else {
            /** NORMAL MODE **/

            if (GlobalConf.program.STEREOSCOPIC_MODE) {
                // Update rc
                rc.w = rw / 2;

                boolean movecam = camera.getMode() == CameraMode.Free_Camera || camera.getMode() == CameraMode.Focus;
                boolean stretch = GlobalConf.program.STEREO_PROFILE == StereoProfile.HD_3DTV;
                boolean crosseye = GlobalConf.program.STEREO_PROFILE == StereoProfile.CROSSEYE;

                // Side by side rendering
                Viewport viewport = stretch ? stretchViewport : extendViewport;

                PerspectiveCamera cam = camera.getCamera();
                Pool<Vector3> vectorPool = MyPools.get(Vector3.class);
                // Vector of 1 meter length pointing to the side of the camera
                Vector3 side = vectorPool.obtain().set(cam.direction);
                float separation = (float) Constants.M_TO_U * GlobalConf.program.STEREOSCOPIC_EYE_SEPARATION_M;
                float dirangleDeg = 0;
                if (camera.getMode() == CameraMode.Focus) {
                    // In focus mode we keep the separation dependent on the
                    // distance with a fixed angle
                    float distToFocus = ((NaturalCamera) camera.getCurrent()).focus.distToCamera - ((NaturalCamera) camera.getCurrent()).focus.getRadius();
                    separation = (float) Math.min((Math.tan(Math.toRadians(1.5)) * distToFocus), 3e13 * Constants.M_TO_U);
                    dirangleDeg = 3f;
                }

                side.crs(cam.up).nor().scl(separation);
                Vector3 backup = vectorPool.obtain().set(cam.position);

                camera.setViewport(viewport);
                viewport.setCamera(camera.getCamera());
                viewport.setWorldSize(stretch ? rw : rw / 2, rh);

                /** LEFT EYE **/

                viewport.setScreenBounds(0, 0, rw / 2, rh);
                viewport.apply();

                FrameBuffer fb3d = getFrameBuffer(rw / 2, rh);

                postproc = postprocessCapture(ppb, fb3d, rw / 2, rh);

                // Camera to left
                if (movecam) {
                    if (crosseye) {
                        cam.position.add(side);
                        cam.direction.rotate(cam.up, dirangleDeg);
                    } else {
                        cam.position.sub(side);
                        cam.direction.rotate(cam.up, -dirangleDeg);
                    }
                    cam.update();
                }
                camera.setCameraStereoLeft(cam);
                renderScene(camera, rc);

                Texture tex = null;
                postprocessRender(ppb, fb3d, postproc, camera);
                tex = fb3d.getColorBufferTexture();

                float scaleX = 1;
                float scaleY = 1;
                if (fb != null) {
                    scaleX = (float) Gdx.graphics.getWidth() / (float) fb.getWidth();
                    scaleY = (float) Gdx.graphics.getHeight() / (float) fb.getHeight();
                    fb.begin();
                }

                GlobalResources.spriteBatch.begin();
                GlobalResources.spriteBatch.setColor(1f, 1f, 1f, 1f);
                GlobalResources.spriteBatch.draw(tex, 0, 0, 0, 0, rw / 2, rh, scaleX, scaleY, 0, 0, 0, rw / 2, rh, false, true);
                GlobalResources.spriteBatch.end();

                if (fb != null)
                    fb.end();

                /** RIGHT EYE **/

                viewport.setScreenBounds(rw / 2, 0, rw / 2, rh);
                viewport.apply();

                postproc = postprocessCapture(ppb, fb3d, rw / 2, rh);

                // Camera to right
                if (movecam) {
                    cam.position.set(backup);
                    if (crosseye) {
                        cam.position.sub(side);
                        cam.direction.rotate(cam.up, -dirangleDeg);
                    } else {
                        cam.position.add(side);
                        cam.direction.rotate(cam.up, dirangleDeg);
                    }
                    cam.update();
                }
                camera.setCameraStereoRight(cam);
                renderScene(camera, rc);

                postprocessRender(ppb, fb3d, postproc, camera);
                tex = fb3d.getColorBufferTexture();

                if (fb != null)
                    fb.begin();

                GlobalResources.spriteBatch.begin();
                GlobalResources.spriteBatch.setColor(1f, 1f, 1f, 1f);
                GlobalResources.spriteBatch.draw(tex, scaleX * rw / 2, 0, 0, 0, rw / 2, rh, scaleX, scaleY, 0, 0, 0, rw / 2, rh, false, true);
                GlobalResources.spriteBatch.end();

                if (fb != null)
                    fb.end();

                /** RESTORE **/
                cam.position.set(backup);
                viewport.setScreenBounds(0, 0, rw, rh);

                vectorPool.free(side);
                vectorPool.free(backup);

            } else {
                postproc = postprocessCapture(ppb, fb, rw, rh);

                camera.setViewport(extendViewport);
                extendViewport.setCamera(camera.getCamera());
                extendViewport.setWorldSize(rw, rh);
                extendViewport.setScreenSize(rw, rh);
                extendViewport.apply();
                renderScene(camera, rc);

                postprocessRender(ppb, fb, postproc, camera);
            }
        }

    }

    public void renderScene(ICamera camera, RenderContext rc) {
        // Update time difference since last update
        long now = new Date().getTime();
        for (ComponentType ct : ComponentType.values()) {
            alphas[ct.ordinal()] = calculateAlpha(ct, now);
        }

        EventManager.instance.post(Events.DEBUG1, "quads: " + (render_lists.get(RenderGroup.SHADER).size() + render_lists.get(RenderGroup.SHADER_F).size()) + ", points: " + render_lists.get(RenderGroup.POINT).size() + ", labels: " + render_lists.get(RenderGroup.LABEL).size());

        int size = renderProcesses.size();
        for (int i = 0; i < size; i++) {
            IRenderSystem process = renderProcesses.get(i);
            List<IRenderable> l = render_lists.get(process.getRenderGroup()).toList();
            process.render(l, camera, rc);
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
                times[idx] = new Date().getTime();
            } else {
                // Only toggle
                visible[idx] = !visible[idx];
                times[idx] = new Date().getTime();
            }
            break;
        case SCREENSHOT_SIZE_UDPATE:
        case FRAME_SIZE_UDPATE:
            final Integer w = (Integer) data[0];
            final Integer h = (Integer) data[1];
            final Integer key = getKey(w / 2, h);
            if (!fb3D.containsKey(key)) {
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        fb3D.put(key, new FrameBuffer(Format.RGB888, w / 2, h, true));
                    }
                });
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
        }
    }

    private float calculateAlpha(ComponentType type, long now) {
        long diff = now - times[type.ordinal()];
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

    private FrameBuffer getFrameBuffer(int w, int h) {
        int key = getKey(w, h);
        if (!fb3D.containsKey(key)) {
            fb3D.put(key, new FrameBuffer(Format.RGB888, w, h, true));
        }
        return fb3D.get(key);
    }

    public void resize(final int w, final int h) {
        extendViewport.update(w, h);
        stretchViewport.update(w, h);

        int key = getKey(w / 2, h);
        if (!fb3D.containsKey(key)) {
            fb3D.put(key, new FrameBuffer(Format.RGB888, w / 2, h, true));
        }

        for (IRenderSystem rendSys : renderProcesses) {
            rendSys.resize(w, h);
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
