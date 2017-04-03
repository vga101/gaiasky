package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.widget.Menu;
import com.kotcrab.vis.ui.widget.MenuBar;
import com.kotcrab.vis.ui.widget.MenuItem;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.util.I18n;

public class GaiaSkyMenu extends Table implements IObserver {
    /**
     * The user interface stage
     */
    protected Stage ui;

    protected Skin skin;

    /**
     * Components
     */
    protected MenuBar menuBar;

    protected ISceneGraph sg;
    private ComponentType[] visibilityEntities;
    private boolean[] visible;

    public GaiaSkyMenu(Stage ui, Skin skin) {
        this.ui = ui;
        this.skin = skin;
    }

    public void initialize() {
        VisUI.load();
        buildGui();
    }

    private void buildGui() {
        menuBar = new MenuBar();

        // Time
        Menu timeMenu = new Menu(txt("gui.time"));
        timeMenu.scaleBy(2f);
        menuBar.addMenu(timeMenu);

        final MenuItem startTime = new MenuItem("Start time", skin.getDrawable("play-icon"));
        startTime.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.TOGGLE_TIME_CMD, true, true);
                    return true;
                }
                return false;
            }
        });
        startTime.setShortcut(Keys.SPACE);

        final MenuItem pauseTime = new MenuItem("Pause time", skin.getDrawable("pause-icon"));
        pauseTime.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.TOGGLE_TIME_CMD, false, true);
                    return true;
                }
                return false;
            }
        });
        pauseTime.setShortcut(Keys.A);

        MenuItem setDate = new MenuItem(txt("gui.pickdate"));

        timeMenu.addItem(startTime);
        timeMenu.addItem(pauseTime);
        timeMenu.addSeparator();
        timeMenu.addItem(setDate);

        // Camera
        Menu cameraMenu = new Menu(txt("gui.camera"));
        menuBar.addMenu(cameraMenu);

        // Effects
        Menu effectsMenu = new Menu(txt("gui.lighting"));
        menuBar.addMenu(effectsMenu);

        // Objects
        Menu objectsMenu = new Menu(txt("gui.visibility"));
        menuBar.addMenu(objectsMenu);

        // Gaia
        Menu gaiaMenu = new Menu(txt("gui.gaiascan"));
        menuBar.addMenu(gaiaMenu);

        // Preferences
        Menu prefsMenu = new Menu(txt("gui.preferences"));
        menuBar.addMenu(prefsMenu);

        // Help
        Menu helpMenu = new Menu(txt("gui.help"));

        // Add menu to table
        add(menuBar.getTable()).fillX().expandX().row();
    }

    public void dispose() {
        VisUI.dispose();

    }

    @Override
    public void notify(Events event, Object... data) {
        // TODO Auto-generated method stub

    }

    @Override
    public Actor findActor(String name) {
        // TODO Auto-generated method stub
        return null;
    }

    public void setSceneGraph(ISceneGraph sg) {
        this.sg = sg;
    }

    public void setVisibilityToggles(ComponentType[] entities, boolean[] visible) {
        this.visibilityEntities = entities;
        this.visible = visible;
    }

    private String txt(String key) {
        return I18n.bundle.get(key);
    }

}
