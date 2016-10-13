package gaia.cu9.ari.gaiaorbit.android.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.DataConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.FrameConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.PerformanceConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.PostprocessConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.RuntimeConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.SceneConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ScreenConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ScreenshotConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ScreenshotMode;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.VersionConf;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.IDateFormat;

/**
 * Desktop GlobalConf initialiser, where the configuration comes from a
 * global.properties file.
 * @author tsagrista
 *
 */
public class AndroidConfInit extends ConfInit {
    CommentedProperties p;
    Properties vp;

    IDateFormat df = DateFormatFactory.getFormatter("dd/MM/yyyy HH:mm:ss");

    public AndroidConfInit() {
        super();
        try {
            String propsFileProperty = System.getProperty("properties.file");
            if (propsFileProperty == null || propsFileProperty.isEmpty()) {
                propsFileProperty = initConfigFile(false);
            }

            File confFile = new File(propsFileProperty);
            InputStream fis = new FileInputStream(confFile);
            // This should work for the normal execution
            InputStream vis = AndroidConfInit.class.getResourceAsStream("/version");
            if (vis == null) {
                // In case of running in 'developer' mode
                vis = new FileInputStream(new File(System.getProperty("assets.location") + "data/dummyversion"));
            }
            vp = new Properties();
            vp.load(vis);

            p = new CommentedProperties();
            p.load(fis);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public AndroidConfInit(InputStream fis, InputStream vis) {
        super();
        try {
            vp = new Properties();
            vp.load(vis);

            p = new CommentedProperties();
            p.load(fis);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    @Override
    public void initGlobalConf() throws Exception {

        // Scale factor
        GlobalConf.updateScaleFactor(1f);

        /** VERSION CONF **/
        VersionConf vc = new VersionConf();
        String versionStr = vp.getProperty("version");
        int[] majminrev = GlobalConf.version.getMajorMinorRevFromString(versionStr);
        vc.initialize(versionStr, vp.getProperty("buildtime"), vp.getProperty("builder"), vp.getProperty("system"), vp.getProperty("build"), majminrev[0], majminrev[1], majminrev[2]);

        /** PERFORMANCE CONF **/
        PerformanceConf pc = new PerformanceConf();
        boolean MULTITHREADING = Boolean.parseBoolean(p.getProperty("global.conf.multithreading"));
        String propNumthreads = p.getProperty("global.conf.numthreads");
        int NUMBER_THREADS = Integer.parseInt((propNumthreads == null || propNumthreads.isEmpty()) ? "0" : propNumthreads);
        pc.initialize(MULTITHREADING, NUMBER_THREADS);

        /** POSTPROCESS CONF **/
        PostprocessConf ppc = new PostprocessConf();
        int POSTPROCESS_ANTIALIAS = Integer.parseInt(p.getProperty("postprocess.antialiasing"));
        float POSTPROCESS_BLOOM_INTENSITY = Float.parseFloat(p.getProperty("postprocess.bloom.intensity"));
        float POSTPROCESS_MOTION_BLUR = Float.parseFloat(p.getProperty("postprocess.motionblur"));
        boolean POSTPROCESS_LENS_FLARE = Boolean.parseBoolean(p.getProperty("postprocess.lensflare"));
        boolean POSTPROCESS_LIGHT_SCATTERING = Boolean.parseBoolean(p.getProperty("postprocess.lightscattering", "false"));
        boolean POSTPROCESS_FISHEYE = Boolean.parseBoolean(p.getProperty("postprocess.fisheye", "false"));
        float POSTPROCESS_BRIGHTNESS = Float.parseFloat(p.getProperty("postprocess.brightness", "0"));
        float POSTPROCESS_CONTRAST = Float.parseFloat(p.getProperty("postprocess.contrast", "1"));
        ppc.initialize(POSTPROCESS_ANTIALIAS, POSTPROCESS_BLOOM_INTENSITY, POSTPROCESS_MOTION_BLUR, POSTPROCESS_LENS_FLARE, POSTPROCESS_LIGHT_SCATTERING, POSTPROCESS_FISHEYE, POSTPROCESS_BRIGHTNESS, POSTPROCESS_CONTRAST);

        /** RUNTIME CONF **/
        RuntimeConf rc = new RuntimeConf();
        rc.initialize(true, false, false, false, true, false, 20, false, false);

        /** DATA CONF **/
        DataConf dc = new DataConf();
        boolean DATA_SOURCE_LOCAL = Boolean.parseBoolean(p.getProperty("data.source.objectserver"));

        String CATALOG_JSON_FILE = p.getProperty("data.json.catalog");
        String HYG_JSON_FILE = p.getProperty("data.json.catalog.hyg");
        String TGAS_JSON_FILE = p.getProperty("data.json.catalog.tgas");

        String OBJECTS_JSON_FILE = p.getProperty("data.json.objects");
        List<String> files = new ArrayList<String>();
        int i = 0;
        String gqualityFile;
        while ((gqualityFile = p.getProperty("data.json.objects.gq." + i)) != null) {
            files.add(gqualityFile);
            i++;
        }
        String[] OBJECTS_JSON_FILE_GQ = new String[files.size()];
        OBJECTS_JSON_FILE_GQ = files.toArray(OBJECTS_JSON_FILE_GQ);
        String OBJECT_SERVER_HOSTNAME = p.getProperty("data.source.hostname");
        int OBJECT_SERVER_PORT = Integer.parseInt(p.getProperty("data.source.port"));
        String VISUALIZATION_ID = p.getProperty("data.source.visid");
        boolean REAL_GAIA_ATTITUDE = Boolean.parseBoolean(p.getProperty("data.attitude.real"));

        float LIMIT_MAG_LOAD;
        if (p.getProperty("data.limit.mag") != null && !p.getProperty("data.limit.mag").isEmpty()) {
            LIMIT_MAG_LOAD = Float.parseFloat(p.getProperty("data.limit.mag"));
        } else {
            LIMIT_MAG_LOAD = Float.MAX_VALUE;
        }
        dc.initialize(DATA_SOURCE_LOCAL, CATALOG_JSON_FILE, HYG_JSON_FILE, TGAS_JSON_FILE, OBJECTS_JSON_FILE, OBJECTS_JSON_FILE_GQ, OBJECT_SERVER_HOSTNAME, OBJECT_SERVER_PORT, VISUALIZATION_ID, LIMIT_MAG_LOAD, REAL_GAIA_ATTITUDE);

        /** PROGRAM CONF **/
        ProgramConf prc = new ProgramConf();
        String LOCALE = p.getProperty("program.locale");

        boolean DISPLAY_TUTORIAL = Boolean.parseBoolean(p.getProperty("program.tutorial"));
        String TUTORIAL_SCRIPT_LOCATION = p.getProperty("program.tutorial.script");
        boolean SHOW_CONFIG_DIALOG = Boolean.parseBoolean(p.getProperty("program.configdialog"));
        boolean SHOW_DEBUG_INFO = Boolean.parseBoolean(p.getProperty("program.debuginfo"));
        Date LAST_CHECKED;
        try {
            LAST_CHECKED = df.parse(p.getProperty("program.lastchecked"));
        } catch (Exception e) {
            LAST_CHECKED = null;
        }
        String LAST_VERSION = p.getProperty("program.lastversion", "0.0.0");
        String VERSION_CHECK_URL = p.getProperty("program.versioncheckurl");
        String UI_THEME = p.getProperty("program.ui.theme");
        String SCRIPT_LOCATION = p.getProperty("program.scriptlocation").isEmpty() ? System.getProperty("user.dir") : p.getProperty("program.scriptlocation");

        boolean STEREOSCOPIC_MODE = Boolean.parseBoolean(p.getProperty("program.stereoscopic"));
        StereoProfile STEREO_PROFILE = StereoProfile.values()[Integer.parseInt(p.getProperty("program.stereoscopic.profile"))];
        boolean CUBEMAPE360_MODE = Boolean.parseBoolean(p.getProperty("program.cubemap360", "False"));
        prc.initialize(DISPLAY_TUTORIAL, TUTORIAL_SCRIPT_LOCATION, SHOW_CONFIG_DIALOG, SHOW_DEBUG_INFO, LAST_CHECKED, LAST_VERSION, VERSION_CHECK_URL, UI_THEME, SCRIPT_LOCATION, LOCALE, STEREOSCOPIC_MODE, STEREO_PROFILE, CUBEMAPE360_MODE);

        /** SCENE CONF **/
        int GRAPHICS_QUALITY = Integer.parseInt(p.getProperty("scene.graphics.quality"));
        long OBJECT_FADE_MS = Long.parseLong(p.getProperty("scene.object.fadems"));
        float STAR_BRIGHTNESS = Float.parseFloat(p.getProperty("scene.star.brightness"));
        float AMBIENT_LIGHT = Float.parseFloat(p.getProperty("scene.ambient"));
        int CAMERA_FOV = Integer.parseInt(p.getProperty("scene.camera.fov"));
        int CAMERA_SPEED_LIMIT_IDX = Integer.parseInt(p.getProperty("scene.camera.speedlimit"));
        float CAMERA_SPEED = Float.parseFloat(p.getProperty("scene.camera.focus.vel"));
        boolean FOCUS_LOCK = Boolean.parseBoolean(p.getProperty("scene.focuslock"));
        boolean FOCUS_LOCK_ORIENTATION = Boolean.parseBoolean(p.getProperty("scene.focuslock.orientation", "false"));
        float TURNING_SPEED = Float.parseFloat(p.getProperty("scene.camera.turn.vel"));
        float ROTATION_SPEED = Float.parseFloat(p.getProperty("scene.camera.rotate.vel"));
        float LABEL_NUMBER_FACTOR = Float.parseFloat(p.getProperty("scene.labelfactor"));
        double STAR_TH_ANGLE_QUAD = Double.parseDouble(p.getProperty("scene.star.threshold.quad"));
        double STAR_TH_ANGLE_POINT = Double.parseDouble(p.getProperty("scene.star.threshold.point"));
        double STAR_TH_ANGLE_NONE = Double.parseDouble(p.getProperty("scene.star.threshold.none"));
        float POINT_ALPHA_MIN = Float.parseFloat(p.getProperty("scene.point.alpha.min"));
        float POINT_ALPHA_MAX = Float.parseFloat(p.getProperty("scene.point.alpha.max"));
        int PIXEL_RENDERER = Integer.parseInt(p.getProperty("scene.renderer.star"));
        int LINE_RENDERER = Integer.parseInt(p.getProperty("scene.renderer.line"));
        boolean OCTREE_PARTICLE_FADE = Boolean.parseBoolean(p.getProperty("scene.octree.particle.fade"));
        float OCTANT_THRESHOLD_0 = Float.parseFloat(p.getProperty("scene.octant.threshold.0"));
        float OCTANT_THRESHOLD_1 = Float.parseFloat(p.getProperty("scene.octant.threshold.1"));
        boolean PROPER_MOTION_VECTORS = Boolean.parseBoolean(p.getProperty("scene.propermotion.vectors", "true"));
        float PM_NUM_FACTOR = Float.parseFloat(p.getProperty("scene.propermotion.numfactor", "20f"));
        float PM_LEN_FACTOR = Float.parseFloat(p.getProperty("scene.propermotion.lenfactor", "1E1f"));
        boolean GALAXY_3D = Boolean.parseBoolean(p.getProperty("scene.galaxy.3d", "true"));
        boolean CROSSHAIR = Boolean.parseBoolean(p.getProperty("scene.crosshair", "true"));
        int CUBEMAP_FACE_RESOLUTION = Integer.parseInt(p.getProperty("scene.cubemapface.resolution", "1000"));
        //Visibility of components
        ComponentType[] cts = ComponentType.values();
        boolean[] VISIBILITY = new boolean[cts.length];
        for (ComponentType ct : cts) {
            String key = "scene.visibility." + ct.name();
            if (p.containsKey(key)) {
                VISIBILITY[ct.ordinal()] = Boolean.parseBoolean(p.getProperty(key));
            }
        }
        float STAR_POINT_SIZE = Float.parseFloat(p.getProperty("scene.star.point.size", "-1"));
        SceneConf sc = new SceneConf();
        sc.initialize(GRAPHICS_QUALITY, OBJECT_FADE_MS, STAR_BRIGHTNESS, AMBIENT_LIGHT, CAMERA_FOV, CAMERA_SPEED, TURNING_SPEED, ROTATION_SPEED, CAMERA_SPEED_LIMIT_IDX, FOCUS_LOCK, FOCUS_LOCK_ORIENTATION, LABEL_NUMBER_FACTOR, VISIBILITY, PIXEL_RENDERER, LINE_RENDERER, STAR_TH_ANGLE_NONE, STAR_TH_ANGLE_POINT, STAR_TH_ANGLE_QUAD, POINT_ALPHA_MIN, POINT_ALPHA_MAX, OCTREE_PARTICLE_FADE, OCTANT_THRESHOLD_0, OCTANT_THRESHOLD_1, PROPER_MOTION_VECTORS, PM_NUM_FACTOR, PM_LEN_FACTOR, STAR_POINT_SIZE, GALAXY_3D, CUBEMAP_FACE_RESOLUTION, CROSSHAIR);

        /** FRAME CONF **/
        String renderFolder = null;
        if (p.getProperty("graphics.render.folder") == null || p.getProperty("graphics.render.folder").isEmpty()) {
            File framesDir = SysUtils.getDefaultFramesDir();
            framesDir.mkdirs();
            renderFolder = framesDir.getAbsolutePath();
        } else {
            renderFolder = p.getProperty("graphics.render.folder");
        }
        String RENDER_FOLDER = renderFolder;
        String RENDER_FILE_NAME = p.getProperty("graphics.render.filename");
        int RENDER_WIDTH = Integer.parseInt(p.getProperty("graphics.render.width"));
        int RENDER_HEIGHT = Integer.parseInt(p.getProperty("graphics.render.height"));
        int RENDER_TARGET_FPS = Integer.parseInt(p.getProperty("graphics.render.targetfps", "60"));
        int CAMERA_REC_TARGET_FPS = Integer.parseInt(p.getProperty("graphics.camera.recording.targetfps", "60"));
        boolean RENDER_SCREENSHOT_TIME = Boolean.parseBoolean(p.getProperty("graphics.render.time"));
        ScreenshotMode FRAME_MODE = ScreenshotMode.valueOf(p.getProperty("graphics.render.mode"));
        FrameConf fc = new FrameConf();
        fc.initialize(RENDER_WIDTH, RENDER_HEIGHT, RENDER_TARGET_FPS, CAMERA_REC_TARGET_FPS, RENDER_FOLDER, RENDER_FILE_NAME, RENDER_SCREENSHOT_TIME, RENDER_SCREENSHOT_TIME, FRAME_MODE);

        /** SCREEN CONF **/
        int SCREEN_WIDTH = Integer.parseInt(p.getProperty("graphics.screen.width"));
        int SCREEN_HEIGHT = Integer.parseInt(p.getProperty("graphics.screen.height"));
        int FULLSCREEN_WIDTH = Integer.parseInt(p.getProperty("graphics.screen.fullscreen.width"));
        int FULLSCREEN_HEIGHT = Integer.parseInt(p.getProperty("graphics.screen.fullscreen.height"));
        boolean FULLSCREEN = Boolean.parseBoolean(p.getProperty("graphics.screen.fullscreen"));
        boolean RESIZABLE = Boolean.parseBoolean(p.getProperty("graphics.screen.resizable"));
        boolean VSYNC = Boolean.parseBoolean(p.getProperty("graphics.screen.vsync"));
        boolean SCREEN_OUTPUT = Boolean.parseBoolean(p.getProperty("graphics.screen.screenoutput"));
        ScreenConf scrc = new ScreenConf();
        scrc.initialize(SCREEN_WIDTH, SCREEN_HEIGHT, FULLSCREEN_WIDTH, FULLSCREEN_HEIGHT, FULLSCREEN, RESIZABLE, VSYNC, SCREEN_OUTPUT);

        /** SCREENSHOT CONF **/
        String screenshotFolder = null;
        if (p.getProperty("screenshot.folder") == null || p.getProperty("screenshot.folder").isEmpty()) {
            File screenshotDir = SysUtils.getDefaultScreenshotsDir();
            screenshotDir.mkdirs();
            screenshotFolder = screenshotDir.getAbsolutePath();
        } else {
            screenshotFolder = p.getProperty("screenshot.folder");
        }
        String SCREENSHOT_FOLDER = screenshotFolder;
        int SCREENSHOT_WIDTH = Integer.parseInt(p.getProperty("screenshot.width"));
        int SCREENSHOT_HEIGHT = Integer.parseInt(p.getProperty("screenshot.height"));
        ScreenshotMode SCREENSHOT_MODE = ScreenshotMode.valueOf(p.getProperty("screenshot.mode"));
        ScreenshotConf shc = new ScreenshotConf();
        shc.initialize(SCREENSHOT_WIDTH, SCREENSHOT_HEIGHT, SCREENSHOT_FOLDER, SCREENSHOT_MODE);

        /** INIT GLOBAL CONF **/
        GlobalConf.initialize(vc, prc, sc, dc, rc, ppc, pc, fc, scrc, shc);

    }

    @Override
    public void persistGlobalConf(File propsFile) {

        /** SCREENSHOT **/
        p.setProperty("screenshot.folder", GlobalConf.screenshot.SCREENSHOT_FOLDER);
        p.setProperty("screenshot.width", Integer.toString(GlobalConf.screenshot.SCREENSHOT_WIDTH));
        p.setProperty("screenshot.height", Integer.toString(GlobalConf.screenshot.SCREENSHOT_HEIGHT));
        p.setProperty("screenshot.mode", GlobalConf.screenshot.SCREENSHOT_MODE.toString());

        /** PERFORMANCE **/
        p.setProperty("global.conf.multithreading", Boolean.toString(GlobalConf.performance.MULTITHREADING));
        p.setProperty("global.conf.numthreads", Integer.toString(GlobalConf.performance.NUMBER_THREADS));

        /** POSTPROCESS **/
        p.setProperty("postprocess.antialiasing", Integer.toString(GlobalConf.postprocess.POSTPROCESS_ANTIALIAS));
        p.setProperty("postprocess.bloom.intensity", Float.toString(GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY));
        p.setProperty("postprocess.motionblur", Float.toString(GlobalConf.postprocess.POSTPROCESS_MOTION_BLUR));
        p.setProperty("postprocess.lensflare", Boolean.toString(GlobalConf.postprocess.POSTPROCESS_LENS_FLARE));
        p.setProperty("postprocess.lightscattering", Boolean.toString(GlobalConf.postprocess.POSTPROCESS_LIGHT_SCATTERING));
        p.setProperty("postprocess.brightness", Float.toString(GlobalConf.postprocess.POSTPROCESS_BRIGHTNESS));
        p.setProperty("postprocess.contrast", Float.toString(GlobalConf.postprocess.POSTPROCESS_CONTRAST));

        /** FRAME CONF **/
        p.setProperty("graphics.render.folder", GlobalConf.frame.RENDER_FOLDER);
        p.setProperty("graphics.render.filename", GlobalConf.frame.RENDER_FILE_NAME);
        p.setProperty("graphics.render.width", Integer.toString(GlobalConf.frame.RENDER_WIDTH));
        p.setProperty("graphics.render.height", Integer.toString(GlobalConf.frame.RENDER_HEIGHT));
        p.setProperty("graphics.render.targetfps", Integer.toString(GlobalConf.frame.RENDER_TARGET_FPS));
        p.setProperty("graphics.camera.recording.targetfps", Integer.toString(GlobalConf.frame.CAMERA_REC_TARGET_FPS));
        p.setProperty("graphics.render.time", Boolean.toString(GlobalConf.frame.RENDER_SCREENSHOT_TIME));
        p.setProperty("graphics.render.mode", GlobalConf.frame.FRAME_MODE.toString());

        /** DATA **/
        p.setProperty("data.json.catalog", GlobalConf.data.CATALOG_JSON_FILE);
        p.setProperty("data.json.objects", GlobalConf.data.OBJECTS_JSON_FILE);
        p.setProperty("data.source.objectserver", Boolean.toString(GlobalConf.data.OBJECT_SERVER_CONNECTION));
        p.setProperty("data.source.hostname", GlobalConf.data.OBJECT_SERVER_HOSTNAME);
        p.setProperty("data.source.port", Integer.toString(GlobalConf.data.OBJECT_SERVER_PORT));
        p.setProperty("data.source.visid", GlobalConf.data.VISUALIZATION_ID);
        p.setProperty("data.limit.mag", Float.toString(GlobalConf.data.LIMIT_MAG_LOAD));
        p.setProperty("data.attitude.real", Boolean.toString(GlobalConf.data.REAL_GAIA_ATTITUDE));

        /** SCREEN **/
        p.setProperty("graphics.screen.width", Integer.toString(GlobalConf.screen.SCREEN_WIDTH));
        p.setProperty("graphics.screen.height", Integer.toString(GlobalConf.screen.SCREEN_HEIGHT));
        p.setProperty("graphics.screen.fullscreen.width", Integer.toString(GlobalConf.screen.FULLSCREEN_WIDTH));
        p.setProperty("graphics.screen.fullscreen.height", Integer.toString(GlobalConf.screen.FULLSCREEN_HEIGHT));
        p.setProperty("graphics.screen.fullscreen", Boolean.toString(GlobalConf.screen.FULLSCREEN));
        p.setProperty("graphics.screen.resizable", Boolean.toString(GlobalConf.screen.RESIZABLE));
        p.setProperty("graphics.screen.vsync", Boolean.toString(GlobalConf.screen.VSYNC));
        p.setProperty("graphics.screen.screenoutput", Boolean.toString(GlobalConf.screen.SCREEN_OUTPUT));

        /** PROGRAM **/
        p.setProperty("program.tutorial", Boolean.toString(GlobalConf.program.DISPLAY_TUTORIAL));
        p.setProperty("program.tutorial.script", GlobalConf.program.TUTORIAL_SCRIPT_LOCATION);
        p.setProperty("program.configdialog", Boolean.toString(GlobalConf.program.SHOW_CONFIG_DIALOG));
        p.setProperty("program.debuginfo", Boolean.toString(GlobalConf.program.SHOW_DEBUG_INFO));
        p.setProperty("program.lastchecked", GlobalConf.program.LAST_CHECKED != null ? df.format(GlobalConf.program.LAST_CHECKED) : "");
        p.setProperty("program.lastversion", GlobalConf.program.LAST_VERSION != null ? GlobalConf.program.LAST_VERSION : "");
        p.setProperty("program.versioncheckurl", GlobalConf.program.VERSION_CHECK_URL);
        p.setProperty("program.ui.theme", GlobalConf.program.UI_THEME);
        p.setProperty("program.scriptlocation", GlobalConf.program.SCRIPT_LOCATION);
        p.setProperty("program.locale", GlobalConf.program.LOCALE);
        p.setProperty("program.stereoscopic", Boolean.toString(GlobalConf.program.STEREOSCOPIC_MODE));
        p.setProperty("program.stereoscopic.profile", Integer.toString(GlobalConf.program.STEREO_PROFILE.ordinal()));
        p.setProperty("program.cubemap360", Boolean.toString(GlobalConf.program.CUBEMAP360_MODE));

        /** SCENE **/
        p.setProperty("scene.graphics.quality", Integer.toString(GlobalConf.scene.GRAPHICS_QUALITY));
        p.setProperty("scene.object.fadems", Long.toString(GlobalConf.scene.OBJECT_FADE_MS));
        p.setProperty("scene.star.brightness", Float.toString(GlobalConf.scene.STAR_BRIGHTNESS));
        p.setProperty("scene.ambient", Float.toString(GlobalConf.scene.AMBIENT_LIGHT));
        p.setProperty("scene.camera.fov", Integer.toString(GlobalConf.scene.CAMERA_FOV));
        p.setProperty("scene.camera.speedlimit", Integer.toString(GlobalConf.scene.CAMERA_SPEED_LIMIT_IDX));
        p.setProperty("scene.camera.focus.vel", Float.toString(GlobalConf.scene.CAMERA_SPEED));
        p.setProperty("scene.camera.turn.vel", Float.toString(GlobalConf.scene.TURNING_SPEED));
        p.setProperty("scene.camera.rotate.vel", Float.toString(GlobalConf.scene.ROTATION_SPEED));
        p.setProperty("scene.focuslock", Boolean.toString(GlobalConf.scene.FOCUS_LOCK));
        p.setProperty("scene.focuslock.orientation", Boolean.toString(GlobalConf.scene.FOCUS_LOCK_ORIENTATION));
        p.setProperty("scene.labelfactor", Float.toString(GlobalConf.scene.LABEL_NUMBER_FACTOR));
        p.setProperty("scene.star.threshold.quad", Double.toString(GlobalConf.scene.STAR_THRESHOLD_QUAD));
        p.setProperty("scene.star.threshold.point", Double.toString(GlobalConf.scene.STAR_THRESHOLD_POINT));
        p.setProperty("scene.star.threshold.none", Double.toString(GlobalConf.scene.STAR_THRESHOLD_NONE));
        p.setProperty("scene.star.point.size", Float.toString(GlobalConf.scene.STAR_POINT_SIZE));
        p.setProperty("scene.point.alpha.min", Float.toString(GlobalConf.scene.POINT_ALPHA_MIN));
        p.setProperty("scene.point.alpha.max", Float.toString(GlobalConf.scene.POINT_ALPHA_MAX));
        p.setProperty("scene.renderer.star", Integer.toString(GlobalConf.scene.PIXEL_RENDERER));
        p.setProperty("scene.renderer.line", Integer.toString(GlobalConf.scene.LINE_RENDERER));
        p.setProperty("scene.octree.particle.fade", Boolean.toString(GlobalConf.scene.OCTREE_PARTICLE_FADE));
        p.setProperty("scene.octant.threshold.0", Float.toString(GlobalConf.scene.OCTANT_THRESHOLD_0));
        p.setProperty("scene.octant.threshold.1", Float.toString(GlobalConf.scene.OCTANT_THRESHOLD_1));
        p.setProperty("scene.propermotion.vectors", Boolean.toString(GlobalConf.scene.PROPER_MOTION_VECTORS));
        p.setProperty("scene.propermotion.numfactor", Float.toString(GlobalConf.scene.PM_NUM_FACTOR));
        p.setProperty("scene.propermotion.lenfactor", Float.toString(GlobalConf.scene.PM_LEN_FACTOR));
        p.setProperty("scene.galaxy.3d", Boolean.toString(GlobalConf.scene.GALAXY_3D));
        p.setProperty("scene.cubemapface.resolution", Integer.toString(GlobalConf.scene.CUBEMAP_FACE_RESOLUTION));

        // Visibility of components
        int idx = 0;
        ComponentType[] cts = ComponentType.values();
        for (boolean b : GlobalConf.scene.VISIBILITY) {
            ComponentType ct = cts[idx];
            p.setProperty("scene.visibility." + ct.name(), Boolean.toString(b));
            idx++;
        }

        try {
            FileOutputStream fos = new FileOutputStream(propsFile);
            p.store(fos, null);
            fos.close();
            Logger.info("Configuration saved to " + propsFile.getAbsolutePath());
        } catch (Exception e) {
            Logger.error(e);
        }

    }

    private String initConfigFile(boolean ow) throws IOException {
        // Use user folder
        File userFolder = SysUtils.getGSHomeDir();
        userFolder.mkdirs();
        File userFolderConfFile = new File(userFolder, "global.properties");

        if (ow || !userFolderConfFile.exists()) {
            // Copy file
            copyFile(new File("conf" + File.separator + "global.properties"), userFolderConfFile, ow);
        }
        String props = userFolderConfFile.getAbsolutePath();
        System.setProperty("properties.file", props);
        return props;
    }

    private void copyFile(File sourceFile, File destFile, boolean ow) throws IOException {
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

}
