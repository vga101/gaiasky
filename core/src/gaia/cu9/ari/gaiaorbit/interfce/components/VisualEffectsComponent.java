package gaia.cu9.ari.gaiaorbit.interfce.components;

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

    protected Slider starBrightness, starSize, starOpacity, bloomEffect, ambientLight, brightness, contrast, hue, saturation;
    protected OwnLabel starbrightnessl, size, opacity, bloom, ambient, brightnessl, contrastl, huel, saturationl, bloomLabel, motionBlurLabel, brightnessLabel, contrastLabel, hueLabel, saturationLabel;
    protected CheckBox lensFlare, lightScattering, motionBlur;
    private HorizontalGroup bloomGroup, brightnessGroup, contrastGroup, hueGroup, saturationGroup;

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
        starBrightness.addListener(event -> {
            if (event instanceof ChangeEvent && hackProgrammaticChangeEvents) {
                EventManager.instance.post(Events.STAR_BRIGHTNESS_CMD, MathUtilsd.lint(starBrightness.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_STAR_BRIGHT, Constants.MAX_STAR_BRIGHT), true);
                starbrightnessl.setText(Integer.toString((int) starBrightness.getValue()));
                return true;
            }
            return false;
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
        starSize.addListener(event -> {
            if (flag && event instanceof ChangeEvent) {
                EventManager.instance.post(Events.STAR_POINT_SIZE_CMD, MathUtilsd.lint(starSize.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_STAR_POINT_SIZE, Constants.MAX_STAR_POINT_SIZE), true);
                size.setText(Integer.toString((int) starSize.getValue()));
                return true;
            }
            return false;
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
        starOpacity.addListener(event -> {
            if (event instanceof ChangeEvent && hackProgrammaticChangeEvents) {
                EventManager.instance.post(Events.STAR_MIN_OPACITY_CMD, MathUtilsd.lint(starOpacity.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_STAR_MIN_OPACITY, Constants.MAX_STAR_MIN_OPACITY), true);
                opacity.setText(Integer.toString((int) starOpacity.getValue()));
                return true;
            }
            return false;
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
        ambientLight.addListener(event -> {
            if (event instanceof ChangeEvent) {
                EventManager.instance.post(Events.AMBIENT_LIGHT_CMD, ambientLight.getValue() / 100f);
                ambient.setText(Integer.toString((int) ambientLight.getValue()));
                return true;
            }
            return false;
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
            bloomEffect.addListener(event -> {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.BLOOM_CMD, bloomEffect.getValue() / 10f, true);
                    bloom.setText(Integer.toString((int) bloomEffect.getValue()));
                    return true;
                }
                return false;
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
            brightness.addListener(event -> {
                if (event instanceof ChangeEvent && hackProgrammaticChangeEvents) {
                    EventManager.instance.post(Events.BRIGHTNESS_CMD, MathUtilsd.lint(brightness.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_BRIGHTNESS, Constants.MAX_BRIGHTNESS), true);
                    brightnessLabel.setText(Integer.toString((int) brightness.getValue()));
                    return true;
                }
                return false;
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
            contrast.addListener(event -> {
                if (event instanceof ChangeEvent && hackProgrammaticChangeEvents) {
                    EventManager.instance.post(Events.CONTRAST_CMD, MathUtilsd.lint(contrast.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_CONTRAST, Constants.MAX_CONTRAST), true);
                    contrastLabel.setText(Integer.toString((int) contrast.getValue()));
                    return true;
                }
                return false;
            });

            contrastGroup = new HorizontalGroup();
            contrastGroup.space(space3);
            contrastGroup.addActor(contrast);
            contrastGroup.addActor(contrastLabel);

            /** Hue **/
            huel = new OwnLabel(txt("gui.hue"), skin, "default");
            hueLabel = new OwnLabel(Integer.toString((int) MathUtilsd.lint(GlobalConf.postprocess.POSTPROCESS_HUE, Constants.MIN_HUE, Constants.MAX_HUE, Constants.MIN_SLIDER, Constants.MAX_SLIDER)), skin);
            hue = new OwnSlider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
            hue.setName("hue");
            hue.setWidth(sliderWidth);
            hue.setValue(MathUtilsd.lint(GlobalConf.postprocess.POSTPROCESS_HUE, Constants.MIN_HUE, Constants.MAX_HUE, Constants.MIN_SLIDER, Constants.MAX_SLIDER));
            hue.addListener(event -> {
                if (event instanceof ChangeEvent && hackProgrammaticChangeEvents) {
                    EventManager.instance.post(Events.HUE_CMD, MathUtilsd.lint(hue.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_HUE, Constants.MAX_HUE), true);
                    hueLabel.setText(Integer.toString((int) hue.getValue()));
                    return true;
                }
                return false;
            });

            hueGroup = new HorizontalGroup();
            hueGroup.space(space3);
            hueGroup.addActor(hue);
            hueGroup.addActor(hueLabel);

            /** Saturation **/
            saturationl = new OwnLabel(txt("gui.saturation"), skin, "default");
            saturationLabel = new OwnLabel(Integer.toString((int) MathUtilsd.lint(GlobalConf.postprocess.POSTPROCESS_SATURATION, Constants.MIN_SATURATION, Constants.MAX_SATURATION, Constants.MIN_SLIDER, Constants.MAX_SLIDER)), skin);
            saturation = new OwnSlider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
            saturation.setName("saturation");
            saturation.setWidth(sliderWidth);
            saturation.setValue(MathUtilsd.lint(GlobalConf.postprocess.POSTPROCESS_SATURATION, Constants.MIN_SATURATION, Constants.MAX_SATURATION, Constants.MIN_SLIDER, Constants.MAX_SLIDER));
            saturation.addListener(event -> {
                if (event instanceof ChangeEvent && hackProgrammaticChangeEvents) {
                    EventManager.instance.post(Events.SATURATION_CMD, MathUtilsd.lint(saturation.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_SATURATION, Constants.MAX_SATURATION), true);
                    saturationLabel.setText(Integer.toString((int) saturation.getValue()));
                    return true;
                }
                return false;
            });

            saturationGroup = new HorizontalGroup();
            saturationGroup.space(space3);
            saturationGroup.addActor(saturation);
            saturationGroup.addActor(saturationLabel);

            /** Motion blur **/
            motionBlur = new CheckBox(" " + txt("gui.motionblur"), skin);
            motionBlur.setName("motion blur");
            motionBlur.setChecked(GlobalConf.postprocess.POSTPROCESS_MOTION_BLUR != 0);
            motionBlur.addListener(event -> {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.MOTION_BLUR_CMD, motionBlur.isChecked() ? Constants.MOTION_BLUR_VALUE : 0.0f, true);
                    return true;
                }
                return false;
            });

            /** Lens flare **/
            lensFlare = new CheckBox(" " + txt("gui.lensflare"), skin);
            lensFlare.setName("lens flare");
            lensFlare.addListener(event -> {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.LENS_FLARE_CMD, lensFlare.isChecked(), true);
                    return true;
                }
                return false;
            });
            lensFlare.setChecked(GlobalConf.postprocess.POSTPROCESS_LENS_FLARE);

            /** Light scattering **/
            lightScattering = new CheckBox(" " + txt("gui.lightscattering"), skin);
            lightScattering.setName("light scattering");
            lightScattering.setChecked(GlobalConf.postprocess.POSTPROCESS_LIGHT_SCATTERING);
            lightScattering.addListener(event -> {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.LIGHT_SCATTERING_CMD, lightScattering.isChecked(), true);
                    return true;
                }
                return false;
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
            lightingGroup.addActor(huel);
            lightingGroup.addActor(hueGroup);
            lightingGroup.addActor(new Separator(skin));
            lightingGroup.addActor(saturationl);
            lightingGroup.addActor(saturationGroup);
            lightingGroup.addActor(new Separator(skin));
            lightingGroup.addActor(motionBlur);
            lightingGroup.addActor(lensFlare);
            lightingGroup.addActor(lightScattering);
        }

        component = lightingGroup;

        EventManager.instance.subscribe(this, Events.STAR_POINT_SIZE_CMD, Events.STAR_BRIGHTNESS_CMD, Events.BRIGHTNESS_CMD, Events.CONTRAST_CMD, Events.HUE_CMD, Events.SATURATION_CMD, Events.MOTION_BLUR_CMD, Events.LENS_FLARE_CMD, Events.LIGHT_SCATTERING_CMD, Events.BLOOM_CMD, Events.STAR_MIN_OPACITY_CMD);
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case STAR_POINT_SIZE_CMD:
            if (!(boolean) data[1]) {
                flag = false;
                float newsize = MathUtilsd.lint((float) data[0], Constants.MIN_STAR_POINT_SIZE, Constants.MAX_STAR_POINT_SIZE, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
                starSize.setValue(newsize);
                size.setText(Integer.toString((int) starSize.getValue()));
                flag = true;
            }
            break;
        case STAR_BRIGHTNESS_CMD:
            if (!(boolean) data[1]) {
                Float brightness = (Float) data[0];
                float sliderValue = MathUtilsd.lint(brightness, Constants.MIN_STAR_BRIGHT, Constants.MAX_STAR_BRIGHT, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
                hackProgrammaticChangeEvents = false;
                starBrightness.setValue(sliderValue);
                starbrightnessl.setText(Integer.toString((int) sliderValue));
                hackProgrammaticChangeEvents = true;
            }
            break;
        case STAR_MIN_OPACITY_CMD:
            if (!(boolean) data[1]) {
                Float minopacity = (Float) data[0];
                float sliderValue = MathUtilsd.lint(minopacity, Constants.MIN_STAR_MIN_OPACITY, Constants.MAX_STAR_MIN_OPACITY, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
                hackProgrammaticChangeEvents = false;
                starOpacity.setValue(sliderValue);
                opacity.setText(Integer.toString((int) sliderValue));
                hackProgrammaticChangeEvents = true;
            }
            break;
        case BRIGHTNESS_CMD:
            if (!(boolean) data[1]) {
                // Update UI element
                float level = (Float) data[0];
                float sliderLevel = MathUtilsd.lint(level, Constants.MIN_BRIGHTNESS, Constants.MAX_BRIGHTNESS, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
                hackProgrammaticChangeEvents = false;
                brightness.setValue(sliderLevel);
                brightnessLabel.setText(Integer.toString((int) sliderLevel));
                hackProgrammaticChangeEvents = true;
            }
            break;
        case CONTRAST_CMD:
            if (!(boolean) data[1]) {
                // Update UI element
                float level = (Float) data[0];
                float sliderLevel = MathUtilsd.lint(level, Constants.MIN_CONTRAST, Constants.MAX_CONTRAST, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
                hackProgrammaticChangeEvents = false;
                contrast.setValue(sliderLevel);
                contrastLabel.setText(Integer.toString((int) sliderLevel));
                hackProgrammaticChangeEvents = true;
            }
            break;
        case HUE_CMD:
            if (!(boolean) data[1]) {
                // Update UI element
                float level = (Float) data[0];
                float sliderLevel = MathUtilsd.lint(level, Constants.MIN_HUE, Constants.MAX_HUE, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
                hackProgrammaticChangeEvents = false;
                hue.setValue(sliderLevel);
                hueLabel.setText(Integer.toString((int) sliderLevel));
                hackProgrammaticChangeEvents = true;
            }
            break;
        case SATURATION_CMD:
            if (!(boolean) data[1]) {
                // Update UI element
                float level = (Float) data[0];
                float sliderLevel = MathUtilsd.lint(level, Constants.MIN_SATURATION, Constants.MAX_SATURATION, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
                hackProgrammaticChangeEvents = false;
                saturation.setValue(sliderLevel);
                saturationLabel.setText(Integer.toString((int) sliderLevel));
                hackProgrammaticChangeEvents = true;
            }
            break;
        case BLOOM_CMD:
            if (!(boolean) data[1]) {
                // Update UI element
                float level = (float) data[0];
                hackProgrammaticChangeEvents = false;
                bloomEffect.setValue(level);
                bloom.setText(Integer.toString((int) level));
                hackProgrammaticChangeEvents = true;
            }
            break;
        case MOTION_BLUR_CMD:
            if (!(boolean) data[1]) {
                float level = (Float) data[0];
                motionBlur.setProgrammaticChangeEvents(false);
                motionBlur.setChecked(level != 0);
                motionBlur.setProgrammaticChangeEvents(true);
            }
            break;

        case LIGHT_SCATTERING_CMD:
            if (!(boolean) data[1]) {
                lightScattering.setProgrammaticChangeEvents(false);
                lightScattering.setChecked((boolean) data[0]);
                lightScattering.setProgrammaticChangeEvents(true);
            }
            break;
        case LENS_FLARE_CMD:
            if (!(boolean) data[1]) {
                lensFlare.setProgrammaticChangeEvents(false);
                lensFlare.setChecked((boolean) data[0]);
                lensFlare.setProgrammaticChangeEvents(true);
            }
            break;
        default:
            break;
        }

    }

}
