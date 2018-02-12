package gaia.cu9.ari.gaiaorbit.render.system;

import java.util.Comparator;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.render.IAnnotationsRenderable;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.scenegraph.Text2D;
import gaia.cu9.ari.gaiaorbit.util.comp.DistToCameraComparator;

public class FontRenderSystem extends AbstractRenderSystem {

    private SpriteBatch batch;
    private ShaderProgram shaderProgram;
    public BitmapFont font3d, font2d, fontTitles;
    private Comparator<IRenderable> comp;

    public FontRenderSystem(RenderGroup rg, float[] alphas, SpriteBatch batch) {
        super(rg, alphas);
        this.batch = batch;
        // Init comparator
        comp = new DistToCameraComparator<IRenderable>();
    }

    public FontRenderSystem(RenderGroup rg, float[] alphas, SpriteBatch batch, ShaderProgram shaderProgram, BitmapFont font3d, BitmapFont font2d, BitmapFont fontTitles) {
        this(rg, alphas, batch);
        this.shaderProgram = shaderProgram;

        this.font3d = font3d;
        this.font2d = font2d;
        this.fontTitles = fontTitles;

    }

    @Override
    public void renderStud(Array<IRenderable> renderables, ICamera camera, double t) {
        renderables.sort(comp);
        batch.begin();

        int size = renderables.size;
        if (shaderProgram == null) {
            for (int i = 0; i < size; i++) {
                IAnnotationsRenderable s = (IAnnotationsRenderable) renderables.get(i);
                // Render sprite
                s.render(batch, camera, getAlpha(s));
            }
        } else {
            float lalpha = alphas[ComponentType.Labels.ordinal()];
            font3d.getData().setScale(0.6f);
            for (int i = 0; i < size; i++) {
                I3DTextRenderable s = (I3DTextRenderable) renderables.get(i);

                // Regular mode, we use 3D distance field font
                I3DTextRenderable lr = (I3DTextRenderable) s;
                // Label color
                shaderProgram.setUniform4fv("u_color", lr.textColour(), 0, 4);
                // Component alpha
                shaderProgram.setUniformf("u_componentAlpha", getAlpha(s) * (s instanceof Text2D ? 1 : lalpha));
                // Font opacity multiplier, take into account element opacity
                shaderProgram.setUniformf("u_opacity", 0.75f * lr.getOpacity());

                s.render(batch, shaderProgram, this, rc, camera);
            }
        }
        batch.end();

    }

    @Override
    public void resize(int w, int h) {
        super.resize(w, h);
        updateBatchSize(w, h);
    }

    @Override
    public void updateBatchSize(int w, int h) {
        batch.setProjectionMatrix(batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h));
    }

}
