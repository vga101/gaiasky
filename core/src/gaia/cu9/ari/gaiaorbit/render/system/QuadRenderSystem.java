package gaia.cu9.ari.gaiaorbit.render.system;

import java.util.Collections;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Mesh.VertexDataType;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.bitfire.postprocessing.filters.Scattering;

import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.render.PostProcessorFactory;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.DecalUtils;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.comp.DistToCameraComparator;
import gaia.cu9.ari.gaiaorbit.util.time.TimeUtils;

public class QuadRenderSystem extends AbstractRenderSystem implements IObserver {

    private ShaderProgram shaderProgram;
    private Mesh mesh;
    private boolean useStarColorTransit;
    private boolean starColorTransit = false;
    private Texture noise;
    private Quaternion quaternion;

    private float[] aux;
    private Vector3 auxv;

    /**
     * Creates a new shader quad render component.
     * 
     * @param rg
     *            The render group.
     * @param priority
     *            The priority of the component.
     * @param alphas
     *            The alphas list.
     * @param shaderProgram
     *            The shader program to render the quad with.
     * @param useStarColorTransit
     *            Whether to use the star color transit or not.
     */
    public QuadRenderSystem(RenderGroup rg, int priority, float[] alphas, ShaderProgram shaderProgram, boolean useStarColorTransit) {
        super(rg, priority, alphas);
        this.shaderProgram = shaderProgram;
        this.useStarColorTransit = useStarColorTransit;
        init();
        if (this.useStarColorTransit)
            EventManager.instance.subscribe(this, Events.TRANSIT_COLOUR_CMD);
    }

    private void init() {
        // Init comparator
        comp = new DistToCameraComparator<IRenderable>();
        // Init vertices
        float[] vertices = new float[20];
        fillVertices(vertices);

        noise = new Texture(Gdx.files.internal(GlobalConf.TEXTURES_FOLDER + "static.jpg"));
        noise.setFilter(TextureFilter.Linear, TextureFilter.Linear);

        // We wont need indices if we use GL_TRIANGLE_FAN to draw our quad
        // TRIANGLE_FAN will draw the verts in this order: 0, 1, 2; 0, 2, 3
        mesh = new Mesh(VertexDataType.VertexArray, true, 4, 6, new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE), new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE), new VertexAttribute(Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE + "0"));

        mesh.setVertices(vertices, 0, vertices.length);
        mesh.getIndicesBuffer().position(0);
        mesh.getIndicesBuffer().limit(6);

        short[] indices = new short[] { 0, 1, 2, 0, 2, 3 };
        mesh.setIndices(indices);

        quaternion = new Quaternion();

        aux = new float[Scattering.N * 2];
        auxv = new Vector3();
    }

    private void fillVertices(float[] vertices) {
        float x = 1;
        float y = 1;
        float width = -2;
        float height = -2;
        final float fx2 = x + width;
        final float fy2 = y + height;
        final float u = 0;
        final float v = 1;
        final float u2 = 1;
        final float v2 = 0;

        float color = Color.WHITE.toFloatBits();

        int idx = 0;
        vertices[idx++] = x;
        vertices[idx++] = y;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v;

        vertices[idx++] = x;
        vertices[idx++] = fy2;
        vertices[idx++] = color;
        vertices[idx++] = u;
        vertices[idx++] = v2;

        vertices[idx++] = fx2;
        vertices[idx++] = fy2;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v2;

        vertices[idx++] = fx2;
        vertices[idx++] = y;
        vertices[idx++] = color;
        vertices[idx++] = u2;
        vertices[idx++] = v;
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
        Collections.sort(renderables, comp);

        // Get N closer stars and project position to screen, then post event
        int nrend = renderables.size();

        int w = Gdx.graphics.getWidth();
        int h = Gdx.graphics.getHeight();

        IRenderable r = renderables.get(nrend - 1);
        if (r instanceof Particle) {
            Particle p = (Particle) r;
            // TODO This is an ugly hack for volumetric lighting to work (only with Sol)
            if (!Constants.webgl && p.name.equalsIgnoreCase("Sol") && PostProcessorFactory.instance.getPostProcessor().isLightScatterEnabled()) {
                camera.getCamera().project(p.transform.getTranslationf(auxv));
                if (auxv.x >= 0 && auxv.y >= 0 && auxv.x <= w && auxv.y <= h) {
                    aux[0] = auxv.x / w;
                    aux[1] = auxv.y / h;
                }
                EventManager.instance.post(Events.LIGHT_POS_2D_UPDATED, 1, aux);
            } else {
                EventManager.instance.post(Events.LIGHT_POS_2D_UPDATED, 0, aux);
            }
        }

        // Calculate billobard rotation quaternion ONCE
        DecalUtils.setBillboardRotation(quaternion, camera.getCamera().direction, camera.getCamera().up);

        shaderProgram.begin();

        // General uniforms
        shaderProgram.setUniformMatrix("u_projTrans", camera.getCamera().combined);
        shaderProgram.setUniformf("u_quaternion", quaternion.x, quaternion.y, quaternion.z, quaternion.w);

        if (!Constants.mobile) {
            // Global uniforms
            shaderProgram.setUniformf("u_time", TimeUtils.getRunningTimeSecs());
            // Bind
            noise.bind(4);
            shaderProgram.setUniformi("u_noiseTexture", 4);
        }

        int size = renderables.size();
        for (int i = 0; i < size; i++) {
            IRenderable s = renderables.get(i);
            s.render(shaderProgram, alphas[s.getComponentType().ordinal()], starColorTransit, mesh, camera);
        }
        shaderProgram.end();

    }

    @Override
    public void notify(Events event, Object... data) {
        if (event == Events.TRANSIT_COLOUR_CMD) {
            starColorTransit = (boolean) data[1];
        }

    }

}
