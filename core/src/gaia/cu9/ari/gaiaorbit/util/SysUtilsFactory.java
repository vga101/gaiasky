package gaia.cu9.ari.gaiaorbit.util;

public abstract class SysUtilsFactory {
    private static SysUtilsFactory instance;

    public static void initialize(SysUtilsFactory inst) {
	instance = inst;
    }

    public static ISysUtils getSysUtils() {
	return instance.getSysutils();
    }

    public abstract ISysUtils getSysutils();
}
