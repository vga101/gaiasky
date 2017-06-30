package gaia.cu9.ari.gaiaorbit.scenegraph;

/**
 * A particle group which additionally to the xyz position, supports color and
 * magnitude. x y z col size appmag absmag sourceid
 * 
 * @author tsagrista
 *
 */
public class StarGroup extends ParticleGroup {

    public StarGroup() {
        super();
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        addToRender(this, RenderGroup.STAR_GROUP);

        if (renderText()) {
            addToRender(this, RenderGroup.LABEL);
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

}
