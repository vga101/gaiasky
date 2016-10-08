package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.input.GestureDetector;
import com.badlogic.gdx.input.GestureDetector.GestureAdapter;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.Intersectord;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
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

    private static final double stopAt = 50000 * Constants.M_TO_U;

    /** Force, acceleration and velocity **/
    public Vector3d force, accel, vel;
    /** Direction and up vectors **/
    public Vector3d direction, up;

    /** Engine thrust vector **/
    public Vector3d thrust;

    /** This is the power **/
    public static double thrustLength = 1e12d;
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

    /** Aux vectors **/
    public Vector3d auxd1, auxd2, auxd3, auxd4;
    public Vector3 auxf1;

    /** The input controller attached to this camera **/
    private SpacecraftInputController inputController;

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
        up = new Vector3d(0, 0, 1);

        // engine thrust direction
        // our spacecraft is a rigid solid so thrust is always the camera direction vector
        thrust = new Vector3d(direction).scl(thrustLength);
        enginePower = 0;

        // spacecraft
        mass = .5e5;

        // aux vectors
        auxd1 = new Vector3d();
        auxd2 = new Vector3d();
        auxd3 = new Vector3d();
        auxd4 = new Vector3d();
        auxf1 = new Vector3();

        // init camera
        camera = new PerspectiveCamera(GlobalConf.scene.CAMERA_FOV, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = (float) CAM_NEAR;
        camera.far = (float) CAM_FAR;

        // init cameras vector
        cameras = new PerspectiveCamera[] { camera, camLeft, camRight };

        // fov factor
        fovFactor = camera.fieldOfView / 40f;

        inputController = new SpacecraftInputController(new GestureAdapter(), this);

        // Init sprite batch for crosshair and cockpit
        spriteBatch = new SpriteBatch();
        crosshairTex = new Texture(Gdx.files.internal("img/crosshair-yellow.png"));
        chw2 = crosshairTex.getWidth() / 2f;
        chh2 = crosshairTex.getHeight() / 2f;

        // Focus is changed from GUI
        EventManager.instance.subscribe(this, Events.FOV_CHANGED_CMD);
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

    @Override
    public void update(float dt, ITimeFrameProvider time) {
        /** POSITION **/
        // Compute force from thrust
        thrust.set(direction).scl(thrustLength * enginePower);
        force.set(thrust);

        // Compute new acceleration in m/s^2
        accel.set(force).scl(1d / mass);

        // Integrate other quantities
        // convert metres to internal units so we have the velocity in u/s
        auxd1.set(accel).scl(Constants.M_TO_U);
        vel.add(auxd1.scl(dt));
        auxd2.set(vel);
        // New position in auxd3
        auxd3.set(pos).add(auxd2.scl(dt));
        // Check collision!
        if (closest != null) {
            // d1 is the new distance to the centre of the object
            double d1 = auxd4.set(closest.pos).sub(auxd3).len();
            if (closest.getRadius() > d1 + stopAt) {
                EventManager.instance.post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), "Crashed against " + closest.name + "!");

                Vector3d[] intersections = Intersectord.lineSphereIntersections(pos, auxd3, closest.pos, closest.getRadius() + stopAt);

                if (intersections.length >= 1) {
                    pos.set(intersections[0]);
                }

                stopAllMovement();
            } else {
                pos.set(auxd3);
            }
        } else {
            pos.set(auxd3);
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
        auxd1.set(direction).crs(up);
        direction.rotate(auxd1, pitchdiff);
        up.rotate(auxd1, pitchdiff);

        // Update camera
        updatePerspectiveCamera();

    }

    protected void updatePerspectiveCamera() {

        camera.near = (float) (100000d * Constants.M_TO_U);
        if (closest != null) {
            camera.near = Math.min(camera.near, (closest.distToCamera - closest.getRadius()) / 2.5f);
        }
        camera.position.set(0, 0, 0);
        camera.direction.set(direction.valuesf());
        camera.up.set(up.valuesf());

        camera.update();

        posinv.set(pos).scl(-1);

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

    @Override
    public void updateMode(CameraMode mode, boolean postEvent) {
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        if (mode == CameraMode.Spacecraft) {
            // Register input controller
            im.addProcessor(inputController);
            stopAllMovement();
        } else {
            // Unregister input controller
            im.removeProcessor(inputController);
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
                    break;
                case Keys.S:
                    // power -1
                    camera.setEnginePower(-1);
                    break;
                case Keys.A:
                    // roll 1
                    camera.setRollPower(1);
                    break;
                case Keys.D:
                    // roll -1
                    camera.setRollPower(-1);
                    break;
                case Keys.DOWN:
                    // pitch 1
                    camera.setPitchPower(1);
                    break;
                case Keys.UP:
                    // pitch -1
                    camera.setPitchPower(-1);
                    break;
                case Keys.LEFT:
                    // yaw 1
                    camera.setYawPower(1);
                    break;
                case Keys.RIGHT:
                    // yaw -1
                    camera.setYawPower(-1);
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
                }
            }
            return false;

        }

    }

    @Override
    public void render() {
        // Renders crosshair if focus mode
        if (GlobalConf.scene.CROSSHAIR) {
            float w = Gdx.graphics.getWidth();
            float h = Gdx.graphics.getHeight();

            spriteBatch.begin();
            spriteBatch.draw(crosshairTex, w / 2f - chw2, h / 2f - chh2);
            spriteBatch.end();

        }
    }

}
