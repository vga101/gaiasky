package gaia.cu9.ari.gaiaorbit.desktop.util;

import gaia.cu9.ari.gaiaorbit.util.ISysUtils;
import gaia.cu9.ari.gaiaorbit.util.SysUtilsFactory;

public class DesktopSysUtilsFactory extends SysUtilsFactory {

    ISysUtils sysutils;

    public DesktopSysUtilsFactory() {
	this.sysutils = new SysUtils();
    }

    @Override
    public ISysUtils getSysutils() {
	return sysutils;
    }

}
