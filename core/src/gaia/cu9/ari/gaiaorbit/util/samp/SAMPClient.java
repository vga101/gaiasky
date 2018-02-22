package gaia.cu9.ari.gaiaorbit.util.samp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.astrogrid.samp.Message;
import org.astrogrid.samp.Metadata;
import org.astrogrid.samp.client.AbstractMessageHandler;
import org.astrogrid.samp.client.ClientProfile;
import org.astrogrid.samp.client.DefaultClientProfile;
import org.astrogrid.samp.client.HubConnection;
import org.astrogrid.samp.client.HubConnector;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.data.group.STILDataProvider;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.StarGroup;
import gaia.cu9.ari.gaiaorbit.scenegraph.StarGroup.StarBean;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;
import uk.ac.starlink.util.DataSource;
import uk.ac.starlink.util.URLDataSource;

public class SAMPClient implements IObserver {

    private static final String ENV_VAR = "SAMP_HUB";
    private static final String VAR_PREFIX = "std-lockurl:";
    private static final String LOCKFILE = ".samp";

    private static SAMPClient instance;

    public static SAMPClient getInstance() {
        if (instance == null)
            instance = new SAMPClient();
        return instance;
    }

    private HubConnector conn;
    private STILDataProvider provider;
    private Map<String, StarGroup> sgMap;

    public SAMPClient() {
        super();
        EventManager.instance.subscribe(this, Events.DISPOSE);
    }

    public void initialize() {
        // Disable logging
        java.util.logging.Logger.getLogger("org.astrogrid.samp").setLevel(Level.OFF);

        // Init provider
        provider = new STILDataProvider();
        
        // Init map
        sgMap = new HashMap<String, StarGroup>();

        ClientProfile cp = DefaultClientProfile.getProfile();
        HubConnector conn = new GaiaSkyHubConnector(cp);

        // Configure it with metadata about this application
        Metadata meta = new Metadata();
        meta.setName(GlobalConf.APPLICATION_NAME);
        meta.setDescriptionText("3D Universe application focused on ESA's Gaia satellite");
        meta.setDocumentationUrl(GlobalConf.DOCUMENTATION);
        meta.setIconUrl(GlobalConf.ICON_URL);
        meta.put("author.name", GlobalConf.AUTHOR_NAME);
        meta.put("author.email", GlobalConf.AUTHOR_EMAIL);
        meta.put("author.affiliation", GlobalConf.AUTHOR_AFFILIATION);
        meta.put("home.page", GlobalConf.WEBPAGE);
        meta.put("gaiasky.version", GlobalConf.version.version);

        conn.declareMetadata(meta);

        // Load table
        conn.addMessageHandler(new AbstractMessageHandler("table.load.votable") {
            public Map processCall(HubConnection c, String senderId, Message msg) {
                // do stuff
                String name = (String) msg.getParam("name");
                String id = (String) msg.getParam("table-id");
                String url = (String) msg.getParam("url");
                Logger.info("Load VOTable: " + msg.getParam("id"));

                try {
                    DataSource ds = new URLDataSource(new URL(url));
                    @SuppressWarnings("unchecked")
                    Array<StarBean> data = (Array<StarBean>) provider.loadData(ds, 1.0f);
                    StarGroup sg = new StarGroup();
                    sg.setName(id);
                    sg.setParent("Universe");
                    sg.setFadeout(new double[] { 21e2, .5e8 });
                    sg.setLabelcolor(new double[] { 1.0, 1.0, 1.0, 1.0 });
                    sg.setColor(new double[] { 1.0, 1.0, 1.0, 0.25 });
                    sg.setSize(6.0);
                    sg.setLabelposition(new double[] { 0.0, -5.0e7, -4e8 });
                    sg.setCt("Stars");
                    sg.setData(data);
                    sg.doneLoading(null);

                    sgMap.put(id, sg);

                    // Insert
                    Gdx.app.postRunnable(() -> {
                        GaiaSky.instance.sg.insert(sg, true);
                    });

                } catch (MalformedURLException e) {
                    Logger.error(e);
                }

                return null;
            }
        });

        // Select one row
        conn.addMessageHandler(new AbstractMessageHandler("table.highlight.row") {
            public Map processCall(HubConnection c, String senderId, Message msg) {
                // do stuff
                Long row = Parser.parseLong((String) msg.getParam("row"));
                String id = (String) msg.getParam("table-id");
                String url = (String) msg.getParam("url");
                Logger.info("Select row " + row + " of " + id);

                if (sgMap.containsKey(id)) {
                    StarGroup sg = sgMap.get(id);
                    sg.setFocusIndex(row.intValue());
                    EventManager.instance.post(Events.FOCUS_CHANGE_CMD, sg);
                }
                return null;
            }
        });

        // Select multiple rows
        conn.addMessageHandler(new AbstractMessageHandler("table.select.rowList") {
            public Map processCall(HubConnection c, String senderId, Message msg) {
                // do stuff
                Logger.info("Select rows");
                return null;
            }
        });

        // Point in sky
        conn.addMessageHandler(new AbstractMessageHandler("coord.pointAt.sky") {
            public Map processCall(HubConnection c, String senderId, Message msg) {
                // do stuff
                Logger.info("Point to coordinate");
                return null;
            }
        });

        // This step required even if no custom message handlers added.
        conn.declareSubscriptions(conn.computeSubscriptions());

        // Keep a look out for hubs if initial one shuts down
        conn.setAutoconnect(10);

        // Broadcast a message
        //conn.getConnection().notifyAll(new Message("stuff.event.doing"));

    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case DISPOSE:
            if (conn != null && conn.isConnected()) {
                conn.setActive(false);
            }
            break;
        }

    }

}
