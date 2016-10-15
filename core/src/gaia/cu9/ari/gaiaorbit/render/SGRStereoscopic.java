package gaia.cu9.ari.gaiaorbit.render;

import java.util.HashMap;
import java.util.Iterator;
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
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

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
    private Vector3d aux1d, aux2d;

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
        aux1d = new Vector3d();
        aux2d = new Vector3d();

        EventManager.instance.subscribe(this, Events.FRAME_SIZE_UDPATE, Events.SCREENSHOT_SIZE_UDPATE);
    }

    @Override
    public void render(SceneGraphRenderer sgr, ICamera camera, float t, int rw, int rh, FrameBuffer fb, PostProcessBean ppb) {
        boolean movecam = camera.getMode() == CameraMode.Free_Camera || camera.getMode() == CameraMode.Focus || camera.getMode() == CameraMode.Spacecraft;
        //movecam = false;

        PerspectiveCamera cam = camera.getCamera();
        // Vector of 1 meter length pointing to the side of the camera
        Vector3d side = aux2d.set(camera.getDirection());
        double separation = Constants.M_TO_U * GlobalConf.program.STEREOSCOPIC_EYE_SEPARATION_M;
        double dirangleDeg = 0;

        CelestialBody currentFocus = null;
        if (camera.getMode() == CameraMode.Focus) {
            currentFocus = camera.getFocus();
        } else if (camera.getCurrent().getClosest() != null) {
            currentFocus = camera.getCurrent().getClosest();
        }
        if (currentFocus != null) {
            // If we have focus, we adapt the eye separation
            double distToFocus = currentFocus.distToCamera - currentFocus.getRadius();
            separation = (float) Math.min((Math.tan(Math.toRadians(1.5)) * distToFocus), 1e3 * Constants.AU_TO_U);
            //separation = Math.tan(Math.toRadians(1.5)) * distToFocus;
            dirangleDeg = 1.5;
        }

        side.crs(camera.getUp()).nor().scl(separation);
        Vector3 backupPos = aux2.set(cam.position);
        Vector3 backupDir = aux3.set(cam.direction);
        Vector3d backupPosd = aux1d.set(camera.getPos());

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
                moveCamera(camera, side, dirangleDeg, false);
            }
            camera.setCameraStereoLeft(cam);
            sgr.renderScene(camera, t, rc);

            postprocessRender(ppb, fb1, postproc, camera, rw, rh);
            Texture texLeft = fb1.getColorBufferTexture();

            /** RIGHT EYE **/
            FrameBuffer fb2 = getFrameBuffer(rw, rh, 2);
            postproc = postprocessCapture(ppb, fb2, rw, rh);

            // Camera to the right
            if (movecam) {
                camera.setPos(backupPosd);
                cam.position.set(backupPos);
                cam.direction.set(backupDir);
                moveCamera(camera, side, dirangleDeg, true);
            }
            camera.setCameraStereoRight(cam);
            sgr.renderScene(camera, t, rc);

            postprocessRender(ppb, fb2, postproc, camera, rw, rh);
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
                moveCamera(camera, side, dirangleDeg, crosseye);
            }
            camera.setCameraStereoLeft(cam);
            sgr.renderScene(camera, t, rc);

            Texture tex = null;
            postprocessRender(ppb, fb3d, postproc, camera, rw / 2, rh);
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
                camera.setPos(backupPosd);
                cam.position.set(backupPos);
                cam.direction.set(backupDir);
                moveCamera(camera, side, dirangleDeg, !crosseye);
            }
            camera.setCameraStereoRight(cam);
            sgr.renderScene(camera, t, rc);

            postprocessRender(ppb, fb3d, postproc, camera, rw / 2, rh);
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
        camera.setPos(backupPosd);
        cam.position.set(backupPos);
        cam.direction.set(backupDir);

    }

    private void moveCamera(ICamera camera, Vector3d side, double angle, boolean switchSides) {
        PerspectiveCamera cam = camera.getCamera();
        Vector3 sidef = side.put(aux1);
        if (switchSides) {
            camera.getPos().add(side);
            cam.position.add(sidef);
            cam.direction.rotate(cam.up, (float) angle);
        } else {
            camera.getPos().sub(side);
            cam.position.sub(sidef);
            cam.direction.rotate(cam.up, (float) -angle);
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

        Iterator<Map.Entry<Integer, FrameBuffer>> iter = fb3D.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry<Integer, FrameBuffer> entry = iter.next();
            if (entry.getKey() != keyHalf && entry.getKey() != keyFull) {
                iter.remove();
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
