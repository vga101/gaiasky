package gaia.cu9.ari.gaiaorbit.interfce.components;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnSlider;
import gaia.cu9.ari.gaiaorbit.util.scene2d.Separator;

public class VisualEffectsComponent extends GuiComponent implements IObserver {

    protected Slider starBrightness, starSize, starOpacity, bloomEffect, ambientLight, brightness, contrast;
    protected OwnLabel starbrightnessl, size, opacity, bloom, ambient, brightnessl, contrastl, bloomLabel, motionBlurLabel, brightnessLabel, contrastLabel;
    protected CheckBox lensFlare, lightScattering, motionBlur;
    private HorizontalGroup bloomGroup, brightnessGroup, contrastGroup;

    boolean flag = true;

    boolean hackProgrammaticChangeEvents = true;

    public VisualEffectsComponent(Skin skin, Stage stage) {
        super(skin, stage);
    }

    public void initialize() {
        float space3 = 3 * GlobalConf.SCALE_FACTOR;
        float space2 = 2 * GlobalConf.SCALE_FACTOR;
        float sliderWidth = 140 * GlobalConf.SCALE_FACTOR;
        /** Star brightness **/
        Label sbrightnessLabel = new Label(txt("gui.starbrightness"), skin, "default");
        starbrightnessl = new OwnLabel(Integer.toString((int) (MathUtilsd.lint(GlobalConf.scene.STAR_BRIGHTNESS, Constants.MIN_STAR_BRIGHT, Constants.MAX_STAR_BRIGHT, Constants.MIN_SLIDER, Constants.MAX_SLIDER))), skin);
        starBrightness = new OwnSlider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
        starBrightness.setName("star brightness");
        starBrightness.setWidth(sliderWidth);
        starBrightness.setValue((float) MathUtilsd.lint(GlobalConf.scene.STAR_BRIGHTNESS, Constants.MIN_STAR_BRIGHT, Constants.MAX_STAR_BRIGHT, Constants.MIN_SLIDER, Constants.MAX_SLIDER));
        starBrightness.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent && hackProgrammaticChangeEvents) {
                    EventManager.instance.post(Events.STAR_BRIGHTNESS_CMD, MathUtilsd.lint(starBrightness.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_STAR_BRIGHT, Constants.MAX_STAR_BRIGHT), true);
                    starbrightnessl.setText(Integer.toString((int) starBrightness.getValue()));
                    return true;
                }
                return false;
            }
        });
        HorizontalGroup sbrightnessGroup = new HorizontalGroup();
        sbrightnessGroup.space(space3);
        sbrightnessGroup.addActor(starBrightness);
        sbrightnessGroup.addActor(starbrightnessl);

        /** Star size **/
        Label sizeLabel = new Label(txt("gui.star.size"), skin, "default");
        size = new OwnLabel(Integer.toString((int) (MathUtilsd.lint(GlobalConf.scene.STAR_POINT_SIZE, Constants.MIN_STAR_POINT_SIZE, Constants.MAX_STAR_POINT_SIZE, Constants.MIN_SLIDER, Constants.MAX_SLIDER))), skin);
        starSize = new OwnSlider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
        starSize.setName("star size");
        starSize.setWidth(sliderWidth);
        starSize.setValue(MathUtilsd.lint(GlobalConf.scene.STAR_POINT_SIZE, Constants.MIN_STAR_POINT_SIZE, Constants.MAX_STAR_POINT_SIZE, Constants.MIN_SLIDER, Constants.MAX_SLIDER));
        starSize.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (flag && event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.STAR_POINT_SIZE_CMD, MathUtilsd.lint(starSize.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_STAR_POINT_SIZE, Constants.MAX_STAR_POINT_SIZE));
                    size.setText(Integer.toString((int) starSize.getValue()));
                    return true;
                }
                return false;
            }
        });
        HorizontalGroup sizeGroup = new HorizontalGroup();
        sizeGroup.space(space3);
        sizeGroup.addActor(starSize);
        sizeGroup.addActor(size);

        /** Star opacity **/
        Label opacityLabel = new Label(txt("gui.star.opacity"), skin, "default");
        opacity = new OwnLabel(Integer.toString((int) (MathUtilsd.lint(GlobalConf.scene.POINT_ALPHA_MIN, Constants.MIN_STAR_MIN_OPACITY, Constants.MAX_STAR_MIN_OPACITY, Constants.MIN_SLIDER, Constants.MAX_SLIDER))), skin);
        starOpacity = new OwnSlider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
        starOpacity.setName("star opacity");
        starOpacity.setWidth(sliderWidth);
        starOpacity.setValue(MathUtilsd.lint(GlobalConf.scene.POINT_ALPHA_MIN, Constants.MIN_STAR_MIN_OPACITY, Constants.MAX_STAR_MIN_OPACITY, Constants.MIN_SLIDER, Constants.MAX_SLIDER));
        starOpacity.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.STAR_MIN_OPACITY_CMD, MathUtilsd.lint(starOpacity.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_STAR_MIN_OPACITY, Constants.MAX_STAR_MIN_OPACITY));
                    opacity.setText(Integer.toString((int) starOpacity.getValue()));
                    return true;
                }
                return false;
            }
        });
        HorizontalGroup opacityGroup = new HorizontalGroup();
        opacityGroup.space(space3);
        opacityGroup.addActor(starOpacity);
        opacityGroup.addActor(opacity);

        /** Ambient light **/
        Label ambientLightLabel = new Label(txt("gui.light.ambient"), skin, "default");
        ambient = new OwnLabel(Integer.toString((int) (GlobalConf.scene.AMBIENT_LIGHT * 100)), skin);
        ambientLight = new OwnSlider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
        ambientLight.setName("ambient light");
        ambientLight.setWidth(sliderWidth);
        ambientLight.setValue((float) GlobalConf.scene.AMBIENT_LIGHT * 100);
        ambientLight.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.AMBIENT_LIGHT_CMD, ambientLight.getValue() / 100f);
                    ambient.setText(Integer.toString((int) ambientLight.getValue()));
                    return true;
                }
                return false;
            }
        });
        HorizontalGroup ambientGroup = new HorizontalGroup();
        ambientGroup.space(space3);
        ambientGroup.addActor(ambientLight);
        ambientGroup.addActor(ambient);

        if (Constants.desktop) {
            /** Bloom **/
            bloomLabel = new OwnLabel(txt("gui.bloom"), skin, "default");
            bloom = new OwnLabel(Integer.toString((int) (GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY * 10)), skin);
            bloomEffect = new OwnSlider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
            bloomEffect.setName("bloom effect");
            bloomEffect.setWidth(sliderWidth);
            bloomEffect.setValue(GlobalConf.postprocess.POSTPROCESS_BLOOM_INTENSITY * 10f);
            bloomEffect.addListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    if (event instanceof ChangeEvent) {
                        EventManager.instance.post(Events.BLOOM_CMD, bloomEffect.getValue() / 10f);
                        bloom.setText(Integer.toString((int) bloomEffect.getValue()));
                        return true;
                    }
                    return false;
                }
            });

            bloomGroup = new HorizontalGroup();
            bloomGroup.space(space3);
            bloomGroup.addActor(bloomEffect);
            bloomGroup.addActor(bloom);

            /** Brightness **/
            brightnessl = new OwnLabel(txt("gui.brightness"), skin, "default");
            brightnessLabel = new OwnLabel(Integer.toString((int) MathUtilsd.lint(GlobalConf.postprocess.POSTPROCESS_BRIGHTNESS, Constants.MIN_BRIGHTNESS, Constants.MAX_BRIGHTNESS, Constants.MIN_SLIDER, Constants.MAX_SLIDER)), skin);
            brightness = new OwnSlider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
            brightness.setName("brightness");
            brightness.setWidth(sliderWidth);
            brightness.setValue(MathUtilsd.lint(GlobalConf.postprocess.POSTPROCESS_BRIGHTNESS, Constants.MIN_BRIGHTNESS, Constants.MAX_BRIGHTNESS, Constants.MIN_SLIDER, Constants.MAX_SLIDER));
            brightness.addListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    if (event instanceof ChangeEvent) {
                        EventManager.instance.post(Events.BRIGHTNESS_CMD, MathUtilsd.lint(brightness.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_BRIGHTNESS, Constants.MAX_BRIGHTNESS));
                        brightnessLabel.setText(Integer.toString((int) brightness.getValue()));
                        return true;
                    }
                    return false;
                }
            });

            brightnessGroup = new HorizontalGroup();
            brightnessGroup.space(space3);
            brightnessGroup.addActor(brightness);
            brightnessGroup.addActor(brightnessLabel);

            /** Contrast **/
            contrastl = new OwnLabel(txt("gui.contrast"), skin, "default");
            contrastLabel = new OwnLabel(Integer.toString((int) MathUtilsd.lint(GlobalConf.postprocess.POSTPROCESS_CONTRAST, Constants.MIN_CONTRAST, Constants.MAX_CONTRAST, Constants.MIN_SLIDER, Constants.MAX_SLIDER)), skin);
            contrast = new OwnSlider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
            contrast.setName("contrast");
            contrast.setWidth(sliderWidth);
            contrast.setValue(MathUtilsd.lint(GlobalConf.postprocess.POSTPROCESS_CONTRAST, Constants.MIN_CONTRAST, Constants.MAX_CONTRAST, Constants.MIN_SLIDER, Constants.MAX_SLIDER));
            contrast.addListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    if (event instanceof ChangeEvent) {
                        EventManager.instance.post(Events.CONTRAST_CMD, MathUtilsd.lint(contrast.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_CONTRAST, Constants.MAX_CONTRAST));
                        contrastLabel.setText(Integer.toString((int) contrast.getValue()));
                        return true;
                    }
                    return false;
                }
            });

            contrastGroup = new HorizontalGroup();
            contrastGroup.space(space3);
            contrastGroup.addActor(contrast);
            contrastGroup.addActor(contrastLabel);

            /** Motion blur **/
            motionBlur = new CheckBox(" " + txt("gui.motionblur"), skin);
            motionBlur.setName("motion blur");
            motionBlur.setChecked(GlobalConf.postprocess.POSTPROCESS_MOTION_BLUR != 0);
            motionBlur.addListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    if (event instanceof ChangeEvent) {
                        EventManager.instance.post(Events.MOTION_BLUR_CMD, motionBlur.isChecked() ? Constants.MOTION_BLUR_VALUE : 0.0f);
                        return true;
                    }
                    return false;
                }
            });

            /** Lens flare **/
            lensFlare = new CheckBox(" " + txt("gui.lensflare"), skin);
            lensFlare.setName("lens flare");
            lensFlare.addListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    if (event instanceof ChangeEvent) {
                        EventManager.instance.post(Events.LENS_FLARE_CMD, lensFlare.isChecked());
                        return true;
                    }
                    return false;
                }
            });
            lensFlare.setChecked(GlobalConf.postprocess.POSTPROCESS_LENS_FLARE);

            /** Light scattering **/
            lightScattering = new CheckBox(" " + txt("gui.lightscattering"), skin);
            lightScattering.setName("light scattering");
            lightScattering.setChecked(GlobalConf.postprocess.POSTPROCESS_LIGHT_SCATTERING);
            lightScattering.addListener(new EventListener() {
                @Override
                public boolean handle(Event event) {
                    if (event instanceof ChangeEvent) {
                        EventManager.instance.post(Events.LIGHT_SCATTERING_CMD, lightScattering.isChecked());
                        return true;
                    }
                    return false;
                }
            });

        }

        VerticalGroup lightingGroup = new VerticalGroup().align(Align.left).columnAlign(Align.left);
        lightingGroup.space(space2);
        lightingGroup.addActor(sbrightnessLabel);
        lightingGroup.addActor(sbrightnessGroup);
        lightingGroup.addActor(new Separator(skin));
        lightingGroup.addActor(sizeLabel);
        lightingGroup.addActor(sizeGroup);
        lightingGroup.addActor(new Separator(skin));
        lightingGroup.addActor(opacityLabel);
        lightingGroup.addActor(opacityGroup);
        lightingGroup.addActor(new Separator(skin));
        lightingGroup.addActor(ambientLightLabel);
        lightingGroup.addActor(ambientGroup);
        lightingGroup.addActor(new Separator(skin));
        if (Constants.desktop) {
            lightingGroup.addActor(bloomLabel);
            lightingGroup.addActor(bloomGroup);
            lightingGroup.addActor(new Separator(skin));
            lightingGroup.addActor(brightnessl);
            lightingGroup.addActor(brightnessGroup);
            lightingGroup.addActor(new Separator(skin));
            lightingGroup.addActor(contrastl);
            lightingGroup.addActor(contrastGroup);
            lightingGroup.addActor(new Separator(skin));
            lightingGroup.addActor(motionBlur);
            lightingGroup.addActor(lensFlare);
            lightingGroup.addActor(lightScattering);
        }

        component = lightingGroup;

        EventManager.instance.subscribe(this, Events.STAR_POINT_SIZE_INFO, Events.STAR_BRIGHTNESS_CMD);
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case STAR_POINT_SIZE_INFO:
            flag = false;
            float newsize = MathUtilsd.lint((float) data[0], Constants.MIN_STAR_POINT_SIZE, Constants.MAX_STAR_POINT_SIZE, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
            starSize.setValue(newsize);
            size.setText(Integer.toString((int) starSize.getValue()));
            flag = true;
            break;
        case STAR_BRIGHTNESS_CMD:
            if (data.length == 1 || data.length > 1 && !(Boolean) data[1]) {
                Float brightness = (Float) data[0];
                float sliderValue = MathUtilsd.lint(brightness, Constants.MIN_STAR_BRIGHT, Constants.MAX_STAR_BRIGHT, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
                hackProgrammaticChangeEvents = false;
                starBrightness.setValue(sliderValue);
                starbrightnessl.setText(Integer.toString((int) sliderValue));
                hackProgrammaticChangeEvents = true;
            }
            break;
        default:
            break;
        }

    }

}
