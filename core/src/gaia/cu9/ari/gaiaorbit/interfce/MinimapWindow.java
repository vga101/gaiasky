package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.scenegraph.ICamera;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector2d;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.scene2d.OwnLabel;
import gaia.cu9.ari.gaiaorbit.util.scene2d.TextureWidget;

public class MinimapWindow extends GenericDialog {
    private FrameBuffer tfb, sfb;
    private TextureWidget topProjection, sideProjection;
    private OrthographicCamera ortho;
    int side, side2;
    int sideshort;
    private Vector2 sunPos;

    private ShapeRenderer sr;
    private SpriteBatch sb;
    private BitmapFont font;

    private Vector3d aux3d1, aux3d2;
    private Vector2d aux2d1, aux2d2;

    public MinimapWindow(Stage stage, Skin skin) {
        super("Minimap", skin, stage);
        aux3d1 = new Vector3d();
        aux3d2 = new Vector3d();
        aux2d1 = new Vector2d();
        aux2d2 = new Vector2d();

        side = (int) (GlobalConf.SCALE_FACTOR * 225);
        side2 = side / 2;
        sideshort = (int) (GlobalConf.SCALE_FACTOR * 112.5);
        ortho = new OrthographicCamera(side, side);

        sunPos = new Vector2(gal2Px(0, side2), gal2Px(-8000, side2));
        sr = new ShapeRenderer();
        sr.setAutoShapeType(true);
        sr.setProjectionMatrix(ortho.combined);

        sb = new SpriteBatch();
        ortho.setToOrtho(true, side, side);
        sb.setProjectionMatrix(ortho.combined);

        font = skin.getFont(GlobalConf.SCALE_FACTOR != 1 ? "ui-20" : "ui-12");

        tfb = new FrameBuffer(Format.RGBA8888, side, side, true);
        sfb = new FrameBuffer(Format.RGBA8888, side, sideshort, true);

        topProjection = new TextureWidget(tfb);
        sideProjection = new TextureWidget(sfb);

        setCancelText(txt("gui.close"));

        // Build
        buildSuper();

        setModal(false);
    }

    @Override
    protected void build() {
        OwnLabel headerSide = new OwnLabel("Side-on", skin, "header");
        Container<TextureWidget> mapSide = new Container<TextureWidget>();
        mapSide.setActor(sideProjection);
        OwnLabel headerTop = new OwnLabel("Top-down", skin, "header");
        Container<TextureWidget> mapTop = new Container<TextureWidget>();
        mapTop.setActor(topProjection);

        content.add(headerSide).left().row();
        content.add(sideProjection).left().padBottom(10 * GlobalConf.SCALE_FACTOR).row();

        content.add(headerTop).left().row();
        content.add(topProjection).left().pad(10 * GlobalConf.SCALE_FACTOR);

    }

    @Override
    protected void accept() {
    }

    @Override
    protected void cancel() {
    }

    public void act(float delta) {
        super.act(delta);
        drawTopProjection();
        drawSideProjection();
    }

    private void drawSideProjection() {

    }

    private void drawTopProjection() {

        // Draw top-down
        tfb.begin();
        // Clear
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT | (Gdx.graphics.getBufferFormat().coverageSampling ? GL20.GL_COVERAGE_BUFFER_BIT_NV : 0));
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);


        ICamera cam = GaiaSky.instance.cam.current;
        // Position
        Vector3d pos = aux3d1.set(cam.getPos()).mul(Coordinates.eqToGal());
        Vector2d campos2 = aux2d1;
        campos2.set(pos.x, pos.z).scl(Constants.U_TO_PC);
        campos2.y -= 8000;
        float cx = gal2Px(campos2.x, side2);
        float cy = gal2Px(campos2.y, side2);
        // Direction
        Vector3d dir = aux3d2.set(cam.getDirection()).mul(Coordinates.eqToGal());
        Vector2d camdir2 = aux2d2.set(dir.x, dir.z).nor().scl(35 * GlobalConf.SCALE_FACTOR);

        sr.begin(ShapeType.Line);
        // Grid
        sr.setColor(0.3f, 0.3f, 0.9f, 1f);
        for (int i = 0; i <= 16000; i += 4000) {
            sr.circle(0, 0, (i + 100) * side / 32000);
        }
        // Camera
        sr.setColor(0.4f, 0.9f, 0.4f, 1f);
        sr.circle(cx, cy, 8f);
        Vector2d endx = aux2d1.set(camdir2.x, camdir2.y);
        endx.rotate(-cam.getCamera().fieldOfView / 2d);
        float c1x = (float) endx.x + cx;
        float c1y = (float) endx.y + cy;
        endx.set(camdir2.x, camdir2.y);
        endx.rotate(cam.getCamera().fieldOfView / 2d);
        sr.triangle(cx, cy, c1x, c1y, (float) endx.x + cx, (float) endx.y + cy);

        // Bounds
        sr.setColor(1, 1, 1, 1);
        sr.rect(-side2, -side2, 0f, 0f, side, side, 1f, 1f, 0f);
        sr.end();

        // Sun position, 8 Kpc to do galactocentric
        sr.begin(ShapeType.Filled);
        sr.setColor(1f, 0f, 0f, 1f);
        sr.circle(sunPos.x, sunPos.y, 5);
        sr.end();

        // Fonts
        sb.begin();
        font.setColor(1, 0, 0, 1);
        font.draw(sb, "Sun", side2 + 8, sunPos.y - 8);
        font.setColor(.5f, .5f, 1, 1);
        font.draw(sb, "Galactic center", side2 + 8, side2 - 8);
        for (int i = 4000; i <= 16000; i += 4000) {
            font.draw(sb, "" + (i / 1000) + "Kpc", (16000 - i) * side / 32000 + 12, side2 + i / 100 + 25);
        }
        sb.end();

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
        return (int) ((pc / 16000d) * side);
    }

}
