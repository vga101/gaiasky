package gaia.cu9.ari.gaiaorbit.data.octreegen.generator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.data.octreegen.StarBrightnessComparator;
import gaia.cu9.ari.gaiaorbit.scenegraph.StarGroup;
import gaia.cu9.ari.gaiaorbit.scenegraph.StarGroup.StarBean;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

public class OctreeGeneratorMag implements IOctreeGenerator {

    private OctreeGeneratorParams params;
    private Comparator<StarBean> comp;

    public OctreeGeneratorMag(OctreeGeneratorParams params) {
        this.params = params;
        comp = new StarBrightnessComparator();
    }

    @Override
    public OctreeNode generateOctree(Array<StarBean> catalog) {
        OctreeNode root = IOctreeGenerator.startGeneration(catalog, this.getClass(), params);

        @SuppressWarnings("unchecked")
        Array<OctreeNode>[] octantsPerLevel = new Array[25];
        octantsPerLevel[0] = new Array<OctreeNode>(1);
        octantsPerLevel[0].add(root);

        Map<OctreeNode, Array<StarBean>> sbMap = new HashMap<OctreeNode, Array<StarBean>>();

        Logger.info(this.getClass().getSimpleName(), "Sorting source catalog with " + catalog.size + " stars");
        catalog.sort(comp);
        Logger.info(this.getClass().getSimpleName(), "Catalog sorting done");

        int catalogIndex = 0;
        for (int level = 0; level < 25; level++) {
            Logger.info(this.getClass().getSimpleName(), "Generating level " + level);
            // Treat each level and set up the next
            Array<OctreeNode> levelOctants = octantsPerLevel[level];
            Logger.info("        Distributing stars to " + levelOctants.size + " octants");
            while (catalogIndex < catalog.size) {
                // Add star beans to octants till we reach max capacity
                StarBean sb = catalog.get(catalogIndex++);
                int addedNum = 0;
                for (OctreeNode octant : levelOctants) {
                    if (contained(sb, octant)) {
                        addedNum = addStarToNode(sb, octant, sbMap);
                        break;
                    }
                }

                if (addedNum >= params.maxPart) {
                    // On to next level!
                    break;
                }
            }

            if (catalogIndex >= catalog.size) {
                // All stars added -> FINISHED
                break;
            } else {
                // Prepare next level (create nodes, etc.)
                Iterator<OctreeNode> it = levelOctants.iterator();
                while (it.hasNext()) {
                    OctreeNode octant = it.next();
                    if (sbMap.containsKey(octant) && sbMap.get(octant).size > 0) {
                        // Generate 8 children per each level octant
                        double hsx = octant.size.x / 4d;
                        double hsy = octant.size.y / 4d;
                        double hsz = octant.size.z / 4d;

                        /** CREATE SUB-OCTANTS **/
                        // Front - top - left
                        addToOctantsPerLevel(octantsPerLevel, level + 1, new OctreeNode(IOctreeGenerator.nextPageId(), octant.centre.x - hsx, octant.centre.y + hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1, octant, 0));
                        // Front - top - right
                        addToOctantsPerLevel(octantsPerLevel, level + 1, new OctreeNode(IOctreeGenerator.nextPageId(), octant.centre.x + hsx, octant.centre.y + hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1, octant, 1));
                        // Front - bottom - left
                        addToOctantsPerLevel(octantsPerLevel, level + 1, new OctreeNode(IOctreeGenerator.nextPageId(), octant.centre.x - hsx, octant.centre.y - hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1, octant, 2));
                        // Front - bottom - right
                        addToOctantsPerLevel(octantsPerLevel, level + 1, new OctreeNode(IOctreeGenerator.nextPageId(), octant.centre.x + hsx, octant.centre.y - hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1, octant, 3));
                        // Back - top - left
                        addToOctantsPerLevel(octantsPerLevel, level + 1, new OctreeNode(IOctreeGenerator.nextPageId(), octant.centre.x - hsx, octant.centre.y + hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1, octant, 4));
                        // Back - top - right
                        addToOctantsPerLevel(octantsPerLevel, level + 1, new OctreeNode(IOctreeGenerator.nextPageId(), octant.centre.x + hsx, octant.centre.y + hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1, octant, 5));
                        // Back - bottom - left
                        addToOctantsPerLevel(octantsPerLevel, level + 1, new OctreeNode(IOctreeGenerator.nextPageId(), octant.centre.x - hsx, octant.centre.y - hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1, octant, 6));
                        // Back - bottom - right
                        addToOctantsPerLevel(octantsPerLevel, level + 1, new OctreeNode(IOctreeGenerator.nextPageId(), octant.centre.x + hsx, octant.centre.y - hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1, octant, 7));
                    } else {
                        // Remove octant from this world
                        if (octant.parent != null) {
                            octant.parent.removeChild(octant);
                        }
                        it.remove();
                    }
                }
            }
        }

        // Create all star groups
        Set<OctreeNode> nodes = sbMap.keySet();
        for (OctreeNode node : nodes) {
            Array<StarBean> list = sbMap.get(node);
            StarGroup sg = new StarGroup();
            sg.setData(list, false);
            node.add(sg);
            sg.octant = node;
            sg.octantId = node.pageId;
        }

        root.updateNumbers();
        return root;
    }

    private void addToOctantsPerLevel(Array<OctreeNode>[] octantsPerLevel, int level, OctreeNode node) {
        if (octantsPerLevel[level] == null) {
            octantsPerLevel[level] = new Array<OctreeNode>();
        }
        octantsPerLevel[level].add(node);
    }

    private int addStarToNode(StarBean sb, OctreeNode node, Map<OctreeNode, Array<StarBean>> map) {
        if (!map.containsKey(node)) {
            map.put(node, new Array<StarBean>());
        }
        map.get(node).add(sb);
        return map.get(node).size;
    }

    private boolean contained(StarBean star, OctreeNode box) {
        return box.box.contains(star.data[StarBean.I_X], star.data[StarBean.I_Y], star.data[StarBean.I_Z]);
    }

    @Override
    public int getDiscarded() {
        return 0;
    }

}
