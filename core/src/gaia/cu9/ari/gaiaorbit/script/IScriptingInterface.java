package gaia.cu9.ari.gaiaorbit.script;

import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;

/**
 * Scripting interface. Provides an interface to the Gaia Sandbox core and
 * exposes all the methods that are callable from a script in order to interact
 * with the program (create demonstrations, tutorials, load data, etc.). You
 * should never use any integration other than this interface for scripting.
 * 
 * @author Toni Sagrista
 *
 */
public interface IScriptingInterface {

    /**
     * Pre-loads the given images as textures for later use. They will be cached
     * so that they do not need to be loaded in the next use.
     * 
     * @param paths
     *            The texture paths.
     */
    public void preloadTextures(String... paths);

    /**
     * Sets the current time frame to <b>real time,</b>. All the commands
     * executed after this command becomes active will be in the <b>real
     * time</b> frame (clock ticks).
     */
    public void activateRealTimeFrame();

    /**
     * Sets the current time frame to <b>simulation time,</b>. All the commands
     * executed after this command becomes active will be in the <b>simulation
     * time</b> frame (simulation clock in the app).
     */
    public void activateSimulationTimeFrame();

    /**
     * Sets a headline message that will appear in a big font in the screen.
     * 
     * @param headline
     *            The headline text.
     */
    public void setHeadlineMessage(String headline);

    /**
     * Sets a subhead message that will appear in a small font below the
     * headline.
     * 
     * @param subhead
     *            The subhead text.
     */
    public void setSubheadMessage(String subhead);

    /**
     * Clears the headline messge.
     */
    public void clearHeadlineMessage();

    /**
     * Clears the subhead message
     */
    public void clearSubheadMessage();

    /**
     * Clears both the subhead and the headline messages.
     */
    public void clearAllMessages();

    /**
     * Adds a new one-line message in the screen with the given id and the given
     * coordinates. If an object already exists with the given id, it is
     * removed. However, if a message object already exists with the same id,
     * its properties are updated.
     * 
     * @param id
     *            A unique identifier, used to identify this message when you
     *            want to remove it.
     * @param message
     *            The string message, to be displayed in one line. But explicit
     *            newline breaks the line.
     * @param x
     *            The x coordinate of the bottom-left corner, in [0..1] from
     *            left to right. This is not resolution-dependant.
     * @param y
     *            The y coordinate of the bottom-left corner, in [0..1] from
     *            bottom to top. This is not resolution-dependant.
     * @param r
     *            The red component of the color in [0..1].
     * @param g
     *            The green component of the color in [0..1].
     * @param b
     *            The blue component of the color in [0..1].
     * @param a
     *            The alpha component of the color in [0..1].
     * @param fontSize
     *            The size of the font. The system will use the existing font
     *            closest to the chosen size.
     */
    public void displayMessageObject(int id, String message, float x, float y, float r, float g, float b, float a, float fontSize);

    /**
     * Adds a new multi-line text in the screen with the given id, coordinates
     * and size. If an object already exists with the given id, it is removed.
     * However, if a text object already exists with the same id, its properties
     * are updated.
     * 
     * @param id
     *            A unique identifier, used to identify this message when you
     *            want to remove it.
     * @param text
     *            The string message, to be displayed line-wrapped in the box
     *            defined by maxWidth and maxHeight. Explicit newline still
     *            breaks the line.
     * @param x
     *            The x coordinate of the bottom-left corner, in [0..1] from
     *            left to right. This is not resolution-dependant.
     * @param y
     *            The y coordinate of the bottom-left corner, in [0..1] from
     *            bottom to top. This is not resolution-dependant.
     * @param maxWidth
     *            The maximum width in screen percentage [0..1]. Set to 0 to let
     *            the system decide.
     * @param maxHeight
     *            The maximum height in screen percentage [0..1]. Set to 0 to
     *            let the system decide.
     * @param r
     *            The red component of the color in [0..1].
     * @param g
     *            The green component of the color in [0..1].
     * @param b
     *            The blue component of the color in [0..1].
     * @param a
     *            The alpha component of the color in [0..1].
     * @param fontSize
     *            The size of the font. The system will use the existing font
     *            closest to the chosen size.
     */
    public void displayTextObject(int id, String text, float x, float y, float maxWidth, float maxHeight, float r, float g, float b, float a, float fontSize);

    /**
     * Adds a new image object at the given coordinates. If an object already
     * exists with the given id, it is removed. However, if an image object
     * already exists with the same id, its properties are updated.<br>
     * <strong>Warning: This method will only work in the asynchronous mode. Run
     * the script with the "asynchronous" check box activated!</strong>
     * 
     * @param id
     *            A unique identifier, used to identify this message when you
     *            want to remove it.
     * @param path
     *            The path to the image. It can either be an absolute path (not
     *            recommended) or a path relative to the Gaia Sandbox folder.
     * @param x
     *            The x coordinate of the bottom-left corner, in [0..1] from
     *            left to right. This is not resolution-dependant.
     * @param y
     *            The y coordinate of the bottom-left corner, in [0..1] from
     *            bottom to top. This is not resolution-dependant.
     */
    public void displayImageObject(int id, String path, float x, float y);

    /**
     * Adds a new image object at the given coordinates. If an object already
     * exists with the given id, it is removed. However, if an image object
     * already exists with the same id, its properties are updated.<br>
     * <strong>Warning: This method will only work in the asynchronous mode. Run
     * the script with the "asynchronous" check box activated!</strong>
     * 
     * @param id
     *            A unique identifier, used to identify this message when you
     *            want to remove it.
     * @param path
     *            The path to the image. It can either be an absolute path (not
     *            recommended) or a path relative to the Gaia Sky folder.
     * @param x
     *            The x coordinate of the bottom-left corner, in [0..1] from
     *            left to right. This is not resolution-dependant.
     * @param y
     *            The y coordinate of the bottom-left corner, in [0..1] from
     *            bottom to top. This is not resolution-dependant.
     * @param r
     *            The red component of the color in [0..1].
     * @param g
     *            The green component of the color in [0..1].
     * @param b
     *            The blue component of the color in [0..1].
     * @param a
     *            The alpha component of the color in [0..1].
     */
    public void displayImageObject(int id, final String path, float x, float y, float r, float g, float b, float a);

    /**
     * Removes all objects.
     */
    public void removeAllObjects();

    /**
     * Removes the item with the given id.
     * 
     * @param id
     *            Integer with the integer id of the object to remove.
     */
    public void removeObject(int id);

    /**
     * Removes the items with the given ids. They can either messages, images or
     * whatever else.
     * 
     * @param ids
     *            Vector with the integer ids of the objects to remove
     */
    public void removeObjects(int[] ids);

    /**
     * Disables all input events from mouse, keyboard, touchscreen, etc.
     */
    public void disableInput();

    /**
     * Enables all input events.
     */
    public void enableInput();

    /**
     * Enables or disables the cinematic camera mode.
     * 
     * @param cinematic
     *            Whether to enable or disable the cinematic mode.
     */
    public void setCinematicCamera(boolean cinematic);

    /**
     * Sets the camera in focus mode with the focus object that bears the given
     * <code>focusName</code>. It returns immediately, so it does not wait for
     * the camera direction to point to the focus.
     * 
     * @param focusName
     *            The name of the new focus object.
     */
    public void setCameraFocus(String focusName);

    /**
     * Sets the camera in focus mode with the focus object that bears the given
     * <code>focusName</code>. The amount of time to block and wait for the
     * camera to face the focus can also be specified in
     * <code>waitTimeSeconds</code>.
     * 
     * @param focusName
     *            The name of the new focus object.
     * @param waitTimeSeconds
     *            Maximum time in seconds to wait for the camera to face the
     *            focus. If negative, we wait indefinitely.
     */
    public void setCameraFocus(String focusName, float waitTimeSeconds);

    /**
     * Sets the camera in focus mode with the given focus object. It also
     * instantly sets the camera direction vector to point towards the focus.
     * 
     * @param focusName
     *            The name of the new focus object.
     */
    public void setCameraFocusInstant(final String focusName);

    /**
     * Activates or deactivates the camera lock to the focus reference system
     * when in focus mode.
     * 
     * @param lock
     *            Activate or deactivate the lock.
     */
    public void setCameraLock(boolean lock);

    /**
     * Sets the camera in free mode.
     */
    public void setCameraFree();

    /**
     * Sets the camera in FoV1 mode. The camera is positioned in Gaia's focal
     * plane and observes what Gaia observes through its field of view 1.
     */
    public void setCameraFov1();

    /**
     * Sets the camera in FoV2 mode. The camera is positioned in Gaia's focal
     * plane and observes what Gaia observes through its field of view 2.
     */
    public void setCameraFov2();

    /**
     * Sets the camera in Fov1 and 2 mode. The camera is positioned in Gaia's
     * focal plane and observes what Gaia observes through its two fields of
     * view.
     */
    public void setCameraFov1and2();

    /**
     * Sets the camera position to the given coordinates, in Km, equatorial
     * system.
     * 
     * @param vec
     *            Vector of three components in internal coordinates and Km.
     * @deprecated Use {@link #setCameraPosition(double[])} instead.
     */
    public void setCameraPostion(double[] vec);

    /**
     * Sets the camera position to the given coordinates, in Km, equatorial
     * system.
     * 
     * @param vec
     *            Vector of three components in internal coordinates and Km.
     */
    public void setCameraPosition(double[] vec);

    /**
     * Gets the current camera position, in km.
     * 
     * @return The camera position coordinates in the internal reference system,
     *         in km.
     */
    public double[] getCameraPosition();

    /**
     * Sets the camera direction vector to the given vector, equatorial system.
     * 
     * @param dir
     *            The direction vector in equatorial coordinates.
     */
    public void setCameraDirection(double[] dir);

    /**
     * Gets the current camera direction vector.
     * 
     * @return The camera direction vector in the internal reference system.
     */
    public double[] getCameraDirection();

    /**
     * Sets the camera up vector to the given vector, equatorial system.
     * 
     * @param up
     *            The up vector in equatorial coordinates.
     */
    public void setCameraUp(double[] up);

    /**
     * Gets the current camera up vector.
     * 
     * @return The camera up vector in the internal reference system.
     */
    public double[] getCameraUp();

    /**
     * Sets the focus and instantly moves the camera to a point in the line
     * defined by <code>focus</code>-<code>other</code> and rotated
     * <code>rotation</code> degrees around <code>focus<code> using the camera
     * up vector as a rotation axis.
     * 
     * @param focus
     *            The name of the focus object.
     * @param other
     *            The name of the other object, to the fine a line from this to
     *            foucs. Usually a light source.
     * @param rotation
     *            The rotation angle, in degrees.
     * @param viewAngle
     *            The view angle which determines the distance, in degrees.
     */
    public void setCameraPositionAndFocus(String focus, String other, double rotation, double viewAngle);

    /**
     * Changes the speed multiplier of the camera and its acceleration.
     * 
     * @param speed
     *            The new speed, from 1 to 100.
     */
    public void setCameraSpeed(float speed);

    /**
     * Gets the current physical speed of the camera in km/h.
     * 
     * @return The current speed of the camera in km/h.
     */
    public double getCameraSpeed();

    /**
     * Changes the speed of the camera when it rotates around a focus.
     * 
     * @param speed
     *            The new rotation speed, from 1 to 100.
     */
    public void setRotationCameraSpeed(float speed);

    /**
     * Changes the turning speed of the camera.
     * 
     * @param speed
     *            The new turning speed, from 1 to 100.
     */
    public void setTurningCameraSpeed(float speed);

    /**
     * Sets the speed limit of the camera given an index. The index corresponds
     * to the following:
     * <ul>
     * <li>0 - 100 Km/h</li>
     * <li>1 - 1 c</li>
     * <li>2 - 2 c</li>
     * <li>3 - 10 c</li>
     * <li>4 - 1e3 c</li>
     * <li>5 - 1 AU/s</li>
     * <li>6 - 10 AU/s</li>
     * <li>7 - 1000 AU/s</li>
     * <li>8 - 10000 AU/s</li>
     * <li>9 - 1 pc/s</li>
     * <li>10 - 1 pc/s</li>
     * <li>11 - 2 pc/s</li>
     * <li>12 - 10 pc/s</li>
     * <li>13 - 1000 pc/s</li>
     * <li>14 - unlimited</li>
     * </ul>
     * 
     * @param index
     *            The index of the top speed.
     */
    public void setCameraSpeedLimit(int index);

    /**
     * Locks or unlocks the orientation of the camera to the focus object's
     * rotation.
     * 
     * @param lock
     *            Whether to lock or unlock the camera orientation to the focus
     */
    public void setCameraOrientationLock(boolean lock);

    /**
     * Adds a forward movement to the camera with the given value. If value is
     * negative the movement is backwards.
     * 
     * @param value
     *            The magnitude of the movement, between -1 and 1.
     */
    public void cameraForward(double value);

    /**
     * Adds a rotation movement to the camera, or a pitch/yaw if in free mode.
     * 
     * @param deltaX
     *            The x component, between 0 and 1. Positive is right and
     *            negative is left.
     * @param deltaY
     *            The y component, between 0 and 1. Positive is up and negative
     *            is down.
     */
    public void cameraRotate(double deltaX, double deltaY);

    /**
     * Adds a roll force to the camera.
     * 
     * @param roll
     *            The intensity of the roll.
     */
    public void cameraRoll(double roll);

    /**
     * Adds a turn force to the camera. If the camera is in focus mode, it
     * permanently deviates the line of sight from the focus until centered
     * again.
     * 
     * @param deltaX
     *            The x component, between 0 and 1. Positive is right and
     *            negative is left.
     * @param deltaY
     *            The y component, between 0 and 1. Positive is up and negative
     *            is down.
     */
    public void cameraTurn(double deltaX, double deltaY);

    /**
     * Stops all camera motion.
     */
    public void cameraStop();

    /**
     * Centers the camera to the focus, removing any deviation of the line of
     * sight. Useful to center the focus object again after turning.
     */
    public void cameraCenter();

    /**
     * Returns the closest object to the camera in this instant as a
     * {@link gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody}.
     * 
     * @return The closest object to the camera
     */
    public CelestialBody getClosestObjectToCamera();

    /**
     * Changes the field of view of the camera.
     * 
     * @param newFov
     *            The new field of view value in degrees, between 20 and 160.
     */
    public void setFov(float newFov);

    /**
     * Sets the component described by the given name visible or invisible.
     * 
     * @param key
     *            The key of the component, see
     *            {@link gaia.cu9.ari.gaiaorbit.render.ComponentType}. Usually
     *            'element.stars', 'element.moons', 'element.atmospheres', etc.
     * @param visible
     *            The visible value.
     */
    public void setVisibility(String key, boolean visible);

    /**
     * Sets the visibility of the crosshair in focus and free modes.
     * 
     * @param visible
     *            The visibility state.
     */
    public void setCrosshairVisibility(boolean visible);

    /**
     * Sets the ambient light to a certain value.
     * 
     * @param value
     *            The value of the ambient light, between 0 and 100.
     */
    public void setAmbientLight(float value);

    /**
     * Sets the time of the application.
     * 
     * @param year
     *            The year to represent
     * @param month
     *            The month-of-year to represent, from 1 (January) to 12
     *            (December)
     * @param day
     *            The day-of-month to represent, from 1 to 31
     * @param hour
     *            The hour-of-day to represent, from 0 to 23
     * @param min
     *            The minute-of-hour to represent, from 0 to 59
     * @param sec
     *            The second-of-minute to represent, from 0 to 59
     * @param millisec
     *            The millisecond-of-second, from 0 to 999
     */
    public void setSimulationTime(int year, int month, int day, int hour, int min, int sec, int millisec);

    /**
     * Sets the time of the application. The long value represents specified
     * number of milliseconds since the standard base time known as "the epoch",
     * namely January 1, 1970, 00:00:00 GMT.
     * 
     * @param time
     *            Number of milliseconds since the epoch (Jan 1, 1970)
     */
    public void setSimulationTime(long time);

    /**
     * Returns the current simulation time as the number of milliseconds since
     * Jan 1, 1970.
     * 
     * @return Number of milliseconds since the epoch (Jan 1, 1970)
     */
    public long getSimulationTime();

    /**
     * Returns the current simulation time in an array.
     * 
     * @return The current simulation time in an array with the given indices.
     *         <ul>
     *         <li>0 - The year</li>
     *         <li>1 - The month, from 1 (January) to 12 (December)</li>
     *         <li>2 - The day-of-month, from 1 to 31</li>
     *         <li>3 - The hour-of-day, from 0 to 23</li>
     *         <li>4 - The minute-of-hour, from 0 to 59</li>
     *         <li>5 - The second-of-minute, from 0 to 59</li>
     *         <li>6 - The millisecond-of-second, from 0 to 999</li>
     *         </ul>
     */
    public int[] getSimulationTimeArr();

    /**
     * Starts the simulation.
     */
    public void startSimulationTime();

    /**
     * Stops the simulation time.
     */
    public void stopSimulationTime();

    /**
     * Changes the pace of time.
     * 
     * @param pace
     *            The pace in number of simulation hours per real time second.
     */
    public void setSimulationPace(double pace);

    /**
     * Sets the star brightness value.
     * 
     * @param brightness
     *            The brightness value, between 0 and 100.
     */
    public void setStarBrightness(float brightness);

    /**
     * Configures the frame outputting system, setting the resolution of the
     * images, the target frames per second, the output folder and the image
     * name prefix.
     * 
     * @param width
     *            Width of images.
     * @param height
     *            Height of images.
     * @param fps
     *            Target frames per second (number of images per second).
     * @param folder
     *            The output folder path.
     * @param namePrefix
     *            The file name prefix.
     * 
     */
    public void configureRenderOutput(int width, int height, int fps, String folder, String namePrefix);

    /**
     * Is the system outputting frames to images?
     * 
     * @return True if the render output is active.
     */
    public boolean isRenderOutputActive();

    /**
     * Gets the current FPS setting in the render output system.
     * 
     * @return The FPS setting.
     */
    public int getRenderOutputFps();

    /**
     * Activates or deactivates the image output system. If called with true,
     * the system starts outputting images right away.
     * 
     * @param active
     *            Whether to activate or deactivate the frame output system.
     * 
     */
    public void setFrameOutput(boolean active);

    /**
     * Gets an object by <code>name</code> or id (HIP, TYC, sourceId).
     * 
     * @param name
     *            The name or id (HIP, TYC, sourceId) of the object.
     * @return The object as a
     *         {@link gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode}, or null
     *         if it does not exist.
     */
    public SceneGraphNode getObject(String name);

    /**
     * Gets the size of the object identified by <code>name</code>, in Km, by
     * name or id (HIP, TYC, sourceId).
     * 
     * @param name
     *            The name or id (HIP, TYC, sourceId) of the object.
     * @return The radius of the object in Km. If the object identifed by name
     *         or id (HIP, TYC, sourceId). does not exist, it returns a negative
     *         value.
     */
    public double getObjectRadius(String name);

    /**
     * Runs a seamless trip to the object with the name <code>focusName</code>
     * until the object view angle is <code>20 degrees</code>.
     * 
     * @param name
     *            The name or id (HIP, TYC, sourceId) of the object.
     */
    public void goToObject(String name);

    /**
     * Runs a seamless trip to the object with the name <code>focusName</code>
     * until the object view angle <code>viewAngle</code> is met. If angle is
     * negative, the default angle is <code>20 degrees</code>.
     * 
     * @param name
     *            The name or id (HIP, TYC, sourceId) of the object.
     * @param viewAngle
     *            The target view angle of the object, in degrees. The angle
     *            gets larger and larger as we approach the object.
     */
    public void goToObject(String name, double viewAngle);

    /**
     * Runs a seamless trip to the object with the name <code>focusName</code>
     * until the object view angle <code>viewAngle</code> is met. If angle is
     * negative, the default angle is <code>20 degrees</code>. If
     * <code>waitTimeSeconds</code> is positive, it indicates the number of
     * seconds to wait (block the function) for the camera to face the focus
     * before starting the forward movement. This very much depends on the
     * <code>turn velocity</code> of the camera. See
     * {@link #setTurningCameraSpeed(float)}.
     * 
     * @param name
     *            The name or id (HIP, TYC, sourceId) of the object.
     * @param viewAngle
     *            The target view angle of the object, in degrees. The angle
     *            gets larger and larger as we approach the object.
     * @param waitTimeSeconds
     *            The seconds to wait for the camera direction vector and the
     *            vector from the camera position to the target object to be
     *            aligned.
     */
    public void goToObject(String name, double viewAngle, float waitTimeSeconds);

    /**
     * Lands on the object with the given name, if it is an instance of
     * {@link gaia.cu9.ari.gaiaorbit.scenegraph.Planet}. The land location is
     * determined by the line of sight from the current position of the camera
     * to the object.
     * 
     * @param name
     *            The proper name of the object.
     */
    public void landOnObject(String name);

    /**
     * Lands on the object with the given <code>name</code>, if it is an
     * instance of {@link gaia.cu9.ari.gaiaorbit.scenegraph.Planet}, at the
     * location with the given name, if it exists.
     * 
     * @param name
     *            The proper name of the object.
     * @param locationName
     *            The name of the location to land on
     */
    public void landOnObjectLocation(String name, String locationName);

    /**
     * Lands on the object with the given <code>name</code>, if it is an
     * instance of {@link gaia.cu9.ari.gaiaorbit.scenegraph.Planet}, at the
     * location specified in by [latitude, longitude], in degrees.
     * 
     * @param name
     *            The proper name of the object.
     * @param longitude
     *            The location longitude, in degrees.
     * @param latitude
     *            The location latitude, in degrees.
     */
    public void landOnObjectLocation(String name, double longitude, double latitude);

    /**
     * Returns the distance to the surface of the object identified with the
     * given <code>name</code>. If the object is an abstract node or does not
     * exist, it returns a negative distance.
     * 
     * @param name
     *            The name or id (HIP, TYC, sourceId) of the object.
     * @return The distance to the object in km if it exists, a negative value
     *         otherwise.
     */
    public double getDistanceTo(String name);

    /**
     * Sets the vertical scroll position in the GUI.
     * 
     * @param pixelY
     *            The pixel to set the scroll position to.
     */
    public void setGuiScrollPosition(float pixelY);

    /**
     * Maximizes the interface window.
     */
    public void maximizeInterfaceWindow();

    /**
     * Minimizes the interface window.
     */
    public void minimizeInterfaceWindow();

    /**
     * Moves the interface window to a new position.
     * 
     * @param x
     *            The new x coordinate of the new top-left corner of the window,
     *            in [0..1] from left to right.
     * @param y
     *            The new y coordinate of the new top-left corner of the window,
     *            in [0..1] from bottom to top.
     */
    public void setGuiPosition(float x, float y);

    /**
     * Blocks the execution until any kind of input (keyboard, mouse, etc.) is
     * received.
     */
    public void waitForInput();

    /**
     * Blocks the execution until the Enter key is pressed.
     */
    public void waitForEnter();

    /**
     * Blocks the execution until the given key or button is pressed.
     * 
     * @param code
     *            The key or button code. Please see
     *            {@link com.badlogic.gdx.Input}.
     */
    public void waitForInput(int code);

    /**
     * Returns the screen width in pixels.
     * 
     * @return The screen width in pixels.
     */
    public int getScreenWidth();

    /**
     * Returns the screen height in pixels.
     * 
     * @return The screen height in pixels.
     */
    public int getScreenHeight();

    /**
     * Returns the size and position of the GUI element that goes by the given
     * name or null if such element does not exist. <strong>Warning> This will
     * only work in asynchronous mode.</strong>
     * 
     * @param name
     *            The name of the gui element.
     * @return A vector of floats with the position (0, 1) of the bottom left
     *         corner in pixels from the bottom-left of the screen and the size
     *         (2, 3) in pixels of the element.
     */
    public float[] getPositionAndSizeGui(String name);

    /**
     * Returns the version number string.
     * 
     * @return The version number string.
     */
    public String getVersionNumber();

    /**
     * Blocks the script until the focus is the object indicated by the name.
     * There is an optional time out.
     * 
     * @param name
     *            The name of the focus to wait for
     * @param timeoutMs
     *            Timeout in ms to wait. Set negative to disable timeout.
     * @return True if the timeout ran out. False otherwise.
     */
    public boolean waitFocus(String name, long timeoutMs);

    /**
     * Starts recording the camera path to a temporary file. This command has no
     * effect if the camera is already being recorded.
     */
    public void startRecordingCameraPath();

    /**
     * Stops the current camera recording. This command has no effect if the
     * camera was not being recorded.
     */
    public void stopRecordingCameraPath();

    /**
     * Runs the camera recording file with the given path.
     * 
     * @param path
     *            The path of the camera recording file to run.
     */
    public void runCameraRecording(String path);

    /**
     * Sleeps for the given number of seconds in the application time (FPS), so
     * if we are capturing frames and the frame rate is set to 30 FPS, the
     * command sleep(1) will put the script to sleep for 30 frames.
     * 
     * @param seconds
     *            The number of seconds to wait.
     */
    public void sleep(float seconds);

    /**
     * Sleeps for a number of frames. This is very useful for scripts which need
     * to run alongside the frame output system.
     * 
     * @param frames
     *            The number of frames to wait.
     */
    public void sleepFrames(int frames);

    /**
     * Expands the component with the given name.
     * 
     * @param name
     *            The name, as in `CameraComponent` or `ObjectsComponent`
     */
    public void expandGuiComponent(String name);

    /**
     * Collapses the component with the given name.
     * 
     * @param name
     *            The name, as in `CameraComponent` or `ObjectsComponent`
     */
    public void collapseGuiComponent(String name);

    /**
     * Converts galactic coordinates to the internal cartesian coordinate
     * system.
     * 
     * @param l
     *            The galactic longitude in degrees.
     * @param b
     *            The galactic latitude in degrees.
     * @param r
     *            The distance in Km.
     * @return An array of doubles containing <code>[x, y, z]</code> in the
     *         internal reference system, in internal units.
     */
    public double[] galacticToInternalCartesian(double l, double b, double r);

    /**
     * Converts ecliptic coordinates to the internal cartesian coordinate
     * system.
     * 
     * @param l
     *            The ecliptic longitude in degrees.
     * @param b
     *            The ecliptic latitude in degrees.
     * @param r
     *            The distance in Km.
     * @return An array of doubles containing <code>[x, y, z]</code> in the
     *         internal reference system, in internal units.
     */
    public double[] eclipticToInternalCartesian(double l, double b, double r);

    /**
     * Converts equatorial coordinates to the internal cartesian coordinate
     * system.
     * 
     * @param ra
     *            The right ascension in degrees.
     * @param dec
     *            The declination in degrees.
     * @param r
     *            The distance in Km.
     * @return An array of doubles containing <code>[x, y, z]</code> in the
     *         internal reference system, in internal units.
     */
    public double[] equatorialToInternalCartesian(double ra, double dec, double r);

    /**
     * Converts internal cartesian coordinates to equatorial
     * <code>[ra, dec, distance]</code> coordinates.
     * 
     * @param x
     *            The x component, in any distance units.
     * @param y
     *            The y component, in any distance units.
     * @param z
     *            The z component, in any distance units.
     * @return An array of doubles containing <code>[ra, dec, distance]</code>
     *         with <code>ra</code> and <code>dec</code> in degrees and
     *         <code>distance</code> in the same distance units as the input
     *         position.
     */
    public double[] internalCartesianToEquatorial(double x, double y, double z);

}
