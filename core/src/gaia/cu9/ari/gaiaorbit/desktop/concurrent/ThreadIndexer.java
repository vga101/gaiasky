package gaia.cu9.ari.gaiaorbit.desktop.concurrent;

/**
 * Class that returns the thread indices.
 * 
 * @author Toni Sagrista
 *
 */
public abstract class ThreadIndexer {

    public static ThreadIndexer instance;

    public static void initialize(ThreadIndexer inst) {
        ThreadIndexer.instance = inst;
    }

    public static int i() {
        return instance.idx();
    }

    /**
     * Gets the index of the current thread
     * 
     * @return The index
     */
    public abstract int idx();

    /**
     * Number of threads
     * 
     * @return The number of threads
     */
    public abstract int nthreads();

}
