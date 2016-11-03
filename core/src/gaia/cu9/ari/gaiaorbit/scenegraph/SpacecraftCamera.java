package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.XBox360Mappings;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.Intersectord;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector2d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * Implements a spacecraft-like movement. The spacecraft is modeled as a rigid
 * solid and it has a mass and an engine model. The rest is physics.
 * 
 * @author tsagrista
 *
 */
public class SpacecraftCamera extends AbstractCamera implements IObserver {

    private static final double stopAt = 10000 * Constants.M_TO_U;

    /** Camera to render the attitude indicator system **/
    private PerspectiveCamera guiCam;

    /** Force, acceleration and velocity **/
    public Vector3d force, accel, vel;
    /** Direction and up vectors **/
    public Vector3d direction, up;

    /** Engine thrust vector **/
    public Vector3d thrust;

    /** This is the power **/
    public static final double thrustLength = 1e12d;

    /** Factor (adapt to be able to navigate small and large scale structures **/
    public static final double[] thrustFactor = new double[] { 0.1, 1.0, 1e1, 1e2, 1e3, 1e4, 1e5, 1e6, 1e7, 1e8, 1e9, 1e10, 1e11 };
    public int thrustFactorIndex = 1;

    /** Instantaneous engine power, this is in [0..1] **/
    public double enginePower;

    /** Yaw, pitch and roll **/
    // yaw, pitch, roll multiplier
    public double yprLength = .1e7d;
    // power in each angle in [0..1]
    public double yawp, pitchp, rollp;
    // angular forces
    public double yawf, pitchf, rollf;
    // angular accelerations in deg/s^2
    public double yawa, pitcha, rolla;
    // angular velocities in deg/s
    public double yawv, pitchv, rollv;
    // angles in radians
    public double yaw, pitch, roll;

    // Are we in the process of stabilising or stopping the spaceship?
    public boolean leveling, stopping;

    /** Aux vectors **/
    private Vector3d aux3d1, aux3d2, aux3d3;
    private Vector3 aux3f1, aux3f2;
    private Quaternion qf;

    /** The input controller attached to this camera **/
    private SpacecraftInputController inputController;

    /** Controller listener **/
    private SpacecraftControllerListener controllerListener;

    /** Mass in kg **/
    public double mass;

    /** Crosshair **/
    private SpriteBatch spriteBatch;
    private Texture crosshairTex;
    private float chw2, chh2;

    public SpacecraftCamera(AssetManager assetManager, CameraManager parent) {
        super(parent);

        // position attributes
        force = new Vector3d();
        accel = new Vector3d();
        vel = new Vector3d();

        // camera
        direction = new Vector3d(1, 0, 0);
        up = new Vector3d(0, 1, 0);

        // engine thrust direction
        // our spacecraft is a rigid solid so thrust is always the camera direction vector
        thrust = new Vector3d(direction).scl(thrustLength);
        enginePower = 0;

        // spacecraft
        mass = .5e5;

        // not stabilising
        leveling = false;

        // aux vectors
        aux3d1 = new Vector3d();
        aux3d2 = new Vector3d();
        aux3d3 = new Vector3d();
        aux3f1 = new Vector3();
        aux3f2 = new Vector3();
        qf = new Quaternion();

        // init camera
        camera = new PerspectiveCamera(GlobalConf.scene.CAMERA_FOV, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = (float) (100000d * Constants.M_TO_U);
        camera.far = (float) CAM_FAR;

        // init cameras vector
        cameras = new PerspectiveCamera[] { camera, camLeft, camRight };

        // init gui camera
        guiCam = new PerspectiveCamera(30, 300, 300);
        guiCam.near = (float) CAM_NEAR;
        guiCam.far = (float) CAM_FAR;

        // aspect ratio
        ar = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();

        // fov factor
        fovFactor = camera.fieldOfView / 40f;

        inputController = new SpacecraftInputController(new GestureAdapter(), this);
        controllerListener = new SpacecraftControllerListener(this);

        // Init sprite batch for crosshair and cockpit
        spriteBatch = new SpriteBatch();
        crosshairTex = new Texture(Gdx.files.internal("img/crosshair-sc-yellow.png"));
        chw2 = crosshairTex.getWidth() / 2f;
        chh2 = crosshairTex.getHeight() / 2f;

        // Focus is changed from GUI
        EventManager.instance.subscribe(this, Events.FOV_CHANGED_CMD, Events.SPACECRAFT_STABILISE_CMD, Events.SPACECRAFT_STOP_CMD);
    }

    public Quaternion getRotationQuaternion() {
        return qf;
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

    double lastangle = 0;

    @Override
    public void update(float dt, ITimeFrameProvider time) {
        distance = pos.len();
        /** POSITION **/
        // Compute force from thrust
        thrust.set(direction).scl(thrustLength * thrustFactor[thrustFactorIndex] * enginePower);
        force.set(thrust);

        if (stopping) {
            double speed = vel.len();
            if (speed != 0) {
                thrust.set(vel).nor().scl(-thrustLength * thrustFactor[thrustFactorIndex]);
                force.set(thrust);
            }

            Vector3d nextvel = aux3d3.set(force).scl(1d / mass).scl(Constants.M_TO_U).scl(dt).add(vel);

            if (vel.angle(nextvel) > 90) {
                setEnginePower(0);
                force.scl(0);
                vel.scl(0);
                EventManager.instance.post(Events.SPACECRAFT_STOP_CMD, false);
            }
        }

        // Compute new acceleration in m/s^2
        accel.set(force).scl(1d / mass);

        // Integrate other quantities
        // convert metres to internal units so we have the velocity in u/s
        aux3d1.set(accel).scl(Constants.M_TO_U);
        vel.add(aux3d1.scl(dt));
        aux3d2.set(vel);
        // New position in auxd3
        aux3d3.set(pos).add(aux3d2.scl(dt));
        // Check collision!
        if (closest != null) {
            // d1 is the new distance to the centre of the object
            if (!vel.isZero() && Intersectord.distanceSegmentPoint(pos, aux3d3, closest.pos) < closest.getRadius() + stopAt) {
                EventManager.instance.post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), "Crashed against " + closest.name + "!");

                Vector3d[] intersections = Intersectord.lineSphereIntersections(pos, aux3d3, closest.pos, closest.getRadius() + stopAt);

                if (intersections.length >= 1) {
                    pos.set(intersections[0]);
                }

                stopAllMovement();
            } else {
                pos.set(aux3d3);
            }
        } else {
            pos.set(aux3d3);
        }

        if (leveling) {
            // No velocity, we just stop euler angle motions
            if (yawv != 0) {
                yawp = -Math.signum(yawv) * MathUtilsd.clamp(Math.abs(yawv), 0, 1);
            }
            if (pitchv != 0) {
                pitchp = -Math.signum(pitchv) * MathUtilsd.clamp(Math.abs(pitchv), 0, 1);
            }
            if (rollv != 0) {
                rollp = -Math.signum(rollv) * MathUtilsd.clamp(Math.abs(rollv), 0, 1);
            }
            if (Math.abs(yawv) < 1e-3 && Math.abs(pitchv) < 1e-3 && Math.abs(rollv) < 1e-3) {
                setYawPower(0);
                setPitchPower(0);
                setRollPower(0);

                yawv = 0;
                pitchv = 0;
                rollv = 0;
                EventManager.instance.post(Events.SPACECRAFT_STABILISE_CMD, false);
            }
        }

        // Yaw, pitch and roll
        yawf = yawp * yprLength;
        pitchf = pitchp * yprLength;
        rollf = rollp * yprLength;

        // accel
        yawa = yawf / mass;
        pitcha = pitchf / mass;
        rolla = rollf / mass;

        // vel
        yawv += yawa * dt;
        pitchv += pitcha * dt;
        rollv += rolla * dt;

        // pos
        double yawdiff = yawv * dt;
        double pitchdiff = pitchv * dt;
        double rolldiff = rollv * dt;
        yaw += yawdiff;
        pitch += pitchdiff;
        roll += rolldiff;

        // apply roll
        up.rotate(direction, -rolldiff);

        // apply yaw
        direction.rotate(up, yawdiff);

        // apply pitch
        aux3d1.set(direction).crs(up);
        direction.rotate(aux3d1, pitchdiff);
        up.rotate(aux3d1, pitchdiff);

        // Update camera
        updatePerspectiveCamera();

        String clname = null;
        float cldist = -1f;
        if (closest != null && ModelBody.closestCamStar != null) {
            if (closest.distToCamera < ModelBody.closestCamStar.distToCamera) {
                clname = closest.name;
                cldist = closest.distToCamera;
            } else {
                clname = ModelBody.closestCamStar.name;
                cldist = ModelBody.closestCamStar.distToCamera;
            }
        } else if (closest == null) {
            clname = ModelBody.closestCamStar.name;
            cldist = ModelBody.closestCamStar.distToCamera;
        }

        EventManager.instance.post(Events.SPACECRAFT_INFO, yaw % 360, pitch % 360, roll % 360, vel.len(), clname, cldist);

    }

    /**
    (pitch, yaw)  -> (x, y, z)
    (0,     0)    -> (1, 0, 0)
    (pi/2,  0)    -> (0, 1, 0)
    (0,    -pi/2) -> (0, 0, 1)
    
    xzLen = cos(pitch)
    x = xzLen * cos(yaw)
    y = sin(pitch)
    z = xzLen * sin(-yaw)
    
    
    pitch = acos(xzLen)
    yaw = acos(x/xzLen)
    -yaw = asin(z/xzLen)
    */
    public Vector2d getPitchYaw(Vector3d vec, Vector2d out) {
        double xzlen = Math.sqrt(direction.x * direction.x + direction.z * direction.z);
        double derivedPitch = -Math.toDegrees(Math.acos(xzlen));
        double derivedYaw = -Math.toDegrees(Math.acos(direction.x / xzlen)) + 90;
        //double derivedYaw2 = -Math.toDegrees(Math.asin(direction.z / xzlen)) + 90;
        out.set(derivedPitch, derivedYaw);
        return out;
    }

    public double convertAngle(double angle) {
        if (angle <= 180)
            return angle;
        else
            return angle - 360;
    }

    protected void updatePerspectiveCamera() {

        camera.near = (float) (100000d * Constants.M_TO_U);
        if (closest != null) {
            camera.near = Math.min(camera.near, ((float) closest.pos.dst(pos) - closest.getRadius()) / 2.5f);
        }
        camera.position.set(0, 0, 0);
        camera.direction.set(direction.valuesf());
        camera.up.set(up.valuesf());

        camera.update();

        posinv.set(pos).scl(-1);

        camera.view.getRotation(qf);

    }

    protected void stopAllMovement() {
        setEnginePower(0);
        vel.set(0, 0, 0);

        setYawPower(0);
        setPitchPower(0);
        setRollPower(0);

        yawv = 0;
        pitchv = 0;
        rollv = 0;

        leveling = false;
        stopping = false;

    }

    /**
     * Sets the current engine power
     * 
     * @param power
     *            The power in [-1..1]
     */
    public void setEnginePower(double enginePower) {
        this.enginePower = MathUtilsd.clamp(enginePower, -1, 1);
    }

    /**
     * Sets the current yaw power
     * 
     * @param yawp
     *            The yaw power in [-1..1]
     */
    public void setYawPower(double yawp) {
        this.yawp = MathUtilsd.clamp(yawp, -1, 1);
    }

    /**
     * Sets the current pitch power
     * 
     * @param pitchp
     *            The pitch power in [-1..1]
     */
    public void setPitchPower(double pitchp) {
        this.pitchp = MathUtilsd.clamp(pitchp, -1, 1);
    }

    /**
     * Sets the current roll power
     * 
     * @param rollp
     *            The roll power in [-1..1]
     */
    public void setRollPower(double rollp) {
        this.rollp = MathUtilsd.clamp(rollp, -1, 1);
    }

    public void increaseThrustFactorIndex() {
        thrustFactorIndex = (thrustFactorIndex + 1) % thrustFactor.length;
        EventManager.instance.post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), "Thrust factor: " + thrustFactor[thrustFactorIndex]);
    }

    public void decreaseThrustFactorIndex() {
        thrustFactorIndex = thrustFactorIndex - 1;
        if (thrustFactorIndex < 0)
            thrustFactorIndex = thrustFactor.length - 1;
        EventManager.instance.post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), "Thrust factor: " + thrustFactor[thrustFactorIndex]);
    }

    @Override
    public void updateMode(CameraMode mode, boolean postEvent) {
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        if (mode == CameraMode.Spacecraft) {
            // Register input controller
            if (!im.getProcessors().contains(inputController, true))
                im.addProcessor(inputController);
            // Register controller listener
            Controllers.clearListeners();
            Controllers.addListener(controllerListener);
            stopAllMovement();
        } else {
            // Unregister input controller
            im.removeProcessor(inputController);
            // Unregister controller listener
            Controllers.removeListener(controllerListener);
            stopAllMovement();
        }
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
        switch (event) {
        case FOV_CHANGED_CMD:
            float fov = MathUtilsd.clamp((float) data[0], Constants.MIN_FOV, Constants.MAX_FOV);

            for (PerspectiveCamera cam : cameras) {
                cam.fieldOfView = fov;
            }

            fovFactor = camera.fieldOfView / 40f;
            if (parent.current == this) {
                EventManager.instance.post(Events.FOV_CHANGE_NOTIFICATION, fov, fovFactor);
            }
            break;
        case SPACECRAFT_STABILISE_CMD:
            leveling = (Boolean) data[0];
            break;
        case SPACECRAFT_STOP_CMD:
            stopping = (Boolean) data[0];
            break;
        }

    }

    public boolean isStopping() {
        return stopping;
    }

    public boolean isStabilising() {
        return leveling;
    }

    @Override
    public void render(int rw, int rh) {
        // Renders crosshair if focus mode
        if (GlobalConf.scene.CROSSHAIR) {
            spriteBatch.begin();
            spriteBatch.draw(crosshairTex, rw / 2f - chw2, rh / 2f - chh2);
            spriteBatch.end();
        }
    }

    /**
     * Input controller for the spacecraft camera
     * @author tsagrista
     *
     */
    private class SpacecraftInputController extends GestureDetector {
        SpacecraftCamera camera;

        public SpacecraftInputController(GestureListener listener, SpacecraftCamera camera) {
            super(listener);
            this.camera = camera;
        }

        @Override
        public boolean keyDown(int keycode) {
            if (GlobalConf.runtime.INPUT_ENABLED) {
                switch (keycode) {
                case Keys.W:
                    // power 1
                    camera.setEnginePower(1);
                    EventManager.instance.post(Events.SPACECRAFT_STOP_CMD, false);
                    break;
                case Keys.S:
                    // power -1
                    camera.setEnginePower(-1);
                    EventManager.instance.post(Events.SPACECRAFT_STOP_CMD, false);
                    break;
                case Keys.A:
                    // roll 1
                    camera.setRollPower(1);
                    EventManager.instance.post(Events.SPACECRAFT_STOP_CMD, false);
                    break;
                case Keys.D:
                    // roll -1
                    camera.setRollPower(-1);
                    EventManager.instance.post(Events.SPACECRAFT_STABILISE_CMD, false);
                    break;
                case Keys.DOWN:
                    // pitch 1
                    camera.setPitchPower(1);
                    EventManager.instance.post(Events.SPACECRAFT_STABILISE_CMD, false);
                    break;
                case Keys.UP:
                    // pitch -1
                    camera.setPitchPower(-1);
                    EventManager.instance.post(Events.SPACECRAFT_STABILISE_CMD, false);
                    break;
                case Keys.LEFT:
                    // yaw 1
                    camera.setYawPower(1);
                    EventManager.instance.post(Events.SPACECRAFT_STABILISE_CMD, false);
                    break;
                case Keys.RIGHT:
                    // yaw -1
                    camera.setYawPower(-1);
                    EventManager.instance.post(Events.SPACECRAFT_STABILISE_CMD, false);
                    break;
                case Keys.PAGE_UP:
                    // Increase thrust factor
                    camera.increaseThrustFactorIndex();
                    break;
                case Keys.PAGE_DOWN:
                    // Decrease thrust length
                    camera.decreaseThrustFactorIndex();
                    break;
                }
            }
            return false;

        }

        @Override
        public boolean keyUp(int keycode) {
            if (GlobalConf.runtime.INPUT_ENABLED) {
                switch (keycode) {
                case Keys.W:
                case Keys.S:
                    // power 0
                    camera.setEnginePower(0);
                    break;
                case Keys.D:
                case Keys.A:
                    // roll 0
                    camera.setRollPower(0);
                    break;
                case Keys.UP:
                case Keys.DOWN:
                    // pitch 0
                    camera.setPitchPower(0);
                    break;
                case Keys.RIGHT:
                case Keys.LEFT:
                    // yaw 0
                    camera.setYawPower(0);
                    break;
                case Keys.L:
                    // level spaceship
                    EventManager.instance.post(Events.SPACECRAFT_STABILISE_CMD, true);
                    break;
                case Keys.P:
                    // stop spaceship
                    EventManager.instance.post(Events.SPACECRAFT_STOP_CMD, true);
                    break;
                }
            }
            return false;

        }

    }

    private class SpacecraftControllerListener implements ControllerListener {

        private SpacecraftCamera cam;

        public SpacecraftControllerListener(SpacecraftCamera cam) {
            super();
            this.cam = cam;
        }

        @Override
        public void connected(Controller controller) {
            EventManager.instance.post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), "Controller connected: " + controller.getName());
        }

        @Override
        public void disconnected(Controller controller) {
            EventManager.instance.post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), "Controller disconnected: " + controller.getName());
        }

        @Override
        public boolean buttonDown(Controller controller, int buttonCode) {
            switch (buttonCode) {
            case XBox360Mappings.BUTTON_A:
                cam.increaseThrustFactorIndex();
                break;
            case XBox360Mappings.BUTTON_X:
                cam.decreaseThrustFactorIndex();
                break;

            }
            return true;
        }

        @Override
        public boolean buttonUp(Controller controller, int buttonCode) {
            return false;
        }

        @Override
        public boolean axisMoved(Controller controller, int axisCode, float value) {
            boolean treated = false;
            // y = x^4
            // http://www.wolframalpha.com/input/?i=y+%3D+sign%28x%29+*+x%5E2+%28x+from+-1+to+1%29}
            value = Math.signum(value) * value * value * value * value;

            switch (axisCode) {
            case XBox360Mappings.AXIS_JOY2HOR:
                cam.setRollPower(-value);
                EventManager.instance.post(Events.SPACECRAFT_STABILISE_CMD, false);
                treated = true;
                break;
            case XBox360Mappings.AXIS_JOY1VERT:
                cam.setPitchPower(value);
                EventManager.instance.post(Events.SPACECRAFT_STABILISE_CMD, false);
                treated = true;
                break;
            case XBox360Mappings.AXIS_JOY1HOR:
                cam.setYawPower(-value);
                EventManager.instance.post(Events.SPACECRAFT_STABILISE_CMD, false);
                treated = true;
                break;
            case XBox360Mappings.AXIS_JOY2VERT:
                treated = true;
                break;
            case XBox360Mappings.AXIS_RT:
                cam.setEnginePower((value + 1) / 2);
                EventManager.instance.post(Events.SPACECRAFT_STOP_CMD, false);
                treated = true;
                break;
            case XBox360Mappings.AXIS_LT:
                cam.setEnginePower(-(value + 1) / 2);
                EventManager.instance.post(Events.SPACECRAFT_STOP_CMD, false);
                treated = true;
                break;
            }
            return treated;
        }

        @Override
        public boolean povMoved(Controller controller, int povCode, PovDirection value) {
            return false;
        }

        @Override
        public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
            return false;
        }

        @Override
        public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
            return false;
        }

        @Override
        public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
            return false;
        }

    }

}
