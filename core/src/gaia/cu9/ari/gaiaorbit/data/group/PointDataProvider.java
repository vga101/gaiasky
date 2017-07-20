package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.scenegraph.ParticleGroup.ParticleBean;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;

/**
 * This provider loads point data in the internal reference system format, [x,
 * y, z]
 * 
 * @author tsagrista
 *
 */
public class PointDataProvider implements IParticleGroupDataProvider {

    public Array<? extends ParticleBean> loadData(String file) {
        return loadData(file, 1d);
    }

    public Array<? extends ParticleBean> loadData(String file, double factor) {
        Array<ParticleBean> pointData = new Array<ParticleBean>();
        FileHandle f = Gdx.files.internal(file);

        try {
            int tokenslen;
            BufferedReader br = new BufferedReader(new InputStreamReader(f.read()));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("#")) {
                    // Read line
                    String[] tokens = line.split("\\s+");
                    tokenslen = tokens.length;
                    double[] point = new double[tokenslen];
                    for (int j = 0; j < tokenslen; j++) {
                        point[j] = Parser.parseDouble(tokens[j]) * factor;
                    }
                    pointData.add(new ParticleBean(point));
                }
            }

            br.close();

            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", pointData.size, file));
        } catch (Exception e) {
            Logger.error(e, PointDataProvider.class.getName());
        }

        return pointData;
    }
}
