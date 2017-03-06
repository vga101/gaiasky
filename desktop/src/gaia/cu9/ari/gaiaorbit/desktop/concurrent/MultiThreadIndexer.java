package gaia.cu9.ari.gaiaorbit.desktop.concurrent;

import gaia.cu9.ari.gaiaorbit.desktop.concurrent.GaiaSandboxThreadFactory.GSThread;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadIndexer;

/**
 * Thread indexer for a multithread environment.
 * @author Toni Sagrista
 *
 */
public class MultiThreadIndexer extends ThreadIndexer {

    @Override
    public int idx() {
        return ((GSThread) Thread.currentThread()).index;
    }

    @Override
    public int nthreads() {
        return Runtime.getRuntime().availableProcessors();
    }

}
