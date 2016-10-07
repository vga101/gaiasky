package gaia.cu9.ari.gaiaorbit.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.bitfire.postprocessing.effects.Anaglyphic;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;

/**
 * Renders all the 3D/stereoscopic modes. Renders basically two scenes, one for
 * each eye, and then blends them together on screen with the necessary
 * processing depending on the 3D regime (anaglyphic, 3dtv, crosseye, vr).
 * 
 * @author tsagrista
 *
 */
public class SGRStereoscopic extends SGRAbstract implements ISGR, IObserver {

    /** Viewport to use in steoeroscopic mode **/
    private Viewport stretchViewport;

    /** Frame buffers for 3D mode (screen, screenshot, frame output) **/
    Map<Integer, FrameBuffer> fb3D;

    private Anaglyphic anaglyphic;

    private Vector3 aux1, aux2, aux3;

    public SGRStereoscopic() {
        super();
        // INIT VIEWPORT
        stretchViewport = new StretchViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // INIT FRAME BUFFER FOR 3D MODE
        fb3D = new HashMap<Integer, FrameBuffer>();
        fb3D.put(getKey(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight()), new FrameBuffer(Format.RGB888, Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight(), true));

        // Init anaglyphic effect
        anaglyphic = new Anaglyphic();

        // Aux vectors
        aux1 = new Vector3();
        aux2 = new Vector3();
        aux3 = new Vector3();

        EventManager.instance.subscribe(this, Events.FRAME_SIZE_UDPATE, Events.SCREENSHOT_SIZE_UDPATE);
    }

    @Override
    public void render(SceneGraphRenderer sgr, ICamera camera, float t, int rw, int rh, FrameBuffer fb, PostProcessBean ppb) {
        boolean movecam = camera.getMode() == CameraMode.Free_Camera || camera.getMode() == CameraMode.Focus || camera.getMode() == CameraMode.Spacecraft;
        //movecam = false;

        PerspectiveCamera cam = camera.getCamera();
        // Vector of 1 meter length pointing to the side of the camera
        Vector3 side = aux1.set(cam.direction);
        float separation = (float) Constants.M_TO_U * GlobalConf.program.STEREOSCOPIC_EYE_SEPARATION_M;
        float dirangleDeg = 0;

        CelestialBody currentFocus = null;
        if (camera.getMode() == CameraMode.Focus) {
            currentFocus = camera.getFocus();
        } else if (camera.getCurrent().getClosest() != null) {
            currentFocus = camera.getCurrent().getClosest();
        }
        if (currentFocus != null) {
            // If we have focus, we adapt the eye separation
            float distToFocus = currentFocus.distToCamera - currentFocus.getRadius();
            separation = (float) Math.min((Math.tan(Math.toRadians(1.5)) * distToFocus), 1e13 * Constants.M_TO_U);
            dirangleDeg = 1.5f;
        }

        side.crs(cam.up).nor().scl(separation);
        Vector3 backupPos = aux2.set(cam.position);
        Vector3 backupDir = aux3.set(cam.direction);

        if (GlobalConf.program.STEREO_PROFILE == StereoProfile.ANAGLYPHIC) {
            extendViewport.setCamera(camera.getCamera());
            extendViewport.setWorldSize(rw, rh);
            extendViewport.setScreenBounds(0, 0, rw, rh);
            extendViewport.apply();

            FrameBuffer fbmain = getFrameBuffer(rw, rh, 0);

            /** LEFT EYE **/
            FrameBuffer fb1 = getFrameBuffer(rw, rh, 1);
            boolean postproc = postprocessCapture(ppb, fb1, rw, rh);

            // Camera to the left
            if (movecam) {
                moveCamera(cam, side, dirangleDeg, false);
            }
            camera.setCameraStereoLeft(cam);
            sgr.renderScene(camera, t, rc);

            postprocessRender(ppb, fb1, postproc, camera);
            Texture texLeft = fb1.getColorBufferTexture();

            /** RIGHT EYE **/
            FrameBuffer fb2 = getFrameBuffer(rw, rh, 2);
            postproc = postprocessCapture(ppb, fb2, rw, rh);

            // Camera to the right
            if (movecam) {
                cam.position.set(backupPos);
                cam.direction.set(backupDir);
                moveCamera(cam, side, dirangleDeg, true);
            }
            camera.setCameraStereoRight(cam);
            sgr.renderScene(camera, t, rc);

            postprocessRender(ppb, fb2, postproc, camera);
            Texture texRight = fb2.getColorBufferTexture();

            // We have left and right images to texLeft and texRight

            anaglyphic.setTextureLeft(texLeft);
            anaglyphic.setTextureRight(texRight);

            // Render 
            anaglyphic.render(fbmain, fb);

            if (fb != null)
                fb.end();

            // ensure default texture unit #0 is active
            Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);
        } else {

            boolean stretch = GlobalConf.program.STEREO_PROFILE == StereoProfile.HD_3DTV;
            boolean crosseye = GlobalConf.program.STEREO_PROFILE == StereoProfile.CROSSEYE;

            // Side by side rendering
            Viewport viewport = stretch ? stretchViewport : extendViewport;

            viewport.setCamera(camera.getCamera());
            viewport.setWorldSize(stretch ? rw : rw / 2, rh);

            /** LEFT EYE **/

            viewport.setScreenBounds(0, 0, rw / 2, rh);
            viewport.apply();

            FrameBuffer fb3d = getFrameBuffer(rw / 2, rh);

            boolean postproc = postprocessCapture(ppb, fb3d, rw / 2, rh);

            // Camera to left
            if (movecam) {
                moveCamera(cam, side, dirangleDeg, crosseye);
            }
            camera.setCameraStereoLeft(cam);
            sgr.renderScene(camera, t, rc);

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
                cam.position.set(backupPos);
                cam.direction.set(backupDir);
                moveCamera(cam, side, dirangleDeg, !crosseye);
            }
            camera.setCameraStereoRight(cam);
            sgr.renderScene(camera, t, rc);

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

            /* Restore viewport */
            viewport.setScreenBounds(0, 0, rw, rh);

        }

        /** RESTORE **/
        cam.position.set(backupPos);
        cam.direction.set(backupDir);

    }

    private void moveCamera(PerspectiveCamera cam, Vector3 side, float angle, boolean switchSides) {
        if (switchSides) {
            cam.position.add(side);
            cam.direction.rotate(cam.up, angle);
        } else {
            cam.position.sub(side);
            cam.direction.rotate(cam.up, -angle);
        }
        cam.update();
    }

    private int getKey(int w, int h) {
        return getKey(w, h, 0);
    }

    private int getKey(int w, int h, int extra) {
        return w * 100 + h * 10 + extra;
    }

    private FrameBuffer getFrameBuffer(int w, int h, int extra) {
        int key = getKey(w, h, extra);
        if (!fb3D.containsKey(key)) {
            fb3D.put(key, new FrameBuffer(Format.RGB888, w, h, true));
        }
        return fb3D.get(key);
    }

    private FrameBuffer getFrameBuffer(int w, int h) {
        return getFrameBuffer(w, h, 0);
    }

    public void resize(final int w, final int h) {
        extendViewport.update(w, h);
        stretchViewport.update(w, h);

        int keyHalf = getKey(w / 2, h);
        int keyFull = getKey(w, h);

        Set<Integer> keySet = fb3D.keySet();
        for (Integer key : keySet) {
            if (key != keyHalf && key != keyFull) {
                FrameBuffer fb = fb3D.get(key);
                fb.dispose();
                fb3D.remove(key);
            }
        }

        if (!fb3D.containsKey(keyHalf)) {
            fb3D.put(keyHalf, new FrameBuffer(Format.RGB888, w / 2, h, true));
        }

        if (!fb3D.containsKey(keyFull)) {
            fb3D.put(keyFull, new FrameBuffer(Format.RGB888, w, h, true));
        }

    }

    public void dispose() {
        Set<Integer> keySet = fb3D.keySet();
        for (Integer key : keySet) {
            FrameBuffer fb = fb3D.get(key);
            fb.dispose();
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
