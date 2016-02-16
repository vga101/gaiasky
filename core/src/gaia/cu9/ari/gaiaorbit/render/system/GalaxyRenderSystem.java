package gaia.cu9.ari.gaiaorbit.render.system;

import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.MilkyWayReal;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;

public class GalaxyRenderSystem extends ImmediateRenderSystem implements IObserver {
    private boolean UPDATE_POINTS = true;

    Vector3 aux;
    int additionalOffset, pmOffset;

    public GalaxyRenderSystem(RenderGroup rg, int priority, float[] alphas) {
        super(rg, priority, alphas);
    }

    @Override
    protected void initShaderProgram() {
        // Initialise renderer
        shaderProgram = new ShaderProgram(Gdx.files.internal("shader/point.galaxy.vertex.glsl"), Gdx.files.internal("shader/point.fragment.glsl"));
        if (!shaderProgram.isCompiled()) {
            Gdx.app.error(this.getClass().getName(), "Point shader compilation failed:\n" + shaderProgram.getLog());
        }
        shaderProgram.begin();
        shaderProgram.setUniformf("u_pointAlphaMin", 0.1f);
        shaderProgram.setUniformf("u_pointAlphaMax", 1.0f);
        shaderProgram.end();

    }

    @Override
    protected void initVertices() {
        meshes = new MeshData[1];
        curr = new MeshData();
        meshes[0] = curr;

        aux = new Vector3();

        /** Init renderer **/
        maxVertices = 3000000;

        VertexAttribute[] attribs = buildVertexAttributes();
        curr.mesh = new Mesh(false, maxVertices, 0, attribs);

        curr.vertices = new float[maxVertices * (curr.mesh.getVertexAttributes().vertexSize / 4)];
        curr.vertexSize = curr.mesh.getVertexAttributes().vertexSize / 4;
        curr.colorOffset = curr.mesh.getVertexAttribute(Usage.ColorPacked) != null ? curr.mesh.getVertexAttribute(Usage.ColorPacked).offset / 4 : 0;
        pmOffset = curr.mesh.getVertexAttribute(Usage.Tangent) != null ? curr.mesh.getVertexAttribute(Usage.Tangent).offset / 4 : 0;
        additionalOffset = curr.mesh.getVertexAttribute(Usage.Generic) != null ? curr.mesh.getVertexAttribute(Usage.Generic).offset / 4 : 0;
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
        int size = renderables.size();
        if (size > 0) {
            MilkyWayReal mw = (MilkyWayReal) renderables.get(0);
            if (UPDATE_POINTS) {
                // Reset variables
                curr.clear();

                Random rand = new Random(24601);
                float[] col = new float[] { 1.0f, 1.0f, 1.0f, 1.0f };

                for (Vector3 star : mw.pointData) {

                    // COLOR
                    float colcorr = (rand.nextFloat() * 0.7f + 0.05f);
                    curr.vertices[curr.vertexIdx + curr.colorOffset] = Color.toFloatBits(col[0] * colcorr, col[1] * colcorr, col[2] * colcorr, col[3]);

                    // SIZE
                    curr.vertices[curr.vertexIdx + additionalOffset] = rand.nextFloat() * 2f;
                    curr.vertices[curr.vertexIdx + additionalOffset + 1] = 0.7f;

                    // VERTEX
                    aux.set(star.x, star.y, star.z);
                    //cb.transform.getTranslationf(aux);
                    final int idx = curr.vertexIdx;
                    curr.vertices[idx] = aux.x;
                    curr.vertices[idx + 1] = aux.y;
                    curr.vertices[idx + 2] = aux.z;

                    // PROPER MOTION
                    curr.vertices[curr.vertexIdx + pmOffset] = 0f;
                    curr.vertices[curr.vertexIdx + pmOffset + 1] = 0f;
                    curr.vertices[curr.vertexIdx + pmOffset + 2] = 0f;

                    curr.vertexIdx += curr.vertexSize;

                }
                // Put flag down
                UPDATE_POINTS = false;
            }

            Gdx.graphics.getGL20().glEnable(0x8642);
            shaderProgram.begin();
            shaderProgram.setUniformMatrix("u_projModelView", camera.getCamera().combined);
            shaderProgram.setUniformf("u_camPos", camera.getCurrent().getPos().setVector3(aux));
            shaderProgram.setUniformf("u_fovFactor", camera.getFovFactor());
            shaderProgram.setUniformf("u_alpha", mw.opacity * alphas[mw.ct.ordinal()]);
            curr.mesh.setVertices(curr.vertices, 0, curr.vertexIdx);
            curr.mesh.render(shaderProgram, ShapeType.Point.getGlType());
            shaderProgram.end();
        }

    }

    protected VertexAttribute[] buildVertexAttributes() {
        Array<VertexAttribute> attribs = new Array<VertexAttribute>();
        attribs.add(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
        attribs.add(new VertexAttribute(Usage.Tangent, 3, "a_pm"));
        attribs.add(new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE));
        attribs.add(new VertexAttribute(Usage.Generic, 4, "a_additional"));

        VertexAttribute[] array = new VertexAttribute[attribs.size];
        for (int i = 0; i < attribs.size; i++)
            array[i] = attribs.get(i);
        return array;
    }

    @Override
    public void notify(Events event, Object... data) {
    }
}
