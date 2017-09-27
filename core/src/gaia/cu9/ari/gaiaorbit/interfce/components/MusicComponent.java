package gaia.cu9.ari.gaiaorbit.interfce.components;

import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.MusicManager;
import gaia.cu9.ari.gaiaorbit.util.format.INumberFormat;
import gaia.cu9.ari.gaiaorbit.util.format.NumberFormatFactory;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnImageButton;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnSlider;

public class MusicComponent extends GuiComponent implements IObserver {

    protected ImageButton prev, play, next;
    protected Slider volume;
    protected Label vol;
    protected INumberFormat nf;

    public MusicComponent(Skin skin, Stage stage) {
        super(skin, stage);
        EventManager.instance.subscribe(this, Events.MUSIC_PLAYPAUSE_CMD, Events.MUSIC_VOLUME_CMD);
    }

    @Override
    public void initialize() {
        float componentWidth = 140 * GlobalConf.SCALE_FACTOR;
        nf = NumberFormatFactory.getFormatter("000");
        prev = new OwnImageButton(skin, "audio-bwd");
        prev.addListener(event -> {
            if (event instanceof ChangeEvent) {
                EventManager.instance.post(Events.MUSIC_PREVIOUS_CMD);
                return true;
            }
            return false;
        });
        prev.addListener(new TextTooltip(txt("gui.music.previous"), skin));

        play = new OwnImageButton(skin, "audio-playpause");
        play.setChecked(false);
        play.addListener(event -> {
            if (event instanceof ChangeEvent) {
                EventManager.instance.post(Events.MUSIC_PLAYPAUSE_CMD);
                return true;
            }
            return false;
        });
        play.addListener(new TextTooltip(txt("gui.music.playpause"), skin));

        next = new OwnImageButton(skin, "audio-fwd");
        next.addListener(event -> {
            if (event instanceof ChangeEvent) {
                EventManager.instance.post(Events.MUSIC_NEXT_CMD);
                return true;
            }
            return false;
        });
        next.addListener(new TextTooltip(txt("gui.music.next"), skin));

        volume = new OwnSlider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
        volume.setName("volume");
        volume.setWidth(componentWidth);
        volume.setValue(MusicManager.instance.getVolume() * Constants.MAX_SLIDER);
        volume.addListener(event -> {
            if (event instanceof ChangeEvent) {
                float value = volume.getValue();
                EventManager.instance.post(Events.MUSIC_VOLUME_CMD, value / Constants.MAX_SLIDER);
                vol.setText(nf.format(value));
                return true;
            }
            return false;
        });
        vol = new OwnLabel(nf.format(volume.getValue()), skin, "default");

        float space3 = 3 * GlobalConf.SCALE_FACTOR;
        VerticalGroup musicGroup = new VerticalGroup().align(Align.center).columnAlign(Align.left).space(space3);

        HorizontalGroup playGroup = new HorizontalGroup();
        playGroup.setWidth(componentWidth);
        playGroup.space(45 * GlobalConf.SCALE_FACTOR);
        prev.align(Align.center);
        play.align(Align.center);
        next.align(Align.center);
        playGroup.addActor(prev);
        playGroup.addActor(play);
        playGroup.addActor(next);

        HorizontalGroup volGroup = new HorizontalGroup();
        volGroup.space(space3);
        volGroup.addActor(volume);
        volGroup.addActor(vol);

        musicGroup.addActor(playGroup);
        musicGroup.addActor(volGroup);

        component = musicGroup;

    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        default:
            break;
        }

    }
}
