package gaia.cu9.ari.gaiaorbit.desktop.util;

import java.io.File;

/**
 * Wee utility class to check the operating system and the desktop environment.
 * It also offers retrieval of common system folders.
 * @author Toni Sagrista
 *
 */
public class SysUtils {

    public static boolean checkLinuxDesktop(String desktop) {
        try {
            String value = System.getenv("DESKTOP_SESSION");
            return value != null && !value.isEmpty() && value.equalsIgnoreCase(desktop);
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
        return false;
    }

    public static boolean checkUnity() {
        return isLinux() && checkLinuxDesktop("ubuntu");
    }

    public static boolean checkGnome() {
        return isLinux() && checkLinuxDesktop("gnome");
    }

    public static boolean checkKDE() {
        return isLinux() && checkLinuxDesktop("kde");
    }

    public static boolean isLinux() {
        return getOSName().equalsIgnoreCase("linux");
    }

    public static String getOSName() {
        return System.getProperty("os.name");
    }

    public static String getOSArchitecture() {
        return System.getProperty("os.arch");
    }

    public static String getOSVersion() {
        return System.getProperty("os.version");
    }

    public static void main(String[] args) {
        System.out.println(getOSName());
        System.out.println("Unity: " + checkUnity());
        System.out.println("KDE: " + checkKDE());
        System.out.println("Gnome: " + checkGnome());
    }

    /**
     * Gets a file pointer to the home directory. It is $HOME/.gaiasky in Linux systems and C:\Users\$USERNAME\.gaiasky in Windows.
     * @return A pointer to the GaiaSandbox directory in the user's home.
     */
    public static File getGSHomeDir() {
        return new File(System.getProperty("user.home") + File.separator + GAIASKY_DIR_NAME + File.separator);
    }

    private static final String GAIASKY_DIR_NAME = ".gaiasky";
    private static final String CAMERA_DIR_NAME = "camera";
    private static final String SCREENSHOTS_DIR_NAME = "screenshots";
    private static final String FRAMES_DIR_NAME = "frames";
    private static final String SCRIPT_DIR_NAME = "script";

    /**
     * Gets a file pointer to the $HOME/.gaiasky/camera directory.
     * @return A pointer to the GaiaSandbox camera directory in the user's home.
     */
    public static File getGSCameraDir() {
        return new File(System.getProperty("user.home") + File.separator + GAIASKY_DIR_NAME + File.separator + CAMERA_DIR_NAME + File.separator);
    }

    /**
     * Gets a file pointer to the $HOME/.gaiasky/screenshots directory.
     * @return A pointer to the GaiaSandbox screenshots directory in the user's home.
     */
    public static File getDefaultScreenshotsDir() {
        return new File(System.getProperty("user.home") + File.separator + GAIASKY_DIR_NAME + File.separator + SCREENSHOTS_DIR_NAME + File.separator);
    }

    /**
     * Gets a file pointer to the $HOME/.gaiasky/frames directory.
     * @return A pointer to the GaiaSandbox frames directory in the user's home.
     */
    public static File getDefaultFramesDir() {
        return new File(System.getProperty("user.home") + File.separator + GAIASKY_DIR_NAME + File.separator + FRAMES_DIR_NAME + File.separator);
    }

    /**
     * Gets a file pointer to the $HOME/.gaiasky/script directory.
     * @return A pointer to the GaiaSandbox script directory in the user's home.
     */
    public static File getDefaultScriptDir() {
        return new File(System.getProperty("user.home") + File.separator + GAIASKY_DIR_NAME + File.separator + SCRIPT_DIR_NAME + File.separator);
    }

}
