package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.scenegraph.StarGroup;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.color.ColourUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;

/**
 * Loads the DR2 catalog in CSV format
 * 
 * Source position and corresponding errors are in radians, parallax in mas and
 * proper motion in mas/yr. The colors we get from the BT and VT magnitudes in
 * the original Tycho2 catalog: B-V = 0.85 * (BT - VT)
 * 
 * @author Toni Sagrista
 *
 */
public class DR2DataProvider extends AbstractStarGroupDataProvider {

    private static final String comma = ",";
    private static final String comment = "#";

    private static final String separator = comma;

    /** Whether to load the sourceId->HIP correspondences file **/
    public boolean useHIP = false;
    /** Map of Gaia sourceId to HIP id **/
    public Map<Long, Integer> sidHIPMap;

    /**
     * INDICES: 
     * 
     * source_id 
     * ra[deg] 
     * dec[deg] 
     * parallax[mas] 
     * ra_err[mas]
     * dec_err[mas] 
     * pllx_err[mas] 
     * mualpha[mas/yr] 
     * mudelta[mas/yr] 
     * radvel[km/s]
     * mualpha_err[mas/yr] 
     * mudelta_err[mas/yr] 
     * radvel_err[km/s] 
     * gmag[mag]
     * bp[mag] 
     * rp[mag] 
     * ref_epoch[julian years]
     */
    private static final int SOURCE_ID = 0;
    private static final int RA = 1;
    private static final int DEC = 2;
    private static final int PLLX = 3;
    private static final int RA_ERR = 4;
    private static final int DEC_ERR = 5;
    private static final int PLLX_ERR = 6;
    private static final int MUALPHA = 7;
    private static final int MUDELTA = 8;
    private static final int RADVEL = 9;
    private static final int MUALPHA_ERR = 10;
    private static final int MUDELTA_ERR = 11;
    private static final int RADVEL_ERR = 12;
    private static final int G_MAG = 13;
    private static final int BP_MAG = 14;
    private static final int RP_MAG = 15;
    private static final int REF_EPOCH = 16;

    private static final int[] indices = new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 };

    public Array<double[]> loadData(String file) {
        return loadData(file, 1d);
    }

    public Array<double[]> loadData(String file, double factor) {
        initLists(50000000);

        FileHandle f = Gdx.files.internal(file);
        Integer i = 0;
        if (f.isDirectory()) {
            // Recursive
            FileHandle[] files = f.list();
            Arrays.sort(files, (FileHandle a, FileHandle b) -> {
                return a.name().compareTo(b.name());
            });
            int fn = 0;
            for (FileHandle fh : files) {
                loadFile(fh, factor, i);
                fn++;
                if (fn >= 80)
                    break;
            }
        } else if (f.name().endsWith(".csv")) {
            loadFile(f, factor, i);
        }
        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", pointData.size, f.path()));
        return pointData;
    }

    public void loadFile(FileHandle f, double factor, Integer i) {
        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.datafile", f.path()));

        // Simple case
        InputStream data = f.read();
        BufferedReader br = new BufferedReader(new InputStreamReader(data));

        try {
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.startsWith(comment)) {
                    // Add star
                    addStar(line, i);
                    i++;
                }
            }
        } catch (IOException e) {
            Logger.error(e);
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                Logger.error(e);
            }

        }
    }

    private void addStar(String line, Integer i) {
        String[] tokens = line.split(separator);
        double[] point = new double[StarGroup.SIZE];

        double pllx = Parser.parseDouble(tokens[indices[PLLX]]);
        double pllxerr = Parser.parseDouble(tokens[indices[PLLX_ERR]]);

        double distpc = (1000d / pllx);
        double dist = distpc * Constants.PC_TO_U;

        // Keep only stars with relevant parallaxes
        if (dist >= 0 && pllx / pllxerr > 7 && pllxerr <= 1) {
            long sourceid = Parser.parseLong(tokens[indices[SOURCE_ID]]);

            /** ID **/
            ids.add(sourceid);

            /** INDEX **/
            index.put(String.valueOf((long) sourceid), i);

            /** NAME **/
            names.add(String.valueOf((long) sourceid));

            /** RA and DEC **/
            double ra = Parser.parseDouble(tokens[indices[RA]]);
            double dec = Parser.parseDouble(tokens[indices[DEC]]);
            Vector3d pos = Coordinates.sphericalToCartesian(Math.toRadians(ra), Math.toRadians(dec), dist, new Vector3d());

            /** PROPER MOTIONS in mas/yr **/
            double mualpha = Parser.parseDouble(tokens[indices[MUALPHA]]);
            double mudelta = Parser.parseDouble(tokens[indices[MUDELTA]]);

            /** PROPER MOTION VECTOR = (pos+dx) - pos **/
            Vector3d pm = Coordinates.sphericalToCartesian(Math.toRadians(ra + mualpha * AstroUtils.MILLARCSEC_TO_DEG), Math.toRadians(dec + mudelta * AstroUtils.MILLARCSEC_TO_DEG), dist, new Vector3d());
            pm.sub(pos);

            double appmag = Parser.parseDouble(tokens[indices[G_MAG]]);
            double absmag = (appmag - 2.5 * Math.log10(Math.pow(distpc / 10d, 2d)));
            double flux = Math.pow(10, -absmag / 2.5f);
            double size = Math.min((Math.pow(flux, 0.5f) * Constants.PC_TO_U * 0.16f), 1e9f) / 1.5;

            /** COLOR, we use the tycBV map if present **/
            double colorbv = 0;
            if (indices[BP_MAG] >= 0 && indices[RP_MAG] >= 0) {
                // Real TGAS
                float bp = new Double(Parser.parseDouble(tokens[indices[BP_MAG]].trim())).floatValue();
                float rp = new Double(Parser.parseDouble(tokens[indices[RP_MAG]].trim())).floatValue();
                colorbv = bp - rp;
            } else {
                // Use color value in BP
                colorbv = new Double(Parser.parseDouble(tokens[indices[BP_MAG]].trim())).floatValue();
            }

            float[] rgb = ColourUtils.BVtoRGB(colorbv);
            double col = Color.toFloatBits(rgb[0], rgb[1], rgb[2], 1.0f);

            point[StarGroup.I_HIP] = -1;
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
            point[StarGroup.I_RADVEL] = 0;
            point[StarGroup.I_COL] = col;
            point[StarGroup.I_SIZE] = size;
            point[StarGroup.I_APPMAG] = appmag;
            point[StarGroup.I_ABSMAG] = absmag;

            pointData.add(point);
            i++;
        }
    }

}
