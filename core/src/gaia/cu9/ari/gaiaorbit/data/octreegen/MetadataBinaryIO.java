package gaia.cu9.ari.gaiaorbit.data.octreegen;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.Pair;
import gaia.cu9.ari.gaiaorbit.util.tree.LoadStatus;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

/**
 * Writes and reats the metadata to/from binary. The format is as follows:
 * 
 * - 32 bits (int) with the number of nodes, nNodes
 * repeat the following nNodes times (for each node)
 * - 64 bits (long) - pageId - The page id
 * - 64 bits (double) - centreX - The x component of the centre
 * - 64 bits (double) - centreY - The y component of the centre
 * - 64 bits (double) - centreZ - The z component of the centre
 * - 64 bits (double) - sx - The size in x
 * - 64 bits (double) - sy - The size in y
 * - 64 bits (double) - sz - The size in z
 * - 64 bits * 8 (long) - childrenIds - 8 longs with the ids of the children. If no child in the given position, the id is negative.
 * - 32 bits (int) - depth - The depth of the node
 * - 32 bits (int) - nObjects - The number of objects of this node and its descendants
 * - 32 bits (int) - ownObjects - The number of objects of this node
 * - 32 bits (int) - childCount - The number of children nodes
 * @author Toni Sagrista
 *
 */
public class MetadataBinaryIO<T extends SceneGraphNode> {
    public Map<Long, Pair<OctreeNode<T>, long[]>> nodesMap;

    /**
     * Reads the metadata into an octree node.
     * @param in
     * @return
     */
    public OctreeNode<T> readMetadata(InputStream in) {
        return readMetadata(in, null);
    }

    /**
     * Reads the metadata into an octree node.
     * @param in Input stream
     * @return
     */
    public OctreeNode<T> readMetadata(InputStream in, LoadStatus status) {
        nodesMap = new HashMap<Long, Pair<OctreeNode<T>, long[]>>();

        DataInputStream data_in = new DataInputStream(in);
        try {
            OctreeNode<T> root = null;
            // Read size of stars
            int size = data_in.readInt();
            int maxDepth = 0;

            for (int idx = 0; idx < size; idx++) {
                try {
                    // name_length, name, appmag, absmag, colorbv, ra, dec, dist	
                    long pageId = data_in.readInt();
                    float x = data_in.readFloat();
                    float y = data_in.readFloat();
                    float z = data_in.readFloat();
                    float hsx = data_in.readFloat() / 2f;
                    float hsy = data_in.readFloat() / 2f;
                    float hsz = data_in.readFloat() / 2f;
                    long[] childrenIds = new long[8];
                    for (int i = 0; i < 8; i++) {
                        childrenIds[i] = data_in.readInt();
                    }
                    int depth = data_in.readInt();
                    int nObjects = data_in.readInt();
                    int ownObjects = data_in.readInt();
                    int childrenCount = data_in.readInt();

                    maxDepth = Math.max(maxDepth, depth);

                    OctreeNode<T> node = new OctreeNode<T>(pageId, x, y, z, hsx, hsy, hsz, childrenCount, nObjects, ownObjects, depth);
                    nodesMap.put(pageId, new Pair<OctreeNode<T>, long[]>(node, childrenIds));
                    if (status != null)
                        node.setStatus(status);

                    if (depth == 0) {
                        root = node;
                    }

                } catch (EOFException eof) {
                    Logger.error(eof);
                }
            }

            OctreeNode.maxDepth = maxDepth;
            // All data has arrived
            if (root != null) {
                root.resolveChildren(nodesMap);
            } else {
                Logger.error(new RuntimeException("No root node in visualization-metadata"));
            }

            return root;

        } catch (IOException e) {
            Logger.error(e);
        }
        return null;
    }

    /**
     * Writes the metadata of the given octree node and its descendants to the 
     * given output stream in binary.
     * @param root
     * @param out
     */
    public void writeMetadata(OctreeNode<? extends SceneGraphNode> root, OutputStream out) {
        List<OctreeNode<? extends SceneGraphNode>> nodes = new ArrayList<OctreeNode<? extends SceneGraphNode>>();
        toList(root, nodes);

        // Wrap the FileOutputStream with a DataOutputStream
        DataOutputStream data_out = new DataOutputStream(out);

        try {
            // Number of nodes
            data_out.writeInt(nodes.size());

            for (OctreeNode<? extends SceneGraphNode> node : nodes) {
                data_out.writeInt((int) node.pageId);
                data_out.writeFloat((float) node.centre.x);
                data_out.writeFloat((float) node.centre.y);
                data_out.writeFloat((float) node.centre.z);
                data_out.writeFloat((float) node.size.x);
                data_out.writeFloat((float) node.size.y);
                data_out.writeFloat((float) node.size.z);
                for (int i = 0; i < 8; i++) {
                    data_out.writeInt((int) (node.children[i] != null ? node.children[i].pageId : -1));
                }
                data_out.writeInt(node.depth);
                data_out.writeInt(node.nObjects);
                data_out.writeInt(node.ownObjects);
                data_out.writeInt(node.childrenCount);
            }

            data_out.close();
            out.close();

        } catch (IOException e) {
            Logger.error(e);
        }

    }

    public void toList(OctreeNode<? extends SceneGraphNode> node, List<OctreeNode<? extends SceneGraphNode>> nodes) {
        nodes.add(node);
        for (OctreeNode<? extends SceneGraphNode> child : node.children) {
            if (child != null) {
                toList(child, nodes);
            }
        }
    }

}
