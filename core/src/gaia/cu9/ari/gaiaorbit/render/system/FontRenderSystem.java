package gaia.cu9.ari.gaiaorbit.render.system;

import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.render.IAnnotationsRenderable;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.FovCamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.comp.DistToCameraComparator;

public class FontRenderSystem extends AbstractRenderSystem {

    private SpriteBatch batch;
    private ShaderProgram shaderProgram;
    private BitmapFont font3d, font2d;
    private Comparator<IRenderable> comp;

    public FontRenderSystem(RenderGroup rg, int priority, float[] alphas, SpriteBatch batch) {
        super(rg, priority, alphas);
        this.batch = batch;

        // Init comparator
        comp = new DistToCameraComparator<IRenderable>();
    }

    public FontRenderSystem(RenderGroup rg, int priority, float[] alphas, SpriteBatch batch, ShaderProgram shaderProgram) {
        this(rg, priority, alphas, batch);
        this.shaderProgram = shaderProgram;

        // 3D font
        Texture texture3d = new Texture(Gdx.files.internal("font/main-font.png"), true);
        texture3d.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        font3d = new BitmapFont(Gdx.files.internal("font/main-font.fnt"), new TextureRegion(texture3d), false);

        // 2D font
        Texture texture2d = new Texture(Gdx.files.internal("font/font2d.png"), true);
        texture2d.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        font2d = new BitmapFont(Gdx.files.internal("font/font2d.fnt"), new TextureRegion(texture2d), false);

    }

    @Override
    public void renderStud(Array<IRenderable> renderables, ICamera camera, float t) {
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
            for (int i = 0; i < size; i++) {
                I3DTextRenderable s = (I3DTextRenderable) renderables.get(i);

                // Regular mode, we use 3D distance field font
                I3DTextRenderable lr = (I3DTextRenderable) s;
                shaderProgram.setUniformf("a_labelAlpha", (lr.isLabel() || camera.getCurrent() instanceof FovCamera ? alphas[ComponentType.Labels.ordinal()] : 1f));
                shaderProgram.setUniformf("a_componentAlpha", getAlpha(s));
                // Font opacity multiplier
                shaderProgram.setUniformf("u_opacity", 0.65f);

                s.render(batch, shaderProgram, font3d, font2d, camera);
            }
        }
        batch.end();

    }

    @Override
    public void resize(int w, int h) {
        super.resize(w, h);
        batch.getProjectionMatrix().setToOrtho2D(0, 0, w, h);
    }

}
