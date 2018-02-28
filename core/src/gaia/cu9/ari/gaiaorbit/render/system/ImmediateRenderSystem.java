package gaia.cu9.ari.gaiaorbit.render.system;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;

public abstract class ImmediateRenderSystem extends AbstractRenderSystem {
    protected static final int shortLimit = (int) Math.pow(2, 2 * 8);
    protected ShaderProgram[] programs;

    protected int meshIdx;
    protected MeshData[] meshes;
    protected MeshData curr;

    protected class MeshData {

	protected Mesh mesh;

	protected int colorOffset;

	protected int vertexIdx;
	protected int vertexSize;
	protected float[] vertices;

	protected int indexIdx;
	protected short indexVert;
	protected short[] indices;
	protected int numVertices;

	public void clear() {
	    vertexIdx = 0;
	    indexIdx = 0;
	    indexVert = 0;
	    numVertices = 0;
	}
    }

    protected int maxVertices;

    protected ImmediateRenderSystem(RenderGroup rg, float[] alphas, ShaderProgram[] shaders) {
        super(rg, alphas);
        /**
         * 0 - normal
         * 1 - relativistic 
         */
        this.programs = shaders;
        initShaderProgram();
        initVertices();
        meshIdx = 0;
    }

    protected abstract void initShaderProgram();

    protected abstract void initVertices();

    protected ShaderProgram getShaderProgram() {
        try {
            if (GlobalConf.runtime.RELATIVISTIC_EFFECTS)
                return programs[1];
            else if (GlobalConf.runtime.GRAVITATIONAL_WAVES)
                return programs[2];
        } catch (Exception e) {
        }
        return programs[0];
    }

    public void color(Color color) {
	curr.vertices[curr.vertexIdx + curr.colorOffset] = color.toFloatBits();
    }

    public void color(float r, float g, float b, float a) {
	curr.vertices[curr.vertexIdx + curr.colorOffset] = Color.toFloatBits(r, g, b, a);
    }

    public void color(double r, double g, double b, double a) {
	curr.vertices[curr.vertexIdx + curr.colorOffset] = Color.toFloatBits((float) r, (float) g, (float) b,
		(float) a);
    }

    public void color(float colorBits) {
	curr.vertices[curr.vertexIdx + curr.colorOffset] = colorBits;
    }

    public void vertex(float x, float y, float z) {
	curr.vertices[curr.vertexIdx] = x;
	curr.vertices[curr.vertexIdx + 1] = y;
	curr.vertices[curr.vertexIdx + 2] = z;

	curr.vertexIdx += curr.vertexSize;
	curr.numVertices++;
    }

}
