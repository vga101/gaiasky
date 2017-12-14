package gaia.cu9.ari.gaiaorbit.render.system;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.data.orbit.OrbitData;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.Gaia;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.Orbit;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

public class LineGPURenderSystem extends ImmediateRenderSystem {
    protected ICamera camera;
    protected int glType;

    private Matrix4 modelView;

    /** Hopefully we won't have more than 1000000 orbits at once **/
    private final int N_MESHES = 1000000;

    public LineGPURenderSystem(RenderGroup rg, int priority, float[] alphas) {
        super(rg, priority, alphas);
        modelView = new Matrix4();
        glType = GL20.GL_LINE_STRIP;
    }

    @Override
    protected void initShaderProgram() {
        shaderProgram = new ShaderProgram(Gdx.files.internal("shader/line.gpu.vertex.glsl"), Gdx.files.internal("shader/line.gpu.fragment.glsl"));
        if (!shaderProgram.isCompiled()) {
            Logger.error(this.getClass().getName(), "Line shader compilation failed:\n" + shaderProgram.getLog());
        }
    }

    @Override
    protected void initVertices() {
        meshes = new MeshData[N_MESHES];
    }

    /**
     * Adds a new mesh data to the meshes list and increases the mesh data index
     * 
     * @param nVertices
     *            The max number of vertices this mesh data can hold
     * @return The index of the new mesh data
     */
    private int addMeshData(int nVertices) {
        // look for index
        int mdi;
        for (mdi = 0; mdi < N_MESHES; mdi++) {
            if (meshes[mdi] == null) {
                break;
            }
        }

        if (mdi >= N_MESHES) {
            Logger.error(this.getClass().getSimpleName(), "No more free meshes!");
            return -1;
        }

        curr = new MeshData();
        meshes[mdi] = curr;

        maxVertices = nVertices + 1;

        VertexAttribute[] attribs = buildVertexAttributes();
        curr.mesh = new Mesh(false, maxVertices, 0, attribs);

        curr.vertices = new float[maxVertices * (curr.mesh.getVertexAttributes().vertexSize / 4)];
        curr.vertexSize = curr.mesh.getVertexAttributes().vertexSize / 4;
        curr.colorOffset = curr.mesh.getVertexAttribute(Usage.ColorPacked) != null ? curr.mesh.getVertexAttribute(Usage.ColorPacked).offset / 4 : 0;
        return mdi;
    }

    /**
     * Clears the mesh data at the index i
     * 
     * @param i
     *            The index
     */
    public void clearMeshData(int i) {
        assert i >= 0 && i < meshes.length : "Mesh data index out of bounds: " + i + " (n meshes = " + N_MESHES + ")";

        MeshData md = meshes[i];

        if (md != null && md.mesh != null) {
            md.mesh.dispose();

            meshes[i] = null;
        }
    }

    @Override
    public void renderStud(Array<IRenderable> renderables, ICamera camera, double t) {
        this.camera = camera;
        int size = renderables.size;

        /**
         * ADD LINES
         */
        for (int i = 0; i < size; i++) {
            Orbit renderable = (Orbit) renderables.get(i);
            if (!renderable.inGpu) {
                OrbitData od = renderable.orbitData;
                int npoints = od.getNumPoints();
                renderable.offset = addMeshData(npoints);
                float[] cc = renderable.cc;
                for (int point_i = 0; point_i < npoints; point_i++) {
                    color(cc[0], cc[1], cc[2], cc[3]);
                    vertex((float) od.getX(point_i), (float) od.getY(point_i), (float) od.getZ(point_i));
                }
                // Close loop
                color(cc[0], cc[1], cc[2], cc[3]);
                vertex((float) od.getX(0), (float) od.getY(0), (float) od.getZ(0));

                renderable.count = npoints * curr.vertexSize;
                renderable.inGpu = true;
            }

            /**
             * RENDER
             */
            curr = meshes[renderable.offset];

            // Smooth lines
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
            // Additive blending
            Gdx.gl20.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE);
            // Line width
            Gdx.gl.glLineWidth(0.5f * GlobalConf.SCALE_FACTOR);

            shaderProgram.begin();

            shaderProgram.setUniformMatrix("u_projModelView", modelView.set(camera.getCamera().combined).mul(renderable.localTransform));
            shaderProgram.setUniformf("u_alpha", (float) (renderable.alpha) * getAlpha(renderable));
            if (renderable.parent.name.equals("Gaia")) {
                Vector3d ppos = ((Gaia) renderable.parent).unrotatedPos;
                shaderProgram.setUniformf("u_parentPos", (float) ppos.x, (float) ppos.y, (float) ppos.z);
            } else {
                shaderProgram.setUniformf("u_parentPos", 0, 0, 0);
            }

            curr.mesh.setVertices(curr.vertices, 0, renderable.count);
            curr.mesh.render(shaderProgram, glType);

            shaderProgram.end();

            // Restore
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
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

}
