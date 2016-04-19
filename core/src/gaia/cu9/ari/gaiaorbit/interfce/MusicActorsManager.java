package gaia.cu9.ari.gaiaorbit.interfce;

public class MusicActorsManager {

    private static IMusicActors musicActors;

    public static void initialize(IMusicActors ma) {
        musicActors = ma;
    }

    public static IMusicActors getMusicActors() {
        return musicActors;
    }
}
