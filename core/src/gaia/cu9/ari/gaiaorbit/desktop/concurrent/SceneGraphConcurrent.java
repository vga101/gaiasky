package gaia.cu9.ari.gaiaorbit.desktop.concurrent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractSceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.IFocus;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.camera.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.OctreeWrapperConcurrent;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

/**
 * Implementation of a 3D scene graph where the node updates takes place
 * concurrently in threads (as many as processors). This implementation takes
 * into account that one of the top-level nodes may be an Octree whose contents also
 * need to be parallelized at the top level.
 * 
 * @author Toni Sagrista
 * @deprecated Deprecated since star and particle groups are in GPU and parallelized by default.
 * Single stars in the object model not supported anymore.
 */
public class SceneGraphConcurrent extends AbstractSceneGraph {

    private ThreadPoolExecutor pool;
    private List<UpdaterTask<SceneGraphNode>> tasks;
    private OctreeWrapperConcurrent octree;
    private Array<SceneGraphNode> roulette;
    int numThreads;

    public SceneGraphConcurrent(int numThreads) {
        super();
        this.numThreads = numThreads;

    }

    /**
     * Builds the scene graph using the given nodes.
     * 
     * @param nodes
     *            The list of nodes
     * @param time
     *            The time provider
     * @param hasOctree
     *            Whether the list of nodes contains an octree
     * @param hasStarGroup
     *            Whether the list contains a star group
     */
    @Override
    public void initialize(Array<SceneGraphNode> nodes, ITimeFrameProvider time, boolean hasOctree, boolean hasStarGroup) {
        super.initialize(nodes, time, hasOctree, hasStarGroup);

        pool = ThreadPoolManager.pool;
        tasks = new ArrayList<UpdaterTask<SceneGraphNode>>(pool.getCorePoolSize());
        roulette = new Array<SceneGraphNode>(false, 1000000);

        Iterator<SceneGraphNode> it = nodes.iterator();
        while (it.hasNext()) {
            SceneGraphNode node = it.next();
            if (node instanceof OctreeWrapperConcurrent) {
                octree = (OctreeWrapperConcurrent) node;
                it.remove();
                octree.setRoulette(roulette);
                break;
            }
        }

        // Create the tasks with the roulette collections references
        for (int i = 0; i < numThreads; i++) {
            tasks.add(new UpdaterTask<SceneGraphNode>(roulette, i, numThreads));
        }

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.threadpool.init", numThreads));
    }

    public void update(ITimeFrameProvider time, ICamera camera) {
        super.update(time, camera);
        root.transform.position.set(camera.getInversePos());

        // Add top-level nodes to roulette
        roulette.addAll(root.children);
        roulette.removeValue(octree, true);

        // Update octree if present - Add nodes to process to roulette
        if (octree != null)
            octree.update(time, root.transform, camera, 1f);

        // Update params
        int size = tasks.size();
        for (int i = 0; i < size; i++) {
            UpdaterTask<SceneGraphNode> task = tasks.get(i);
            task.setParameters(camera, time);
        }

        try {
            pool.invokeAll(tasks);
        } catch (InterruptedException e) {
            Logger.error(e);
        }

        // Update focus, just in case
        IFocus focus = camera.getFocus();
        if (focus != null) {
            SceneGraphNode star = focus.getFirstStarAncestor();
            OctreeNode parent = octree != null ? octree.parenthood.get(star) : null;
            if (parent != null && !parent.isObserved()) {
                star.update(time, star.parent.transform, camera);
            }
        }

        // Debug info
        EventManager.instance.post(Events.DEBUG3, "Objs/thd, Total objs, threads: " + getRouletteDebug());

        // Clear roulette
        roulette.clear();
    }

    public void dispose() {
        super.dispose();
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(3, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(3, TimeUnit.SECONDS))
                    Logger.error("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }

    protected String getRouletteDebug() {
        int size = roulette.size / numThreads;
        return size + ", " + roulette.size + ", " + numThreads;
    }

}
