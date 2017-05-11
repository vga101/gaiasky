package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.util.Arrays;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * Implementation of a 3D scene graph.
 * @author Toni Sagrista
 *
 */
public class SceneGraph extends AbstractSceneGraph {

    public SceneGraph() {
        super();
    }

    public void update(ITimeFrameProvider time, ICamera camera) {
        super.update(time, camera);

        root.transform.position.set(camera.getInversePos());
        root.update(time, null, camera);
        objectsPerThread[0] = root.numChildren;

        // Debug info
        EventManager.instance.post(Events.DEBUG3, "Objects/thread: " + Arrays.toString(objectsPerThread));
    }

    public void dispose() {
        super.dispose();
    }

}
