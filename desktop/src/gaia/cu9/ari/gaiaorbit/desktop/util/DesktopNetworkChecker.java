package gaia.cu9.ari.gaiaorbit.desktop.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.desktop.network.HttpQuery;
import gaia.cu9.ari.gaiaorbit.interfce.INetworkChecker;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ModelBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.scene2d.Link;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;

public class DesktopNetworkChecker extends Thread implements INetworkChecker {
    private static String URL_SIMBAD = "http://simbad.u-strasbg.fr/simbad/sim-id?Ident=";
    private static String URL_WIKIPEDIA = "https://en.wikipedia.org/wiki/";

    private Skin skin;
    private CelestialBody focus;
    public Object monitor;
    public boolean executing = false;
    private LabelStyle linkStyle;
    private GaiaCatalogWindow gaiaWindow = null;

    // The table to modify
    private Table table;
    private float pad;

    public DesktopNetworkChecker() {
        super("NetworkThread");
        this.monitor = new Object();
        this.setDaemon(true);
    }

    @Override
    public boolean executing() {
        return executing;
    }

    @Override
    public void setParameters(Table table, Skin skin, float pad) {
        this.table = table;
        this.skin = skin;
        this.linkStyle = skin.get("link", LabelStyle.class);
        this.pad = pad;
    }

    public void setFocus(CelestialBody focus) {
        this.focus = focus;
    }

    public void doWait() {
        synchronized (monitor) {
            try {
                monitor.wait();
            } catch (InterruptedException e) {
                Logger.error(e);
            }
        }
    }

    public void doNotify() {
        synchronized (monitor) {
            monitor.notify();
        }
    }

    public void run() {
        try {
            while (true) {
                executing = false;
                doWait();
                executing = true;
                if (focus != null) {
                    Logger.debug(this.getClass().getSimpleName(), "Looking up network resources for '" + focus.name + "'");

                    String wikiname = focus.name.replace(' ', '_');

                    final String wikilink = getWikiLink(wikiname, focus);
                    final String simbadlink = getSimbadLink(focus);

                    Gdx.app.postRunnable(new FocusRunnable(focus, simbadlink, wikilink));

                    //                    pack();
                    focus = null;
                }
            }

        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private class FocusRunnable implements Runnable {
        String simbadlink, wikilink;
        CelestialBody focus;

        public FocusRunnable(CelestialBody focus, String simbadlink, String wikilink) {
            this.focus = focus;
            this.simbadlink = simbadlink;
            this.wikilink = wikilink;
        }

        @Override
        public void run() {
            if (focus instanceof Star) {
                Button gaiaButton = new OwnTextButton("Gaia", skin, "link");
                gaiaButton.addListener(new EventListener() {
                    @Override
                    public boolean handle(Event event) {
                        if (event instanceof ChangeEvent) {
                            if (gaiaWindow == null) {
                                gaiaWindow = new GaiaCatalogWindow(GaiaSky.instance.mainGui.getGuiStage(), skin);
                            }
                            gaiaWindow.initialize((Star) focus);
                            gaiaWindow.display();
                            return true;
                        }
                        return false;
                    }
                });
                table.add(gaiaButton);
            }
            if (simbadlink != null)
                table.add(new Link("Simbad", linkStyle, simbadlink)).padLeft(pad);
            if (wikilink != null)
                table.add(new Link("Wikipedia ", linkStyle, wikilink)).padLeft(pad);
        }

    }

    private String getSimbadLink(CelestialBody focus) {
        if (focus instanceof Star) {
            String url = URL_SIMBAD;
            Star st = (Star) focus;
            if (st.hip > 0) {
                return url + "HIP+" + st.hip;
            } else if (st.tycho != null) {
                return url + "TYC+" + st.tycho;
            }
        }
        return null;
    }

    private String[] suffixes = { "_(planet)", "_(moon)", "_(asteroid)", "_(dwarf_planet)", "_(spacecraft)" };

    private String getWikiLink(String wikiname, CelestialBody focus) {
        try {
            String url = URL_WIKIPEDIA;
            if (focus instanceof ModelBody) {
                ModelBody f = (ModelBody) focus;
                if (f.wikiname != null) {
                    return url + f.wikiname.replace(' ', '_');
                } else {
                    for (int i = 0; i < suffixes.length; i++) {
                        String suffix = suffixes[i];
                        if (HttpQuery.getResponseCode(url + wikiname + suffix) == 200)
                            return url + wikiname + suffix;

                    }
                }
            }
            if (HttpQuery.getResponseCode(url + wikiname) == 200)
                return url + wikiname;

        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

}
