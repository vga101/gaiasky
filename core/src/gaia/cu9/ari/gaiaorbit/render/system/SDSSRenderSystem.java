package gaia.cu9.ari.gaiaorbit.render.system;

import com.badlogic.gdx.Application.ApplicationType;
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
import gaia.cu9.ari.gaiaorbit.scenegraph.SDSS;
import gaia.cu9.ari.gaiaorbit.scenegraph.SceneGraphNode.RenderGroup;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf.ProgramConf.StereoProfile;
import gaia.cu9.ari.gaiaorbit.util.Logger;

public class SDSSRenderSystem extends ImmediateRenderSystem implements IObserver {
    private boolean UPDATE_POINTS = true;

    Vector3 aux1;
    int additionalOffset, pmOffset;

    public SDSSRenderSystem(RenderGroup rg, int priority, float[] alphas) {
	super(rg, priority, alphas);
    }

    @Override
    protected void initShaderProgram() {

	// POINT (STARS) PROGRAM
	if (Gdx.app.getType() == ApplicationType.WebGL)
	    shaderProgram = new ShaderProgram(Gdx.files.internal("shader/point.galaxy.vertex.glsl"),
		    Gdx.files.internal("shader/point.galaxy.fragment.wgl.glsl"));
	else
	    shaderProgram = new ShaderProgram(Gdx.files.internal("shader/point.galaxy.vertex.glsl"),
		    Gdx.files.internal("shader/point.galaxy.fragment.glsl"));
	if (!shaderProgram.isCompiled()) {
	    Logger.error(this.getClass().getName(), "Point shader compilation failed:\n" + shaderProgram.getLog());
	}
	shaderProgram.begin();
	shaderProgram.setUniformf("u_pointAlphaMin", 0.1f);
	shaderProgram.setUniformf("u_pointAlphaMax", 1.0f);
	shaderProgram.end();

    }

    @Override
    protected void initVertices() {
	/** STARS **/
	meshes = new MeshData[1];
	curr = new MeshData();
	meshes[0] = curr;

	aux1 = new Vector3();

	maxVertices = 3000000;

	VertexAttribute[] attribs = buildVertexAttributes();
	curr.mesh = new Mesh(false, maxVertices, 0, attribs);

	curr.vertices = new float[maxVertices * (curr.mesh.getVertexAttributes().vertexSize / 4)];
	curr.vertexSize = curr.mesh.getVertexAttributes().vertexSize / 4;
	curr.colorOffset = curr.mesh.getVertexAttribute(Usage.ColorPacked) != null
		? curr.mesh.getVertexAttribute(Usage.ColorPacked).offset / 4 : 0;
	pmOffset = curr.mesh.getVertexAttribute(Usage.Tangent) != null
		? curr.mesh.getVertexAttribute(Usage.Tangent).offset / 4 : 0;
	additionalOffset = curr.mesh.getVertexAttribute(Usage.Generic) != null
		? curr.mesh.getVertexAttribute(Usage.Generic).offset / 4 : 0;

    }

    @Override
    public void renderStud(Array<IRenderable> renderables, ICamera camera, float t) {
	if (renderables.size > 0) {
	    SDSS sdss = (SDSS) renderables.get(0);

	    /**
	     * GALAXY RENDER
	     */
	    if (UPDATE_POINTS) {

		/** STARS **/
		curr.clear();
		for (double[] star : sdss.pointData) {
		    // COLOR
		    curr.vertices[curr.vertexIdx + curr.colorOffset] = Color.toFloatBits(.9f, .9f, 1f, 0.9f);

		    // SIZE
		    curr.vertices[curr.vertexIdx + additionalOffset] = -1;
		    curr.vertices[curr.vertexIdx + additionalOffset + 1] = 5f * GlobalConf.SCALE_FACTOR;

		    // cb.transform.getTranslationf(aux);
		    // POSITION
		    final int idx = curr.vertexIdx;
		    curr.vertices[idx] = (float) star[0];
		    curr.vertices[idx + 1] = (float) star[1];
		    curr.vertices[idx + 2] = (float) star[2];

		    curr.vertexIdx += curr.vertexSize;
		}

	    }

	    // Put flag down
	    UPDATE_POINTS = false;

	    /**
	     * STAR RENDERER
	     */
	    if (Gdx.app.getType() == ApplicationType.Desktop) {
		// Enable gl_PointCoord
		Gdx.gl20.glEnable(34913);
		// Enable point sizes
		Gdx.gl20.glEnable(0x8642);
	    }
	    shaderProgram.begin();
	    shaderProgram.setUniformMatrix("u_projModelView", camera.getCamera().combined);
	    shaderProgram.setUniformf("u_camPos", camera.getCurrent().getPos().put(aux1));
	    shaderProgram.setUniformf("u_fovFactor", camera.getFovFactor());
	    shaderProgram.setUniformf("u_alpha", sdss.opacity * alphas[sdss.ct.getFirstOrdinal()]);
	    shaderProgram.setUniformf("u_ar",
		    GlobalConf.program.STEREOSCOPIC_MODE && (GlobalConf.program.STEREO_PROFILE != StereoProfile.HD_3DTV
			    && GlobalConf.program.STEREO_PROFILE != StereoProfile.ANAGLYPHIC) ? 0.5f : 1f);
	    curr.mesh.setVertices(curr.vertices, 0, curr.vertexIdx);
	    curr.mesh.render(shaderProgram, ShapeType.Point.getGlType());
	    shaderProgram.end();
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
