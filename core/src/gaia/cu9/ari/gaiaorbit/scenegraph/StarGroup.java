package gaia.cu9.ari.gaiaorbit.scenegraph;

/**
 * A particle group which additionally to the xyz position, supports color and
 * magnitude.
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
}
