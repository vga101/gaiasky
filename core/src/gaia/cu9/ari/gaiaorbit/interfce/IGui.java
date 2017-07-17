package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Disposable;

import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.util.ComponentTypes;

/**
 * An interface to be implemented by all top-level GUIs in Gaia Sky
 * 
 * @author tsagrista
 *
 */
public interface IGui extends Disposable {

    /**
     * Initializes the GUI, adding all the resources to the asset manager queue
     * for loading
     * 
     * @param assetManager
     *            The asset manager to load the resources with
     */
    public void initialize(AssetManager assetManager);

    /**
     * Hook that runs after the assets have been loaded. Completes the
     * initialization process
     * 
     * @param assetManager
     *            The asset manager
     */
    public void doneLoading(AssetManager assetManager);

    /**
     * Updates the GUI
     * 
     * @param dt
     *            Time in seconds since the last frame
     */
    public void update(double dt);

    /**
     * Renders this GUI
     * 
     * @param rw
     *            The render width
     * @param rh
     *            The render height
     */
    public void render(int rw, int rh);

    /**
     * Resizes this GUI to the given values at the end of the current loop
     * 
     * @param width
     *            The new width
     * @param height
     *            The new height
     */
    public void resize(int width, int height);

    /**
     * Resizes without waiting for the current loop to finish
     * 
     * @param width
     *            The new width
     * @param height
     *            The new height
     */
    public void resizeImmediate(int width, int height);

    /**
     * Removes the focus from this GUI and returns true if the focus was in the
     * GUI, false otherwise.
     * 
     * @return true if the focus was in the GUI, false otherwise.
     */
    public boolean cancelTouchFocus();

    /**
     * Returns the stage
     * 
     * @return The stage
     */
    public Stage getGuiStage();

    /**
     * Sets the scene graph to this GUI
     * 
     * @param sg
     *            The scene graph
     */
    public void setSceneGraph(ISceneGraph sg);

    /**
     * Sets the visibility state of the component entities
     * 
     * @param entities
     *            The entities
     * @param visible
     *            The states
     */
    public void setVisibilityToggles(ComponentType[] entities, ComponentTypes visible);

    /**
     * Returns the first actor found with the specified name. Note this
     * recursively compares the name of every actor in the GUI.
     * 
     * @return The actor if it exists, null otherwise.
     **/
    public Actor findActor(String name);

}