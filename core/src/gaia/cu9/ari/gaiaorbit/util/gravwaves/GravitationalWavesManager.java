package gaia.cu9.ari.gaiaorbit.util.gravwaves;

import com.badlogic.gdx.math.Matrix3;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * Central hub where the parameters of the current gravitational
 * wave are updated and served to the renderers.
 * @author tsagrista
 *
 */
public class GravitationalWavesManager {

    private static GravitationalWavesManager instance;

    public static GravitationalWavesManager instance() {
        return instance;
    }

    public static void initialize(ITimeFrameProvider time) {
        instance = new GravitationalWavesManager(time);
    }

    /** Cartesian coordinates from the origin of the grav wave. Unit vector **/
    public Vector3 gw;
    /** Rotation of gw **/
    public Matrix3 gwmat3;
    /** Rotation of gw **/
    public Matrix4 gwmat4;
    /** Time in seconds, synced to the simulation time. Ready to pass to the shader **/
    public float gwtime;

    /** Wave frequency **/
    public float omgw;

    /** Hterms: hpluscos, hplussin, htimescos, htimessin **/
    public float[] hterms;

    /** Intial time for the counter **/
    private long initime;

    /** Unit vector **/
    private Vector3 unitz;

    private GravitationalWavesManager(ITimeFrameProvider time) {
        super();
        gw = new Vector3();
        gwmat3 = new Matrix3();
        gwmat4 = new Matrix4();
        initime = time.getTime().getTime();
        gwtime = 0f;
        hterms = new float[4];
        unitz = new Vector3(0, 0, 1);
    }

    /**
     * This must be called every cycle, it updates
     * the needed parameters for the gravitational waves
     * @param time
     */
    public void update(ITimeFrameProvider time) {
        if (GlobalConf.runtime.GRAVITATIONAL_WAVES) {
            // Time
            gwtime = (float) ((time.getTime().getTime() - initime) / 1000d);

            // Frequency
            omgw = 0.8f;

            // Hterms - hpluscos, hplussin, htimescos, htimessin
            hterms[0] = 0.3f;
            hterms[1] = 0.f;
            hterms[2] = 0.f;
            hterms[3] = 0.f;

            // Coordinates of wave
            gw.set(1, 1, 0).nor();

            // Rotation matrix
            gwmat4.setToRotation(unitz, gw);
            gwmat3.set(gwmat4);
        }
    }
}
