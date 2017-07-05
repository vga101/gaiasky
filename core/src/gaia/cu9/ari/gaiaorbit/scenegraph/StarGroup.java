package gaia.cu9.ari.gaiaorbit.scenegraph;

import java.util.Arrays;
import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.TimeUtils;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.render.ILineRenderable;
import gaia.cu9.ari.gaiaorbit.render.system.LineRenderSystem;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import net.jafama.FastMath;

/**
 * A particle group which additionally to the xyz position, supports color and
 * magnitude. id x y z pmx pmy pmz appmag absmag col size additional
 * 
 * @author tsagrista
 *
 */
public class StarGroup extends ParticleGroup implements ILineRenderable, IStarFocus {

    public static final int SIZE = 19;
    /** INDICES **/
    public static final int I_X = 0;
    public static final int I_Y = 1;
    public static final int I_Z = 2;
    public static final int I_PMX = 3;
    public static final int I_PMY = 4;
    public static final int I_PMZ = 5;
    public static final int I_ID = 6;
    public static final int I_APPMAG = 7;
    public static final int I_ABSMAG = 8;
    public static final int I_COL = 9;
    public static final int I_SIZE = 10;
    public static final int I_HIP = 11;
    public static final int I_TYC1 = 12;
    public static final int I_TYC2 = 13;
    public static final int I_TYC3 = 14;
    public static final int I_MUALPHA = 15;
    public static final int I_MUDELTA = 16;
    public static final int I_RADVEL = 17;
    public static final int I_ADDITIONAL = 18;

    // Camera dx threshold
    private static final double CAM_DX_TH = 100 * Constants.AU_TO_U;
    // Min update time
    private static final double MIN_UPDATE_TIME_MS = 50;
    // Close up stars treated
    private static final int N_CLOSEUP_STARS = 5000;

    // Additional values
    double[] additional;

    // Indices list buffer 1
    Integer[] indices1;
    // Indices list buffer 2
    Integer[] indices2;
    // Active indices list
    Integer[] active;
    // Background indices list (the one we sort)
    Integer[] background;

    // Sorter daemon
    private SorterThread daemon;

    private Vector3d lastSortCameraPos;
    private Comparator<Integer> comp;

    public StarGroup() {
        super();
        comp = new StarGroupComparator();
        lastSortCameraPos = new Vector3d(Double.MAX_VALUE, Double.MAX_VALUE, Double.MAX_VALUE);
        EventManager.instance.subscribe(this, Events.CAMERA_MOTION_UPDATED, Events.DISPOSE);
    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);

        // Additional
        additional = new double[pointData.size];

        // Initialise indices list with natural order
        indices1 = new Integer[pointData.size];
        indices2 = new Integer[pointData.size];
        for (int i = 0; i < pointData.size; i++) {
            indices1[i] = i;
            indices2[i] = i;
        }
        active = indices1;
        background = indices2;

        /**
         * INITIALIZE DAEMON LOADER THREAD
         */
        daemon = new SorterThread(this);
        daemon.setDaemon(true);
        daemon.setName("daemon-star-group-sorter");
        daemon.setPriority(Thread.MIN_PRIORITY);
        daemon.start();
    }

    /**
     * Updates the additional information array, to use for sorting.
     * 
     * @param camera
     */
    public void updateAdditional(ICamera camera) {
        Vector3d cpos = camera.getPos();
        int n = pointData.size;

        for (int i = 0; i < n; i++) {
            double[] d = pointData.get(i);
            additional[i] = -(((d[I_SIZE] * Constants.STAR_SIZE_FACTOR) / cpos.dst(d[I_X], d[I_Y], d[I_Z])) / camera.getFovFactor()) * GlobalConf.scene.STAR_BRIGHTNESS;
        }
    }

    /**
     * Sorts the list of particles using the apparent magnitude (view angle)
     * criterion. This should be called only when the camera frustum is
     * significantly changed. By no means should this be called every cycle.
     * Sorting happens always in the working buffer, which is not the active
     * buffer. After sorting, buffers are swapped.
     */
    public void updateSorter(ITimeFrameProvider time, ICamera camera) {
        // Prepare metadata to sort
        updateAdditional(camera);
        // Sort background list of indices
        Arrays.sort(background, comp);
        // Update last sort call
        super.updateSorter(time, camera);
        // Swap indices lists
        swapBuffers();
    }

    private void swapBuffers() {
        Gdx.app.postRunnable(new Runnable() {

            @Override
            public void run() {
                if (active == indices1) {
                    active = indices2;
                    background = indices1;
                } else {
                    active = indices1;
                    background = indices2;
                }

            }

        });
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        addToRender(this, RenderGroup.STAR_GROUP);
        addToRender(this, RenderGroup.LINE);
        if (renderText()) {
            addToRender(this, RenderGroup.LABEL);
        }
    }

    /**
     * Proper motion rendering
     */
    @Override
    public void render(LineRenderSystem renderer, ICamera camera, float alpha) {
        float thpointTimesFovfactor = (float) GlobalConf.scene.STAR_THRESHOLD_POINT * camera.getFovFactor();
        for (int i = N_CLOSEUP_STARS; i >= 0; i--) {
            double[] star = pointData.get(active[i]);
            float radius = (float) (getSize(active[i]) * Constants.STAR_SIZE_FACTOR);
            Vector3d lpos = aux3d1.get().set(star[I_X], star[I_Y], star[I_Z]).sub(camera.getPos());
            float distToCamera = (float) lpos.len();
            float viewAngle = (float) (((radius / distToCamera) / camera.getFovFactor()) * GlobalConf.scene.STAR_BRIGHTNESS);
            if (viewAngle >= thpointTimesFovfactor / GlobalConf.scene.PM_NUM_FACTOR) {

                Vector3d p1 = aux3d1.get().set(star[I_X], star[I_Y], star[I_Z]).sub(camera.getPos());
                Vector3d ppm = aux3d2.get().set(star[I_PMX], star[I_PMY], star[I_PMZ]).scl(GlobalConf.scene.PM_LEN_FACTOR);
                Vector3d p2 = ppm.add(p1);

                // Mualpha -> red channel
                // Mudelta -> green channel
                // Radvel -> blue channel
                // Min value per channel = 0.2
                final double mumin = -80;
                final double mumax = 80;
                final double maxmin = mumax - mumin;
                renderer.addLine(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, (float) ((star[I_MUALPHA] - mumin) / maxmin) * 0.8f + 0.2f, (float) ((star[I_MUDELTA] - mumin) / maxmin) * 0.8f + 0.2f, (float) (star[I_RADVEL]) * 0.8f + 0.2f, alpha * this.opacity);

            }

        }

    }

    /**
     * Label rendering
     */
    @Override
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font3d, BitmapFont font2d, ICamera camera) {
        float thOverFactor = (float) (GlobalConf.scene.STAR_THRESHOLD_POINT / GlobalConf.scene.LABEL_NUMBER_FACTOR / camera.getFovFactor());
        float textScale = 2f;
        for (int i = N_CLOSEUP_STARS; i >= 0; i--) {
            double[] star = pointData.get(active[i]);
            float radius = (float) (getSize(active[i]) * Constants.STAR_SIZE_FACTOR);
            Vector3d lpos = aux3d1.get().set(star[I_X], star[I_Y], star[I_Z]).sub(camera.getPos());
            float distToCamera = (float) lpos.len();
            float viewAngle = (float) (((radius / distToCamera) / camera.getFovFactor()) * GlobalConf.scene.STAR_BRIGHTNESS);

            if (viewAngle >= thOverFactor) {

                textPosition(camera, lpos, distToCamera, radius);
                shader.setUniformf("a_viewAngle", viewAngle);
                shader.setUniformf("a_viewAnglePow", 1f);
                shader.setUniformf("a_thOverFactor", thOverFactor);
                shader.setUniformf("a_thOverFactorScl", camera.getFovFactor());
                float textSize = (float) FastMath.tan(viewAngle) * distToCamera * 1e5f;
                float alpha = Math.min((float) FastMath.atan(textSize / distToCamera), 2.e-3f);
                textSize = (float) FastMath.tan(alpha) * distToCamera;
                render3DLabel(batch, shader, font3d, camera, String.valueOf((long) star[I_ID]), lpos, textScale, textSize, textColour(), this.opacity);

            }
        }

    }

    public void textPosition(ICamera cam, Vector3d out, float len, float rad) {
        out.clamp(0, len - rad).scl(0.9f);

        Vector3d aux = aux3d2.get();
        aux.set(cam.getUp());

        aux.crs(out).nor();

        float dist = -0.02f * (float) out.len();

        aux.add(cam.getUp()).nor().scl(dist);

        out.add(aux);

    }

    public double getFocusSize() {
        return focusData[I_SIZE];
    }

    public float getAppmag() {
        return (float) focusData[I_APPMAG];
    }

    public float getAbsmag() {
        return (float) focusData[I_ABSMAG];
    }

    public String getName() {
        if (focusData != null)
            return String.valueOf((long) focusData[I_ID]);
        else
            return null;
    }

    public long getId() {
        if (focusData != null)
            return (long) focusData[I_ID];
        else
            return -1;
    }

    /**
     * Returns the size of the particle at index i
     * 
     * @param i
     *            The index
     * @return The size
     */
    public double getSize(int i) {
        return pointData.get(i)[I_SIZE];
    }

    /**
     * Uses whatever precomputed value is in index 8 to compare the values
     * 
     * @author tsagrista
     *
     */
    private class StarGroupComparator implements Comparator<Integer> {
        @Override
        public int compare(Integer i1, Integer i2) {
            return Double.compare(additional[i1], additional[i2]);
        }
    }

    private static class SorterThread extends Thread {
        public boolean awake;
        public boolean running;

        private StarGroup sg;
        public Vector3d currentCameraPos;

        public SorterThread(StarGroup sg) {
            super();
            this.awake = false;
            this.running = true;
            this.sg = sg;
        }

        public void stopExecution() {
            this.running = false;
        }

        @Override
        public void run() {
            while (running) {
                sg.updateSorter(GaiaSky.instance.time, GaiaSky.instance.getICamera());
                sg.lastSortCameraPos.set(currentCameraPos);

                /** ----------- SLEEP UNTIL INTERRUPTED ----------- **/
                try {
                    awake = false;
                    Thread.sleep(Long.MAX_VALUE - 8);
                } catch (InterruptedException e) {
                    // New data!
                    awake = true;
                }
            }
        }

    }

    @Override
    public void notify(Events event, Object... data) {
        // Super handles FOCUS_CHANGED event
        super.notify(event, data);
        switch (event) {
        case CAMERA_MOTION_UPDATED:
            final Vector3d currentCameraPos = (Vector3d) data[0];
            long t = TimeUtils.millis() - lastSortTime;
            if (!daemon.awake && this.opacity > 0 && (t > MIN_UPDATE_TIME_MS * 2 || (lastSortCameraPos.dst(currentCameraPos) > CAM_DX_TH && t > MIN_UPDATE_TIME_MS))) {
                // Update
                daemon.currentCameraPos = currentCameraPos;
                daemon.interrupt();
            }
            break;
        case DISPOSE:
            if (daemon != null)
                daemon.stopExecution();
            break;
        default:
            break;
        }

    }

    @Override
    public int getCatalogSource() {
        return 1;
    }

    @Override
    public int getHip() {
        return -1;
    }

    @Override
    public String getTycho() {
        return null;
    }

    @Override
    public long getCandidateId() {
        return (long) pointData.get(candidateFocusIndex)[I_ID];
    }

    @Override
    public String getCandidateName() {
        return String.valueOf((long) pointData.get(candidateFocusIndex)[I_ID]);
    }

}
