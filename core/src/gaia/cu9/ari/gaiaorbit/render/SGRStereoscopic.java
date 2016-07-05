package gaia.cu9.ari.gaiaorbit.render;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.NaturalCamera;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.MyPools;

public class SGRStereoscopic extends SGRAbstract implements ISGR, IObserver {

    /** Viewport to use in steoeroscopic mode **/
    private Viewport stretchViewport;

    /** Frame buffers for 3D mode (screen, screenshot, frame output) **/
    Map<Integer, FrameBuffer> fb3D;

    public SGRStereoscopic() {
        super();
        // INIT VIEWPORT
        stretchViewport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // INIT FRAME BUFFER FOR 3D MODE
        fb3D = new HashMap<Integer, FrameBuffer>();
        fb3D.put(getKey(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight()), new FrameBuffer(Format.RGB888, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight(), true));

        EventManager.instance.subscribe(this, Events.FRAME_SIZE_UDPATE, Events.SCREENSHOT_SIZE_UDPATE);
    }

    @Override
    public void render(SceneGraphRenderer sgr, ICamera camera, int rw, int rh, FrameBuffer fb, PostProcessBean ppb) {
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

        boolean postproc = postprocessCapture(ppb, fb3d, rw / 2, rh);

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
        sgr.renderScene(camera, rc);

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
        sgr.renderScene(camera, rc);

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

    }

    public FrameBuffer getFrameBuffer(int w, int h) {
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
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
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
        }

    }

}
