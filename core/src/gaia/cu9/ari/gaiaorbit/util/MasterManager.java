package gaia.cu9.ari.gaiaorbit.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.net.HttpParametersUtils;
import com.badlogic.gdx.net.HttpStatus;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * Manages a master instance which makes available state information to others
 * in order to synchronize a session.
 * 
 * @author tsagrista
 *
 */
public class MasterManager implements IObserver {
	private static final String URL = "http://localhost:8080/api/";

	public static MasterManager instance;

	public static void initialize() {
		if (GlobalConf.program.NET_MASTER)
			MasterManager.instance = new MasterManager();
	}

	private Map<String, String> camStateTimeParams, camStateParams, params;
	private HttpRequest request, evtrequest;
	private MasterResponseListener responseListener;

	public MasterManager() {
		super();
		camStateTimeParams = new HashMap<String, String>();
		camStateParams = new HashMap<String, String>();
		params = new HashMap<String, String>();

		request = new HttpRequest(HttpMethods.POST);
		evtrequest = new HttpRequest(HttpMethods.POST);
		responseListener = new MasterResponseListener();

		// Subscribe to events that need to be broadcasted
		EventManager.instance.subscribe(this, Events.FOV_CHANGED_CMD, Events.TOGGLE_VISIBILITY_CMD,
				Events.STAR_BRIGHTNESS_CMD, Events.STAR_MIN_OPACITY_CMD, Events.STAR_POINT_SIZE_CMD);
	}

	/**
	 * Broadcasts the given camera state and time to all the slaves
	 * 
	 * @param pos  Camera position
	 * @param dir  Camera direction
	 * @param up   Camera up
	 * @param time Current time
	 */
	public void boardcastCameraAndTime(Vector3d pos, Vector3d dir, Vector3d up, ITimeFrameProvider time) {
		camStateTimeParams.put("arg0", Arrays.toString(pos.values()));
		camStateTimeParams.put("arg1", Arrays.toString(dir.values()));
		camStateTimeParams.put("arg2", Arrays.toString(up.values()));
		camStateTimeParams.put("arg3", Long.toString(time.getTime().toEpochMilli()));
		String paramString = HttpParametersUtils.convertHttpParameters(camStateTimeParams);

		request.setUrl(URL + "setCameraStateAndTime");
		request.setContent(paramString);
		Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
			@Override
			public void handleHttpResponse(HttpResponse httpResponse) {
				if (httpResponse.getStatus().getStatusCode() == HttpStatus.SC_OK) {
				} else {
					Logger.error("Ko");
				}
			}

			@Override
			public void failed(Throwable t) {
				Logger.error(t);
			}

			@Override
			public void cancelled() {
				Logger.info("Cancelled");
			}
		});
	}

	/**
	 * Broadcasts the given camera state to all the slaves
	 * 
	 * @param pos Camera position
	 * @param dir Camera direction
	 * @param up  Camera up
	 */
	public void boardcastCamera(Vector3d pos, Vector3d dir, Vector3d up) {
		camStateParams.put("arg0", Arrays.toString(pos.values()));
		camStateParams.put("arg1", Arrays.toString(dir.values()));
		camStateParams.put("arg2", Arrays.toString(up.values()));
		String paramString = HttpParametersUtils.convertHttpParameters(camStateParams);

		request.setUrl(URL + "setCameraState");
		request.setContent(paramString);
		Gdx.net.sendHttpRequest(request, responseListener);
	}

	@Override
	public void notify(Events event, Object... data) {
		params.clear();
		switch (event) {
		case FOV_CHANGED_CMD:
			params.put("arg0", Float.toString((float) data[0]));
			String paramString = HttpParametersUtils.convertHttpParameters(params);
			evtrequest.setUrl(URL + "setFov");
			evtrequest.setContent(paramString);
			Gdx.net.sendHttpRequest(evtrequest, responseListener);
			break;
		case TOGGLE_VISIBILITY_CMD:
			String key = (String) data[0];
			Boolean state = null;
			if (data.length > 2) {
				state = (Boolean) data[2];
			} else {
				ComponentType ct = ComponentType.getFromKey(key);
				state = GlobalConf.scene.VISIBILITY[ct.ordinal()];
			}

			params.put("arg0", key);
			params.put("arg1", state.toString());
			paramString = HttpParametersUtils.convertHttpParameters(params);
			evtrequest.setUrl(URL + "setVisibility");
			evtrequest.setContent(paramString);
			Gdx.net.sendHttpRequest(evtrequest, responseListener);
			break;
		case STAR_BRIGHTNESS_CMD:
			float brightness = MathUtilsd.lint((float) data[0], Constants.MIN_STAR_BRIGHT, Constants.MAX_STAR_BRIGHT,
					Constants.MIN_SLIDER, Constants.MAX_SLIDER);
			params.put("arg0", Float.toString(brightness));
			paramString = HttpParametersUtils.convertHttpParameters(params);
			evtrequest.setUrl(URL + "setStarBrightness");
			evtrequest.setContent(paramString);
			Gdx.net.sendHttpRequest(evtrequest, responseListener);
			break;
		case STAR_POINT_SIZE_CMD:
			float size = MathUtilsd.lint((float) data[0], Constants.MIN_STAR_POINT_SIZE, Constants.MAX_STAR_POINT_SIZE,
					Constants.MIN_SLIDER, Constants.MAX_SLIDER);
			params.put("arg0", Float.toString(size));
			paramString = HttpParametersUtils.convertHttpParameters(params);
			evtrequest.setUrl(URL + "setStarSize");
			evtrequest.setContent(paramString);
			Gdx.net.sendHttpRequest(evtrequest, responseListener);
			break;
		case STAR_MIN_OPACITY_CMD:
                float opacity = MathUtilsd.lint((float)data[0], Constants.MIN_STAR_MIN_OPACITY, Constants.MAX_STAR_MIN_OPACITY, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
			params.put("arg0", Float.toString(opacity));
			paramString = HttpParametersUtils.convertHttpParameters(params);
			evtrequest.setUrl(URL + "setStarMinOpacity");
			evtrequest.setContent(paramString);
			Gdx.net.sendHttpRequest(evtrequest, responseListener);
			break;
		default:
			break;
		}
	}

	private class MasterResponseListener implements HttpResponseListener {
		@Override
		public void handleHttpResponse(HttpResponse httpResponse) {
			if (httpResponse.getStatus().getStatusCode() == HttpStatus.SC_OK) {
			} else {
				Logger.error("Ko");
			}
		}

		@Override
		public void failed(Throwable t) {
			Logger.error(t);
		}

		@Override
		public void cancelled() {
			Logger.info("Cancelled");
		}
	}
}
