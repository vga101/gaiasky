package gaia.cu9.ari.gaiaorbit.data;

import gaia.cu9.ari.gaiaorbit.desktop.concurrent.SceneGraphConcurrent;
import gaia.cu9.ari.gaiaorbit.desktop.concurrent.SceneGraphConcurrentOctree;
import gaia.cu9.ari.gaiaorbit.scenegraph.ISceneGraph;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraph;

public class DesktopSceneGraphImplementationProvider extends SceneGraphImplementationProvider {

    @Override
    public ISceneGraph getImplementation(boolean multithreading, boolean hasOctree, boolean hasStarGroup, int maxThreads) {
        ISceneGraph sg = null;
        if (multithreading && !hasStarGroup) {
            if (!hasOctree) {
                // No octree, local data
                sg = new SceneGraphConcurrent(maxThreads);
            } else {
                // Object server, we use octree mode
                sg = new SceneGraphConcurrentOctree(maxThreads);
            }
        } else {
            // Multithreading is off or star group is present
            // Star groups are inherently parallel, update happens in a background thread by default
            sg = new SceneGraph();
        }
        return sg;
    }

}
