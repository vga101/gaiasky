package gaia.cu9.ari.gaiaorbit.interfce.components;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.ControlsWindow;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextIconButton;

public class VisibilityComponent extends GuiComponent implements IObserver {
    protected Map<String, Button> buttonMap;
    /**
     * Entities that will go in the visibility check boxes
     */
    private ComponentType[] visibilityEntities;
    private boolean[] visible;
    private CheckBox properMotions;
    private Slider pmNumFactorSlider, pmLenFactorSlider;
    private Label pmNumFactor, pmLenFactor, pmNumFactorLabel, pmLenFactorLabel;
    private VerticalGroup pmNumFactorGroup, pmLenFactorGroup;
    private VerticalGroup visGroup;
    private ControlsWindow window;

    public VisibilityComponent(Skin skin, Stage stage, ControlsWindow window) {
        super(skin, stage);
        this.window = window;
        EventManager.instance.subscribe(this, Events.TOGGLE_VISIBILITY_CMD);
    }

    public void setVisibilityEntitites(ComponentType[] ve, boolean[] v) {
        visibilityEntities = ve;
        visible = v;
    }

    public void initialize() {

        final Table visibilityTable = new Table(skin);
        visibilityTable.setName("visibility table");
        buttonMap = new HashMap<String, Button>();
        Set<Button> buttons = new HashSet<Button>();
        if (visibilityEntities != null) {
            for (int i = 0; i < visibilityEntities.length; i++) {
                final ComponentType ct = visibilityEntities[i];
                final String name = ct.getName();

                Button button = null;
                if (ct.style != null) {
                    Image icon = new Image(skin.getDrawable(ct.style));
                    button = new OwnTextIconButton(name, icon, skin, "toggle");
                } else {
                    button = new OwnTextButton(name, skin, "toggle");
                }
                button.setName(name);

                buttonMap.put(name, button);
                if (!ct.toString().equals(name)) {
                    buttonMap.put(ct.toString(), button);
                }

                button.setChecked(visible[i]);
                button.addListener(new EventListener() {
                    @Override
                    public boolean handle(Event event) {
                        if (event instanceof ChangeEvent) {
                            EventManager.instance.post(Events.TOGGLE_VISIBILITY_CMD, name, true, ((Button) event.getListenerActor()).isChecked());
                            return true;
                        }
                        return false;
                    }
                });
                visibilityTable.add(button).pad(1).align(Align.center);
                if (i % 2 != 0) {
                    visibilityTable.row();
                }
                buttons.add(button);
            }
        }

        /** Proper motions **/
        float space3 = 3 * GlobalConf.SCALE_FACTOR;

        // NUM FACTOR
        pmNumFactorLabel = new Label(txt("gui.pmnumfactor"), skin, "default");
        pmNumFactor = new OwnLabel(Integer.toString((int) (MathUtilsd.lint(GlobalConf.scene.PM_NUM_FACTOR, Constants.MIN_PM_NUM_FACTOR, Constants.MAX_PM_NUM_FACTOR, Constants.MIN_SLIDER_1, Constants.MAX_SLIDER))), skin);

        pmNumFactorSlider = new Slider(Constants.MIN_SLIDER_1, Constants.MAX_SLIDER, 1, false, skin);
        pmNumFactorSlider.setName("proper motion vectors number factor");
        pmNumFactorSlider.setValue(MathUtilsd.lint(GlobalConf.scene.PM_NUM_FACTOR, Constants.MIN_PM_NUM_FACTOR, Constants.MAX_PM_NUM_FACTOR, Constants.MIN_SLIDER_1, Constants.MAX_SLIDER));
        pmNumFactorSlider.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.PM_NUM_FACTOR_CMD, MathUtilsd.lint(pmNumFactorSlider.getValue(), Constants.MIN_SLIDER_1, Constants.MAX_SLIDER, Constants.MIN_PM_NUM_FACTOR, Constants.MAX_PM_NUM_FACTOR));
                    pmNumFactor.setText(Integer.toString((int) pmNumFactorSlider.getValue()));
                    return true;
                }
                return false;
            }
        });

        pmNumFactorGroup = new VerticalGroup();
        pmNumFactorGroup.align(Align.left);
        HorizontalGroup pnfg = new HorizontalGroup();
        pnfg.space(space3);
        pnfg.addActor(pmNumFactorSlider);
        pnfg.addActor(pmNumFactor);
        pmNumFactorGroup.addActor(pmNumFactorLabel);
        pmNumFactorGroup.addActor(pnfg);

        // LEN FACTOR
        pmLenFactorLabel = new Label(txt("gui.pmlenfactor"), skin, "default");
        pmLenFactor = new OwnLabel(Integer.toString(Math.round(GlobalConf.scene.PM_LEN_FACTOR)), skin);

        pmLenFactorSlider = new Slider(Constants.MIN_PM_LEN_FACTOR, Constants.MAX_PM_LEN_FACTOR, 0.5f, false, skin);
        pmLenFactorSlider.setName("proper motion vectors number factor");
        pmLenFactorSlider.setValue(GlobalConf.scene.PM_LEN_FACTOR);
        pmLenFactorSlider.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.PM_LEN_FACTOR_CMD, pmLenFactorSlider.getValue());
                    pmLenFactor.setText(Integer.toString(Math.round(pmLenFactorSlider.getValue())));
                    return true;
                }
                return false;
            }
        });
        pmLenFactorGroup = new VerticalGroup();
        pmLenFactorGroup.align(Align.left);
        HorizontalGroup plfg = new HorizontalGroup();
        plfg.space(space3);
        plfg.addActor(pmLenFactorSlider);
        plfg.addActor(pmLenFactor);
        pmLenFactorGroup.addActor(pmLenFactorLabel);
        pmLenFactorGroup.addActor(plfg);

        // PM CHECKBOX
        properMotions = new CheckBox(" " + txt("gui.checkbox.propermotionvectors"), skin);
        properMotions.setName("pm vectors");
        properMotions.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.PROPER_MOTIONS_CMD, "Proper motions", properMotions.isChecked());
                    if (visGroup != null) {
                        if (properMotions.isChecked()) {
                            visGroup.addActor(pmNumFactorGroup);
                            visGroup.addActor(pmLenFactorGroup);
                        } else {
                            visGroup.removeActor(pmNumFactorGroup);
                            visGroup.removeActor(pmLenFactorGroup);
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        // Set button width to max width
        visibilityTable.pack();
        float maxw = 0f;
        for (Button b : buttons) {
            if (b.getWidth() > maxw) {
                maxw = b.getWidth();
            }
        }
        for (Button b : buttons) {
            b.setSize(maxw, 20 * GlobalConf.SCALE_FACTOR);
        }
        visibilityTable.pack();

        visGroup = new VerticalGroup().align(Align.left);
        visGroup.addActor(visibilityTable);
        visGroup.addActor(properMotions);

        properMotions.setChecked(GlobalConf.scene.PROPER_MOTION_VECTORS);

        component = visGroup;
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case TOGGLE_VISIBILITY_CMD:
            boolean interf = (Boolean) data[1];
            if (!interf) {
                String name = (String) data[0];
                Button b = buttonMap.get(name.toLowerCase());

                b.setProgrammaticChangeEvents(false);
                if (b != null) {
                    if (data.length == 3) {
                        b.setChecked((Boolean) data[2]);
                    } else {
                        b.setChecked(!b.isChecked());
                    }
                }
                b.setProgrammaticChangeEvents(true);
            }
            break;
        }

    }

}
