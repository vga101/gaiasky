package gaia.cu9.ari.gaiaorbit.script;

import java.util.concurrent.atomic.AtomicBoolean;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;

/**
 * This guy implements high level operations which run concurrently to the main
 * thread by starting new threads
 * 
 * @author tsagrista
 *
 */
public class HiddenHelperUser implements IObserver {

    private static HiddenHelperUser hhu;

    public static HiddenHelperUser instance() {
	if (hhu == null)
	    hhu = new HiddenHelperUser();
	return hhu;
    }

    public static void initialise() {
	instance();
    }

    private Array<HelperTask> currentTasks;

    private long lastCommandTime;

    private HiddenHelperUser() {
	super();
	currentTasks = new Array<HelperTask>(5);
	lastCommandTime = -1;
	EventManager.instance.subscribe(this, Events.NAVIGATE_TO_OBJECT, Events.LAND_ON_OBJECT,
		Events.LAND_AT_LOCATION_OF_OBJECT, Events.INPUT_EVENT);
    }

    @Override
    public void notify(Events event, Object... data) {
	switch (event) {
	case NAVIGATE_TO_OBJECT:
	    CelestialBody body;
	    if (data[0] instanceof String)
		body = GaiaSky.instance.sg.findFocus((String) data[0]);
	    else
		body = ((CelestialBody) data[0]);

	    String name = body.getName();
	    // final double angle = body.getRadius() * 10 * Constants.U_TO_KM;

	    GoToObjectTask gotoTask = new GoToObjectTask(name, currentTasks);
	    Thread gotoT = new Thread(gotoTask);
	    gotoT.start();
	    currentTasks.add(gotoTask);
	    lastCommandTime = TimeUtils.millis();
	    break;
	case LAND_ON_OBJECT:
	    if (data[0] instanceof String)
		body = GaiaSky.instance.sg.findFocus((String) data[0]);
	    else
		body = ((CelestialBody) data[0]);

	    name = body.getName();

	    LandOnObjectTask landonTask = new LandOnObjectTask(name, currentTasks);
	    Thread landonT = new Thread(landonTask);
	    landonT.start();
	    currentTasks.add(landonTask);
	    lastCommandTime = TimeUtils.millis();

	    break;
	case LAND_AT_LOCATION_OF_OBJECT:

	    break;
	case INPUT_EVENT:
	    // More than one second after the command is given to be able to
	    // stop
	    if (TimeUtils.millis() - lastCommandTime > 1000) {

		// Stop all current threads
		for (HelperTask tsk : currentTasks) {
		    tsk.stop();
		}
		currentTasks.clear();
	    }
	    break;
	default:
	    break;
	}

    }

    private abstract class HelperTask implements Runnable {
	protected AtomicBoolean stop;
	protected Array<HelperTask> currentTasks;

	protected HelperTask(Array<HelperTask> currentTasks) {
	    this.stop = new AtomicBoolean(false);
	    this.currentTasks = currentTasks;
	}

	public void stop() {
	    this.stop.set(true);
	}
    }

    private class GoToObjectTask extends HelperTask {
	String name;

	public GoToObjectTask(String name, Array<HelperTask> currentTasks) {
	    super(currentTasks);
	    this.name = name;

	}

	@Override
	public void run() {
	    EventScriptingInterface.instance().goToObject(name, 20, 1, stop);
	    currentTasks.removeValue(this, true);
	}

    }

    private class LandOnObjectTask extends HelperTask {
	String name;

	public LandOnObjectTask(String name, Array<HelperTask> currentTasks) {
	    super(currentTasks);
	    this.name = name;

	}

	@Override
	public void run() {
	    EventScriptingInterface.instance().landOnObject(name, stop);
	    currentTasks.removeValue(this, true);
	}

    }

}
