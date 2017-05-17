package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.util.GSEnumSet;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;

public class DebugGui implements IGui {
    private Skin skin;
    protected DebugInterface debugInterface;
    protected Array<IGuiInterface> interfaces;
    protected Stage ui;

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
        interfaces = new Array<IGuiInterface>();
        skin = GlobalResources.skin;

        // DEBUG INFO - TOP RIGHT
        debugInterface = new DebugInterface(skin, lock);
        debugInterface.setFillParent(true);
        debugInterface.right().top();
        debugInterface.pad(5, 0, 0, 5);
        interfaces.add(debugInterface);

        rebuildGui();
    }

    private void rebuildGui() {
        if (ui != null) {
            ui.clear();
            if (debugInterface != null)
                ui.addActor(debugInterface);
        }
    }

    @Override
    public void dispose() {
        for (IGuiInterface iface : interfaces)
            iface.dispose();

        ui.dispose();
    }

    @Override
    public void update(float dt) {
        ui.act(dt);
    }

    @Override
    public void render(int rw, int rh) {
        ui.draw();
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

    @Override
    public boolean cancelTouchFocus() {
        return false;
    }

    @Override
    public Stage getGuiStage() {
        return ui;
    }

    @Override
    public void setSceneGraph(ISceneGraph sg) {
    }

    @Override
    public void setVisibilityToggles(ComponentType[] entities, GSEnumSet<ComponentType> visible) {
    }

    @Override
    public Actor findActor(String name) {
        return ui.getRoot().findActor(name);
    }

}
