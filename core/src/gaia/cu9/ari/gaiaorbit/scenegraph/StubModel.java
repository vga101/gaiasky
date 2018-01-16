package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;

import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.render.RenderingContext;
import gaia.cu9.ari.gaiaorbit.util.ComponentTypes;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public class StubModel extends AbstractPositionEntity implements IModelRenderable {

    private ModelInstance instance;
    private Environment env;

    public StubModel(ModelInstance instance, Environment env) {
        super();
        this.instance = instance;
        this.env = env;
        setCt("Others");
    }

    @Override
    public ComponentTypes getComponentType() {
        return ct;
    }

    @Override
    public double getDistToCamera() {
        return 0;
    }

    @Override
    public float getOpacity() {
        return 0;
    }

    @Override
    public void render(ModelBatch modelBatch, float alpha, double t, RenderingContext rc) {
        setTransparency(alpha);
        modelBatch.render(instance, env);
    }

    public void setTransparency(float alpha) {
        if (instance != null) {
            int n = instance.materials.size;
            for (int i = 0; i < n; i++) {
                Material mat = instance.materials.get(i);
                BlendingAttribute ba = null;
                if (mat.has(BlendingAttribute.Type)) {
                    ba = (BlendingAttribute) mat.get(BlendingAttribute.Type);
                } else {
                    ba = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
                    mat.set(ba);
                }
                ba.opacity = alpha;
            }
        }
    }

    @Override
    public boolean hasAtmosphere() {
        return false;
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        addToRender(this, RenderGroup.MODEL_NORMAL);
    }

    public void addToRenderLists() {
        this.addToRenderLists(null);
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {

    }

}
