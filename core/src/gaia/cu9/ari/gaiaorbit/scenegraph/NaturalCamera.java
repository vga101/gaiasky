package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Peripheral;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.controllers.Controllers;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.NaturalControllerListener;
import gaia.cu9.ari.gaiaorbit.interfce.NaturalInputController;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * Models the movement of the camera
 * 
 * @author Toni Sagrista
 *
 */
public class NaturalCamera extends AbstractCamera implements IObserver {

    private static final double MIN_DIST = 5 * Constants.M_TO_U;

    /** Acceleration and velocity **/
    public Vector3d accel, vel;
    /** The force acting on the entity and the friction **/
    private Vector3d force, friction;

    public Vector3d direction, up, focusDirection;
    /** Indicates whether the camera is facing the focus or not **/
    public boolean facingFocus;

    /** Auxiliary double vectors **/
    private Vector3d aux1, aux2, aux3, aux5, dx;
    /** Auxiliary float vector **/
    private Vector3 auxf1;
    /** Acceleration, velocity and position for pitch, yaw and roll **/
    private Vector3d pitch, yaw, roll;
    /**
     * Acceleration, velocity and position for the horizontal and vertical
     * rotation around the focus
     **/
    private Vector3d horizontal, vertical;
    /** Time since last forward control issued, in seconds **/
    private double lastFwdTime = 0d;
    /** The last forward amount, positive forward, negative backward **/
    private double lastFwdAmount = 0;
    /** Previous angle in orientation lock **/
    double previousOrientationAngle = 0;

    /** Thrust which keeps the camera going. Mainly for game pads **/
    private double thrust = 0;
    private int thrustDirection = 0;

    /** Info about whether the previous state is saved **/
    protected boolean stateSaved = false;

    /** Whether the camera stops after a few seconds or keeps going **/
    private boolean fullStop = true;

    /** Entities for the Gaia_Scene mode **/
    protected CelestialBody entity1 = null, entity2 = null, entity3 = null;

    private CameraMode lastMode;

    /**
     * The focus entity
     */
    public CelestialBody focus, focusBak;

    /**
     * The direction point to seek
     */
    private Vector3d lastvel;
    /** Focus position **/
    private Vector3d focusPos;

    private Vector3d desired;

    /** Velocity module, in case it comes from a gamepad **/
    private double velocityGamepad = 0;
    private double gamepadMultiplier = 1;

    boolean diverted = false;

    boolean accelerometer = false;

    private float planetariumFocusAngle = 0f;

    public static float[] upSensor, lookAtSensor;

    /** The input controller attached to this camera **/
    private NaturalInputController inputController;

    /** Controller listener **/
    private NaturalControllerListener controllerListener;

    private SpriteBatch spriteBatch;
    private Texture crosshair;
    private float chw2, chh2;

    public NaturalCamera(AssetManager assetManager, CameraManager parent) {
        super(parent);
        vel = new Vector3d();
        accel = new Vector3d();
        force = new Vector3d();
        initialize(assetManager);

    }

    public void initialize(AssetManager assetManager) {
        camera = new PerspectiveCamera(GlobalConf.scene.CAMERA_FOV, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.near = (float) CAM_NEAR;
        camera.far = (float) CAM_FAR;

        // init cameras vector
        cameras = new PerspectiveCamera[] { camera, camLeft, camRight };

        fovFactor = camera.fieldOfView / 40f;

        up = new Vector3d(-0.024214629529207728, 0.7563044458865531, -0.6537715479041569);
        direction = new Vector3d();
        focusDirection = new Vector3d();
        desired = new Vector3d();
        pitch = new Vector3d(0.0f, 0.0f, -3.0291599E-6f);
        yaw = new Vector3d(0.0f, 0.0f, -7.9807205E-6f);
        roll = new Vector3d(0.0f, 0.0f, -1.4423944E-4f);
        horizontal = new Vector3d();
        vertical = new Vector3d();

        friction = new Vector3d();
        lastvel = new Vector3d();
        focusPos = new Vector3d();

        aux1 = new Vector3d();
        aux2 = new Vector3d();
        aux3 = new Vector3d();
        aux5 = new Vector3d();
        auxf1 = new Vector3();

        dx = new Vector3d();

        accelerometer = Gdx.input.isPeripheralAvailable(Peripheral.Accelerometer);

        inputController = new NaturalInputController(this);
        controllerListener = new NaturalControllerListener(this);

        // Init sprite batch for crosshair
        spriteBatch = new SpriteBatch();
        crosshair = new Texture(Gdx.files.internal("img/crosshair-green.png"));
        crosshair.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        chw2 = crosshair.getWidth() / 2f;
        chh2 = crosshair.getHeight() / 2f;

        // Focus is changed from GUI
        EventManager.instance.subscribe(this, Events.FOCUS_CHANGE_CMD, Events.FOV_CHANGED_CMD, Events.ORIENTATION_LOCK_CMD, Events.CAMERA_POS_CMD, Events.CAMERA_DIR_CMD, Events.CAMERA_UP_CMD, Events.CAMERA_FWD, Events.CAMERA_ROTATE, Events.CAMERA_PAN, Events.CAMERA_ROLL, Events.CAMERA_TURN, Events.CAMERA_STOP, Events.CAMERA_CENTER, Events.GO_TO_OBJECT_CMD, Events.PLANETARIUM_FOCUS_ANGLE_CMD);
    }

    // Set up direction and lookAtSensor if accelerometer is enabled
    public void update(float dt, ITimeFrameProvider time) {
        if (accelerometer) {
            synchronized (lookAtSensor) {
                direction.set(lookAtSensor).nor();
                up.set(upSensor).nor();
            }
            updatePerspectiveCamera();
        } else {
            camUpdate(dt, time);
        }

    }

    private void camUpdate(float dt, ITimeFrameProvider time) {
        // The whole update thread must lock the value of direction and up
        distance = pos.len();
        CameraMode m = (parent.current == this ? parent.mode : lastMode);
        double realTransUnits = getTranslateUnits();
        double translateUnits = Math.max(10d * Constants.M_TO_U, realTransUnits);
        switch (m) {
        case Focus:
            if (focus.withinMagLimit()) {
                focusBak = focus;
                focus = (CelestialBody) focus.getComputedAncestor();
                focus.getPosition(focusPos);

                dx.set(0, 0, 0);

                if (GlobalConf.scene.FOCUS_LOCK) {

                    focus.getPredictedPosition(aux1, time, this, false);
                    // Get focus dx
                    dx.set(aux1).sub(focusPos);

                    // Lock orientation - FOR NOW THIS ONLY WORKS WITH PLANETS/MOONS
                    if (GlobalConf.scene.FOCUS_LOCK_ORIENTATION && time.getDt() > 0 && focus.orientation != null) {
                        Double anglebak = null;
                        if (focus.rc != null) {
                            // Rotation component present - planets, etc
                            anglebak = focus.rc.angle;
                        } else if (focus.getOrientationQuaternion() != null) {
                            anglebak = focus.getOrientationQuaternion().getPitch();
                        }
                        if (anglebak != null) {
                            Double angle = previousOrientationAngle != 0 ? (anglebak - previousOrientationAngle) : 0;
                            focus.getAbsolutePosition(aux5);
                            aux3.set(pos).sub(aux5);
                            aux2.set(0, 1, 0).mul(focus.orientation);
                            aux3.rotate(aux2, angle);
                            aux3.add(aux5);
                            pos.set(aux3);
                            direction.rotate(aux2, angle);
                            up.rotate(aux2, angle);

                            previousOrientationAngle = anglebak;
                        }

                    }

                    // Add dx to camera position
                    pos.add(dx);

                }

                // Update direction to follow focus and activate custom input
                // listener
                updatePosition(dt, translateUnits, realTransUnits);
                updateRotation(dt, focusPos);

                if (!diverted) {
                    directionToTarget(dt, focusPos, GlobalConf.scene.TURNING_SPEED / (GlobalConf.scene.CINEMATIC_CAMERA ? 1e3f : 1e2f), planetariumFocusAngle);
                } else {
                    updateRotationFree(dt, GlobalConf.scene.TURNING_SPEED);
                }
                updateRoll(dt, GlobalConf.scene.TURNING_SPEED);

                // Update focus direction
                focus.transform.getTranslation(focusDirection);
                focus = focusBak;

                this.focus.getAbsolutePosition(aux1).add(dx);
                double dist = aux1.dst(pos);
                if (dist < focus.getRadius()) {
                    // aux2 <- focus-cam with a length of radius
                    aux2.set(pos).sub(aux1).nor().scl(focus.getRadius());
                    // Correct camera position
                    pos.set(aux1).add(aux2);
                }

                EventManager.instance.post(Events.FOCUS_INFO_UPDATED, focus.distToCamera - focus.getRadius(), ((AbstractPositionEntity) focus).viewAngle);
            } else {
                EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.Free_Camera);
            }
            break;
        case Free_Camera:
            updatePosition(dt, translateUnits, 1);

            // Update direction with pitch, yaw, roll
            updateRotationFree(dt, GlobalConf.scene.TURNING_SPEED);
            updateRoll(dt, GlobalConf.scene.TURNING_SPEED);
            updateLateral(dt, translateUnits);
            break;
        case Gaia_Scene:
            if (entity1 == null || entity2 == null) {
                entity1 = (CelestialBody) GaiaSky.instance.sg.getNode("Gaia");
                entity2 = (CelestialBody) GaiaSky.instance.sg.getNode("Earth");
                entity3 = (CelestialBody) GaiaSky.instance.sg.getNode("Mars");
            }
            AbstractPositionEntity fccopy = entity1.getLineCopy();
            fccopy.getRoot().transform.position.set(0f, 0f, 0f);
            fccopy.getRoot().update(time, null, this);
            this.pos.set(fccopy.transform.getTranslation());

            this.pos.add(0, 0, entity1.getRadius() * 5);
            this.posinv.set(this.pos).scl(-1);
            this.direction.set(0, 0, -1);
            this.up.set(0, 1, 0);
            closest = entity1;

            // Return to pool
            SceneGraphNode ape = fccopy;
            do {
                ape.returnToPool();
                ape = ape.parent;
            } while (ape != null);

            break;
        default:
            break;
        }

        // Update camera recorder
        EventManager.instance.post(Events.UPDATE_CAM_RECORDER, time, pos, direction, up);

        // Update actual camera
        lastFwdTime += dt;
        lastMode = m;

        updatePerspectiveCamera();
        updateFrustum(frustum, camera, pos, direction, up);
    }

    protected void updatePerspectiveCamera() {

        if (closest != null) {
            double stardist = ModelBody.closestCamStar != null ? ModelBody.closestCamStar.distToCamera : Float.MAX_VALUE;
            camera.near = (float) Math.min(CAM_NEAR, Math.min(closest.distToCamera - closest.getRadius(), stardist) / 10f);
        }
        camera.position.set(0f, 0f, 0f);
        camera.direction.set(direction.valuesf());
        camera.up.set(up.valuesf());
        camera.update();

        posinv.set(pos).scl(-1);

    }

    /**
     * Adds a forward movement by the given amount.
     * 
     * @param amount
     *            Positive for forward force, negative for backward force.
     */
    public void addForwardForce(double amount) {
        double tu = getTranslateUnits();
        if (amount <= 0) {
            // Avoid getting stuck in surface
            tu = Math.max(10d * Constants.M_TO_U, tu);
        }
        if (parent.mode == CameraMode.Focus) {
            desired.set(focusDirection);
        } else {
            desired.set(direction);
        }

        desired.nor().scl(amount * tu * 10);
        force.add(desired);
        // We reset the time counter
        lastFwdTime = 0;
        lastFwdAmount = amount;
    }

    /**
     * Sets the gamepad velocity as it comes from the joystick sensor.
     * 
     * @param amount
     *            The amount in [-1, 1].
     */
    public void setVelocity(double amount) {
        velocityGamepad = amount;
    }

    /**
     * Adds a pan movement to the camera.
     * 
     * @param deltaX
     *            Amount of horizontal movement.
     * @param deltaY
     *            Amount of vertical movement.
     */
    public void addPanMovement(double deltaX, double deltaY) {
        double tu = getTranslateUnits();
        desired.set(direction).crs(up).nor().scl(-deltaX * tu);
        desired.add(aux1.set(up).nor().scl(-deltaY * tu));
        force.add(desired);
    }

    /**
     * Adds a rotation force to the camera. DeltaX corresponds to yaw
     * (right/left) and deltaY corresponds to pitch (up/down).
     * 
     * @param deltaX
     *            The yaw amount.
     * @param deltaY
     *            The pitch amount.
     * @param focusLookKeyPressed
     *            The key to look around when on focus mode is pressed.
     */
    public void addRotateMovement(double deltaX, double deltaY, boolean focusLookKeyPressed, boolean acceleration) {
        // Just update yaw with X and pitch with Y
        if (parent.mode.equals(CameraMode.Free_Camera)) {
            addYaw(deltaX, acceleration);
            addPitch(deltaY, acceleration);
        } else if (parent.mode.equals(CameraMode.Focus)) {
            double th = 30;
            double vadeg = Math.toDegrees(focus.getViewAngle());
            // This factor slows the rotation as the focus gets closer and closer
            double factor = vadeg > th ? Math.pow(th / vadeg, 3) : 1.0;
            if (focusLookKeyPressed) {
                diverted = true;
                addYaw(deltaX * factor, acceleration);
                addPitch(deltaY * factor, acceleration);
            } else {
                addHorizontalRotation(deltaX * factor, acceleration);
                addVerticalRotation(deltaY * factor, acceleration);
            }
        }
    }

    public void setGamepadMultiplier(double amount) {
        gamepadMultiplier = amount;
    }

    public void addAmount(Vector3d vec, double amount, boolean x) {
        if (x)
            vec.x += amount;
        else
            vec.y = amount;
    }

    /** Adds the given amount to the camera yaw acceleration **/
    public void addYaw(double amount, boolean acceleration) {
        addAmount(yaw, amount, acceleration);
    }

    public void setYaw(double amount) {
        yaw.x = 0;
        yaw.y = amount;
    }

    /** Adds the given amount to the camera pitch acceleration **/
    public void addPitch(double amount, boolean acceleration) {
        addAmount(pitch, amount, acceleration);
    }

    public void setPitch(double amount) {
        pitch.x = 0;
        pitch.y = amount;
    }

    /** Adds the given amount to the camera roll acceleration **/
    public void addRoll(double amount, boolean acceleration) {
        addAmount(roll, amount, acceleration);
    }

    public void setRoll(double amount) {
        roll.x = 0;
        roll.y = amount;
    }

    /**
     * Adds the given amount to camera horizontal rotation around the focus
     * acceleration
     **/
    public void addHorizontalRotation(double amount, boolean acceleration) {
        addAmount(horizontal, amount, acceleration);
    }

    public void setHorizontalRotation(double amount) {
        horizontal.x = 0;
        horizontal.y = amount;
    }

    /**
     * Adds the given amount to camera vertical rotation around the focus
     * acceleration
     **/
    public void addVerticalRotation(double amount, boolean acceleration) {
        addAmount(vertical, amount, acceleration);
    }

    public void setVerticalRotation(double amount) {
        vertical.x = 0;
        vertical.y = amount;
    }

    /**
     * Stops the camera movement.
     * 
     * @return True if the camera had any movement at all and it has been
     *         stopped. False if camera was already still.
     */
    public boolean stopMovement() {
        boolean stopped = (vel.len2() != 0 || yaw.y != 0 || pitch.y != 0 || roll.y != 0 || vertical.y != 0 || horizontal.y != 0);
        force.scl(0f);
        vel.scl(0f);
        yaw.y = 0;
        pitch.y = 0;
        roll.y = 0;
        horizontal.y = 0;
        vertical.y = 0;
        return stopped;
    }

    /**
     * Stops the camera movement.
     * 
     * @return True if the camera had any movement at all and it has been
     *         stopped. False if camera was already still.
     */
    public boolean stopTotalMovement() {
        boolean stopped = (vel.len2() != 0 || yaw.y != 0 || pitch.y != 0 || roll.y != 0 || vertical.y != 0 || horizontal.y != 0);
        force.scl(0f);
        vel.scl(0f);
        yaw.scl(0f);
        pitch.scl(0f);
        roll.scl(0f);
        horizontal.scl(0f);
        vertical.scl(0f);
        return stopped;
    }

    /**
     * Stops the camera movement.
     * 
     * @return True if the camera had any movement at all and it has been
     *         stopped. False if camera was already still.
     */
    public boolean stopForwardMovement() {
        boolean stopped = (vel.len2() != 0);
        force.scl(0f);
        vel.scl(0f);
        return stopped;
    }

    /**
     * Updates the position of this entity using the current force
     * 
     * @param dt
     * @param multiplier
     */
    protected void updatePosition(double dt, double multiplier, double transUnits) {
        // Calculate velocity if coming from gamepad
        if (velocityGamepad != 0) {
            vel.set(direction).nor().scl(velocityGamepad * gamepadMultiplier * multiplier);
        }

        double forceLen = force.len();
        double velocity = vel.len();

        // Half a second after we have stopped zooming, real friction kicks in
        if (fullStop)
            friction.set(force).nor().scl(-forceLen * dt * (lastFwdTime > 1 ? (lastFwdTime - 1) * 1000 : 1));
        else
            friction.set(force).nor().scl(-forceLen * dt);

        force.add(friction);

        if (lastFwdTime > (GlobalConf.scene.CINEMATIC_CAMERA ? 1.5 : 0.25) && velocityGamepad == 0 && fullStop || lastFwdAmount > 0 && transUnits == 0) {
            stopForwardMovement();
        }

        if (thrust != 0)
            force.add(thrust).scl(thrustDirection);
        applyForce(force);

        if (!(force.isZero() && velocity == 0 && accel.isZero())) {
            vel.add(accel.scl(dt));

            // Clamp to top speed
            if (GlobalConf.scene.CAMERA_SPEED_LIMIT > 0 && vel.len() > GlobalConf.scene.CAMERA_SPEED_LIMIT) {
                vel.clamp(0, GlobalConf.scene.CAMERA_SPEED_LIMIT);
            }

            // Velocity changed direction
            if (lastvel.dot(vel) < 0) {
                vel.scl(0);
                force.scl(0);
            }

            velocity = vel.len();

            if (parent.mode.equals(CameraMode.Focus)) {
                // Use direction vector as velocity so that if we turn the
                // velocity also turns
                double sign = Math.signum(vel.dot(focusDirection));
                focus.getPosition(vel).nor().scl(sign * velocity);
            }

            vel.clamp(0, multiplier);
            // Aux1 is the step to take
            aux1.set(vel).scl(dt);
            // Aux2 contains the new position
            aux2.set(pos).add(aux1);
            pos.add(aux1);

            accel.scl(0);

            lastvel.set(vel);
        }
        posinv.set(pos).scl(-1);
    }

    /**
     * Updates the rotation for the free camera.
     * 
     * @param dt
     */
    private void updateRotationFree(float dt, double rotateSpeed) {
        // Add position to compensate for coordinates centered on camera
        if (updatePosition(pitch, dt)) {
            // Pitch
            aux1.set(direction).crs(up).nor();
            rotate(aux1, pitch.z * rotateSpeed);
        }
        if (updatePosition(yaw, dt)) {
            // Yaw
            rotate(up, -yaw.z * rotateSpeed);
        }

        defaultState(pitch, !GlobalConf.scene.CINEMATIC_CAMERA);
        defaultState(yaw, !GlobalConf.scene.CINEMATIC_CAMERA);
    }

    private void updateRoll(float dt, double rotateSpeed) {
        if (updatePosition(roll, dt)) {
            // Roll
            rotate(direction, -roll.z * rotateSpeed);
        }
        defaultState(roll, !GlobalConf.scene.CINEMATIC_CAMERA);
    }

    /**
     * Updates the direction vector using the pitch, yaw and roll forces.
     * 
     * @param dt
     */
    private void updateRotation(float dt, final Vector3d rotationCenter) {
        // Add position to compensate for coordinates centered on camera
        rotationCenter.add(pos);
        if (updatePosition(vertical, dt)) {
            // Pitch
            aux1.set(direction).crs(up).nor();
            rotateAround(rotationCenter, aux1, vertical.z * GlobalConf.scene.ROTATION_SPEED);
        }
        if (updatePosition(horizontal, dt)) {
            // Yaw
            rotateAround(rotationCenter, up, -horizontal.z * GlobalConf.scene.ROTATION_SPEED);
        }

        defaultState(vertical, !GlobalConf.scene.CINEMATIC_CAMERA);
        defaultState(horizontal, !GlobalConf.scene.CINEMATIC_CAMERA);

    }

    private void defaultState(Vector3d vec, boolean resetVelocity) {
        // Always reset acceleration
        vec.x = 0;

        // Reset velocity if needed
        if (resetVelocity)
            vec.y = 0;
    }

    private void updateLateral(float dt, double translateUnits) {
        // Pan with hor
        aux1.set(direction).crs(up).nor();
        aux1.scl(horizontal.y * dt * translateUnits);
        translate(aux1);

    }

    /**
     * Updates the given accel/vel/pos of the angle using dt.
     * 
     * @param angle
     * @param dt
     * @return
     */
    private boolean updatePosition(Vector3d angle, float dt) {
        if (angle.x != 0 || angle.y != 0) {
            // Calculate velocity from acceleration
            angle.y += angle.x * dt;
            // Cap velocity
            angle.y = Math.signum(angle.y) * Math.abs(angle.y);
            // Update position
            angle.z = (angle.y * dt) % 360f;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Updates the camera direction and up vectors with a gentle turn towards
     * the given target.
     * 
     * @param dt
     *            The current time step
     * @param target
     *            The position of the target
     * @param turnVelocity
     *            The velocity at which to turn
     * @param planetariumAngle
     *            In degrees. In the case of planetaria, the target must be a
     *            few degrees lower (skewed domes) so that we need to target a
     *            point which is a few degrees above the focus.
     */
    private void directionToTarget(double dt, final Vector3d target, double turnVelocity, double planetariumAngle) {
        desired.set(target).sub(pos);
        if (planetariumAngle != 0) {
            // Use up to target area above focus with given angle
            double uplen = Math.tan(MathUtilsd.degRad * planetariumAngle) * desired.len();
            aux3.set(desired).crs(up);
            aux3.crs(desired);
            aux2.set(aux3).nor().scl(uplen);
            aux1.set(target).add(aux2);
            desired.set(aux1).sub(pos);
        }
        desired.nor();
        double dist = desired.dst(direction);
        if (dist > 2 * Constants.KM_TO_U) {
            // Add desired to direction with given turn velocity (v*dt)
            desired.scl(turnVelocity * dt);
            direction.add(desired).nor();

            // Update up so that it is always perpendicular
            aux1.set(direction).crs(up);
            up.set(aux1).crs(direction).nor();
            facingFocus = false;
        } else {
            facingFocus = true;
        }
    }

    /**
     * Updates the camera mode
     */
    @Override
    public void updateMode(CameraMode mode, boolean postEvent) {
        InputMultiplexer im = (InputMultiplexer) Gdx.input.getInputProcessor();
        switch (mode) {
        case Focus:
            diverted = false;
            checkFocus();
        case Free_Camera:
        case Gaia_Scene:
            // Register input controller
            if (!im.getProcessors().contains(inputController, true))
                im.addProcessor(im.size(), inputController);
            // Register controller listener
            Controllers.clearListeners();
            Controllers.addListener(controllerListener);
            break;
        default:
            // Unregister input controller
            im.removeProcessor(inputController);
            // Unregister controller listener
            Controllers.removeListener(controllerListener);
            break;
        }
    }

    public void setFocus(CelestialBody focus) {
        if (focus != null) {
            this.focus = focus;
            // Create event to notify focus change
            EventManager.instance.post(Events.FOCUS_CHANGED, focus);
        }
    }

    /**
     * This depends on the distance from the focus.
     * 
     * @return
     */
    public double getTranslateUnits() {
        double dist;
        if (parent.mode == CameraMode.Focus && focus != null) {
            AbstractPositionEntity ancestor = focus.getComputedAncestor();
            dist = ancestor.distToCamera - (ancestor.getRadius() + MIN_DIST);
        } else {
            dist = distance;
        }
        return dist > 0 ? dist * GlobalConf.scene.CAMERA_SPEED : 0;
    }

    /**
     * Depends on the distance to the focus
     * 
     * @return
     */
    public double getRotationUnits() {
        double dist;
        if (parent.mode == CameraMode.Focus) {
            AbstractPositionEntity ancestor = focus.getComputedAncestor();
            dist = ancestor.distToCamera - ancestor.getRadius();
        } else {
            dist = distance;
        }
        return Math.max(2000, Math.min(dist * Constants.U_TO_KM, GlobalConf.scene.ROTATION_SPEED));
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case FOCUS_CHANGE_CMD:
            // Check the type of the parameter: CelestialBody or String
            CelestialBody focus = null;
            if (data[0] instanceof String) {
                SceneGraphNode sgn = GaiaSky.instance.sg.getNode((String) data[0]);
                if (sgn instanceof CelestialBody) {
                    focus = (CelestialBody) sgn;
                    diverted = false;
                }
            } else if (data[0] instanceof CelestialBody) {
                focus = (CelestialBody) data[0];
                diverted = false;
            }
            if (focus != null) {
                setFocus(focus);
            }

            checkFocus();

            break;
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
        case CAMERA_POS_CMD:
            pos.set((double[]) data[0]);
            posinv.set(pos).scl(-1d);
            break;
        case CAMERA_DIR_CMD:
            direction.set((double[]) data[0]);
            break;
        case CAMERA_UP_CMD:
            up.set((double[]) data[0]);
            break;
        case CAMERA_FWD:
            addForwardForce((double) data[0]);
            break;
        case CAMERA_ROTATE:
            addRotateMovement((double) data[0], (double) data[1], false, true);
            break;
        case CAMERA_TURN:
            addRotateMovement((double) data[0], (double) data[1], true, true);
            break;
        case CAMERA_PAN:

            break;
        case CAMERA_ROLL:
            addRoll((double) data[0], GlobalConf.scene.CINEMATIC_CAMERA);
            break;
        case CAMERA_STOP:
            stopTotalMovement();
            break;
        case CAMERA_CENTER:
            diverted = false;
            break;
        case GO_TO_OBJECT_CMD:
            if (this.focus != null) {

                // Position camera near focus
                stopTotalMovement();

                this.focus.getAbsolutePosition(aux1);
                pos.set(aux1);

                pos.add(0, 0, -this.focus.size * 6);
                posinv.set(pos).scl(-1);
                direction.set(0, 0, 1);

            }
            break;
        case PLANETARIUM_FOCUS_ANGLE_CMD:
            if (data.length == 0)
                planetariumFocusAngle = 0;
            else
                planetariumFocusAngle = (float) data[0];
            break;
        case ORIENTATION_LOCK_CMD:
            previousOrientationAngle = 0;
            break;
        default:
            break;
        }

    }

    /**
     * Rotates the direction and up vector of this camera by the given angle
     * around the given axis, with the axis attached to given point. The
     * direction and up vector will not be orthogonalized.
     *
     * @param point
     *            the point to attach the axis to
     * @param axis
     *            the axis to rotate around
     * @param angle
     *            the angle
     */
    public void rotateAround(final Vector3d point, Vector3d axis, double angle) {
        aux3.set(point);
        aux3.sub(pos);
        translate(aux3);
        rotate(axis, angle);
        aux3.rotate(axis, angle);
        translate(-aux3.x, -aux3.y, -aux3.z);
    }

    public void rotate(Vector3d axis, double angle) {
        direction.rotate(axis, angle);
        up.rotate(axis, angle);
    }

    /**
     * Moves the camera by the given amount on each axis.
     * 
     * @param x
     *            the displacement on the x-axis
     * @param y
     *            the displacement on the y-axis
     * @param z
     *            the displacement on the z-axis
     */
    public void translate(double x, double y, double z) {
        pos.add(x, y, z);
    }

    /**
     * Moves the camera by the given vector.
     * 
     * @param vec
     *            the displacement vector
     */
    public void translate(Vector3d vec) {
        pos.add(vec);
    }

    /**
     * Applies the given force to this entity's acceleration
     * 
     * @param force
     */
    protected void applyForce(Vector3d force) {
        if (force != null)
            accel.add(force);
    }

    @Override
    public PerspectiveCamera[] getFrontCameras() {
        return new PerspectiveCamera[] { camera };
    }

    @Override
    public PerspectiveCamera getCamera() {
        return camera;
    }

    @Override
    public Vector3d getDirection() {
        return direction;
    }

    @Override
    public void setDirection(Vector3d dir) {
        this.direction.set(dir);
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
    public CameraMode getMode() {
        return parent.mode;
    }

    @Override
    public double getVelocity() {
        return parent.getVelocity();
    }

    @Override
    public boolean isFocus(CelestialBody cb) {
        return focus != null && cb == focus;
    }

    @Override
    public CelestialBody getFocus() {
        return getMode().equals(CameraMode.Focus) ? focus : null;
    }

    /**
     * Checks the position of the camera does not collide with the focus object.
     */
    public void checkFocus() {
        if (focus != null && !(focus instanceof Star)) {
            // Move camera if too close to focus
            this.focus.getAbsolutePosition(aux1);
            if (pos.dst(aux1) < this.focus.getRadius()) {
                // Position camera near focus
                stopTotalMovement();

                this.focus.getAbsolutePosition(aux1);
                pos.set(aux1);

                pos.add(0, 0, -this.focus.size * 6);
                posinv.set(pos).scl(-1);
                direction.set(0, 0, 1);
            }
        }
    }

    public void resetState() {
        pos.scl(0);
        posinv.scl(0);
        direction.set(0, 0, -1);
        for (PerspectiveCamera cam : cameras) {
            cam.position.scl(0);
            cam.direction.set(0, 0, -1);
            cam.update();
        }
    }

    @Override
    public void setCamera(PerspectiveCamera cam) {
        this.camera = cam;
    }

    public void setThrust(double thrust, int direction) {
        this.thrust = thrust;
        this.thrustDirection = direction;
    }

    @Override
    public void render(int rw, int rh) {
        // Renders crosshair if focus mode
        if (GlobalConf.scene.CROSSHAIR && getMode().equals(CameraMode.Focus)) {

            float cw = crosshair.getWidth();
            float ch = crosshair.getHeight();

            boolean draw = !GlobalConf.program.CUBEMAP360_MODE && !GlobalConf.program.STEREOSCOPIC_MODE;

            if (draw) {
                // Unproject focus
                focus.getPosition(aux1);

                aux1.put(auxf1);
                camera.project(auxf1);

                if (direction.angle(aux1) > 90) {
                    auxf1.x = rw - auxf1.x;
                    auxf1.y = rh - auxf1.y;

                    float w2 = rw / 2f;
                    float h2 = rh / 2f;

                    // Q1 | Q2
                    // -------
                    // Q3 | Q4

                    if (auxf1.x <= w2 && auxf1.y >= h2) {
                        // Q1
                        auxf1.x = chw2;
                        auxf1.y = rh - chh2;

                    } else if (auxf1.x > w2 && auxf1.y >= h2) {
                        // Q2
                        auxf1.x = rw - chw2;
                        auxf1.y = rh - chh2;
                    } else if (auxf1.x <= w2 && auxf1.y < h2) {
                        // Q3
                        auxf1.x = chw2;
                        auxf1.y = chh2;
                    } else if (auxf1.x > w2 && auxf1.y < h2) {
                        // Q4
                        auxf1.x = rw - chw2;
                        auxf1.y = chh2;
                    }

                }

                auxf1.x = MathUtils.clamp(auxf1.x, chw2, rw - chw2);
                auxf1.y = MathUtils.clamp(auxf1.y, chh2, rh - chh2);

                spriteBatch.begin();
                spriteBatch.draw(crosshair, auxf1.x - chw2, auxf1.y - chh2, cw, ch);
                spriteBatch.end();
            }
        }
    }

    @Override
    public void resize(int width, int height) {
        spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
    }

}
