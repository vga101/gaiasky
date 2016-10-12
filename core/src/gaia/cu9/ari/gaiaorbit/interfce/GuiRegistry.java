package gaia.cu9.ari.gaiaorbit.interfce;

import java.util.ArrayList;
import java.util.List;

public class GuiRegistry {

    private static List<IGui> guis;

    static {
        guis = new ArrayList<IGui>(2);
    }

    public static void registerGui(IGui gui) {
        if (!guis.contains(gui))
            guis.add(gui);
    }

    public static boolean unregisterGui(IGui gui) {
        return guis.remove(gui);
    }

    public static void render(int rw, int rh) {
        for (IGui gui : guis) {
            gui.getGuiStage().getViewport().apply();
            gui.render(rw, rh);
        }
    }

    public static void update(float dt) {
        for (IGui gui : guis)
            gui.update(dt);
    }

}
