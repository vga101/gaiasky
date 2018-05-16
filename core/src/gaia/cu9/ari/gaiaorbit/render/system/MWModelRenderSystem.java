package gaia.cu9.ari.gaiaorbit.render.system;

import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.GL30;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.FloatFrameBuffer;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.GLFrameBuffer.FloatFrameBufferBuilder;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.bitfire.postprocessing.utils.FullscreenQuad;

import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.MilkyWay;
import gaia.cu9.ari.gaiaorbit.scenegraph.ParticleGroup.ParticleBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.math.MathUtilsd;

public class MWModelRenderSystem extends ImmediateRenderSystem implements IObserver {
    private boolean UPDATE_POINTS = true;

    private Vector3 aux1;
    private int additionalOffset;

    private ShaderProgram postShader;
    private MeshData particlesMesh;
    private FullscreenQuad postMesh;
    private Random rand = new Random(24601);

    public FrameBuffer accumFb, revealFb;

    public MWModelRenderSystem(RenderGroup rg, float[] alphas, ShaderProgram[] starShaders) {
        super(rg, alphas, starShaders);

        accumFb = new FloatFrameBuffer(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);

        FloatFrameBufferBuilder ffbb = new FloatFrameBufferBuilder(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        ffbb.addFloatAttachment(GL30.GL_R32F, GL30.GL_DEPTH_COMPONENT32F, GL30.GL_FLOAT, false);
        //ffbb.addFloatAttachment(GL30.GL_R32F, GL30.GL_DEPTH_COMPONENT32F, GL30.GL_FLOAT, false);
        //ffbb.addDepthRenderBuffer(GL30.GL_DEPTH_COMPONENT32F);

        revealFb = new FloatFrameBuffer(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(), true);
        //revealFb = ffbb.build();

        postShader = new ShaderProgram(Gdx.files.internal("shader/galaxy.post.vertex.glsl"), Gdx.files.internal("shader/galaxy.post.fragment.glsl"));
        if (!postShader.isCompiled()) {
            Logger.error("Galaxy post shader compilation failed:");
            Logger.error(postShader.getLog());
        }

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
        particlesMesh = new MeshData();
        initMesh(particlesMesh);

        /** Post mesh **/
        postMesh = new FullscreenQuad();
    }

    private void initMesh(MeshData md) {
        aux1 = new Vector3();

        maxVertices = 5000000;

        VertexAttribute[] attribs = buildVertexAttributes();
        md.mesh = new Mesh(false, maxVertices, 0, attribs);

        md.vertices = new float[maxVertices * (md.mesh.getVertexAttributes().vertexSize / 4)];
        md.vertexSize = md.mesh.getVertexAttributes().vertexSize / 4;
        md.colorOffset = md.mesh.getVertexAttribute(Usage.ColorPacked) != null ? md.mesh.getVertexAttribute(Usage.ColorPacked).offset / 4 : 0;
        additionalOffset = md.mesh.getVertexAttribute(Usage.Generic) != null ? md.mesh.getVertexAttribute(Usage.Generic).offset / 4 : 0;
    }

    private void streamToGpu(MilkyWay mw) {
        Vector3 center = mw.getPosition().toVector3();

        /** PARTICLES **/
        particlesMesh.clear();
        float density = GlobalConf.SCALE_FACTOR;
        for (ParticleBean star : mw.starData) {
            // VERTEX
            aux1.set((float) star.data[0], (float) star.data[1], (float) star.data[2]);
            double distanceCenter = aux1.sub(center).len() / (mw.getRadius() * 2f);

            float[] col = new float[] { (float) (rand.nextGaussian() * 0.02f) + 0.93f, (float) (rand.nextGaussian() * 0.02) + 0.8f, (float) (rand.nextGaussian() * 0.02) + 0.97f, 0.9f };

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
                starSize = (star.data[3] * 1 + 1);
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

            float[] col = new float[] { (float) (rand.nextGaussian() * 0.02f) + 0.03f, (float) (rand.nextGaussian() * 0.02) + 0.03f, (float) (rand.nextGaussian() * 0.02) + 0.05f, 0.8f };

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
        }
        particlesMesh.mesh.setVertices(particlesMesh.vertices, 0, particlesMesh.vertexIdx);
    }

    public void renderPrePasses(MilkyWay mw, ICamera camera) {
        if (UPDATE_POINTS) {
            streamToGpu(mw);
            // Put flag down
            UPDATE_POINTS = false;
        }

        /**
         * PARTICLES RENDERER
         */
        float alpha = getAlpha(mw);
        if (alpha > 0) {
            // Enable gl_PointCoord
            Gdx.gl20.glEnable(34913);
            // Enable point sizes
            Gdx.gl20.glEnable(0x8642);
            Gdx.gl30.glEnable(GL30.GL_DEPTH_TEST);
            Gdx.gl30.glDepthMask(false);
            Gdx.gl30.glEnable(GL30.GL_BLEND);

            // Set camera (hack)
            camera.getCamera().near = .3e11f;
            camera.getCamera().far = 1e22f;
            camera.getCamera().update(false);
            /**
             * PASS 0
             */
            Gdx.gl20.glBlendFunc(GL30.GL_ONE, GL30.GL_ONE);

            ShaderProgram shaderProgram = getShaderProgram();

            accumFb.begin();
            Gdx.gl.glClearColor(0, 0, 0, 0);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

            shaderProgram.begin();
            shaderProgram.setUniformf("u_pass", 0.0f);

            shaderProgram.setUniformMatrix("u_projModelView", camera.getCamera().combined);
            shaderProgram.setUniformMatrix("u_view", camera.getCamera().view);
            shaderProgram.setUniformf("u_camPos", camera.getCurrent().getPos().put(aux1));
            shaderProgram.setUniformf("u_alpha", mw.opacity * alpha);
            shaderProgram.setUniformf("u_ar", GlobalConf.program.STEREOSCOPIC_MODE && (GlobalConf.program.STEREO_PROFILE != StereoProfile.HD_3DTV && GlobalConf.program.STEREO_PROFILE != StereoProfile.ANAGLYPHIC) ? 0.5f : 1f);

            // Relativistic effects
            addEffectsUniforms(shaderProgram, camera);

            particlesMesh.mesh.render(shaderProgram, ShapeType.Point.getGlType());
            shaderProgram.end();
            accumFb.end();

            /**
             * PASS 1
             */
            Gdx.gl20.glBlendFunc(GL30.GL_ZERO, GL30.GL_ONE_MINUS_SRC_COLOR);

            revealFb.begin();
            Gdx.gl.glClearColor(1, 1, 1, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

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

                ShaderProgram shaderProgram = postShader;

                shaderProgram.begin();
                int t0 = 0;
                int t1 = 21;
                accumFb.getColorBufferTexture().bind(t0);
                shaderProgram.setUniformi("tex_accum", t0);
                revealFb.getColorBufferTexture().bind(t1);
                shaderProgram.setUniformi("tex_reveal", t1);

                Gdx.gl20.glBlendFunc(GL30.GL_SRC_ALPHA, GL30.GL_ONE_MINUS_SRC_ALPHA);
                postMesh.render(shaderProgram);
                shaderProgram.end();

            }
        }

    }

    public void renderStudOld(Array<IRenderable> renderables, ICamera camera, double t) {
        if (renderables.size > 0) {
            MilkyWay mw = (MilkyWay) renderables.get(0);

            /**
             * PARTICLES RENDERER
             */
            if (UPDATE_POINTS) {
                streamToGpu(mw);
                // Put flag down
                UPDATE_POINTS = false;
            }
            float alpha = getAlpha(mw);
            if (alpha > 0) {
                /**
                 * PARTICLE RENDERER
                 */
                // Enable gl_PointCoord
                Gdx.gl20.glEnable(34913);
                // Enable point sizes
                Gdx.gl20.glEnable(0x8642);

                // Multiplicative blending (RGBs * RBGd)
                //Gdx.gl20.glBlendFunc(GL20.GL_ZERO, GL20.GL_SRC_COLOR);

                // Additive blending (RGBs + RGBd)
                //Gdx.gl20.glBlendFunc(GL20.GL_ONE, GL20.GL_ONE);

                // Transparency blending - Order dependent transparency
                Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

                ShaderProgram shaderProgram = getShaderProgram();

                camera.getCamera().near = .3e11f;
                camera.getCamera().far = 1e22f;
                camera.getCamera().update(false);

                shaderProgram.begin();
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
