package gaia.cu9.ari.gaiaorbit.render.system;

import java.util.List;

import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.render.RenderContext;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;

/**
 * A component that renders a type of objects.
 * @author Toni Sagrista
 *
 */
public interface IRenderSystem extends Comparable<IRenderSystem> {

    public RenderGroup getRenderGroup();

    public int getPriority();

    public void render(List<IRenderable> renderables, ICamera camera, RenderContext rc);

    public void resize(int w, int h);

}
