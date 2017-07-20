package gaia.cu9.ari.gaiaorbit.data.octreegen;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.tree.OctreeNode;

public class MagnitudeCut implements IAggregationAlgorithm {

    private static final float START_MAG = 7;
    int starId;

    public MagnitudeCut() {
        starId = (int) TimeUtils.millis();
    }

    @Override
    public boolean sample(Array<AbstractPositionEntity> inputStars, OctreeNode octant, float percentage) {
        float limitMag = START_MAG + octant.depth * 2;
        List<AbstractPositionEntity> candidates = new ArrayList<AbstractPositionEntity>(10000);
        for (AbstractPositionEntity ape : inputStars) {
            Particle s = (Particle) ape;
            if (s.appmag < limitMag) {
                candidates.add(s);
            }
        }
        boolean leaf = candidates.size() == inputStars.size;

        for (AbstractPositionEntity ape : candidates) {
            Particle s = (Particle) ape;
            if (leaf) {
                octant.add(s);
                s.octant = octant;
                s.octantId = octant.pageId;
            } else {
                // New virtual star
                Particle virtual = getVirtualCopy(s);

                // Add virtual to octant
                octant.add(virtual);
                virtual.octant = octant;
                virtual.octantId = octant.pageId;
            }
        }
        return leaf;
    }

    private Particle getVirtualCopy(Particle s) {
        Particle copy = new Particle();
        copy.name = s.name;
        copy.absmag = s.absmag;
        copy.appmag = s.appmag;
        copy.cc = s.cc;
        copy.colorbv = s.colorbv;
        copy.ct = s.ct;
        copy.pos = new Vector3d(s.pos);
        copy.id = starId++;
        return copy;
    }

    public int getMaxPart() {
        return Integer.MAX_VALUE;
    }

    public int getDiscarded() {
        return 0;
    }
}
