package gaia.cu9.ari.gaiaorbit.data.octreegen;

import java.util.Comparator;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

public class BrightestStars implements IAggregationAlgorithm {
    /** Maximum depth of the octree **/
    private int MAX_DEPTH;
    /** Maximum number of objects in the densest node of a level **/
    private int MAX_PART;
    /**
     * Minimum number of objects under which we do not further break the octree
     **/
    private int MIN_PART;

    /** Whether to discard stars due to density or not **/
    private boolean DISCARD = false;

    Comparator<Particle> comp;

    int discarded = 0;

    /**
     * Constructor using fields
     * 
     * @param maxDepth
     *            Maximum depth of the octree
     * @param maxPart
     *            Maximum number of objects in the densest node of this level
     * @param minPart
     *            Minimum number of objects under which we do not further break
     *            the octree
     * @param discard
     *            Whether to discard stars due to density or not
     */
    public BrightestStars(int maxDepth, int maxPart, int minPart, boolean discard) {
	comp = new StarBrightnessComparator();
	this.MAX_DEPTH = maxDepth;
	this.MAX_PART = maxPart;
	this.MIN_PART = minPart;
	this.DISCARD = discard;
    }

    @Override
    public boolean sample(Array<Particle> inputStars, OctreeNode octant, float percentage) {
	// Calculate nObjects for this octant based on maxObjs and the MAX_PART
	int nInput = inputStars.size;
	int nObjects = MathUtils.clamp(Math.round(nInput * percentage), 1, Integer.MAX_VALUE);

	if (nInput <= MIN_PART || octant.depth >= MAX_DEPTH) {
	    if (!DISCARD) {
		// Never discard any
		for (Particle s : inputStars) {
		    if (s.octant == null) {
			octant.add(s);
			s.octant = octant;
			s.octantId = octant.pageId;
		    }
		}
	    } else if (DISCARD) {
		if (nInput <= MIN_PART) {
		    // Downright use all stars that have not been assigned
		    for (Particle s : inputStars) {
			if (s.octant == null) {
			    octant.add(s);
			    s.octant = octant;
			    s.octantId = octant.pageId;
			}
		    }
		} else {
		    // Select sample, discard the rest
		    inputStars.sort(comp);
		    for (int i = 0; i < nObjects; i++) {
			Particle s = inputStars.get(i);
			if (s.octant == null) {
			    // Add star
			    octant.add(s);
			    s.octant = octant;
			    s.octantId = octant.pageId;
			}
		    }

		    discarded += nInput - nObjects;
		}
	    }
	    return true;
	} else {
	    // Extract sample
	    inputStars.sort(comp);
	    int added = 0;
	    int i = 0;
	    while (added < nObjects && i < inputStars.size) {
		Particle s = inputStars.get(i);
		if (s.octant == null) {
		    // Add star
		    octant.add(s);
		    s.octant = octant;
		    s.octantId = octant.pageId;
		    added++;
		}
		i++;
	    }
	    // It is leaf if we didn't add any star
	    return added == 0;
	}

    }

    public class StarBrightnessComparator implements Comparator<Particle> {
	@Override
	public int compare(Particle o1, Particle o2) {
	    return Float.compare(o1.absmag, o2.absmag);
	}

    }

    public int getMaxPart() {
	return MAX_PART;
    }

    public int getDiscarded() {
	return discarded;
    }

}
