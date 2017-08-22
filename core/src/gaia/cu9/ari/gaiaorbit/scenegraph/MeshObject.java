package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ITransform;
import gaia.cu9.ari.gaiaorbit.scenegraph.component.ModelComponent;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public class MeshObject extends FadeNode implements IModelRenderable {

    private String transformName;
    private Matrix4 coordinateSystem;
    private Vector3 scale, axis, translate;
    private float degrees;

    /** MODEL **/
    public ModelComponent mc;

    /** TRANSFORMATIONS - are applied each cycle **/
    public ITransform[] transformations;

    public MeshObject() {
        super();
        localTransform = new Matrix4();
    }

    public void initialize() {
        if (mc != null) {
            mc.initialize();
        }
    }

    @Override
    public void doneLoading(AssetManager manager) {
        super.doneLoading(manager);
        if (mc != null) {
            try {
                mc.doneLoading(manager, localTransform, cc);
            } catch (Exception e) {
                mc = null;
            }
        }

        if (mc != null) {
            coordinateSystem = new Matrix4();
            if (transformName != null) {
                Class<Coordinates> c = Coordinates.class;
                try {
                    Method m = ClassReflection.getMethod(c, transformName);
                    Matrix4 trf = (Matrix4) m.invoke(null);
                    coordinateSystem.set(trf);
                } catch (ReflectionException e) {
                    Logger.error(Grid.class.getName(), "Error getting/invoking method Coordinates." + transformName + "()");
                }
            } else {
                // Equatorial, nothing
            }

            if (scale != null)
                coordinateSystem.scl(scale);
            if (axis != null)
                coordinateSystem.rotate(axis, degrees);
            if (translate != null)
                coordinateSystem.translate(translate);
        }
    }

    @Override
    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
        super.updateLocal(time, camera);
        // Update light with global position
        if (mc != null) {
            mc.dlight.direction.set(transform.getTranslationf());
            mc.dlight.direction.add((float) camera.getPos().x, (float) camera.getPos().y, (float) camera.getPos().z);
            mc.dlight.color.set(1f, 1f, 1f, 0f);

            updateLocalTransform();
        }
    }

    /**
     * Update the local transform with the transform and the rotations/scales
     * necessary. Override if your model contains more than just the position
     * and size.
     */
    protected void updateLocalTransform() {
        setToLocalTransform(1, localTransform, true);
    }

    public void setToLocalTransform(float sizeFactor, Matrix4 localTransform, boolean forceUpdate) {
        if (sizeFactor != 1 || forceUpdate) {
            float[] trnsltn = transform.getTranslationf();
            localTransform.idt().translate(trnsltn[0], trnsltn[1], trnsltn[2]).scl((float) (size * sizeFactor)).mul(coordinateSystem);
        } else {
            localTransform.set(this.localTransform);
        }

        // Apply transformations
        if (transformations != null)
            for (ITransform tc : transformations)
                tc.apply(localTransform);
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        if (GaiaSky.instance.isOn(ct) && opacity > 0) {
            addToRender(this, RenderGroup.MODEL_F);
        }

    }

    @Override
    public void render(ModelBatch modelBatch, float alpha, double t) {
        if (mc != null) {
            mc.touch();
            mc.setTransparency(alpha * opacity);
            modelBatch.render(mc.instance, mc.env);
        }
    }

    public void setModel(ModelComponent mc) {
        this.mc = mc;
    }

    public void setTransformName(String transformName) {
        this.transformName = transformName;
    }

    public void setTranslate(double[] tr) {
        translate = new Vector3((float) tr[0], (float) tr[1], (float) tr[2]);
    }

    public void setRotate(double[] rt) {
        axis = new Vector3((float) rt[0], (float) rt[1], (float) rt[2]);
        degrees = (float) rt[3];
    }

    public void setScale(double[] sc) {
        scale = new Vector3((float) sc[0], (float) sc[1], (float) sc[2]);
    }

    @Override
    public boolean hasAtmosphere() {
        return false;
    }
}
