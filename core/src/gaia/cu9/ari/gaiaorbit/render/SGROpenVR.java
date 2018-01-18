package gaia.cu9.ari.gaiaorbit.render;

import java.util.HashMap;
import java.util.Map;

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
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.scenegraph.StubModel;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
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

    private ModelBatch modelBatch;
    private Array<StubModel> controllerObjects;
    private Map<VRDevice, StubModel> vrdevices;
    private Environment controllersEnv;

    private Vector3 auxf1;
    private Vector3d auxd1;

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
            auxf1 = new Vector3();
            auxd1 = new Vector3d();

            // Controllers
            Array<VRDevice> controllers = vrContext.getDevicesByType(VRDeviceType.Controller);

            // Env
            controllersEnv = new Environment();
            controllersEnv.set(new ColorAttribute(ColorAttribute.AmbientLight, 1f, 1f, 1f, 1f));
            DirectionalLight dlight = new DirectionalLight();
            dlight.color.set(1f, 0f, 0f, 1f);
            dlight.direction.set(0, 1, 0);
            controllersEnv.add(dlight);

            // Controller objects
            vrdevices = new HashMap<VRDevice, StubModel>();
            controllerObjects = new Array<StubModel>(controllers.size);
            for (VRDevice controller : controllers) {
                addVRController(controller);
            }

            EventManager.instance.subscribe(this, Events.FRAME_SIZE_UDPATE, Events.SCREENSHOT_SIZE_UDPATE, Events.VR_DEVICE_CONNECTED, Events.VR_DEVICE_DISCONNECTED);
        }
    }

    @Override
    public void render(SceneGraphRenderer sgr, ICamera camera, double t, int rw, int rh, FrameBuffer fb, PostProcessBean ppb) {
        if (vrContext != null) {
            rc.ppb = null;

            vrContext.pollEvents();

            // Add controllers
            float camnearbak = camera.getCamera().near;
            double closestDist = camera.getClosest().distToCamera;
            boolean r = false;
            for (StubModel controller : controllerObjects) {
                Vector3 devicepos = controller.getDevice().getPosition(Space.Tracker);
                // Length from headset to controller
                auxd1.set(devicepos).sub(vrContext.getDeviceByType(VRDeviceType.HeadMountedDisplay).getPosition(Space.Tracker));
                double controllerDist = auxd1.len();
                if (camnearbak < controllerDist || closestDist / controllerDist < 0.5) {
                    controller.addToRenderLists(RenderGroup.MODEL_NORMAL);
                    controller.setDelayRender(false);
                } else {
                    controller.addToRenderLists(null);
                    controller.setDelayRender(true);
                    r = true;
                }

            }

            /** LEFT EYE **/

            // Camera to left
            updateCamera((NaturalCamera) camera.getCurrent(), camera.getCamera(), 0, false, rc, camnearbak);

            boolean postproc = postprocessCapture(ppb, fbLeft, rw, rh);

            // Render scene
            sgr.renderScene(camera, t, rc);
            // Camera
            camera.render(rw, rh);

            if (r) {
                updateCamera((NaturalCamera) camera.getCurrent(), camera.getCamera(), 0, false, rc, 0.1f);
                Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
                modelBatch.begin(camera.getCamera());
                for (StubModel controller : controllerObjects) {
                    if (controller.getDelayRender())
                        modelBatch.render(controller.instance, controllersEnv);
                }
                modelBatch.end();
            }

            postprocessRender(ppb, fbLeft, postproc, camera, rw, rh);

            /** RIGHT EYE **/

            // Camera to right
            updateCamera((NaturalCamera) camera.getCurrent(), camera.getCamera(), 1, false, rc, camnearbak);

            postproc = postprocessCapture(ppb, fbRight, rw, rh);

            // Render scene
            sgr.renderScene(camera, t, rc);
            // Camera
            camera.render(rw, rh);

            if (r) {
                updateCamera((NaturalCamera) camera.getCurrent(), camera.getCamera(), 1, false, rc, 0.1f);
                Gdx.gl.glClear(GL20.GL_DEPTH_BUFFER_BIT);
                modelBatch.begin(camera.getCamera());
                for (StubModel controller : controllerObjects) {
                    if (controller.getDelayRender())
                        modelBatch.render(controller.instance, controllersEnv);
                }
                modelBatch.end();
            }

            postprocessRender(ppb, fbRight, postproc, camera, rw, rh);

            /** SUBMIT TO VR COMPOSITOR **/
            VRCompositor.VRCompositor_Submit(VR.EVREye_Eye_Left, texLeft, null, VR.EVRSubmitFlags_Submit_Default);
            VRCompositor.VRCompositor_Submit(VR.EVREye_Eye_Right, texRight, null, VR.EVRSubmitFlags_Submit_Default);
        }

    }

    private void updateCamera(NaturalCamera cam, PerspectiveCamera camera, int eye, boolean updateFrustum, RenderingContext rc, float near) {
        // get the projection matrix from the HDM 
        VRSystem.VRSystem_GetProjectionMatrix(eye, near, camera.far, projectionMat);
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
        cam.vroffset.set(pos).scl(0.1);
        cam.direction.set(dir);
        cam.up.set(up);
        rc.vroffset = cam.vroffset;

        // Update Eye camera
        //pos.set(0, 0, 0);
        camera.view.idt();
        camera.view.setToLookAt(pos, auxf1.set(pos).add(dir), up);

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

    private void addVRController(VRDevice device) {
        if (!vrdevices.containsKey(device)) {
            StubModel sm = new StubModel(device, controllersEnv);
            controllerObjects.add(sm);
            vrdevices.put(device, sm);
        }
    }

    private void removeVRController(VRDevice device) {
        if (vrdevices.containsKey(device)) {
            StubModel sm = vrdevices.get(device);
            controllerObjects.removeValue(sm, true);
            vrdevices.remove(device);
        }
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case VR_DEVICE_CONNECTED:
            VRDevice device = (VRDevice) data[0];
            if (device.getType() == VRDeviceType.Controller) {
                Gdx.app.postRunnable(() -> {
                    addVRController(device);
                });
            }
            break;
        case VR_DEVICE_DISCONNECTED:
            device = (VRDevice) data[0];
            if (device.getType() == VRDeviceType.Controller) {
                Gdx.app.postRunnable(() -> {
                    removeVRController(device);
                });
            }
            break;
        default:
            break;
        }

    }

}
