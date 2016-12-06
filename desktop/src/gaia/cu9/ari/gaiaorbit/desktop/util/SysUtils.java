package gaia.cu9.ari.gaiaorbit.desktop.util;

import java.io.File;

/**
 * Wee utility class to check the operating system and the desktop environment.
 * It also offers retrieval of common system folders.
 * @author Toni Sagrista
 *
 */
public class SysUtils {

    private static String OS = System.getProperty("os.name").toLowerCase();

    public static boolean checkLinuxDesktop(String desktop) {
        try {
            String value = System.getenv("XDG_CURRENT_DESKTOP");
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

    public static boolean checkXfce() {
        return isLinux() && checkLinuxDesktop("xfce");
    }

    public static boolean isLinux() {

        return (OS.indexOf("linux") >= 0);

    }

    public static boolean isWindows() {

        return (OS.indexOf("win") >= 0);

    }

    public static boolean isMac() {

        return (OS.indexOf("mac") >= 0);

    }

    public static boolean isUnix() {

        return (OS.indexOf("unix") >= 0);

    }

    public static boolean isSolaris() {

        return (OS.indexOf("sunos") >= 0);

    }

    public static String getOSArchitecture() {
        return System.getProperty("os.arch");
    }

    public static String getOSVersion() {
        return System.getProperty("os.version");
    }

    public static void main(String[] args) {
        System.out.println(OS);
        System.out.println("Unity: " + checkUnity());
        System.out.println("KDE: " + checkKDE());
        System.out.println("Gnome: " + checkGnome());
        System.out.println("Xfce: " + checkXfce());
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
    private static final String MUSIC_DIR_NAME = "music";

    /**
     * Gets a file pointer to the $HOME/.gaiasky/camera directory.
     * @return A pointer to the GaiaSandbox camera directory in the user's home.
     */
    public static File getDefaultCameraDir() {
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

    /**
     * Gets a file pointer to the $HOME/.gaiasky/music directory.
     * @return A pointer to the GaiaSandbox music directory in the user's home.
     */
    public static File getDefaultMusicDir() {
        return new File(System.getProperty("user.home") + File.separator + GAIASKY_DIR_NAME + File.separator + MUSIC_DIR_NAME + File.separator);
    }

}
