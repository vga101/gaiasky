package gaia.cu9.ari.gaiaorbit.render.system;

import java.util.Comparator;

import com.badlogic.gdx.Application.ApplicationType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.event.EventManager;
import gaia.cu9.ari.gaiaorbit.event.Events;
import gaia.cu9.ari.gaiaorbit.event.IObserver;
import gaia.cu9.ari.gaiaorbit.render.IRenderable;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.scenegraph.ParticleGroup;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.comp.DistToCameraComparator;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;

public class StarGroupRenderSystem extends ImmediateRenderSystem implements IObserver {
    private final double BRIGHTNESS_FACTOR;

    Vector3 aux1;
    int sizeOffset, pmOffset;

    Comparator<IRenderable> comp;
    float[] pointAlpha, alphaSizeFovBr;

    public StarGroupRenderSystem(RenderGroup rg, int priority, float[] alphas) {
        super(rg, priority, alphas);
        BRIGHTNESS_FACTOR = Constants.webgl ? 15 : 10;
        this.comp = new DistToCameraComparator<IRenderable>();
        this.alphaSizeFovBr = new float[4];
        EventManager.instance.subscribe(this, Events.STAR_MIN_OPACITY_CMD);
    }

    @Override
    protected void initShaderProgram() {
        shaderProgram = new ShaderProgram(Gdx.files.internal("shader/star.group.vertex.glsl"), Gdx.files.internal("shader/star.group.fragment.glsl"));
        if (!shaderProgram.isCompiled()) {
            Logger.error(this.getClass().getName(), "Star group shader compilation failed:\n" + shaderProgram.getLog());
        }
        pointAlpha = new float[] { GlobalConf.scene.POINT_ALPHA_MIN, GlobalConf.scene.POINT_ALPHA_MAX };
        shaderProgram.begin();
        shaderProgram.setUniform2fv("u_pointAlpha", pointAlpha, 0, 2);
        shaderProgram.end();
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
        //pmOffset = curr.mesh.getVertexAttribute(Usage.Tangent) != null ? curr.mesh.getVertexAttribute(Usage.Tangent).offset / 4 : 0;
        sizeOffset = curr.mesh.getVertexAttribute(Usage.Generic) != null ? curr.mesh.getVertexAttribute(Usage.Generic).offset / 4 : 0;

    }

    @Override
    public void renderStud(Array<IRenderable> renderables, ICamera camera, float t) {
        renderables.sort(comp);
        if (renderables.size > 0) {
            for (IRenderable renderable : renderables) {
                ParticleGroup particleGroup = (ParticleGroup) renderable;

                /**
                 * ADD PARTICLES
                 */
                if (!particleGroup.inGpu) {
                    particleGroup.offset = curr.vertexIdx;
                    for (double[] p : particleGroup.pointData) {
                        // COLOR
                        curr.vertices[curr.vertexIdx + curr.colorOffset] = (float) p[3];

                        // SIZE
                        curr.vertices[curr.vertexIdx + sizeOffset] = (float) (p[4] * Constants.STAR_SIZE_FACTOR);

                        // POSITION
                        final int idx = curr.vertexIdx;
                        curr.vertices[idx] = (float) p[0];
                        curr.vertices[idx + 1] = (float) p[1];
                        curr.vertices[idx + 2] = (float) p[2];

                        // PROPER MOTION
                        //                        curr.vertices[curr.vertexIdx + pmOffset] = 0f;
                        //                        curr.vertices[curr.vertexIdx + pmOffset + 1] = 0f;
                        //                        curr.vertices[curr.vertexIdx + pmOffset + 2] = 0f;

                        curr.vertexIdx += curr.vertexSize;
                    }
                    particleGroup.count = particleGroup.pointData.size * curr.vertexSize;

                    particleGroup.inGpu = true;

                }

                /**
                 * RENDER
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

                alphaSizeFovBr[0] = particleGroup.opacity * alphas[particleGroup.ct.getFirstOrdinal()];
                alphaSizeFovBr[1] = camera.getNCameras() == 1 ? (GlobalConf.scene.STAR_POINT_SIZE * GlobalConf.SCALE_FACTOR * (GlobalConf.program.isStereoFullWidth() ? 1 : 2)) : (GlobalConf.scene.STAR_POINT_SIZE * GlobalConf.SCALE_FACTOR * 10);
                alphaSizeFovBr[2] = camera.getFovFactor();
                alphaSizeFovBr[3] = (float) (GlobalConf.scene.STAR_BRIGHTNESS * BRIGHTNESS_FACTOR);
                shaderProgram.setUniform4fv("u_alphaSizeFovBr", alphaSizeFovBr, 0, 4);

                shaderProgram.setUniformf("u_t", (float) AstroUtils.getMsSinceJ2000(GaiaSky.instance.time.getTime()));
                shaderProgram.setUniformf("u_ar", GlobalConf.program.STEREOSCOPIC_MODE && (GlobalConf.program.STEREO_PROFILE != StereoProfile.HD_3DTV && GlobalConf.program.STEREO_PROFILE != StereoProfile.ANAGLYPHIC) ? 0.5f : 1f);
                shaderProgram.setUniformf("u_thAnglePoint", (float) GlobalConf.scene.STAR_THRESHOLD_POINT);

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
        //attribs.add(new VertexAttribute(Usage.Tangent, 3, "a_pm"));
        attribs.add(new VertexAttribute(Usage.ColorPacked, 4, ShaderProgram.COLOR_ATTRIBUTE));
        attribs.add(new VertexAttribute(Usage.Generic, 1, "a_size"));

        VertexAttribute[] array = new VertexAttribute[attribs.size];
        for (int i = 0; i < attribs.size; i++)
            array[i] = attribs.get(i);
        return array;
    }

    @Override
    public void notify(Events event, Object... data) {
        switch (event) {
        case STAR_MIN_OPACITY_CMD:
            if (shaderProgram != null && shaderProgram.isCompiled()) {
                pointAlpha[0] = (float) data[0];
                Gdx.app.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        shaderProgram.begin();
                        shaderProgram.setUniform2fv("u_pointAlpha", pointAlpha, 0, 2);
                        shaderProgram.end();
                    }

                });
            }
            break;
        default:
            break;
        }
    }

}
