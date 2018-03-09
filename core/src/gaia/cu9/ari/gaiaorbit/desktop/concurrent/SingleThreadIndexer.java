package gaia.cu9.ari.gaiaorbit.desktop.concurrent;

/**
 * Single thread indexer. All indexes are 0.
 * @deprecated No longer used
 * @author Toni Sagrista
 *
 */
public class SingleThreadIndexer extends ThreadIndexer {

    @Override
    public int idx() {
        return 0;
    }

    @Override
    public int nthreads() {
        return 1;
    }

}
