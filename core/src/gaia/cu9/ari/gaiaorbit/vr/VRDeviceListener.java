package gaia.cu9.ari.gaiaorbit.vr;

import gaia.cu9.ari.gaiaorbit.vr.VRContext.VRControllerButtons;
import gaia.cu9.ari.gaiaorbit.vr.VRContext.VRDevice;

public interface VRDeviceListener {
    /** A new {@link VRDevice} has connected **/
    void connected(VRDevice device);

    /** A {@link VRDevice} has disconnected **/
    void disconnected(VRDevice device);

    /**
     * A button from {@link VRControllerButtons} was pressed on the
     * {@link VRDevice}
     **/
    void buttonPressed(VRDevice device, int button);

    /**
     * A button from {@link VRControllerButtons} was released on the
     * {@link VRDevice}
     **/
    void buttonReleased(VRDevice device, int button);

    /**
     * A button from {@link VRControllerButtons} was touched on the {@link VRDevice}
     */
    void buttonTouched(VRDevice device, int button);

    /**
     * A button from {@link VRControllerButtons} was untouched on the {@link VRDevice}
     */
    void buttonUntouched(VRDevice device, int button);

    /**
     * Unhandled event on the {@link VRDevice}
     * 
     * @param code
     *            Event code
     */
    void event(int code);
}
