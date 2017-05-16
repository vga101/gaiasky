package gaia.cu9.ari.gaiaorbit.desktop.util;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Net.HttpMethods;
import com.badlogic.gdx.Net.HttpRequest;
import com.badlogic.gdx.Net.HttpResponse;
import com.badlogic.gdx.Net.HttpResponseListener;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Net;
import com.badlogic.gdx.net.HttpStatus;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.interfce.INetworkChecker;
import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.ModelBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.scene2d.Link;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;

public class DesktopNetworkChecker extends Thread implements INetworkChecker {
    private static String URL_SIMBAD = "http://simbad.u-strasbg.fr/simbad/sim-id?Ident=";
    // TODO Use Wikipedia API to get localized content to the current language
    private static String URL_WIKIPEDIA = "https://en.wikipedia.org/wiki/";

    private static int TIMEOUT_MS = 5000;

    private boolean running = true;

    private Skin skin;
    private CelestialBody focus;
    public Object monitor;
    public boolean executing = false;
    private LabelStyle linkStyle;
    private GaiaCatalogWindow gaiaWindow = null;

    private Cell<Link> wikiCell, simbadCell;
    private Link wikiLink, simbadLink;

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

    public void stopExecution() {
        running = false;
        doNotify();
    }

    private class GaiaButtonListener implements EventListener {
        private final CelestialBody focus;

        public GaiaButtonListener(CelestialBody focus) {
            super();
            this.focus = focus;
        }

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
    }

    public void run() {
        try {
            while (running) {
                table.align(Align.top | Align.left);
                executing = false;
                doWait();
                if (!running)
                    break;
                executing = true;
                if (focus != null) {
                    Logger.debug(this.getClass().getSimpleName(), "Looking up network resources for '" + focus.name + "'");

                    // Add table
                    if (focus instanceof Star) {
                        Button gaiaButton = new OwnTextButton("Gaia", skin, "link");
                        gaiaButton.addListener(new GaiaButtonListener(focus));
                        table.add(gaiaButton).center();
                    }

                    simbadLink = new Link("Simbad", linkStyle, "");
                    wikiLink = new Link("Wikipedia ", linkStyle, "");

                    simbadCell = table.add().center();
                    wikiCell = table.add().center();

                    String wikiname = focus.name.replace(' ', '_');

                    setWikiLink(wikiname, focus, new LinkListener() {
                        @Override
                        public void ok(String link) {
                            wikiLink.setLinkURL(link);
                            wikiCell.setActor(wikiLink);
                            wikiCell.padLeft(pad);
                        }

                        @Override
                        public void ko(String link) {
                        }
                    });
                    setSimbadLink(focus, new LinkListener() {

                        @Override
                        public void ok(String link) {
                            simbadLink.setLinkURL(link);
                            simbadCell.setActor(simbadLink);
                            simbadCell.padLeft(pad);
                        }

                        @Override
                        public void ko(String link) {
                        }

                    });

                    focus = null;
                }
            }

        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private void setSimbadLink(CelestialBody focus, LinkListener listener) {
        if (focus instanceof Star) {
            String url = URL_SIMBAD;
            Star st = (Star) focus;
            if (st.hip > 0) {
                listener.ok(url + "HIP+" + st.hip);
            } else if (st.tycho != null) {
                listener.ok(url + "TYC+" + st.tycho);
            } else {
                listener.ko(null);
            }
        } else {
            listener.ko(null);
        }
    }

    private String[] suffixes = { "_(planet)", "_(moon)", "_(asteroid)", "_(dwarf_planet)", "_(spacecraft)" };

    private void setWikiLink(String wikiname, CelestialBody focus, LinkListener listener) {
        try {
            String url = URL_WIKIPEDIA;
            if (focus instanceof ModelBody) {
                ModelBody f = (ModelBody) focus;
                if (f.wikiname != null) {
                    listener.ok(url + f.wikiname.replace(' ', '_'));
                } else {
                    for (int i = 0; i < suffixes.length; i++) {
                        String suffix = suffixes[i];
                        urlCheck(url + wikiname + suffix, listener);
                    }
                }
            }
            urlCheck(url + wikiname, listener);

        } catch (Exception e) {
            Logger.error(e);
        }
        listener.ko(null);
    }

    private void urlCheck(final String url, final LinkListener listener) {
        HttpRequest request = new HttpRequest(HttpMethods.GET);
        request.setUrl(url);
        request.setTimeOut(TIMEOUT_MS);
        Gdx.net.sendHttpRequest(request, new HttpResponseListener() {
            @Override
            public void handleHttpResponse(HttpResponse httpResponse) {
                if (httpResponse.getStatus().getStatusCode() == HttpStatus.SC_OK) {
                    listener.ok(url);
                } else {
                    listener.ko(url);
                }
            }

            @Override
            public void failed(Throwable t) {
                listener.ko(url);
            }

            @Override
            public void cancelled() {
                listener.ko(url);
            }
        });

    }

    public static void main(String[] args) {
        Gdx.net = new Lwjgl3Net();
        DesktopNetworkChecker dnc = new DesktopNetworkChecker();
        dnc.urlCheck("https://ca.ba.de.si.com", new LinkListener() {
            @Override
            public void ok(String link) {
                System.out.println("ok : " + link);
            }

            @Override
            public void ko(String link) {
                System.out.println("ko : " + link);
            }
        });
        dnc.urlCheck("https://www.google.com", new LinkListener() {
            @Override
            public void ok(String link) {
                System.out.println("ok : " + link);
            }

            @Override
            public void ko(String link) {
                System.out.println("ko : " + link);
            }
        });

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private interface LinkListener {
        public void ok(String link);

        public void ko(String link);
    }
}
