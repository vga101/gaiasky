package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

public class TGASDataProvider implements IStarGroupDataProvider {
    private static final boolean dumpToDisk = true;
    /** Colors BT, VT for all Tycho2 stars file **/
    private static final String btvtColorsFile = "data/tgas_final/bt-vt-tycho.csv";
    /** TYC identifier to B-V colours **/
    private Map<String, Float> tycBV;

    private Map<String, Integer> index;

    public Array<double[]> loadData(String file) {
        return loadData(file, 1d);
    }

    public Array<double[]> loadData(String file, double factor) {
        tycBV = loadTYCBVColours(btvtColorsFile);

        Array<double[]> pointData = new Array<double[]>();
        Map<String, Integer> index = new HashMap<String, Integer>();
        FileHandle f = Gdx.files.internal(file);

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(f.read()));
            String line;
            int i = 0;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("#")) {
                    // Read line
                    String[] tokens = line.split(",");
                    double[] point = new double[StarGroup.SIZE];

                    double pllx = Parser.parseDouble(tokens[3]);
                    double pllxerr = Parser.parseDouble(tokens[4]);

                    double distpc = (1000d / pllx);
                    double dist = distpc * Constants.PC_TO_U;

                    // Keep only stars with relevant parallaxes
                    if (dist >= 0 && pllx / pllxerr > 7 && pllxerr <= 1) {
                        long sourceid = Parser.parseLong(tokens[0]);
                        int hip = Parser.parseInt(tokens[12]);
                        String tyc = tokens[13];
                        String[] tycgroups = tyc.split("-");
                        int tyc1 = !tyc.isEmpty() ? Integer.parseInt(tycgroups[0]) : -1;
                        int tyc2 = !tyc.isEmpty() ? Integer.parseInt(tycgroups[1]) : -1;
                        int tyc3 = !tyc.isEmpty() ? Integer.parseInt(tycgroups[2]) : -1;

                        index.put(String.valueOf(sourceid), i);
                        if (hip > 0)
                            index.put("HIP " + hip, i);
                        if (tyc1 >= 0) {
                            index.put("TYC " + tyc1 + "-" + tyc2 + "-" + tyc3, i);
                        }

                        /** RA and DEC **/
                        double ra = Parser.parseDouble(tokens[1]);
                        double dec = Parser.parseDouble(tokens[2]);
                        Vector3d pos = Coordinates.sphericalToCartesian(Math.toRadians(ra), Math.toRadians(dec), dist, new Vector3d());

                        /** PROPER MOTIONS in mas/yr **/
                        double mualpha = Parser.parseDouble(tokens[5]);
                        double mudelta = Parser.parseDouble(tokens[6]);

                        /** RADIAL VELOCITY in km/s **/
                        double radvel = tokens[7] != null && !tokens[7].isEmpty() ? Parser.parseDouble(tokens[7].trim()) : 0;

                        /** PROPER MOTION VECTOR = (pos+dx) - pos **/
                        Vector3d pm = Coordinates.sphericalToCartesian(Math.toRadians(ra + mualpha * AstroUtils.MILLARCSEC_TO_DEG), Math.toRadians(dec + mudelta * AstroUtils.MILLARCSEC_TO_DEG), dist + radvel * Constants.KM_TO_U / Constants.S_TO_Y, new Vector3d());
                        pm.sub(pos);

                        double appmag = Parser.parseDouble(tokens[9]);
                        double absmag = (appmag - 2.5 * Math.log10(Math.pow(distpc / 10d, 2d)));
                        double flux = Math.pow(10, -absmag / 2.5f);
                        double size = Math.min((Math.pow(flux, 0.5f) * Constants.PC_TO_U * 0.16f), 1e9f) / 1.5;

                        /** COLOR, we use the tycBV map if present **/
                        double colorbv = 0;
                        if (tycBV != null) {
                            if (tycBV.containsKey(tyc)) {
                                colorbv = tycBV.get(tyc);
                            }
                        } else {
                            double bp = new Double(Parser.parseDouble(tokens[10]));
                            double rp = new Double(Parser.parseDouble(tokens[11]));
                            colorbv = bp - rp;
                        }
                        float[] rgb = ColourUtils.BVtoRGB(colorbv);
                        double col = Color.toFloatBits(rgb[0], rgb[1], rgb[2], 1.0f);

                        point[StarGroup.I_ID] = sourceid;
                        point[StarGroup.I_HIP] = hip;
                        point[StarGroup.I_TYC1] = tyc1;
                        point[StarGroup.I_TYC2] = tyc2;
                        point[StarGroup.I_TYC3] = tyc3;
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
                        point[StarGroup.I_SIZE] = size;
                        point[StarGroup.I_APPMAG] = appmag;
                        point[StarGroup.I_ABSMAG] = absmag;

                        pointData.add(point);
                        i++;
                    }
                }
            }

            br.close();

            if (dumpToDisk) {
                dumpToDisk(pointData, index);
                System.exit(0);
            }

            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", pointData.size, file));
        } catch (Exception e) {
            Logger.error(e, TGASDataProvider.class.getName());
        }

        return pointData;
    }

    private void dumpToDisk(Array<double[]> pointData, Map<String, Integer> index) {
        List<double[]> l = new ArrayList<double[]>(pointData.size);
        for (double[] p : pointData)
            l.add(p);

        try {
            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("/tmp/tgas.bin"));
            oos.writeObject(l);
            oos.close();

            oos = new ObjectOutputStream(new FileOutputStream("/tmp/tgas.bin.index"));
            oos.writeObject(index);
            oos.close();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    private Map<String, Float> loadTYCBVColours(String file) {
        final String comma = ",";
        final String comment = "#";

        Map<String, Float> result = new HashMap<String, Float>();
        FileHandle f = Gdx.files.internal(file);
        InputStream data = f.read();
        BufferedReader br = new BufferedReader(new InputStreamReader(data));
        try {
            // skip first line with headers
            br.readLine();
            int i = 1;
            String line;
            while ((line = br.readLine()) != null) {

                if (!line.startsWith(comment))
                    // Add B-V colour
                    addColour(line, result, comma);
                Logger.debug("Line " + i++);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                br.close();
            } catch (IOException e) {
                Logger.error(e);
            }

        }

        return result;
    }

    private void addColour(String line, Map<String, Float> map, String split) {
        String[] st = line.split(split);
        int tyc1 = Parser.parseInt(st[1].trim());
        int tyc2 = Parser.parseInt(st[2].trim());
        int tyc3 = Parser.parseInt(st[3].trim());

        float BV = 0;
        if (st.length >= 7) {
            float bt = Parser.parseFloat(st[5].trim());
            float vt = Parser.parseFloat(st[6].trim());
            BV = 0.850f * (bt - vt);
        }

        map.put(String.format("%04d", tyc1) + "-" + String.format("%05d", tyc2) + "-" + tyc3, BV);
    }

    @Override
    public Map<String, Integer> getIndex() {
        return index;
    }

}
