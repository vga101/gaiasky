package gaia.cu9.ari.gaiaorbit.util.update;

import com.badlogic.gdx.scenes.scene2d.Event;

public class VersionCheckEvent extends Event {
    private Object result;
    private boolean failed = false;

    public VersionCheckEvent(boolean falied) {
        this.failed = falied;
    }

    public VersionCheckEvent(Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }

    public boolean isFailed() {
        return failed;
    }
}
