package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.math.Vector2;

import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

/**
 * Node that offers fade-in and fade-out capabilities.
 * 
 * @author tsagrista
 *
 */
public class FadeNode extends AbstractPositionEntity {

    /**
     * Fade in low and high limits
     */
    private Vector2 fadeIn;

    /**
     * Fade out low and high limits
     */
    private Vector2 fadeOut;

    /**
     * Colour of label
     */
    protected float[] labelColour;

    /**
     * The current distance at each cycle, in internal units
     */
    private double currentDistance;

    /**
     * If set, the fade distance will be computed against this object.
     * Otherwise, we use the static position in {@link SceneGraphNode.pos}
     */
    private AbstractPositionEntity position;

    /**
     * The name of the position object
     */
    private String positionobjectname;

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);
        if (positionobjectname != null) {
            this.position = (AbstractPositionEntity) sg.getNode(positionobjectname);
        }
    }

    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera, float opacity) {
        this.opacity = opacity * this.opacity;
        transform.set(parentTransform);
        Vector3d aux = aux3d1.get();

        if (this.name.equals("The Earth")) {
            int a = 234;
        }

        if (this.position == null) {
            this.currentDistance = aux.set(this.pos).sub(camera.getPos()).len() * camera.getFovFactor();
        } else {
            this.currentDistance = this.position.distToCamera;
        }

        // Update with translation/rotation/etc
        updateLocal(time, camera);

        if (children != null) {
            for (int i = 0; i < children.size; i++) {
                SceneGraphNode child = children.get(i);
                child.update(time, transform, camera, this.opacity);
            }
        }
    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
        this.distToCamera = this.position == null ? (float) pos.dst(camera.getPos()) : this.position.distToCamera;

        // Update alpha
        this.opacity = getBaseOpacity();
        if (fadeIn != null)
            this.opacity *= MathUtilsd.lint((float) this.currentDistance, fadeIn.x, fadeIn.y, 0, 1);
        if (fadeOut != null)
            this.opacity *= MathUtilsd.lint((float) this.currentDistance, fadeOut.x, fadeOut.y, 1, 0);

        if (!copy && opacity > 0) {
            addToRenderLists(camera);
        }

    }

    protected float getBaseOpacity() {
        return 1;
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
    }

    public void setFadein(double[] fadein) {
        fadeIn = new Vector2((float) (fadein[0] * Constants.PC_TO_U), (float) (fadein[1] * Constants.PC_TO_U));
    }

    public void setFadeout(double[] fadeout) {
        fadeOut = new Vector2((float) (fadeout[0] * Constants.PC_TO_U), (float) (fadeout[1] * Constants.PC_TO_U));
    }

    public void setPosition(double[] pos) {
        this.pos.set(pos[0] * Constants.PC_TO_U, pos[1] * Constants.PC_TO_U, pos[2] * Constants.PC_TO_U);
    }

    /**
     * Sets the label color
     * 
     * @param labelcolor
     */
    public void setLabelcolor(double[] labelcolor) {
        this.labelColour = GlobalResources.toFloatArray(labelcolor);
    }

    public void setPositionobjectname(String po) {
        this.positionobjectname = po;
    }

}
