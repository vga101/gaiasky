package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.color.ColourUtils;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;
import gaia.cu9.ari.gaiaorbit.util.units.Position;
import gaia.cu9.ari.gaiaorbit.util.units.Position.PositionType;

public class TGASDataProvider implements IParticleGroupDataProvider {

    /** Colors BT, VT for all Tycho2 stars file **/
    private static final String btvtColorsFile = "data/tgas_final/bt-vt-tycho.csv";
    /** TYC identifier to B-V colours **/
    private Map<String, Float> tycBV;

    public Array<double[]> loadData(String file) {
        return loadData(file, 1d);
    }

    public Array<double[]> loadData(String file, double factor) {
        tycBV = loadTYCBVColours(btvtColorsFile);

        Array<double[]> pointData = new Array<double[]>();
        FileHandle f = Gdx.files.internal(file);

        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(f.read()));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.isEmpty() && !line.startsWith("#")) {
                    // Read line
                    String[] tokens = line.split(",");
                    double[] point = new double[8];

                    double pllx = Parser.parseDouble(tokens[3]);
                    double pllxerr = Parser.parseDouble(tokens[4]);

                    double dist = (1000d / pllx);

                    // Keep only stars with relevant parallaxes
                    if (dist >= 0 && pllx / pllxerr > 20 && pllxerr <= 1) {
                        long sourceid = Parser.parseLong(tokens[0]);
                        int hip = Parser.parseInt(tokens[12]);
                        String tycho2 = tokens[13];

                        double ra = Parser.parseDouble(tokens[1]);
                        double dec = Parser.parseDouble(tokens[2]);
                        double appmag = Parser.parseDouble(tokens[9]);
                        double absmag = (appmag - 2.5 * Math.log10(Math.pow(dist / 10d, 2d)));
                        double flux = Math.pow(10, -absmag / 2.5f);
                        double size = Math.min((Math.pow(flux, 0.5f) * Constants.PC_TO_U * 0.16f), 1e9f) / 1.5;

                        /** COLOR, we use the tycBV map if present **/
                        double colorbv = 0;
                        if (tycBV != null) {
                            if (tycBV.containsKey(tycho2)) {
                                colorbv = tycBV.get(tycho2);
                            }
                        } else {
                            double bp = new Double(Parser.parseDouble(tokens[10]));
                            double rp = new Double(Parser.parseDouble(tokens[11]));
                            colorbv = bp - rp;
                        }
                        float[] rgb = ColourUtils.BVtoRGB(colorbv);
                        double col = Color.toFloatBits(rgb[0], rgb[1], rgb[2], 1.0f);

                        Position p = new Position(ra, "deg", dec, "deg", dist, "pc", PositionType.RA_DEC_DIST);
                        p.gsposition.scl(Constants.PC_TO_U);
                        point[0] = p.gsposition.x;
                        point[1] = p.gsposition.y;
                        point[2] = p.gsposition.z;
                        point[3] = col;
                        point[4] = size;
                        point[5] = appmag;
                        point[6] = absmag;
                        point[7] = sourceid;
                        pointData.add(point);
                    }
                }
            }

            br.close();

            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", pointData.size, file));
        } catch (Exception e) {
            Logger.error(e, TGASDataProvider.class.getName());
        }

        return pointData;
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

}
