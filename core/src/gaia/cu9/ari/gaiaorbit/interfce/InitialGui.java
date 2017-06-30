package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

import gaia.cu9.ari.gaiaorbit.util.GlobalResources;

/**
 * Displays dataset chooser screen.
 * 
 * @author Toni Sagrista
 *
 */
public class InitialGui extends AbstractGui {

    protected ChooseDatasetWindow cdw;

    /** Lock object for synchronisation **/

    public InitialGui() {
        lock = new Object();
    }

    @Override
    public void initialize(AssetManager assetManager) {
        // User interface
        ui = new Stage(new ScreenViewport(), GlobalResources.spriteBatch);
        skin = GlobalResources.skin;

        rebuildGui();

    }

    @Override
    public void doneLoading(AssetManager assetManager) {
    }

    protected void rebuildGui() {
        if (ui != null) {
            ui.clear();
        }
        cdw = new ChooseDatasetWindow(ui, skin);
        cdw.show(ui);
    }

}
