package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.render.IShapeRenderable;
import gaia.cu9.ari.gaiaorbit.render.RenderingContext;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;

public class Text2D extends FadeNode implements I3DTextRenderable, IShapeRenderable {

    private float scale = 1f;
    private int align;
    private boolean lines = false;
    private float lineHeight = 0f;

    public Text2D() {
        super();
    }

    @Override
    public void initialize() {
    }

    public void updateLocal(ITimeFrameProvider time, ICamera camera) {
        super.updateLocal(time, camera);
        this.viewAngle = 80f;
        this.viewAngleApparent = this.viewAngle;
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
    public void render(ShapeRenderer shapeRenderer, RenderingContext rc, float alpha, ICamera camera) {
        scale = 0.7f;
        float lenw = 0.4f * scale * rc.w();
        float x0 = (rc.w() - lenw) / 2f;
        float x1 = x0 + lenw;

        float ytop = (60f + 10f * scale) * GlobalConf.SCALE_FACTOR;
        float ybottom = (60f - lineHeight * scale - 10f * scale) * GlobalConf.SCALE_FACTOR;

        shapeRenderer.getProjectionMatrix().setToOrtho2D(0, 0, rc.w(), rc.h());
        shapeRenderer.setColor(1f, 1f, 1f, 0.7f * opacity * alpha);
        shapeRenderer.line(x0, ytop, x1, ytop);
        shapeRenderer.line(x0, ybottom, x1, ybottom);

    }

    @Override
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font3d, BitmapFont font2d, RenderingContext rc, ICamera camera) {
        scale = 0.7f;
        shader.setUniformf("u_viewAngle", (float) viewAngleApparent);
        shader.setUniformf("u_viewAnglePow", 1f);
        shader.setUniformf("u_thOverFactor", 1f);
        shader.setUniformf("u_thOverFactorScl", 1f);

        // Text
        render2DLabel(batch, shader, rc, font3d, camera, text(), 0, 60f * GlobalConf.SCALE_FACTOR, scale * GlobalConf.SCALE_FACTOR, align);

        lineHeight = font3d.getLineHeight();
    }

    @Override
    public float[] textColour() {
        return labelColour;
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
            if (lines) {
                addToRender(this, RenderGroup.SHAPE);
            }
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

    public void setLines(String linesText) {
        lines = Boolean.parseBoolean(linesText);
    }

}
