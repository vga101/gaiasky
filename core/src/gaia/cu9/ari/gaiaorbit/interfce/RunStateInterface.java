package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.script.ScriptingFactory;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextButton;

public class RunStateInterface extends Table implements IObserver, IGuiInterface {

    private Image keyboardImg, cameraImg;
    private TextButton cancelScript, cancelCamera, bgLoading;
    private boolean loadingPaused = false;

    public RunStateInterface(Skin skin) {
        super(skin);

        float buttonWidth = 170 * GlobalConf.SCALE_FACTOR;

        bgLoading = new OwnTextButton("Pause background loading", skin);
        this.add(bgLoading).center().row();
        bgLoading.setVisible(false);
        bgLoading.setWidth(buttonWidth);
        bgLoading.addListener(new EventListener() {

            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    if (loadingPaused) {
                        EventManager.instance.post(Events.RESUME_BACKGROUND_LOADING);
                        loadingPaused = false;
                        bgLoading.setText("Pause background loading");
                    } else {
                        EventManager.instance.post(Events.PAUSE_BACKGROUND_LOADING);
                        loadingPaused = true;
                        bgLoading.setText("Resume background loading");
                    }
                }
                return false;
            }

        });

        keyboardImg = new Image(skin.getDrawable("no-input"));
        cameraImg = new Image(skin.getDrawable("camera"));
        this.add(keyboardImg).center().row();
        this.add(cameraImg).center().row();

        keyboardImg.setVisible(!GlobalConf.runtime.INPUT_ENABLED);
        cameraImg.setVisible(false);

        int num = ScriptingFactory.getInstance().getNumRunningScripts();
        cancelScript = new OwnTextButton(I18n.bundle.format("gui.script.stop", num), skin);
        this.add(cancelScript).center().row();
        cancelScript.setVisible(num > 0);
        cancelScript.setWidth(buttonWidth);
        cancelScript.addListener(new EventListener() {

            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.CANCEL_SCRIPT_CMD);
                }
                return false;
            }
        });

        cancelCamera = new OwnTextButton(I18n.bundle.get("gui.stop"), skin);
        this.add(cancelCamera).center();
        cancelCamera.setVisible(false);
        cancelCamera.setWidth(buttonWidth);
        cancelCamera.addListener(new EventListener() {

            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.STOP_CAMERA_PLAY);
                }
                return false;
            }

        });

        EventManager.instance.subscribe(this, Events.INPUT_ENABLED_CMD, Events.NUM_RUNNING_SCRIPTS, Events.CAMERA_PLAY_INFO, Events.BACKGROUND_LOADING_INFO);
    }

    private void unsubscribe() {
        EventManager.instance.unsubscribe(this, Events.INPUT_ENABLED_CMD, Events.NUM_RUNNING_SCRIPTS, Events.CAMERA_PLAY_INFO, Events.BACKGROUND_LOADING_INFO);
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case INPUT_ENABLED_CMD:
            keyboardImg.setVisible(!(boolean) data[0]);
            break;
        case CAMERA_PLAY_INFO:
            boolean play = (boolean) data[0];
            cameraImg.setVisible(play);
            cancelCamera.setVisible(play);
            break;
        case NUM_RUNNING_SCRIPTS:
            int num = (Integer) data[0];
            cancelScript.setVisible(num > 0);
            cancelScript.setText(I18n.bundle.format("gui.script.stop", num));
            break;
        case BACKGROUND_LOADING_INFO:
            bgLoading.setVisible(true);
            break;
        default:
            break;
        }
    }

    @Override
    public void dispose() {
        unsubscribe();
    }

}
