package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.InputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.data.stars.HYGBinaryLoader;
import gaia.cu9.ari.gaiaorbit.scenegraph.StarGroup;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.color.ColourUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

public class HYGDataProvider extends AbstractStarGroupDataProvider {
    private static final boolean dumpToDisk = true;

    public HYGDataProvider() {
        super();
    }

    public Array<double[]> loadData(String file) {
        return loadData(file, 1d);
    }

    public Array<double[]> loadData(String file, double factor) {

        Array<double[]> pointData = new Array<double[]>();

        FileHandle f = Gdx.files.internal(file);
        InputStream data = f.read();
        DataInputStream data_in = new DataInputStream(data);

        try {
            // Read size of stars
            int size = data_in.readInt();
            int stari = 0;
            for (int idx = 0; idx < size; idx++) {
                try {
                    // name_length, name, appmag, absmag, colorbv, ra, dec,
                    // dist, mualpha, mudelta, radvel, id, hip
                    int nameLength = data_in.readInt();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < nameLength; i++) {
                        sb.append(data_in.readChar());
                    }
                    String name = sb.toString();
                    float appmag = data_in.readFloat();
                    float absmag = data_in.readFloat();
                    float colorbv = data_in.readFloat();
                    float ra = data_in.readFloat();
                    float dec = data_in.readFloat();
                    float dist = data_in.readFloat();
                    float mualpha = data_in.readFloat();
                    float mudelta = data_in.readFloat();
                    float radvel = data_in.readFloat();
                    long id = data_in.readInt();
                    id = -1l;// HIP stars with no gaia id go by 0
                    int hip = data_in.readInt();
                    if (appmag < GlobalConf.data.LIMIT_MAG_LOAD && !name.equals("Sol")) {
                        /** INDEX **/
                        index.put(name.toLowerCase(), stari);
                        index.put(name, stari);
                        index.put("HIP " + hip, stari);
                        index.put("hip " + hip, stari);

                        /** NAME **/
                        names.add(name);

                        double flux = Math.pow(10, -absmag / 2.5f);
                        double starsize = Math.min((Math.pow(flux, 0.5f) * Constants.PC_TO_U * 0.16f), 1e9f) / 1.5;

                        Vector3d pos = Coordinates.sphericalToCartesian(Math.toRadians(ra), Math.toRadians(dec), dist, new Vector3d());
                        Vector3d pm = Coordinates.sphericalToCartesian(Math.toRadians(ra + mualpha * AstroUtils.MILLARCSEC_TO_DEG), Math.toRadians(dec + mudelta * AstroUtils.MILLARCSEC_TO_DEG), dist + radvel * Constants.KM_TO_U * Constants.S_TO_Y, new Vector3d());
                        pm.sub(pos);

                        float[] rgb = ColourUtils.BVtoRGB(colorbv);
                        double col = Color.toFloatBits(rgb[0], rgb[1], rgb[2], 1.0f);

                        double[] point = new double[StarGroup.SIZE];
                        point[StarGroup.I_ID] = id;
                        point[StarGroup.I_HIP] = hip;
                        point[StarGroup.I_TYC1] = -1;
                        point[StarGroup.I_TYC2] = -1;
                        point[StarGroup.I_TYC3] = -1;
                        point[StarGroup.I_X] = pos.x;
                        point[StarGroup.I_Y] = pos.y;
                        point[StarGroup.I_Z] = pos.z;
                        point[StarGroup.I_PMX] = pm.x;
                        point[StarGroup.I_PMY] = pm.y;
                        point[StarGroup.I_PMZ] = pm.z;
                        point[StarGroup.I_MUALPHA] = mualpha;
                        point[StarGroup.I_MUDELTA] = mudelta;
                        point[StarGroup.I_RADVEL] = radvel;
                        point[StarGroup.I_COL] = col;
                        point[StarGroup.I_SIZE] = starsize;
                        point[StarGroup.I_APPMAG] = appmag;
                        point[StarGroup.I_ABSMAG] = absmag;

                        pointData.add(point);
                        stari++;
                    }
                } catch (EOFException eof) {
                    Logger.error(eof, HYGBinaryLoader.class.getSimpleName());
                }
            }

            data_in.close();

            if (dumpToDisk) {
                dumpToDisk(pointData, "/tmp/hyg.bin");
            }

            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", pointData.size, file));
        } catch (Exception e) {
            Logger.error(e, HYGDataProvider.class.getName());
        }

        return pointData;
    }

}
