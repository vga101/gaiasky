package gaia.cu9.ari.gaiaorbit.render.system;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Texture.TextureWrap;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.bitfire.postprocessing.utils.FullscreenQuad;
import com.bitfire.utils.ShaderLoader;

import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.render.RenderContext;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.util.Constants;

/**
 * Renders volumetric clouds in the galaxy. Just a test for now.
 * @author tsagrista
 *
 */
public class VolumeCloudsRenderSystem extends AbstractRenderSystem {

    private FullscreenQuad quad;
    private ShaderProgram shaderProgram;

    private Vector3 camPos, camDir, camUp;

    private Texture staticTex;

    private float opacity;

    private long initime;

    public VolumeCloudsRenderSystem(int priority, float[] alphas) {
        super(null, priority, alphas);
        ShaderLoader.BasePath = "shaders/";
        shaderProgram = ShaderLoader.fromFile("screenspace", "volumeclouds");
        quad = new FullscreenQuad();

        staticTex = new Texture(Gdx.files.internal("img/static.jpg"));
        staticTex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
        staticTex.setWrap(TextureWrap.Repeat, TextureWrap.Repeat);

        camPos = new Vector3();
        camDir = new Vector3();
        camUp = new Vector3();

        initime = TimeUtils.millis();
        opacity = 1.0f;

        // Init uniforms
        shaderProgram.begin();
        shaderProgram.setUniformi("u_texture0", 0);
        shaderProgram.setUniformf("u_iterations", 100f);
        shaderProgram.setUniformf("u_cloudDensity", 0.5f);
        shaderProgram.setUniformf("u_viewDistance", 6.0f);
        shaderProgram.setUniformf("u_cloudColor", 1.0f, 1.0f, 1.0f);
        shaderProgram.setUniformf("u_skyColor", 0.0f, 0.0f, 0.0f);
        shaderProgram.end();
    }

    @Override
    public void render(Array<IRenderable> renderables, ICamera camera, float t, RenderContext rc) {
        this.rc = rc;
        run(preRunnable, renderables, camera);

        shaderProgram.begin();
        staticTex.bind(0);
        // Set uniforms - camera and viewport basically
        shaderProgram.setUniformf("u_time", (float) ((TimeUtils.millis() - initime) / 1000d));
        shaderProgram.setUniformf("u_opacity", opacity);
        shaderProgram.setUniformf("u_viewport", rc.w, rc.h);
        shaderProgram.setUniformf("u_camPos", camera.getPos().setVector3(camPos).scl((float) Constants.U_TO_PC));
        shaderProgram.setUniformf("u_camDir", camera.getDirection().setVector3(camDir).nor());
        shaderProgram.setUniformf("u_camUp", camera.getUp().setVector3(camUp).nor());

        // Render
        quad.render(shaderProgram);
        shaderProgram.end();

        run(postRunnable, renderables, camera);
    }

    public void renderStud(Array<IRenderable> renderables, ICamera camera, float t) {
        // empty
    }

}
