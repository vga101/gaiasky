package gaia.cu9.ari.gaiaorbit.desktop.gui.swing;

import javax.swing.JFrame;

import gaia.cu9.ari.gaiaorbit.util.I18n;

/**
 * Provides a couple of functions to retrieve internationalised messages.
 * @author Toni Sagrista
 *
 */
public abstract class I18nJFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    public I18nJFrame(String name) {
        super(name);
    }

    protected static String txt(String key) {
        return I18n.bundle.get(key);
    }

    protected static String txt(String key, Object... args) {
        return I18n.bundle.format(key, args);
    }
}
