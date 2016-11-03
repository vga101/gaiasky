package gaia.cu9.ari.gaiaorbit.desktop.gui.swing.jsplash;

import java.awt.Dimension;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.MouseInfo;
import java.awt.Rectangle;
import java.awt.Window;

public class GuiUtility {
    public GuiUtility() {
    }

    /**
     * Centers the given window on the screen where the mouse is
     * @param w
     */
    public static void centerOnScreen(Window w) {
        // Get graphics device where the mouse is
        GraphicsDevice currentMouseScreen = MouseInfo.getPointerInfo().getDevice();
        GraphicsConfiguration gc = currentMouseScreen.getDefaultConfiguration();
        Rectangle screenSize = gc.getBounds();
        Dimension windowSize = w.getPreferredSize();
        w.setLocation(screenSize.x + screenSize.width / 2 - windowSize.width / 2, screenSize.y + screenSize.height / 2 - windowSize.height / 2);
    }
}
