package gaia.cu9.ari.gaiaorbit.interfce;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.ButtonGroup;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Stack;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Disableable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.beans.ComboBoxBean;
import gaia.cu9.ari.gaiaorbit.interfce.beans.LangComboBoxBean;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.scene2d.CollapsibleWindow;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnCheckBox;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnScrollPane;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnSelectBox;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextField;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextIconButton;
import gaia.cu9.ari.gaiaorbit.util.validator.IntValidator;

public class PreferencesWindow extends CollapsibleWindow implements IObserver {

    final private Stage stage;
    final private Skin skin;
    private PreferencesWindow me;
    private Table table;

    private LabelStyle linkStyle;

    private List<OwnScrollPane> scrolls;
    private List<Actor> textareas;

    private IntValidator widthValidator, heightValidator;

    public PreferencesWindow(Stage stage, Skin skin) {
        super(txt("gui.settings") + " - v" + GlobalConf.version.version + " - " + txt("gui.build", GlobalConf.version.build), skin);
        this.stage = stage;
        this.skin = skin;
        this.me = this;
        this.linkStyle = skin.get("link", LabelStyle.class);

        this.scrolls = new ArrayList<OwnScrollPane>(5);
        this.textareas = new ArrayList<Actor>();

        // Build UI
        build();

        // Position
        this.setPosition(Math.round(stage.getWidth() / 2f - this.getWidth() / 2f), Math.round(stage.getHeight() / 2f - this.getHeight() / 2f));
    }

    public void build() {
        float contentw = 700 * GlobalConf.SCALE_FACTOR;
        float contenth = 700 * GlobalConf.SCALE_FACTOR;
        float tabwidth = 180 * GlobalConf.SCALE_FACTOR;
        float textwidth = 65 * GlobalConf.SCALE_FACTOR;
        float pad = 5 * GlobalConf.SCALE_FACTOR;

        /** TABLE and SCROLL **/
        table = new Table(skin);

        // Create the tab buttons
        VerticalGroup group = new VerticalGroup();
        group.align(Align.left);

        final Button tabGraphics = new OwnTextIconButton(txt("gui.graphicssettings"), new Image(skin.getDrawable("icon-p-graphics")), skin, "toggle-big");
        tabGraphics.pad(pad);
        tabGraphics.setWidth(tabwidth);
        final Button tabUI = new OwnTextIconButton(txt("gui.ui.interfacesettings"), new Image(skin.getDrawable("icon-p-interface")), skin, "toggle-big");
        tabUI.pad(pad);
        tabUI.setWidth(tabwidth);
        final Button tabPerformance = new OwnTextIconButton(txt("gui.multithreading"), new Image(skin.getDrawable("icon-p-performance")), skin, "toggle-big");
        tabPerformance.pad(pad);
        tabPerformance.setWidth(tabwidth);
        final Button tabControls = new OwnTextIconButton(txt("gui.controls"), new Image(skin.getDrawable("icon-p-controls")), skin, "toggle-big");
        tabControls.pad(pad);
        tabControls.setWidth(tabwidth);
        final Button tabScreenshots = new OwnTextIconButton(txt("gui.screenshots"), new Image(skin.getDrawable("icon-p-screenshots")), skin, "toggle-big");
        tabScreenshots.pad(pad);
        tabScreenshots.setWidth(tabwidth);
        final Button tabFrames = new OwnTextIconButton(txt("gui.frameoutput.title"), new Image(skin.getDrawable("icon-p-frameoutput")), skin, "toggle-big");
        tabFrames.pad(pad);
        tabFrames.setWidth(tabwidth);
        final Button tabCamera = new OwnTextIconButton(txt("gui.camerarec.title"), new Image(skin.getDrawable("icon-p-camera")), skin, "toggle-big");
        tabCamera.pad(pad);
        tabCamera.setWidth(tabwidth);
        final Button tab360 = new OwnTextIconButton(txt("gui.360.title"), new Image(skin.getDrawable("icon-p-360")), skin, "toggle-big");
        tab360.pad(pad);
        tab360.setWidth(tabwidth);
        final Button tabData = new OwnTextIconButton(txt("gui.data"), new Image(skin.getDrawable("icon-p-data")), skin, "toggle-big");
        tabData.pad(pad);
        tabData.setWidth(tabwidth);
        final Button tabGaia = new OwnTextIconButton(txt("gui.gaia"), new Image(skin.getDrawable("icon-p-gaia")), skin, "toggle-big");
        tabGaia.pad(pad);
        tabGaia.setWidth(tabwidth);

        group.addActor(tabGraphics);
        group.addActor(tabUI);
        group.addActor(tabPerformance);
        group.addActor(tabControls);
        group.addActor(tabScreenshots);
        group.addActor(tabFrames);
        group.addActor(tabCamera);
        group.addActor(tab360);
        group.addActor(tabData);
        group.addActor(tabGaia);
        table.add(group).align(Align.left).padLeft(pad);

        // Create the tab content. Just using images here for simplicity.
        Stack content = new Stack();
        content.setSize(contentw, contenth);

        /**
         *  ==== GRAPHICS ====
         **/
        final Table contentGraphics = new Table(skin);
        contentGraphics.align(Align.top);

        // RESOLUTION/MODE
        Label titleResolution = new OwnLabel(txt("gui.resolutionmode"), skin, "help-title");
        Table mode = new Table();

        // Full screen mode resolutions
        Array<DisplayMode> modes = new Array<DisplayMode>(Gdx.graphics.getDisplayModes());
        modes.sort(new Comparator<DisplayMode>() {

            @Override
            public int compare(DisplayMode o1, DisplayMode o2) {
                return Integer.compare(o2.height * o2.width, o1.height * o1.width);
            }
        });
        final OwnSelectBox<DisplayMode> fullscreenResolutions = new OwnSelectBox<DisplayMode>(skin);
        fullscreenResolutions.setWidth(textwidth * 3.3f);
        fullscreenResolutions.setItems(modes);

        DisplayMode selectedMode = null;
        for (DisplayMode dm : modes) {
            if (dm.width == GlobalConf.screen.FULLSCREEN_WIDTH && dm.height == GlobalConf.screen.FULLSCREEN_HEIGHT) {
                selectedMode = dm;
                break;
            }
        }
        if (selectedMode != null)
            fullscreenResolutions.setSelected(selectedMode);

        // Get current resolution
        Table windowedResolutions = new Table(skin);
        DisplayMode nativeMode = Gdx.graphics.getDisplayMode();
        widthValidator = new IntValidator(100, nativeMode.width);
        final OwnTextField widthField = new OwnTextField(Integer.toString(MathUtils.clamp(GlobalConf.screen.SCREEN_WIDTH, 100, nativeMode.width)), skin);
        widthField.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    String str = widthField.getText();
                    boolean correct = widthValidator.validate(str);
                    if (correct) {
                        widthField.setColor(Color.WHITE);
                    } else {
                        widthField.setColor(Color.RED);
                    }
                    return true;
                }
                return false;
            }
        });
        widthField.setWidth(textwidth);
        heightValidator = new IntValidator(100, nativeMode.height);
        final OwnTextField heightField = new OwnTextField(Integer.toString(MathUtils.clamp(GlobalConf.screen.SCREEN_HEIGHT, 100, nativeMode.height)), skin);
        heightField.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    String str = heightField.getText();
                    boolean correct = heightValidator.validate(str);
                    if (correct) {
                        heightField.setColor(Color.WHITE);
                    } else {
                        heightField.setColor(Color.RED);
                    }
                    return true;
                }
                return false;
            }
        });
        heightField.setWidth(textwidth);
        final OwnCheckBox resizable = new OwnCheckBox(txt("gui.resizable"), skin, "default", pad);
        resizable.setChecked(GlobalConf.screen.RESIZABLE);
        final OwnLabel widthLabel = new OwnLabel(txt("gui.width") + ":", skin);
        final OwnLabel heightLabel = new OwnLabel(txt("gui.height") + ":", skin);

        windowedResolutions.add(widthLabel).left().padRight(pad);
        windowedResolutions.add(widthField).left().padRight(pad);
        windowedResolutions.add(heightLabel).left().padRight(pad);
        windowedResolutions.add(heightField).left().row();
        windowedResolutions.add(resizable).left().colspan(4);

        // Radio buttons
        final OwnCheckBox fullscreen = new OwnCheckBox(txt("gui.fullscreen"), skin, "radio", pad);
        fullscreen.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    GlobalConf.screen.FULLSCREEN = fullscreen.isChecked();
                    selectFullscreen(fullscreen.isChecked(), widthField, heightField, fullscreenResolutions, resizable, widthLabel, heightLabel);
                    return true;
                }
                return false;
            }
        });
        fullscreen.setChecked(GlobalConf.screen.FULLSCREEN);

        final OwnCheckBox windowed = new OwnCheckBox(txt("gui.windowed"), skin, "radio", pad);
        windowed.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    GlobalConf.screen.FULLSCREEN = !windowed.isChecked();
                    selectFullscreen(!windowed.isChecked(), widthField, heightField, fullscreenResolutions, resizable, widthLabel, heightLabel);
                    return true;
                }
                return false;
            }
        });
        windowed.setChecked(!GlobalConf.screen.FULLSCREEN);
        selectFullscreen(GlobalConf.screen.FULLSCREEN, widthField, heightField, fullscreenResolutions, resizable, widthLabel, heightLabel);

        new ButtonGroup<CheckBox>(fullscreen, windowed);

        mode.add(fullscreen).left().padRight(pad * 2);
        mode.add(fullscreenResolutions).left().row();
        mode.add(windowed).left().padRight(pad * 2).padTop(pad * 2);
        mode.add(windowedResolutions).left().padTop(pad * 2);

        // Add to content
        contentGraphics.add(titleResolution).left().padBottom(pad * 2).row();
        contentGraphics.add(mode).left().padBottom(pad * 4).row();

        // GRAPHICS SETTINGS
        Label titleGraphics = new OwnLabel(txt("gui.graphicssettings"), skin, "help-title");
        Table graphics = new Table();

        OwnLabel gqualityLabel = new OwnLabel(txt("gui.gquality"), skin);
        gqualityLabel.addListener(new TextTooltip(txt("gui.gquality.info"), skin));

        ComboBoxBean[] gqs = new ComboBoxBean[] { new ComboBoxBean(txt("gui.gquality.high"), 0), new ComboBoxBean(txt("gui.gquality.normal"), 1), new ComboBoxBean(txt("gui.gquality.low"), 2) };
        OwnSelectBox<ComboBoxBean> gquality = new OwnSelectBox<ComboBoxBean>(skin);
        gquality.setItems(gqs);
        gquality.setWidth(textwidth * 3f);
        int index = -1;
        for (int i = 0; i < GlobalConf.data.OBJECTS_JSON_FILE_GQ.length; i++) {
            if (GlobalConf.data.OBJECTS_JSON_FILE_GQ[i].equals(GlobalConf.data.OBJECTS_JSON_FILE)) {
                index = i;
                break;
            }
        }
        int gqidx = index;
        gquality.setSelected(gqs[gqidx]);

        // AA
        OwnLabel aaLabel = new OwnLabel(txt("gui.aa"), skin);
        aaLabel.addListener(new TextTooltip(txt("gui.aa.info"), skin));

        ComboBoxBean[] aas = new ComboBoxBean[] { new ComboBoxBean(txt("gui.aa.no"), 0), new ComboBoxBean(txt("gui.aa.fxaa"), -1), new ComboBoxBean(txt("gui.aa.nfaa"), -2) };
        OwnSelectBox<ComboBoxBean> msaa = new OwnSelectBox<ComboBoxBean>(skin);
        msaa.setItems(aas);
        msaa.setWidth(textwidth * 3f);
        msaa.setSelected(aas[idxAa(2, GlobalConf.postprocess.POSTPROCESS_ANTIALIAS)]);

        // LINE RENDERER
        ComboBoxBean[] lineRenderers = new ComboBoxBean[] { new ComboBoxBean(txt("gui.linerenderer.normal"), 0), new ComboBoxBean(txt("gui.linerenderer.quad"), 1) };
        OwnSelectBox<ComboBoxBean> lineRenderer = new OwnSelectBox<ComboBoxBean>(skin);
        lineRenderer.setItems(lineRenderers);
        lineRenderer.setWidth(textwidth * 3f);
        lineRenderer.setSelected(lineRenderers[GlobalConf.scene.LINE_RENDERER]);

        // VSYNC
        OwnCheckBox vsync = new OwnCheckBox(txt("gui.vsync"), skin, "default", pad);
        vsync.setChecked(GlobalConf.screen.VSYNC);

        graphics.add(gqualityLabel).left().padRight(pad * 4).padBottom(pad);
        graphics.add(gquality).left().padBottom(pad).row();
        graphics.add(aaLabel).left().padRight(pad * 4).padBottom(pad);
        graphics.add(msaa).left().padBottom(pad).row();
        graphics.add(new OwnLabel(txt("gui.linerenderer"), skin)).left().padRight(pad * 4).padBottom(pad);
        graphics.add(lineRenderer).left().padBottom(pad).row();
        graphics.add(vsync).left().colspan(2);

        // Add to content
        contentGraphics.add(titleGraphics).left().padBottom(pad * 2).row();
        contentGraphics.add(graphics).left();

        /**
         *  ==== UI ====
         **/
        final Table contentUI = new Table(skin);
        contentUI.align(Align.top);
        contentUI.add(new Label("UI here", skin));

        /**
         *  ==== PERFORMANCE ====
         **/
        final Table contentPerformance = new Table(skin);
        contentPerformance.align(Align.top);
        contentPerformance.add(new Label("performance here", skin));

        /**
         *  ==== CONTROLS ====
         **/
        final Table contentControls = new Table(skin);
        contentControls.align(Align.top);
        contentControls.add(new Label("controls here", skin));

        /**
         *  ==== SCREENSHOTS ====
         **/
        final Table contentScreenshots = new Table(skin);
        contentScreenshots.align(Align.top);
        contentScreenshots.add(new Label("screenshots here", skin));

        /**
         *  ==== FRAME OUTPUT ====
         **/
        final Table contentFrames = new Table(skin);
        contentFrames.align(Align.top);
        contentFrames.add(new Label("frames here", skin));

        /**
         *  ==== CAMERA ====
         **/
        final Table contentCamera = new Table(skin);
        contentCamera.align(Align.top);
        contentCamera.add(new Label("camera here", skin));

        /**
         *  ==== 360 ====
         **/
        final Table content360 = new Table(skin);
        content360.align(Align.top);
        content360.add(new Label("360 here", skin));

        /**
         *  ==== DATA ====
         **/
        final Table contentData = new Table(skin);
        contentData.align(Align.top);
        contentData.add(new Label("data here", skin));

        /**
         *  ==== GAIA ====
         **/
        final Table contentGaia = new Table(skin);
        contentGaia.align(Align.top);
        contentGaia.add(new Label("gaia here", skin));

        /** ADD ALL CONTENT **/
        content.addActor(contentGraphics);
        content.addActor(contentUI);
        content.addActor(contentPerformance);
        content.addActor(contentControls);
        content.addActor(contentScreenshots);
        content.addActor(contentFrames);
        content.addActor(contentCamera);
        content.addActor(content360);
        content.addActor(contentData);
        content.addActor(contentGaia);

        table.add(content).padLeft(10).expand().fill();

        // Listen to changes in the tab button checked states
        // Set visibility of the tab content to match the checked state
        ChangeListener tab_listener = new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                contentGraphics.setVisible(tabGraphics.isChecked());
                contentUI.setVisible(tabUI.isChecked());
                contentPerformance.setVisible(tabPerformance.isChecked());
                contentControls.setVisible(tabControls.isChecked());
                contentScreenshots.setVisible(tabScreenshots.isChecked());
                contentFrames.setVisible(tabFrames.isChecked());
                contentCamera.setVisible(tabCamera.isChecked());
                content360.setVisible(tab360.isChecked());
                contentData.setVisible(tabData.isChecked());
                contentGaia.setVisible(tabGaia.isChecked());
            }
        };
        tabGraphics.addListener(tab_listener);
        tabUI.addListener(tab_listener);
        tabPerformance.addListener(tab_listener);
        tabControls.addListener(tab_listener);
        tabScreenshots.addListener(tab_listener);
        tabFrames.addListener(tab_listener);
        tabCamera.addListener(tab_listener);
        tab360.addListener(tab_listener);
        tabData.addListener(tab_listener);
        tabGaia.addListener(tab_listener);

        // Let only one tab button be checked at a time
        ButtonGroup<Button> tabs = new ButtonGroup<Button>();
        tabs.setMinCheckCount(1);
        tabs.setMaxCheckCount(1);
        tabs.add(tabGraphics);
        tabs.add(tabUI);
        tabs.add(tabPerformance);
        tabs.add(tabControls);
        tabs.add(tabScreenshots);
        tabs.add(tabFrames);
        tabs.add(tabCamera);
        tabs.add(tab360);
        tabs.add(tabData);
        tabs.add(tabGaia);

        /** BUTTONS **/
        HorizontalGroup buttonGroup = new HorizontalGroup();
        buttonGroup.pad(pad);
        buttonGroup.space(pad);

        TextButton accept = new OwnTextButton(txt("gui.saveprefs"), skin, "default");
        accept.setName("accept");
        accept.setSize(130 * GlobalConf.SCALE_FACTOR, 20 * GlobalConf.SCALE_FACTOR);
        accept.addListener(new EventListener() {

            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {

                    me.hide();
                    return true;
                }
                return false;
            }

        });
        TextButton close = new OwnTextButton(txt("gui.cancel"), skin, "default");
        close.setName("cancel");
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
        buttonGroup.addActor(accept);
        buttonGroup.addActor(close);

        add(table).pad(pad);
        row();
        add(buttonGroup).pad(pad).bottom().right();
        getTitleTable().align(Align.left);

        pack();

        /** CAPTURE SCROLL FOCUS **/
        stage.addListener(new EventListener() {

            @Override
            public boolean handle(Event event) {
                if (event instanceof InputEvent) {
                    InputEvent ie = (InputEvent) event;

                    if (ie.getType() == Type.mouseMoved) {
                        for (OwnScrollPane scroll : scrolls) {
                            if (ie.getTarget().isDescendantOf(scroll)) {
                                stage.setScrollFocus(scroll);
                            }
                        }
                        return true;
                    }
                }
                return false;
            }
        });

    }

    private void enableComponents(boolean enabled, Disableable... components) {
        for (Disableable c : components) {
            if (c != null)
                c.setDisabled(!enabled);
        }
    }

    private void selectFullscreen(boolean fullscreen, OwnTextField widthField, OwnTextField heightField, SelectBox<DisplayMode> fullScreenResolutions, CheckBox resizable, OwnLabel widthLabel, OwnLabel heightLabel) {
        if (fullscreen) {
            GlobalConf.screen.SCREEN_WIDTH = ((DisplayMode) fullScreenResolutions.getSelected()).width;
            GlobalConf.screen.SCREEN_HEIGHT = ((DisplayMode) fullScreenResolutions.getSelected()).height;
        } else {
            GlobalConf.screen.SCREEN_WIDTH = Integer.parseInt(widthField.getText());
            GlobalConf.screen.SCREEN_HEIGHT = Integer.parseInt(heightField.getText());
        }

        enableComponents(!fullscreen, widthField, heightField, resizable, widthLabel, heightLabel);
        enableComponents(fullscreen, fullScreenResolutions);
    }

    private int idxAa(int base, int x) {
        if (x == -1)
            return 1;
        if (x == -2)
            return 2;
        if (x == 0)
            return 0;
        return (int) (Math.log(x) / Math.log(2) + 1e-10) + 2;
    }

    private int idxLang(String code, LangComboBoxBean[] langs) {
        if (code.isEmpty()) {
            code = I18n.bundle.getLocale().toLanguageTag();
        }
        for (int i = 0; i < langs.length; i++) {
            if (langs[i].locale.toLanguageTag().equals(code)) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public void notify(Events event, Object... data) {
        // TODO Auto-generated method stub

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
