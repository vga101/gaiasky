package gaia.cu9.ari.gaiaorbit.render.system;

import java.util.Comparator;

import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.render.RenderContext;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;

public abstract class AbstractRenderSystem implements IRenderSystem {
    /**
     * When this is true, new point information is available, so new data is
     * streamed to the GPU
     **/
    public static boolean POINT_UPDATE_FLAG = true;

    private RenderGroup group;
    protected int priority;
    protected float[] alphas;
    /** Comparator of renderables, in case of need **/
    protected Comparator<IRenderable> comp;
    public RenderContext rc;

    protected RenderSystemRunnable preRunnable, postRunnable;

    protected AbstractRenderSystem(RenderGroup rg, int priority, float[] alphas) {
        super();
        this.group = rg;
        this.priority = priority;
        this.alphas = alphas;
    }

    @Override
    public RenderGroup getRenderGroup() {
        return group;
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void render(Array<IRenderable> renderables, ICamera camera, float t, RenderContext rc) {
        if (renderables != null && renderables.size != 0) {
            this.rc = rc;
            run(preRunnable, renderables, camera);
            renderStud(renderables, camera, t);
            run(postRunnable, renderables, camera);
        }
    }

    public abstract void renderStud(Array<IRenderable> renderables, ICamera camera, float t);

    public void setPreRunnable(RenderSystemRunnable r) {
        preRunnable = r;
    }

    public void setPostRunnable(RenderSystemRunnable r) {
        postRunnable = r;
    }

    protected void run(RenderSystemRunnable runnable, Array<IRenderable> renderables, ICamera camera) {
        if (runnable != null) {
            runnable.run(this, renderables, camera);
        }
    }

    /**
     * Computes the alpha opacity value of a given renderable using its
     * component types
     * 
     * @param renderable
     *            The renderable
     * @return The alpha value as the product of all the alphas of its component
     *         types.
     */
    public float getAlpha(IRenderable renderable) {
        int idx = -1;
        float alpha = 1f;
        while ((idx = renderable.getComponentType().nextSetBit(idx + 1)) >= 0) {
            alpha *= alphas[idx];
        }
        return alpha;
    }

    @Override
    public int compareTo(IRenderSystem o) {
        return Integer.compare(priority, o.getPriority());
    }

    @Override
    public void resize(int w, int h) {
        // Empty, to override in subclasses if needed
    }

    public interface RenderSystemRunnable {
        public abstract void run(AbstractRenderSystem renderSystem, Array<IRenderable> renderables, ICamera camera);
    }

}
