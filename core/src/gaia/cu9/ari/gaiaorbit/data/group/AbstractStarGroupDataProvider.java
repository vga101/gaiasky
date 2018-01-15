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
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

public abstract class AbstractStarGroupDataProvider implements IStarGroupDataProvider {

    protected Array<StarBean> list;
    protected Map<Long, double[]> sphericalPositions;
    protected Map<Long, float[]> colors;

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

            } catch (Exception e) {
                Logger.error(e);
            }
        } else {
            // Use own binary format
            BinaryDataProvider io = new BinaryDataProvider();
            try {
                io.writeData(data, new FileOutputStream(filename));
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
            for (StarBean star : data) {
                float[] col = colors.get(star.id);
                double x = star.z();
                double y = -star.x();
                double z = star.y();
                gal.set(x, y, z);
                gal.mul(Coordinates.equatorialToGalactic());
                writer.println(star.name + sep + x + sep + y + sep + z + sep + star.absmag() + sep + star.appmag() + sep + col[0] + sep + col[1] + sep + col[2]);
            }
            writer.close();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public void setFileNumberCap(int cap) {
    }

}
