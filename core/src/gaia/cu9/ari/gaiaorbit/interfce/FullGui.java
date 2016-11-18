package gaia.cu9.ari.gaiaorbit.interfce;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.components.VisualEffectsComponent;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.format.INumberFormat;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;

/**
 * Full OpenGL GUI with all the controls and whistles.
 * @author Toni Sagrista
 *
 */
public class FullGui implements IGui, IObserver {
    private Skin skin;
    /**
     * The user interface stage
     */
    protected Stage ui;

    protected ControlsWindow controlsWindow;

    protected Container<FocusInfoInterface> fi;
    protected FocusInfoInterface focusInterface;
    protected NotificationsInterface notificationsInterface;
    protected MessagesInterface messagesInterface;
    protected CustomInterface customInterface;
    protected ScriptStateInterface inputInterface;
    protected Container<WebGLInterface> wgl;
    protected WebGLInterface webglInterface;

    protected SearchDialog searchDialog;
    protected AboutWindow aboutWindow;
    protected VisualEffectsComponent visualEffectsComponent;

    protected INumberFormat nf;
    protected Label mouseXCoord, mouseYCoord;

    protected ISceneGraph sg;
    private ComponentType[] visibilityEntities;
    private boolean[] visible;

    private List<Actor> invisibleInStereoMode;

    /** Lock object for synchronisation **/
    private Object lock;

    public void setSceneGraph(ISceneGraph sg) {
        this.sg = sg;
    }

    public void setVisibilityToggles(ComponentType[] entities, boolean[] visible) {
        this.visibilityEntities = entities;
        this.visible = visible;
    }

    public void initialize(AssetManager assetManager) {
        // User interface
        ui = new Stage(new ScreenViewport(), GlobalResources.spriteBatch);
        lock = new Object();
    }

    /**
     * Constructs the interface
     */
    public void doneLoading(AssetManager assetManager) {
        Logger.info(txt("notif.gui.init"));

        skin = GlobalResources.skin;

        buildGui();

        // We must subscribe to the desired events
        EventManager.instance.subscribe(this, Events.FOV_CHANGED_CMD, Events.SHOW_TUTORIAL_ACTION, Events.SHOW_SEARCH_ACTION, Events.REMOVE_KEYBOARD_FOCUS, Events.REMOVE_GUI_COMPONENT, Events.ADD_GUI_COMPONENT, Events.SHOW_ABOUT_ACTION, Events.RA_DEC_UPDATED, Events.LON_LAT_UPDATED);
    }

    private void buildGui() {
        // Component types name init
        for (ComponentType ct : ComponentType.values()) {
            ct.getName();
        }

        if (Constants.focalplane) {
            // WEBGL INTERFACE - TOP LEFT
            addWebglInterface();
        } else {
            // CONTROLS WINDOW
            addControlsWindow();
        }

        nf = NumberFormatFactory.getFormatter("##0.###");

        // FOCUS INFORMATION - BOTTOM RIGHT
        focusInterface = new FocusInfoInterface(skin);
        //focusInterface.setFillParent(true);
        focusInterface.left().top();
        fi = new Container<FocusInfoInterface>(focusInterface);
        fi.setFillParent(true);
        fi.bottom().right();
        fi.pad(0, 0, 10, 10);

        // NOTIFICATIONS INTERFACE - BOTTOM LEFT
        notificationsInterface = new NotificationsInterface(skin, lock, true);
        notificationsInterface.setFillParent(true);
        notificationsInterface.left().bottom();
        notificationsInterface.pad(0, 5, 5, 0);

        // MESSAGES INTERFACE - LOW CENTER
        messagesInterface = new MessagesInterface(skin, lock);
        messagesInterface.setFillParent(true);
        messagesInterface.left().bottom();
        messagesInterface.pad(0, 300, 150, 0);

        // INPUT STATE
        inputInterface = new ScriptStateInterface(skin);
        inputInterface.setFillParent(true);
        inputInterface.right().top();
        inputInterface.pad(100, 0, 0, 5);

        // CUSTOM OBJECTS INTERFACE
        customInterface = new CustomInterface(ui, skin, lock);

        // MOUSE X/Y COORDINATES
        mouseXCoord = new OwnLabel("", skin, "default");
        mouseYCoord = new OwnLabel("", skin, "default");

        /** ADD TO UI **/
        rebuildGui();
        //controls.collapse();

        // INVISIBLE IN STEREOSCOPIC MODE
        invisibleInStereoMode = new ArrayList<Actor>();
        invisibleInStereoMode.add(controlsWindow);
        invisibleInStereoMode.add(fi);
        invisibleInStereoMode.add(messagesInterface);
        invisibleInStereoMode.add(inputInterface);
        //invisibleInStereoMode.add(customInterface);
        invisibleInStereoMode.add(mouseXCoord);
        invisibleInStereoMode.add(mouseYCoord);
    }

    public void recalculateOptionsSize() {
        controlsWindow.recalculateSize();
    }

    private void rebuildGui() {

        if (ui != null) {
            ui.clear();
            boolean collapsed = false;
            if (controlsWindow != null) {
                collapsed = controlsWindow.isCollapsed();
                recalculateOptionsSize();
                if (collapsed)
                    controlsWindow.collapse();
                controlsWindow.setPosition(0, Gdx.graphics.getHeight() - controlsWindow.getHeight());
                ui.addActor(controlsWindow);
            }
            if (webglInterface != null)
                ui.addActor(wgl);
            if (notificationsInterface != null)
                ui.addActor(notificationsInterface);
            if (messagesInterface != null)
                ui.addActor(messagesInterface);
            if (focusInterface != null && !GlobalConf.runtime.STRIPPED_FOV_MODE)
                ui.addActor(fi);
            if (inputInterface != null && Constants.desktop) {
                ui.addActor(inputInterface);
            }
            if (mouseXCoord != null && mouseYCoord != null) {
                ui.addActor(mouseXCoord);
                ui.addActor(mouseYCoord);
            }

            if (customInterface != null) {
                customInterface.reAddObjects();
            }

            /** CAPTURE SCROLL FOCUS **/
            ui.addListener(new EventListener() {

                @Override
                public boolean handle(Event event) {
                    if (event instanceof InputEvent) {
                        InputEvent ie = (InputEvent) event;

                        if (ie.getType() == Type.mouseMoved) {
                            Actor scrollPanelAncestor = getScrollPanelAncestor(ie.getTarget());
                            ui.setScrollFocus(scrollPanelAncestor);
                        } else if (ie.getType() == Type.touchDown) {
                            if (ie.getTarget() instanceof TextField)
                                ui.setKeyboardFocus(ie.getTarget());
                        }
                    }
                    return false;
                }

                private Actor getScrollPanelAncestor(Actor actor) {
                    if (actor == null) {
                        return null;
                    } else if (actor instanceof ScrollPane) {
                        return actor;
                    } else {
                        return getScrollPanelAncestor(actor.getParent());
                    }
                }

            });
        }
    }

    /**
     * Removes the focus from this Gui and returns true if the focus was in the GUI, false otherwise.
     * @return true if the focus was in the GUI, false otherwise.
     */
    public boolean cancelTouchFocus() {
        if (ui.getScrollFocus() != null) {
            ui.setScrollFocus(null);
            ui.setKeyboardFocus(null);
            return true;
        }
        return false;
    }

    @Override
    public Stage getGuiStage() {
        return ui;
    }

    @Override
    public void dispose() {
        ui.dispose();
    }

    @Override
    public void update(float dt) {
        ui.act(dt);
        notificationsInterface.update();
    }

    @Override
    public void render(int rw, int rh) {
        synchronized (lock) {
            ui.draw();
        }
    }

    public String getName() {
        return "GUI";
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case SHOW_TUTORIAL_ACTION:
            EventManager.instance.post(Events.RUN_SCRIPT_PATH, GlobalConf.program.TUTORIAL_SCRIPT_LOCATION);
            break;
        case SHOW_SEARCH_ACTION:
            if (searchDialog == null) {
                searchDialog = new SearchDialog(this, skin, sg);
            } else {
                searchDialog.clearText();
            }
            searchDialog.display();
            break;
        case SHOW_ABOUT_ACTION:
            if (aboutWindow == null) {
                aboutWindow = new AboutWindow(ui, skin);
            }
            aboutWindow.display();
            break;
        case REMOVE_KEYBOARD_FOCUS:
            ui.setKeyboardFocus(null);
            break;
        case REMOVE_GUI_COMPONENT:
            String name = (String) data[0];
            String method = "remove" + TextUtils.capitalise(name);
            try {
                Method m = ClassReflection.getMethod(this.getClass(), method);
                m.invoke(this);
            } catch (ReflectionException e) {
                Logger.error(e);
            }
            rebuildGui();
            break;
        case ADD_GUI_COMPONENT:
            name = (String) data[0];
            method = "add" + TextUtils.capitalise(name);
            try {
                Method m = ClassReflection.getMethod(this.getClass(), method);
                m.invoke(this);
            } catch (ReflectionException e) {
                Logger.error(e);
            }
            rebuildGui();
            break;
        case RA_DEC_UPDATED:
            Double ra = (Double) data[0];
            Double dec = (Double) data[1];
            Integer x = (Integer) data[2];
            Integer y = (Integer) data[3];

            mouseXCoord.setText("RA/" + nf.format(ra) + "°");
            mouseXCoord.setPosition(x, 10);
            mouseYCoord.setText("DEC/" + nf.format(dec) + "°");
            mouseYCoord.setPosition(Gdx.graphics.getWidth() - 65, Gdx.graphics.getHeight() - y);
            break;
        case LON_LAT_UPDATED:
            Double lon = (Double) data[0];
            Double lat = (Double) data[1];
            x = (Integer) data[2];
            y = (Integer) data[3];

            mouseXCoord.setText("Lon/" + nf.format(lon) + "°");
            mouseXCoord.setPosition(x, 10);
            mouseYCoord.setText("Lat/" + nf.format(lat) + "°");
            mouseYCoord.setPosition(Gdx.graphics.getWidth() - 65, Gdx.graphics.getHeight() - y);
        }

    }

    @Override
    public void resize(final int width, final int height) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                resizeImmediate(width, height);
            }
        });
    }

    @Override
    public void resizeImmediate(final int width, final int height) {
        ui.getViewport().update(width, height, true);
        rebuildGui();
    }

    /**
     * Small override that returns the user set width as preferred width.
     * @author Toni Sagrista
     *
     */
    private class OwnTextField extends TextField {

        public OwnTextField(String text, Skin skin) {
            super(text, skin);
        }

        @Override
        public float getPrefWidth() {
            return getWidth() > 0 ? getWidth() : 150;
        }

    }

    @Override
    public Actor findActor(String name) {
        return ui.getRoot().findActor(name);
    }

    private String txt(String key) {
        return I18n.bundle.get(key);
    }

    private String txt(String key, Object... params) {
        return I18n.bundle.format(key, params);
    }

    public void removeWebglInterface() {
        if (webglInterface != null) {
            webglInterface.remove();
            webglInterface = null;
            wgl.remove();
            wgl = null;
        }
    }

    public void addWebglInterface() {
        webglInterface = new WebGLInterface(skin, GaiaSky.instance.time);
        wgl = new Container<WebGLInterface>(webglInterface);
        wgl.setFillParent(true);
        wgl.left().bottom();
        wgl.pad(0, 5, 45, 0);
    }

    public void removeControlsWindow() {
        if (controlsWindow != null) {
            controlsWindow.remove();
            controlsWindow = null;
        }
    }

    public void addControlsWindow() {
        controlsWindow = new ControlsWindow(txt("gui.controls"), skin, ui);
        controlsWindow.setSceneGraph(sg);
        controlsWindow.setVisibilityToggles(visibilityEntities, visible);
        controlsWindow.initialize();
        controlsWindow.left();
        controlsWindow.getTitleTable().align(Align.left);
        controlsWindow.setFillParent(false);
        controlsWindow.setMovable(true);
        controlsWindow.setResizable(false);
        controlsWindow.padRight(5);
        controlsWindow.padBottom(5);

        controlsWindow.collapse();
    }
}
