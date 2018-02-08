package gaia.cu9.ari.gaiaorbit.render.system;

import java.util.Comparator;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
import gaia.cu9.ari.gaiaorbit.scenegraph.ParticleGroup;
import gaia.cu9.ari.gaiaorbit.scenegraph.ParticleGroup.ParticleBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.comp.DistToCameraComparator;

public class ParticleGroupRenderSystem extends ImmediateRenderSystem implements IObserver {

    Vector3 aux1;
    int additionalOffset, pmOffset;

    Comparator<IRenderable> comp;

    public ParticleGroupRenderSystem(RenderGroup rg, int priority, float[] alphas, ShaderProgram shaderProgram) {
        super(rg, priority, alphas, shaderProgram);
        comp = new DistToCameraComparator<IRenderable>();
    }

    @Override
    protected void initShaderProgram() {
    }

    @Override
    protected void initVertices() {
        /** STARS **/
        meshes = new MeshData[1];
        curr = new MeshData();
        meshes[0] = curr;

        aux1 = new Vector3();

        maxVertices = 10000000;

        VertexAttribute[] attribs = buildVertexAttributes();
        curr.mesh = new Mesh(false, maxVertices, 0, attribs);

        curr.vertices = new float[maxVertices * (curr.mesh.getVertexAttributes().vertexSize / 4)];
        curr.vertexSize = curr.mesh.getVertexAttributes().vertexSize / 4;
        curr.colorOffset = curr.mesh.getVertexAttribute(Usage.ColorPacked) != null ? curr.mesh.getVertexAttribute(Usage.ColorPacked).offset / 4 : 0;
        pmOffset = curr.mesh.getVertexAttribute(Usage.Tangent) != null ? curr.mesh.getVertexAttribute(Usage.Tangent).offset / 4 : 0;
        additionalOffset = curr.mesh.getVertexAttribute(Usage.Generic) != null ? curr.mesh.getVertexAttribute(Usage.Generic).offset / 4 : 0;

    }

    @Override
    public void renderStud(Array<IRenderable> renderables, ICamera camera, double t) {
        renderables.sort(comp);
        if (renderables.size > 0) {
            for (IRenderable renderable : renderables) {
                ParticleGroup particleGroup = (ParticleGroup) renderable;

                /**
                 * GROUP RENDER
                 */
                if (!particleGroup.inGpu) {
                    particleGroup.offset = curr.vertexIdx;
                    for (ParticleBean pb : particleGroup.data()) {
                        double[] p = pb.data;
                        // COLOR
                        float[] c = particleGroup.cc;
                        curr.vertices[curr.vertexIdx + curr.colorOffset] = Color.toFloatBits(c[0], c[1], c[2], c[3]);

                        // SIZE
                        curr.vertices[curr.vertexIdx + additionalOffset] = particleGroup.size;

                        // cb.transform.getTranslationf(aux);
                        // POSITION
                        final int idx = curr.vertexIdx;
                        curr.vertices[idx] = (float) p[0];
                        curr.vertices[idx + 1] = (float) p[1];
                        curr.vertices[idx + 2] = (float) p[2];

                        curr.vertexIdx += curr.vertexSize;
                    }
                    particleGroup.count = particleGroup.size() * curr.vertexSize;

                    particleGroup.inGpu = true;

                }

                /**
                 * PARTICLE RENDERER
                 */
                if (Gdx.app.getType() == ApplicationType.Desktop) {
                    // Enable gl_PointCoord
                    Gdx.gl20.glEnable(34913);
                    // Enable point sizes
                    Gdx.gl20.glEnable(0x8642);
                }

                // Additive blending
                Gdx.gl20.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE);

                shaderProgram.begin();
                shaderProgram.setUniformMatrix("u_projModelView", camera.getCamera().combined);
                shaderProgram.setUniformf("u_camPos", camera.getCurrent().getPos().put(aux1));
                shaderProgram.setUniformf("u_alpha", particleGroup.opacity * alphas[particleGroup.ct.getFirstOrdinal()]);
                shaderProgram.setUniformf("u_ar", GlobalConf.program.STEREOSCOPIC_MODE && (GlobalConf.program.STEREO_PROFILE != StereoProfile.HD_3DTV && GlobalConf.program.STEREO_PROFILE != StereoProfile.ANAGLYPHIC) ? 0.5f : 1f);
                shaderProgram.setUniformf("u_profileDecay", particleGroup.profileDecay);
                shaderProgram.setUniformf("u_sizeFactor", rc.scaleFactor);
                curr.mesh.setVertices(curr.vertices, particleGroup.offset, particleGroup.count);
                curr.mesh.render(shaderProgram, ShapeType.Point.getGlType());
                shaderProgram.end();

                // Restore
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }

    }

    protected VertexAttribute[] buildVertexAttributes() {
        Array<VertexAttribute> attribs = new Array<VertexAttribute>();
        attribs.add(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
        attribs.add(new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE));
        attribs.add(new VertexAttribute(Usage.Generic, 1, "a_size"));

        VertexAttribute[] array = new VertexAttribute[attribs.size];
        for (int i = 0; i < attribs.size; i++)
            array[i] = attribs.get(i);
        return array;
    }

    @Override
    public void notify(Events event, Object... data) {
    }

}
