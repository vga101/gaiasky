package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.render.ILineRenderable;
import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.render.RenderingContext;
import gaia.cu9.ari.gaiaorbit.render.system.LineRenderSystem;
import gaia.cu9.ari.gaiaorbit.util.ComponentTypes;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.vr.VRContext.VRDevice;

public class StubModel extends AbstractPositionEntity implements IModelRenderable, ILineRenderable {

    public ModelInstance instance;
    private Environment env;
    private VRDevice device;
    private boolean delayRender = false;
    private Vector3 aux;

    public StubModel(VRDevice device, Environment env) {
        super();
        this.env = env;
        this.instance = device.getModelInstance();
        this.device = device;
        aux = new Vector3();
        this.cc = new float[] { 1f, 0f, 0f };
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

    public void addToRenderLists(RenderGroup rg) {
        if (rg != null) {
            addToRender(this, rg);
        }
        addToRender(this, RenderGroup.LINE);
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
    }

    @Override
    public void render(ModelBatch modelBatch, float alpha, double t, RenderingContext rc) {
        setTransparency(alpha);
        modelBatch.render(instance, env);
    }

    @Override
    public void render(LineRenderSystem renderer, ICamera camera, float alpha) {
        Matrix4 transform = instance.transform;
        aux.set(0, -0.1f, 0).mul(transform);
        double x = aux.x;
        double y = aux.y;
        double z = aux.z;
        aux.set(0, -.8e15f, -1e15f).mul(transform);
        renderer.addLine(x, y, z, aux.x, aux.y - 0.1, aux.z, 0.5f, 0f, 0f, alpha);
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
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {

    }

    public VRDevice getDevice() {
        return device;
    }

    public boolean getDelayRender() {
        return delayRender;
    }

    public void setDelayRender(boolean dr) {
        this.delayRender = dr;
    }

}
