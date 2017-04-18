package gaia.cu9.ari.gaiaorbit.util.time;

import java.util.Date;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.Constants;

/**
 * Keeps pace of the simulation time vs real time and holds the global clock
 * @author Toni Sagrista
 *
 */
public class GlobalClock implements IObserver, ITimeFrameProvider {
    private static final double MS_TO_HOUR = 1 / 3600000d;

    /**The current time of the clock **/
    public Date time, lastTime;
    /** The hour difference from the last frame **/
    public double hdiff;

    /** Represents the time wrap multiplier. Scales the real time pace **/
    public double timeWarp = 1;
    // Seconds since last event POST
    private float lastUpdate = 1;
    /** The fixed frame rate when not in real time. Set negative to use real time **/
    public float fps = -1;

    /**
     * Creates a new GlobalClock
     * @param timeWrap The time wrap multiplier
     * @param date The date with which to initialise the clock
     */
    public GlobalClock(double timeWrap, Date date) {
        super();
        // Now
        this.timeWarp = timeWrap;
        hdiff = 0d;
        time = date;
        lastTime = new Date(time.getTime());
        EventManager.instance.subscribe(this, Events.PACE_CHANGE_CMD, Events.TIME_WARP_DECREASE_CMD, Events.TIME_WARP_INCREASE_CMD, Events.TIME_CHANGE_CMD);
    }

    double msacum = 0d;

    /** 
     * Update function
     * @param dt Delta time in seconds
     */
    public void update(double dt) {
        if (dt != 0) {
            // In case we are in constant rate mode
            if (fps > 0) {
                dt = 1 / fps;
            }

            int sign = (int) Math.signum(timeWarp);
            double h = Math.abs(dt * timeWarp * Constants.S_TO_H);
            hdiff = h * sign;

            double ms = sign * h * Constants.H_TO_MS;

            long currentTime = time.getTime();
            lastTime.setTime(currentTime);
            time.setTime(currentTime + (long) ms);

            // Post event each 1/2 second
            lastUpdate += dt;
            if (lastUpdate > .5) {
                EventManager.instance.post(Events.TIME_CHANGE_INFO, time);
                lastUpdate = 0;
            }
        } else if (time.getTime() - lastTime.getTime() != 0) {
            hdiff = (time.getTime() - lastTime.getTime()) * MS_TO_HOUR;
            lastTime.setTime(time.getTime());
        } else {
            hdiff = 0d;
        }
    }

    @Override
    public Date getTime() {
        return time;
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case PACE_CHANGE_CMD:
            // Update pace
            this.timeWarp = (Double) data[0];
            EventManager.instance.post(Events.PACE_CHANGED_INFO, this.timeWarp);
            break;
        case TIME_WARP_INCREASE_CMD:
            if (timeWarp == 0) {
                timeWarp = 0.125;
            } else if (timeWarp == -0.125) {
                timeWarp = 0;
            } else if (timeWarp < 0) {
                timeWarp /= 2.0;
            } else {
                timeWarp *= 2.0;
            }
            EventManager.instance.post(Events.PACE_CHANGED_INFO, this.timeWarp);
            break;
        case TIME_WARP_DECREASE_CMD:
            if (timeWarp == 0.125) {
                timeWarp = 0;
            } else if (timeWarp == 0) {
                timeWarp = -0.125;
            } else if (timeWarp < 0) {
                timeWarp *= 2.0;
            } else {
                timeWarp /= 2.0;
            }
            EventManager.instance.post(Events.PACE_CHANGED_INFO, this.timeWarp);
            break;
        case TIME_CHANGE_CMD:
            // Update time
            long newt = ((Date) data[0]).getTime();
            this.time.setTime(newt);
            break;
        default:
            break;
        }

    }

    /**
     * Provides the time difference in hours
     */
    @Override
    public double getDt() {
        return hdiff;
    }

    @Override
    public double getWarpFactor() {
        return timeWarp;
    }

    public boolean isFixedRateMode() {
        return fps > 0;
    }

    @Override
    public float getFixedRate() {
        return fps;
    }

}
