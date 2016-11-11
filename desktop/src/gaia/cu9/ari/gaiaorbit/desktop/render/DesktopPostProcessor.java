package gaia.cu9.ari.gaiaorbit.desktop.render;

import java.util.Arrays;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.Curvature;
import com.bitfire.postprocessing.effects.Fisheye;
import com.bitfire.postprocessing.effects.Fxaa;
import com.bitfire.postprocessing.effects.LensFlare2;
import com.bitfire.postprocessing.effects.Levels;
import com.bitfire.postprocessing.effects.LightGlow;
import com.bitfire.postprocessing.effects.MotionBlur;
import com.bitfire.postprocessing.effects.Nfaa;
import com.bitfire.postprocessing.filters.Glow;
import com.bitfire.utils.ShaderLoader;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

public class DesktopPostProcessor implements IPostProcessor, IObserver {

    private PostProcessBean[] pps;

    float bloomFboScale = 0.5f;

    float flareIntensity = 0.6f;

    // Number of flares
    int nghosts;

    Vector3d auxd, prevCampos;
    Vector3 auxf;
    Matrix4 prevViewProj, prevCombined;

    public DesktopPostProcessor() {
        ShaderLoader.BasePath = "shaders/";

        auxd = new Vector3d();
        auxf = new Vector3();
        prevCampos = new Vector3d();
        prevViewProj = new Matrix4();
        prevCombined = new Matrix4();

        pps = new PostProcessBean[RenderType.values().length];

        pps[RenderType.screen.index] = newPostProcessor(getWidth(RenderType.screen), getHeight(RenderType.screen));
        if (Constants.desktop) {
            pps[RenderType.screenshot.index] = newPostProcessor(getWidth(RenderType.screenshot), getHeight(RenderType.screenshot));
            pps[RenderType.frame.index] = newPostProcessor(getWidth(RenderType.frame), getHeight(RenderType.frame));
        }

        EventManager.instance.subscribe(this, Events.PROPERTIES_WRITTEN, Events.BLOOM_CMD, Events.LENS_FLARE_CMD, Events.MOTION_BLUR_CMD, Events.LIGHT_POS_2D_UPDATED, Events.LIGHT_SCATTERING_CMD, Events.TOGGLE_STEREOSCOPIC_CMD, Events.TOGGLE_STEREO_PROFILE_CMD, Events.FISHEYE_CMD, Events.CAMERA_MOTION_UPDATED, Events.CUBEMAP360_CMD, Events.ANTIALIASING_CMD, Events.BRIGHTNESS_CMD, Events.CONTRAST_CMD);

    }

    private int getWidth(RenderType type) {
        switch (type) {
        case screen:
            return Gdx.graphics.getWidth();
        case screenshot:
            return GlobalConf.screenshot.SCREENSHOT_WIDTH;
        case frame:
            return GlobalConf.frame.RENDER_WIDTH;
        }
        return 0;
    }

    private int getHeight(RenderType type) {
        switch (type) {
        case screen:
            return Gdx.graphics.getHeight();
        case screenshot:
            return GlobalConf.screenshot.SCREENSHOT_HEIGHT;
        case frame:
            return GlobalConf.frame.RENDER_HEIGHT;
        }
        return 0;
    }

    private PostProcessBean newPostProcessor(int width, int height) {
        PostProcessBean ppb = new PostProcessBean();

        float ar = (float) width / (float) height;

        ppb.pp = new PostProcessor(width, height, true, false, true);

        // BLOOM
        ppb.bloom = new Bloom((int) (width * bloomFboScale), (int) (height * bloomFboScale));
        ppb.bloom.setBloomIntesity(GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY);
        ppb.bloom.setThreshold(0f);
        ppb.bloom.setEnabled(GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY > 0);
        ppb.pp.addEffect(ppb.bloom);

        // LIGHT GLOW
        int nsamples;
        int lgw, lgh;
        Texture glow;
        if (GlobalConf.scene.isHighQuality()) {
            nsamples = 30;
            lgw = 1920;
            lgh = Math.round(lgw / ar);
            glow = new Texture(Gdx.files.internal("img/star_glow.png"));
        } else if (GlobalConf.scene.isNormalQuality()) {
            nsamples = 20;
            lgw = 1920;
            lgh = Math.round(lgw / ar);
            glow = new Texture(Gdx.files.internal("img/star_glow_s.png"));
        } else {
            nsamples = 15;
            lgw = 1280;
            lgh = Math.round(lgw / ar);
            glow = new Texture(Gdx.files.internal("img/star_glow_s.png"));
        }

        Glow.N = 30;
        ppb.lglow = new LightGlow(lgw, lgh);
        ppb.lglow.setScatteringIntesity(1f);
        ppb.lglow.setScatteringSaturation(1f);
        ppb.lglow.setBaseIntesity(1f);
        ppb.lglow.setBias(-0.95f);
        ppb.lglow.setLightGlowTexture(glow);
        ppb.lglow.setNSamples(nsamples);
        ppb.lglow.setTextureScale(1f / GaiaSky.instance.cam.getFovFactor());
        ppb.lglow.setEnabled(GlobalConf.postprocess.POSTPROCESS_LIGHT_SCATTERING);
        ppb.pp.addEffect(ppb.lglow);

        // LENS FLARE
        float lensFboScale;
        if (GlobalConf.scene.isHighQuality()) {
            nghosts = 12;
            lensFboScale = 0.5f;
        } else if (GlobalConf.scene.isNormalQuality()) {
            nghosts = 10;
            lensFboScale = 0.3f;
        } else {
            nghosts = 6;
            lensFboScale = 0.2f;
        }
        ppb.lens = new LensFlare2((int) (width * lensFboScale), (int) (height * lensFboScale));
        ppb.lens.setGhosts(nghosts);
        ppb.lens.setHaloWidth(0.4f);
        ppb.lens.setLensColorTexture(new Texture(Gdx.files.internal("img/lenscolor.png")));
        ppb.lens.setLensDirtTexture(new Texture(Gdx.files.internal(GlobalConf.scene.isHighQuality() ? "img/lensdirt.jpg" : "img/lensdirt_s.jpg")));
        ppb.lens.setLensStarburstTexture(new Texture(Gdx.files.internal("img/lensstarburst.jpg")));
        ppb.lens.setFlareIntesity(GlobalConf.postprocess.POSTPROCESS_LENS_FLARE ? flareIntensity : 0f);
        ppb.lens.setFlareSaturation(0.5f);
        ppb.lens.setBaseIntesity(1f);
        ppb.lens.setBias(-0.95f);
        ppb.lens.setBlurAmount(1f);
        ppb.lens.setBlurPasses(10);
        ppb.lens.setEnabled(true);
        ppb.pp.addEffect(ppb.lens);

        // DISTORTION (STEREOSCOPIC MODE)
        ppb.curvature = new Curvature();
        ppb.curvature.setDistortion(0.8f);
        ppb.curvature.setZoom(0.8f);
        ppb.curvature.setEnabled(GlobalConf.program.STEREOSCOPIC_MODE && GlobalConf.program.STEREO_PROFILE == StereoProfile.VR_HEADSET);
        ppb.pp.addEffect(ppb.curvature);

        // FISHEYE DISTORTION (DOME)
        ppb.fisheye = new Fisheye();
        ppb.fisheye.setEnabled(GlobalConf.postprocess.POSTPROCESS_FISHEYE);
        ppb.pp.addEffect(ppb.fisheye);

        // ANTIALIAS
        initAntiAliasing(GlobalConf.postprocess.POSTPROCESS_ANTIALIAS, width, height, ppb);

        // LEVELS - BRIGHTNESS & CONTRAST
        initLevels(ppb);

        // MOTION BLUR
        initMotionBlur(width, height, ppb);

        return ppb;
    }

    private void initLevels(PostProcessBean ppb) {
        ppb.levels = new Levels();
        ppb.levels.setBrightness(GlobalConf.postprocess.POSTPROCESS_BRIGHTNESS);
        ppb.levels.setContrast(GlobalConf.postprocess.POSTPROCESS_CONTRAST);
        ppb.pp.addEffect(ppb.levels);
    }

    private void initMotionBlur(int width, int height, PostProcessBean ppb) {
        ppb.motionblur = new MotionBlur(width, height);
        ppb.motionblur.setBlurRadius(0.7f);
        ppb.motionblur.setBlurOpacity(GlobalConf.postprocess.POSTPROCESS_MOTION_BLUR);
        ppb.motionblur.setEnabled(GlobalConf.postprocess.POSTPROCESS_MOTION_BLUR > 0);
        ppb.pp.addEffect(ppb.motionblur);
    }

    private void initAntiAliasing(int aavalue, int width, int height, PostProcessBean ppb) {
        if (aavalue == -1) {
            ppb.antialiasing = new Fxaa(width, height);
            ((Fxaa) ppb.antialiasing).setSpanMax(8f);
            ((Fxaa) ppb.antialiasing).setReduceMin(1f / 16f);
            ((Fxaa) ppb.antialiasing).setReduceMul(1f / 8f);
            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.selected", "FXAA"));
        } else if (aavalue == -2) {
            ppb.antialiasing = new Nfaa(width, height);
            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.selected", "NFAA"));
        }
        if (ppb.antialiasing != null) {
            ppb.antialiasing.setEnabled(GlobalConf.postprocess.POSTPROCESS_ANTIALIAS < 0);
            ppb.pp.addEffect(ppb.antialiasing);
        }
    }

    @Override
    public PostProcessBean getPostProcessBean(RenderType type) {
        return pps[type.index];
    }

    @Override
    public void resize(final int width, final int height) {
        Gdx.app.postRunnable(new Runnable() {
            @Override
            public void run() {
                replace(RenderType.screen.index, width, height);
            }
        });
    }

    @Override
    public void dispose() {
        for (int i = 0; i < RenderType.values().length; i++) {
            if (pps[i] != null) {
                PostProcessBean ppb = pps[i];
                ppb.dispose();
            }
        }
    }

    @Override
    public void notify(Events event, final Object... data) {
        switch (event) {
        case PROPERTIES_WRITTEN:
            if (pps != null)
                if (changed(pps[RenderType.screenshot.index].pp, GlobalConf.screenshot.SCREENSHOT_WIDTH, GlobalConf.screenshot.SCREENSHOT_HEIGHT)) {
                    Gdx.app.postRunnable(new Runnable() {
                        @Override
                        public void run() {
                            replace(RenderType.screenshot.index, GlobalConf.screenshot.SCREENSHOT_WIDTH, GlobalConf.screenshot.SCREENSHOT_HEIGHT);
                        }
                    });
                }
            if (pps != null)
                if (changed(pps[RenderType.frame.index].pp, GlobalConf.frame.RENDER_WIDTH, GlobalConf.frame.RENDER_HEIGHT)) {
                    Gdx.app.postRunnable(new Runnable() {

                        @Override
                        public void run() {
                            replace(RenderType.frame.index, GlobalConf.frame.RENDER_WIDTH, GlobalConf.frame.RENDER_HEIGHT);
                        }
                    });
                }
            break;
        case BLOOM_CMD:
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    float intensity = (float) data[0];
                    for (int i = 0; i < RenderType.values().length; i++) {
                        if (pps[i] != null) {
                            PostProcessBean ppb = pps[i];
                            ppb.bloom.setBloomIntesity(intensity);
                            ppb.bloom.setEnabled(intensity > 0);
                        }
                    }
                }
            });
            break;
        case LENS_FLARE_CMD:
            boolean active = (Boolean) data[0];
            int nnghosts = active ? nghosts : 0;
            float intensity = active ? flareIntensity : 0;
            for (int i = 0; i < RenderType.values().length; i++) {
                if (pps[i] != null) {
                    PostProcessBean ppb = pps[i];
                    ppb.lens.setGhosts(nnghosts);
                    ppb.lens.setFlareIntesity(intensity);
                    //ppb.lens.setEnabled(active);
                }
            }
            break;
        case LIGHT_SCATTERING_CMD:
            active = (Boolean) data[0];
            for (int i = 0; i < RenderType.values().length; i++) {
                if (pps[i] != null) {
                    PostProcessBean ppb = pps[i];
                    ppb.lglow.setEnabled(active);
                }
            }
            break;
        case FISHEYE_CMD:
            active = (Boolean) data[0];
            for (int i = 0; i < RenderType.values().length; i++) {
                if (pps[i] != null) {
                    PostProcessBean ppb = pps[i];
                    ppb.fisheye.setEnabled(active);
                }
            }
            break;
        case CAMERA_MOTION_UPDATED:
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    Vector3d campos = (Vector3d) data[0];
                    PerspectiveCamera cam = (PerspectiveCamera) data[3];

                    boolean cameraChanged = !Arrays.equals(cam.combined.val, prevCombined.val) || !campos.equals(prevCampos);

                    for (int i = 0; i < RenderType.values().length; i++) {
                        if (pps[i] != null) {
                            PostProcessBean ppb = pps[i];

                            // Motion blur
                            ppb.motionblur.setEnabled(GlobalConf.postprocess.POSTPROCESS_MOTION_BLUR != 0 && cameraChanged);
                        }
                    }
                    prevCombined.set(cam.combined);
                    prevCampos.set(campos);
                }
            });
            break;
        case MOTION_BLUR_CMD:
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    float opacity = (float) data[0];
                    for (int i = 0; i < RenderType.values().length; i++) {
                        if (pps[i] != null) {
                            PostProcessBean ppb = pps[i];
                            ppb.motionblur.setBlurOpacity(opacity);
                            ppb.motionblur.setEnabled(opacity > 0);
                        }
                    }
                }
            });
            break;

        case LIGHT_POS_2D_UPDATED:
            Integer nLights = (Integer) data[0];
            float[] lightpos = (float[]) data[1];
            float[] angles = (float[]) data[2];
            float[] colors = (float[]) data[3];

            for (int i = 0; i < RenderType.values().length; i++) {
                if (pps[i] != null) {
                    PostProcessBean ppb = pps[i];
                    ppb.lglow.setLightPositions(nLights, lightpos);
                    ppb.lglow.setLightViewAngles(angles);
                    ppb.lglow.setLightColors(colors);
                    ppb.lglow.setTextureScale(1f / GaiaSky.instance.cam.getFovFactor());
                }
            }
            break;
        case TOGGLE_STEREOSCOPIC_CMD:
        case TOGGLE_STEREO_PROFILE_CMD:
            boolean curvatureEnabled = GlobalConf.program.STEREOSCOPIC_MODE && GlobalConf.program.STEREO_PROFILE == StereoProfile.VR_HEADSET;
            for (int i = 0; i < RenderType.values().length; i++) {
                if (pps[i] != null) {
                    PostProcessBean ppb = pps[i];
                    ppb.curvature.setEnabled(curvatureEnabled);
                }
            }
            break;
        case ANTIALIASING_CMD:
            final int aavalue = (Integer) data[0];
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < RenderType.values().length; i++) {
                        if (pps[i] != null) {
                            PostProcessBean ppb = pps[i];
                            if (aavalue < 0) {
                                // clean
                                if (ppb.antialiasing != null) {
                                    ppb.antialiasing.setEnabled(false);
                                    ppb.pp.removeEffect(ppb.antialiasing);
                                    ppb.antialiasing = null;
                                }
                                // update
                                initAntiAliasing(aavalue, Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), ppb);
                                // ensure motion blur and levels go after
                                ppb.pp.removeEffect(ppb.levels);
                                initLevels(ppb);
                                ppb.pp.removeEffect(ppb.motionblur);
                                initMotionBlur(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), ppb);

                            } else {
                                // remove
                                if (ppb.antialiasing != null) {
                                    ppb.antialiasing.setEnabled(false);
                                    ppb.pp.removeEffect(ppb.antialiasing);
                                    ppb.antialiasing = null;
                                }
                            }
                        }
                    }
                }
            });
            break;
        case BRIGHTNESS_CMD:
            float br = (Float) data[0];
            for (int i = 0; i < RenderType.values().length; i++) {
                if (pps[i] != null) {
                    PostProcessBean ppb = pps[i];
                    ppb.levels.setBrightness(br);
                }
            }
            break;
        case CONTRAST_CMD:
            float cn = (Float) data[0];
            for (int i = 0; i < RenderType.values().length; i++) {
                if (pps[i] != null) {
                    PostProcessBean ppb = pps[i];
                    ppb.levels.setContrast(cn);
                }
            }
            break;
        }

    }

    /**
     * Reloads the postprocessor at the given index with the given width and
     * height.
     * 
     * @param index
     * @param width
     * @param height
     */
    private void replace(int index, final int width, final int height) {
        // pps[index].pp.dispose(false);
        pps[index] = newPostProcessor(width, height);

    }

    private boolean changed(PostProcessor postProcess, int width, int height) {
        return postProcess.getCombinedBuffer().width != width || postProcess.getCombinedBuffer().height != height;
    }

    @Override
    public boolean isLightScatterEnabled() {
        return pps[RenderType.screen.index].lglow.isEnabled();
    }

}
