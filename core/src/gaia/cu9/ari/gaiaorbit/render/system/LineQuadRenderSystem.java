package gaia.cu9.ari.gaiaorbit.render.system;

import java.util.Comparator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

import gaia.cu9.ari.gaiaorbit.render.ILineRenderable;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.IStarFocus;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

public class LineQuadRenderSystem extends LineRenderSystem {
    private static final int MAX_VERTICES = 5000000;
    private static final int INI_DPOOL_SIZE = 5000;
    private static final int MAX_DPOOL_SIZE = 100000;

    private MeshDataExt currext;
    private Array<double[]> provisionalLines;
    private LineArraySorter sorter;
    private Pool<double[]> dpool;

    private class MeshDataExt extends MeshData {
        int uvOffset;
        int indexIdx;
        int maxIndices;
        short[] indices;

        public void clear() {
            super.clear();
            indexIdx = 0;
        }
    }

    Vector3d line, camdir0, camdir1, camdir15, point, vec, aux, aux2;
    final static double widthAngle = Math.toRadians(0.06);
    final static double widthAngleTan = Math.tan(widthAngle);

    public LineQuadRenderSystem(RenderGroup rg, int priority, float[] alphas) {
        super(rg, priority, alphas);
        dpool = new DPool(INI_DPOOL_SIZE, MAX_DPOOL_SIZE);
        provisionalLines = new Array<double[]>();
        sorter = new LineArraySorter();
        glType = GL20.GL_TRIANGLES;
        line = new Vector3d();
        camdir0 = new Vector3d();
        camdir1 = new Vector3d();
        camdir15 = new Vector3d();
        point = new Vector3d();
        vec = new Vector3d();
        aux = new Vector3d();
        aux2 = new Vector3d();
    }

    @Override
    protected void initShaderProgram() {
        shaderProgram = new ShaderProgram(Gdx.files.internal("shader/line.quad.vertex.glsl"), Gdx.files.internal("shader/line.quad.fragment.glsl"));
        if (!shaderProgram.isCompiled()) {
            Logger.error(this.getClass().getName(), "Line shader compilation failed:\n" + shaderProgram.getLog());
        }
    }

    @Override
    protected void initVertices() {
        meshes = new MeshDataExt[1000];
        initVertices(meshIdx++);
    }

    private void initVertices(int index) {
        if (meshes[index] == null) {
            currext = new MeshDataExt();
            meshes[index] = currext;
            curr = currext;

            maxVertices = MAX_VERTICES;
            currext.maxIndices = maxVertices + maxVertices / 2;

            VertexAttribute[] attribs = buildVertexAttributes();
            currext.mesh = new Mesh(Mesh.VertexDataType.VertexArray, false, maxVertices, currext.maxIndices, attribs);

            currext.indices = new short[currext.maxIndices];
            currext.vertexSize = currext.mesh.getVertexAttributes().vertexSize / 4;
            currext.vertices = new float[maxVertices * currext.vertexSize];

            currext.colorOffset = currext.mesh.getVertexAttribute(Usage.ColorPacked) != null ? currext.mesh.getVertexAttribute(Usage.ColorPacked).offset / 4 : 0;
            currext.uvOffset = currext.mesh.getVertexAttribute(Usage.TextureCoordinates) != null ? currext.mesh.getVertexAttribute(Usage.TextureCoordinates).offset / 4 : 0;
        } else {
            currext = (MeshDataExt) meshes[index];
            curr = currext;
        }
    }

    protected VertexAttribute[] buildVertexAttributes() {
        Array<VertexAttribute> attribs = new Array<VertexAttribute>();
        attribs.add(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
        attribs.add(new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE));
        attribs.add(new VertexAttribute(Usage.TextureCoordinates, 2, "a_uv"));

        VertexAttribute[] array = new VertexAttribute[attribs.size];
        for (int i = 0; i < attribs.size; i++)
            array[i] = attribs.get(i);
        return array;
    }

    public void uv(float u, float v) {
        currext.vertices[currext.vertexIdx + currext.uvOffset] = u;
        currext.vertices[currext.vertexIdx + currext.uvOffset + 1] = v;
    }

    public void addLine(double x0, double y0, double z0, double x1, double y1, double z1, float r, float g, float b, float a) {
        addLineInternal(x0, y0, z0, x1, y1, z1, r, g, b, a);
    }

    public void addLineInternal(double x0, double y0, double z0, double x1, double y1, double z1, float r, float g, float b, float a) {
        addLineInternal(x0, y0, z0, x1, y1, z1, r, g, b, a, true);
    }

    public void addLineInternal(double x0, double y0, double z0, double x1, double y1, double z1, float r, float g, float b, float a, boolean rec) {
        double distToSegment = MathUtilsd.distancePointSegment(x0, y0, z0, x1, y1, z1, 0, 0, 0);

        double dist0 = Math.sqrt(x0 * x0 + y0 * y0 + z0 * z0);
        double dist1 = Math.sqrt(x1 * x1 + y1 * y1 + z1 * z1);

        Vector3d p15 = null;

        if (rec && distToSegment < dist0 && distToSegment < dist1) {
            // Projection falls in line, split line
            p15 = MathUtilsd.getClosestPoint2(x0, y0, z0, x1, y1, z1, 0, 0, 0);

            addLineInternal(x0, y0, z0, p15.x, p15.y, p15.z, r, g, b, a, false);
            addLineInternal(p15.x, p15.y, p15.z, x1, y1, z1, r, g, b, a, false);
        } else {
            // Add line to list
            // x0 y0 z0 x1 y1 z1 r g b a dist0 dist1 distMean
            double[] l = dpool.obtain();
            l[0] = x0;
            l[1] = y0;
            l[2] = z0;
            l[3] = x1;
            l[4] = y1;
            l[5] = z1;
            l[6] = r;
            l[7] = g;
            l[8] = b;
            l[9] = a;
            l[10] = dist0;
            l[11] = dist1;
            l[12] = (dist0 + dist1) / 2d;
            provisionalLines.add(l);
        }
    }

    public void addLinePostproc(double x0, double y0, double z0, double x1, double y1, double z1, double r, double g, double b, double a, double dist0, double dist1) {

        // Check if 6 more indices fit
        if (currext.numVertices + 3 >= shortLimit) {
            // We need to open a new MeshDataExt!
            initVertices(meshIdx++);
        }

        // Projection falls outside line
        double width0 = widthAngleTan * dist0 * camera.getFovFactor();
        double width1 = widthAngleTan * dist1 * camera.getFovFactor();

        line.set(x1 - x0, y1 - y0, z1 - z0);

        camdir0.set(x0, y0, z0);
        camdir1.set(x1, y1, z1);

        // Camdir0 and 1 will contain the perpendicular to camdir and line
        camdir0.crs(line);
        camdir1.crs(line);

        camdir0.setLength(width0);
        // P1
        point.set(x0, y0, z0).add(camdir0);
        color(r, g, b, a);
        uv(0, 0);
        vertex((float) point.x, (float) point.y, (float) point.z);

        // P2
        point.set(x0, y0, z0).sub(camdir0);
        color(r, g, b, a);
        uv(0, 1);
        vertex((float) point.x, (float) point.y, (float) point.z);

        camdir1.setLength(width1);
        // P3
        point.set(x1, y1, z1).add(camdir1);
        color(r, g, b, a);
        uv(1, 0);
        vertex((float) point.x, (float) point.y, (float) point.z);

        // P4
        point.set(x1, y1, z1).sub(camdir1);
        color(r, g, b, a);
        uv(1, 1);
        vertex((float) point.x, (float) point.y, (float) point.z);

        // Add indexes
        index((short) (currext.numVertices - 4));
        index((short) (currext.numVertices - 2));
        index((short) (currext.numVertices - 3));

        index((short) (currext.numVertices - 2));
        index((short) (currext.numVertices - 1));
        index((short) (currext.numVertices - 3));

    }

    private void index(short idx) {
        currext.indices[currext.indexIdx] = idx;
        currext.indexIdx++;
    }

    @Override
    public void renderStud(Array<IRenderable> renderables, ICamera camera, float t) {
        this.camera = camera;

        int size = renderables.size;
        for (int i = 0; i < size; i++) {
            ILineRenderable renderable = (ILineRenderable) renderables.get(i);
            boolean rend = true;
            if (renderable instanceof IStarFocus && !GlobalConf.scene.PROPER_MOTION_VECTORS)
                rend = false;
            if (rend)
                renderable.render(this, camera, getAlpha(renderable));
        }

        // Sort phase
        provisionalLines.sort(sorter);
        for (double[] l : provisionalLines)
            addLinePostproc(l[0], l[1], l[2], l[3], l[4], l[5], l[6], l[7], l[8], l[9], l[10], l[11]);

        shaderProgram.begin();
        shaderProgram.setUniformMatrix("u_projModelView", camera.getCamera().combined);

        for (int i = 0; i < meshIdx; i++) {
            MeshDataExt md = (MeshDataExt) meshes[i];
            md.mesh.setVertices(md.vertices, 0, md.vertexIdx);
            md.mesh.setIndices(md.indices, 0, md.indexIdx);
            md.mesh.render(shaderProgram, glType);

            md.clear();
        }

        shaderProgram.end();

        // Reset mesh index and current
        meshIdx = 1;
        currext = (MeshDataExt) meshes[0];
        curr = currext;
        int n = provisionalLines.size;
        for (int i = 0; i < n; i++)
            dpool.free(provisionalLines.get(i));
        provisionalLines.clear();
    }

    private class LineArraySorter implements Comparator<double[]> {

        @Override
        public int compare(double[] o1, double[] o2) {
            double f = o1[12] - o2[12];
            if (f == 0)
                return 0;
            else if (f < 0)
                return 1;
            else
                return -1;
        }

    }

    private class DPool extends Pool<double[]> {

        public DPool(int initialCapacity, int max) {
            super(initialCapacity, max);
        }

        @Override
        protected double[] newObject() {
            return new double[13];
        }

    }

}
