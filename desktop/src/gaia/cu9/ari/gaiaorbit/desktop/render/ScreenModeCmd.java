package gaia.cu9.ari.gaiaorbit.desktop.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics.DisplayMode;
import com.badlogic.gdx.Graphics.Monitor;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Window;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;

public class ScreenModeCmd implements IObserver {

    public static ScreenModeCmd instance;

    public static void initialize() {
        ScreenModeCmd.instance = new ScreenModeCmd();
    }

    private ScreenModeCmd() {
        EventManager.instance.subscribe(this, Events.SCREEN_MODE_CMD);
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case SCREEN_MODE_CMD:
            boolean toFullscreen = GlobalConf.screen.FULLSCREEN;
            if (toFullscreen) {
                // TODO hack
                Monitor m = Gdx.graphics.getPrimaryMonitor();
                // Available modes for this monitor
                DisplayMode[] modes = Gdx.graphics.getDisplayModes(m);
                // Find best mode
                DisplayMode mymode = null;
                for (DisplayMode mode : modes) {
                    if (mode.height == GlobalConf.screen.FULLSCREEN_HEIGHT && mode.width == GlobalConf.screen.FULLSCREEN_WIDTH) {
                        mymode = mode;
                        break;
                    }
                }
                // If no mode found, get default
                if (mymode == null) {
                    mymode = Gdx.graphics.getDisplayMode(m);
                    GlobalConf.screen.FULLSCREEN_WIDTH = mymode.width;
                    GlobalConf.screen.FULLSCREEN_HEIGHT = mymode.height;
                }

                // set the window to fullscreen mode
                boolean good = Gdx.graphics.setFullscreenMode(mymode);
                if (!good) {
                    Logger.error(I18n.bundle.format("notif.error", I18n.bundle.get("gui.fullscreen")));
                }

            } else {
                Monitor m = Gdx.graphics.getMonitor();
                DisplayMode dm = Gdx.graphics.getDisplayMode(m);

                int width = GlobalConf.screen.SCREEN_WIDTH;
                int height = GlobalConf.screen.SCREEN_HEIGHT;

                boolean good = Gdx.graphics.setWindowedMode(width, height);
                if (!good) {
                    Logger.error(I18n.bundle.format("notif.error", I18n.bundle.get("gui.windowed")));
                }

                Lwjgl3Window window = ((Lwjgl3Graphics) Gdx.graphics).getWindow();
                window.setSizeLimits(width, height, width, height);
                int posx = m.virtualX + dm.width / 2 - Gdx.graphics.getWidth() / 2;
                int posy = m.virtualY + dm.height / 2 - Gdx.graphics.getHeight() / 2;
                window.setPosition(posx, posy);

            }
            Gdx.graphics.setVSync(GlobalConf.screen.VSYNC);
            break;
        default:
            break;

        }
    }
}
