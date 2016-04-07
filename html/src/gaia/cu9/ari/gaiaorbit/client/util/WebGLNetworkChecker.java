package gaia.cu9.ari.gaiaorbit.client.util;

import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gaia.cu9.ari.gaiaorbit.interfce.INetworkChecker;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;

public class WebGLNetworkChecker implements INetworkChecker {

    @Override
    public void start() {
    }

    @Override
    public boolean executing() {
        return false;
    }

    @Override
    public void setFocus(CelestialBody focus) {
    }

    @Override
    public void doNotify() {
    }

    @Override
    public void setParameters(Table table, Skin skin, float pad) {
    }

}
