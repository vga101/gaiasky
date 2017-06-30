package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gaia.cu9.ari.gaiaorbit.scenegraph.IFocus;

/**
 * Interface all network checkers must comply.
 * 
 * @author tsagrista
 *
 */
public interface INetworkChecker {

    public void start();

    public boolean executing();

    public void setFocus(IFocus focus);

    public void doNotify();

    public void stopExecution();

    public void setParameters(Table table, Skin skin, float pad);
}
