package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.scenegraph.ParticleGroup.ParticleBean;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;
import gaia.cu9.ari.gaiaorbit.util.units.Position;
import gaia.cu9.ari.gaiaorbit.util.units.Position.PositionType;

public class SDSSDataProvider implements IParticleGroupDataProvider {

    public Array<ParticleBean> loadData(String file) {
        return loadData(file, 1d);
    }

    public Array<ParticleBean> loadData(String file, double factor) {
        FileHandle f = Gdx.files.internal(file);

        @SuppressWarnings("unchecked")
        Array<ParticleBean> pointData = (Array<ParticleBean>) loadData(f.read(), factor);
        if (pointData != null)
            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", pointData.size, file));

        return pointData;
    }

    @Override
    public Array<? extends ParticleBean> loadData(InputStream is, double factor) {
        Array<ParticleBean> pointData = new Array<ParticleBean>();

        try {
            int tokenslen;
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("#")) {
                    // Read line
                    String[] tokens = line.split(",");
                    tokenslen = tokens.length;
                    double[] point = new double[tokenslen];
                    double ra = Parser.parseDouble(tokens[0]);
                    double dec = Parser.parseDouble(tokens[1]);
                    double z = Parser.parseDouble(tokens[2]);
                    if (z >= 0) {
                        // Dist in MPC
                        // double dist = redshiftToDistance(0.272, 0.0000812,
                        // 0.728, 70.4, z);
                        double dist = ((z * 299792.46) / 71);
                        if (dist > 16) {
                            // Convert position
                            Position p = new Position(ra, "deg", dec, "deg", dist, "mpc", PositionType.RA_DEC_DIST);
                            p.gsposition.scl(Constants.PC_TO_U);
                            point[0] = p.gsposition.x;
                            point[1] = p.gsposition.y;
                            point[2] = p.gsposition.z;
                            pointData.add(new ParticleBean(point));
                        }
                    }
                }
            }

            br.close();

        } catch (Exception e) {
            Logger.error(e, SDSSDataProvider.class.getName());
            return null;
        }

        return pointData;
    }

    public void setFileNumberCap(int cap) {
    }

    @Override
    public Array<? extends ParticleBean> loadDataMapped(File file, double factor) {
        // TODO Auto-generated method stub
        return null;
    }
}
