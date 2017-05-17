package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.format.INumberFormat;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;

public class DebugInterface extends Table implements IObserver, IGuiInterface {
    private OwnLabel debug1, debug2, debug3, debug4, fps;
    /** Lock object for synchronization **/
    private Object lock;

    private INumberFormat fpsFormatter, memFormatter, timeFormatter;

    public DebugInterface(Skin skin, Object lock) {
        super(skin);
        // Formatters
        fpsFormatter = NumberFormatFactory.getFormatter("###.00");
        memFormatter = NumberFormatFactory.getFormatter("#000.00");
        timeFormatter = NumberFormatFactory.getFormatter("00");

        fps = new OwnLabel("", skin, "hud-med");
        add(fps).right();
        row();

        debug1 = new OwnLabel("", skin, "hud-med");
        add(debug1).right();
        row();

        debug2 = new OwnLabel("", skin, "hud-med");
        add(debug2).right();
        row();

        debug3 = new OwnLabel("", skin, "hud-med");
        add(debug3).right();
        row();

        debug4 = new OwnLabel("", skin, "hud-med");
        add(debug4).right();
        row();

        this.setVisible(GlobalConf.program.SHOW_DEBUG_INFO);
        this.lock = lock;
        EventManager.instance.subscribe(this, Events.DEBUG1, Events.DEBUG2, Events.DEBUG3, Events.DEBUG4, Events.FPS_INFO, Events.SHOW_DEBUG_CMD);
    }

    private void unsubscribe() {
        EventManager.instance.unsubscribe(this, Events.DEBUG1, Events.DEBUG2, Events.DEBUG3, Events.DEBUG4, Events.FPS_INFO, Events.SHOW_DEBUG_CMD);
    }

    @Override
    public void notify(Events event, Object... data) {
        synchronized (lock) {
            switch (event) {
            case DEBUG1:
                if (GlobalConf.program.SHOW_DEBUG_INFO && data.length > 0 && data[0] != null) {
                    // Double with run time
                    Double runTime = (Double) data[0];
                    debug1.setText("Run time: " + getRunTimeString(runTime));
                }
                break;

            case DEBUG2:
                if (GlobalConf.program.SHOW_DEBUG_INFO && data.length > 0 && data[0] != null) {
                    // Doubles (MB):
                    // used/free/total/max
                    Double used = (Double) data[0];
                    Double free = (Double) data[1];
                    Double total = (Double) data[2];
                    Double max = (Double) data[3];
                    debug2.setText("Mem[MB] - used: " + memFormatter.format(used) + "  free: " + memFormatter.format(free) + "  total: " + memFormatter.format(total) + "  max: " + memFormatter.format(max));
                }

                break;

            case DEBUG3:
                if (GlobalConf.program.SHOW_DEBUG_INFO && data.length > 0 && data[0] != null)
                    debug3.setText((String) data[0]);
                break;
            case DEBUG4:
                if (GlobalConf.program.SHOW_DEBUG_INFO && data.length > 0 && data[0] != null)
                    debug4.setText((String) data[0]);
                break;
            case FPS_INFO:
                if (GlobalConf.program.SHOW_DEBUG_INFO && data.length > 0 && data[0] != null)
                    fps.setText(fpsFormatter.format((Float) data[0]).concat(" FPS"));
                break;
            case SHOW_DEBUG_CMD:
                boolean shw;
                if (data.length >= 1) {
                    shw = (boolean) data[0];
                } else {
                    shw = !this.isVisible();
                }
                GlobalConf.program.SHOW_DEBUG_INFO = shw;
                this.setVisible(GlobalConf.program.SHOW_DEBUG_INFO);
                break;
            default:
                break;
            }
        }
    }

    private String getRunTimeString(Double seconds) {
        double hours = seconds / 3600d;
        double minutes = (seconds % 3600d) / 60d;
        double secs = seconds % 60d;

        return timeFormatter.format(hours) + ":" + timeFormatter.format(minutes) + ":" + timeFormatter.format(secs);
    }

    @Override
    public void dispose() {
        unsubscribe();
    }

}
