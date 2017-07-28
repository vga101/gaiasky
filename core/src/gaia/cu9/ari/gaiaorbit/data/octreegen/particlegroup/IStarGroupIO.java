package gaia.cu9.ari.gaiaorbit.data.octreegen.particlegroup;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.scenegraph.AbstractPositionEntity;

public interface IStarGroupIO {

    public void writeParticles(Array<AbstractPositionEntity> list, OutputStream out);

    public Array<AbstractPositionEntity> readParticles(InputStream in) throws FileNotFoundException;
}
