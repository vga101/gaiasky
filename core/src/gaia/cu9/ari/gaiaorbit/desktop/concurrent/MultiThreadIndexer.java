package gaia.cu9.ari.gaiaorbit.desktop.concurrent;

import gaia.cu9.ari.gaiaorbit.desktop.concurrent.GaiaSkyThreadFactory.GSThread;

/**
 * Thread indexer for a multithread environment.
 * 
 * @author Toni Sagrista
 *
 */
public class MultiThreadIndexer extends ThreadIndexer {

    @Override
    public int idx() {
        Thread t = Thread.currentThread();
        if (t instanceof GSThread)
            return ((GSThread) Thread.currentThread()).index;
        else
            return 0;
    }

    @Override
    public int nthreads() {
        return Runtime.getRuntime().availableProcessors();
    }

}
