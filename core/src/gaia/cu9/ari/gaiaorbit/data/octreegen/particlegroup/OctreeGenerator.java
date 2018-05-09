package gaia.cu9.ari.gaiaorbit.data.octreegen.particlegroup;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.scenegraph.StarGroup.StarBean;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.BoundingBoxd;
import gaia.cu9.ari.gaiaorbit.util.math.Longref;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

public class OctreeGenerator {

    /** Is the octree centred at the sun? **/
    private boolean SUN_CENTRE = false;
    /** Maximum distance in pc **/
    private static final double MAX_DISTANCE_CAP = 1e6;

    IAggregationAlgorithm aggregation;
    Longref pageid;

    public OctreeGenerator(IAggregationAlgorithm aggregation) {
        this.aggregation = aggregation;
        this.pageid = new Longref(0l);
    }

    public OctreeNode generateOctree(Array<StarBean> catalog) {
        Logger.info(this.getClass().getSimpleName(), "Starting generation of octree");

        /** Maximum distance allowed **/
        double maxdist = Double.MIN_VALUE;

        /** Furthest star from origin **/
        StarBean furthest = null;

        // Aux vectors
        Vector3d pos0 = new Vector3d();
        Vector3d pos1 = new Vector3d();

        Iterator<StarBean> it = catalog.iterator();
        while (it.hasNext()) {
            StarBean s = it.next();

            double dist = pos(s.data, pos0).len();
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
            pos(furthest.data, pos0);
            double halfSize = Math.max(Math.max(pos0.x, pos0.y), pos0.z);
            root = new OctreeNode(nextPageId(), 0, 0, 0, halfSize, halfSize, halfSize, 0);
        } else {
            /** THE CENTRE OF THE OCTREE MAY BE ANYWHERE **/
            double volume = Double.MIN_VALUE;
            BoundingBoxd aux = new BoundingBoxd();
            BoundingBoxd box = new BoundingBoxd();
            // Lets try to maximize the volume: from furthest star to star where axis-aligned bounding box volume is maximum
            pos(furthest.data, pos1);
            for (StarBean s : catalog) {
                pos(s.data, pos0);
                aux.set(pos1, pos0);
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

        Array<OctreeNode>[] octantsPerLevel = new Array[25];
        octantsPerLevel[0] = new Array<OctreeNode>(1);
        octantsPerLevel[0].add(root);

        Map<OctreeNode, Array<StarBean>> inputLists = new HashMap<OctreeNode, Array<StarBean>>();
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
    private void treatLevel(Map<OctreeNode, Array<StarBean>> inputLists, int level, Array<OctreeNode>[] octantsPerLevel, float percentage) {
        Logger.info(this.getClass().getSimpleName(), "Generating level " + level);
        Array<OctreeNode> levelOctants = octantsPerLevel[level];

        octantsPerLevel[level + 1] = new Array<OctreeNode>(levelOctants.size * 8);

        /** CREATE OCTANTS FOR LEVEL+1 **/
        Iterator<OctreeNode> it = levelOctants.iterator();
        while (it.hasNext()) {
            OctreeNode octant = it.next();
            Array<StarBean> list = inputLists.get(octant);

            if (list.size == 0) {
                // Empty node, remove
                it.remove();
                octant.remove();
            } else {
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
        }

        /** IF WE HAVE OCTANTS IN THE NEXT LEVEL, INTERSECT **/
        if (octantsPerLevel[level + 1].size != 0) {

            /** INTERSECT CATALOG WITH OCTANTS, COMPUTE PERCENTAGE **/
            int maxSublevelObjs = 0;
            double maxSublevelMag = Double.MAX_VALUE;
            double minSublevelMag = 0;
            Map<OctreeNode, Array<StarBean>> lists = new HashMap<OctreeNode, Array<StarBean>>();

            for (OctreeNode octant : octantsPerLevel[level + 1]) {
                Array<StarBean> list = intersect(inputLists.get(octant.parent), octant);
                lists.put(octant, list);
                if (list.size > maxSublevelObjs) {
                    maxSublevelObjs = list.size;
                }
                // Adapt levels by magnitude
                for (StarBean sb : list) {
                    if (sb.absmag() < maxSublevelMag) {
                        maxSublevelMag = sb.absmag();
                    }
                    if (sb.absmag() > minSublevelMag) {
                        minSublevelMag = sb.absmag();
                    }
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
    private Array<StarBean> intersect(Array<StarBean> stars, OctreeNode box) {
        Array<StarBean> result = new Array<StarBean>();
        for (StarBean star : stars) {
            if (star.octant == null && box.box.contains(star.data[StarBean.I_X], star.data[StarBean.I_Y], star.data[StarBean.I_Z])) {
                result.add(star);
            }
        }
        return result;

    }

    private long nextPageId() {
        return pageid.num++;
    }

    private Vector3d pos(double[] s, Vector3d p) {
        return p.set(s[StarBean.I_X], s[StarBean.I_Y], s[StarBean.I_Z]);
    }

    public void setSunCentre(boolean sunCentre) {
        SUN_CENTRE = sunCentre;
    }

}
