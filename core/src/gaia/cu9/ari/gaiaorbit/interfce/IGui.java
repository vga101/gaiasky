package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;

import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.util.GSEnumSet;

public interface IGui extends Disposable {

    public void initialize(AssetManager assetManager);

    public void doneLoading(AssetManager assetManager);

    public void update(float dt);

    public void render(int rw, int rh);

    public void resize(int width, int height);

    public void resizeImmediate(int width, int height);

    public boolean cancelTouchFocus();

    public Stage getGuiStage();

    public void setSceneGraph(ISceneGraph sg);

    public void setVisibilityToggles(ComponentType[] entities, GSEnumSet<ComponentType> visible);

    /**
     * Returns the first actor found with the specified name. Note this
     * recursively compares the name of every actor in the GUI.
     * 
     * @return The actor if it exists, null otherwise.
     **/
    public Actor findActor(String name);

}