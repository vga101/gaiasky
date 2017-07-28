package gaia.cu9.ari.gaiaorbit.util.update;

import java.util.Date;

import com.badlogic.gdx.scenes.scene2d.Event;

public class VersionCheckEvent extends Event {
    private final String tag;
    private final Date tagTime;
    private final boolean failed;

    public VersionCheckEvent(boolean falied) {
        this.tag = null;
        this.tagTime = null;
        this.failed = falied;
    }

    public VersionCheckEvent(String tag, Date tagTime) {
        this.tag = tag;
        this.tagTime = tagTime;
        this.failed = false;
    }

    public String getTag() {
        return tag;
    }

    public Date getTagTime() {
        return tagTime;
    }

    public boolean isFailed() {
        return failed;
    }
}
