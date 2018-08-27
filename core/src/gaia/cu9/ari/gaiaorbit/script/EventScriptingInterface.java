package gaia.cu9.ari.gaiaorbit.script;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.utils.TimeUtils;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.EventManager.TimeFrame;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.ControlsWindow;
import gaia.cu9.ari.gaiaorbit.interfce.IGui;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.IFocus;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.IStarFocus;
import gaia.cu9.ari.gaiaorbit.scenegraph.Invisible;
import gaia.cu9.ari.gaiaorbit.scenegraph.Loc;
import gaia.cu9.ari.gaiaorbit.scenegraph.ModelBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Planet;
import gaia.cu9.ari.gaiaorbit.scenegraph.Polyline;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.camera.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.camera.NaturalCamera;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.Logger.Log;
import gaia.cu9.ari.gaiaorbit.util.LruCache;
import gaia.cu9.ari.gaiaorbit.util.Nature;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Intersectord;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector2d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * Implementation of the scripting interface using the event system.
 * 
 * @author Toni Sagrista
 *
 */
public class EventScriptingInterface implements IScriptingInterface, IObserver {
        private static final Log logger = Logger.getLogger(EventScriptingInterface.class);
        
	private EventManager em;
	private AssetManager manager;
	private LruCache<String, Texture> textures;

	private static EventScriptingInterface instance = null;

	public static EventScriptingInterface instance() {
		if (instance == null) {
			instance = new EventScriptingInterface();
		}
		return instance;
	}

	private Vector3d aux3d1, aux3d2, aux3d3, aux3d4, aux3d5, aux3d6;
	private Vector2d aux2d1;

	private Set<AtomicBoolean> stops;

	private EventScriptingInterface() {
		em = EventManager.instance;
		manager = GaiaSky.instance.manager;

		stops = new HashSet<AtomicBoolean>();

		aux3d1 = new Vector3d();
		aux3d2 = new Vector3d();
		aux3d3 = new Vector3d();
		aux3d4 = new Vector3d();
		aux3d5 = new Vector3d();
		aux3d6 = new Vector3d();
		aux2d1 = new Vector2d();

		em.subscribe(this, Events.INPUT_EVENT, Events.DISPOSE);
	}

	public void initializeTextures() {
		if (textures == null) {
			textures = new LruCache<String, Texture>(100);
		}
	}

	@Override
	public void activateRealTimeFrame() {
		Gdx.app.postRunnable(() -> {
			em.post(Events.EVENT_TIME_FRAME_CMD, TimeFrame.REAL_TIME);
		});
	}

	@Override
	public void activateSimulationTimeFrame() {
		Gdx.app.postRunnable(() -> {
			em.post(Events.EVENT_TIME_FRAME_CMD, TimeFrame.SIMULATION_TIME);
		});
	}

	@Override
	public void setHeadlineMessage(final String headline) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.POST_HEADLINE_MESSAGE, headline);
		});
	}

	@Override
	public void setSubheadMessage(final String subhead) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.POST_SUBHEAD_MESSAGE, subhead);
		});
	}

	@Override
	public void clearHeadlineMessage() {
		Gdx.app.postRunnable(() -> {
			em.post(Events.CLEAR_HEADLINE_MESSAGE);
		});
	}

	@Override
	public void clearSubheadMessage() {
		Gdx.app.postRunnable(() -> {
			em.post(Events.CLEAR_SUBHEAD_MESSAGE);
		});
	}

	@Override
	public void clearAllMessages() {
		Gdx.app.postRunnable(() -> {
			em.post(Events.CLEAR_MESSAGES);
		});
	}

	@Override
	public void disableInput() {
		Gdx.app.postRunnable(() -> {
			em.post(Events.INPUT_ENABLED_CMD, false);
		});
	}

	@Override
	public void enableInput() {
		Gdx.app.postRunnable(() -> {
			em.post(Events.INPUT_ENABLED_CMD, true);
		});
	}

	@Override
	public void setCinematicCamera(boolean cinematic) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.CAMERA_CINEMATIC_CMD, cinematic, false);
		});
	}

	@Override
	public void setCameraFocus(final String focusName) {
		setCameraFocus(focusName.toLowerCase(), 0.0f);
	}

	@Override
	public void setCameraFocus(final String focusName, final float waitTimeSeconds) {
		assert focusName != null : "Focus name can't be null";

		ISceneGraph sg = GaiaSky.instance.sg;
		if (sg.containsNode(focusName.toLowerCase())) {
			IFocus focus = sg.findFocus(focusName.toLowerCase());
			NaturalCamera cam = GaiaSky.instance.cam.naturalCamera;
			changeFocusAndWait(focus, cam, waitTimeSeconds);
		}
	}

	@Override
	public void setCameraFocusInstant(final String focusName) {
		assert focusName != null : "Focus name can't be null";

		ISceneGraph sg = GaiaSky.instance.sg;
		if (sg.containsNode(focusName.toLowerCase())) {
			IFocus focus = sg.findFocus(focusName.toLowerCase());
			em.post(Events.CAMERA_MODE_CMD, CameraMode.Focus);
			em.post(Events.FOCUS_CHANGE_CMD, focus);

			Gdx.app.postRunnable(() -> {
				// Instantly set the camera direction to look towards the focus
				double[] campos = GaiaSky.instance.cam.getPos().values();
				Vector3d dir = new Vector3d();
				focus.getAbsolutePosition(dir).sub(campos[0], campos[1], campos[2]);
				double[] d = dir.nor().values();
				em.post(Events.CAMERA_DIR_CMD, d);

			});
		}
	}

	@Override
	public void setCameraFocusInstantAndGo(final String focusName) {
		assert focusName != null : "Focus name can't be null";

		em.post(Events.CAMERA_MODE_CMD, CameraMode.Focus);
		em.post(Events.FOCUS_CHANGE_CMD, focusName, true);
		em.post(Events.GO_TO_OBJECT_CMD);
	}

	@Override
	public void setCameraLock(final boolean lock) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.FOCUS_LOCK_CMD, I18n.bundle.get("gui.camera.lock"), lock);
		});

	}

	@Override
	public void setCameraFree() {
		Gdx.app.postRunnable(() -> {
			em.post(Events.CAMERA_MODE_CMD, CameraMode.Free_Camera);
		});
	}

	@Override
	public void setCameraFov1() {
		Gdx.app.postRunnable(() -> {
			em.post(Events.CAMERA_MODE_CMD, CameraMode.Gaia_FOV1);
		});
	}

	@Override
	public void setCameraFov2() {
		Gdx.app.postRunnable(() -> {
			em.post(Events.CAMERA_MODE_CMD, CameraMode.Gaia_FOV2);
		});
	}

	@Override
	public void setCameraFov1and2() {
		Gdx.app.postRunnable(() -> {
			em.post(Events.CAMERA_MODE_CMD, CameraMode.Gaia_FOV1and2);
		});
	}

	@Override
	public void setCameraPostion(final double[] vec) {
		setCameraPosition(vec);
	}

	@Override
	public void setCameraPosition(final double[] vec) {
		if (vec.length != 3)
			throw new RuntimeException("vec parameter must have three components");
		Gdx.app.postRunnable(() -> {
			// Convert to km
			vec[0] = vec[0] * Constants.KM_TO_U;
			vec[1] = vec[1] * Constants.KM_TO_U;
			vec[2] = vec[2] * Constants.KM_TO_U;
			// Send event
			em.post(Events.CAMERA_POS_CMD, vec);
		});

	}

	@Override
	public double[] getCameraPosition() {
		Vector3d campos = GaiaSky.instance.cam.getPos();
		return new double[] { campos.x * Constants.U_TO_KM, campos.y * Constants.U_TO_KM,
				campos.z * Constants.U_TO_KM };
	}

	@Override
	public void setCameraDirection(final double[] dir) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.CAMERA_DIR_CMD, dir);
		});

	}

	@Override
	public double[] getCameraDirection() {
		Vector3d camdir = GaiaSky.instance.cam.getDirection();
		return new double[] { camdir.x, camdir.y, camdir.z };
	}

	@Override
	public void setCameraUp(final double[] up) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.CAMERA_UP_CMD, up);
		});

	}

	@Override
	public double[] getCameraUp() {
		Vector3d camup = GaiaSky.instance.cam.getUp();
		return new double[] { camup.x, camup.y, camup.z };
	}

	@Override
	public void setCameraPositionAndFocus(String focus, String other, double rotation, double viewAngle) {
		assert viewAngle > 0 : "View angle must be larger than zero";
		assert focus != null : "Focus can't be null";
		assert other != null : "Other can't be null";

		String focuslc = focus.toLowerCase();
		String otherlc = other.toLowerCase();
		ISceneGraph sg = GaiaSky.instance.sg;
		if (sg.containsNode(focuslc) && sg.containsNode(otherlc)) {
			IFocus focusObj = sg.findFocus(focuslc);
			IFocus otherObj = sg.findFocus(otherlc);
			setCameraPositionAndFocus(focusObj, otherObj, rotation, viewAngle);
		}
	}

	public void pointAtSkyCoordinate(double ra, double dec) {
		em.post(Events.CAMERA_MODE_CMD, CameraMode.Free_Camera);
		em.post(Events.FREE_MODE_COORD_CMD, (float) ra, (float) dec);
	}

	public void setCameraPositionAndFocus(IFocus focus, IFocus other, double rotation, double viewAngle) {
		assert viewAngle > 0 : "View angle must be larger than zero";
		assert focus != null : "Focus can't be null";
		assert other != null : "Other can't be null";

		em.post(Events.CAMERA_MODE_CMD, CameraMode.Focus);
		em.post(Events.FOCUS_CHANGE_CMD, focus);

		double radius = focus.getRadius();
		double dist = radius / Math.tan(Math.toRadians(viewAngle / 2)) + radius;

		// Up to ecliptic north pole
		Vector3d up = new Vector3d(0, 1, 0).mul(Coordinates.eclToEq());

		Vector3d focusPos = aux3d1;
		focus.getAbsolutePosition(focusPos);
		Vector3d otherPos = aux3d2;
		other.getAbsolutePosition(otherPos);

		Vector3d otherToFocus = aux3d3;
		otherToFocus.set(focusPos).sub(otherPos).nor();
		Vector3d focusToOther = aux3d4.set(otherToFocus);
		focusToOther.scl(-dist).rotate(up, rotation);

		// New camera position
		Vector3d newCamPos = aux3d5.set(focusToOther).add(focusPos).scl(Constants.U_TO_KM);

		// New camera direction
		Vector3d newCamDir = aux3d6.set(focusToOther);
		newCamDir.scl(-1).nor();

		// New up vector
		Vector3d newCamUp = up;

		// Finally, set values
		setCameraPosition(newCamPos.values());
		setCameraDirection(newCamDir.values());
		setCameraUp(newCamUp.values());

	}

	@Override
	public void setCameraSpeed(final float speed) {
		assert speed >= Constants.MIN_SLIDER && speed <= Constants.MAX_SLIDER : "Speed must be between "
				+ Constants.MIN_SLIDER + " and " + Constants.MAX_SLIDER;
		Gdx.app.postRunnable(() -> {
			em.post(Events.CAMERA_SPEED_CMD, speed / 10f, false);
		});

	}

	@Override
	public double getCameraSpeed() {
		return GaiaSky.instance.cam.getSpeed();
	}

	@Override
	public void setRotationCameraSpeed(final float speed) {
		assert speed >= Constants.MIN_SLIDER && speed <= Constants.MAX_SLIDER : "Speed must be between "
				+ Constants.MIN_SLIDER + " and " + Constants.MAX_SLIDER;
		Gdx.app.postRunnable(() -> {
			em.post(Events.ROTATION_SPEED_CMD, MathUtilsd.lint(speed, Constants.MIN_SLIDER, Constants.MAX_SLIDER,
					Constants.MIN_ROT_SPEED, Constants.MAX_ROT_SPEED), false);
		});
	}

	@Override
	public void setTurningCameraSpeed(final float speed) {
		assert speed >= Constants.MIN_SLIDER && speed <= Constants.MAX_SLIDER : "Speed must be between "
				+ Constants.MIN_SLIDER + " and " + Constants.MAX_SLIDER;
		Gdx.app.postRunnable(() -> {
			em.post(Events.TURNING_SPEED_CMD, MathUtilsd.lint(speed, Constants.MIN_SLIDER, Constants.MAX_SLIDER,
					Constants.MIN_TURN_SPEED, Constants.MAX_TURN_SPEED), false);
		});

	}

	@Override
	public void setCameraSpeedLimit(int index) {
		assert index >= 0 && index <= 19 : "Speed limit index must be in [0..19]";
		Gdx.app.postRunnable(() -> {
			em.post(Events.SPEED_LIMIT_CMD, index, false);
		});
	}

	@Override
	public void setCameraOrientationLock(boolean lock) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.ORIENTATION_LOCK_CMD, I18n.bundle.get("gui.camera.lock.orientation"), lock, false);
		});
	}

	@Override
	public void cameraForward(final double value) {
		assert value >= -1d && value <= 1d : "Value must be between -1 and 1";
		Gdx.app.postRunnable(() -> {
			em.post(Events.CAMERA_FWD, value);
		});

	}

	@Override
	public void cameraRotate(final double deltaX, final double deltaY) {
		assert deltaX >= 0d && deltaX <= 1d && deltaY >= 0d
				&& deltaY <= 1d : "DeltaX and deltaY must be between 0 and 1";
		Gdx.app.postRunnable(() -> {
			em.post(Events.CAMERA_ROTATE, deltaX, deltaY);
		});

	}

	@Override
	public void cameraRoll(final double roll) {
		assert roll >= 0d && roll <= 1d : "Roll must be between 0 and 1";
		Gdx.app.postRunnable(() -> {
			em.post(Events.CAMERA_ROLL, roll);
		});
	}

	@Override
	public void cameraTurn(final double deltaX, final double deltaY) {
		assert deltaX >= 0d && deltaX <= 1d && deltaY >= 0d
				&& deltaY <= 1d : "DeltaX and deltaY must be between 0 and 1";
		Gdx.app.postRunnable(() -> {
			em.post(Events.CAMERA_TURN, deltaX, deltaY);
		});
	}

	@Override
	public void cameraStop() {
		Gdx.app.postRunnable(() -> {
			em.post(Events.CAMERA_STOP);
		});

	}

	@Override
	public void cameraCenter() {
		Gdx.app.postRunnable(() -> {
			em.post(Events.CAMERA_CENTER);
		});
	}

	@Override
	public CelestialBody getClosestObjectToCamera() {
		return GaiaSky.instance.cam.getClosest();
	}

	@Override
	public void setFov(final float newFov) {
		assert newFov >= Constants.MIN_FOV && newFov <= Constants.MAX_FOV : "Fov value must be between "
				+ Constants.MIN_FOV + " and " + Constants.MAX_FOV;
		Gdx.app.postRunnable(() -> {
			em.post(Events.FOV_CHANGED_CMD, newFov);
		});
	}

	@Override
	public void setVisibility(final String key, final boolean visible) {
		Gdx.app.postRunnable(() -> {
			if (key.equals("element.propermotions")) {
				EventManager.instance.post(Events.PROPER_MOTIONS_CMD, key, visible);
			} else {
				em.post(Events.TOGGLE_VISIBILITY_CMD, key, false, visible);
			}
		});
	}

	@Override
	public void setProperMotionsNumberFactor(float factor) {
		Gdx.app.postRunnable(() -> {
			EventManager.instance.post(Events.PM_NUM_FACTOR_CMD, MathUtilsd.lint(factor, Constants.MIN_SLIDER,
					Constants.MAX_SLIDER, Constants.MIN_PM_NUM_FACTOR, Constants.MAX_PM_NUM_FACTOR), false);
		});
	}

	@Override
	public void setProperMotionsLengthFactor(float factor) {
		Gdx.app.postRunnable(() -> {
			EventManager.instance.post(Events.PM_LEN_FACTOR_CMD, factor, false);
		});
	}

	@Override
	public void setCrosshairVisibility(boolean visible) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.CROSSHAIR_CMD, visible);
		});
	}

	@Override
	public void setAmbientLight(final float value) {
		assert value >= Constants.MIN_SLIDER && value <= Constants.MAX_SLIDER : "Value must be between 0 and 100";
		Gdx.app.postRunnable(() -> {
			em.post(Events.AMBIENT_LIGHT_CMD, value / 100f);
		});
	}

	@Override
	public void setSimulationTime(int year, int month, int day, int hour, int min, int sec, int millisec) {
		LocalDateTime date = LocalDateTime.of(year, month, day, hour, min, sec, millisec);
		em.post(Events.TIME_CHANGE_CMD, date.toInstant(ZoneOffset.UTC));
	}

	@Override
	public void setSimulationTime(final long time) {
		assert time > 0 : "Time can not be negative";
		em.post(Events.TIME_CHANGE_CMD, Instant.ofEpochMilli(time));
	}

	@Override
	public long getSimulationTime() {
		ITimeFrameProvider time = GaiaSky.instance.time;
		return time.getTime().toEpochMilli();
	}

	@Override
	public int[] getSimulationTimeArr() {
		ITimeFrameProvider time = GaiaSky.instance.time;
		Instant instant = time.getTime();
		LocalDateTime c = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
		int[] result = new int[7];
		result[0] = c.get(ChronoField.YEAR_OF_ERA);
		result[1] = c.getMonthValue();
		result[2] = c.getDayOfMonth();
		result[3] = c.getHour();
		result[4] = c.getMinute();
		result[5] = c.getSecond();
		result[6] = c.get(ChronoField.MILLI_OF_SECOND);
		return result;
	}

	@Override
	public void startSimulationTime() {
		em.post(Events.TOGGLE_TIME_CMD, true, false);
	}

	@Override
	public void stopSimulationTime() {
		em.post(Events.TOGGLE_TIME_CMD, false, false);
	}

	@Override
	public boolean isSimulationTimeOn() {
		return GaiaSky.instance.time.isTimeOn();
	}

	@Override
	public void setSimulationPace(final double pace) {
		em.post(Events.PACE_CHANGE_CMD, pace);
	}

	@Override
	public void setTargetTime(long ms) {
		em.post(Events.TARGET_TIME_CMD, Instant.ofEpochMilli(ms));
	}

	@Override
	public void setTargetTime(int year, int month, int day, int hour, int min, int sec, int millisec) {
		em.post(Events.TARGET_TIME_CMD,
				LocalDateTime.of(year, month, day, hour, min, sec, millisec).toInstant(ZoneOffset.UTC));
	}

	@Override
	public void unsetTargetTime() {
		em.post(Events.TARGET_TIME_CMD);
	}

	@Override
	public void setStarBrightness(final float brightness) {
		assert brightness >= Constants.MIN_SLIDER
				&& brightness <= Constants.MAX_SLIDER : "Brightness value must be between 0 and 100";
		Gdx.app.postRunnable(() -> {
			em.post(Events.STAR_BRIGHTNESS_CMD, MathUtilsd.lint(brightness, Constants.MIN_SLIDER, Constants.MAX_SLIDER,
					Constants.MIN_STAR_BRIGHT, Constants.MAX_STAR_BRIGHT), false);
		});
	}

	@Override
	public float getStarBrightness() {
		return (float) MathUtilsd.lint(GlobalConf.scene.STAR_BRIGHTNESS, Constants.MIN_STAR_BRIGHT,
				Constants.MAX_STAR_BRIGHT, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
	}

	@Override
	public void setStarSize(final float size) {
		assert size >= Constants.MIN_SLIDER && size <= Constants.MAX_SLIDER : "Size value must be between 0 and 100";
		Gdx.app.postRunnable(() -> {
			em.post(Events.STAR_POINT_SIZE_CMD, MathUtilsd.lint(size, Constants.MIN_SLIDER, Constants.MAX_SLIDER,
					Constants.MIN_STAR_POINT_SIZE, Constants.MAX_STAR_POINT_SIZE), false);
		});
	}

	@Override
	public float getStarSize() {
		return MathUtilsd.lint(GlobalConf.scene.STAR_POINT_SIZE, Constants.MIN_STAR_POINT_SIZE,
				Constants.MAX_STAR_POINT_SIZE, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
	}

	@Override
	public float getMinStarOpacity() {
		return MathUtilsd.lint(GlobalConf.scene.POINT_ALPHA_MIN, Constants.MIN_STAR_MIN_OPACITY,
				Constants.MAX_STAR_MIN_OPACITY, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
	}

	@Override
	public void setMinStarOpacity(float opacity) {
		assert opacity >= Constants.MIN_SLIDER
				&& opacity <= Constants.MAX_SLIDER : "Opacity value must be between 0 and 100";
		Gdx.app.postRunnable(() -> {
			EventManager.instance.post(Events.STAR_MIN_OPACITY_CMD, MathUtilsd.lint(opacity, Constants.MIN_SLIDER,
					Constants.MAX_SLIDER, Constants.MIN_STAR_MIN_OPACITY, Constants.MAX_STAR_MIN_OPACITY), false);
		});
	}

	@Override
	public void configureFrameOutput(int width, int height, int fps, String folder, String namePrefix) {
		assert width > 0 : "Width must be positive";
		assert height > 0 : "Height must be positive";
		assert fps > 0 : "FPS must be positive";
		assert folder != null && namePrefix != null : "Folder and file name prefix must not be null";
		em.post(Events.CONFIG_FRAME_OUTPUT, width, height, fps, folder, namePrefix);
	}

	@Override
	public void configureRenderOutput(int width, int height, int fps, String folder, String namePrefix) {
		configureFrameOutput(width, height, fps, folder, namePrefix);
	}

	@Override
	public boolean isFrameOutputActive() {
		return GlobalConf.frame.RENDER_OUTPUT;
	}

	@Override
	public boolean isRenderOutputActive() {
		return isFrameOutputActive();
	}

	@Override
	public int getFrameOutputFps() {
		return GlobalConf.frame.RENDER_TARGET_FPS;
	}

	@Override
	public int getRenderOutputFps() {
		return getFrameOutputFps();
	}

	@Override
	public void setFrameOutput(boolean active) {
		em.post(Events.FRAME_OUTPUT_CMD, active);
	}

	@Override
	public SceneGraphNode getObject(String name) {
		ISceneGraph sg = GaiaSky.instance.sg;
		return sg.getNode(name.toLowerCase());
	}

	@Override
	public void setObjectSizeScaling(String name, double scalingFactor) {
		SceneGraphNode sgn = getObject(name);
		if (sgn == null) {
			logger.error("Object '" + name + "' does not exist");
			return;
		}
		if (sgn instanceof ModelBody) {
			ModelBody m = (ModelBody) sgn;
			m.setSizescalefactor(scalingFactor);
		} else {
			logger.error("Object '" + name + "' is not a model object");
			return;
		}
	}

	@Override
	public double getObjectRadius(String name) {
		ISceneGraph sg = GaiaSky.instance.sg;
		IFocus obj = sg.findFocus(name.toLowerCase());
		if (obj == null)
			return -1;
		else if (obj instanceof IStarFocus) {
			// TODO Remove this dirty hack
			return obj.getRadius() * 1.4856329941301618 * Constants.U_TO_KM;
		} else {
			return obj.getRadius() * Constants.U_TO_KM;
		}
	}

	@Override
	public void goToObject(String name) {
		goToObject(name, -1);
	}

	@Override
	public void goToObject(String name, double angle) {
		goToObject(name, angle, -1);
	}

	@Override
	public void goToObject(String name, double viewAngle, float waitTimeSeconds) {
		goToObject(name, viewAngle, waitTimeSeconds, null);
	}

	public void goToObject(String name, double viewAngle, float waitTimeSeconds, AtomicBoolean stop) {
		assert name != null : "Name can't be null";

		String namelc = name.toLowerCase();
		ISceneGraph sg = GaiaSky.instance.sg;
		if (sg.containsNode(namelc)) {
			IFocus focus = sg.findFocus(namelc);
			goToObject(focus, viewAngle, waitTimeSeconds, stop);
		}
	}

	public void goToObject(IFocus object, double viewAngle, float waitTimeSeconds, AtomicBoolean stop) {
		assert object != null : "Object can't be null";
		assert viewAngle > 0 : "Angle must be larger than zero";

		stops.add(stop);
		NaturalCamera cam = GaiaSky.instance.cam.naturalCamera;

		changeFocusAndWait(object, cam, waitTimeSeconds);

		/* target angle */
		double target = Math.toRadians(viewAngle);
		if (target < 0)
			target = Math.toRadians(20d);

		long prevtime = TimeUtils.millis();
		if (object.getViewAngleApparent() < target) {
			// Add forward movement while distance > target distance
			while (object.getViewAngleApparent() < target && (stop == null || (stop != null && !stop.get()))) {
				// dt in ms
				long dt = TimeUtils.timeSinceMillis(prevtime);
				prevtime = TimeUtils.millis();

				em.post(Events.CAMERA_FWD, 1d * dt);
				try {
					Thread.sleep(5);
				} catch (Exception e) {
				}
			}
		} else {
			// Add backward movement while distance > target distance
			while (object.getViewAngleApparent() > target && (stop == null || (stop != null && !stop.get()))) {
				// dt in ms
				long dt = TimeUtils.timeSinceMillis(prevtime);
				prevtime = TimeUtils.millis();

				em.post(Events.CAMERA_FWD, -1d * dt);
				try {
					Thread.sleep(5);
				} catch (Exception e) {
				}
			}
		}

		// We can stop now
		em.post(Events.CAMERA_STOP);

	}

	@Override
	public void landOnObject(String name) {
		landOnObject(name, null);
	}

	public void landOnObject(String name, AtomicBoolean stop) {
		assert name != null : "Name can't be null";

		String namelc = name.toLowerCase();
		ISceneGraph sg = GaiaSky.instance.sg;
		if (sg.containsNode(namelc)) {
			IFocus focus = sg.findFocus(namelc);
			landOnObject(focus, stop);
		}

	}

	public void landOnObject(IFocus object, AtomicBoolean stop) {
		assert object != null : "Object can't be null";

		stops.add(stop);
		if (object instanceof Planet) {
			NaturalCamera cam = GaiaSky.instance.cam.naturalCamera;
			// Focus wait - 2 seconds
			float waitTimeSeconds = -1;

			/**
			 * SAVE
			 */

			// Save speed, set it to 50
			double speed = GlobalConf.scene.CAMERA_SPEED;
			em.post(Events.CAMERA_SPEED_CMD, 25f / 10f, false);

			// Save turn speed, set it to 50
			double turnSpeedBak = GlobalConf.scene.TURNING_SPEED;
			em.post(Events.TURNING_SPEED_CMD, (float) MathUtilsd.lint(20d, Constants.MIN_SLIDER, Constants.MAX_SLIDER,
					Constants.MIN_TURN_SPEED, Constants.MAX_TURN_SPEED), false);

			// Save cinematic
			boolean cinematic = GlobalConf.scene.CINEMATIC_CAMERA;
			GlobalConf.scene.CINEMATIC_CAMERA = true;

			/**
			 * FOCUS
			 */

			changeFocusAndWait(object, cam, waitTimeSeconds);

			/* target distance */
			double target = 100 * Constants.M_TO_U;

			object.getAbsolutePosition(aux3d1).add(cam.posinv).nor();
			Vector3d dir = cam.direction;

			// Add forward movement while distance > target distance
			boolean distanceNotMet = (object.getDistToCamera() - object.getRadius()) > target;
			boolean viewNotMet = Math.abs(dir.angle(aux3d1)) < 90;

			long prevtime = TimeUtils.millis();
			while ((distanceNotMet || viewNotMet) && (stop == null || (stop != null && !stop.get()))) {
				// dt in ms
				long dt = TimeUtils.timeSinceMillis(prevtime);
				prevtime = TimeUtils.millis();

				if (distanceNotMet)
					em.post(Events.CAMERA_FWD, 0.1d * dt);
				else
					cam.stopForwardMovement();

				if (viewNotMet) {
					if (object.getDistToCamera() - object.getRadius() < object.getRadius() * 5)
						// Start turning where we are at n times the radius
						em.post(Events.CAMERA_TURN, 0d, dt / 500d);
				} else {
					cam.stopRotateMovement();
				}

				try {
					Thread.sleep(20);
				} catch (Exception e) {
				}

				// focus.transform.getTranslation(aux);
				viewNotMet = Math.abs(dir.angle(aux3d1)) < 90;
				distanceNotMet = (object.getDistToCamera() - object.getRadius()) > target;
			}

			// STOP
			em.post(Events.CAMERA_STOP);

			// Roll till done
			Vector3d up = cam.up;
			// aux1 <- camera-object
			object.getAbsolutePosition(aux3d1).sub(cam.pos);
			double ang1 = up.angle(aux3d1);
			double ang2 = up.cpy().rotate(cam.direction, 1).angle(aux3d1);
			double rollsign = ang1 < ang2 ? -1d : 1d;

			if (ang1 < 170) {

				rollAndWait(rollsign * 0.02d, 170d, 50l, cam, aux3d1, stop);
				// STOP
				cam.stopMovement();

				rollAndWait(rollsign * 0.006d, 176d, 50l, cam, aux3d1, stop);
				// STOP
				cam.stopMovement();

				rollAndWait(rollsign * 0.003d, 178d, 50l, cam, aux3d1, stop);
			}
			/**
			 * RESTORE
			 */

			// We can stop now
			em.post(Events.CAMERA_STOP);

			// Restore cinematic
			GlobalConf.scene.CINEMATIC_CAMERA = cinematic;

			// Restore speed
			em.post(Events.CAMERA_SPEED_CMD, (float) speed, false);

			// Restore turning speed
			em.post(Events.TURNING_SPEED_CMD, (float) turnSpeedBak, false);

		}

	}

	@Override
	public void landOnObjectLocation(String name, String locationName) {
		landOnObjectLocation(name, locationName, null);
	}

	public void landOnObjectLocation(String name, String locationName, AtomicBoolean stop) {
		assert name != null : "Name can't be null";

		stops.add(stop);
		String namelc = name.toLowerCase();
		ISceneGraph sg = GaiaSky.instance.sg;
		if (sg.containsNode(namelc)) {
			IFocus focus = sg.findFocus(namelc);
			landOnObjectLocation(focus, locationName, stop);
		}
	}

	public void landOnObjectLocation(IFocus object, String locationName, AtomicBoolean stop) {
		assert object != null : "Name can't be null";
		assert locationName != null : "locationName can't be null";

		stops.add(stop);
		if (object instanceof Planet) {
			Planet planet = (Planet) object;
			SceneGraphNode sgn = planet.getChildByNameAndType(locationName, Loc.class);
			if (sgn != null) {
				Loc location = (Loc) sgn;
				landOnObjectLocation(object, location.getLocation().x, location.getLocation().y, stop);
				return;
			}
			logger.info("Location '" + locationName + "' not found on object '" + object.getCandidateName() + "'");
		}
	}

	@Override
	public void landOnObjectLocation(String name, double longitude, double latitude) {
		landOnObjectLocation(name, longitude, latitude, null);
	}

	public void landOnObjectLocation(String name, double longitude, double latitude, AtomicBoolean stop) {
		assert name != null : "Name can't be null";

		String namelc = name.toLowerCase();
		ISceneGraph sg = GaiaSky.instance.sg;
		if (sg.containsNode(namelc)) {
			IFocus focus = sg.findFocus(namelc);
			landOnObjectLocation(focus, longitude, latitude, stop);
		}

	}

	public void landOnObjectLocation(IFocus object, double longitude, double latitude, AtomicBoolean stop) {
		assert object != null : "Object can't be null";
		assert latitude >= -90 && latitude <= 90 && longitude >= 0
				&& longitude <= 360 : "Latitude must be in [-90..90] and longitude must be in [0..360]";

		stops.add(stop);
		ISceneGraph sg = GaiaSky.instance.sg;
		String nameStub = object.getCandidateName() + " ";

		if (!sg.containsNode(nameStub)) {
			Invisible invisible = new Invisible(nameStub);
			sg.insert(invisible, true);
		}
		Invisible invisible = (Invisible) sg.getNode(nameStub);

		if (object instanceof Planet) {
			Planet planet = (Planet) object;
			NaturalCamera cam = GaiaSky.instance.cam.naturalCamera;

			double targetAngle = 35 * MathUtilsd.degRad;
			if (planet.viewAngle > targetAngle) {
				// Zoom out
				while (planet.viewAngle > targetAngle && (stop == null || (stop != null && !stop.get()))) {
					cam.addForwardForce(-5d);
					sleep(0.3f);
				}
				// STOP
				cam.stopMovement();
			}

			// Go to object
			goToObject(object, 20, -1, stop);

			// Save speed, set it to 50
			double speed = GlobalConf.scene.CAMERA_SPEED;
			em.post(Events.CAMERA_SPEED_CMD, 25f / 10f, false);

			// Save turn speed, set it to 50
			double turnSpeedBak = GlobalConf.scene.TURNING_SPEED;
			em.post(Events.TURNING_SPEED_CMD, (float) MathUtilsd.lint(50d, Constants.MIN_SLIDER, Constants.MAX_SLIDER,
					Constants.MIN_TURN_SPEED, Constants.MAX_TURN_SPEED), false);

			// Save rotation speed, set it to 20
			double rotationSpeedBak = GlobalConf.scene.ROTATION_SPEED;
			em.post(Events.ROTATION_SPEED_CMD, (float) MathUtilsd.lint(20d, Constants.MIN_SLIDER, Constants.MAX_SLIDER,
					Constants.MIN_ROT_SPEED, Constants.MAX_ROT_SPEED), false);

			// Save cinematic
			boolean cinematic = GlobalConf.scene.CINEMATIC_CAMERA;
			GlobalConf.scene.CINEMATIC_CAMERA = true;

			// Save crosshair
			boolean crosshair = GlobalConf.scene.CROSSHAIR;
			GlobalConf.scene.CROSSHAIR = false;

			// Get target position
			Vector3d target = aux3d1;
			planet.getPositionAboveSurface(longitude, latitude, 50, target);

			// Get object position
			Vector3d objectPosition = planet.getAbsolutePosition(aux3d2);

			// Check intersection with object
			boolean intersects = Intersectord.checkIntersectSegmentSphere(cam.pos, target, objectPosition,
					planet.getRadius());

			if (intersects) {
				cameraRotate(5, 5);
			}

			while (intersects && (stop == null || (stop != null && !stop.get()))) {
				sleep(0.1f);

				objectPosition = planet.getAbsolutePosition(aux3d2);
				intersects = Intersectord.checkIntersectSegmentSphere(cam.pos, target, objectPosition,
						planet.getRadius());
			}

			cameraStop();

			invisible.ct = planet.ct;
			invisible.pos.set(target);

			// Go to object
			goToObject(nameStub, 20, 0, stop);

			// Restore cinematic
			GlobalConf.scene.CINEMATIC_CAMERA = cinematic;

			// Restore speed
			em.post(Events.CAMERA_SPEED_CMD, (float) speed, false);

			// Restore turning speed
			em.post(Events.TURNING_SPEED_CMD, (float) turnSpeedBak, false);

			// Restore rotation speed
			em.post(Events.ROTATION_SPEED_CMD, (float) rotationSpeedBak, false);

			// Restore crosshair
			GlobalConf.scene.CROSSHAIR = crosshair;

			// Land
			landOnObject(object, stop);

		}

		sg.remove(invisible, true);
	}

	private void rollAndWait(double roll, double target, long sleep, NaturalCamera cam, Vector3d camobj,
			AtomicBoolean stop) {
		// Apply roll and wait
		double ang = cam.up.angle(camobj);

		while (ang < target && (stop == null || (stop != null && !stop.get()))) {
			cam.addRoll(roll, false);

			try {
				Thread.sleep(sleep);
			} catch (Exception e) {
			}

			ang = cam.up.angle(aux3d1);
		}
	}

	@Override
	public double getDistanceTo(String name) {
		String namelc = name.toLowerCase();
		ISceneGraph sg = GaiaSky.instance.sg;
		if (sg.containsNode(namelc)) {
			SceneGraphNode object = sg.getNode(namelc);
			if (object instanceof IFocus) {
				IFocus obj = (IFocus) object;
				return (obj.getDistToCamera() - obj.getRadius()) * Constants.U_TO_KM;
			}
		}
		return -1;
	}

	@Override
	public double[] getObjectPosition(String name) {
		String namelc = name.toLowerCase();
		ISceneGraph sg = GaiaSky.instance.sg;
		if (sg.containsNode(namelc)) {
			SceneGraphNode object = sg.getNode(namelc);
			if (object instanceof IFocus) {
				IFocus obj = (IFocus) object;
				obj.getAbsolutePosition(namelc, aux3d1);
				return new double[] { aux3d1.x, aux3d1.y, aux3d1.z };
			}
		}
		return null;
	}

	@Override
	public void setGuiScrollPosition(final float pixelY) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.GUI_SCROLL_POSITION_CMD, pixelY);
		});

	}

	@Override
	public void displayMessageObject(final int id, final String message, final float x, final float y, final float r,
			final float g, final float b, final float a, final float fontSize) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.ADD_CUSTOM_MESSAGE, id, message, x, y, r, g, b, a, fontSize);
		});

	}

	@Override
	public void displayTextObject(final int id, final String text, final float x, final float y, final float maxWidth,
			final float maxHeight, final float r, final float g, final float b, final float a, final float fontSize) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.ADD_CUSTOM_TEXT, id, text, x, y, maxWidth, maxHeight, r, g, b, a, fontSize);
		});

	}

	@Override
	public void displayImageObject(final int id, final String path, final float x, final float y, final float r,
			final float g, final float b, final float a) {
		Gdx.app.postRunnable(() -> {
			Texture tex = getTexture(path);
			em.post(Events.ADD_CUSTOM_IMAGE, id, tex, x, y, r, g, b, a);
		});

	}

	@Override
	public void displayImageObject(final int id, final String path, final float x, final float y) {
		Gdx.app.postRunnable(() -> {
			Texture tex = getTexture(path);
			em.post(Events.ADD_CUSTOM_IMAGE, id, tex, x, y);
		});

	}

	@Override
	public void removeAllObjects() {
		Gdx.app.postRunnable(() -> {
			em.post(Events.REMOVE_ALL_OBJECTS);
		});

	}

	@Override
	public void removeObject(final int id) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.REMOVE_OBJECTS, new int[] { id });
		});

	}

	@Override
	public void removeObjects(final int[] ids) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.REMOVE_OBJECTS, ids);
		});

	}

	@Override
	public void maximizeInterfaceWindow() {
		Gdx.app.postRunnable(() -> {
			em.post(Events.GUI_FOLD_CMD, false);
		});

	}

	@Override
	public void minimizeInterfaceWindow() {
		Gdx.app.postRunnable(() -> {
			em.post(Events.GUI_FOLD_CMD, true);
		});

	}

	@Override
	public void setGuiPosition(final float x, final float y) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.GUI_MOVE_CMD, x, y);
		});

	}

	@Override
	public void waitForInput() {
		while (inputCode < 0) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				em.post(Events.JAVA_EXCEPTION, e);
			}
		}
		// Consume
		inputCode = -1;

	}

	@Override
	public void waitForEnter() {
		while (inputCode != Keys.ENTER) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				em.post(Events.JAVA_EXCEPTION, e);
			}
		}
		// Consume
		inputCode = -1;
	}

	@Override
	public void waitForInput(int keyCode) {
		while (inputCode != keyCode) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				em.post(Events.JAVA_EXCEPTION, e);
			}
		}
		// Consume
		inputCode = -1;
	}

	int inputCode = -1;

	@Override
	public void notify(Events event, Object... data) {
		switch (event) {
		case INPUT_EVENT:
			inputCode = (Integer) data[0];
			break;
		case DISPOSE:
			// Stop all
			for (AtomicBoolean stop : stops) {
				if (stop != null)
					stop.set(true);
			}
			break;
		default:
			break;
		}

	}

	@Override
	public int getScreenWidth() {
		return Gdx.graphics.getWidth();
	}

	@Override
	public int getScreenHeight() {
		return Gdx.graphics.getHeight();
	}

	@Override
	public float[] getPositionAndSizeGui(String name) {
		IGui gui = GaiaSky.instance.mainGui;
		Actor actor = gui.getGuiStage().getRoot().findActor(name);
		if (actor != null) {
			float x = actor.getX();
			float y = actor.getY();
			// x and y relative to parent, so we need to add coordinates of
			// parents up to top
			Group parent = actor.getParent();
			while (parent != null) {
				x += parent.getX();
				y += parent.getY();
				parent = parent.getParent();
			}
			return new float[] { x, y, actor.getWidth(), actor.getHeight() };
		} else {
			return null;
		}

	}

	@Override
	public void expandGuiComponent(String name) {
		IGui gui = GaiaSky.instance.mainGui;
		ControlsWindow controls = (ControlsWindow) gui.getGuiStage().getRoot()
				.findActor(I18n.bundle.get("gui.controlpanel"));
		controls.getCollapsiblePane(name).expandPane();
	}

	@Override
	public void collapseGuiComponent(String name) {
		IGui gui = GaiaSky.instance.mainGui;
		ControlsWindow controls = (ControlsWindow) gui.getGuiStage().getRoot()
				.findActor(I18n.bundle.get("gui.controlpanel"));
		controls.getCollapsiblePane(name).collapsePane();
	}

	@Override
	public String getVersionNumber() {
		return GlobalConf.version.version;
	}

	@Override
	public boolean waitFocus(String name, long timeoutMs) {
		long iniTime = TimeUtils.millis();
		NaturalCamera cam = GaiaSky.instance.cam.naturalCamera;
		while (cam.focus == null || !cam.focus.getName().equalsIgnoreCase(name)) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				em.post(Events.JAVA_EXCEPTION, e);
			}
			long spent = TimeUtils.millis() - iniTime;
			if (timeoutMs > 0 && spent > timeoutMs) {
				// Timeout!
				return true;
			}
		}
		return false;
	}

	private Texture getTexture(String path) {
		if (textures == null || !textures.containsKey(path)) {
			preloadTextures(path);
		}
		return textures.get(path);
	}

	@Override
	public void preloadTextures(String... paths) {
		initializeTextures();
		for (final String path : paths) {
			// This only works in async mode!
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					manager.load(path, Texture.class);
				}
			});
			while (!manager.isLoaded(path)) {
				try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					em.post(Events.JAVA_EXCEPTION, e);
				}
			}
			Texture tex = manager.get(path, Texture.class);
			textures.put(path, tex);
		}
	}

	@Override
	public void startRecordingCameraPath() {
		em.post(Events.RECORD_CAMERA_CMD, true);
	}

	@Override
	public void stopRecordingCameraPath() {
		em.post(Events.RECORD_CAMERA_CMD, false);
	}

	@Override
	public void runCameraRecording(String path) {
		em.post(Events.PLAY_CAMERA_CMD, path);
	}

	@Override
	public void sleep(float seconds) {
		if (this.isFrameOutputActive()) {
			this.sleepFrames(Math.round(this.getFrameOutputFps() * seconds));
		} else {
			try {
				Thread.sleep(Math.round(seconds * 1000));
			} catch (InterruptedException e) {
				em.post(Events.JAVA_EXCEPTION, e);
			}
		}

	}

	@Override
	public void sleepFrames(int frames) {
		long iniframe = GaiaSky.instance.frames;
		while (GaiaSky.instance.frames - iniframe < frames) {
			// Active wait, fix this
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				em.post(Events.JAVA_EXCEPTION, e);
			}
		}

	}

	/**
	 * Checks if the object is the current focus of the given camera. If it is not,
	 * it sets it as focus and waits if necessary.
	 * 
	 * @param object          The new focus object.
	 * @param cam             The current camera.
	 * @param waitTimeSeconds Max time to wait for the camera to face the focus, in
	 *                        seconds. If negative, we wait until the end.
	 */
	private void changeFocusAndWait(IFocus object, NaturalCamera cam, float waitTimeSeconds) {
		// Post focus change and wait, if needed
		IFocus currentFocus = cam.getFocus();
		if (currentFocus != object) {
			em.post(Events.CAMERA_MODE_CMD, CameraMode.Focus);
			em.post(Events.FOCUS_CHANGE_CMD, object);

			// Wait til camera is facing focus or
			if (waitTimeSeconds < 0) {
				waitTimeSeconds = Float.MAX_VALUE;
			}
			long start = System.currentTimeMillis();
			long elapsedTimeMs;
			while (!cam.facingFocus) {
				elapsedTimeMs = System.currentTimeMillis() - start;
				if (elapsedTimeMs / 1000f > waitTimeSeconds) {
					// We've waited long enough, stop!
					break;
				}
				// Wait
				try {
					Thread.sleep(100);
				} catch (Exception e) {
				}
			}
		}
	}

	@Override
	public double[] galacticToInternalCartesian(double l, double b, double r) {
		Vector3d pos = Coordinates.sphericalToCartesian(l * Nature.TO_RAD, b * Nature.TO_RAD, r,
				new Vector3d());
		pos.mul(Coordinates.galacticToEquatorial());
		return new double[] { pos.x, pos.y, pos.z };
	}

	@Override
	public double[] eclipticToInternalCartesian(double l, double b, double r) {
		Vector3d pos = Coordinates.sphericalToCartesian(l * Nature.TO_RAD, b * Nature.TO_RAD, r,
				new Vector3d());
		pos.mul(Coordinates.eclipticToEquatorial());
		return new double[] { pos.x, pos.y, pos.z };
	}

	@Override
	public double[] equatorialToInternalCartesian(double ra, double dec, double r) {
		Vector3d pos = Coordinates.sphericalToCartesian(ra * Nature.TO_RAD, dec * Nature.TO_RAD, r,
				new Vector3d());
		return new double[] { pos.x, pos.y, pos.z };
	}

	public double[] internalCartesianToEquatorial(double x, double y, double z) {
		Vector3d in = new Vector3d(x, y, z);
		Vector3d out = new Vector3d();
		Coordinates.cartesianToSpherical(in, out);
		return new double[] { out.x * Nature.TO_DEG, out.y * Nature.TO_DEG, in.len() };
	}

	@Override
	public double[] equatorialToGalactic(double[] eq) {
		aux3d1.set(eq).mul(Coordinates.eqToGal());
		return aux3d1.values();
	}

	@Override
	public double[] equatorialToEcliptic(double[] eq) {
		aux3d1.set(eq).mul(Coordinates.eqToEcl());
		return aux3d1.values();
	}

	@Override
	public double[] galacticToEquatorial(double[] gal) {
		aux3d1.set(gal).mul(Coordinates.galToEq());
		return aux3d1.values();
	}

	@Override
	public double[] eclipticToEquatorial(double[] ecl) {
		aux3d1.set(ecl).mul(Coordinates.eclToEq());
		return aux3d1.values();
	}

	@Override
	public void setBrightnessLevel(double level) {
		assert level >= -1d && level <= 1d : "Brightness level value must be in [-1..1]: " + Double.toString(level);
		Gdx.app.postRunnable(() -> {
			em.post(Events.BRIGHTNESS_CMD, (float) level, false);
		});

	}

	@Override
	public void setContrastLevel(double level) {
		assert level >= 0d && level <= 2d : "Contrast level value must be in [0..2]: " + Double.toString(level);
		Gdx.app.postRunnable(() -> {
			em.post(Events.CONTRAST_CMD, (float) level, false);
		});
	}

	@Override
	public void setHueLevel(double level) {
		assert level >= 0d && level <= 2d : "Hue level value must be in [0..2]: " + Double.toString(level);
		Gdx.app.postRunnable(() -> {
			em.post(Events.HUE_CMD, (float) level, false);
		});
	}

	@Override
	public void setSaturationLevel(double level) {
		assert level >= 0d && level <= 2d : "Saturation level value must be in [0..2]: " + Double.toString(level);
		Gdx.app.postRunnable(() -> {
			em.post(Events.SATURATION_CMD, (float) level, false);
		});
	}

	@Override
	public void set360Mode(boolean state) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.CUBEMAP360_CMD, state, false);
		});
	}

	@Override
	public void setCubemapResolution(int resolution) {
		if (resolution > 19 && resolution < 15000) {
			Gdx.app.postRunnable(() -> {
				em.post(Events.CUBEMAP_RESOLUTION_CMD, resolution);
			});
		} else {
			logger.error("Cubemap resolution must be 20 <= resolution <= 15000");
		}
	}

	@Override
	public void setStereoscopicMode(boolean state) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.STEREOSCOPIC_CMD, state, false);
		});
	}

	@Override
	public void setStereoscopicProfile(int index) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.STEREO_PROFILE_CMD, index);
		});
	}

	@Override
	public void setPlanetariumMode(boolean state) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.PLANETARIUM_CMD, state, false);
		});

	}

	@Override
	public long getCurrentFrameNumber() {
		return GaiaSky.instance.frames;
	}

	@Override
	public void setLensFlare(boolean state) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.LENS_FLARE_CMD, state, false);
		});
	}

	@Override
	public void setMotionBlur(boolean state) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.MOTION_BLUR_CMD, state ? Constants.MOTION_BLUR_VALUE : 0f, false);
		});
	}

	@Override
	public void setStarGlow(boolean state) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.LIGHT_SCATTERING_CMD, state, false);
		});
	}

	@Override
	public void setBloom(float value) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.BLOOM_CMD, value, false);
		});
	}

	@Override
	public void setSmoothLodTransitions(boolean value) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.OCTREE_PARTICLE_FADE_CMD, I18n.bundle.get("element.octreeparticlefade"), value);
		});

	}

	@Override
	public double[] rotate3(double[] vector, double[] axis, double angle) {
		Vector3d v = aux3d1.set(vector);
		Vector3d a = aux3d2.set(axis);
		return v.rotate(a, angle).values();
	}

	@Override
	public double[] rotate2(double[] vector, double angle) {
		Vector2d v = aux2d1.set(vector);
		return v.rotate(angle).values();
	}

	@Override
	public double[] cross3(double[] vec1, double[] vec2) {
		return aux3d1.set(vec1).crs(aux3d2.set(vec2)).values();
	}

	@Override
	public double dot3(double[] vec1, double[] vec2) {
		return aux3d1.set(vec1).dot(aux3d2.set(vec2));
	}

	@Override
	public void addPolyline(String name, double[] points, double[] color) {
		addPolyline(name, points, color, 1f);
	}

	@Override
	public void addPolyline(String name, double[] points, double[] color, float lineWidth) {
		Polyline pl = new Polyline();
		pl.setCt("Others");
		pl.setColor(color);
		pl.setName(name);
		pl.setPoints(points);
		pl.setLineWidth(lineWidth);
		pl.setParent("Universe");
		pl.initialize();

		Gdx.app.postRunnable(() -> {
			GaiaSky.instance.sg.insert(pl, true);
		});
	}

	@Override
	public void removeModelObject(String name) {
		Gdx.app.postRunnable(() -> {
			SceneGraphNode sgn = GaiaSky.instance.sg.getNode(name);
			if (sgn != null) {
				GaiaSky.instance.sg.remove(sgn, true);
			}
		});
	}

	@Override
	public void postRunnable(Runnable runnable) {
		Gdx.app.postRunnable(runnable);
	}

	@Override
	public void parkRunnable(String id, Runnable runnable) {
		em.post(Events.POST_RUNNABLE, id, runnable);
	}

	@Override
	public void unparkRunnable(String id) {
		em.post(Events.UNPOST_RUNNABLE, id);
	}

	@Override
	public void setCameraState(double[] pos, double[] dir, double[] up) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.CAMERA_POS_CMD, pos);
			em.post(Events.CAMERA_DIR_CMD, dir);
			em.post(Events.CAMERA_UP_CMD, up);
		});
	}

	@Override
	public void setCameraStateAndTime(double[] pos, double[] dir, double[] up, long time) {
		Gdx.app.postRunnable(() -> {
			em.post(Events.CAMERA_POS_CMD, pos);
			em.post(Events.CAMERA_DIR_CMD, dir);
			em.post(Events.CAMERA_UP_CMD, up);
			em.post(Events.TIME_CHANGE_CMD, Instant.ofEpochMilli(time));
		});
	}

}
