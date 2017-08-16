package gaia.cu9.ari.gaiaorbit.desktop;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.data.SceneGraphImplementationProvider;
import gaia.cu9.ari.gaiaorbit.data.WebGLSceneGraphImplementationProvider;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopDateFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.format.DesktopNumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.desktop.render.WebGLPostProcessorFactory;
import gaia.cu9.ari.gaiaorbit.desktop.util.DesktopNetworkChecker;
import gaia.cu9.ari.gaiaorbit.desktop.util.WebGLConfInitLite;
import gaia.cu9.ari.gaiaorbit.interfce.NetworkCheckerManager;
import gaia.cu9.ari.gaiaorbit.render.PostProcessorFactory;
import gaia.cu9.ari.gaiaorbit.script.DummyFactory;
import gaia.cu9.ari.gaiaorbit.script.ScriptingFactory;
import gaia.cu9.ari.gaiaorbit.util.ConfInit;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.concurrent.SingleThreadIndexer;
import gaia.cu9.ari.gaiaorbit.util.concurrent.SingleThreadLocalFactory;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadIndexer;
import gaia.cu9.ari.gaiaorbit.util.concurrent.ThreadLocalFactory;
import gaia.cu9.ari.gaiaorbit.util.format.DateFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

public class GaiaSkyDesktopWebGL {

    public static void main(String[] args) throws Exception {
        NumberFormatFactory.initialize(new DesktopNumberFormatFactory());
        DateFormatFactory.initialize(new DesktopDateFormatFactory());
        ScriptingFactory.initialize(new DummyFactory());
        ConfInit.initialize(new WebGLConfInitLite());
        PostProcessorFactory.initialize(new WebGLPostProcessorFactory());
        ThreadIndexer.initialize(new SingleThreadIndexer());
        ThreadLocalFactory.initialize(new SingleThreadLocalFactory());
        SceneGraphImplementationProvider.initialize(new WebGLSceneGraphImplementationProvider());
        NetworkCheckerManager.initialize(new DesktopNetworkChecker());

        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle(GlobalConf.getFullApplicationName());
        cfg.setWindowedMode(1024, 600);
        cfg.setResizable(false);
        cfg.setBackBufferConfig(8, 8, 8, 8, 16, 0, MathUtilsd.clamp(GlobalConf.postprocess.POSTPROCESS_ANTIALIAS, 0, 16));
        cfg.setIdleFPS(0);
        cfg.useVsync(true);
        cfg.setWindowIcon(Files.FileType.Internal, "icon/ic_launcher.png");

        // Launch app
        new Lwjgl3Application(new GaiaSky(), cfg);
    }
}
