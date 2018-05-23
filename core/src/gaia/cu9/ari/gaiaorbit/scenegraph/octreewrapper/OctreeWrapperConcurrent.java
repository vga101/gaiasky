package gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper;

import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.render.system.AbstractRenderSystem;
import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.scenegraph.Transform;
import gaia.cu9.ari.gaiaorbit.scenegraph.camera.ICamera;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

/**
 * Static Octree wrapper that can be inserted into the scene graph. This
 * implementation is prepared to work with the
 * {@link gaia.cu9.ari.gaiaorbit.desktop.concurrent.SceneGraphConcurrent},
 * which is designed to take advantage of knowing an octree is present in the
 * scenegraph.
 * 
 * @deprecated No longer used since the addition of star and particle groups
 * @author Toni Sagrista
 *
 */
public class OctreeWrapperConcurrent extends AbstractOctreeWrapper {

    public OctreeWrapperConcurrent() {
        super();
    }

    public OctreeWrapperConcurrent(String parentName, OctreeNode root) {
        super(parentName, root);
    }

    public void setRoulette(Array<SceneGraphNode> roulette) {
        this.roulette = roulette;
    }

    @Override
    public void initialize() {
        super.initialize();
    }

    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera, float opacity) {
        this.opacity = opacity;
        transform.set(parentTransform);

        // Update octants
        if (!copy) {
            // Compute observed octants and fill roulette list
            OctreeNode.nOctantsObserved = 0;
            OctreeNode.nObjectsObserved = 0;
            root.update(transform, camera, roulette, 1f);

            if (OctreeNode.nObjectsObserved != lastNumberObjects) {
                // Need to update the points in renderer
                AbstractRenderSystem.POINT_UPDATE_FLAG = true;
                lastNumberObjects = OctreeNode.nObjectsObserved;
            }

            updateLocal(time, camera);

        } else {
            // Just update children
            for (SceneGraphNode sgn : children) {
                if (copy)
                    sgn.update(time, transform, camera);
                else
                    sgn.update(time, transform, camera, ((AbstractPositionEntity) sgn).octant.opacity);
            }
        }

    }

    @Override
    protected void updateOctreeObjects(ITimeFrameProvider time, Transform parentTransform, ICamera camera) {
    }

}
