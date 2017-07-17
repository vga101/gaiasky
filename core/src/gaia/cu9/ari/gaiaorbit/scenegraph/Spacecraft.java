package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.ILineRenderable;
import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.render.system.LineRenderSystem;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * The spacecraft.
 * 
 * @author tsagrista
 *
 */
public class Spacecraft extends ModelBody implements IModelRenderable, ILineRenderable, IObserver {

    /** This is the power **/
    public static final double thrustLength = 1e12d;

    /**
     * Factor (adapt to be able to navigate small and large scale structures
     **/
    public static final double[] thrustFactor = new double[17];
    static {
        double val = 0.1;
        for (int i = 0; i < 17; i++) {
            thrustFactor[i] = val * Math.pow(10, i);
        }
    }

    /** Seconds to reach full power **/
    private static final double fullPowerTime = 1.0;

    /** Collision stop-at value **/
    private static final double stopAt = 10000 * Constants.M_TO_U;

    /** Force, acceleration and velocity **/
    public Vector3d force, accel, vel;
    /** Direction and up vectors **/
    public Vector3d direction, up;

    /** Float counterparts **/
    public Vector3 posf, directionf, upf;

    /** Engine thrust vector **/
    public Vector3d thrust;

    /** Mass in kg **/
    public double mass;

    /** Factor hack **/
    public double factor = 1d;

    /**
     * Index of the current engine power setting
     */
    public int thrustFactorIndex = 0;

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
    private Quaternion qf;

    private boolean render;

    public Spacecraft() {
        super();
        localTransform = new Matrix4();
        EventManager.instance.subscribe(this, Events.CAMERA_MODE_CMD);

        // position attributes
        pos.set(2e6 * Constants.KM_TO_U, 0, 0);
        force = new Vector3d();
        accel = new Vector3d();
        vel = new Vector3d();

        // orientation
        direction = new Vector3d(1, 0, 0);
        up = new Vector3d(0, 1, 0);

        posf = new Vector3();
        directionf = new Vector3(1, 0, 0);
        upf = new Vector3(0, 1, 0);

        // engine thrust direction
        // our spacecraft is a rigid solid so thrust is always the camera direction vector
        thrust = new Vector3d(direction).scl(thrustLength);
        enginePower = 0;

        // not stabilising
        leveling = false;

        qf = new Quaternion();
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

        // Broadcast me
        EventManager.instance.post(Events.SPACECRAFT_LOADED, this);

        EventManager.instance.subscribe(this, Events.SPACECRAFT_STABILISE_CMD, Events.SPACECRAFT_STOP_CMD, Events.SPACECRAFT_THRUST_DECREASE_CMD, Events.SPACECRAFT_THRUST_INCREASE_CMD, Events.SPACECRAFT_THRUST_SET_CMD);
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
        case SPACECRAFT_STABILISE_CMD:
            leveling = (Boolean) data[0];
            break;
        case SPACECRAFT_STOP_CMD:
            stopping = (Boolean) data[0];
            break;
        case SPACECRAFT_THRUST_DECREASE_CMD:
            decreaseThrustFactorIndex(true);
            break;
        case SPACECRAFT_THRUST_INCREASE_CMD:
            increaseThrustFactorIndex(true);
            break;
        case SPACECRAFT_THRUST_SET_CMD:
            setThrustFactorIndex((Integer) data[0], false);
            break;
        default:
            break;
        }

    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
        super.updateLocal(time, camera);

        if (render) {
            //TODO remove clname, cldist
            EventManager.instance.post(Events.SPACECRAFT_INFO, yaw % 360, pitch % 360, roll % 360, vel.len(), "None", 0d, thrustFactor[thrustFactorIndex], enginePower, yawp, pitchp, rollp);
        }

        addToRenderLists(camera);

    }

    protected void updateLocalTransform() {
        // Local transform
        localTransform.idt().setToLookAt(posf, directionf.add(posf), upf).inv();
        localTransform.getRotation(qf);
        float sizeFac = (float) (factor * size);
        localTransform.scale(sizeFac, sizeFac, sizeFac);

    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
        double dt = Gdx.graphics.getDeltaTime();
        // Poll keys
        pollKeys(dt);

        /** POSITION **/
        // Compute force from thrust
        thrust.set(direction).scl(thrustLength * thrustFactor[thrustFactorIndex] * enginePower);
        force.set(thrust);

        if (stopping) {
            double speed = vel.len();
            if (speed != 0) {
                enginePower = -1;
                thrust.set(vel).nor().scl(thrustLength * thrustFactor[thrustFactorIndex] * enginePower);
                force.set(thrust);
            }

            Vector3d nextvel = aux3d3.get().set(force).scl(1d / mass).scl(Constants.M_TO_U).scl(dt).add(vel);

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
        Vector3d acc = aux3d1.get().set(accel).scl(Constants.M_TO_U);

        if (true) {
            double vellen = vel.len();
            vel.set(direction).nor().scl(vellen);
        }
        vel.add(acc.scl(dt));
        Vector3d velo = aux3d2.get().set(vel);
        // New position in auxd3
        Vector3d position = aux3d3.get().set(pos).add(velo.scl(dt));
        // Check collision!
        // TODO
        //        if (closest != null) {
        //            // d1 is the new distance to the centre of the object
        //            if (!vel.isZero() && Intersectord.distanceSegmentPoint(pos, aux3d3, closest.pos) < closest.getRadius() + stopAt) {
        //                EventManager.instance.post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), "Crashed against " + closest.name + "!");
        //
        //                Array<Vector3d> intersections = Intersectord.intersectRaySphere(pos, aux3d3, closest.pos, closest.getRadius() + stopAt);
        //
        //                if (intersections.size >= 1) {
        //                    pos.set(intersections.get(0));
        //                }
        //
        //                stopAllMovement();
        //            } else {
        //                pos.set(position);
        //            }
        //        } else {
        pos.set(position);
        //        }

        factor = pos.len() > 100 * Constants.AU_TO_U ? 100 : 1;

        if (leveling) {
            // No velocity, we just stop Euler angle motions
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

        // apply yaw
        direction.rotate(up, yawdiff);

        // apply pitch
        Vector3d aux1 = aux3d1.get().set(direction).crs(up);
        direction.rotate(aux1, pitchdiff);
        up.rotate(aux1, pitchdiff);

        // apply roll
        up.rotate(direction, -rolldiff);

        double len = direction.len();
        pitch = Math.asin(direction.y / len);
        yaw = Math.atan2(direction.z, direction.x);
        roll += rolldiff;

        pitch = Math.toDegrees(pitch);
        yaw = Math.toDegrees(yaw);

        // Update float vectors
        aux1.set(pos).add(camera.getInversePos()).put(posf);
        direction.put(directionf);
        up.put(upf);

    }

    private void pollKeys(double dt) {
        double powerStep = dt / fullPowerTime;
        if (Gdx.input.isKeyPressed(Keys.W))
            setEnginePower(enginePower + powerStep);
        if (Gdx.input.isKeyPressed(Keys.S))
            setEnginePower(enginePower - powerStep);

        if (Gdx.input.isKeyPressed(Keys.A))
            setRollPower(rollp + powerStep);
        if (Gdx.input.isKeyPressed(Keys.D))
            setRollPower(rollp - powerStep);

        if (Gdx.input.isKeyPressed(Keys.DOWN))
            setPitchPower(pitchp + powerStep);
        if (Gdx.input.isKeyPressed(Keys.UP))
            setPitchPower(pitchp - powerStep);

        if (Gdx.input.isKeyPressed(Keys.LEFT))
            setYawPower(yawp + powerStep);
        if (Gdx.input.isKeyPressed(Keys.RIGHT))
            setYawPower(yawp - powerStep);
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

    public void increaseThrustFactorIndex(boolean broadcast) {
        thrustFactorIndex = (thrustFactorIndex + 1) % thrustFactor.length;
        EventManager.instance.post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), "Thrust factor: " + thrustFactor[thrustFactorIndex]);
        if (broadcast)
            EventManager.instance.post(Events.SPACECRAFT_THRUST_INFO, thrustFactorIndex);
    }

    public void decreaseThrustFactorIndex(boolean broadcast) {
        thrustFactorIndex = thrustFactorIndex - 1;
        if (thrustFactorIndex < 0)
            thrustFactorIndex = thrustFactor.length - 1;
        EventManager.instance.post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), "Thrust factor: " + thrustFactor[thrustFactorIndex]);
        if (broadcast)
            EventManager.instance.post(Events.SPACECRAFT_THRUST_INFO, thrustFactorIndex);
    }

    public void setThrustFactorIndex(int i, boolean broadcast) {
        assert i >= 0 && i < thrustFactor.length : "Index " + i + " out of range of thrustFactor vector: [0.." + (thrustFactor.length - 1);
        thrustFactorIndex = i;
        EventManager.instance.post(Events.POST_NOTIFICATION, this.getClass().getSimpleName(), "Thrust factor: " + thrustFactor[thrustFactorIndex]);
        if (broadcast)
            EventManager.instance.post(Events.SPACECRAFT_THRUST_INFO, thrustFactorIndex);
    }

    /**
     * Adds this entity to the necessary render lists after the distance to the
     * camera and the view angle have been determined.
     */
    protected void addToRenderLists(ICamera camera) {
        camera.checkClosest(this);
        addToRender(this, RenderGroup.MODEL_F);
        addToRender(this, RenderGroup.LINE);
    }

    public void setModel(ModelComponent mc) {
        this.mc = mc;
    }

    /**
     * Sets the absolute size of this entity
     * 
     * @param size
     */
    public void setSize(Double size) {
        this.size = size.floatValue() * (float) Constants.KM_TO_U;
    }

    public void setSize(Long size) {
        this.size = (float) size * (float) Constants.KM_TO_U;
    }

    public void setMass(Double mass) {
        this.mass = mass;
    }

    public boolean isStopping() {
        return stopping;
    }

    public boolean isStabilising() {
        return leveling;
    }

    @Override
    public double getDistToCamera() {
        return distToCamera;
    }

    public Quaternion getRotationQuaternion() {
        return qf;
    }

    @Override
    public void render(ModelBatch modelBatch, float alpha, double t) {
        mc.touch();
        mc.setTransparency(alpha * opacity);
        modelBatch.render(mc.instance, mc.env);
    }

    @Override
    public boolean hasAtmosphere() {
        return false;
    }

    public void dispose() {
        super.dispose();
    }

    @Override
    public void render(LineRenderSystem renderer, ICamera camera, float alpha) {
        // Direction
        Vector3d d = aux3d1.get().set(direction);
        d.nor().scl(.5e-4);
        renderer.addLine(posf.x, posf.y, posf.z, posf.x + d.x, posf.y + d.y, posf.z + d.z, 1, 0, 0, 1);

        // Up
        Vector3d u = aux3d1.get().set(up);
        u.nor().scl(.2e-4);
        renderer.addLine(posf.x, posf.y, posf.z, posf.x + u.x, posf.y + u.y, posf.z + u.z, 0, 0, 1, 1);

    }

    @Override
    public double THRESHOLD_NONE() {
        return 0;
    }

    @Override
    public double THRESHOLD_QUAD() {
        return 0;
    }

    @Override
    protected float labelFactor() {
        return 0;
    }

}
