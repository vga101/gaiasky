package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.utils.Array;

public class GuiRegistry {

    private static Array<IGui> guis;

    static {
        guis = new Array<IGui>(true, 2);
    }

    /**
     * Render lock object
     */
    public static Object guirenderlock = new Object();

    public static void registerGui(IGui gui) {
        if (!guis.contains(gui, true))
            guis.add(gui);
    }

    public static boolean unregisterGui(IGui gui) {
        return guis.removeValue(gui, true);
    }

    public static boolean unregisterAll() {
        guis.clear();
        return true;
    }

    public static void render(int rw, int rh) {
        synchronized (guirenderlock) {
            for (int i = 0; i < guis.size; i++) {
                guis.get(i).getGuiStage().getViewport().apply();
                guis.get(i).render(rw, rh);
            }
        }
    }

    public static void update(double dt) {
        for (IGui gui : guis)
            gui.update(dt);
    }

}
