package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.scenegraph.CameraManager.CameraMode;
import gaia.cu9.ari.gaiaorbit.scenegraph.NaturalCamera;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.vr.VRContext.VRControllerButtons;
import gaia.cu9.ari.gaiaorbit.vr.VRContext.VRControllerRole;
import gaia.cu9.ari.gaiaorbit.vr.VRContext.VRDevice;
import gaia.cu9.ari.gaiaorbit.vr.VRDeviceListener;

public class OpenVRListener implements VRDeviceListener {

    private NaturalCamera cam;

    public OpenVRListener(NaturalCamera cam) {
        this.cam = cam;
    }

    public void connected(VRDevice device) {
        Logger.info(device + " connected");
        EventManager.instance.post(Events.VR_DEVICE_CONNECTED, device);
    }

    public void disconnected(VRDevice device) {
        Logger.info(device + " disconnected");
        EventManager.instance.post(Events.VR_DEVICE_DISCONNECTED, device);
    }

    public void buttonPressed(VRDevice device, int button) {
        if (GlobalConf.controls.DEBUG_MODE) {
            Logger.info(device + " button pressed: " + button);
        }

        if (button == VRControllerButtons.Grip) {
            if (device.getControllerRole().compareTo(VRControllerRole.RightHand) == 0) {
                cam.setVelocity(1);
            }
            if (device.getControllerRole().compareTo(VRControllerRole.LeftHand) == 0) {
                cam.setVelocity(-1);
            }
        }
    }

    public void buttonReleased(VRDevice device, int button) {
        if (GlobalConf.controls.DEBUG_MODE) {
            Logger.info(device + " button released: " + button);
        }

        if (button == VRControllerButtons.Grip) {
            cam.setVelocity(0);
        } else if (button == VRControllerButtons.A) {
            // Change mode from free to focus and viceversa
            CameraMode cm = cam.getMode().equals(CameraMode.Focus) ? CameraMode.Free_Camera : CameraMode.Focus;
            EventManager.instance.post(Events.CAMERA_MODE_CMD, cm);
        }
    }

    @Override
    public void event(int code) {
        if (GlobalConf.controls.DEBUG_MODE) {
            Logger.info("Unhandled event: " + code);
        }
    }
}
