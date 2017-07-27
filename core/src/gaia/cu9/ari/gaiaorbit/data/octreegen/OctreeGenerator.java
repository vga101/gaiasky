package gaia.cu9.ari.gaiaorbit.data.octreegen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.BoundingBoxd;
import gaia.cu9.ari.gaiaorbit.util.math.Longref;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

public class OctreeGenerator {

    /** Is the octree centred at the sun? **/
    private static final boolean SUN_CENTRE = false;
    /** Maximum distance in parsecs **/
    private static final double MAX_DISTANCE_CAP = 3e6;

    IAggregationAlgorithm aggregation;
    Longref pageid;

    public OctreeGenerator(IAggregationAlgorithm aggregation) {
        this.aggregation = aggregation;
        this.pageid = new Longref(0l);
    }

    public OctreeNode generateOctree(Array<AbstractPositionEntity> catalog) {
        Logger.info(this.getClass().getSimpleName(), "Starting generation of octree");

        double maxdist = Double.MIN_VALUE;
        Iterator<AbstractPositionEntity> it = catalog.iterator();
        AbstractPositionEntity furthest = null;
        while (it.hasNext()) {
            AbstractPositionEntity s = it.next();
            double dist = s.pos.len();
            if (dist * Constants.U_TO_PC > MAX_DISTANCE_CAP) {
                // Remove star
                it.remove();
            } else if (dist > maxdist) {
                furthest = s;
                maxdist = dist;
            }
        }

        OctreeNode root = null;
        if (SUN_CENTRE) {
            /** THE CENTRE OF THE OCTREE IS THE SUN **/
            double halfSize = Math.max(Math.max(furthest.pos.x, furthest.pos.y), furthest.pos.z);
            root = new OctreeNode(nextPageId(), 0, 0, 0, halfSize, halfSize, halfSize, 0);
        } else {
            /** THE CENTRE OF THE OCTREE MAY BE ANYWHERE **/
            double volume = Double.MIN_VALUE;
            BoundingBoxd aux = new BoundingBoxd();
            BoundingBoxd box = new BoundingBoxd();
            // Lets try to maximize the volume
            for (AbstractPositionEntity s : catalog) {
                aux.set(furthest.pos, s.pos);
                double vol = aux.getVolume();
                if (vol > volume) {
                    volume = vol;
                    box.set(aux);
                }
            }
            double halfSize = Math.max(Math.max(box.getDepth(), box.getHeight()), box.getWidth()) / 2d;
            root = new OctreeNode(nextPageId(), box.getCenterX(), box.getCenterY(), box.getCenterZ(), halfSize, halfSize, halfSize, 0);
        }

        // treatOctant(root, catalog, MathUtils.clamp((float)
        // aggregation.getMaxPart() / (float) catalog.size(), 0f, 1f));

        List<OctreeNode>[] octantsPerLevel = new List[25];
        octantsPerLevel[0] = new ArrayList<OctreeNode>(1);
        octantsPerLevel[0].add(root);

        Map<OctreeNode, Array<AbstractPositionEntity>> inputLists = new HashMap<OctreeNode, Array<AbstractPositionEntity>>();
        inputLists.put(root, catalog);

        treatLevel(inputLists, 0, octantsPerLevel, MathUtils.clamp((float) aggregation.getMaxPart() / (float) catalog.size, 0f, 1f));

        root.updateNumbers();

        return root;
    }

    /**
     * Generate the octree on a per-level basis to have a uniform density in all
     * the nodes of the same level. Breadth-first.
     * 
     * @param catalog
     * @param level
     * @param octantsPerLevel
     * @param percentage
     */
    private void treatLevel(Map<OctreeNode, Array<AbstractPositionEntity>> inputLists, int level, List<OctreeNode>[] octantsPerLevel, float percentage) {
        Logger.info(this.getClass().getSimpleName(), "Generating level " + level);
        List<OctreeNode> levelOctants = octantsPerLevel[level];

        octantsPerLevel[level + 1] = new ArrayList<OctreeNode>(levelOctants.size() * 8);

        /** CREATE OCTANTS FOR LEVEL+1 **/
        for (OctreeNode octant : levelOctants) {
            Array<AbstractPositionEntity> list = inputLists.get(octant);
            boolean leaf = aggregation.sample(list, octant, percentage);

            if (!leaf) {
                // Generate 8 children per each level octant
                double hsx = octant.size.x / 4d;
                double hsy = octant.size.y / 4d;
                double hsz = octant.size.z / 4d;

                /** CREATE SUB-OCTANTS **/
                // Front - top - left
                octantsPerLevel[level + 1].add(new OctreeNode(nextPageId(), octant.centre.x - hsx, octant.centre.y + hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1, octant, 0));
                // Front - top - right
                octantsPerLevel[level + 1].add(new OctreeNode(nextPageId(), octant.centre.x + hsx, octant.centre.y + hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1, octant, 1));
                // Front - bottom - left
                octantsPerLevel[level + 1].add(new OctreeNode(nextPageId(), octant.centre.x - hsx, octant.centre.y - hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1, octant, 2));
                // Front - bottom - right
                octantsPerLevel[level + 1].add(new OctreeNode(nextPageId(), octant.centre.x + hsx, octant.centre.y - hsy, octant.centre.z - hsz, hsx, hsy, hsz, octant.depth + 1, octant, 3));
                // Back - top - left
                octantsPerLevel[level + 1].add(new OctreeNode(nextPageId(), octant.centre.x - hsx, octant.centre.y + hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1, octant, 4));
                // Back - top - right
                octantsPerLevel[level + 1].add(new OctreeNode(nextPageId(), octant.centre.x + hsx, octant.centre.y + hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1, octant, 5));
                // Back - bottom - left
                octantsPerLevel[level + 1].add(new OctreeNode(nextPageId(), octant.centre.x - hsx, octant.centre.y - hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1, octant, 6));
                // Back - bottom - right
                octantsPerLevel[level + 1].add(new OctreeNode(nextPageId(), octant.centre.x + hsx, octant.centre.y - hsy, octant.centre.z + hsz, hsx, hsy, hsz, octant.depth + 1, octant, 7));
            }
        }

        /** IF WE HAVE OCTANTS IN THE NEXT LEVEL, INTERSECT **/
        if (!octantsPerLevel[level + 1].isEmpty()) {

            /** INTERSECT CATALOG WITH OCTANTS, COMPUTE PERCENTAGE **/
            int maxSublevelObjs = 0;
            int minSubLevelObjs = Integer.MAX_VALUE;
            Map<OctreeNode, Array<AbstractPositionEntity>> lists = new HashMap<OctreeNode, Array<AbstractPositionEntity>>();

            for (OctreeNode octant : octantsPerLevel[level + 1]) {
                Array<AbstractPositionEntity> list = intersect(inputLists.get(octant.parent), octant);
                lists.put(octant, list);
                if (list.size > maxSublevelObjs) {
                    maxSublevelObjs = list.size;
                }
                if (list.size < minSubLevelObjs) {
                    minSubLevelObjs = list.size;
                }
            }
            float sublevelPercentage = MathUtils.clamp((float) aggregation.getMaxPart() / (float) maxSublevelObjs, 0f, 1f);

            /** GO ONE MORE LEVEL DOWN **/
            treatLevel(lists, level + 1, octantsPerLevel, sublevelPercentage);
        }
    }

    /**
     * Returns a new list with all the stars of the incoming list that are
     * inside the box.
     * 
     * @param stars
     * @param box
     * @return
     */
    private Array<AbstractPositionEntity> intersect(Array<AbstractPositionEntity> stars, OctreeNode box) {
        Array<AbstractPositionEntity> result = new Array<AbstractPositionEntity>();
        for (AbstractPositionEntity star : stars) {
            if (star.octant == null && box.box.contains(star.pos)) {
                result.add(star);
            }
        }
        return result;

    }

    private long nextPageId() {
        return pageid.num++;
    }

}
