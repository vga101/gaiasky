package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

import gaia.cu9.ari.gaiaorbit.render.system.PixelRenderSystem;
import gaia.cu9.ari.gaiaorbit.scenegraph.octreewrapper.AbstractOctreeWrapper;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.tree.IPosition;

public abstract class AbstractSceneGraph implements ISceneGraph {

    /** The root of the tree **/
    public SceneGraphNode root;
    /** Quick lookup map. Name to node. **/
    HashMap<String, SceneGraphNode> stringToNode;
    /**
     * Map from integer to position with all hipparcos stars, for the
     * constellations
     **/
    IntMap<IPosition> hipMap;
    /** Number of objects per thread **/
    protected int[] objectsPerThread;
    /** Does it contain an octree **/
    protected boolean hasOctree;

    public AbstractSceneGraph() {
        // Id = -1 for root
        root = new SceneGraphNode(-1);
        root.name = SceneGraphNode.ROOT_NAME;

        // Objects per thread
        objectsPerThread = new int[1];
    }

    @Override
    public void initialize(Array<SceneGraphNode> nodes, ITimeFrameProvider time, boolean hasOctree) {
        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.sg.insert", nodes.size));

        // Set the reference
        SceneGraphNode.sg = this;

        // Octree
        this.hasOctree = hasOctree;

        // Initialize stringToNode and starMap maps
        stringToNode = new HashMap<String, SceneGraphNode>(nodes.size * 2);
        stringToNode.put(root.name, root);
        hipMap = new IntMap<IPosition>();
        for (SceneGraphNode node : nodes) {
            addToIndex(node, stringToNode);

            // Unwrap octree objects
            if (node instanceof AbstractOctreeWrapper) {
                AbstractOctreeWrapper ow = (AbstractOctreeWrapper) node;
                if (ow.children != null)
                    for (SceneGraphNode ownode : ow.children) {
                        addToIndex(ownode, stringToNode);
                    }
            }

            // Star map            
            addToHipMap(node);
        }

        // Insert all the nodes
        for (SceneGraphNode node : nodes) {
            insert(node, false);
        }

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.sg.init", root.numChildren));
    }

    public void insert(SceneGraphNode node, boolean addToIndex) {
        SceneGraphNode parent = stringToNode.get(node.parentName);
        if (parent != null) {
            parent.addChild(node, true);
            node.setUp();

            if (addToIndex) {
                addToIndex(node, stringToNode);
            }
        } else {
            throw new RuntimeException("Parent of node " + node.name + " not found: " + node.parentName);
        }
    }

    public void remove(SceneGraphNode node, boolean removeFromIndex) {
        if (node != null) {
            node.parent.removeChild(node, true);

            if (removeFromIndex) {
                removeFromIndex(node, stringToNode);
            }
        } else {
            throw new RuntimeException("Given node is null");
        }
    }

    private void addToHipMap(SceneGraphNode node) {
        if (node.getStarCount() == 1) {
            CelestialBody s = (CelestialBody) node.getStars();
            if (s instanceof Star && ((Star) s).hip >= 0) {
                if (hipMap.containsKey(((Star) s).hip)) {
                    Logger.debug(this.getClass().getSimpleName(), "Duplicated HIP id: " + ((Star) s).hip);
                } else {
                    hipMap.put(((Star) s).hip, (Star) s);
                }
            }
        } else if (node.getStarCount() > 1) {
            if (node instanceof AbstractOctreeWrapper) {
                Array<AbstractPositionEntity> stars = (Array<AbstractPositionEntity>) node.getStars();
                for (AbstractPositionEntity s : stars) {
                    if (s instanceof Star && ((Star) s).hip >= 0) {
                        if (hipMap.containsKey(((Star) s).hip)) {
                            Logger.debug(this.getClass().getSimpleName(), "Duplicated HIP id: " + ((Star) s).hip);
                        } else {
                            hipMap.put(((Star) s).hip, (Star) s);
                        }
                    }
                }
            } else if (node instanceof StarGroup) {
                Array<double[]> stars = ((StarGroup) node).pointData;
                for (double[] s : stars) {
                    if (s[StarGroup.I_HIP] > 0) {
                        hipMap.put((int) s[StarGroup.I_HIP], new Position(s[StarGroup.I_X], s[StarGroup.I_Y], s[StarGroup.I_Z]));
                    }
                }
            }
        }
    }

    private void removeFromStarMap(SceneGraphNode node) {
        if (node.getStarCount() == 1) {
            CelestialBody s = (CelestialBody) node.getStars();
            if (s instanceof Star && ((Star) s).hip >= 0) {
                hipMap.remove(((Star) s).hip);
            }
        } else if (node.getStarCount() > 1) {
            Array<AbstractPositionEntity> stars = (Array<AbstractPositionEntity>) node.getStars();
            for (AbstractPositionEntity s : stars) {
                if (s instanceof Star && ((Star) s).hip >= 0) {
                    hipMap.remove(((Star) s).hip);
                }
            }
        }
    }

    private void addToIndex(SceneGraphNode node, HashMap<String, SceneGraphNode> map) {
        if (node.name != null && !node.name.isEmpty()) {
            map.put(node.name, node);
            map.put(node.name.toLowerCase(), node);

            // Id
            if (node.id > 0) {
                String id = String.valueOf(node.id);
                map.put(id, node);
            }

            if (node instanceof Star) {
                // Hip
                if (((Star) node).hip > 0) {
                    String hipid = "hip " + ((Star) node).hip;
                    map.put(hipid, node);
                }
                // Tycho
                if (((Star) node).tycho != null && !((Star) node).tycho.isEmpty()) {
                    map.put(((Star) node).tycho, node);
                }

            }

        }
    }

    private void removeFromIndex(SceneGraphNode node, HashMap<String, SceneGraphNode> map) {
        if (node.name != null && !node.name.isEmpty()) {
            map.remove(node.name);
            map.remove(node.name.toLowerCase());

            // Id
            if (node.id > 0) {
                String id = String.valueOf(node.id);
                map.remove(id);
            }

            if (node instanceof Star) {
                // Hip
                if (((Star) node).hip > 0) {
                    String hipid = "hip " + ((Star) node).hip;
                    map.remove(hipid);
                }

                // Tycho
                if (((Star) node).tycho != null && !((Star) node).tycho.isEmpty()) {
                    map.remove(((Star) node).tycho);
                }
            }
        }
    }

    public synchronized void addToStringToNode(String key, SceneGraphNode node) {
        stringToNode.put(key, node);
    }

    public synchronized void removeFromStringToNode(String key) {
        stringToNode.remove(key);
    }

    public synchronized void removeFromStringToNode(SceneGraphNode node) {
        Set<String> keys = stringToNode.keySet();
        Set<String> hits = new HashSet<String>();
        for (String key : keys) {
            if (stringToNode.get(key) == node)
                hits.add(key);
        }
        for (String key : hits)
            stringToNode.remove(key);
    }

    @Override
    public void update(ITimeFrameProvider time, ICamera camera) {
        // Check if we need to update the points
        if (GlobalConf.scene.COMPUTE_GAIA_SCAN && time.getDt() != 0) {
            PixelRenderSystem.POINT_UPDATE_FLAG = true;
        }
    }

    public HashMap<String, SceneGraphNode> getStringToNodeMap() {
        return stringToNode;
    }

    public void addNodeAuxiliaryInfo(SceneGraphNode node) {
        // Name index
        addToIndex(node, stringToNode);
        // Star map
        addToHipMap(node);
    }

    public void removeNodeAuxiliaryInfo(SceneGraphNode node) {
        // Name index
        removeFromIndex(node, stringToNode);
        // Star map
        removeFromStarMap(node);
    }

    public boolean containsNode(String name) {
        return stringToNode.containsKey(name);
    }

    public SceneGraphNode getNode(String name) {
        //return root.getNode(name);
        return stringToNode.get(name);
    }

    public Array<SceneGraphNode> getNodes() {
        Array<SceneGraphNode> objects = new Array<SceneGraphNode>();
        root.addNodes(objects);
        return objects;
    }

    public Array<IFocus> getFocusableObjects() {
        Array<IFocus> objects = new Array<IFocus>();
        root.addFocusableObjects(objects);
        return objects;
    }

    public CelestialBody findFocus(String name) {
        SceneGraphNode node = getNode(name);
        if (node == null || !(node instanceof CelestialBody))
            return null;
        else
            return (CelestialBody) node;
    }

    public int getSize() {
        return root.getAggregatedChildren();
    }

    public void dispose() {
        root.dispose();
    }

    @Override
    public SceneGraphNode getRoot() {
        return root;
    }

    @Override
    public IntMap<IPosition> getStarMap() {
        return hipMap;
    }

}
