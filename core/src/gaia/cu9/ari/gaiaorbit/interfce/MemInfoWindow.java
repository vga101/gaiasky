package gaia.cu9.ari.gaiaorbit.interfce;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextArea;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;

import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.scene2d.CollapsibleWindow;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnScrollPane;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextArea;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;

public class MemInfoWindow extends CollapsibleWindow {
    private MemInfoWindow me;
    private Stage stage;

    private final OwnScrollPane meminfoscroll;

    public MemInfoWindow(Stage stg, Skin skin) {
        super(txt("gui.help.meminfo"), skin);

        this.stage = stg;
        this.me = this;

        float pad = 5 * GlobalConf.SCALE_FACTOR;
        float tawidth = 500 * GlobalConf.SCALE_FACTOR;

        String meminfostr = "";
        for (MemoryPoolMXBean mpBean : ManagementFactory.getMemoryPoolMXBeans()) {
            meminfostr += txt("gui.help.name") + ": " + mpBean.getName() + ": " + mpBean.getUsage() + "\n";
        }

        TextArea meminfo = new OwnTextArea(meminfostr, skin);
        meminfo.setDisabled(true);
        meminfo.setPrefRows(10);
        meminfo.setWidth(tawidth);

        meminfoscroll = new OwnScrollPane(meminfo, skin, "default-nobg");
        meminfoscroll.setWidth(tawidth);
        meminfoscroll.setForceScroll(false, true);
        meminfoscroll.setSmoothScrolling(true);
        meminfoscroll.setFadeScrollBars(false);

        add(meminfoscroll).align(Align.center).pad(pad);
        row();

        /** BUTTONS **/
        HorizontalGroup buttonGroup = new HorizontalGroup();
        TextButton close = new OwnTextButton(txt("gui.close"), skin, "default");
        close.setName("close");
        close.setSize(70 * GlobalConf.SCALE_FACTOR, 20 * GlobalConf.SCALE_FACTOR);
        close.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    me.hide();
                    return true;
                }

                return false;
            }

        });
        buttonGroup.addActor(close);

        add(buttonGroup).pad(pad).bottom().right();
        getTitleTable().align(Align.left);

        pack();

        this.setPosition(Math.round(stage.getWidth() / 2f - this.getWidth() / 2f), Math.round(stage.getHeight() / 2f - this.getHeight() / 2f));

        /** CAPTURE SCROLL FOCUS **/
        stage.addListener(new EventListener() {

            @Override
            public boolean handle(Event event) {
                if (event instanceof InputEvent) {
                    InputEvent ie = (InputEvent) event;

                    if (ie.getType() == Type.mouseMoved) {

                        if (ie.getTarget().isDescendantOf(meminfoscroll)) {
                            stage.setScrollFocus(meminfoscroll);
                        }
                        return true;

                    }
                }
                return false;
            }
        });

    }

    protected static String txt(String key) {
        return I18n.bundle.get(key);
    }

    public void hide() {
        if (stage.getActors().contains(me, true))
            me.remove();
    }

    public void display() {
        if (!stage.getActors().contains(me, true))
            stage.addActor(this);
    }

    protected static String txt(String key, Object... args) {
        return I18n.bundle.format(key, args);
    }

}
