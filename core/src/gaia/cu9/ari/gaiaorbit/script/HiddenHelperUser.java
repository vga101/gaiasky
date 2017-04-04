package gaia.cu9.ari.gaiaorbit.script;

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

    private HiddenHelperUser() {
        super();
        EventManager.instance.subscribe(this, Events.NAVIGATE_TO_OBJECT);
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
            final double distance = body.getRadius() * 10 * Constants.U_TO_KM;

            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    EventScriptingInterface.instance().goToObject(name, distance, 1);
                }
            });
            t.start();
            break;
        }

    }

}
