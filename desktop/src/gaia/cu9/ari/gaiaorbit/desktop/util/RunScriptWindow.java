package gaia.cu9.ari.gaiaorbit.desktop.util;

import org.python.core.PyCode;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.scene2d.CollapsibleWindow;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;

/**
 * The run script window, which allows the user to choose a script to run.
 * @author tsagrista
 *
 */
public class RunScriptWindow extends CollapsibleWindow {

    final private Stage stage;
    private RunScriptWindow me;
    private Table table;

    private PyCode code;

    public RunScriptWindow(Stage stg, Skin skin) {
        super(txt("gui.script.title"), skin);

        this.stage = stg;
        this.me = this;

        float pad = 5 * GlobalConf.SCALE_FACTOR;

        /** BUTTONS **/
        HorizontalGroup buttonGroup = new HorizontalGroup();
        TextButton cancel = new OwnTextButton(txt("gui.cancel"), skin, "default");
        cancel.setName("cancel");
        cancel.setSize(70 * GlobalConf.SCALE_FACTOR, 20 * GlobalConf.SCALE_FACTOR);
        cancel.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    me.hide();
                    return true;
                }

                return false;
            }

        });
        TextButton run = new OwnTextButton(txt("gui.script.run"), skin, "default");
        run.setName("run");
        run.setSize(70 * GlobalConf.SCALE_FACTOR, 20 * GlobalConf.SCALE_FACTOR);
        run.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                boolean async = true;
                if (event instanceof ChangeEvent) {
                    me.hide();
                    if (code != null) {
                        EventManager.instance.post(Events.RUN_SCRIPT_PYCODE, code, GlobalConf.program.SCRIPT_LOCATION, async);
                    }
                    return true;
                }

                return false;
            }

        });
        buttonGroup.addActor(run);

        add(table).pad(pad);
        row();
        add(buttonGroup).pad(pad).bottom().right();
        getTitleTable().align(Align.left);

        pack();

        this.setPosition(Math.round(stage.getWidth() / 2f - this.getWidth() / 2f), Math.round(stage.getHeight() / 2f - this.getHeight() / 2f));
    }

    public void hide() {
        if (stage.getActors().contains(me, true))
            me.remove();
    }

    public void display() {
        if (!stage.getActors().contains(me, true))
            stage.addActor(this);
    }

    protected static String txt(String key) {
        return I18n.bundle.get(key);
    }

    protected static String txt(String key, Object... args) {
        return I18n.bundle.format(key, args);
    }
}
