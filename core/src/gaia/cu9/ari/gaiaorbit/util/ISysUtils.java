package gaia.cu9.ari.gaiaorbit.util;

import java.io.File;

public interface ISysUtils {

    public String getAssetsLocation();

    /**
     * Gets the true path to the file.
     * If the given path is absolute, then it is 
     * returned immediately. Otherwise, the assets
     * location is prepended.
     * @param file The true path to access the file directly
     * @return
     */
    public String getTruePath(String file);

    public File getGSHomeDir();

    public File getDefaultCameraDir();

    public File getDefaultScreenshotsDir();

    public File getDefaultFramesDir();

    public File getDefaultScriptDir();

    public File getDefaultMusicDir();

    public File getDefaultMappingsDir();

    public boolean isLinux();

    public boolean isWindows();

    public boolean isMac();

    public boolean isUnix();

    public boolean isSolaris();

    /**
     * OS architecture
     * 
     * @return OS architecture: amd64, i386, etc.
     */
    public String getOSArchitecture();

    /**
     * Queries the version of the operating system
     * 
     * @return The operating system version
     */
    public String getOSVersion();

    /**
     * The name of the operating system
     * 
     * @return The name as in "windows 10", "linux", etc.
     */
    public String getOSName();

    /**
     * The operating system family: "linux", "win", "macos", "unix", "solaris"
     * or "unknown"
     * 
     * @return The family of the operating system
     */
    public String getOSFamily();
}
