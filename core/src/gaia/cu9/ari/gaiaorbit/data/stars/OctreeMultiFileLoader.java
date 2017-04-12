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
 */
public class OctreeMultiFileLoader implements ISceneGraphLoader {
    /**
     * Data will be pre-loaded at startup down to this octree depth.
     */
    private static final int PRELOAD_DEPTH = 4;

    /** Singleton instance **/
    private static OctreeMultiFileLoader instance;

    /** Adds an octant to the queue to be loaded **/
    public static void addToQueue(OctreeNode octant) {
        if (instance != null && octant != null) {
            instance.toLoadQueue.add(octant);
            octant.setStatus(LoadStatus.QUEUED);
        }
    }

    /** Adds a list of octants to the queue to be loaded **/
    public static void addToQueue(OctreeNode... octants) {
        if (instance != null)
            for (OctreeNode<Particle> octant : octants) {
                instance.toLoadQueue.add(octant);
                octant.setStatus(LoadStatus.QUEUED);
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
    private ParticleDataBinaryIO<Particle> particleReader;

    String metadata, particles;

    /** The octant loading queue **/
    private Queue<OctreeNode<Particle>> toLoadQueue = null;
    /** Load status of the different levels of detail **/
    public LoadStatus[] lodStatus = new LoadStatus[50];

    /** Daemon thread that gets the data loading requests and serves them **/
    private DaemonLoader daemon;

    /** Current number of stars that are loaded **/
    int nLoadedStars = 0;
    /** Max number of stars loaded at once **/
    final int maxLoadedStars;
    /** This queue is sorted ascending by access date, so that we know which element to release if needed (oldest) **/
    private Queue<OctreeNode<Particle>> toUnloadQueue;

    public OctreeMultiFileLoader() {
        instance = this;
        toLoadQueue = new ArrayBlockingQueue<OctreeNode<Particle>>(4000);

        toUnloadQueue = new ArrayBlockingQueue<OctreeNode<Particle>>(4000);
        particleReader = new ParticleDataBinaryIO();

        maxLoadedStars = 300000;
    }

    @Override
    public Array<? extends SceneGraphNode> loadData() throws FileNotFoundException {

        /**
         * LOAD METADATA
         */

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.loading", metadata));

        MetadataBinaryIO<Particle> metadataReader = new MetadataBinaryIO<Particle>();
        OctreeNode<Particle> root = metadataReader.readMetadata(Gdx.files.internal(metadata).read());

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

            // Load octant with Sol - 608
            OctreeNode<Particle> solOctant = root.findOctant(608l);
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
    public void loadLod(final Integer lod, final AbstractOctreeWrapper octreeWrapper) throws IOException {
        loadOctant(octreeWrapper.root, octreeWrapper, lod);

    }

    /**
     * Loads the data of the given octant into the given list from the visualization identified by <tt>visid</tt>
     * @param octant The octant to load.
     * @param octreeWrapper The octree wrapper.
     * @throws IOException
     */
    public void loadOctant(final OctreeNode<Particle> octant, final AbstractOctreeWrapper octreeWrapper, Integer level) throws IOException {
        if (level >= 0) {
            loadOctant(octant, octreeWrapper);
            if (octant.children != null) {
                for (OctreeNode<Particle> child : octant.children) {
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
    public void loadOctant(final OctreeNode<Particle> octant, final AbstractOctreeWrapper octreeWrapper) throws IOException {
        FileHandle octantFile = Gdx.files.internal(particles + "particles_" + String.format("%06d", octant.pageId) + ".bin");
        if (!octantFile.exists() || octantFile.isDirectory()) {
            return;
        }
        Logger.info(I18n.bundle.format("notif.loadingoctant", octant.pageId));

        Array<Particle> data = particleReader.readParticles(octantFile.read());
        synchronized (octant) {
            for (Particle star : data) {
                star.octant = octant;
                // Add objects to octree wrapper node
                octreeWrapper.add(star, octant);
            }
            nLoadedStars += data.size;
            octant.objects = data;

            // Put it at the end of the queue
            touch(octant);

            octant.setStatus(LoadStatus.LOADED);
        }
    }

    /**
     * Loads the objects of the given octants
     * @param octants The list holding the octants to load.
     * @param octreeWrapper The octree wrapper.
     * @throws IOException
     */
    public void loadOctants(final Array<OctreeNode> octants, final AbstractOctreeWrapper octreeWrapper) throws IOException {
        for (OctreeNode octant : octants)
            loadOctant(octant, octreeWrapper);

    }

    public void unloadOctant(OctreeNode octant, final AbstractOctreeWrapper octreeWrapper) {
        synchronized (octant) {
            Array<SceneGraphNode> objects = octant.objects;
            if (objects != null) {
                for (SceneGraphNode star : objects) {
                    ((Particle) star).octant = null;
                    octreeWrapper.removeParenthood(star);
                }
            }
            nLoadedStars -= objects.size;
            objects.clear();
            octant.setStatus(LoadStatus.NOT_LOADED);

        }
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
        private Array<OctreeNode> toLoad;

        public DaemonLoader(AbstractOctreeWrapper aow, OctreeMultiFileLoader loader) {
            this.loader = loader;
            this.octreeWrapper = aow;
            this.toLoad = new Array<OctreeNode>();
        }

        @Override
        public void run() {
            while (true) {
                /** ----------- PROCESS OCTANTS ----------- **/
                while (!instance.toLoadQueue.isEmpty()) {
                    toLoad.clear();
                    int n = 0;
                    while (instance.toLoadQueue.peek() != null) {
                        OctreeNode octant = (OctreeNode) instance.toLoadQueue.poll();
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

                    // Release resources if needed
                    System.out.println("Loaded stars (before): " + loader.nLoadedStars);
                    while (loader.nLoadedStars >= loader.maxLoadedStars) {
                        // Get first in queue (unaccessed for the longest time) and release it
                        OctreeNode octant = loader.toUnloadQueue.poll();
                        if (octant.getStatus() == LoadStatus.LOADED) {
                            loader.unloadOctant(octant, octreeWrapper);
                        }
                    }
                    System.out.println("Loaded stars (after): " + loader.nLoadedStars);
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
