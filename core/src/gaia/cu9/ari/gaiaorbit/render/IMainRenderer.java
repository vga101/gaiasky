package gaia.cu9.ari.gaiaorbit.render;

import com.badlogic.gdx.graphics.glutils.FrameBuffer;

import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;

public interface IMainRenderer {

    public FrameBuffer getFrameBuffer(int w, int h);

    public void preRenderScene();

    public void renderSgr(ICamera camera, float dt, int width, int height, FrameBuffer frameBuffer, PostProcessBean ppb);

    public ICamera getICamera();

    public float getT();

    public CameraManager getCameraManager();

    public IPostProcessor getPostProcessor();
}
