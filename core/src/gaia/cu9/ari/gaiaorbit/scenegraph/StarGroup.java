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
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * A particle group which additionally to the xyz position, supports color and
 * magnitude. x y z col size appmag absmag sourceid additional
 * 
 * @author tsagrista
 *
 */
public class StarGroup extends ParticleGroup {
    // Camera dx threshold
    private static final double CAM_DX_TH = 100 * Constants.AU_TO_U;
    // Min update time
    private static final double MIN_UPDATE_TIME_MS = 300;

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
            additional[i] = -(((d[4] * Constants.STAR_SIZE_FACTOR) / cpos.dst(d[0], d[1], d[2])) / camera.getFovFactor()) * GlobalConf.scene.STAR_BRIGHTNESS;
            //additional[i] = cpos.dst(d[0], d[1], d[2]);
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

        if (renderText()) {
            addToRender(this, RenderGroup.LABEL);
        }
    }

    /**
     * Label rendering.
     */
    @Override
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font3d, BitmapFont font2d, ICamera camera) {
        float thOverFactor = (float) (GlobalConf.scene.STAR_THRESHOLD_POINT / GlobalConf.scene.LABEL_NUMBER_FACTOR / camera.getFovFactor());
        float textScale = 0.487463442f;
        for (int i = 600; i >= 0; i--) {
            double[] star = pointData.get(active[i]);
            double radius = getSize(i) * Constants.STAR_SIZE_FACTOR;
            Vector3d lpos = aux3d2.get().set(star[0], star[1], star[2]).sub(camera.getPos());
            float distToCamera = (float) lpos.len();
            float viewAngle = (float) (((radius / distToCamera) / camera.getFovFactor()) * GlobalConf.scene.STAR_BRIGHTNESS);
            shader.setUniformf("a_viewAngle", viewAngle);
            shader.setUniformf("a_viewAnglePow", 1f);
            shader.setUniformf("a_thOverFactor", thOverFactor);
            shader.setUniformf("a_thOverFactorScl", camera.getFovFactor());
            float textSize = Math.min(viewAngle * .3e14f, 1e6f);

            render3DLabel(batch, shader, font3d, camera, Integer.toString(i), lpos, textScale, textSize, textColour(), this.opacity);
        }

    }

    public double getFocusSize() {
        return focusData[4];
    }

    public float getAppmag() {
        return (float) focusData[5];
    }

    public float getAbsmag() {
        return (float) focusData[6];
    }

    public String getName() {
        if (focusData != null)
            return String.valueOf((long) focusData[7]);
        else
            return null;
    }

    /**
     * Returns the size of the particle at index i
     * 
     * @param i
     *            The index
     * @return The size
     */
    public double getSize(int i) {
        return pointData.get(i)[4];
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

}
