package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;

import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public class Text2D extends AbstractPositionEntity implements I3DTextRenderable {

    float scale = 1f;
    int align;

    public Text2D() {
	super();
    }

    @Override
    public void initialize() {
	super.initialize();
    }

    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
	this.distToCamera = 0f;
	this.viewAngle = 80f;
	this.viewAngleApparent = this.viewAngle;
	if (!copy) {
	    addToRenderLists(camera);
	}
    }

    @Override
    public double getDistToCamera() {
	return 0;
    }

    @Override
    public boolean renderText() {
	return this.opacity > 0;
    }

    @Override
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font3d, BitmapFont font2d, ICamera camera) {
	shader.setUniformf("a_viewAngle", (float) viewAngleApparent);
	shader.setUniformf("a_viewAnglePow", 1f);
	shader.setUniformf("a_thOverFactor", 1f);
	shader.setUniformf("a_thOverFactorScl", 1f);

	font3d.setColor(cc[0], cc[1], cc[2], cc[3] * opacity);
	render2DLabel(batch, shader, font3d, camera, text(), 0, 60f * GlobalConf.SCALE_FACTOR,
		scale * GlobalConf.SCALE_FACTOR, align);
    }

    @Override
    public float[] textColour() {
	return cc;
    }

    @Override
    public float textSize() {
	return 10;
    }

    @Override
    public float textScale() {
	return 1;
    }

    @Override
    public void textPosition(ICamera cam, Vector3d out) {
    }

    @Override
    public String text() {
	return name;
    }

    @Override
    public void textDepthBuffer() {
    }

    @Override
    public boolean isLabel() {
	return true;
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
	if (renderText()) {
	    addToRender(this, RenderGroup.LABEL);
	}
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {

    }

    public void setScale(Double scale) {
	this.scale = scale.floatValue();
    }

    public void setAlign(Long align) {
	this.align = align.intValue();
    }

}
