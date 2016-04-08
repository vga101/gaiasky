package gaia.cu9.ari.gaiaorbit.desktop.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import gaia.cu9.ari.gaiaorbit.desktop.network.HttpQuery;
import gaia.cu9.ari.gaiaorbit.interfce.INetworkChecker;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ModelBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.scene2d.Link;

public class DesktopNetworkChecker extends Thread implements INetworkChecker {
    private CelestialBody focus;
    public Object monitor;
    public boolean executing = false;
    private LabelStyle linkStyle;

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
                    Logger.info(this.getClass().getSimpleName(), "Looking up network resources for '" + focus.name + "'");

                    String wikiname = focus.name.replace(' ', '_');

                    final String gaialink = getGaiaLink(focus);
                    final String wikilink = getWikiLink(wikiname, focus);
                    final String simbadlink = getSimbadLink(focus);

                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            if (gaialink != null)
                                table.add(new Link("Gaia", linkStyle, gaialink));
                            if (simbadlink != null)
                                table.add(new Link("Simbad", linkStyle, simbadlink)).padLeft(pad);
                            if (wikilink != null)
                                table.add(new Link("Wikipedia ", linkStyle, wikilink)).padLeft(pad);
                        }

                    });

                    //                    pack();
                    focus = null;
                }
            }

        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private String getGaiaLink(CelestialBody focus) {
        if (focus instanceof Star) {

        }
        return null;
    }

    private String getSimbadLink(CelestialBody focus) {
        String url = "http://simbad.u-strasbg.fr/simbad/sim-id?Ident=";
        if (focus instanceof Star) {
            Star st = (Star) focus;
            if (st.hip > 0) {
                return url + "HIP+" + st.hip;
            } else if (st.tychostr != null) {
                return url + "TYC+" + st.tychostr;
            }
        }
        return null;
    }

    private String[] suffixes = { "_(planet)", "_(moon)", "_(asteroid)", "_(dwarf_planet)", "_(spacecraft)" };

    private String getWikiLink(String wikiname, CelestialBody focus) {
        try {
            if (focus instanceof ModelBody) {
                ModelBody f = (ModelBody) focus;
                if (f.wikiname != null) {
                    return "https://en.wikipedia.org/wiki/" + f.wikiname.replace(' ', '_');
                } else {
                    for (int i = 0; i < suffixes.length; i++) {
                        String suffix = suffixes[i];
                        if (HttpQuery.getResponseCode("https://en.wikipedia.org/wiki/" + wikiname + suffix) == 200)
                            return "https://en.wikipedia.org/wiki/" + wikiname + suffix;

                    }
                }
            }
            if (HttpQuery.getResponseCode("https://en.wikipedia.org/wiki/" + wikiname) == 200)
                return "https://en.wikipedia.org/wiki/" + wikiname;

        } catch (Exception e) {
            Logger.error(e);
        }
        return null;
    }

}
