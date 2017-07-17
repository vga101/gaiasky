package gaia.cu9.ari.gaiaorbit.render.system;

import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.render.IAtmosphereRenderable;
import gaia.cu9.ari.gaiaorbit.render.IModelRenderable;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.comp.ModelComparator;

/**
 * Renders with a given model batch.
 * 
 * @author Toni Sagrista
 *
 */
public class ModelBatchRenderSystem extends AbstractRenderSystem {
    private ModelBatch batch;
    private boolean atmosphere;

    /**
     * Creates a new model batch render component.
     * 
     * @param rg
     *            The render group.
     * @param priority
     *            The priority.
     * @param alphas
     *            The alphas list.
     * @param batch
     *            The model batch.
     * @param atmosphere
     *            Atmosphere rendering.
     */
    public ModelBatchRenderSystem(RenderGroup rg, int priority, float[] alphas, ModelBatch batch, boolean atmosphere) {
        super(rg, priority, alphas);
        this.batch = batch;
        this.atmosphere = atmosphere;
        comp = new ModelComparator<IRenderable>();
    }

    @Override
    public void renderStud(Array<IRenderable> renderables, ICamera camera, double t) {
        renderables.sort(comp);
        if (mustRender()) {
            batch.begin(camera.getCamera());
            int size = renderables.size;
            for (int i = 0; i < size; i++) {
                IModelRenderable s = (IModelRenderable) renderables.get(i);
                if (!atmosphere) {
                    s.render(batch, getAlpha(s), t);
                } else {
                    ((IAtmosphereRenderable) s).render(batch, getAlpha(s), t, atmosphere);
                }
            }
            batch.end();

        }
    }

    protected boolean mustRender() {
        return true;
    }

}
