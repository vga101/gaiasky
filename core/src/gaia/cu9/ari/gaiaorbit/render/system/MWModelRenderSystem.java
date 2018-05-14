package gaia.cu9.ari.gaiaorbit.render.system;

import java.util.Random;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.FloatFrameBuffer;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FloatFrameBufferBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.MilkyWay;
import gaia.cu9.ari.gaiaorbit.scenegraph.ParticleGroup.ParticleBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

public class MWModelRenderSystem extends ImmediateRenderSystem implements IObserver {
    private boolean UPDATE_POINTS = true;

    Vector3 aux1;
    int additionalOffset, pmOffset;

    ShaderProgram preShader;

    private MeshData particlesMesh, postMesh;

    Texture accumTex, revealTex;
    FrameBuffer accumFb, revealFb;

    public MWModelRenderSystem(RenderGroup rg, float[] alphas, ShaderProgram[] starShaders) {
        super(rg, alphas, starShaders);

        accumFb = new FloatFrameBuffer(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        accumTex = accumFb.getColorBufferTexture();

        FloatFrameBufferBuilder ffbb = new FloatFrameBufferBuilder(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ffbb.addFloatAttachment(GL30.GL_R32F, GL30.GL_DEPTH_COMPONENT32F, GL30.GL_FLOAT, false);

        revealFb = ffbb.build();
        revealTex = revealFb.getColorBufferTexture();

        preShader = new ShaderProgram("shader/galaxy.post.vertex.glsl", "shader/galaxy.post.fragment.glsl");

    }

    @Override
    protected void initShaderProgram() {
        for (ShaderProgram shaderProgram : programs) {
            shaderProgram.begin();
            shaderProgram.setUniformf("u_pointAlphaMin", 0.1f);
            shaderProgram.setUniformf("u_pointAlphaMax", 1.0f);
            shaderProgram.end();
        }
    }

    @Override
    protected void initVertices() {
        /** Particles **/
        meshes = new MeshData[1];
        particlesMesh = new MeshData();
        meshes[0] = particlesMesh;

        aux1 = new Vector3();

        maxVertices = 5000000;

        VertexAttribute[] attribs = buildVertexAttributes();
        particlesMesh.mesh = new Mesh(false, maxVertices, 0, attribs);

        particlesMesh.vertices = new float[maxVertices * (particlesMesh.mesh.getVertexAttributes().vertexSize / 4)];
        particlesMesh.vertexSize = particlesMesh.mesh.getVertexAttributes().vertexSize / 4;
        particlesMesh.colorOffset = particlesMesh.mesh.getVertexAttribute(Usage.ColorPacked) != null ? particlesMesh.mesh.getVertexAttribute(Usage.ColorPacked).offset / 4 : 0;
        pmOffset = particlesMesh.mesh.getVertexAttribute(Usage.Tangent) != null ? particlesMesh.mesh.getVertexAttribute(Usage.Tangent).offset / 4 : 0;
        additionalOffset = particlesMesh.mesh.getVertexAttribute(Usage.Generic) != null ? particlesMesh.mesh.getVertexAttribute(Usage.Generic).offset / 4 : 0;

        /** Post mesh **/
        postMesh = new MeshData();

        attribs = buildVertexAttributesPost();
        postMesh.mesh = new Mesh(true, 4, 0, attribs);
        postMesh.vertices = new float[] { -1, -1, -1, 1, 1, -1, 1, 1 };
        postMesh.vertexSize = postMesh.mesh.getVertexAttributes().vertexSize / 4;

    }

    private void streamToGpu(MilkyWay mw) {
        if (UPDATE_POINTS) {
            Random rand = new Random(24601);
            Vector3 center = mw.getPosition().toVector3();

            /** PARTICLES **/
            particlesMesh.clear();
            float density = GlobalConf.SCALE_FACTOR;
            for (ParticleBean star : mw.starData) {
                // VERTEX
                aux1.set((float) star.data[0], (float) star.data[1], (float) star.data[2]);
                double distanceCenter = aux1.sub(center).len() / (mw.getRadius() * 2f);

                float[] col = new float[] { (float) (rand.nextGaussian() * 0.02f) + 0.93f, (float) (rand.nextGaussian() * 0.02) + 0.8f, (float) (rand.nextGaussian() * 0.02) + 0.97f, rand.nextFloat() * 0.5f + 0.6f };

                if (distanceCenter < 1f) {
                    float add = (float) MathUtilsd.clamp(1f - distanceCenter, 0f, 1f) * 0.5f;
                    col[0] = col[0] + add;
                    col[1] = col[1] + add;
                    col[2] = col[2] + add;
                }

                col[0] = MathUtilsd.clamp(col[0], 0f, 1f);
                col[1] = MathUtilsd.clamp(col[1], 0f, 1f);
                col[2] = MathUtilsd.clamp(col[2], 0f, 1f);

                // COLOR
                particlesMesh.vertices[particlesMesh.vertexIdx + particlesMesh.colorOffset] = Color.toFloatBits(col[0], col[1], col[2], col[3]);

                // SIZE
                double starSize = 0;
                if (star.data.length > 3) {
                    starSize = (star.data[3] * 5 + 1);
                } else {
                    starSize = (float) Math.abs(rand.nextGaussian()) * 8f + 1.0f;
                }
                particlesMesh.vertices[particlesMesh.vertexIdx + additionalOffset] = (float) (starSize * density * 2);
                particlesMesh.vertices[particlesMesh.vertexIdx + additionalOffset + 1] = 0.7f;

                // cb.transform.getTranslationf(aux);
                // POSITION
                aux1.set((float) star.data[0], (float) star.data[1], (float) star.data[2]);
                final int idx = particlesMesh.vertexIdx;
                particlesMesh.vertices[idx] = aux1.x;
                particlesMesh.vertices[idx + 1] = aux1.y;
                particlesMesh.vertices[idx + 2] = aux1.z;

                particlesMesh.vertexIdx += particlesMesh.vertexSize;

            }
            for (ParticleBean dust : mw.dustData) {
                // VERTEX
                aux1.set((float) dust.data[0], (float) dust.data[1], (float) dust.data[2]);

                float[] col = new float[] { (float) (rand.nextGaussian() * 0.02f) + 0.03f, (float) (rand.nextGaussian() * 0.02) + 0.03f, (float) (rand.nextGaussian() * 0.02) + 0.05f, rand.nextFloat() * 0.5f + 0.4f };

                col[0] = MathUtilsd.clamp(col[0], 0f, 1f);
                col[1] = MathUtilsd.clamp(col[1], 0f, 1f);
                col[2] = MathUtilsd.clamp(col[2], 0f, 1f);

                // COLOR
                particlesMesh.vertices[particlesMesh.vertexIdx + particlesMesh.colorOffset] = Color.toFloatBits(col[0], col[1], col[2], col[3]);

                // SIZE
                double starSize = 0;
                if (dust.data.length > 3) {
                    starSize = (dust.data[3] * 3 + 1) /** (Constants.webgl ? 0.08f : 1f) */
                    ;
                } else {
                    starSize = (float) Math.abs(rand.nextGaussian()) * 8f + 1.0f;
                }
                particlesMesh.vertices[particlesMesh.vertexIdx + additionalOffset] = (float) (starSize * density * 2);
                particlesMesh.vertices[particlesMesh.vertexIdx + additionalOffset + 1] = 0.7f;

                // cb.transform.getTranslationf(aux);
                // POSITION
                aux1.set((float) dust.data[0], (float) dust.data[1], (float) dust.data[2]);
                final int idx = particlesMesh.vertexIdx;
                particlesMesh.vertices[idx] = aux1.x;
                particlesMesh.vertices[idx + 1] = aux1.y;
                particlesMesh.vertices[idx + 2] = aux1.z;

                particlesMesh.vertexIdx += particlesMesh.vertexSize;

                particlesMesh.mesh.setVertices(particlesMesh.vertices, 0, particlesMesh.vertexIdx);

            }

            // Put flag down
            UPDATE_POINTS = false;
        }

    }

    public void renderPrePasses(MilkyWay mw, ICamera camera) {
        // Send data to GPU
        streamToGpu(mw);

        /**
         * PARTICLES RENDERER
         */
        float alpha = getAlpha(mw);
        if (alpha > 0) {
            if (Gdx.app.getType() == ApplicationType.Desktop) {
                // Enable gl_PointCoord
                Gdx.gl20.glEnable(34913);
                // Enable point sizes
                Gdx.gl20.glEnable(0x8642);
            }
            Gdx.gl30.glEnable(GL30.GL_DEPTH_TEST);
            Gdx.gl30.glDepthMask(false);
            Gdx.gl30.glEnable(GL30.GL_BLEND);

            /**
             * PASS 0
             */
            Gdx.gl20.glBlendFuncSeparate(GL30.GL_ONE, GL30.GL_ONE, GL30.GL_ONE, GL30.GL_ONE);


            ShaderProgram shaderProgram = preShader;

            accumFb.begin();
            Gdx.gl.glClearColor(0, 0, 0, 0);

            shaderProgram.begin();
            shaderProgram.setUniformf("u_pass", 0.0f);

            shaderProgram.setUniformMatrix("u_projModelView", camera.getCamera().combined);
            shaderProgram.setUniformMatrix("u_view", camera.getCamera().view);
            shaderProgram.setUniformMatrix("u_projection", camera.getCamera().projection);

            shaderProgram.setUniformf("u_camPos", camera.getCurrent().getPos().put(aux1));
            shaderProgram.setUniformf("u_alpha", mw.opacity * alpha * 0.2f);
            shaderProgram.setUniformf("u_ar", GlobalConf.program.STEREOSCOPIC_MODE && (GlobalConf.program.STEREO_PROFILE != StereoProfile.HD_3DTV && GlobalConf.program.STEREO_PROFILE != StereoProfile.ANAGLYPHIC) ? 0.5f : 1f);

            // Relativistic effects
            addEffectsUniforms(shaderProgram, camera);

            particlesMesh.mesh.render(shaderProgram, ShapeType.Point.getGlType());
            shaderProgram.end();
            accumFb.end();

            /**
             * PASS 1
             */
            Gdx.gl20.glBlendFuncSeparate(GL30.GL_ZERO, GL30.GL_ONE_MINUS_SRC_COLOR, GL30.GL_ZERO, GL30.GL_ONE_MINUS_SRC_COLOR);

            revealFb.begin();
            Gdx.gl.glClearColor(1, 1, 1, 1);

            shaderProgram.begin();
            shaderProgram.setUniformf("u_pass", 1.0f);

            particlesMesh.mesh.render(shaderProgram, ShapeType.Point.getGlType());
            shaderProgram.end();
            revealFb.end();

            // Restore
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        }
    }

    @Override
    public void renderStud(Array<IRenderable> renderables, ICamera camera, double t) {
        if (renderables.size > 0) {
            MilkyWay mw = (MilkyWay) renderables.get(0);
            // Send data to GPU

            /**
             * PARTICLES RENDERER
             */
            float alpha = getAlpha(mw);
            if (alpha > 0) {

                ShaderProgram shaderProgram = preShader;

                shaderProgram.begin();
                int t0 = GL30.GL_TEXTURE23 - GL30.GL_TEXTURE0;
                int t1 = GL30.GL_TEXTURE24 - GL30.GL_TEXTURE0;
                accumTex.bind(t0);
                shaderProgram.setUniformi("tex_accumulation", t0);
                accumTex.bind(t1);
                shaderProgram.setUniformi("tex_revealage", t1);

                postMesh.mesh.render(shaderProgram, GL30.GL_TRIANGLES);
                shaderProgram.end();

                // Restore
                Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
            }
        }

    }

    public void renderStudOld(Array<IRenderable> renderables, ICamera camera, double t) {
        if (renderables.size > 0) {
            MilkyWay mw = (MilkyWay) renderables.get(0);
            // Send data to GPU
            streamToGpu(mw);

            /**
             * PARTICLES RENDERER
             */
            float alpha = getAlpha(mw);
            if (alpha > 0) {
                /**
                 * PARTICLES RENDERER
                 */
                if (Gdx.app.getType() == ApplicationType.Desktop) {
                    // Enable gl_PointCoord
                    Gdx.gl20.glEnable(34913);
                    // Enable point sizes
                    Gdx.gl20.glEnable(0x8642);
                }
                // Multiplicative blending (RGBs * RBGd)
                Gdx.gl20.glBlendFunc(GL20.GL_ZERO, GL20.GL_SRC_COLOR);

                // Additive blending (RGBs + RGBd)
                Gdx.gl20.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE);

                // Transparency blending - Order dependent transparency
                //Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                ShaderProgram shaderProgram = getShaderProgram();

                shaderProgram.begin();
                shaderProgram.setUniformMatrix("u_projModelView", camera.getCamera().combined);
                shaderProgram.setUniformMatrix("u_view", camera.getCamera().view);
                shaderProgram.setUniformMatrix("u_projection", camera.getCamera().projection);

                shaderProgram.setUniformf("u_camPos", camera.getCurrent().getPos().put(aux1));
                shaderProgram.setUniformf("u_alpha", mw.opacity * alpha * 0.2f);
                shaderProgram.setUniformf("u_ar", GlobalConf.program.STEREOSCOPIC_MODE && (GlobalConf.program.STEREO_PROFILE != StereoProfile.HD_3DTV && GlobalConf.program.STEREO_PROFILE != StereoProfile.ANAGLYPHIC) ? 0.5f : 1f);

                // Relativistic effects
                addEffectsUniforms(shaderProgram, camera);

                particlesMesh.mesh.setVertices(particlesMesh.vertices, 0, particlesMesh.vertexIdx);
                particlesMesh.mesh.render(shaderProgram, ShapeType.Point.getGlType());
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
        attribs.add(new VertexAttribute(Usage.Generic, 4, "a_additional"));

        VertexAttribute[] array = new VertexAttribute[attribs.size];
        for (int i = 0; i < attribs.size; i++)
            array[i] = attribs.get(i);
        return array;
    }

    protected VertexAttribute[] buildVertexAttributesPost() {
        Array<VertexAttribute> attribs = new Array<VertexAttribute>();
        attribs.add(new VertexAttribute(Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE));

        VertexAttribute[] array = new VertexAttribute[attribs.size];
        for (int i = 0; i < attribs.size; i++)
            array[i] = attribs.get(i);
        return array;
    }

    @Override
    public void notify(Events event, Object... data) {
    }

}
