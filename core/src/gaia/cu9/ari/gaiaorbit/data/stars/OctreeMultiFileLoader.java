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

import gaia.cu9.ari.gaiaorbit.data.ISceneGraphLoader;
import gaia.cu9.ari.gaiaorbit.data.octreegen.MetadataBinaryIO;
import gaia.cu9.ari.gaiaorbit.data.octreegen.ParticleDataBinaryIO;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.AbstractOctreeWrapper;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.OctreeWrapper;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.OctreeWrapperConcurrent;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.tree.LoadStatus;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

/**
 * Implements the loading and streaming of octree nodes from files.
 * @author tsagrista
 *
 */
public class OctreeMultiFileLoader implements ISceneGraphLoader {
    /**
     * Data will be pre-loaded at startup down to this octree depth.
     */
    private static final int PRELOAD_DEPTH = 4;
    /**
     * Max number of pages to retrieve at once
     */
    private static final int MAX_PAGES_AT_ONCE = 500;

    /** The octant loading queue **/
    private static Queue<OctreeNode<?>> octantQueue = new ArrayBlockingQueue<OctreeNode<?>>(40000);
    /** Load status of the different levels of detail **/
    public static LoadStatus[] lodStatus = new LoadStatus[50];

    /** Daemon thread that gets the data loading requests and serves them **/
    private static DaemonLoader daemon;

    /** Adds an octant to the queue to be loaded **/
    public static void addToQueue(OctreeNode<?> object) {
        octantQueue.add(object);
        ((OctreeNode<?>) object).setStatus(LoadStatus.QUEUED);
    }

    /** Adds a list of octants to the queue to be loaded **/
    public static void addToQueue(OctreeNode<?>... octants) {
        for (OctreeNode<?> octant : octants) {
            if (octant != null && octant.getStatus() == LoadStatus.NOT_LOADED) {
                octantQueue.add(octant);
                octant.setStatus(LoadStatus.QUEUED);
            }
        }
    }

    /**
     * Tells the loader to start loading the octants in the queue.
     */
    public static void flushLoadQueue() {
        if (!daemon.awake && !octantQueue.isEmpty()) {
            daemon.interrupt();
        }
    }

    /** Binary particle reader **/
    private ParticleDataBinaryIO particleReader;

    String metadata, particles;

    @Override
    public Array<? extends SceneGraphNode> loadData() throws FileNotFoundException {

        /**
         * LOAD METADATA
         */

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.loading", metadata));

        MetadataBinaryIO metadataReader = new MetadataBinaryIO();
        OctreeNode<SceneGraphNode> root = (OctreeNode<SceneGraphNode>) metadataReader.readMetadata(Gdx.files.internal(metadata).read());

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
        particleReader = new ParticleDataBinaryIO();
        try {
            int depthLevel = Math.min(OctreeNode.maxDepth, PRELOAD_DEPTH);
            loadLod(depthLevel, octreeWrapper);

            // Load octant with Sol - 608
            OctreeNode<SceneGraphNode> solOctant = root.findOctant(608l);
            if (solOctant != null) {
                loadOctant(solOctant, octreeWrapper);
            }

        } catch (IOException e) {
            Logger.error(e);
        }

        /**
         * INITIALIZE DAEMON LOADER THREAD
         */
        daemon = new DaemonLoader(octreeWrapper, this);
        daemon.setDaemon(true);
        daemon.setName("daemon-objectserver-loader");
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

    @Override
    public void initialize(String[] files) throws RuntimeException {
        if (files == null || files.length < 2) {
            throw new RuntimeException("Error loading octree files: " + files.length);
        }
        particles = files[0];
        metadata = files[1];
    }

    /**
     * Loads all the levels of detail unitl the given one.
     * @param lod The level of detail to load.
     * @param octreeWrapper The octree wrapper.
     * @throws IOException 
     */
    public synchronized void loadLod(final Integer lod, final AbstractOctreeWrapper octreeWrapper) throws IOException {
        loadOctant(octreeWrapper.root, octreeWrapper, lod);

    }

    /**
     * Loads the data of the given octant into the given list from the visualization identified by <tt>visid</tt>
     * @param octant The octant to load.
     * @param octreeWrapper The octree wrapper.
     * @throws IOException
     */
    public synchronized void loadOctant(final OctreeNode<SceneGraphNode> octant, final AbstractOctreeWrapper octreeWrapper, Integer level) throws IOException {
        if (level >= 0) {
            loadOctant(octant, octreeWrapper);
            if (octant.children != null) {
                for (OctreeNode<SceneGraphNode> child : octant.children) {
                    if (child != null && child.nObjects > 0)
                        loadOctant(child, octreeWrapper, level - 1);
                }
            }
        }
    }

    /**
     * Loads the data of the given octant into the given list from the visualization identified by <tt>visid</tt>
     * @param octant The octant to load.
     * @param octreeWrapper The octree wrapper.
     * @throws IOException
     */
    public synchronized void loadOctant(final OctreeNode<SceneGraphNode> octant, final AbstractOctreeWrapper octreeWrapper) throws IOException {
        FileHandle octantFile = Gdx.files.internal(particles + "particles_" + String.format("%06d", octant.pageId) + ".bin");
        if (!octantFile.exists() || octantFile.isDirectory()) {
            return;
        }
        Logger.info(I18n.bundle.format("notif.loadingoctant", octant.pageId));

        Array<SceneGraphNode> data = particleReader.readParticles(octantFile.read());
        synchronized (octant) {
            for (SceneGraphNode star : data) {
                ((Particle) star).octant = octant;
                // Add objects to octree wrapper node
                octreeWrapper.add(star, octant);
            }

            octant.objects = data;
            octant.setStatus(LoadStatus.LOADED);
        }
    }

    /**
     * Loads the objects of the given octants using the given list from the visualization identified by <tt>visid</tt>
     * @param octants The list holding the octants to load.
     * @param octreeWrapper The octree wrapper.
     * @throws IOException
     */
    public synchronized void loadOctants(final Array<OctreeNode<SceneGraphNode>> octants, final AbstractOctreeWrapper octreeWrapper) throws IOException {
        for (OctreeNode<SceneGraphNode> octant : octants)
            loadOctant(octant, octreeWrapper);

    }

    /**
     * The daemon loader thread.
     * @author Toni Sagrista
     *
     */
    private static class DaemonLoader extends Thread {
        public boolean awake = false;

        private OctreeMultiFileLoader loader;
        private AbstractOctreeWrapper octreeWrapper;
        private Array<OctreeNode<SceneGraphNode>> toLoad;

        public DaemonLoader(AbstractOctreeWrapper aow, OctreeMultiFileLoader loader) {
            this.loader = loader;
            this.octreeWrapper = aow;
            this.toLoad = new Array<OctreeNode<SceneGraphNode>>();
        }

        @Override
        public void run() {
            while (true) {
                /** ----------- PROCESS OCTANTS ----------- **/
                while (!octantQueue.isEmpty()) {
                    toLoad.clear();
                    int n = 0;
                    while (octantQueue.peek() != null && n < MAX_PAGES_AT_ONCE) {
                        OctreeNode<SceneGraphNode> octant = (OctreeNode<SceneGraphNode>) octantQueue.poll();
                        toLoad.add(octant);
                        n++;
                    }

                    // Load octants if any
                    if (toLoad.size > 0) {
                        EventManager.instance.post(Events.POST_NOTIFICATION, I18n.bundle.format("notif.loadingoctants", toLoad.size), true);
                        try {
                            loader.loadOctants(toLoad, octreeWrapper);
                            EventManager.instance.post(Events.POST_NOTIFICATION, I18n.bundle.format("notif.loadingoctants.finished", toLoad.size));
                        } catch (Exception e) {
                            EventManager.instance.post(Events.JAVA_EXCEPTION, e);
                            EventManager.instance.post(Events.POST_NOTIFICATION, I18n.bundle.get("notif.loadingoctants.fail"));
                        }
                    }
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
