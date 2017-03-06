package gaia.cu9.ari.gaiaorbit.render.system;

import com.badlogic.gdx.utils.Array;

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

    public void render(Array<IRenderable> renderables, ICamera camera, float t, RenderContext rc);

    public void resize(int w, int h);

}
