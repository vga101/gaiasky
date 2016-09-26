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
import com.bitfire.postprocessing.effects.CubemapEquirectangular;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;

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

    CubemapEquirectangular cubemapEffect;

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

        cubemapEffect = new CubemapEquirectangular();
    }

    @Override
    public void render(SceneGraphRenderer sgr, ICamera camera, float t, int rw, int rh, FrameBuffer fb, PostProcessBean ppb) {

        PerspectiveCamera cam = camera.getCamera();

        float fovbak = cam.fieldOfView;
        dirbak.set(cam.direction);
        upbak.set(cam.up);

        EventManager.instance.post(Events.FOV_CHANGED_CMD, 90f);

        FrameBuffer mainfb = getFrameBuffer(rw, rh);

        // The sides of the cubemap must be square. We use the max of our resolution
        int wh = 400;
        FrameBuffer zposfb = getFrameBuffer(wh, wh, 0);
        FrameBuffer znegfb = getFrameBuffer(wh, wh, 1);
        FrameBuffer xposfb = getFrameBuffer(wh, wh, 2);
        FrameBuffer xnegfb = getFrameBuffer(wh, wh, 3);
        FrameBuffer yposfb = getFrameBuffer(wh, wh, 4);
        FrameBuffer ynegfb = getFrameBuffer(wh, wh, 5);

        Viewport viewport = stretchViewport;
        camera.setViewport(viewport);
        viewport.setCamera(cam);
        viewport.setWorldSize(wh, wh);
        viewport.setScreenBounds(0, 0, wh, wh);
        viewport.apply();

        boolean postproc;

        // FRONT +Z
        cam.direction.set(dirbak);
        cam.up.set(upbak);
        cam.update();
        postproc = postprocessCapture(ppb, zposfb, wh, wh);
        sgr.renderScene(camera, t, rc);
        postprocessRender(ppb, zposfb, postproc, camera);

        Texture zpos = zposfb.getColorBufferTexture();

        // UP +Y
        cam.direction.set(dirbak);
        cam.up.set(upbak);
        aux1.set(cam.direction);
        aux2.set(cam.up);
        aux1.crs(aux2);
        cam.direction.rotate(aux1, 90);
        cam.up.rotate(aux1, 90);
        cam.update();

        postproc = postprocessCapture(ppb, yposfb, wh, wh);
        sgr.renderScene(camera, t, rc);
        postprocessRender(ppb, yposfb, postproc, camera);

        Texture ypos = yposfb.getColorBufferTexture();

        // BOTTOM -Y
        cam.direction.set(dirbak);
        cam.up.set(upbak);
        aux1.set(dirbak);
        aux2.set(upbak);
        aux1.crs(aux2);
        cam.direction.set(dirbak).rotate(aux1, -90);
        cam.up.set(upbak).rotate(aux1, -90);
        cam.update();

        postproc = postprocessCapture(ppb, ynegfb, wh, wh);
        sgr.renderScene(camera, t, rc);
        postprocessRender(ppb, ynegfb, postproc, camera);

        Texture yneg = ynegfb.getColorBufferTexture();

        // LEFT -X
        cam.direction.set(dirbak);
        cam.up.set(upbak);
        cam.up.set(upbak);
        cam.direction.set(dirbak).rotate(upbak, 90);
        cam.update();

        postproc = postprocessCapture(ppb, xnegfb, wh, wh);
        sgr.renderScene(camera, t, rc);
        postprocessRender(ppb, xnegfb, postproc, camera);

        Texture xneg = xnegfb.getColorBufferTexture();

        // RIGHT +X
        cam.direction.set(dirbak);
        cam.up.set(upbak);
        cam.up.set(upbak);
        cam.direction.set(dirbak).rotate(upbak, -90);
        cam.update();

        postproc = postprocessCapture(ppb, xposfb, wh, wh);
        sgr.renderScene(camera, t, rc);
        postprocessRender(ppb, xposfb, postproc, camera);

        Texture xpos = xposfb.getColorBufferTexture();

        // BACK -Z
        cam.direction.set(dirbak);
        cam.up.set(upbak);
        cam.up.set(upbak);
        cam.direction.set(dirbak).rotate(upbak, -180);
        cam.update();

        postproc = postprocessCapture(ppb, znegfb, wh, wh);
        sgr.renderScene(camera, t, rc);
        postprocessRender(ppb, znegfb, postproc, camera);

        Texture zneg = znegfb.getColorBufferTexture();

        // Restore camera parameters
        cam.direction.set(dirbak);
        cam.up.set(upbak);

        // Effect
        cubemapEffect.setSides(xpos.getTextureData(), xneg.getTextureData(), ypos.getTextureData(), yneg.getTextureData(), zpos.getTextureData(), zneg.getTextureData());
        cubemapEffect.render(znegfb, fb);

        if (fb != null)
            fb.end();

        // ensure default texture unit #0 is active
        Gdx.gl.glActiveTexture(GL20.GL_TEXTURE0);

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
