package gaia.cu9.ari.gaiaorbit.interfce.components;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.HorizontalGroup;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Slider;
import com.badlogic.gdx.scenes.scene2d.ui.TextTooltip;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener.ChangeEvent;
import com.badlogic.gdx.utils.Align;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnTextIconButton;

public class CameraComponent extends GuiComponent implements IObserver {

    protected OwnLabel fov, speed, turn, rotate, date;
    protected SelectBox<String> cameraMode, cameraSpeedLimit;
    protected Slider fieldOfView, cameraSpeed, turnSpeed, rotateSpeed;
    protected CheckBox focusLock, orientationLock, crosshair;
    protected OwnTextIconButton button3d, buttonDome, buttonCubemap, buttonAnaglyph, button3dtv, buttonVR, buttonCrosseye;
    private float fovBackup;
    protected boolean fovFlag = true;

    public CameraComponent(Skin skin, Stage stage) {
        super(skin, stage);
        EventManager.instance.subscribe(this, Events.CAMERA_MODE_CMD, Events.ROTATION_SPEED_CMD, Events.TURNING_SPEED_CMD, Events.CAMERA_SPEED_CMD, Events.SPEED_LIMIT_CMD, Events.TOGGLE_STEREOSCOPIC_INFO, Events.FOV_CHANGE_NOTIFICATION);
    }

    @Override
    public void initialize() {
        Label modeLabel = new Label(txt("gui.camera.mode"), skin, "default");
        int cameraModes = CameraMode.values().length;
        String[] cameraOptions = new String[cameraModes];
        for (int i = 0; i < cameraModes; i++) {
            cameraOptions[i] = CameraMode.getMode(i).toString();
        }
        cameraMode = new SelectBox<String>(skin);
        cameraMode.setName("camera mode");
        cameraMode.setItems(cameraOptions);
        cameraMode.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    String selection = cameraMode.getSelected();
                    CameraMode mode = null;
                    try {
                        mode = CameraMode.fromString(selection);
                    } catch (IllegalArgumentException e) {
                        // Foucs to one of our models
                        mode = CameraMode.Focus;
                        EventManager.instance.post(Events.FOCUS_CHANGE_CMD, selection, true);
                    }

                    EventManager.instance.post(Events.CAMERA_MODE_CMD, mode);
                    return true;
                }
                return false;
            }
        });

        List<Button> buttonList = new ArrayList<Button>();
        Image img3d = new Image(new Texture(Gdx.files.internal("img/3d.png")));
        Image imgDome = new Image(new Texture(Gdx.files.internal("img/dome.png")));
        Image imgCubemap = new Image(new Texture(Gdx.files.internal("img/cubemap.png")));
        Image imgAnaglyph = new Image(new Texture(Gdx.files.internal("img/anaglyph.png")));
        Image img3dtv = new Image(new Texture(Gdx.files.internal("img/3dtv.png")));
        Image imgCrosseye = new Image(new Texture(Gdx.files.internal("img/crosseye.png")));
        Image imgVR = new Image(new Texture(Gdx.files.internal("img/vr.png")));

        button3d = new OwnTextIconButton("", img3d, skin, "toggle");
        button3d.addListener(new TextTooltip(GlobalResources.capitalise(txt("element.stereomode")), skin));
        button3d.setName("3d");
        button3d.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.TOGGLE_STEREOSCOPIC_CMD, txt("notif.stereoscopic"), button3d.isChecked());
                    return true;
                }
                return false;
            }
        });

        buttonDome = new OwnTextIconButton("", imgDome, skin, "toggle");
        buttonDome.addListener(new TextTooltip(GlobalResources.capitalise(txt("element.planetarium")), skin));
        buttonDome.setName("dome");
        buttonDome.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {

                    EventManager.instance.post(Events.FISHEYE_CMD, buttonDome.isChecked());
                    if (buttonDome.isChecked()) {
                        fovBackup = GaiaSky.instance.cam.getCamera().fieldOfView;
                        EventManager.instance.post(Events.FOV_CHANGED_CMD, 110f);
                        EventManager.instance.post(Events.PLANETARIUM_FOCUS_ANGLE_CMD, 30f);
                    } else {
                        EventManager.instance.post(Events.FOV_CHANGED_CMD, fovBackup);
                        EventManager.instance.post(Events.PLANETARIUM_FOCUS_ANGLE_CMD, 0f);
                    }
                    return true;
                }
                return false;
            }
        });

        buttonCubemap = new OwnTextIconButton("", imgCubemap, skin, "toggle");
        buttonCubemap.addListener(new TextTooltip(GlobalResources.capitalise(txt("element.360")), skin));
        buttonCubemap.setName("cubemap");
        buttonCubemap.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.CUBEMAP360_CMD, buttonCubemap.isChecked());
                    return true;
                }
                return false;
            }
        });

        buttonList.add(button3d);
        buttonList.add(buttonDome);
        buttonList.add(buttonCubemap);

        for (Button b : buttonList)
            b.setSize(Math.round(35 * GlobalConf.SCALE_FACTOR), Math.round(32 * GlobalConf.SCALE_FACTOR));

        Label fovLabel = new Label(txt("gui.camera.fov"), skin, "default");
        fieldOfView = new Slider(Constants.MIN_FOV, Constants.MAX_FOV, 1, false, skin);
        fieldOfView.setName("field of view");
        fieldOfView.setValue(GlobalConf.scene.CAMERA_FOV);
        fieldOfView.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (fovFlag && event instanceof ChangeEvent) {
                    float value = MathUtilsd.clamp(fieldOfView.getValue(), Constants.MIN_FOV, Constants.MAX_FOV);
                    EventManager.instance.post(Events.FOV_CHANGED_CMD, value);
                    fov.setText(Integer.toString((int) value) + "°");
                    return true;
                }
                return false;
            }

        });
        fov = new OwnLabel(Integer.toString((int) GlobalConf.scene.CAMERA_FOV) + "°", skin, "default");

        /** CAMERA SPEED LIMIT **/
        String[] speedLimits = new String[14];
        speedLimits[0] = txt("gui.camera.speedlimit.100kmh");
        speedLimits[1] = txt("gui.camera.speedlimit.c");
        speedLimits[2] = txt("gui.camera.speedlimit.cfactor", 2);
        speedLimits[3] = txt("gui.camera.speedlimit.cfactor", 10);
        speedLimits[4] = txt("gui.camera.speedlimit.cfactor", 1000);
        speedLimits[5] = txt("gui.camera.speedlimit.aus", 1);
        speedLimits[6] = txt("gui.camera.speedlimit.aus", 10);
        speedLimits[7] = txt("gui.camera.speedlimit.aus", 1000);
        speedLimits[8] = txt("gui.camera.speedlimit.aus", 10000);
        speedLimits[9] = txt("gui.camera.speedlimit.pcs", 1);
        speedLimits[10] = txt("gui.camera.speedlimit.pcs", 2);
        speedLimits[11] = txt("gui.camera.speedlimit.pcs", 10);
        speedLimits[12] = txt("gui.camera.speedlimit.pcs", 1000);
        speedLimits[13] = txt("gui.camera.speedlimit.nolimit");

        cameraSpeedLimit = new SelectBox<String>(skin);
        cameraSpeedLimit.setName("camera speed limit");
        cameraSpeedLimit.setItems(speedLimits);
        cameraSpeedLimit.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    int idx = cameraSpeedLimit.getSelectedIndex();
                    EventManager.instance.post(Events.SPEED_LIMIT_CMD, idx, true);
                    return true;
                }
                return false;
            }
        });
        cameraSpeedLimit.setSelectedIndex(GlobalConf.scene.CAMERA_SPEED_LIMIT_IDX);

        /** CAMERA SPEED **/
        cameraSpeed = new Slider(Constants.MIN_SLIDER, Constants.MAX_SLIDER / 2, 1, false, skin);
        cameraSpeed.setName("camera speed");
        cameraSpeed.setValue(GlobalConf.scene.CAMERA_SPEED * Constants.CAMERA_SPEED_FACTOR);
        cameraSpeed.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.CAMERA_SPEED_CMD, cameraSpeed.getValue() / Constants.CAMERA_SPEED_FACTOR, true);
                    speed.setText(Integer.toString((int) cameraSpeed.getValue()));
                    return true;
                }
                return false;
            }

        });
        speed = new OwnLabel(Integer.toString((int) (GlobalConf.scene.CAMERA_SPEED * Constants.CAMERA_SPEED_FACTOR)), skin, "default");

        /** ROTATION SPEED **/
        rotateSpeed = new Slider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
        rotateSpeed.setName("rotate speed");
        rotateSpeed.setValue(MathUtilsd.lint(GlobalConf.scene.ROTATION_SPEED, Constants.MIN_ROT_SPEED, Constants.MAX_ROT_SPEED, Constants.MIN_SLIDER, Constants.MAX_SLIDER));
        rotateSpeed.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.ROTATION_SPEED_CMD, MathUtilsd.lint(rotateSpeed.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_ROT_SPEED, Constants.MAX_ROT_SPEED), true);
                    rotate.setText(Integer.toString((int) rotateSpeed.getValue()));
                    return true;
                }
                return false;
            }

        });
        rotate = new OwnLabel(Integer.toString((int) MathUtilsd.lint(GlobalConf.scene.ROTATION_SPEED, Constants.MIN_ROT_SPEED, Constants.MAX_ROT_SPEED, Constants.MIN_SLIDER, Constants.MAX_SLIDER)), skin, "default");

        /** TURNING SPEED **/
        turnSpeed = new Slider(Constants.MIN_SLIDER, Constants.MAX_SLIDER, 1, false, skin);
        turnSpeed.setName("turn speed");
        turnSpeed.setValue(MathUtilsd.lint(GlobalConf.scene.TURNING_SPEED, Constants.MIN_TURN_SPEED, Constants.MAX_TURN_SPEED, Constants.MIN_SLIDER, Constants.MAX_SLIDER));
        turnSpeed.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.TURNING_SPEED_CMD, MathUtilsd.lint(turnSpeed.getValue(), Constants.MIN_SLIDER, Constants.MAX_SLIDER, Constants.MIN_TURN_SPEED, Constants.MAX_TURN_SPEED), true);
                    turn.setText(Integer.toString((int) turnSpeed.getValue()));
                    return true;
                }
                return false;
            }

        });
        turn = new OwnLabel(Integer.toString((int) MathUtilsd.lint(GlobalConf.scene.TURNING_SPEED, Constants.MIN_TURN_SPEED, Constants.MAX_TURN_SPEED, Constants.MIN_SLIDER, Constants.MAX_SLIDER)), skin, "default");

        /** Focus lock **/
        focusLock = new CheckBox(" " + txt("gui.camera.lock"), skin);
        focusLock.setName("focus lock");
        focusLock.setChecked(GlobalConf.scene.FOCUS_LOCK);
        focusLock.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.FOCUS_LOCK_CMD, txt("gui.camera.lock"), focusLock.isChecked());
                    orientationLock.setVisible(focusLock.isChecked());
                    return true;
                }
                return false;
            }
        });

        /** Focus orientation lock **/
        orientationLock = new CheckBox(" " + txt("gui.camera.lock.orientation"), skin);
        orientationLock.setName("orientation lock");
        orientationLock.setChecked(GlobalConf.scene.FOCUS_LOCK_ORIENTATION);
        orientationLock.setVisible(GlobalConf.scene.FOCUS_LOCK);
        orientationLock.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.ORIENTATION_LOCK_CMD, txt("gui.camera.lock.orientation"), orientationLock.isChecked());
                    return true;
                }
                return false;
            }
        });

        /** Crosshair **/
        crosshair = new CheckBox(" " + txt("gui.camera.crosshair"), skin);
        crosshair.setName("orientation lock");
        crosshair.setChecked(GlobalConf.scene.CROSSHAIR);
        crosshair.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event instanceof ChangeEvent) {
                    EventManager.instance.post(Events.CROSSHAIR_CMD, crosshair.isChecked());
                    return true;
                }
                return false;
            }
        });

        VerticalGroup cameraGroup = new VerticalGroup().align(Align.left);

        float space3 = 3 * GlobalConf.SCALE_FACTOR;

        HorizontalGroup buttonGroup = new HorizontalGroup();
        buttonGroup.space(space3);
        buttonGroup.addActor(button3d);
        buttonGroup.addActor(buttonDome);
        buttonGroup.addActor(buttonCubemap);

        HorizontalGroup fovGroup = new HorizontalGroup();
        fovGroup.space(space3);
        fovGroup.addActor(fieldOfView);
        fovGroup.addActor(fov);

        HorizontalGroup speedGroup = new HorizontalGroup();
        speedGroup.space(space3);
        speedGroup.addActor(cameraSpeed);
        speedGroup.addActor(speed);

        HorizontalGroup rotateGroup = new HorizontalGroup();
        rotateGroup.space(space3);
        rotateGroup.addActor(rotateSpeed);
        rotateGroup.addActor(rotate);

        HorizontalGroup turnGroup = new HorizontalGroup();
        turnGroup.space(space3);
        turnGroup.addActor(turnSpeed);
        turnGroup.addActor(turn);

        cameraGroup.addActor(modeLabel);
        cameraGroup.addActor(cameraMode);
        cameraGroup.addActor(fovLabel);
        cameraGroup.addActor(fovGroup);
        cameraGroup.addActor(new Label(txt("gui.camera.speedlimit"), skin, "default"));
        cameraGroup.addActor(cameraSpeedLimit);
        cameraGroup.addActor(new Label(txt("gui.camera.speed"), skin, "default"));
        cameraGroup.addActor(speedGroup);
        cameraGroup.addActor(new Label(txt("gui.rotation.speed"), skin, "default"));
        cameraGroup.addActor(rotateGroup);
        cameraGroup.addActor(new Label(txt("gui.turn.speed"), skin, "default"));
        cameraGroup.addActor(turnGroup);
        cameraGroup.addActor(focusLock);
        cameraGroup.addActor(orientationLock);
        cameraGroup.addActor(crosshair);
        cameraGroup.space(5);
        cameraGroup.addActor(buttonGroup);

        component = cameraGroup;

        cameraGroup.pack();
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case CAMERA_MODE_CMD:
            // Update camera mode selection
            CameraMode mode = (CameraMode) data[0];
            cameraMode.setSelected(mode.toString());
            break;
        case ROTATION_SPEED_CMD:
            Boolean interf = (Boolean) data[1];
            if (!interf) {
                float value = (Float) data[0];
                value = MathUtilsd.lint(value, Constants.MIN_ROT_SPEED, Constants.MAX_ROT_SPEED, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
                rotateSpeed.setValue(value);
                rotate.setText(Integer.toString((int) value));
            }
            break;
        case CAMERA_SPEED_CMD:
            interf = (Boolean) data[1];
            if (!interf) {
                float value = (Float) data[0];
                value *= Constants.CAMERA_SPEED_FACTOR;
                cameraSpeed.setValue(value);
                speed.setText(Integer.toString((int) value));
            }
            break;

        case TURNING_SPEED_CMD:
            interf = (Boolean) data[1];
            if (!interf) {
                float value = (Float) data[0];
                value = MathUtilsd.lint(value, Constants.MIN_TURN_SPEED, Constants.MAX_TURN_SPEED, Constants.MIN_SLIDER, Constants.MAX_SLIDER);
                turnSpeed.setValue(value);
                turn.setText(Integer.toString((int) value));
            }
            break;
        case SPEED_LIMIT_CMD:
            interf = false;
            if (data.length > 1)
                interf = (Boolean) data[1];
            if (!interf) {
                int value = (Integer) data[0];
                cameraSpeedLimit.setSelectedIndex(value);
            }
            break;
        case TOGGLE_STEREOSCOPIC_INFO:
            boolean stereo = (boolean) data[0];
            button3d.setProgrammaticChangeEvents(false);
            button3d.setChecked(stereo);
            button3d.setProgrammaticChangeEvents(true);
            break;
        case FOV_CHANGE_NOTIFICATION:
            fovFlag = false;
            fieldOfView.setValue(GlobalConf.scene.CAMERA_FOV);
            fov.setText(Integer.toString((int) GlobalConf.scene.CAMERA_FOV) + "°");
            fovFlag = true;
            break;
        }

    }
}
