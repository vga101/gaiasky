package gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper;

import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.Transform;
import gaia.cu9.ari.gaiaorbit.scenegraph.camera.ICamera;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

/**
 * Static Octree wrapper that can be inserted into the scene graph. This
 * implementation is single-threaded.
 * 
 * @author Toni Sagrista
 *
 */
public class OctreeWrapper extends AbstractOctreeWrapper {

    public OctreeWrapper() {
        super();
    }

    public OctreeWrapper(String parentName, OctreeNode root) {
        super(parentName, root);
        roulette = new Array<SceneGraphNode>(false, root.nObjects);
    }

    @Override
    protected void updateOctreeObjects(ITimeFrameProvider time, Transform parentTransform, ICamera camera) {
        int size = roulette.size;
        for (int i = 0; i < size; i++) {
            SceneGraphNode sgn = roulette.get(i);
            sgn.update(time, parentTransform, camera, ((AbstractPositionEntity) sgn).octant.opacity);
        }
    }

}
