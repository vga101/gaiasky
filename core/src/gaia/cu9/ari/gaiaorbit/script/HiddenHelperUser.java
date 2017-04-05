package gaia.cu9.ari.gaiaorbit.script;

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

    private Array<Thread> currentThreads;

    private HiddenHelperUser() {
        super();
        currentThreads = new Array<Thread>(5);
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

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    EventScriptingInterface.instance().goToObject(name, 20, 1);
                    currentThreads.removeValue(Thread.currentThread(), true);
                }
            });
            t.start();
            currentThreads.add(t);
            break;
        case INPUT_EVENT:
            // Stop all current threads
            for (Thread th : currentThreads) {
                th.stop();
            }
            currentThreads.clear();

            break;
        }

    }

}
