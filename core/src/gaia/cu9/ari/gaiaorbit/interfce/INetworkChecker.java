package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;

/**
 * Interface all network checkers must comply.
 * @author tsagrista
 *
 */
public interface INetworkChecker {

    public void start();

    public boolean executing();

    public void setFocus(CelestialBody focus);

    public void doNotify();

    public void setParameters(Table table, Skin skin, float pad);
}
