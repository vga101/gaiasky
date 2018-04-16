package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.BufferedInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.scenegraph.StarGroup.StarBean;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

public abstract class AbstractStarGroupDataProvider implements IStarGroupDataProvider {

    protected Array<StarBean> list;
    protected Map<Long, double[]> sphericalPositions;
    protected Map<Long, float[]> colors;
    protected long[] countsPerMag;

    /**
     * <p>
     * The loader will only load stars for which the parallax error is
     * at most the percentage given here, in [0..1]. Faint stars (gmag >= 13.1)
     * More specifically, the following must be met:
     * </p>
     * <code>pllx_err &lt; pllx * pllxErrFactor</code>
     **/
    protected double parallaxErrorFactorFaint = 0.125;

    /**
     * <p>
     * The loader will only load stars for which the parallax error is
     * at most the percentage given here, in [0..1]. Bright stars (gmag < 13.1)
     * More specifically, the following must be met:
     * </p>
     * <code>pllx_err &lt; pllx * pllxErrFactor</code>
     **/
    protected double parallaxErrorFactorBright = 0.25;
    /**
     * Whether to use an adaptive threshold which lets more
     * bright stars in to avoid artifacts.
     */
    protected boolean adaptiveParallax = true;

    /**
     * The zero point for the parallaxes in mas. Gets added to all loaded
     * parallax values
     */
    protected double parallaxZeroPoint = 0;

    /**
     * Apply magnitude/color corrections for extinction/reddening
     */
    protected boolean magCorrections = false;

    public AbstractStarGroupDataProvider() {
        super();
    }

    /**
     * Initialises the lists and structures given a file by counting the number
     * of lines
     * 
     * @param f
     *            The file handle to count the lines
     */
    protected void initLists(FileHandle f) {
        try {
            int lines = countLines(f);
            initLists(lines - 1);
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    /**
     * Initialises the lists and structures given number of elements
     * 
     * @param f
     */
    protected void initLists(int elems) {
        list = new Array<StarBean>(elems);
    }

    protected void initLists() {
        initLists(1000);
        sphericalPositions = new HashMap<Long, double[]>();
        colors = new HashMap<Long, float[]>();
    }

    /**
     * Checks whether the parallax is accepted or not.
     * <p>
     * <b>If adaptive is not enabled:</b>
     * <pre>
     * accepted = pllx > 0 && pllx_err < pllx * pllx_err_factor && pllx_err <= 1
     * </pre>
     * </p>
     * <p>
     * <b>If adaptive is enabled:</b>
     * <pre>
     * accepted = pllx > 0 && pllx_err < pllx * max(0.5, pllx_err_factor) && pllx_err <= 1, if apparent_magnitude < 13.2
     * accepted = pllx > 0 && pllx_err < pllx * pllx_err_factor && pllx_err <= 1, otherwise
     * </pre>
     * </p>
     * 
     * @param appmag Apparent magnitude of star
     * @param pllx Parallax of star
     * @param pllxerr Parallax error of star
     * @return True if parallax is accepted, false otherwise
     */
    protected boolean acceptParallax(double appmag, double pllx, double pllxerr) {
        if (adaptiveParallax && appmag < 13.1) {
            return pllx >= 0 && pllxerr < pllx * parallaxErrorFactorBright && pllxerr <= 1;
        } else {
            return pllx >= 0 && pllxerr < pllx * parallaxErrorFactorFaint && pllxerr <= 1;
        }
    }

    protected int countLines(FileHandle f) throws IOException {
        InputStream is = new BufferedInputStream(f.read());
        return countLines(is);
    }

    /**
     * Counts the lines on this input stream
     * 
     * @param is
     *            The input stream
     * @return The number of lines
     * @throws IOException
     */
    protected int countLines(InputStream is) throws IOException {
        byte[] c = new byte[1024];
        int count = 0;
        int readChars = 0;
        boolean empty = true;
        while ((readChars = is.read(c)) != -1) {
            empty = false;
            for (int i = 0; i < readChars; ++i) {
                if (c[i] == '\n') {
                    ++count;
                }
            }
        }
        is.close();

        return (count == 0 && !empty) ? 1 : count;
    }

    protected void dumpToDisk(Array<StarBean> data, String filename, String format) {
        if (format.equals("bin"))
            dumpToDiskBin(data, filename, false);
        else if (format.equals("csv"))
            dumpToDiskCsv(data, filename);
    }

    protected void dumpToDiskBin(Array<StarBean> data, String filename, boolean serialized) {
        if (serialized) {
            // Use java serialization method
            List<StarBean> l = new ArrayList<StarBean>(data.size);
            for (StarBean p : data)
                l.add(p);

            try {
                ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
                oos.writeObject(l);
                oos.close();
                Logger.info("File " + filename + " written with " + l.size() + " stars");
            } catch (Exception e) {
                Logger.error(e);
            }
        } else {
            // Use own binary format
            BinaryDataProvider io = new BinaryDataProvider();
            try {
                int n = data.get(0).data.length;
                io.writeData(data, new FileOutputStream(filename));
                Logger.info("File " + filename + " written with " + n + " stars");
            } catch (Exception e) {
                Logger.error(e);
            }
        }
    }

    protected void dumpToDiskCsv(Array<StarBean> data, String filename) {
        String sep = "' ";
        try {
            PrintWriter writer = new PrintWriter(filename, "UTF-8");
            writer.println("name, x[km], y[km], z[km], absmag, appmag, r, g, b");
            Vector3d gal = new Vector3d();
            int n = 0;
            for (StarBean star : data) {
                float[] col = colors.get(star.id);
                double x = star.z();
                double y = -star.x();
                double z = star.y();
                gal.set(x, y, z).scl(Constants.U_TO_KM);
                gal.mul(Coordinates.equatorialToGalactic());
                writer.println(star.name + sep + x + sep + y + sep + z + sep + star.absmag() + sep + star.appmag() + sep + col[0] + sep + col[1] + sep + col[2]);
                n++;
            }
            writer.close();
            Logger.info("File " + filename + " written with " + n + " stars");
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public void setFileNumberCap(int cap) {
    }

    @Override
    public Map<Long, float[]> getColors() {
        return colors;
    }

    public void setParallaxErrorFactorFaint(double parallaxErrorFactor) {
        this.parallaxErrorFactorFaint = parallaxErrorFactor;
    }

    public void setParallaxErrorFactorBright(double parallaxErrorFactor) {
        this.parallaxErrorFactorBright = parallaxErrorFactor;
    }

    public void setAdaptiveParallax(boolean adaptive) {
        this.adaptiveParallax = adaptive;
    }

    public void setParallaxZeroPoint(double parallaxZeroPoint) {
        this.parallaxZeroPoint = parallaxZeroPoint;
    }

    public void setMagCorrections(boolean magCorrections) {
        this.magCorrections = magCorrections;
    }

    public long[] getCountsPerMag() {
        return this.countsPerMag;
    }
}
