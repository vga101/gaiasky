package gaia.cu9.ari.gaiaorbit.script;

import java.util.concurrent.atomic.AtomicBoolean;

import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.util.Constants;

/**
 * This guy implements high level operations which run concurrently to the main thread by 
 * starting new threads
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

    private HiddenHelperUser() {
        super();
        currentTasks = new Array<HelperTask>(5);
        EventManager.instance.subscribe(this, Events.NAVIGATE_TO_OBJECT, Events.INPUT_EVENT);
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

            final String name = body.getName();
            final double angle = body.getRadius() * 10 * Constants.U_TO_KM;

            GoToObjectTask task = new GoToObjectTask(name, currentTasks);
            Thread t = new Thread(task);
            t.start();
            currentTasks.add(task);
            break;
        case INPUT_EVENT:
            // Stop all current threads
            for (HelperTask tsk : currentTasks) {
                tsk.stop();
            }
            currentTasks.clear();

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

}
