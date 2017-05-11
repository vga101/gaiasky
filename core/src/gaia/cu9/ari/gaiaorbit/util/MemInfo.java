package gaia.cu9.ari.gaiaorbit.util;

public class MemInfo {

    private static double BYTE_TO_MB = 1024 * 1024;

    public static double getUsedMemory() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / BYTE_TO_MB;
    }

    public static double getFreeMemory() {
        return (Runtime.getRuntime().freeMemory()) / BYTE_TO_MB;
    }

    public static double getTotalMemory() {
        return (Runtime.getRuntime().totalMemory()) / BYTE_TO_MB;
    }

    public static double getMaxMemory() {
        return (Runtime.getRuntime().maxMemory()) / BYTE_TO_MB;
    }

}
