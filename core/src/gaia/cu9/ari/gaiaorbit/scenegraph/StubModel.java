package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;

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
        modelBatch.render(instance, env);
    }

    @Override
    public boolean hasAtmosphere() {
        return false;
    }

    @Override
    protected void addToRenderLists(ICamera camera) {

    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {

    }

}
