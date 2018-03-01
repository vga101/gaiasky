package gaia.cu9.ari.gaiaorbit.desktop.util;

import java.io.File;

import gaia.cu9.ari.gaiaorbit.util.ISysUtils;
import gaia.cu9.ari.gaiaorbit.util.SysUtilsFactory;

/**
 * Wee utility class to check the operating system and the desktop environment.
 * It also offers retrieval of common system folders.
 * 
 * @author Toni Sagrista
 *
 */
public class SysUtils implements ISysUtils {

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

    public boolean checkUnity() {
        return isLinux() && checkLinuxDesktop("ubuntu");
    }

    public boolean checkGnome() {
        return isLinux() && checkLinuxDesktop("gnome");
    }

    public boolean checkKDE() {
        return isLinux() && checkLinuxDesktop("kde");
    }

    public boolean checkXfce() {
        return isLinux() && checkLinuxDesktop("xfce");
    }

    public boolean checkBudgie() {
        return isLinux() && checkLinuxDesktop("budgie:GNOME");
    }

    public String getOSName() {
        return OS;
    }

    public String getOSFamily() {
        if (isLinux())
            return "linux";
        if (isWindows())
            return "win";
        if (isMac())
            return "macos";
        if (isUnix())
            return "unix";
        if (isSolaris())
            return "solaris";

        return "unknown";
    }

    public boolean isLinux() {
        return (OS.indexOf("linux") >= 0);
    }

    public boolean isWindows() {
        return (OS.indexOf("win") >= 0);
    }

    public boolean isMac() {
        return (OS.indexOf("mac") >= 0);
    }

    public boolean isUnix() {
        return (OS.indexOf("unix") >= 0);
    }

    public boolean isSolaris() {
        return (OS.indexOf("sunos") >= 0);
    }

    public String getOSArchitecture() {
        return System.getProperty("os.arch");
    }

    public String getOSVersion() {
        return System.getProperty("os.version");
    }


    public String getAssetsLocation() {
        return System.getProperty("assets.location") != null ? System.getProperty("assets.location") : "";
    }

    public String getTruePath(String file) {
        return (new File(file)).isAbsolute() ? file : SysUtilsFactory.getSysUtils().getAssetsLocation() + File.separator + file;
    }

    /**
     * Gets a file pointer to the home directory. It is $HOME/.gaiasky in Linux
     * systems and C:\Users\$USERNAME\.gaiasky in Windows.
     * 
     * @return A pointer to the Gaia Sky directory in the user's home.
     */
    public File getGSHomeDir() {
        return new File(System.getProperty("user.home") + File.separator + GAIASKY_DIR_NAME + File.separator);
    }

    private static final String GAIASKY_DIR_NAME = ".gaiasky";
    private static final String CAMERA_DIR_NAME = "camera";
    private static final String SCREENSHOTS_DIR_NAME = "screenshots";
    private static final String FRAMES_DIR_NAME = "frames";
    private static final String SCRIPT_DIR_NAME = "script";
    private static final String MUSIC_DIR_NAME = "music";
    private static final String MAPPINGS_DIR_NAME = "mappings";

    /**
     * Gets a file pointer to the $HOME/.gaiasky/camera directory.
     * 
     * @return A pointer to the Gaia Sky camera directory in the user's home.
     */
    public File getDefaultCameraDir() {
        return new File(System.getProperty("user.home") + File.separator + GAIASKY_DIR_NAME + File.separator + CAMERA_DIR_NAME + File.separator);
    }

    /**
     * Gets a file pointer to the $HOME/.gaiasky/screenshots directory.
     * 
     * @return A pointer to the Gaia Sky screenshots directory in the user's
     *         home.
     */
    public File getDefaultScreenshotsDir() {
        return new File(System.getProperty("user.home") + File.separator + GAIASKY_DIR_NAME + File.separator + SCREENSHOTS_DIR_NAME + File.separator);
    }

    /**
     * Gets a file pointer to the $HOME/.gaiasky/frames directory.
     * 
     * @return A pointer to the Gaia Sky frames directory in the user's home.
     */
    public File getDefaultFramesDir() {
        return new File(System.getProperty("user.home") + File.separator + GAIASKY_DIR_NAME + File.separator + FRAMES_DIR_NAME + File.separator);
    }

    /**
     * Gets a file pointer to the $HOME/.gaiasky/script directory.
     * 
     * @return A pointer to the Gaia Sky script directory in the user's home.
     */
    public File getDefaultScriptDir() {
        return new File(System.getProperty("user.home") + File.separator + GAIASKY_DIR_NAME + File.separator + SCRIPT_DIR_NAME + File.separator);
    }

    /**
     * Gets a file pointer to the $HOME/.gaiasky/music directory.
     * 
     * @return A pointer to the Gaia Sky music directory in the user's home.
     */
    public File getDefaultMusicDir() {
        return new File(System.getProperty("user.home") + File.separator + GAIASKY_DIR_NAME + File.separator + MUSIC_DIR_NAME + File.separator);
    }

    /**
     * Gets a file pointer to the $HOME/.gaiasky/mappings directory.
     * 
     * @return A pointer to the Gaia Sky mappings directory in the user's home.
     */
    public File getDefaultMappingsDir() {
        return new File(System.getProperty("user.home") + File.separator + GAIASKY_DIR_NAME + File.separator + MAPPINGS_DIR_NAME + File.separator);
    }

    public static void main(String[] args) {
        SysUtils su = new SysUtils();
        System.out.println(OS);
        System.out.println("Unity: " + su.checkUnity());
        System.out.println("KDE: " + su.checkKDE());
        System.out.println("Gnome: " + su.checkGnome());
        System.out.println("Xfce: " + su.checkXfce());
        System.out.println("Budgie: " + su.checkBudgie());
    }

}
