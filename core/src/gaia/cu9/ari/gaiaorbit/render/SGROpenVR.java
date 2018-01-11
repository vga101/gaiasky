package gaia.cu9.ari.gaiaorbit.render;

import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.HmdMatrix44;
import org.lwjgl.openvr.Texture;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRCompositor;
import org.lwjgl.openvr.VRSystem;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor.PostProcessBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.NaturalCamera;
import gaia.cu9.ari.gaiaorbit.vr.VRContext;
import gaia.cu9.ari.gaiaorbit.vr.VRContext.Space;
import gaia.cu9.ari.gaiaorbit.vr.VRContext.VRDevice;
import gaia.cu9.ari.gaiaorbit.vr.VRContext.VRDeviceType;

/**
 * Renders to OpenVR. Renders basically two scenes, one for each eye, using the
 * OpenVR context.
 * 
 * @author tsagrista
 *
 */
public class SGROpenVR extends SGRAbstract implements ISGR, IObserver {

    private VRContext vrContext;

    /** Frame buffers for each eye **/
    FrameBuffer fbLeft, fbRight;
    /** Textures **/
    Texture texLeft, texRight;

    HmdMatrix44 projectionMat = HmdMatrix44.create();
    HmdMatrix34 eyeMat = HmdMatrix34.create();

    public final Matrix4 eyeSpace = new Matrix4();
    public final Matrix4 invEyeSpace = new Matrix4();

    private Vector3 tmp;

    public SGROpenVR(VRContext vrContext) {
        super();
        // VR Context
        this.vrContext = vrContext;

        // Left eye, fb and texture
        fbLeft = new FrameBuffer(Format.RGBA8888, vrContext.getWidth(), vrContext.getHeight(), true);
        texLeft = org.lwjgl.openvr.Texture.create();
        texLeft.set(fbLeft.getColorBufferTexture().getTextureObjectHandle(), VR.ETextureType_TextureType_OpenGL, VR.EColorSpace_ColorSpace_Gamma);

        // Right eye, fb and texture
        fbRight = new FrameBuffer(Format.RGBA8888, vrContext.getWidth(), vrContext.getHeight(), true);
        texRight = org.lwjgl.openvr.Texture.create();
        texRight.set(fbRight.getColorBufferTexture().getTextureObjectHandle(), VR.ETextureType_TextureType_OpenGL, VR.EColorSpace_ColorSpace_Gamma);

        // Aux vectors
        tmp = new Vector3();

        EventManager.instance.subscribe(this, Events.FRAME_SIZE_UDPATE, Events.SCREENSHOT_SIZE_UDPATE);
    }

    @Override
    public void render(SceneGraphRenderer sgr, ICamera camera, double t, int rw, int rh, FrameBuffer fb, PostProcessBean ppb) {
        rc.ppb = null;

        vrContext.pollEvents();

        /** LEFT EYE **/

        // Camera to left
        updateCamera((NaturalCamera) camera.getCurrent(), camera.getCamera(), 0, true);

        boolean postproc = postprocessCapture(ppb, fbLeft, rw, rh);
        sgr.renderScene(camera, t, rc);
        postprocessRender(ppb, fbLeft, postproc, camera, rw, rh);

        /** RIGHT EYE **/

        // Camera to right
        updateCamera((NaturalCamera) camera.getCurrent(), camera.getCamera(), 1, true);

        postproc = postprocessCapture(ppb, fbRight, rw, rh);
        sgr.renderScene(camera, t, rc);
        postprocessRender(ppb, fbRight, postproc, camera, rw, rh);

        /** SUBMIT TO VR COMPOSITOR **/
        VRCompositor.VRCompositor_Submit(VR.EVREye_Eye_Left, texLeft, null, VR.EVRSubmitFlags_Submit_Default);
        VRCompositor.VRCompositor_Submit(VR.EVREye_Eye_Right, texRight, null, VR.EVRSubmitFlags_Submit_Default);

    }

    private void clear() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    private void updateCamera(NaturalCamera cam, PerspectiveCamera camera, int eye, boolean updateFrustum) {
        // get the projection matrix from the HDM 
        VRSystem.VRSystem_GetProjectionMatrix(eye, camera.near, camera.far, projectionMat);
        VRContext.hmdMat4toMatrix4(projectionMat, camera.projection);

        // get the eye space matrix from the HDM
        VRSystem.VRSystem_GetEyeToHeadTransform(eye, eyeMat);
        VRContext.hmdMat34ToMatrix4(eyeMat, eyeSpace);
        invEyeSpace.set(eyeSpace).inv();

        // get the pose matrix from the HDM
        VRDevice hmd = vrContext.getDeviceByType(VRDeviceType.HeadMountedDisplay);
        Vector3 up = hmd.getUp(Space.Tracker);
        Vector3 dir = hmd.getDirection(Space.Tracker);
        Vector3 pos = hmd.getPosition(Space.Tracker);

        camera.view.idt();
        camera.view.setToLookAt(pos, tmp.set(pos).add(dir), up);

        // Update main camera
        cam.vroffset.set(pos);
        cam.direction.set(dir);
        cam.up.set(up);

        camera.combined.set(camera.projection);
        Matrix4.mul(camera.combined.val, invEyeSpace.val);
        Matrix4.mul(camera.combined.val, camera.view.val);

        if (updateFrustum) {
            camera.invProjectionView.set(camera.combined);
            Matrix4.inv(camera.invProjectionView.val);
            camera.frustum.update(camera.invProjectionView);
        }
    }

    public void resize(final int w, final int h) {

    }

    public void dispose() {
        vrContext.dispose();
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        default:
            break;
        }

    }

}
