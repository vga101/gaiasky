package gaia.cu9.ari.gaiaorbit.render;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;

import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;

public class SGR extends SGRAbstract implements ISGR {

    public SGR() {
        super();
    }

    @Override
    public void render(SceneGraphRenderer sgr, ICamera camera, int rw, int rh, FrameBuffer fb, PostProcessBean ppb) {
        boolean postproc = postprocessCapture(ppb, fb, rw, rh);

        camera.setViewport(extendViewport);
        extendViewport.setCamera(camera.getCamera());
        extendViewport.setWorldSize(rw, rh);
        extendViewport.setScreenSize(rw, rh);
        extendViewport.apply();
        sgr.renderScene(camera, rc);

        postprocessRender(ppb, fb, postproc, camera);

    }

    @Override
    public void resize(int w, int h) {

    }

    @Override
    public void dispose() {
    }

}
