package gaia.cu9.ari.gaiaorbit.desktop.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import gaia.cu9.ari.gaiaorbit.desktop.network.HttpQuery;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;
import gaia.cu9.ari.gaiaorbit.util.scene2d.CollapsibleWindow;
import gaia.cu9.ari.gaiaorbit.util.scene2d.Link;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnScrollPane;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;

public class GaiaCatalogWindow extends CollapsibleWindow {
    private static String URL_WEB = "http://gaia.esac.esa.int/archive/";
    private static String URL_GAIA_CSV_SOURCE = "http://gaia.esac.esa.int/tap-server/tap/sync?REQUEST=doQuery&LANG=ADQL&FORMAT=csv&QUERY=SELECT+*+FROM+gaia_source+WHERE+source_id=";
    private static String URL_GAIA_JSON_SOURCE = "http://gaia.esac.esa.int/tap-server/tap/sync?REQUEST=doQuery&LANG=ADQL&FORMAT=json&QUERY=SELECT+*+FROM+gaia_source+WHERE+source_id=";
    private static String URL_GAIA_HIP = "http://gaia.esac.esa.int/tap-server/tap/sync?REQUEST=doQuery&LANG=ADQL&FORMAT=csv&QUERY=SELECT+*+FROM+gaia_hip_tycho2_match+WHERE+ext_cat_solution_type='5'+AND+hyp_tyc_oid=";
    private static final String separator = "\n";

    private final Stage stage;
    private HorizontalGroup buttonGroup;
    private Table table;
    private OwnScrollPane scroll;

    private LabelStyle linkStyle;

    private Star st;

    public GaiaCatalogWindow(Stage stg, Skin skin) {
        super(I18n.bundle.get("gui.data.catalog"), skin);

        this.stage = stg;
        this.linkStyle = skin.get("link", LabelStyle.class);

        /** BUTTONS **/
        buttonGroup = new HorizontalGroup();
        TextButton ok = new OwnTextButton(txt("gui.close"), skin, "default");
        ok.setName("close");
        ok.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    me.remove();
                    return true;
                }

                return false;
            }

        });
        ok.setSize(70, 20);
        buttonGroup.addActor(ok);

        /** TABLE and SCROLL **/
        table = new Table(skin);
        scroll = new OwnScrollPane(table, skin, "minimalist-nobg");
        scroll.setFadeScrollBars(false);
        scroll.setScrollingDisabled(true, false);
        scroll.setOverscroll(false, false);
        scroll.setSmoothScrolling(true);

        add(scroll);
        row();
        add(buttonGroup).colspan(2).pad(5, 5, 5, 5).bottom().right();
        getTitleTable().align(Align.left);

        pack();

        /** CAPTURE SCROLL FOCUS **/
        stage.addListener(new EventListener() {

            @Override
            public boolean handle(Event event) {
                if (event instanceof InputEvent) {
                    InputEvent ie = (InputEvent) event;

                    if (ie.getType() == Type.mouseMoved) {
                        if (scroll != null) {
                            if (ie.getTarget().isDescendantOf(scroll)) {
                                stage.setScrollFocus(scroll);
                            }
                            return true;
                        }
                    }
                }
                return false;
            }
        });

    }

    public void initialize(Star st) {
        this.st = st;

        table.clear();

        // Make request
        try {
            String[][] data = getData();
            if (data != null) {

                HorizontalGroup links = new HorizontalGroup();
                links.align(Align.center);
                links.pad(5, 5, 5, 5);
                links.space(10);

                links.addActor(new Link(txt("gui.data.json"), linkStyle, URL_GAIA_JSON_SOURCE + st.id));
                links.addActor(new OwnLabel("|", skin));
                links.addActor(new Link(txt("gui.data.archive"), linkStyle, URL_WEB));

                table.add(links).colspan(2).padTop(10).padBottom(10);
                table.row();

                table.add(new OwnLabel(txt("gui.data.name"), skin, "msg-17")).padLeft(10).left();
                table.add(new OwnLabel(st.name, skin, "msg-17")).padLeft(10).padRight(10).left();
                table.row();
                for (int col = 0; col < data[0].length; col++) {
                    Actor first = null;

                    if (data.length <= 2) {
                        first = new OwnLabel(data[0][col], skin, "ui-13");
                    } else {
                        HorizontalGroup hg = new HorizontalGroup();
                        hg.space(5);
                        ImageButton tooltip = new ImageButton(skin, "tooltip");
                        tooltip.addListener(new TextTooltip(data[2][col], skin));
                        hg.addActor(tooltip);
                        hg.addActor(new OwnLabel(data[0][col], skin, "ui-13"));

                        first = hg;

                    }

                    table.add(first).padLeft(10).left();
                    table.add(new OwnLabel(data[1][col], skin, "ui-12")).padLeft(10).padRight(10).left();
                    left();
                    table.row();
                }
                scroll.setHeight(Gdx.graphics.getHeight() * 0.7f);
                scroll.pack();

            } else {
                // Message
                String msg = I18n.bundle.format("error.gaiacatalog.notfound", st.name);
                table.add(new OwnLabel(msg, skin, "ui-15"));
            }
        } catch (Exception e) {
            table.add(new OwnLabel(e.getLocalizedMessage(), skin, "ui-15"));
        }
        table.pack();
        scroll.pack();
        scroll.setWidth(table.getWidth() + scroll.getStyle().vScroll.getMinWidth());

        pack();
        //this.setPosition(stage.getWidth() / 2f - this.getWidth() / 2f, stage.getHeight() / 2f - this.getHeight() / 2f);

    }

    public void display() {
        if (!stage.getActors().contains(me, true))
            stage.addActor(me);
    }

    private String[][] getData() throws MalformedURLException, IOException {
        if (st.catalogSource > 0) {

            if (st.catalogSource == 1) {
                // GAIA
                return getDataBySourceId(st.id);
            } else if (st.catalogSource == 2 && st.hip > 0) {
                // HIPPARCOS
                // Get sourceId corresponding to HIP number
                return getDataByHipId(st.hip);

            }

        }
        return null;
    }

    private String[][] getDataBySourceId(long sourceid) throws MalformedURLException, IOException {
        return getTAPData(URL_GAIA_JSON_SOURCE + Long.toString(sourceid), "json");
    }

    private String[][] getDataByHipId(int hip) throws MalformedURLException, IOException {
        String[][] xmatch = getTAPData(URL_GAIA_HIP + Integer.toString(hip), "csv");

        if (xmatch != null) {
            long sourceid = -1;
            for (int col = 0; col < xmatch[0].length; col++) {
                if (xmatch[0][col].equalsIgnoreCase("source_id")) {
                    sourceid = Parser.parseLong(xmatch[1][col]);
                    break;
                }
            }

            if (sourceid > 0) {
                Logger.info("Hip Id: " + hip + ", Gaia Source Id: " + sourceid);
                st.id = sourceid;
                return getDataBySourceId(sourceid);
            }
        }
        return null;

    }

    private String[][] getTAPData(String url, String format) throws MalformedURLException, IOException {
        InputStream is = HttpQuery.httpRequest(url);
        String data = slurp(is, 2046);

        if (format.equalsIgnoreCase("csv")) {
            /** PARSE CSV **/
            String[] rows = data.split(separator);
            if (rows.length <= 1) {
                return null;
            }

            String[][] matrix = new String[rows.length][];

            int r = 0;
            for (String row : rows) {
                String[] tokens = row.split(",");
                int i = 0;
                for (String token : tokens) {
                    token = token.trim();
                    tokens[i] = trim(token, "\"");
                    i++;
                }

                matrix[r] = tokens;
                r++;
            }

            return matrix;
        } else if (format.equalsIgnoreCase("json")) {
            /** PARSE JSON **/
            JsonReader json = new JsonReader();
            JsonValue root = json.parse(data);

            JsonValue metadata = root.child;
            int size = metadata.size;
            JsonValue column = metadata.child;
            String[] colnames = new String[size];
            String[] descriptions = new String[size];
            String[] values = new String[size];

            int i = 0;
            do {
                colnames[i] = column.getString("name");
                descriptions[i] = column.getString("description");
                i++;
                column = column.next;
            } while (column != null);

            JsonValue datacol = metadata.next.child.child;
            i = 0;
            do {
                values[i] = datacol.asString();
                i++;
                datacol = datacol.next;
            } while (datacol != null);

            String[][] matrix = new String[3][];
            matrix[0] = colnames;
            matrix[1] = values;
            matrix[2] = descriptions;
            return matrix;
        }

        return null;
    }

    public String trim(String stringToTrim, String stringToRemove) {
        String answer = stringToTrim;

        while (answer.startsWith(stringToRemove)) {
            answer = answer.substring(stringToRemove.length());
        }

        while (answer.endsWith(stringToRemove)) {
            answer = answer.substring(0, answer.length() - stringToRemove.length());
        }

        return answer;
    }

    public static String slurp(final InputStream is, final int bufferSize) {
        final char[] buffer = new char[bufferSize];
        final StringBuilder out = new StringBuilder();
        try (Reader in = new InputStreamReader(is, "UTF-8")) {
            for (;;) {
                int rsz = in.read(buffer, 0, buffer.length);
                if (rsz < 0)
                    break;
                out.append(buffer, 0, rsz);
            }
        } catch (UnsupportedEncodingException ex) {
            Logger.error(ex);
        } catch (IOException ex) {
            Logger.error(ex);
        }
        return out.toString();
    }

    protected String txt(String key) {
        return I18n.bundle.get(key);
    }

    protected String txt(String key, Object... params) {
        return I18n.bundle.format(key, params);
    }

}
