package gaia.cu9.ari.gaiaorbit.render.system;

import java.util.List;
import java.util.Random;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
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
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

public class GalaxyRenderSystem extends ImmediateRenderSystem implements IObserver {
    private boolean UPDATE_POINTS = true;

    Vector3 aux;
    int additionalOffset, pmOffset;

    private ShaderProgram quadProgram;
    private MeshData quad;
    private Texture[] nebulatextures;

    private ModelBatch modelBatch;

    public GalaxyRenderSystem(RenderGroup rg, int priority, float[] alphas, ModelBatch modelBatch) {
        super(rg, priority, alphas);
        this.modelBatch = modelBatch;
    }

    @Override
    protected void initShaderProgram() {

        // POINT (STARS) PROGRAM
        if (Gdx.app.getType() == ApplicationType.WebGL)
            pointProgram = new ShaderProgram(Gdx.files.internal("shader/point.galaxy.vertex.glsl"), Gdx.files.internal("shader/point.galaxy.fragment.wgl.glsl"));
        else
            pointProgram = new ShaderProgram(Gdx.files.internal("shader/point.galaxy.vertex.glsl"), Gdx.files.internal("shader/point.galaxy.fragment.glsl"));
        if (!pointProgram.isCompiled()) {
            Gdx.app.error(this.getClass().getName(), "Point shader compilation failed:\n" + pointProgram.getLog());
        }
        pointProgram.begin();
        pointProgram.setUniformf("u_pointAlphaMin", 0.1f);
        pointProgram.setUniformf("u_pointAlphaMax", 1.0f);
        pointProgram.end();

        // QUAD (NEBULA) PROGRAM
        quadProgram = new ShaderProgram(Gdx.files.internal("shader/nebula.vertex.glsl"), Gdx.files.internal("shader/nebula.fragment.glsl"));
        if (!quadProgram.isCompiled()) {
            Gdx.app.error(this.getClass().getName(), "Nebula shader compilation failed:\n" + quadProgram.getLog());
        }
        nebulatextures = new Texture[4];
        for (int i = 0; i < 4; i++) {
            Texture tex = new Texture(Gdx.files.internal(GlobalConf.TEXTURES_FOLDER + "nebula00" + (i + 1) + ".png"));
            tex.setFilter(TextureFilter.Linear, TextureFilter.Linear);
            nebulatextures[i] = tex;
        }
    }

    @Override
    protected void initVertices() {
        /** STARS **/
        meshes = new MeshData[1];
        curr = new MeshData();
        meshes[0] = curr;

        aux = new Vector3();

        maxVertices = 3000000;

        VertexAttribute[] attribs = buildVertexAttributes();
        curr.mesh = new Mesh(false, maxVertices, 0, attribs);

        curr.vertices = new float[maxVertices * (curr.mesh.getVertexAttributes().vertexSize / 4)];
        curr.vertexSize = curr.mesh.getVertexAttributes().vertexSize / 4;
        curr.colorOffset = curr.mesh.getVertexAttribute(Usage.ColorPacked) != null ? curr.mesh.getVertexAttribute(Usage.ColorPacked).offset / 4 : 0;
        pmOffset = curr.mesh.getVertexAttribute(Usage.Tangent) != null ? curr.mesh.getVertexAttribute(Usage.Tangent).offset / 4 : 0;
        additionalOffset = curr.mesh.getVertexAttribute(Usage.Generic) != null ? curr.mesh.getVertexAttribute(Usage.Generic).offset / 4 : 0;

        /** NEBULA **/

        // Max of 5000 nebula clouds
        int maxQuads = 5000;
        int maxQuadVertices = maxQuads * 4;
        int maxQuadIndices = maxQuads * 6;
        quad = new MeshData();

        quad.mesh = new Mesh(false, maxQuadVertices, maxQuadIndices, VertexAttribute.Position(), VertexAttribute.Normal(), VertexAttribute.TexCoords(0), new VertexAttribute(Usage.Generic, 2, "a_additional"));
        quad.vertices = new float[maxQuadVertices * (quad.mesh.getVertexAttributes().vertexSize / 4)];
        quad.vertexSize = quad.mesh.getVertexAttributes().vertexSize / 4;
        quad.indices = new short[maxQuadIndices];
    }

    @Override
    public void renderStud(List<IRenderable> renderables, ICamera camera) {
        int size = renderables.size();
        if (size > 0) {
            MilkyWayReal mw = (MilkyWayReal) renderables.get(0);
            Random rand = new Random(24601);

            /**
             * STAR RENDER
             */
            if (UPDATE_POINTS) {
                Vector3 center = mw.getPosition().toVector3();

                /** STARS **/
                curr.clear();
                float density = Gdx.graphics.getDensity() * 1.4f;
                for (float[] star : mw.pointData) {
                    // VERTEX
                    aux.set(star[0], star[1], star[2]);
                    float distanceCenter = aux.sub(center).len() / (mw.getRadius() * 2f);

                    float[] col = new float[] { (float) (rand.nextGaussian() * 0.02f) + 0.93f, (float) (rand.nextGaussian() * 0.02) + 0.8f, (float) (rand.nextGaussian() * 0.02) + 0.97f, rand.nextFloat() * 0.5f + 0.4f };

                    if (distanceCenter < 1f) {
                        float add = MathUtilsd.clamp(1f - distanceCenter, 0f, 1f) * 0.5f;
                        col[0] = col[0] + add;
                        col[1] = col[1] + add;
                        col[2] = col[2] + add;
                    }

                    col[0] = MathUtilsd.clamp(col[0], 0f, 1f);
                    col[1] = MathUtilsd.clamp(col[1], 0f, 1f);
                    col[2] = MathUtilsd.clamp(col[2], 0f, 1f);

                    // COLOR
                    curr.vertices[curr.vertexIdx + curr.colorOffset] = Color.toFloatBits(col[0], col[1], col[2], col[3]);

                    // SIZE
                    float starSize = 0;
                    if (star.length > 3) {
                        starSize = (star[3] * 10f + 1f) /** (Constants.webgl ? 0.08f : 1f)*/
                        ;
                    } else {
                        starSize = (float) Math.abs(rand.nextGaussian()) * 8f + 1.0f;
                    }
                    curr.vertices[curr.vertexIdx + additionalOffset] = starSize * density;
                    curr.vertices[curr.vertexIdx + additionalOffset + 1] = 0.7f;

                    //cb.transform.getTranslationf(aux);
                    // POSITION
                    aux.set(star[0], star[1], star[2]);
                    final int idx = curr.vertexIdx;
                    curr.vertices[idx] = aux.x;
                    curr.vertices[idx + 1] = aux.y;
                    curr.vertices[idx + 2] = aux.z;

                    curr.vertexIdx += curr.vertexSize;

                }

                /** QUADS **/
                quad.clear();
                Vector3 rotaxis = new Vector3();
                Vector3 transl = new Vector3();
                Vector3 bl = new Vector3();
                Vector3 br = new Vector3();
                Vector3 tl = new Vector3();
                Vector3 tr = new Vector3();
                Vector3 normal = new Vector3();
                Vector3 quadpoint = new Vector3();
                for (float[] qp : mw.nebulaData) {
                    // 5 quads per nebula
                    for (int i = 0; i < 5; i++) {
                        quadpoint.set(qp[0], qp[1], qp[2]);
                        float quadpointdist = quadpoint.len();
                        float texnum, alphamultiplier, quadsize;
                        if (quadpointdist < mw.size / 2f) {
                            texnum = (float) Math.floor(rand.nextFloat() + 0.3f);
                        } else {
                            texnum = rand.nextInt(4);
                        }
                        quadsize = qp.length > 3 ? (qp[3] + 1.0f) * .46e11f : (float) (rand.nextFloat() + 1.0f) * 2e11f;
                        alphamultiplier = MathUtilsd.lint(quadpointdist, 0, mw.size * 3, 6.0f, 1.0f);

                        rotaxis.set(rand.nextFloat(), rand.nextFloat(), rand.nextFloat());
                        float rotangle = rand.nextFloat() * 360f;
                        transl.set(rand.nextFloat() * 1e10f, rand.nextFloat() * 1e10f, rand.nextFloat() * 1e10f).add(quadpoint);

                        bl.set(-1, -1, 0).scl(quadsize).rotate(rotaxis, rotangle).add(transl);
                        br.set(1, -1, 0).scl(quadsize).rotate(rotaxis, rotangle).add(transl);
                        tr.set(1, 1, 0).scl(quadsize).rotate(rotaxis, rotangle).add(transl);
                        tl.set(-1, 1, 0).scl(quadsize).rotate(rotaxis, rotangle).add(transl);
                        normal.set(0, 0, 1).rotate(rotaxis, rotangle);

                        // Bottom left
                        quad.vertices[quad.vertexIdx] = bl.x;
                        quad.vertices[quad.vertexIdx + 1] = bl.y;
                        quad.vertices[quad.vertexIdx + 2] = bl.z;
                        quad.vertices[quad.vertexIdx + 3] = normal.x;
                        quad.vertices[quad.vertexIdx + 4] = normal.y;
                        quad.vertices[quad.vertexIdx + 5] = normal.z;
                        quad.vertices[quad.vertexIdx + 6] = 0;
                        quad.vertices[quad.vertexIdx + 7] = 0;
                        quad.vertices[quad.vertexIdx + 8] = texnum; // texture number
                        quad.vertices[quad.vertexIdx + 9] = alphamultiplier; // alpha multiplier 
                        quad.vertexIdx += quad.vertexSize;

                        // Bottom right
                        quad.vertices[quad.vertexIdx] = br.x;
                        quad.vertices[quad.vertexIdx + 1] = br.y;
                        quad.vertices[quad.vertexIdx + 2] = br.z;
                        quad.vertices[quad.vertexIdx + 3] = normal.x;
                        quad.vertices[quad.vertexIdx + 4] = normal.y;
                        quad.vertices[quad.vertexIdx + 5] = normal.z;
                        quad.vertices[quad.vertexIdx + 6] = 1;
                        quad.vertices[quad.vertexIdx + 7] = 0;
                        quad.vertices[quad.vertexIdx + 8] = texnum; // texture number
                        quad.vertices[quad.vertexIdx + 9] = alphamultiplier; // alpha multiplier 
                        quad.vertexIdx += quad.vertexSize;

                        // Top right
                        quad.vertices[quad.vertexIdx] = tr.x;
                        quad.vertices[quad.vertexIdx + 1] = tr.y;
                        quad.vertices[quad.vertexIdx + 2] = tr.z;
                        quad.vertices[quad.vertexIdx + 3] = normal.x;
                        quad.vertices[quad.vertexIdx + 4] = normal.y;
                        quad.vertices[quad.vertexIdx + 5] = normal.z;
                        quad.vertices[quad.vertexIdx + 6] = 1;
                        quad.vertices[quad.vertexIdx + 7] = 1;
                        quad.vertices[quad.vertexIdx + 8] = texnum; // texture number
                        quad.vertices[quad.vertexIdx + 9] = alphamultiplier; // alpha multiplier 
                        quad.vertexIdx += quad.vertexSize;

                        // Top left
                        quad.vertices[quad.vertexIdx] = tl.x;
                        quad.vertices[quad.vertexIdx + 1] = tl.y;
                        quad.vertices[quad.vertexIdx + 2] = tl.z;
                        quad.vertices[quad.vertexIdx + 3] = normal.x;
                        quad.vertices[quad.vertexIdx + 4] = normal.y;
                        quad.vertices[quad.vertexIdx + 5] = normal.z;
                        quad.vertices[quad.vertexIdx + 6] = 0;
                        quad.vertices[quad.vertexIdx + 7] = 1;
                        quad.vertices[quad.vertexIdx + 8] = texnum; // texture number
                        quad.vertices[quad.vertexIdx + 9] = alphamultiplier; // alpha multiplier 
                        quad.vertexIdx += quad.vertexSize;

                        // Indices
                        quad.indices[quad.indexIdx] = quad.indexVert;
                        quad.indices[quad.indexIdx + 1] = (short) (quad.indexVert + 1);
                        quad.indices[quad.indexIdx + 2] = (short) (quad.indexVert + 2);
                        quad.indices[quad.indexIdx + 3] = (short) (quad.indexVert + 2);
                        quad.indices[quad.indexIdx + 4] = (short) (quad.indexVert + 3);
                        quad.indices[quad.indexIdx + 5] = (short) (quad.indexVert + 0);
                        quad.indexIdx += 6;
                        quad.indexVert += 4;

                    }

                }

                // Put flag down
                UPDATE_POINTS = false;
            }

            /**
             * NEBULA RENDERER
             */

            quadProgram.begin();

            // General uniforms
            quadProgram.setUniformMatrix("u_projModelView", camera.getCamera().combined);
            quadProgram.setUniformf("u_camPos", camera.getCurrent().getPos().setVector3(aux));
            quadProgram.setUniformf("u_alpha", 0.05f * mw.opacity * alphas[mw.ct.ordinal()]);

            for (int i = 0; i < 4; i++) {
                nebulatextures[i].bind(i);
                quadProgram.setUniformi("u_nebulaTexture" + i, i);
            }

            quad.mesh.setVertices(quad.vertices, 0, quad.vertexIdx);
            quad.mesh.setIndices(quad.indices, 0, quad.indexIdx);
            quad.mesh.render(quadProgram, GL20.GL_TRIANGLES, 0, quad.indexIdx);

            quadProgram.end();

            /**
             * STAR RENDERER
             */
            if (Gdx.app.getType() == ApplicationType.Desktop) {
                // Enable gl_PointCoord
                Gdx.gl20.glEnable(34913);
                // Enable point sizes
                Gdx.gl20.glEnable(0x8642);
            }
            pointProgram.begin();
            pointProgram.setUniformMatrix("u_projModelView", camera.getCamera().combined);
            pointProgram.setUniformf("u_camPos", camera.getCurrent().getPos().setVector3(aux));
            pointProgram.setUniformf("u_fovFactor", camera.getFovFactor());
            pointProgram.setUniformf("u_alpha", mw.opacity * alphas[mw.ct.ordinal()]);
            curr.mesh.setVertices(curr.vertices, 0, curr.vertexIdx);
            curr.mesh.render(pointProgram, ShapeType.Point.getGlType());
            pointProgram.end();

            /**
             * IMAGE RENDERER
             */
            mw.mc.setTransparency(mw.opacity * alphas[mw.ct.ordinal()] * 0.4f);
            modelBatch.begin(camera.getCamera());
            modelBatch.render(mw.mc.instance, mw.mc.env);
            modelBatch.end();

        }

    }

    protected VertexAttribute[] buildVertexAttributes() {
        Array<VertexAttribute> attribs = new Array<VertexAttribute>();
        attribs.add(new VertexAttribute(Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
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
