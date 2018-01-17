package gaia.cu9.ari.gaiaorbit.interfce;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.vr.VRContext.VRDevice;
import gaia.cu9.ari.gaiaorbit.vr.VRDeviceListener;

public class OpenVRListener implements VRDeviceListener {
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

        //        if (device == context.getDeviceByType(VRDeviceType.Controller)) {
        //            if (button == VRControllerButtons.SteamVR_Trigger)
        //                isTeleporting = true;
        //        }
    }

    public void buttonReleased(VRDevice device, int button) {
        if (GlobalConf.controls.DEBUG_MODE) {
            Logger.info(device + " button released: " + button);
        }

        //        if (device == context.getDeviceByType(VRDeviceType.Controller)) {
        //            if (button == VRControllerButtons.SteamVR_Trigger) {
        //                if (intersectControllerXZPlane(context.getDeviceByType(VRDeviceType.Controller), tmp)) {
        //                    // Teleportation
        //                    // - Tracker space origin in world space is initially at [0,0,0]
        //                    // - When teleporting, we want to set the tracker space origin in world space to the
        //                    // teleportation point
        //                    // - Then we need to offset the tracker space
        //                    // origin in world space by the camera
        //                    // x/z position so the camera is at the
        //                    // teleportation point in world space
        //                    tmp2.set(context.getDeviceByType(VRDeviceType.HeadMountedDisplay).getPosition(Space.Tracker));
        //                    tmp2.y = 0;
        //                    tmp.sub(tmp2);
        //
        //                    context.getTrackerSpaceOriginToWorldSpaceTranslationOffset().set(tmp);
        //                }
        //                isTeleporting = false;
        //            }
        //        }
    }

    @Override
    public void event(int code) {
        if (GlobalConf.controls.DEBUG_MODE) {
            Logger.info("Unhandled event: " + code);
        }
    }
}
