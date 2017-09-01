package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.ui.Cell;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.script.ScriptingFactory;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextIconButton;

/**
 * Contains elements which depend on the current state of the program, such as
 * the running scripts, the buttons to pause the camera subsystem, etc.
 * 
 * @author tsagrista
 *
 */
public class RunStateInterface extends Table implements IObserver, IGuiInterface {

    private Cell<?> keyboardImgCell, stopScriptCell, stopCameraCell, pauseBgCell;
    private Image keyboardImg;
    private TextButton cancelScript, cancelCamera, bgLoading;
    private boolean loadingPaused = false;

    public RunStateInterface(Skin skin) {
        super(skin);

        float pad = 2 * GlobalConf.SCALE_FACTOR;

        keyboardImg = new Image(skin.getDrawable("no-input"));

        Image dataloadPauseImg = new Image(skin.getDrawable("dataload-pause"));
        bgLoading = new OwnTextIconButton("", dataloadPauseImg, skin, "toggle");
        TextTooltip pauseBgTT = new TextTooltip("Pause background loading", skin);
        bgLoading.addListener(pauseBgTT);
        bgLoading.addListener((event) -> {
            if (event instanceof ChangeEvent) {
                if (loadingPaused) {
                    EventManager.instance.post(Events.RESUME_BACKGROUND_LOADING);
                    loadingPaused = false;
                    pauseBgTT.getActor().setText("Pause background loading");
                } else {
                    EventManager.instance.post(Events.PAUSE_BACKGROUND_LOADING);
                    loadingPaused = true;
                    pauseBgTT.getActor().setText("Resume background loading");
                }
            }
            return false;
        });

        int num = ScriptingFactory.getInstance().getNumRunningScripts();
        Image scriptStopImg = new Image(skin.getDrawable("script-stop"));
        cancelScript = new OwnTextIconButton("", scriptStopImg, skin);
        cancelScript.addListener(new TextTooltip(I18n.bundle.format("gui.script.stop", num), skin));
        cancelScript.addListener((event) -> {
            if (event instanceof ChangeEvent) {
                EventManager.instance.post(Events.CANCEL_SCRIPT_CMD);
            }
            return false;
        });

        Image cameraStopImg = new Image(skin.getDrawable("camera-stop"));
        cancelCamera = new OwnTextIconButton("", cameraStopImg, skin);
        cancelCamera.addListener(new TextTooltip(I18n.bundle.get("gui.stop"), skin));
        cancelCamera.addListener((event) -> {
            if (event instanceof ChangeEvent) {
                EventManager.instance.post(Events.STOP_CAMERA_PLAY);
            }
            return false;
        });

        pauseBgCell = this.add().right().padTop(pad);
        pauseBgCell.row();
        stopScriptCell = this.add().right().padTop(pad);
        stopScriptCell.row();
        stopCameraCell = this.add().right().padTop(pad);
        stopCameraCell.row();
        keyboardImgCell = this.add().right().padTop(pad);
        keyboardImgCell.row();

        EventManager.instance.subscribe(this, Events.INPUT_ENABLED_CMD, Events.NUM_RUNNING_SCRIPTS, Events.CAMERA_PLAY_INFO, Events.BACKGROUND_LOADING_INFO);
    }

    private void unsubscribe() {
        EventManager.instance.unsubscribe(this, Events.INPUT_ENABLED_CMD, Events.NUM_RUNNING_SCRIPTS, Events.CAMERA_PLAY_INFO, Events.BACKGROUND_LOADING_INFO);
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case INPUT_ENABLED_CMD:
            Gdx.app.postRunnable(() -> {
                boolean visible = !(boolean) data[0];
                if (visible) {
                    if (keyboardImgCell.getActor() == null)
                        keyboardImgCell.setActor(keyboardImg);
                } else {
                    keyboardImgCell.setActor(null);
                }
            });
            break;
        case CAMERA_PLAY_INFO:
            Gdx.app.postRunnable(() -> {
                boolean visible = (boolean) data[0];
                if (visible) {
                    if (stopCameraCell.getActor() == null)
                        stopCameraCell.setActor(cancelCamera);
                } else {
                    stopCameraCell.setActor(null);
                }
            });

            break;
        case NUM_RUNNING_SCRIPTS:
            Gdx.app.postRunnable(() -> {
                boolean visible = (Integer) data[0] > 0;
                if (visible) {
                    if (stopScriptCell.getActor() == null)
                        stopScriptCell.setActor(cancelScript);
                } else {
                    stopScriptCell.setActor(null);
                }
            });
            break;
        case BACKGROUND_LOADING_INFO:
            Gdx.app.postRunnable(() -> {
                if (pauseBgCell.getActor() == null)
                    pauseBgCell.setActor(bgLoading);
            });
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
