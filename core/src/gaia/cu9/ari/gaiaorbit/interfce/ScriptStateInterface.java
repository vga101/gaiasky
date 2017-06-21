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

public class ScriptStateInterface extends Table implements IObserver, IGuiInterface {

    private Image keyboardImg, cameraImg;
    private TextButton cancelScript, cancelCamera;

    public ScriptStateInterface(Skin skin) {
        super(skin);

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
        cancelScript.setWidth(cancelScript.getWidth() + 4 * GlobalConf.SCALE_FACTOR);
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
        cancelCamera.setWidth(cancelCamera.getWidth() + 4 * GlobalConf.SCALE_FACTOR);
        cancelCamera.addListener(new EventListener() {

            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.STOP_CAMERA_PLAY);
                }
                return false;
            }

        });

        EventManager.instance.subscribe(this, Events.INPUT_ENABLED_CMD, Events.NUM_RUNNING_SCRIPTS, Events.CAMERA_PLAY_INFO);
    }

    private void unsubscribe() {
        EventManager.instance.unsubscribe(this, Events.INPUT_ENABLED_CMD, Events.NUM_RUNNING_SCRIPTS, Events.CAMERA_PLAY_INFO);
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
        default:
            break;
        }
    }

    @Override
    public void dispose() {
        unsubscribe();
    }

}
