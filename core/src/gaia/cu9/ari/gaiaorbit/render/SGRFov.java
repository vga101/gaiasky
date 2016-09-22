package gaia.cu9.ari.gaiaorbit.render;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;

import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;

/**
 * Renders the Gaia Field of View camera mode. Positions two cameras inside 
 * gaia, each looking through one of the apertures, and renders them in the same
 * viewport with a CCD texture.
 * @author tsagrista
 *
 */
public class SGRFov extends SGRAbstract implements ISGR {

    public SGRFov() {
        super();
    }

    @Override
    public void render(SceneGraphRenderer sgr, ICamera camera, float t, int rw, int rh, FrameBuffer fb, PostProcessBean ppb) {
        boolean postproc = postprocessCapture(ppb, fb, rw, rh);

        /** FIELD OF VIEW CAMERA **/

        CameraMode aux = camera.getMode();

        camera.updateMode(CameraMode.Gaia_FOV1, false);

        sgr.renderScene(camera, t, rc);

        camera.updateMode(CameraMode.Gaia_FOV2, false);

        sgr.renderScene(camera, t, rc);

        camera.updateMode(aux, false);

        postprocessRender(ppb, fb, postproc, camera);

    }

    @Override
    public void resize(int w, int h) {

    }

    @Override
    public void dispose() {
    }
}
