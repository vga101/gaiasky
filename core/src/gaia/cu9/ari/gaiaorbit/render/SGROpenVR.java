package gaia.cu9.ari.gaiaorbit.render;

import org.lwjgl.openvr.HmdMatrix34;
import org.lwjgl.openvr.HmdMatrix44;
import org.lwjgl.openvr.Texture;
import org.lwjgl.openvr.VR;
import org.lwjgl.openvr.VRCompositor;
import org.lwjgl.openvr.VRSystem;

import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

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

    /** Model batch to render controllers **/
    ModelBatch modelBatch;

    HmdMatrix44 projectionMat = HmdMatrix44.create();
    HmdMatrix34 eyeMat = HmdMatrix34.create();

    public final Matrix4 eyeSpace = new Matrix4();
    public final Matrix4 invEyeSpace = new Matrix4();

    private Array<VRDevice> controllers;
    private Environment controllersEnv;

    private Vector3 tmp;

    public SGROpenVR(VRContext vrContext, ModelBatch modelBatch) {
        super();
        // VR Context
        this.vrContext = vrContext;
        // Model batch
        this.modelBatch = modelBatch;

        if (vrContext != null) {
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

            // Controllers
            controllers = vrContext.getDevicesByType(VRDeviceType.Controller);

            // Env
            controllersEnv = new Environment();
            controllersEnv.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
            DirectionalLight dlight = new DirectionalLight();
            dlight.color.set(1f, 0f, 0f, 1f);
            dlight.direction.set(0, 1, 0);
            controllersEnv.add(dlight);

            EventManager.instance.subscribe(this, Events.FRAME_SIZE_UDPATE, Events.SCREENSHOT_SIZE_UDPATE);
        }
    }

    @Override
    public void render(SceneGraphRenderer sgr, ICamera camera, double t, int rw, int rh, FrameBuffer fb, PostProcessBean ppb) {
        if (vrContext != null) {
            rc.ppb = null;

            vrContext.pollEvents();

            /** LEFT EYE **/

            // Camera to left
            updateCamera((NaturalCamera) camera.getCurrent(), camera.getCamera(), 0, true, rc);

            boolean postproc = postprocessCapture(ppb, fbLeft, rw, rh);

            // Render scene
            sgr.renderScene(camera, t, rc);

            // Render controllers
            float near = camera.getCamera().near;
            float far = camera.getCamera().far;
            camera.getCamera().near = 0.01f;
            camera.getCamera().far = 100f;
            camera.getCamera().update();
            modelBatch.begin(camera.getCamera());
            for (VRDevice controller : controllers) {
                modelBatch.render(controller.getModelInstance(), controllersEnv);
            }
            modelBatch.end();
            camera.getCamera().near = near;
            camera.getCamera().far = far;
            camera.getCamera().update();

            postprocessRender(ppb, fbLeft, postproc, camera, rw, rh);

            /** RIGHT EYE **/

            // Camera to right
            updateCamera((NaturalCamera) camera.getCurrent(), camera.getCamera(), 1, true, rc);

            postproc = postprocessCapture(ppb, fbRight, rw, rh);

            // Render scene
            sgr.renderScene(camera, t, rc);

            // Render controllers
            camera.getCamera().near = 0.01f;
            camera.getCamera().far = 100f;
            camera.getCamera().update();
            modelBatch.begin(camera.getCamera());
            for (VRDevice controller : controllers) {
                modelBatch.render(controller.getModelInstance(), controllersEnv);
            }
            modelBatch.end();
            camera.getCamera().near = near;
            camera.getCamera().far = far;
            camera.getCamera().update();

            postprocessRender(ppb, fbRight, postproc, camera, rw, rh);

            /** SUBMIT TO VR COMPOSITOR **/
            VRCompositor.VRCompositor_Submit(VR.EVREye_Eye_Left, texLeft, null, VR.EVRSubmitFlags_Submit_Default);
            VRCompositor.VRCompositor_Submit(VR.EVREye_Eye_Right, texRight, null, VR.EVRSubmitFlags_Submit_Default);
        }

    }

    private void updateCamera(NaturalCamera cam, PerspectiveCamera camera, int eye, boolean updateFrustum, RenderingContext rc) {
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

        // Update main camera
        cam.vroffset.set(pos);
        cam.direction.set(dir);
        cam.up.set(up);
        rc.vroffset = cam.vroffset;

        // Update Eye camera
        //pos.set(0, 0, 0);
        camera.view.idt();
        camera.view.setToLookAt(pos, tmp.set(pos).add(dir), up);

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
        if (fbLeft != null)
            fbLeft.dispose();
        if (fbRight != null)
            fbRight.dispose();
        if (vrContext != null) {
            vrContext.dispose();
        }
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        default:
            break;
        }

    }

}
