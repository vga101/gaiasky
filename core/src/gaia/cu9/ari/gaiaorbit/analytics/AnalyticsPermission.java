package gaia.cu9.ari.gaiaorbit.analytics;

import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

public class AnalyticsPermission implements IPermission {

    @Override
    public boolean check() {
        return GlobalConf.program != null && GlobalConf.program.ANALYTICS_ENABLED;
    }

}
