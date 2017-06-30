package gaia.cu9.ari.gaiaorbit.interfce;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.data.stars.UncertaintiesHandler;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.interfce.components.VisualEffectsComponent;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.Planet;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.ComponentTypes;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.camera.CameraUtils;
import gaia.cu9.ari.gaiaorbit.util.format.INumberFormat;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.scene2d.ContextMenu;
import gaia.cu9.ari.gaiaorbit.util.scene2d.MenuItem;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;

/**
 * Full OpenGL GUI with all the controls and whistles.
 * 
 * @author Toni Sagrista
 *
 */
public class FullGui extends AbstractGui {

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
    protected PreferencesWindow preferencesWindow;
    protected VisualEffectsComponent visualEffectsComponent;

    protected INumberFormat nf;
    protected Label mouseXCoord, mouseYCoord;

    protected ISceneGraph sg;
    private ComponentType[] visibilityEntities;
    private boolean[] visible;

    private List<Actor> invisibleInStereoMode;

    public FullGui() {
        super();
    }

    @Override
    public void initialize(AssetManager assetManager) {
        // User interface
        ui = new Stage(new ScreenViewport(), GlobalResources.spriteBatch);
    }

    @Override
    public void doneLoading(AssetManager assetManager) {
        Logger.info(txt("notif.gui.init"));

        skin = GlobalResources.skin;
        interfaces = new Array<IGuiInterface>();

        buildGui();

        // We must subscribe to the desired events
        EventManager.instance.subscribe(this, Events.FOV_CHANGED_CMD, Events.SHOW_TUTORIAL_ACTION, Events.SHOW_SEARCH_ACTION, Events.REMOVE_KEYBOARD_FOCUS, Events.REMOVE_GUI_COMPONENT, Events.ADD_GUI_COMPONENT, Events.SHOW_ABOUT_ACTION, Events.RA_DEC_UPDATED, Events.LON_LAT_UPDATED, Events.POPUP_MENU_FOCUS, Events.SHOW_PREFERENCES_ACTION, Events.SHOW_LAND_AT_LOCATION_ACTION);
    }

    protected void buildGui() {
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
        // focusInterface.setFillParent(true);
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
        interfaces.add(notificationsInterface);

        // MESSAGES INTERFACE - LOW CENTER
        messagesInterface = new MessagesInterface(skin, lock);
        messagesInterface.setFillParent(true);
        messagesInterface.left().bottom();
        messagesInterface.pad(0, 300, 150, 0);
        interfaces.add(messagesInterface);

        // INPUT STATE
        inputInterface = new ScriptStateInterface(skin);
        inputInterface.setFillParent(true);
        inputInterface.right().top();
        inputInterface.pad(100, 0, 0, 5);
        interfaces.add(inputInterface);

        // CUSTOM OBJECTS INTERFACE
        customInterface = new CustomInterface(ui, skin, lock);
        interfaces.add(customInterface);

        // MOUSE X/Y COORDINATES
        mouseXCoord = new OwnLabel("", skin, "default");
        mouseYCoord = new OwnLabel("", skin, "default");

        /** ADD TO UI **/
        rebuildGui();
        // controls.collapse();

        // INVISIBLE IN STEREOSCOPIC MODE
        invisibleInStereoMode = new ArrayList<Actor>();
        invisibleInStereoMode.add(controlsWindow);
        invisibleInStereoMode.add(fi);
        invisibleInStereoMode.add(messagesInterface);
        invisibleInStereoMode.add(inputInterface);
        // invisibleInStereoMode.add(customInterface);
        invisibleInStereoMode.add(mouseXCoord);
        invisibleInStereoMode.add(mouseYCoord);
    }

    public void recalculateOptionsSize() {
        controlsWindow.recalculateSize();
    }

    protected void rebuildGui() {

        if (ui != null) {
            ui.clear();
            boolean collapsed = false;
            if (controlsWindow != null) {
                collapsed = controlsWindow.isCollapsed();
                recalculateOptionsSize();
                if (collapsed)
                    controlsWindow.collapseInstant();
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
     * Removes the focus from this Gui and returns true if the focus was in the
     * GUI, false otherwise.
     * 
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
    public void update(float dt) {
        ui.act(dt);
        notificationsInterface.update();
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
        case SHOW_LAND_AT_LOCATION_ACTION:
            CelestialBody target = (CelestialBody) data[0];
            LandAtWindow landAtLocation = new LandAtWindow(target, ui, skin);
            landAtLocation.show(ui);
            break;
        case SHOW_ABOUT_ACTION:
            if (aboutWindow == null) {
                aboutWindow = new AboutWindow(ui, skin);
            }
            aboutWindow.show(ui);
            break;
        case SHOW_PREFERENCES_ACTION:
            preferencesWindow = new PreferencesWindow(ui, skin);
            preferencesWindow.show(ui);
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

            mouseXCoord.setText("RA/".concat(nf.format(ra)).concat("°"));
            mouseXCoord.setPosition(x, 10f * GlobalConf.SCALE_FACTOR);
            mouseYCoord.setText("DEC/".concat(nf.format(dec)).concat("°"));
            mouseYCoord.setPosition(Gdx.graphics.getWidth() - (60f * GlobalConf.SCALE_FACTOR), Gdx.graphics.getHeight() - y);
            break;
        case LON_LAT_UPDATED:
            Double lon = (Double) data[0];
            Double lat = (Double) data[1];
            x = (Integer) data[2];
            y = (Integer) data[3];

            mouseXCoord.setText("Lon/" + nf.format(lon) + "°");
            mouseXCoord.setPosition(x, 10f * GlobalConf.SCALE_FACTOR);
            mouseYCoord.setText("Lat/" + nf.format(lat) + "°");
            mouseYCoord.setPosition(Gdx.graphics.getWidth() - (60f * GlobalConf.SCALE_FACTOR), Gdx.graphics.getHeight() - y);
            break;
        case POPUP_MENU_FOCUS:
            final CelestialBody candidate = (CelestialBody) data[0];
            int screenX = Gdx.input.getX();
            int screenY = Gdx.input.getY();

            ContextMenu popup = new ContextMenu(skin, "default");

            MenuItem select = new MenuItem(txt("context.select", candidate.getName()), skin, "default");
            select.addListener(new EventListener() {

                @Override
                public boolean handle(Event event) {
                    if (event instanceof ChangeEvent) {
                        EventManager.instance.post(Events.CAMERA_MODE_CMD, CameraMode.Focus);
                        EventManager.instance.post(Events.FOCUS_CHANGE_CMD, candidate);
                    }
                    return false;
                }

            });
            popup.addItem(select);

            MenuItem go = new MenuItem(txt("context.goto", candidate.getName()), skin, "default");
            go.addListener(new EventListener() {

                @Override
                public boolean handle(Event event) {
                    if (event instanceof ChangeEvent)
                        EventManager.instance.post(Events.NAVIGATE_TO_OBJECT, candidate);
                    return false;
                }

            });
            popup.addItem(go);

            if (candidate instanceof Planet) {
                popup.addSeparator();

                MenuItem landOn = new MenuItem(txt("context.landon", candidate.getName()), skin, "default");
                landOn.addListener(new EventListener() {

                    @Override
                    public boolean handle(Event event) {
                        if (event instanceof ChangeEvent) {
                            EventManager.instance.post(Events.LAND_ON_OBJECT, candidate);
                            return true;
                        }
                        return false;
                    }

                });
                popup.addItem(landOn);

                double[] lonlat = new double[2];
                boolean ok = CameraUtils.getLonLat((Planet) candidate, GaiaSky.instance.getICamera(), screenX, screenY, new Vector3(), new Vector3(), new Vector3(), new Vector3(), new Vector3d(), new Vector3d(), new Matrix4(), lonlat);
                if (ok) {
                    final Double pointerLon = lonlat[0];
                    final Double pointerLat = lonlat[1];
                    // Add mouse pointer
                    MenuItem landOnPointer = new MenuItem(txt("context.landatpointer", candidate.getName()), skin, "default");
                    landOnPointer.addListener(new EventListener() {

                        @Override
                        public boolean handle(Event event) {
                            if (event instanceof ChangeEvent) {
                                EventManager.instance.post(Events.LAND_AT_LOCATION_OF_OBJECT, candidate, pointerLon, pointerLat);
                                return true;
                            }
                            return false;
                        }

                    });
                    popup.addItem(landOnPointer);
                }

                MenuItem landOnCoord = new MenuItem(txt("context.landatcoord", candidate.getName()), skin, "default");
                landOnCoord.addListener(new EventListener() {

                    @Override
                    public boolean handle(Event event) {
                        if (event instanceof ChangeEvent) {
                            EventManager.instance.post(Events.SHOW_LAND_AT_LOCATION_ACTION, candidate);
                            return true;
                        }
                        return false;
                    }

                });
                popup.addItem(landOnCoord);
            }

            if (candidate instanceof Star) {
                boolean sep = false;
                if (UncertaintiesHandler.getInstance().containsStar(candidate.id)) {
                    popup.addSeparator();
                    sep = true;

                    MenuItem showUncertainties = new MenuItem("Show uncertainties", skin, "default");
                    showUncertainties.addListener(new EventListener() {

                        @Override
                        public boolean handle(Event event) {
                            if (event instanceof ChangeEvent) {
                                EventManager.instance.post(Events.SHOW_UNCERTAINTIES, candidate);
                                return true;
                            }
                            return false;
                        }

                    });
                    popup.addItem(showUncertainties);
                }

                if (UncertaintiesHandler.getInstance().containsUncertainties()) {
                    if (!sep)
                        popup.addSeparator();

                    MenuItem hideUncertainties = new MenuItem("Hide uncertainties", skin, "default");
                    hideUncertainties.addListener(new EventListener() {

                        @Override
                        public boolean handle(Event event) {
                            if (event instanceof ChangeEvent) {
                                EventManager.instance.post(Events.HIDE_UNCERTAINTIES, candidate);
                                return true;
                            }
                            return false;
                        }

                    });
                    popup.addItem(hideUncertainties);

                }
            }

            int mx = Gdx.input.getX();
            int my = Gdx.input.getY();
            int h = Gdx.graphics.getHeight();

            float px = mx;
            float py = h - my - 20 * GlobalConf.SCALE_FACTOR;

            popup.showMenu(ui, px, py);

            break;
        default:
            break;
        }

    }

    @Override
    public void setSceneGraph(ISceneGraph sg) {
        this.sg = sg;
    }

    @Override
    public void setVisibilityToggles(ComponentType[] entities, ComponentTypes visible) {
        this.visibilityEntities = entities;
        ComponentType[] vals = ComponentType.values();
        this.visible = new boolean[vals.length];
        for (int i = 0; i < vals.length; i++)
            this.visible[i] = visible.get(vals[i].ordinal());
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
        controlsWindow = new ControlsWindow(txt("gui.controlpanel"), skin, ui);
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

        controlsWindow.collapseInstant();
    }

}
