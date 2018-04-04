package gaia.cu9.ari.gaiaorbit.util;

import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;

public class NotificationsListener implements IObserver {
    public Array<String> logMessages;
    public NotificationsListener() {
        super();
        logMessages = new Array<String>();
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case POST_NOTIFICATION:
            String message = "";
            for (int i = 0; i < data.length; i++) {
                if (i == data.length - 1 && data[i] instanceof Boolean) {
                } else {
                    message += (String) data[i];
                    if (i < data.length - 1 && !(i == data.length - 2 && data[data.length - 1] instanceof Boolean)) {
                        message += " - ";
                    }
                }
            }
            logMessages.add(message);
            System.out.println(message);
            break;
        case JAVA_EXCEPTION:
            Exception e = (Exception) data[0];
            e.printStackTrace(System.err);
            logMessages.add(e.getMessage());
            break;
        default:
            break;
        }

    }
}
