package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;

import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public class SpacecraftCamera extends AbstractCamera implements IObserver {

    /** Camera far value **/
    public static final double CAM_FAR = 1e6 * Constants.PC_TO_U;
    /** Camera near values **/
    public static final double CAM_NEAR = 1e5 * Constants.KM_TO_U;

    /** Force, acceleration and velocity **/
    public Vector3d force, accel, vel;

    public Vector3d direction, up;

    public SpacecraftCamera(CameraManager parent) {
        super(parent);
        force = new Vector3d();
        accel = new Vector3d();
        vel = new Vector3d();
        direction = new Vector3d();
        up = new Vector3d();

        camera = new PerspectiveCamera(GlobalConf.scene.CAMERA_FOV, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = (float) CAM_NEAR;
        camera.far = (float) CAM_FAR;
    }

    @Override
    public PerspectiveCamera getCamera() {
        return this.camera;
    }

    @Override
    public void setCamera(PerspectiveCamera cam) {
        this.camera = cam;
    }

    @Override
    public PerspectiveCamera getCameraStereoLeft() {
        return null;
    }

    @Override
    public PerspectiveCamera getCameraStereoRight() {
        return null;
    }

    @Override
    public void setCameraStereoLeft(PerspectiveCamera cam) {
    }

    @Override
    public void setCameraStereoRight(PerspectiveCamera cam) {
    }

    @Override
    public PerspectiveCamera[] getFrontCameras() {
        return new PerspectiveCamera[] { camera };
    }

    @Override
    public Vector3d getDirection() {
        return direction;
    }

    @Override
    public Vector3d getUp() {
        return up;
    }

    @Override
    public Vector3d[] getDirections() {
        return new Vector3d[] { direction };
    }

    @Override
    public int getNCameras() {
        return 1;
    }

    @Override
    public void update(float dt, ITimeFrameProvider time) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateMode(CameraMode mode, boolean postEvent) {

    }

    @Override
    public CameraMode getMode() {
        return parent.mode;
    }

    @Override
    public double getVelocity() {
        return parent.getVelocity();
    }

    @Override
    public CelestialBody getFocus() {
        return null;
    }

    @Override
    public boolean isFocus(CelestialBody cb) {
        return false;
    }

    @Override
    public void notify(Events event, Object... data) {
        // TODO Auto-generated method stub

    }

}
