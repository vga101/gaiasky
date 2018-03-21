package gaia.cu9.ari.gaiaorbit.interfce;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

public interface IMinimapScale {
    public boolean isActive(Vector3d campos);

    public void initialize(OrthographicCamera ortho, SpriteBatch sb, ShapeRenderer sr, BitmapFont font, int side, int sideshort);

    public void renderSideProjection(FrameBuffer fb);

    public void renderTopProjection(FrameBuffer fb);
}
