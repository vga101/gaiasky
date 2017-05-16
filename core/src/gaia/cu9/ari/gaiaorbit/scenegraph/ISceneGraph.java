package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.io.Serializable;
import java.util.HashMap;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.IntMap;

import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public interface ISceneGraph extends Serializable, Disposable {
    public void initialize(Array<SceneGraphNode> nodes, ITimeFrameProvider time);

    public void update(ITimeFrameProvider time, ICamera camera);

    public boolean containsNode(String name);

    /**
     * Returns the node with the given name, or null if it does not exist.
     * 
     * @param name
     *            The name of the node.
     * @return The node with the name.
     */
    public SceneGraphNode getNode(String name);

    /**
     * Updates the string to node map and the star map if necessary.
     * 
     * @param node
     */
    public void addNodeAuxiliaryInfo(SceneGraphNode node);

    /**
     * Removes the info of the node from the aux lists.
     * 
     * @param node
     */
    public void removeNodeAuxiliaryInfo(SceneGraphNode node);

    public HashMap<String, SceneGraphNode> getStringToNodeMap();

    /**
     * Gets a star map: HIP -&gt; Star It only contains the stars with HIP
     * number
     * 
     * @return The HIP star map
     */
    public IntMap<Star> getStarMap();

    public Array<SceneGraphNode> getNodes();

    public SceneGraphNode getRoot();

    public Array<CelestialBody> getFocusableObjects();

    public CelestialBody findFocus(String name);

    public int getSize();

}
