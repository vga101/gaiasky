package gaia.cu9.ari.gaiaorbit.desktop;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.LifecycleListener;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.backends.lwjgl.LwjglFiles;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.brsanthu.googleanalytics.GoogleAnalyticsResponse;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.analytics.AnalyticsPermission;
import gaia.cu9.ari.gaiaorbit.analytics.AnalyticsReporting;
import gaia.cu9.ari.gaiaorbit.data.DesktopSceneGraphImplementationProvider;
import gaia.cu9.ari.gaiaorbit.data.SceneGraphImplementationProvider;
import gaia.cu9.ari.gaiaorbit.desktop.concurrent.MultiThreadIndexer;
import gaia.cu9.ari.gaiaorbit.desktop.concurrent.MultiThreadLocalFactory;
import gaia.cu9.ari.gaiaorbit.desktop.concurrent.ThreadPoolManager;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopDateFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopNumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.render.DesktopPostProcessorFactory;
import gaia.cu9.ari.gaiaorbit.desktop.render.ScreenModeCmd;
import gaia.cu9.ari.gaiaorbit.desktop.util.CamRecorder;
import gaia.cu9.ari.gaiaorbit.desktop.util.DesktopConfInit;
import gaia.cu9.ari.gaiaorbit.desktop.util.DesktopMusicActors;
import gaia.cu9.ari.gaiaorbit.desktop.util.DesktopNetworkChecker;
import gaia.cu9.ari.gaiaorbit.desktop.util.DesktopSysUtilsFactory;
import gaia.cu9.ari.gaiaorbit.desktop.util.MemInfoWindow;
import gaia.cu9.ari.gaiaorbit.desktop.util.RunCameraWindow;
import gaia.cu9.ari.gaiaorbit.desktop.util.RunScriptWindow;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.interfce.KeyBindings;
import gaia.cu9.ari.gaiaorbit.interfce.MusicActorsManager;
import gaia.cu9.ari.gaiaorbit.interfce.NetworkCheckerManager;
import gaia.cu9.ari.gaiaorbit.render.PostProcessorFactory;
import gaia.cu9.ari.gaiaorbit.screenshot.ScreenshotsManager;
import gaia.cu9.ari.gaiaorbit.script.JythonFactory;
import gaia.cu9.ari.gaiaorbit.script.ScriptingFactory;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.MusicManager;
import gaia.cu9.ari.gaiaorbit.util.SysUtilsFactory;
import gaia.cu9.ari.gaiaorbit.util.concurrent.SingleThreadIndexer;
import gaia.cu9.ari.gaiaorbit.util.concurrent.SingleThreadLocalFactory;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadIndexer;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadLocalFactory;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.math.MathManager;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

/**
 * Main class for the desktop launcher
 * 
 * @author Toni Sagrista
 *
 */
public class GaiaSkyDesktop implements IObserver {
    private static GaiaSkyDesktop gsd;
    public static String ASSETS_LOC;

    private MemInfoWindow memInfoWindow;

    public static void main(String[] args) {

        try {
            gsd = new GaiaSkyDesktop();
            // Assets location
            ASSETS_LOC = (System.getProperty("assets.location") != null ? System.getProperty("assets.location") : "");

            Gdx.files = new LwjglFiles();

            // Sys utils
            SysUtilsFactory.initialize(new DesktopSysUtilsFactory());

            // Initialize number format
            NumberFormatFactory.initialize(new DesktopNumberFormatFactory());

            // Initialize date format
            DateFormatFactory.initialize(new DesktopDateFormatFactory());

            // Init .gaiasky folder in user's home folder
            initUserDirectory();

            // Init properties file
            String props = System.getProperty("properties.file");
            if (props == null || props.isEmpty()) {
                props = initConfigFile(false);
            }

            // Init global configuration
            ConfInit.initialize(new DesktopConfInit(ASSETS_LOC));

            // Initialize i18n
            I18n.initialize(Gdx.files.internal("i18n/gsbundle"));

            // Dev mode
            I18n.initialize(Gdx.files.absolute(ASSETS_LOC + "i18n/gsbundle"));

            // Jython
            ScriptingFactory.initialize(JythonFactory.getInstance());

            // Fullscreen command
            ScreenModeCmd.initialize();

            // Init cam recorder
            CamRecorder.initialize();

            // Music actors
            MusicActorsManager.initialize(new DesktopMusicActors());

            // Init music manager
            MusicManager.initialize(Gdx.files.absolute(ASSETS_LOC + "music"), Gdx.files.absolute(SysUtilsFactory.getSysUtils().getDefaultMusicDir().getAbsolutePath()));

            // Initialize post processor factory
            PostProcessorFactory.initialize(new DesktopPostProcessorFactory());

            // Key mappings
            Constants.desktop = true;
            KeyBindings.initialize();

            // Scene graph implementation provider
            SceneGraphImplementationProvider.initialize(new DesktopSceneGraphImplementationProvider());

            // Initialize screenshots manager
            ScreenshotsManager.initialize();

            // Network checker
            NetworkCheckerManager.initialize(new DesktopNetworkChecker());

            // Analytics
            AnalyticsReporting.initialize(new AnalyticsPermission());
            AnalyticsReporting.getInstance().sendStartAppReport();

            // Math
            MathManager.initialize();

            gsd.init();
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

    }

    public static void setUIFont(javax.swing.plaf.FontUIResource f) {
        java.util.Enumeration<Object> keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
            Object key = keys.nextElement();
            Object value = UIManager.get(key);
            if (value != null && value instanceof javax.swing.plaf.FontUIResource && ((FontUIResource) value).getSize() > f.getSize()) {
                UIManager.put(key, f);
            }
        }
    }

    public GaiaSkyDesktop() {
        super();
        EventManager.instance.subscribe(this, Events.SHOW_ABOUT_ACTION, Events.SHOW_RUNSCRIPT_ACTION, Events.JAVA_EXCEPTION, Events.SHOW_PLAYCAMERA_ACTION, Events.DISPLAY_MEM_INFO_WINDOW);
    }

    private void init() {
        launchMainApp();
    }

    public void terminate() {
        System.exit(0);
    }

    public void launchMainApp() {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        LwjglApplicationConfiguration.disableAudio = false;
        cfg.title = GlobalConf.getFullApplicationName();
        cfg.fullscreen = GlobalConf.screen.FULLSCREEN;
        cfg.resizable = GlobalConf.screen.RESIZABLE;
        cfg.width = GlobalConf.screen.getScreenWidth();
        cfg.height = GlobalConf.screen.getScreenHeight();
        cfg.samples = MathUtilsd.clamp(GlobalConf.postprocess.POSTPROCESS_ANTIALIAS, 0, 16);
        cfg.vSyncEnabled = GlobalConf.screen.VSYNC;
        cfg.foregroundFPS = 0;
        cfg.backgroundFPS = 0;
        cfg.useHDPI = true;
        cfg.addIcon("icon/ic_launcher.png", Files.FileType.Internal);

        System.out.println("Display mode set to " + cfg.width + "x" + cfg.height + ", fullscreen: " + cfg.fullscreen);

        // Thread pool manager
        if (GlobalConf.performance.MULTITHREADING) {
            ThreadIndexer.initialize(new MultiThreadIndexer());
            ThreadPoolManager.initialize(GlobalConf.performance.NUMBER_THREADS());
            ThreadLocalFactory.initialize(new MultiThreadLocalFactory());
        } else {
            ThreadIndexer.initialize(new SingleThreadIndexer());
            ThreadLocalFactory.initialize(new SingleThreadLocalFactory());
        }

        // Launch app
        LwjglApplication app = new LwjglApplication(new GaiaSky(), cfg);
        app.addLifecycleListener(new GaiaSkyWindowListener());

        EventManager.instance.unsubscribe(this, Events.POST_NOTIFICATION, Events.JAVA_EXCEPTION);
    }

    RunScriptWindow scriptWindow = null;
    RunCameraWindow cameraWindow = null;

    @Override
    public void notify(Events event, final Object... data) {
        switch (event) {
        case SHOW_PLAYCAMERA_ACTION:
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if (cameraWindow == null)
                        cameraWindow = new RunCameraWindow((Stage) data[0], (Skin) data[1]);
                    cameraWindow.display();
                }
            });
            break;
        case SHOW_RUNSCRIPT_ACTION:
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    if (scriptWindow == null)
                        scriptWindow = new RunScriptWindow((Stage) data[0], (Skin) data[1]);
                    scriptWindow.display();
                }
            });

            break;
        case DISPLAY_MEM_INFO_WINDOW:
            if (memInfoWindow == null) {
                memInfoWindow = new MemInfoWindow((Stage) data[0], (Skin) data[1]);
            }
            memInfoWindow.show((Stage) data[0]);
            break;
        case JAVA_EXCEPTION:
            ((Throwable) data[0]).printStackTrace(System.err);
            break;
        default:
            break;
        }

    }

    private static void initUserDirectory() {
        SysUtilsFactory.getSysUtils().getGSHomeDir().mkdirs();
        SysUtilsFactory.getSysUtils().getDefaultFramesDir().mkdirs();
        SysUtilsFactory.getSysUtils().getDefaultScreenshotsDir().mkdirs();
        SysUtilsFactory.getSysUtils().getDefaultMusicDir().mkdirs();
        SysUtilsFactory.getSysUtils().getDefaultScriptDir().mkdirs();
        SysUtilsFactory.getSysUtils().getDefaultCameraDir().mkdirs();
    }

    /**
     * Initialises the configuration file. Tries to load first the file in
     * <code>$HOME/.gaiasky/global.properties</code>. Checks the
     * <code>properties.version</code> key to determine whether the file is
     * compatible or not. If it is, it uses the existing file. If it is not, it
     * replaces it with the default file.
     * 
     * @param ow
     *            Whether to overwrite
     * @return The path of the file used
     * @throws IOException
     */
    private static String initConfigFile(boolean ow) throws IOException {
        // Use user folder
        File userFolder = SysUtilsFactory.getSysUtils().getGSHomeDir();
        userFolder.mkdirs();
        File userFolderConfFile = new File(userFolder, "global.properties");

        // Internal config
        File confFolder = new File("conf" + File.separator);
        File internalFolderConfFile = new File(confFolder, "global.properties");

        boolean overwrite = ow;
        if (userFolderConfFile.exists()) {
            Properties userprops = new Properties();
            userprops.load(new FileInputStream(userFolderConfFile));
            int internalversion = 150;
            if (internalFolderConfFile.exists()) {
                Properties internalprops = new Properties();
                internalprops.load(new FileInputStream(internalFolderConfFile));
                internalversion = Integer.parseInt(internalprops.getProperty("properties.version"));
            }

            // Check latest version
            if (!userprops.containsKey("properties.version") || (userprops.containsKey("properties.version") && Integer.parseInt(userprops.getProperty("properties.version")) < internalversion)) {
                overwrite = true;
            }
        }

        if (overwrite || !userFolderConfFile.exists()) {
            // Copy file
            if (confFolder.exists() && confFolder.isDirectory()) {
                // Running released package
                copyFile(new File("conf" + File.separator + "global.properties"), userFolderConfFile, overwrite);
            } else {
                // Running from code?
                if (!new File("../android/assets/conf" + File.separator).exists()) {
                    throw new IOException("File ../android/assets/conf does not exist!");
                }
                copyFile(new File("../android/assets/conf" + File.separator + "global.properties"), userFolderConfFile, overwrite);
            }
        }
        String props = userFolderConfFile.getAbsolutePath();
        System.setProperty("properties.file", props);
        return props;
    }

    private static void copyFile(File sourceFile, File destFile, boolean ow) throws IOException {
        if (destFile.exists()) {
            if (ow) {
                // Overwrite, delete file
                destFile.delete();
            } else {
                return;
            }
        }
        // Create new
        destFile.createNewFile();

        FileChannel source = null;
        FileChannel destination = null;
        try {
            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
        } finally {
            if (source != null) {
                source.close();
            }
            if (destination != null) {
                destination.close();
            }
        }
    }

    private class GaiaSkyWindowListener implements LifecycleListener {

        @Override
        public void pause() {

        }

        @Override
        public void resume() {

        }

        @Override
        public void dispose() {
            // Terminate here

            // Analytics stop event
            Future<GoogleAnalyticsResponse> f1 = AnalyticsReporting.getInstance().sendTimingAppReport();

            try {
                f1.get(2000, TimeUnit.MILLISECONDS);
            } catch (Exception e) {
                Logger.error(e);
            }

        }
    }
}
