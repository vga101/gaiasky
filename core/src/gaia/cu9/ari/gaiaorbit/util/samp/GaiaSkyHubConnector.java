package gaia.cu9.ari.gaiaorbit.util.samp;

import org.astrogrid.samp.client.ClientProfile;
import org.astrogrid.samp.client.HubConnector;

import gaia.cu9.ari.gaiaorbit.util.Logger;

/**
 * Extends hub connector to provide some very basic logging using
 * the Gaia Sky internal logging system.
 * @author tsagrista
 *
 */
public class GaiaSkyHubConnector extends HubConnector {

    public GaiaSkyHubConnector(ClientProfile profile) {
        super(profile);
    }

    @Override
    protected void connectionChanged(boolean isConnected) {
        super.connectionChanged(isConnected);
        Logger.info(this.getClass().getSimpleName(), isConnected ? "Connected to SAMP hub" : "Disconnected from SAMP hub");
    }

    @Override
    protected void disconnect() {
        super.disconnect();
        Logger.info(this.getClass().getSimpleName(), "Disconnected from SAMP hub");
    }

}
