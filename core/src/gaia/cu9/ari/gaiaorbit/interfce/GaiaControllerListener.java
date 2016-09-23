package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.controllers.Controller;
import com.badlogic.gdx.controllers.ControllerListener;
import com.badlogic.gdx.controllers.PovDirection;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;

public class GaiaControllerListener implements ControllerListener {

    CameraManager cam;
    IGui gui;

    // These are the XBOX 360 mappings
    private static final int AXIS_JOY1VERT = 1;
    private static final int AXIS_JOY1HOR = 0;
    private static final int AXIS_JOY2HOR = 3;
    private static final int AXIS_JOY2VERT = 4;
    private static final int AXIS_LT = 2;
    private static final int AXIS_RT = 5;

    private static final int BUTTON_A = 0;
    private static final int BUTTON_B = 1;
    private static final int BUTTON_X = 2;
    private static final int BUTTON_Y = 3;
    private static final int BUTTON_LB = 4;
    private static final int BUTTON_RB = 5;
    private static final int BUTTON_BACK = 6;
    private static final int BUTTON_START = 7;
    private static final int BUTTON_XBOX_CROSS = 8;
    private static final int BUTTON_JOY1 = 9;
    private static final int BUTTON_JOY2 = 10;

    public GaiaControllerListener(CameraManager cam, IGui gui) {
        this.cam = cam;
        this.gui = gui;
    }

    @Override
    public void connected(Controller controller) {
        // TODO Auto-generated method stub

    }

    @Override
    public void disconnected(Controller controller) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean buttonDown(Controller controller, int buttonCode) {
        switch (buttonCode) {
        case BUTTON_LB:
            cam.naturalCamera.setGamepadMultiplier(0.5);
            break;
        case BUTTON_RB:
            cam.naturalCamera.setGamepadMultiplier(0.1);
            break;
        }
        return true;
    }

    @Override
    public boolean buttonUp(Controller controller, int buttonCode) {
        System.out.println(buttonCode);
        switch (buttonCode) {
        case BUTTON_LB:
        case BUTTON_RB:
            cam.naturalCamera.setGamepadMultiplier(1);
            break;
        }
        return true;
    }

    @Override
    public boolean axisMoved(Controller controller, int axisCode, float value) {
        boolean treated = false;
        // y = x^4
        // http://www.wolframalpha.com/input/?i=y+%3D+sign%28x%29+*+x%5E2+%28x+from+-1+to+1%29}
        value = Math.signum(value) * value * value * value * value;
        switch (axisCode) {
        case AXIS_JOY2HOR:
            if (cam.mode.equals(CameraMode.Focus)) {
                cam.naturalCamera.setRoll(value * 1e-2f);
            } else {
                // Use this for lateral movement
                cam.naturalCamera.setHorizontalRotation(value);
            }

            treated = true;
            break;
        case AXIS_JOY1VERT:
            if (cam.mode.equals(CameraMode.Focus)) {
                cam.naturalCamera.setVerticalRotation(value * 0.1);
            } else {
                cam.naturalCamera.setPitch(value * 1.5e-2f);
            }

            treated = true;
            break;
        case AXIS_JOY1HOR:
            if (cam.mode.equals(CameraMode.Focus)) {
                cam.naturalCamera.setHorizontalRotation(value * 0.1);
            } else {
                cam.naturalCamera.setYaw(value * 1.5e-2f);
            }

            treated = true;
            break;
        case AXIS_JOY2VERT:
            if (Math.abs(value) < 0.005)
                value = 0;
            cam.naturalCamera.setVelocity(-value);
            treated = true;
            break;
        case AXIS_RT:
            System.out.println(value);
            cam.naturalCamera.setVelocity((value + 1f) / 2.0f);
            treated = true;
            break;
        case AXIS_LT:

            cam.naturalCamera.setVelocity(-(value + 1f) / 2.0f);
            treated = true;
            break;
        }
        return treated;
    }

    @Override
    public boolean povMoved(Controller controller, int povCode, PovDirection value) {
        return false;
    }

    @Override
    public boolean xSliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean ySliderMoved(Controller controller, int sliderCode, boolean value) {
        return false;
    }

    @Override
    public boolean accelerometerMoved(Controller controller, int accelerometerCode, Vector3 value) {
        return false;
    }

}
