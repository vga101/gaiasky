package gaia.cu9.ari.gaiaorbit.render;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;

/**
 * Renders the cube map 360 degree mode. Basically, it renders the six sides of the cube map 
 * (front, back, up, down, right, left) with a 90 degree fov each and applies the cube map to 
 * equirectangular transformation.
 * @author tsagrista
 *
 */
public class SGRCubemap extends SGRAbstract implements ISGR {

    Vector3 aux1, aux2, aux3, dirbak, upbak;

    StretchViewport stretchViewport;

    /** Frame buffers for 3D mode (screen, screenshot, frame output) **/
    Map<Integer, FrameBuffer> fb3D;

    public SGRCubemap() {
        super();
        aux1 = new Vector3();
        aux3 = new Vector3();
        aux2 = new Vector3();
        dirbak = new Vector3();
        upbak = new Vector3();
        stretchViewport = new StretchViewport(Gdx.graphics.getHeight(), Gdx.graphics.getHeight());

        fb3D = new HashMap<Integer, FrameBuffer>();
    }

    @Override
    public void render(SceneGraphRenderer sgr, ICamera camera, float t, int rw, int rh, FrameBuffer fb, PostProcessBean ppb) {

        PerspectiveCamera cam = camera.getCamera();

        float fovbak = cam.fieldOfView;
        dirbak.set(cam.direction);
        upbak.set(cam.up);

        EventManager.instance.post(Events.FOV_CHANGED_CMD, 90f);

        int sizew = rw / 4;
        int sizeh = rh / 3;

        FrameBuffer fb3d = getFrameBuffer(sizew, sizeh);

        // FRONT
        Viewport viewport = stretchViewport;
        camera.setViewport(viewport);
        viewport.setCamera(cam);
        viewport.setWorldSize(sizeh, sizeh);
        viewport.setScreenBounds(sizew, sizeh, sizew, sizeh);
        viewport.apply();

        boolean postproc = postprocessCapture(ppb, fb3d, sizew, sizeh);
        sgr.renderScene(camera, t, rc);
        postprocessRender(ppb, fb3d, postproc, camera);

        Texture tex = fb3d.getColorBufferTexture();

        float scaleX = 1f;
        float scaleY = 1f;
        if (fb != null) {
            scaleX = (float) Gdx.graphics.getWidth() / (float) fb.getWidth();
            scaleY = (float) Gdx.graphics.getHeight() / (float) fb.getHeight();
            fb.begin();
        }

        GlobalResources.spriteBatch.begin();
        GlobalResources.spriteBatch.setColor(1f, 1f, 1f, 1f);
        GlobalResources.spriteBatch.draw(tex, sizew, sizeh, 0, 0, sizew, sizeh, scaleX, scaleY, 0, 0, 0, sizew, sizeh, false, true);
        GlobalResources.spriteBatch.end();

        if (fb != null)
            fb.end();

        // UP
        viewport.setScreenBounds(sizew, sizeh * 2, sizew, sizeh);
        viewport.apply();

        aux1.set(cam.direction);
        aux2.set(cam.up);
        aux1.crs(aux2);
        cam.direction.rotate(aux1, 90);
        cam.up.rotate(aux1, 90);
        cam.update();

        postproc = postprocessCapture(ppb, fb3d, sizew, sizeh);
        sgr.renderScene(camera, t, rc);
        postprocessRender(ppb, fb3d, postproc, camera);

        tex = fb3d.getColorBufferTexture();

        scaleX = 1f;
        scaleY = 1f;
        if (fb != null) {
            scaleX = (float) Gdx.graphics.getWidth() / (float) fb.getWidth();
            scaleY = (float) Gdx.graphics.getHeight() / (float) fb.getHeight();
            fb.begin();
        }

        GlobalResources.spriteBatch.begin();
        GlobalResources.spriteBatch.setColor(1f, 1f, 1f, 1f);
        GlobalResources.spriteBatch.draw(tex, sizew, sizeh * 2, 0, 0, sizew, sizeh, scaleX, scaleY, 0, 0, 0, sizew, sizeh, false, true);
        GlobalResources.spriteBatch.end();

        if (fb != null)
            fb.end();

        // BOTTOM
        viewport.setScreenBounds(sizew, 0, sizew, sizeh);
        viewport.apply();

        aux1.set(dirbak);
        aux2.set(upbak);
        aux1.crs(aux2);
        cam.direction.set(dirbak).rotate(aux1, -90);
        cam.up.set(upbak).rotate(aux1, -90);
        cam.update();

        postproc = postprocessCapture(ppb, fb3d, sizew, sizeh);
        sgr.renderScene(camera, t, rc);
        postprocessRender(ppb, fb3d, postproc, camera);

        tex = fb3d.getColorBufferTexture();

        scaleX = 1f;
        scaleY = 1f;
        if (fb != null) {
            scaleX = (float) Gdx.graphics.getWidth() / (float) fb.getWidth();
            scaleY = (float) Gdx.graphics.getHeight() / (float) fb.getHeight();
            fb.begin();
        }

        GlobalResources.spriteBatch.begin();
        GlobalResources.spriteBatch.setColor(1f, 1f, 1f, 1f);
        GlobalResources.spriteBatch.draw(tex, sizew, 0, 0, 0, sizew, sizeh, scaleX, scaleY, 0, 0, 0, sizew, sizeh, false, true);
        GlobalResources.spriteBatch.end();

        if (fb != null)
            fb.end();

        // LEFT
        viewport.setScreenBounds(0, sizeh, sizew, sizeh);
        viewport.apply();

        cam.up.set(upbak);
        cam.direction.set(dirbak).rotate(upbak, 90);
        cam.update();

        postproc = postprocessCapture(ppb, fb3d, sizew, sizeh);
        sgr.renderScene(camera, t, rc);
        postprocessRender(ppb, fb3d, postproc, camera);

        tex = fb3d.getColorBufferTexture();

        scaleX = 1f;
        scaleY = 1f;
        if (fb != null) {
            scaleX = (float) Gdx.graphics.getWidth() / (float) fb.getWidth();
            scaleY = (float) Gdx.graphics.getHeight() / (float) fb.getHeight();
            fb.begin();
        }

        GlobalResources.spriteBatch.begin();
        GlobalResources.spriteBatch.setColor(1f, 1f, 1f, 1f);
        GlobalResources.spriteBatch.draw(tex, 0, sizeh, 0, 0, sizew, sizeh, scaleX, scaleY, 0, 0, 0, sizew, sizeh, false, true);
        GlobalResources.spriteBatch.end();

        if (fb != null)
            fb.end();

        // RIGHT
        viewport.setScreenBounds(sizew * 2, sizeh, sizew, sizeh);
        viewport.apply();

        cam.up.set(upbak);
        cam.direction.set(dirbak).rotate(upbak, -90);
        cam.update();

        postproc = postprocessCapture(ppb, fb3d, sizew, sizeh);
        sgr.renderScene(camera, t, rc);
        postprocessRender(ppb, fb3d, postproc, camera);

        tex = fb3d.getColorBufferTexture();

        scaleX = 1f;
        scaleY = 1f;
        if (fb != null) {
            scaleX = (float) Gdx.graphics.getWidth() / (float) fb.getWidth();
            scaleY = (float) Gdx.graphics.getHeight() / (float) fb.getHeight();
            fb.begin();
        }

        GlobalResources.spriteBatch.begin();
        GlobalResources.spriteBatch.setColor(1f, 1f, 1f, 1f);
        GlobalResources.spriteBatch.draw(tex, sizew * 2, sizeh, 0, 0, sizew, sizeh, scaleX, scaleY, 0, 0, 0, sizew, sizeh, false, true);
        GlobalResources.spriteBatch.end();

        if (fb != null)
            fb.end();

        // BACK
        viewport.setScreenBounds(sizew * 3, sizeh, sizew, sizeh);
        viewport.apply();

        cam.up.set(upbak);
        cam.direction.set(dirbak).rotate(upbak, -180);
        cam.update();

        postproc = postprocessCapture(ppb, fb3d, sizew, sizeh);
        sgr.renderScene(camera, t, rc);
        postprocessRender(ppb, fb3d, postproc, camera);

        tex = fb3d.getColorBufferTexture();

        scaleX = 1f;
        scaleY = 1f;
        if (fb != null) {
            scaleX = (float) Gdx.graphics.getWidth() / (float) fb.getWidth();
            scaleY = (float) Gdx.graphics.getHeight() / (float) fb.getHeight();
            fb.begin();
        }

        GlobalResources.spriteBatch.begin();
        GlobalResources.spriteBatch.setColor(1f, 1f, 1f, 1f);
        GlobalResources.spriteBatch.draw(tex, sizew * 3, sizeh, 0, 0, sizew, sizeh, scaleX, scaleY, 0, 0, 0, sizew, sizeh, false, true);
        GlobalResources.spriteBatch.end();

        if (fb != null)
            fb.end();

        // Restore camera parameters
        cam.direction.set(dirbak);
        cam.up.set(upbak);

        EventManager.instance.post(Events.FOV_CHANGED_CMD, fovbak);

    }

    private int getKey(int w, int h, int extra) {
        return w * 100 + h * 10 + extra;
    }

    private FrameBuffer getFrameBuffer(int w, int h) {
        return getFrameBuffer(w, h, 0);
    }

    private FrameBuffer getFrameBuffer(int w, int h, int extra) {
        int key = getKey(w, h, extra);
        if (!fb3D.containsKey(key)) {
            fb3D.put(key, new FrameBuffer(Format.RGB888, w, h, true));
        }
        return fb3D.get(key);
    }

    @Override
    public void resize(int w, int h) {

    }

    @Override
    public void dispose() {
        Set<Integer> keySet = fb3D.keySet();
        for (Integer key : keySet) {
            FrameBuffer fb = fb3D.get(key);
            fb.dispose();
        }
    }

}
