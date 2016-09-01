package gaia.cu9.ari.gaiaorbit.desktop.render;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.bitfire.postprocessing.PostProcessor;
import com.bitfire.postprocessing.effects.Bloom;
import com.bitfire.postprocessing.effects.Curvature;
import com.bitfire.postprocessing.effects.Fisheye;
import com.bitfire.postprocessing.effects.Fxaa;
import com.bitfire.postprocessing.effects.LensFlare2;
import com.bitfire.postprocessing.effects.LightScattering;
import com.bitfire.postprocessing.effects.MotionBlur;
import com.bitfire.postprocessing.effects.Nfaa;
import com.bitfire.utils.ShaderLoader;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IPostProcessor;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

public class DesktopPostProcessor implements IPostProcessor, IObserver {

    private PostProcessBean[] pps;

    float bloomFboScale = 0.5f;

    float scatteringFboScale = 1.0f;

    // Number of flares
    int nghosts;

    long lastMotionBlurUpdate = 0;
    float lastMotionBlurOpacity = 0;

    public DesktopPostProcessor() {
        ShaderLoader.BasePath = "shaders/";

        pps = new PostProcessBean[RenderType.values().length];

        pps[RenderType.screen.index] = newPostProcessor(getWidth(RenderType.screen), getHeight(RenderType.screen));
        if (Constants.desktop) {
            pps[RenderType.screenshot.index] = newPostProcessor(getWidth(RenderType.screenshot), getHeight(RenderType.screenshot));
            pps[RenderType.frame.index] = newPostProcessor(getWidth(RenderType.frame), getHeight(RenderType.frame));
        }

        // Output AA info.
        if (GlobalConf.postprocess.POSTPROCESS_ANTIALIAS == -1) {
            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.selected", "FXAA"));
        } else if (GlobalConf.postprocess.POSTPROCESS_ANTIALIAS == -2) {
            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.selected", "NFAA"));
        }

        EventManager.instance.subscribe(this, Events.PROPERTIES_WRITTEN, Events.BLOOM_CMD, Events.LENS_FLARE_CMD, Events.MOTION_BLUR_CMD, Events.LIGHT_POS_2D_UPDATED, Events.LIGHT_SCATTERING_CMD, Events.TOGGLE_STEREOSCOPIC_CMD, Events.TOGGLE_STEREO_PROFILE_CMD, Events.FISHEYE_CMD, Events.CAMERA_MOTION_UPDATED);

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

        ppb.pp = new PostProcessor(width, height, true, false, true);

        // MOTION BLUR
        ppb.motionblur = new MotionBlur();
        ppb.pp.addEffect(ppb.motionblur);

        // BLOOM
        ppb.bloom = new Bloom((int) (width * bloomFboScale), (int) (height * bloomFboScale));
        ppb.bloom.setBloomIntesity(GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY);
        ppb.bloom.setThreshold(0f);
        ppb.bloom.setEnabled(GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY > 0);
        ppb.pp.addEffect(ppb.bloom);

        // LIGHT SCATTERING
        int nsamples;
        float density;
        if (GlobalConf.scene.isHighQuality()) {
            nsamples = 200;
            density = 1.5f;
        } else if (GlobalConf.scene.isNormalQuality()) {
            nsamples = 90;
            density = 0.96f;
        } else {
            nsamples = 40;
            density = .9f;
        }
        ppb.lscatter = new LightScattering((int) (width * scatteringFboScale), (int) (height * scatteringFboScale));
        ppb.lscatter.setScatteringIntesity(0.9f);
        ppb.lscatter.setScatteringSaturation(1f);
        ppb.lscatter.setBaseIntesity(1f);
        ppb.lscatter.setBias(-0.95f);
        ppb.lscatter.setBlurAmount(1f);
        ppb.lscatter.setBlurPasses(5);
        ppb.lscatter.setDensity(density);
        ppb.lscatter.setNumSamples(nsamples);
        ppb.lscatter.setEnabled(GlobalConf.postprocess.POSTPROCESS_LIGHT_SCATTERING);
        ppb.pp.addEffect(ppb.lscatter);

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
        ppb.lens.setFlareIntesity(1f);
        ppb.lens.setFlareSaturation(0.5f);
        ppb.lens.setBaseIntesity(1f);
        ppb.lens.setBias(-0.999f);
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
        if (GlobalConf.postprocess.POSTPROCESS_ANTIALIAS == -1) {
            ppb.antialiasing = new Fxaa(width, height);
            ((Fxaa) ppb.antialiasing).setSpanMax(4f);
        } else if (GlobalConf.postprocess.POSTPROCESS_ANTIALIAS == -2) {
            ppb.antialiasing = new Nfaa(width, height);
        }
        if (ppb.antialiasing != null) {
            ppb.antialiasing.setEnabled(GlobalConf.postprocess.POSTPROCESS_ANTIALIAS < 0);
            ppb.pp.addEffect(ppb.antialiasing);
        }

        return ppb;
    }

    @Override
    public PostProcessBean getPostProcessBean(RenderType type) {
        return pps[type.index];
    }

    @Override
    public void resize(final int width, final int height) {
        if (pps[RenderType.screen.index].antialiasing != null) {
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    replace(RenderType.screen.index, width, height);
                }
            });
        }

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
            float flareIntensity = active ? 0.6f : 0;
            for (int i = 0; i < RenderType.values().length; i++) {
                if (pps[i] != null) {
                    PostProcessBean ppb = pps[i];
                    ppb.lens.setGhosts(nnghosts);
                    ppb.lens.setFlareIntesity(flareIntensity);
                }
            }
            break;
        case LIGHT_SCATTERING_CMD:
            active = (Boolean) data[0];
            for (int i = 0; i < RenderType.values().length; i++) {
                if (pps[i] != null) {
                    PostProcessBean ppb = pps[i];
                    ppb.lscatter.setEnabled(active);
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
                    double vel = (double) data[1];
                    float opacity = (float) MathUtilsd.lint(vel, 0, 1e18, 0, GlobalConf.postprocess.POSTPROCESS_MOTION_BLUR);
                    long t = System.currentTimeMillis();
                    boolean enabled = opacity > 0 || (t - lastMotionBlurUpdate) < 5000;
                    for (int i = 0; i < RenderType.values().length; i++) {
                        if (pps[i] != null) {
                            PostProcessBean ppb = pps[i];

                            ppb.motionblur.setEnabled(enabled);

                            if (opacity == 0)
                                ppb.motionblur.setBlurOpacity(lastMotionBlurOpacity);
                            else
                                ppb.motionblur.setBlurOpacity(opacity);

                        }
                    }
                    if (opacity > 0) {
                        lastMotionBlurUpdate = t;
                        lastMotionBlurOpacity = opacity;
                    }
                }
            });
            break;
        case MOTION_BLUR_CMD:
            //            Gdx.app.postRunnable(new Runnable() {
            //                @Override
            //                public void run() {
            //                    float opacity = (float) data[0];
            //                    System.out.println(opacity);
            //                    for (int i = 0; i < RenderType.values().length; i++) {
            //                        if (pps[i] != null) {
            //                            PostProcessBean ppb = pps[i];
            //                            ppb.motionblur.setBlurOpacity(opacity);
            //                            ppb.motionblur.setEnabled(opacity > 0);
            //                        }
            //                    }
            //                }
            //            });
            break;

        case LIGHT_POS_2D_UPDATED:
            Integer nLights = (Integer) data[0];
            float[] lightpos = (float[]) data[1];

            for (int i = 0; i < RenderType.values().length; i++) {
                if (pps[i] != null) {
                    PostProcessBean ppb = pps[i];
                    ppb.lscatter.setLightPositions(nLights, lightpos);
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
        return pps[RenderType.screen.index].lscatter.isEnabled();
    }

}
