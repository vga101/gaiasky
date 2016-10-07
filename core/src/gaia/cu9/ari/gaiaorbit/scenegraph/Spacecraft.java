package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * The spacecraft in the spacecraft camera mode
 * @author tsagrista
 *
 */
public class Spacecraft extends AbstractPositionEntity implements IModelRenderable, IObserver {
    private ModelComponent mc;
    private boolean render;
    private float size;
    private float frontDist;
    private float downDist;

    public Spacecraft() {
        super();
        localTransform = new Matrix4();
        EventManager.instance.subscribe(this, Events.CAMERA_MODE_CMD);

        // 150m front
        frontDist = -150f * (float) Constants.M_TO_U;
        // 40m down
        downDist = -40 * (float) Constants.M_TO_U;
    }

    public void initialize() {
        opacity = 1;
        if (mc != null) {
            mc.initialize();
        }

        EventManager.instance.subscribe(this, Events.CAMERA_MODE_CMD);
    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);
        if (mc != null) {
            mc.doneLoading(manager, localTransform, null);
        }
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case CAMERA_MODE_CMD:
            CameraMode mode = (CameraMode) data[0];
            if (mode == CameraMode.Spacecraft) {
                render = true;
            } else {
                render = false;
            }
            break;
        }

    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
        if (render) {
            this.distToCamera = frontDist;
            super.updateLocal(time, camera);
            // Update light with global position
            if (mc != null) {
                // Update directional light
                mc.dlight.direction.set(0f, 0f, 0f);
                if (ModelBody.closestCamStar != null) {
                    float intensity = (float) MathUtilsd.lint(ModelBody.closestCamStar.distToCamera / MathUtilsd.lint(ModelBody.closestCamStar.size, 0.6e6, 1e8, 1, 0.05), 0, 2 * Constants.PC_TO_U, 1f, 0f);
                    mc.dlight.direction.sub(ModelBody.closestCamStar.transform.getTranslationf(aux3f1.get()));
                    mc.dlight.color.set(ModelBody.closestCamStar.cc[0] * intensity, ModelBody.closestCamStar.cc[1] * intensity, ModelBody.closestCamStar.cc[2] * intensity, 1.0f);
                } else {
                    mc.dlight.direction.add((float) camera.getPos().x, (float) camera.getPos().y, (float) camera.getPos().z);
                    mc.dlight.color.set(1f, 1f, 1f, 0f);
                }
            }
            // Local transform
            PerspectiveCamera cam = camera.getCamera();
            localTransform.setToLookAt(cam.position, cam.direction, cam.up).inv();
            localTransform.translate(0, downDist, frontDist);
            localTransform.scale(size, size, size);

            addToRenderLists(camera);
        }
    }

    /**
     * Adds this entity to the necessary render lists after the
     * distance to the camera and the view angle have been determined.
     */
    protected void addToRenderLists(ICamera camera) {
        addToRender(this, RenderGroup.MODEL_F);
    }

    public void setModel(ModelComponent mc) {
        this.mc = mc;
    }

    /**
     * Sets the absolute size of this entity
     * @param size
     */
    public void setSize(Double size) {
        this.size = size.floatValue() * (float) Constants.KM_TO_U;
    }

    public void setSize(Long size) {
        this.size = (float) size * (float) Constants.KM_TO_U;
    }

    @Override
    public void render(Object... params) {
        if (params[0] instanceof ModelBatch)
            render((ModelBatch) params[0], (Float) params[1], (Float) params[2]);
    }

    @Override
    public float getDistToCamera() {
        return 0;
    }

    @Override
    public void render(ModelBatch modelBatch, float alpha, float t) {
        mc.setTransparency(alpha * opacity);
        modelBatch.render(mc.instance, mc.env);
    }

    @Override
    public boolean hasAtmosphere() {
        return false;
    }

    public void dispose() {
        super.dispose();
        mc.dispose();
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
        // TODO Auto-generated method stub

    }

}
