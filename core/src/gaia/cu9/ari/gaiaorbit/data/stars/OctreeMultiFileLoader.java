package gaia.cu9.ari.gaiaorbit.data.stars;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Queue;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.data.ISceneGraphLoader;
import gaia.cu9.ari.gaiaorbit.data.octreegen.MetadataBinaryIO;
import gaia.cu9.ari.gaiaorbit.data.octreegen.ParticleDataBinaryIO;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;
import gaia.cu9.ari.gaiaorbit.scenegraph.Constellation;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.AbstractOctreeWrapper;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.OctreeWrapper;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.OctreeWrapperConcurrent;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.tree.LoadStatus;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

/**
 * Implements the loading and streaming of octree nodes from files.
 * 
 * @author tsagrista
 */
public class OctreeMultiFileLoader implements ISceneGraphLoader, IObserver {
    /**
     * Data will be pre-loaded at startup down to this octree depth.
     */
    private static final int PRELOAD_DEPTH = 3;

    /**
     * Default load queue size in octants
     */
    private static final int LOAD_QUEUE_MAX_SIZE = 100000;

    /** Singleton instance **/
    private static OctreeMultiFileLoader instance;

    /** Adds an octant to the queue to be loaded **/
    public static void addToQueue(OctreeNode octant) {
        // Add only if there is room
        if (instance.toLoadQueue.size() < LOAD_QUEUE_MAX_SIZE - 1) {
            instance.toLoadQueue.add(octant);
            octant.setStatus(LoadStatus.QUEUED);
        }
    }

    /** Adds a list of octants to the queue to be loaded **/
    public static void addToQueue(OctreeNode... octants) {
        if (instance != null)
            for (OctreeNode octant : octants) {
                addToQueue(octant);
            }

    }

    /** Puts it at the end of the toUnloadQueue **/
    public static void touch(OctreeNode octant) {
        if (instance != null) {
            if (instance.toUnloadQueue.contains(octant)) {
                instance.toUnloadQueue.remove(octant);
            }
            instance.toUnloadQueue.offer(octant);
        }
    }

    /**
     * Tells the loader to start loading the octants in the queue.
     */
    public static void flushLoadQueue() {
        if (instance != null && !instance.daemon.awake && !instance.toLoadQueue.isEmpty()) {
            instance.daemon.interrupt();
        }
    }

    /** Binary particle reader **/
    private ParticleDataBinaryIO particleReader;

    String metadata, particles;

    /** The octant loading queue **/
    private Queue<OctreeNode> toLoadQueue = null;
    /** Load status of the different levels of detail **/
    public LoadStatus[] lodStatus = new LoadStatus[50];

    /** Daemon thread that gets the data loading requests and serves them **/
    private DaemonLoader daemon;

    /** Current number of stars that are loaded **/
    int nLoadedStars = 0;
    /** Max number of stars loaded at once **/
    final int maxLoadedStars;
    /**
     * This queue is sorted ascending by access date, so that we know which
     * element to release if needed (oldest)
     **/
    private Queue<OctreeNode> toUnloadQueue;

    /** Loaded octant ids, for logging **/
    private long[] loadedIds;
    private int maxLoadedIds, idxLoadedIds;

    public OctreeMultiFileLoader() {
        instance = this;
        toLoadQueue = new ArrayBlockingQueue<OctreeNode>(LOAD_QUEUE_MAX_SIZE);
        toUnloadQueue = new ArrayBlockingQueue<OctreeNode>(LOAD_QUEUE_MAX_SIZE);
        particleReader = new ParticleDataBinaryIO();

        maxLoadedStars = 3000000;

        maxLoadedIds = 50;
        idxLoadedIds = 0;
        loadedIds = new long[maxLoadedIds];

        EventManager.instance.subscribe(this, Events.DISPOSE);
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case DISPOSE:
            if (daemon != null) {
                daemon.stopExecution();
            }
            break;
        default:
            break;
        }

    }

    @Override
    public Array<? extends SceneGraphNode> loadData() throws FileNotFoundException {

        /**
         * LOAD METADATA
         */

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.loading", metadata));

        MetadataBinaryIO metadataReader = new MetadataBinaryIO();
        OctreeNode root = metadataReader.readMetadata(Gdx.files.internal(metadata).read());

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", root.numNodes(), metadata));
        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.loading", particles));

        /**
         * CREATE OCTREE WRAPPER WITH ROOT NODE
         */
        AbstractOctreeWrapper octreeWrapper = null;
        if (GlobalConf.performance.MULTITHREADING) {
            octreeWrapper = new OctreeWrapperConcurrent("Universe", root);
        } else {
            octreeWrapper = new OctreeWrapper("Universe", root);
        }

        /**
         * LOAD LOD LEVELS - LOAD PARTICLE DATA
         */

        try {
            int depthLevel = Math.min(OctreeNode.maxDepth, PRELOAD_DEPTH);
            loadLod(depthLevel, octreeWrapper);
            flushLoadedIds();

            OctreeNode solOctant = root.getBestOctant(new Vector3d());
            boolean found = false;
            while (!found) {
                loadOctant(solOctant, octreeWrapper);
                SceneGraphNode sol = octreeWrapper.getNode("Sol");
                if (sol == null) {
                    solOctant = solOctant.parent;
                } else {
                    found = true;
                }
            }

        } catch (IOException e) {
            Logger.error(e);
        }

        /**
         * INITIALIZE DAEMON LOADER THREAD
         */
        daemon = new DaemonLoader(octreeWrapper, this);
        daemon.setDaemon(true);
        daemon.setName("daemon-octree-loader");
        daemon.setPriority(Thread.MIN_PRIORITY);
        daemon.start();

        /**
         * INITIALIZE TIMER TO FLUSH THE QUEUE AT REGULAR INTERVALS
         */
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                flushLoadQueue();
            }

        }, 1000, 2000);

        // Add octreeWrapper to result list and return
        Array<SceneGraphNode> result = new Array<SceneGraphNode>(1);
        result.add(octreeWrapper);

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.catalog.init", root.countObjects()));

        return result;
    }

    private void addLoadedId(long id) {
        if (idxLoadedIds >= maxLoadedIds) {
            flushLoadedIds();
        }
        loadedIds[idxLoadedIds++] = id;
    }

    private void flushLoadedIds() {
        if (idxLoadedIds > 0) {
            String str = "[" + loadedIds[0] + ", ..., " + loadedIds[idxLoadedIds - 1] + "]";
            Logger.info(I18n.bundle.format("notif.octantsloaded", idxLoadedIds, str));

            idxLoadedIds = 0;
        }

    }

    @Override
    public void initialize(String[] files) throws RuntimeException {
        if (files == null || files.length < 2) {
            throw new RuntimeException("Error loading octree files: " + files.length);
        }
        particles = files[0];
        metadata = files[1];
    }

    /**
     * Loads all the levels of detail until the given one.
     * 
     * @param lod
     *            The level of detail to load.
     * @param octreeWrapper
     *            The octree wrapper.
     * @throws IOException
     */
    public void loadLod(final Integer lod, final AbstractOctreeWrapper octreeWrapper) throws IOException {
        loadOctant(octreeWrapper.root, octreeWrapper, lod);

    }

    /**
     * Loads the data of the given octant
     * 
     * @param octant
     *            The octant to load.
     * @param octreeWrapper
     *            The octree wrapper.
     * @throws IOException
     */
    public void loadOctant(final OctreeNode octant, final AbstractOctreeWrapper octreeWrapper, Integer level) throws IOException {
        if (level >= 0) {
            loadOctant(octant, octreeWrapper);
            if (octant.children != null) {
                for (OctreeNode child : octant.children) {
                    if (child != null && child.nObjects > 0)
                        loadOctant(child, octreeWrapper, level - 1);
                }
            }
        }
    }

    /**
     * Loads the data of the given octant
     * 
     * @param octant
     *            The octant to load.
     * @param octreeWrapper
     *            The octree wrapper.
     * @return True if the octant was loaded, false otherwise
     * @throws IOException
     */
    public boolean loadOctant(final OctreeNode octant, final AbstractOctreeWrapper octreeWrapper) throws IOException {
        FileHandle octantFile = Gdx.files.internal(particles + "particles_" + String.format("%06d", octant.pageId) + ".bin");
        if (!octantFile.exists() || octantFile.isDirectory()) {
            return false;
        }
        Array<AbstractPositionEntity> data = particleReader.readParticles(octantFile.read());
        synchronized (octant) {
            for (AbstractPositionEntity star : data) {
                star.octant = octant;
                // Add objects to octree wrapper node
                octreeWrapper.add(star, octant);
                // Aux info
                if (GaiaSky.instance != null && GaiaSky.instance.sg != null)
                    GaiaSky.instance.sg.addNodeAuxiliaryInfo(star);
            }
            nLoadedStars += data.size;
            octant.objects = data;

            // Put it at the end of the queue
            touch(octant);

            octant.setStatus(LoadStatus.LOADED);

            addLoadedId(octant.pageId);
        }
        return true;

    }

    /**
     * Loads the objects of the given octants
     * 
     * @param octants
     *            The list holding the octants to load.
     * @param octreeWrapper
     *            The octree wrapper.
     * @return The actual number of loaded octants
     * @throws IOException
     */
    public int loadOctants(final Array<OctreeNode> octants, final AbstractOctreeWrapper octreeWrapper) throws IOException {
        int loaded = 0;
        for (OctreeNode octant : octants)
            if (loadOctant(octant, octreeWrapper))
                loaded++;
        flushLoadedIds();
        return loaded;
    }

    public void unloadOctant(OctreeNode octant, final AbstractOctreeWrapper octreeWrapper) {
        synchronized (octant) {
            Array<AbstractPositionEntity> objects = octant.objects;
            if (objects != null) {
                for (AbstractPositionEntity star : objects) {
                    star.octant = null;
                    octreeWrapper.removeParenthood(star);
                    // Aux info
                    if (GaiaSky.instance != null && GaiaSky.instance.sg != null)
                        GaiaSky.instance.sg.removeNodeAuxiliaryInfo(star);
                }
            }
            nLoadedStars -= objects.size;
            objects.clear();
            octant.setStatus(LoadStatus.NOT_LOADED);

        }
    }

    /**
     * The daemon loader thread.
     * 
     * @author Toni Sagrista
     *
     */
    private static class DaemonLoader extends Thread {
        public boolean awake;
        public boolean running;

        private OctreeMultiFileLoader loader;
        private AbstractOctreeWrapper octreeWrapper;
        private Array<OctreeNode> toLoad;

        public DaemonLoader(AbstractOctreeWrapper aow, OctreeMultiFileLoader loader) {
            this.awake = false;
            this.running = true;
            this.loader = loader;
            this.octreeWrapper = aow;
            this.toLoad = new Array<OctreeNode>();
        }

        public void stopExecution() {
            running = false;
        }

        @Override
        public void run() {
            while (running) {
                /** ----------- PROCESS OCTANTS ----------- **/
                while (!instance.toLoadQueue.isEmpty()) {
                    toLoad.clear();
                    while (instance.toLoadQueue.peek() != null) {
                        OctreeNode octant = (OctreeNode) instance.toLoadQueue.poll();
                        toLoad.add(octant);
                    }

                    // Load octants if any
                    if (toLoad.size > 0) {
                        Logger.debug(I18n.bundle.format("notif.loadingoctants", toLoad.size));
                        try {
                            int loaded = loader.loadOctants(toLoad, octreeWrapper);
                            Logger.debug(I18n.bundle.format("notif.loadingoctants.finished", loaded));
                        } catch (Exception e) {
                            Logger.error(I18n.bundle.get("notif.loadingoctants.fail"));
                        }
                    }

                    // Release resources if needed
                    while (loader.nLoadedStars >= loader.maxLoadedStars) {
                        // Get first in queue (unaccessed for the longest time)
                        // and release it
                        OctreeNode octant = loader.toUnloadQueue.poll();
                        if (octant.getStatus() == LoadStatus.LOADED) {
                            loader.unloadOctant(octant, octreeWrapper);
                        }
                    }

                    // Update constellations :S
                    Constellation.updateConstellations();
                }

                /** ----------- SLEEP UNTIL INTERRUPTED ----------- **/
                try {
                    awake = false;
                    Thread.sleep(Long.MAX_VALUE - 8);
                } catch (InterruptedException e) {
                    // New data!
                    awake = true;
                }
            }
        }
    }

}
