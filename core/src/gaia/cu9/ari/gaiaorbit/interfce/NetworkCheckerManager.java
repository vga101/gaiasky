package gaia.cu9.ari.gaiaorbit.interfce;

/**
 * Holds the network checker entity.
 * @author tsagrista
 *
 */
public class NetworkCheckerManager {

    private static INetworkChecker networkChecker;

    public static void initialize(INetworkChecker networkChecker) {
        NetworkCheckerManager.networkChecker = networkChecker;
    }

    public static INetworkChecker getNewtorkChecker() {
        return networkChecker;
    }
}
