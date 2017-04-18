package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.util.GSEnumSet;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.format.INumberFormat;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;

/**
 * Full OpenGL GUI with all the controls and whistles.
 * @author Toni Sagrista
 *
 */
public class StereoGui implements IGui, IObserver {
    private Skin skin;
    /**
     * The user interface stage
     */
    protected Stage ui;

    protected NotificationsInterface notificationsOne, notificationsTwo;

    protected INumberFormat nf;

    /** Lock object for synchronisation **/
    private Object lock;

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
        EventManager.instance.subscribe(this, Events.TOGGLE_STEREO_PROFILE_INFO);
    }

    private void buildGui() {
        // Component types name init
        for (ComponentType ct : ComponentType.values()) {
            ct.getName();
        }

        nf = NumberFormatFactory.getFormatter("##0.###");

        // NOTIFICATIONS ONE - BOTTOM LEFT
        notificationsOne = new NotificationsInterface(skin, lock, true, true, false);
        notificationsOne.setFillParent(true);
        notificationsOne.left().bottom();
        notificationsOne.pad(0, 5, 5, 0);

        // NOTIFICATIONS TWO - BOTTOM CENTRE
        notificationsTwo = new NotificationsInterface(skin, lock, true, true, false);
        notificationsTwo.setFillParent(true);
        notificationsTwo.bottom();
        notificationsTwo.setX(Gdx.graphics.getWidth() / 2);
        notificationsTwo.pad(0, 5, 5, 0);

        /** ADD TO UI **/
        rebuildGui();

    }

    private void rebuildGui() {

        if (ui != null) {
            ui.clear();
            if (notificationsOne != null)
                ui.addActor(notificationsOne);
            if (notificationsTwo != null)
                ui.addActor(notificationsTwo);

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
        EventManager.instance.removeAllSubscriptions(this);
    }

    @Override
    public void update(float dt) {
        notificationsTwo.setX(notificationsTwo.getMessagesWidth() / 2);
        ui.act(dt);
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
        case TOGGLE_STEREO_PROFILE_INFO:
            StereoProfile profile = (StereoProfile) data[0];
            if (profile == StereoProfile.ANAGLYPHIC) {
                notificationsTwo.setVisible(false);
            } else {
                notificationsTwo.setVisible(true);
            }
            break;
        default:
            break;
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

    @Override
    public void setVisibilityToggles(ComponentType[] entities, GSEnumSet<ComponentType> visible) {
    }

    public void setSceneGraph(ISceneGraph sg) {
    }
}
