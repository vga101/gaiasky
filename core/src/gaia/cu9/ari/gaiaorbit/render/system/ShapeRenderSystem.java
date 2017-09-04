package gaia.cu9.ari.gaiaorbit.render.system;

import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.render.IShapeRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;

public class ShapeRenderSystem extends AbstractRenderSystem {

    private ShapeRenderer shapeRenderer;

    public ShapeRenderSystem(RenderGroup rg, int priority, float[] alphas) {
        super(rg, priority, alphas);
        this.shapeRenderer = new ShapeRenderer();

    }

    @Override
    public void renderStud(Array<IRenderable> renderables, ICamera camera, double t) {
        int size = renderables.size;

        shapeRenderer.begin(ShapeType.Line);
        for (int i = 0; i < size; i++) {
            IShapeRenderable sr = (IShapeRenderable) renderables.get(i);
            sr.render(shapeRenderer, rc, getAlpha(sr), camera);
        }
        shapeRenderer.end();

    }

    @Override
    public void resize(int w, int h) {
        super.resize(w, h);
        shapeRenderer.setProjectionMatrix(shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, w, h));
    }

}
