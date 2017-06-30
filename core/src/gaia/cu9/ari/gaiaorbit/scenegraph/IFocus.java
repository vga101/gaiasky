package gaia.cu9.ari.gaiaorbit.scenegraph;

import gaia.cu9.ari.gaiaorbit.scenegraph.component.RotationComponent;
import gaia.cu9.ari.gaiaorbit.util.math.Matrix4d;
import gaia.cu9.ari.gaiaorbit.util.math.Quaterniond;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * Contract that all focus objects must implement.
 * 
 * @author tsagrista
 *
 */
public interface IFocus {

    /**
     * Returns the name of this focus.
     * 
     * @return The name.
     */
    public String getName();

    /**
     * Returns true if the focus is within the magnitude limit defined in
     * {@link gaia.cu9.ari.gaiaorbit.util.GlobalConf}
     * 
     * @return True if focus within magnitude limit
     */
    public boolean withinMagLimit();

    /**
     * Gets the first computed ancestor of this entity. Usually it is itself.
     * 
     * @return First computed ancestor
     */
    public AbstractPositionEntity getComputedAncestor();

    /**
     * Gets the first ancestor of this node that is of type {@link Star}
     * 
     * @return The first ancestor of type {@link Star}
     */
    public SceneGraphNode getFirstStarAncestor();

    /**
     * Returns the absolute position of this entity in the native coordinates
     * (equatorial system).
     * 
     * @param aux
     *            Vector3d where to put the return value
     * @return The absolute position, same as aux
     */
    public Vector3d getAbsolutePosition(Vector3d aux);

    /**
     * Gets the predicted position of this entity in the next time step in the
     * internal reference system using the given time provider and the given
     * camera.
     * 
     * @param aux
     *            The out vector where the result will be stored.
     * @param time
     *            The time frame provider.
     * @param camera
     *            The camera.
     * @param force
     *            Whether to force the computation if time is off.
     * @return The aux vector for chaining.
     */
    public Vector3d getPredictedPosition(Vector3d aux, ITimeFrameProvider time, ICamera camera, boolean force);

    /**
     * Returns the current distance to the camera in internal units.
     * 
     * @return The current distance to the camera, in internal units.
     */
    public double getDistToCamera();

    /**
     * Returns the current view angle of this entity, in radians.
     * 
     * @return The view angle in radians.
     */
    public double getViewAngle();

    /**
     * Returns the current apparent view angle (view angle corrected with the
     * field of view) of this entity, in radians.
     * 
     * @return The apparent view angle in radians.
     */
    public double getViewAngleApparent();

    /**
     * Returns the size (diameter) of this entity in internal units.
     * 
     * @return The size in internal units.
     */
    public double getSize();

    /**
     * Returns the radius of this focus object in internal units.
     * 
     * @return The radius of the focus, in internal units.
     */
    public float getRadius();

    /**
     * Returns the orientation matrix of this focus.
     * 
     * @return The orientation matrix. Can be null.
     */
    public Matrix4d getOrientation();

    /**
     * Returns the rotation component of this focus.
     * 
     * @return The rotation component. Can be null.
     */
    public RotationComponent getRotationComponent();

    /**
     * Returns the orientation quaternion of this focus.
     * 
     * @return The orientation quaternion. Can be null.
     */
    public Quaterniond getOrientationQuaternion();

}
