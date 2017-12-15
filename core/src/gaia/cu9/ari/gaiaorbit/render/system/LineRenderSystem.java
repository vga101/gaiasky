package gaia.cu9.ari.gaiaorbit.render.system;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.render.ILineRenderable;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.Logger;

public class LineRenderSystem extends ImmediateRenderSystem {
    protected ICamera camera;
    protected int glType;

    protected MeshData curr_outline;

    public LineRenderSystem(RenderGroup rg, int priority, float[] alphas) {
        super(rg, priority, alphas);
        glType = GL20.GL_LINES;
    }

    @Override
    protected void initShaderProgram() {
        shaderProgram = new ShaderProgram(Gdx.files.internal("shader/line.vertex.glsl"), Gdx.files.internal("shader/line.fragment.glsl"));
        if (!shaderProgram.isCompiled()) {
            Logger.error(this.getClass().getName(), "Line shader compilation failed:\n" + shaderProgram.getLog());
        }
    }

    @Override
    protected void initVertices() {
        meshes = new MeshData[2];
        maxVertices = 4000000;

        // ORIGINAL LINES
        curr = new MeshData();
        meshes[0] = curr;

        VertexAttribute[] attribs = buildVertexAttributes();
        curr.mesh = new Mesh(false, maxVertices, 0, attribs);

        curr.vertices = new float[maxVertices * (curr.mesh.getVertexAttributes().vertexSize / 4)];
        curr.vertexSize = curr.mesh.getVertexAttributes().vertexSize / 4;
        curr.colorOffset = curr.mesh.getVertexAttribute(Usage.ColorPacked) != null ? curr.mesh.getVertexAttribute(Usage.ColorPacked).offset / 4 : 0;
    }

    protected VertexAttribute[] buildVertexAttributes() {
        Array<VertexAttribute> attribs = new Array<VertexAttribute>();
        attribs.add(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
        attribs.add(new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE));

        VertexAttribute[] array = new VertexAttribute[attribs.size];
        for (int i = 0; i < attribs.size; i++)
            array[i] = attribs.get(i);
        return array;
    }

    @Override
    public void renderStud(Array<IRenderable> renderables, ICamera camera, double t) {
        this.camera = camera;
        int size = renderables.size;
        for (int i = 0; i < size; i++) {
            ILineRenderable renderable = (ILineRenderable) renderables.get(i);
            boolean rend = true;
            // TODO ugly hack
            if (renderable instanceof Particle && !GlobalConf.scene.PROPER_MOTION_VECTORS)
                rend = false;
            if (rend)
                // Orbits depend on sub component type
                renderable.render(this, camera, getAlpha(renderable));
        }

        shaderProgram.begin();
        shaderProgram.setUniformMatrix("u_projModelView", camera.getCamera().combined);

        if (Gdx.app.getType() == ApplicationType.Desktop) {
            // Enable GL_LINE_SMOOTH
            Gdx.gl20.glEnable(0xB20);
            // Enable GL_LINE_WIDTH
            Gdx.gl20.glEnable(0xB21);
            // Enable GL_ALIASED_LINE_WIDTH_RANGE
            Gdx.gl20.glEnable(0x846E);
            // Enable GL_SMOOTH_LINE_WIDTH_RANGE
            Gdx.gl20.glEnable(0xB22);
            // Enable GL_SMOOTH_LINE_WIDTH_GRANULARITY
            Gdx.gl20.glEnable(0xB23);
        }
        // Regular
        Gdx.gl.glLineWidth(1f * GlobalConf.SCALE_FACTOR);
        curr.mesh.setVertices(curr.vertices, 0, curr.vertexIdx);
        curr.mesh.render(shaderProgram, glType);

        shaderProgram.end();

        // CLEAR
        curr.clear();
    }

    public void addLine(double x0, double y0, double z0, double x1, double y1, double z1, Color col) {
        addLine(x0, y0, z0, x1, y1, z1, col.r, col.g, col.b, col.a);
    }

    public void addLine(double x0, double y0, double z0, double x1, double y1, double z1, float r, float g, float b, float a) {
        color(r, g, b, a);
        vertex((float) x0, (float) y0, (float) z0);
        color(r, g, b, a);
        vertex((float) x1, (float) y1, (float) z1);

    }

    public void addLine(double x0, double y0, double z0, double x1, double y1, double z1, float r, float g, float b, float a, double width) {
        addLine(x0, y0, z0, x1, y1, z1, r, g, b, a);
    }

    public void color_outline(float r, float g, float b, float a) {
        curr_outline.vertices[curr_outline.vertexIdx + curr_outline.colorOffset] = Color.toFloatBits(r, g, b, a);
    }

    public void vertex_outline(float x, float y, float z) {
        curr_outline.vertices[curr_outline.vertexIdx] = x;
        curr_outline.vertices[curr_outline.vertexIdx + 1] = y;
        curr_outline.vertices[curr_outline.vertexIdx + 2] = z;

        curr_outline.vertexIdx += curr_outline.vertexSize;
        curr_outline.numVertices++;
    }

}
