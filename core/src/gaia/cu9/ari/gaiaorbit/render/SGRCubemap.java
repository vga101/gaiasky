package gaia.cu9.ari.gaiaorbit.render;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;

public class SGRCubemap extends SGRAbstract implements ISGR {

    Vector3 aux1, aux2, aux3, dirbak, upbak;

    public SGRCubemap() {
        super();
        aux1 = new Vector3();
        aux3 = new Vector3();
        aux2 = new Vector3();
        dirbak = new Vector3();
        upbak = new Vector3();
    }

    @Override
    public void render(SceneGraphRenderer sgr, ICamera camera, int rw, int rh, FrameBuffer fb, PostProcessBean ppb) {
        boolean postproc = postprocessCapture(ppb, fb, rw, rh);

        PerspectiveCamera cam = camera.getCamera();
        float fovbak = cam.fieldOfView;
        dirbak.set(cam.direction);
        upbak.set(cam.up);

        cam.fieldOfView = 90;

        int sizew = rw / 4;
        int sizeh = rh / 3;

        // FRONT

        camera.setViewport(extendViewport);
        extendViewport.setCamera(cam);
        extendViewport.setWorldSize(sizew, sizeh);

        // UP
        extendViewport.setScreenBounds(sizew, sizeh, sizew, sizeh);
        extendViewport.apply();

        sgr.renderScene(camera, rc);

        // BOTTOM
        extendViewport.setScreenBounds(sizew, sizeh * 2, sizew, sizeh);
        extendViewport.apply();

        aux1.set(cam.direction);
        aux2.set(cam.up);
        aux1.crs(aux2);
        cam.direction.rotate(aux1, 90);
        cam.up.rotate(aux1, 90);
        cam.update();

        sgr.renderScene(camera, rc);

        // LEFT

        // RIGHT

        // BACK

        postprocessRender(ppb, fb, postproc, camera);

    }

    @Override
    public void resize(int w, int h) {

    }

    @Override
    public void dispose() {
    }

}
