package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.VerticalGroup;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.math.Vector2d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;

public class MinimapWindow extends GenericDialog {
    private Texture s, t;
    private FrameBuffer tfb;
    private Image sideBg, topBg;

    int side, side2;
    int sideshort;

    private SpriteBatch sb;
    private ShapeRenderer sr;

    private Vector3d aux3d1;
    private Vector2d aux2d1;

    public MinimapWindow(Stage stage, Skin skin) {
        super("Minimap", skin, stage);
        aux3d1 = new Vector3d();
        aux2d1 = new Vector2d();

        side = (int) (GlobalConf.SCALE_FACTOR * 450);
        side2 = side / 2;
        sideshort = (int) (GlobalConf.SCALE_FACTOR * 112.5);

        sb = new SpriteBatch(1);
        sr = new ShapeRenderer();
        sr.setAutoShapeType(true);

        tfb = new FrameBuffer(Format.RGBA8888, side, side, false);
        s = new Texture("img/minimap-side.png");
        t = new Texture("img/minimap-top.png");
        sideBg = new Image(s);
        topBg = new Image(new TextureRegionDrawable(new TextureRegion(tfb.getColorBufferTexture())));

        setCancelText(txt("gui.close"));

        // Build
        buildSuper();

        setModal(false);
    }

    @Override
    protected void build() {
        // Main layout, vertical
        VerticalGroup mainLayout = new VerticalGroup();
        mainLayout.align(Align.left);

        OwnLabel headerSide = new OwnLabel("Side-on", skin, "header");
        Container<Image> mapSide = new Container<Image>();
        mapSide.setActor(sideBg);
        OwnLabel headerTop = new OwnLabel("Top-down", skin, "header");
        Container<Image> mapTop = new Container<Image>();
        mapTop.setActor(topBg);

        mainLayout.addActor(headerSide);
        mainLayout.addActor(mapSide);

        mainLayout.addActor(headerTop);
        mainLayout.addActor(mapTop);

        content.add(mainLayout);
    }

    @Override
    protected void accept() {
    }

    @Override
    protected void cancel() {
    }

    public void act(float delta) {
        super.act(delta);

        // Draw top-down
        tfb.begin();
        // Clear
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Render bg
        sb.begin();
        sb.draw(t, 0, 0, side, side);
        sb.end();

        // Render camera
        ICamera cam = GaiaSky.instance.cam.current;
        Vector3d pos = aux3d1.set(cam.getPos());
        Vector2d campos2 = aux2d1;
        campos2.set(pos.z, pos.x).scl(Constants.U_TO_PC);
        campos2.y += 8000;
        // 8 Kpc to do galactocentric
        sr.begin(ShapeType.Filled);
        sr.setColor(1f, 0f, 0f, 1f);
        sr.circle(gal2Px(0, side2), gal2Px(8000, side2), 5);
        sr.end();

        sr.begin(ShapeType.Line);
        sr.setColor(0.4f, 0.4f, 0.9f, 1f);
        sr.circle(gal2Px(campos2.x, side2), gal2Px(campos2.y, side2), 20f);
        //sr.circle(side, side, 30);
        sr.end();

        tfb.end();

    }

    /** 
     * Converts a galactocentric coordinate in parsecs to pixels, given the side/2 of
     * the end minimap
     * @param pc The galactocentric coordinate in parsecs
     * @param side Side/2 of minimap
     * @return Pixel coordinate
     */
    private int gal2Px(double pc, float side) {
        return (int) ((pc / 16000d) * side + side);
    }

}
