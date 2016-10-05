package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.Ray;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.viewport.Viewport;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.MyPools;
import gaia.cu9.ari.gaiaorbit.util.TwoWayHashmap;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public class CameraManager implements ICamera, IObserver {
    protected static Pool<Vector3d> v3dpool = MyPools.get(Vector3d.class);
    protected static Pool<Vector3> v3pool = MyPools.get(Vector3.class);

    /**
     * Convenience enum to describe the camera mode
     * @author Toni Sagrista
     *
     */
    public enum CameraMode {
        /** Free navigation **/
        Free_Camera,
        /** Focus **/
        Focus,
        /** Gaia Scene **/
        Gaia_Scene,
        /** FOV1 **/
        Gaia_FOV1,
        /** FOV2 **/
        Gaia_FOV2,
        /** Both fields of view **/
        Gaia_FOV1and2;

        static TwoWayHashmap<String, CameraMode> equivalences;

        static {
            String fc = "Free camera";
            String foc = "Focus object";
            String gs = "Gaia scene";
            String f1 = "Gaia FoV 1";
            String f2 = "Gaia FoV 2";
            String f12 = "Gaia FoV1 and FoV2";

            equivalences = new TwoWayHashmap<String, CameraMode>();
            equivalences.add(fc, Free_Camera);
            equivalences.add(foc, Focus);
            equivalences.add(gs, Gaia_Scene);
            equivalences.add(f1, Gaia_FOV1);
            equivalences.add(f2, Gaia_FOV2);
            equivalences.add(f12, Gaia_FOV1and2);

        }

        public static CameraMode getMode(int idx) {
            if (idx >= 0 && idx < CameraMode.values().length) {
                return CameraMode.values()[idx];
            } else {
                return null;
            }
        }

        @Override
        public String toString() {
            return equivalences.getBackward(this);
        }

        public static CameraMode fromString(String str) {
            return equivalences.getForward(str);
        }

        public boolean isGaiaFov() {
            return this.equals(CameraMode.Gaia_FOV1) || this.equals(CameraMode.Gaia_FOV2) || this.equals(CameraMode.Gaia_FOV1and2);
        }
    }

    public CameraMode mode;

    public ICamera current;

    public NaturalCamera naturalCamera;
    public FovCamera fovCamera;

    /** Last position, for working out velocity **/
    private Vector3d lastPos;

    /** Are we moving at high speeds? **/
    private boolean supervelocity;

    /** Current velocity in km/h **/
    private double speed;
    /** Velocity vector **/
    private Vector3d velocity;

    public CameraManager(AssetManager manager, CameraMode mode) {
        // Initialize
        // Initialize Cameras
        naturalCamera = new NaturalCamera(manager, this);
        fovCamera = new FovCamera(manager, this);
        this.mode = mode;
        lastPos = new Vector3d();
        velocity = new Vector3d();
        supervelocity = true;

        updateCurrentCamera();

        EventManager.instance.subscribe(this, Events.CAMERA_MODE_CMD, Events.FOV_CHANGE_NOTIFICATION);
    }

    public void updateCurrentCamera() {

        // Update
        switch (mode) {
        case Free_Camera:
        case Focus:
        case Gaia_Scene:
            current = naturalCamera;
            break;
        case Gaia_FOV1:
        case Gaia_FOV2:
        case Gaia_FOV1and2:
            current = fovCamera;
            break;
        }

    }

    public boolean isNatural() {
        return current == naturalCamera;
    }

    @Override
    public PerspectiveCamera getCamera() {
        return current.getCamera();
    }

    @Override
    public float getFovFactor() {
        return current.getFovFactor();
    }

    @Override
    public Viewport getViewport() {
        return current.getViewport();
    }

    @Override
    public void setViewport(Viewport viewport) {
        current.setViewport(viewport);
    }

    @Override
    public Vector3d getPos() {
        return current.getPos();
    }

    @Override
    public Vector3d getInversePos() {
        return current.getInversePos();
    }

    @Override
    public Vector3d getDirection() {
        return current.getDirection();
    }

    @Override
    public Vector3d getUp() {
        return current.getUp();
    }

    /**
     * Update method.
     * @param dt Delta time in seconds.
     * @param time The time frame provider.
     */
    public void update(float dt, ITimeFrameProvider time) {
        current.update(dt, time);
        if (current != fovCamera && GlobalConf.scene.COMPUTE_GAIA_SCAN) {
            fovCamera.updateDirections(time);
        }

        // Speed = dx/dt
        velocity.set(lastPos).sub(current.getPos());
        speed = (velocity.len() * Constants.U_TO_KM) / (dt * Constants.S_TO_H);

        // Post event with camera motion parameters
        EventManager.instance.post(Events.CAMERA_MOTION_UPDATED, current.getPos(), speed, velocity.nor(), current.getCamera());

        // Update last pos
        lastPos.set(current.getPos());

        int screenX = Gdx.input.getX();
        int screenY = Gdx.input.getY();

        // Update Pointer Alpha/Delta
        updatePointerRADEC(screenX, screenY);
        // Update Pointer LAT/LON
        updateFocusLatLon(screenX, screenY);
    }

    private void updatePointerRADEC(int screenX, int screenY) {
        Vector3 vec = v3pool.obtain().set(screenX, screenY, 0.5f);
        ICamera camera = current;
        camera.getCamera().unproject(vec);

        Vector3d vecd = v3dpool.obtain().set(vec);
        Vector3d out = v3dpool.obtain();

        Coordinates.cartesianToSpherical(vecd, out);

        double alpha = out.x * AstroUtils.TO_DEG;
        double delta = out.y * AstroUtils.TO_DEG;

        Logger.debug("Alpha/delta: " + alpha + "/" + delta);
        EventManager.instance.post(Events.RA_DEC_UPDATED, alpha, delta, screenX, screenY);

        v3dpool.free(out);
        v3dpool.free(vecd);
        v3pool.free(vec);
    }

    private void updateFocusLatLon(int screenX, int screenY) {
        if (isNatural()) {
            // Hover over planets gets us lat/lon
            if (current.getFocus() != null && current.getFocus() instanceof Planet) {
                Planet p = (Planet) current.getFocus();
                Vector3 pcenter = v3pool.obtain();
                p.transform.getTranslationf(pcenter);
                //pcenter.set((float) p.pos.x, (float) p.pos.y, (float) p.pos.z);
                ICamera camera = current;
                Vector3 v0 = v3pool.obtain().set(screenX, screenY, 0f);
                Vector3 v1 = v3pool.obtain().set(screenX, screenY, 0.5f);
                camera.getCamera().unproject(v0);
                camera.getCamera().unproject(v1);

                Ray ray = new Ray(v0, v1.sub(v0));
                Vector3 intersection = new Vector3();
                boolean inter = Intersector.intersectRaySphere(ray, pcenter, p.getRadius(), intersection);

                if (inter) {
                    // We found an intersection point
                    Matrix4 localTransformInv = new Matrix4();
                    p.setToLocalTransform(1, localTransformInv, false);
                    localTransformInv.inv();
                    intersection.mul(localTransformInv);

                    Vector3d vec = v3dpool.obtain();
                    vec.set(intersection);
                    Vector3d out = v3dpool.obtain();
                    Coordinates.cartesianToSpherical(vec, out);

                    double lon = (Math.toDegrees(out.x) + 90) % 360;
                    double lat = Math.toDegrees(out.y);

                    Logger.debug("Lon/lat: " + lon + "/" + lat);
                    EventManager.instance.post(Events.LON_LAT_UPDATED, lon, lat, screenX, screenY);

                    v3dpool.free(vec);
                    v3dpool.free(out);
                }

                v3pool.free(pcenter);
                v3pool.free(v0);
                v3pool.free(v1);

            }

        }
    }

    int pxRendererBackup = -1;

    /**
     * Sets the new camera mode and updates the frustum
     * @param mode
     */
    public void updateMode(CameraMode mode, boolean postEvent) {
        CameraMode prevMode = this.mode;
        boolean modeChange = mode != this.mode;
        // Save state of current if mode is different
        //        if (modeChange)
        //            saveState();

        // Save state of old camera
        this.mode = mode;
        updateCurrentCamera();
        naturalCamera.updateMode(mode, postEvent);
        fovCamera.updateMode(mode, postEvent);

        // Restore state of new camera
        //        if (modeChange && this.mode == CameraMode.Focus)
        //            restoreState();

        if (postEvent) {
            EventManager.instance.post(Events.FOV_CHANGE_NOTIFICATION, this.getCamera().fieldOfView, getFovFactor());
        }
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case CAMERA_MODE_CMD:
            CameraMode cm = (CameraMode) data[0];
            updateMode(cm, true);
            break;
        case FOV_CHANGE_NOTIFICATION:
            updateAngleEdge(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            break;
        default:
            break;
        }

    }

    @Override
    public Vector3d[] getDirections() {
        return current.getDirections();
    }

    @Override
    public int getNCameras() {
        return current.getNCameras();
    }

    @Override
    public PerspectiveCamera[] getFrontCameras() {
        return current.getFrontCameras();
    }

    @Override
    public CameraMode getMode() {
        return mode;
    }

    @Override
    public void updateAngleEdge(int width, int height) {
        naturalCamera.updateAngleEdge(width, height);
        fovCamera.updateAngleEdge(width, height);
    }

    @Override
    public float getAngleEdge() {
        return current.getAngleEdge();
    }

    @Override
    public CameraManager getManager() {
        return this;
    }

    @Override
    public void render() {
        current.render();
    }

    @Override
    public float getMotionMagnitude() {
        return current.getMotionMagnitude();
    }

    @Override
    public ICamera getCurrent() {
        return current;
    }

    @Override
    public void saveState() {
        if (current != null)
            current.saveState();
    }

    @Override
    public void restoreState() {
        if (current != null)
            current.restoreState();
    }

    @Override
    public double getVelocity() {
        return speed;
    }

    @Override
    public boolean superVelocity() {
        return supervelocity;
    }

    @Override
    public boolean isFocus(CelestialBody cb) {
        return current.isFocus(cb);
    }

    @Override
    public void checkClosest(CelestialBody cb) {
        current.checkClosest(cb);
    }

    @Override
    public CelestialBody getFocus() {
        return current.getFocus();
    }

    public void computeGaiaScan(ITimeFrameProvider time, CelestialBody cb) {
        current.computeGaiaScan(time, cb);
    }

    @Override
    public boolean isVisible(ITimeFrameProvider time, CelestialBody cb) {
        return current.isVisible(time, cb);
    }

    @Override
    public double getDistance() {
        return current.getDistance();
    }

    @Override
    public void setCamera(PerspectiveCamera cam) {
        current.setCamera(cam);
    }

    @Override
    public void setCameraStereoLeft(PerspectiveCamera cam) {
        current.setCameraStereoLeft(cam);
    }

    @Override
    public void setCameraStereoRight(PerspectiveCamera cam) {
        current.setCameraStereoRight(cam);
    }

    @Override
    public PerspectiveCamera getCameraStereoLeft() {
        return current.getCameraStereoLeft();
    }

    @Override
    public PerspectiveCamera getCameraStereoRight() {
        return current.getCameraStereoRight();
    }

}
