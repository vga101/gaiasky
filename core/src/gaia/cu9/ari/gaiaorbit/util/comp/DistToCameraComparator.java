package gaia.cu9.ari.gaiaorbit.util.comp;

import java.util.Comparator;

import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;

/**
 * Compares entities. Further entities go first, nearer entities go last.
 * 
 * @author Toni Sagrista
 */
public class DistToCameraComparator<T> implements Comparator<T> {

    @Override
    public int compare(T o1, T o2) {
        return -Double.compare(((AbstractPositionEntity) o1).distToCamera, ((AbstractPositionEntity) o2).distToCamera);
    }

}
