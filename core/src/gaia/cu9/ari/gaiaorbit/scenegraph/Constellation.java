package gaia.cu9.ari.gaiaorbit.scenegraph;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;

import gaia.cu9.ari.gaiaorbit.GaiaSky;
import gaia.cu9.ari.gaiaorbit.render.ComponentType;
import gaia.cu9.ari.gaiaorbit.render.I3DTextRenderable;
import gaia.cu9.ari.gaiaorbit.render.RenderingContext;
import gaia.cu9.ari.gaiaorbit.render.system.LineRenderSystem;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.time.ITimeFrameProvider;
import gaia.cu9.ari.gaiaorbit.util.tree.IPosition;

/**
 * Represents a constellation object.
 * 
 * @author Toni Sagrista
 *
 */
public class Constellation extends LineObject implements I3DTextRenderable {
    private static Array<Constellation> allConstellations = new Array<Constellation>(88);

    public static void updateConstellations() {
        for (Constellation c : allConstellations) {
            c.setUp();
        }
    }

    float alpha = .8f;
    float constalpha;
    boolean allLoaded = false;

    /** List of pairs of HIP identifiers **/
    public Array<int[]> ids;
    /**
     * The lines themselves as pairs of positions
     **/
    public Vector3[][] lines;

    public Constellation() {
        super();
        cc = new float[] { .9f, 1f, .9f, alpha };
    }

    public Constellation(String name, String parentName) {
        this();
        this.name = name;
        this.parentName = parentName;
    }

    @Override
    public void initialize() {
        allConstellations.add(this);
    }

    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera) {
        update(time, parentTransform, camera, 1f);
    }

    public void update(ITimeFrameProvider time, final Transform parentTransform, ICamera camera, float opacity) {
        // Recompute mean position
        pos.scl(0);
        Vector3d p = aux3d1.get();
        int nstars = 0;
        for (int i = 0; i < lines.length; i++) {
            Vector3[] line = lines[i];
            if (line != null) {
                p.set(line[0]).add(camera.getInversePos());
                pos.add(p);
                nstars++;
            }
        }
        pos.scl((1d / nstars));
        pos.nor().scl(100 * Constants.PC_TO_U);
        addToRenderLists(camera);
    }

    @Override
    public void setUp() {
        if (!allLoaded) {
            int npairs = ids.size;
            if (lines == null)
                lines = new Vector3[npairs][];
            IntMap<IPosition> hipMap = sg.getStarMap();
            allLoaded = true;
            for (int i = 0; i < npairs; i++) {
                int[] pair = ids.get(i);
                IPosition s1, s2;
                s1 = hipMap.get(pair[0]);
                s2 = hipMap.get(pair[1]);
                if (lines[i] == null && s1 != null && s2 != null) {
                    lines[i] = new Vector3[] { s1.getPosition().toVector3(), s2.getPosition().toVector3() };
                } else {
                    allLoaded = false;
                }
            }
        }
    }

    /**
     * Line rendering.
     */
    @Override
    public void render(LineRenderSystem renderer, ICamera camera, float alpha) {
        constalpha = alpha;
        alpha *= this.alpha;

        Vector3 campos = aux3f1.get();
        Vector3 p1 = aux3f2.get();
        Vector3 p2 = aux3f3.get();
        camera.getPos().setVector3(campos);
        // Fix, using positions directly
        for (Vector3[] pair : lines) {
            if (pair != null) {
                p1.set(pair[0]).sub(campos);
                p2.set(pair[1]).sub(campos);

                renderer.addLine(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, cc[0], cc[1], cc[2], alpha);
            }
        }

    }

    /**
     * Label rendering.
     */
    @Override
    public void render(SpriteBatch batch, ShaderProgram shader, BitmapFont font3d, BitmapFont font2d, RenderingContext rc, ICamera camera) {
        Vector3d pos = aux3d1.get();
        textPosition(camera, pos);
        shader.setUniformf("u_viewAngle", 90f);
        shader.setUniformf("u_thOverFactor", 1f);
        render3DLabel(batch, shader, font3d, camera, text(), pos, textScale(), textSize(), textColour());
    }

    @Override
    protected void addToRenderLists(ICamera camera) {
        addToRender(this, RenderGroup.LINE);
        if (renderText()) {
            addToRender(this, RenderGroup.LABEL);

        }
    }

    @Override
    public void updateLocalValues(ITimeFrameProvider time, ICamera camera) {
    }

    @Override
    public float[] textColour() {
        return cc;
    }

    @Override
    public float textSize() {
        return .3e7f;
    }

    @Override
    public float textScale() {
        return .5f;
    }

    @Override
    public void textPosition(ICamera cam, Vector3d out) {
        out.set(pos);
    }

    @Override
    public String text() {
        return name;
    }

    @Override
    public boolean renderText() {
        return GaiaSky.instance.isOn(ComponentType.Labels);
    }

    @Override
    public void textDepthBuffer() {
        Gdx.gl.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl.glDepthMask(true);
    }

    @Override
    public boolean isLabel() {
        return true;
    }

}
