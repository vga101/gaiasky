package gaia.cu9.ari.gaiaorbit.util;

import java.io.FilenameFilter;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import net.jafama.FastMath;

/**
 * Holds and initializes resources utilized globally.
 * 
 * @author Toni Sagrista
 *
 */
public class GlobalResources {

    /** Global all-purpose sprite batch **/
    public static SpriteBatch spriteBatch;
    /** Link cursor **/
    public static Pixmap linkCursor;
    /** The global skin **/
    public static Skin skin;

    /**
     * Model for atmosphere scattering
     */
    public static final String atmModelLocation = "models/atm/atm-uv.g3db";

    public static void initialize(AssetManager manager) {

        // Sprite batch
        spriteBatch = new SpriteBatch();

        // Create skin right now, it is needed.
        FileHandle fh = Gdx.files.internal("skins/" + GlobalConf.program.UI_THEME + ".json");
        skin = new Skin(fh);

        // Async load
        manager.load("img/cursor-link.png", Pixmap.class);
    }

    public static void doneLoading(AssetManager manager) {
        // Cursor for links
        linkCursor = manager.get("img/cursor-link.png", Pixmap.class);
    }

    /**
     * Converts from property name to method name by removing the separator dots
     * and capitalising each chunk. Example: model.texture.bump ->
     * ModelTextureBump
     * 
     * @param property
     *            The property name.
     * @return
     */
    public static String propertyToMethodName(String property) {
        String[] parts = property.split("\\.");
        StringBuilder b = new StringBuilder();
        for (String part : parts) {
            b.append(capitalise(part));
        }
        return b.toString();
    }

    /**
     * Returns the given string with the first letter capitalised
     * 
     * @param line
     * @return
     */
    public static String capitalise(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    /**
     * Returns the given string with the first letter capitalised and all the
     * others in lower case.
     * 
     * @param line
     * @return
     */
    public static String trueCapitalise(String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1).toLowerCase();
    }

    /**
     * Converts this double to the string representation of a distance.
     * 
     * @param d
     *            In internal units
     * @return An array containing the float number and the string units.
     */
    public static Pair<Double, String> doubleToDistanceString(double d) {
        d = d * Constants.U_TO_KM;
        if (Math.abs(d) < 1f) {
            // m
            return new Pair<Double, String>((d * 1000), "m");
        }
        if (Math.abs(d) < AstroUtils.AU_TO_KM) {
            // km
            return new Pair<Double, String>(d, "km");
        } else if (Math.abs(d) < AstroUtils.PC_TO_KM) {
            // AU
            return new Pair<Double, String>(d * AstroUtils.KM_TO_AU, "AU");
        } else {
            // pc
            return new Pair<Double, String>((d * AstroUtils.KM_TO_PC), "pc");
        }
    }

    /** Converts the double to the string representation of a velocity (always in seconds)
     * 
     * @param d In internal units
     * @return Array containing the number and the units
     */
    public static Pair<Double, String> doubleToVelocityString(double d) {
        Pair<Double, String> res = doubleToDistanceString(d);
        res.setSecond(res.getSecond().concat("/s"));
        return res;
    }

    /**
     * Converts this float to the string representation of a distance.
     * 
     * @param f
     *            In internal units
     * @return An array containing the float number and the string units.
     */
    public static Pair<Float, String> floatToDistanceString(float f) {
        Pair<Double, String> result = doubleToDistanceString((double) f);
        return new Pair<Float, String>(result.getFirst().floatValue(), result.getSecond());
    }

    /**
     * Transforms the given double array into a float array by casting each of
     * its numbers.
     * 
     * @param array
     * @return
     */
    public static float[] toFloatArray(double[] array) {
        float[] res = new float[array.length];
        for (int i = 0; i < array.length; i++)
            res[i] = (float) array[i];
        return res;
    }

    /**
     * Computes whether a body with the given position is visible by a camera
     * with the given direction and angle. Coordinates are assumed to be in the
     * camera-origin system.
     * 
     * @param point
     *            The position of the body in the reference system of the camera
     *            (i.e. camera is at origin).
     * @param coneAngle
     *            The cone angle of the camera.
     * @param dir
     *            The direction.
     * @return True if the body is visible.
     */
    public static boolean isInView(Vector3d point, double pointDistance, float coneAngle, Vector3d dir) {
        return FastMath.acos(point.dot(dir) / pointDistance) < coneAngle;
    }

    /**
     * Computes whether any of the given points is visible by a camera with the
     * given direction and the given cone angle. Coordinates are assumed to be
     * in the camera-origin system.
     * 
     * @param points
     *            The array of points to check.
     * @param coneAngle
     *            The cone angle of the camera (field of view).
     * @param dir
     *            The direction.
     * @return True if any of the points is in the camera view cone.
     */
    public static boolean isAnyInView(Vector3d[] points, float coneAngle, Vector3d dir) {
        boolean inview = false;
        int size = points.length;
        for (int i = 0; i < size; i++) {
            inview = inview || FastMath.acos(points[i].dot(dir) / points[i].len()) < coneAngle;
        }
        return inview;
    }

    /**
     * Compares a given buffer with another buffer.
     * 
     * @param buf
     *            Buffer to compare against
     * @param compareTo
     *            Buffer to compare to (content should be ASCII lowercase if
     *            possible)
     * @return True if the buffers compare favourably, false otherwise
     */
    public static boolean equal(String buf, char[] compareTo, boolean ignoreCase) {
        if (buf == null || compareTo == null || buf.length() == 0)
            return false;
        char a, b;
        int len = Math.min(buf.length(), compareTo.length);
        if (ignoreCase) {
            for (int i = 0; i < len; i++) {
                a = buf.charAt(i);
                b = compareTo[i];
                if (a == b || (a - 32) == b)
                    continue; // test a == a or A == a;
                return false;
            }
        } else {
            for (int i = 0; i < len; i++) {
                a = buf.charAt(i);
                b = compareTo[i];
                if (a == b)
                    continue; // test a == a
                return false;
            }
        }
        return true;
    }

    public static int countOccurrences(String haystack, char needle) {
        int count = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) {
                count++;
            }
        }
        return count;
    }

    public static int nthIndexOf(String text, char needle, int n) {
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == needle) {
                n--;
                if (n == 0) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Gets all the files with the given extension in the given file handle f.
     * 
     * @param f
     *            The directory to get all the files
     * @param l
     *            The list with re results
     * @param extension
     *            The extension of the files
     * @return The list l
     */
    public static Array<FileHandle> listRec(FileHandle f, Array<FileHandle> l, String extension) {
        if (f.exists()) {
            if (f.isDirectory()) {
                FileHandle[] partial = f.list();
                for (FileHandle fh : partial) {
                    l = listRec(fh, l, extension);
                }

            } else {
                if (f.name().endsWith(extension)) {
                    l.add(f);
                }
            }
        }

        return l;
    }

    public static Array<FileHandle> listRec(FileHandle f, Array<FileHandle> l, FilenameFilter filter) {
        if (f.exists()) {
            if (f.isDirectory()) {
                FileHandle[] partial = f.list();
                for (FileHandle fh : partial) {
                    l = listRec(fh, l, filter);
                }

            } else {
                if (filter.accept(null, f.name())) {
                    l.add(f);
                }
            }
        }

        return l;
    }

    public static boolean isNumeric(String str) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }

    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValue(Map<K, V> map) {
        List<Map.Entry<K, V>> list = new LinkedList<Map.Entry<K, V>>(map.entrySet());
        Collections.sort(list, new Comparator<Map.Entry<K, V>>() {
            public int compare(Map.Entry<K, V> o1, Map.Entry<K, V> o2) {
                return (o1.getValue()).compareTo(o2.getValue());
            }
        });

        Map<K, V> result = new LinkedHashMap<K, V>();
        for (Map.Entry<K, V> entry : list) {
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }

}
