package gaia.cu9.ari.gaiaorbit.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;

public class SGRCubemap extends SGRAbstract implements ISGR {

    Vector3 aux1, aux2, aux3, dirbak, upbak;

    StretchViewport stretchViewport;

    public SGRCubemap() {
        super();
        aux1 = new Vector3();
        aux3 = new Vector3();
        aux2 = new Vector3();
        dirbak = new Vector3();
        upbak = new Vector3();
        stretchViewport = new StretchViewport(Gdx.graphics.getHeight(), Gdx.graphics.getHeight());
    }

    @Override
    public void render(SceneGraphRenderer sgr, ICamera camera, int rw, int rh, FrameBuffer fb, PostProcessBean ppb) {
        boolean postproc = postprocessCapture(ppb, fb, rw, rh);

        PerspectiveCamera cam = camera.getCamera();

        float fovbak = cam.fieldOfView;
        dirbak.set(cam.direction);
        upbak.set(cam.up);

        EventManager.instance.post(Events.FOV_CHANGED_CMD, 90f);

        int sizew = rw / 4;
        int sizeh = rh / 3;

        // FRONT
        Viewport viewport = stretchViewport;
        camera.setViewport(viewport);
        viewport.setCamera(cam);
        viewport.setWorldSize(sizeh, sizeh);
        viewport.setScreenBounds(sizew, sizeh, sizew, sizeh);
        viewport.apply();

        sgr.renderScene(camera, rc);

        // UP
        viewport.setScreenBounds(sizew, sizeh * 2, sizew, sizeh);
        viewport.apply();

        aux1.set(cam.direction);
        aux2.set(cam.up);
        aux1.crs(aux2);
        cam.direction.rotate(aux1, 90);
        cam.up.rotate(aux1, 90);
        cam.update();

        sgr.renderScene(camera, rc);

        // BOTTOM
        viewport.setScreenBounds(sizew, 0, sizew, sizeh);
        viewport.apply();

        aux1.set(dirbak);
        aux2.set(upbak);
        aux1.crs(aux2);
        cam.direction.set(dirbak).rotate(aux1, -90);
        cam.up.set(upbak).rotate(aux1, -90);
        cam.update();

        sgr.renderScene(camera, rc);

        // LEFT
        viewport.setScreenBounds(0, sizeh, sizew, sizeh);
        viewport.apply();

        cam.up.set(upbak);
        cam.direction.set(dirbak).rotate(upbak, 90);
        cam.update();

        sgr.renderScene(camera, rc);

        // RIGHT
        viewport.setScreenBounds(sizew * 2, sizeh, sizew, sizeh);
        viewport.apply();

        cam.up.set(upbak);
        cam.direction.set(dirbak).rotate(upbak, -90);
        cam.update();

        sgr.renderScene(camera, rc);

        // BACK
        viewport.setScreenBounds(sizew * 3, sizeh, sizew, sizeh);
        viewport.apply();

        cam.up.set(upbak);
        cam.direction.set(dirbak).rotate(upbak, -180);
        cam.update();

        sgr.renderScene(camera, rc);

        postprocessRender(ppb, fb, postproc, camera);

        // Restore camera parameters
        cam.direction.set(dirbak);
        cam.up.set(upbak);

        EventManager.instance.post(Events.FOV_CHANGED_CMD, fovbak);

    }

    @Override
    public void resize(int w, int h) {

    }

    @Override
    public void dispose() {
    }

}
