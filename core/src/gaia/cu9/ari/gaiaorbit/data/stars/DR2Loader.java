package gaia.cu9.ari.gaiaorbit.data.stars;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.data.ISceneGraphLoader;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;

/**
 * Loads the DR2 catalog in CSV format
 * 
 * Source position and corresponding errors are in radians, parallax in mas and
 * propermotion in mas/yr. The colors we get from the BT and VT magnitudes in
 * the original Tycho2 catalog: B-V = 0.85 * (BT - VT)
 * 
 * @author Toni Sagrista
 *
 */
public class DR2Loader extends AbstractCatalogLoader implements ISceneGraphLoader {

    private static final String comma = ",";
    private static final String comment = "#";

    private static final String separator = comma;

    /** Whether to load the sourceId->HIP correspondences file **/
    public boolean useHIP = false;
    /** Map of Gaia sourceId to HIP id **/
    public Map<Long, Integer> sidHIPMap;

    /**
     * INDICES: source_id ra[deg] dec[deg] parallax[mas] parallax_error[mas]
     * pmra[mas/yr] pmdec[mas/yr] radial_velocity[km/s]
     * radial_velocity_error[km/s] phot_g_mean_mag[mag] phot_bp_mean_mag[mag]
     * phot_rp_mean_mag[mag] hip tycho2_id ref_epoch[julianyears]
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

    private int sidhipfound = 0;

    @Override
    public Array<Particle> loadData() throws FileNotFoundException {

	Array<Particle> stars = new Array<Particle>();
	for (String file : files) {

	    FileHandle fh = Gdx.files.absolute(file);
	    loadFile(fh, stars);

	}

	Logger.info(this.getClass().getSimpleName(), "SourceId matched to HIP in " + sidhipfound + " stars");
	Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.catalog.init", stars.size));
	return stars;
    }

    public void loadFile(FileHandle f, Array<Particle> stars) {
	if (f.isDirectory()) {
	    // Recursive
	    FileHandle[] files = f.list();
	    for (FileHandle fh : files)
		loadFile(fh, stars);
	} else if (f.name().endsWith(".csv")) {
	    // Simple case
	    InputStream data = f.read();
	    BufferedReader br = new BufferedReader(new InputStreamReader(data));

	    long i = 0;
	    try {
		String line;
		while ((line = br.readLine()) != null) {
		    if (!line.startsWith(comment)) {
			// Add star
			addStar(line, stars);
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
	    Logger.warn(this.getClass().getSimpleName(), "File loaded: " + f.path() + " - Objects: " + i);
	} else {
	    Logger.warn(this.getClass().getSimpleName(), "File skipped: " + f.path());
	}
    }

    private void addStar(String line, Array<Particle> stars) {
	String[] st = line.split(separator);

	long sourceid = Parser.parseLong(st[indices[SOURCE_ID]]);

	float appmag = new Double(Parser.parseDouble(st[indices[G_MAG]].trim())).floatValue();

	if (appmag < GlobalConf.data.LIMIT_MAG_LOAD) {

	    double ra = Parser.parseDouble(st[indices[RA]].trim());
	    double dec = Parser.parseDouble(st[indices[DEC]].trim());
	    double pllx = Parser.parseDouble(st[indices[PLLX]].trim());
	    double pllxerr = Parser.parseDouble(st[indices[PLLX_ERR]].trim());

	    double dist = (1000d / pllx) * Constants.PC_TO_U;
	    // Keep only stars with relevant parallaxes
	    if (dist >= 0 && pllx / pllxerr > 8 && pllxerr <= 1) {

		Vector3d pos = Coordinates.sphericalToCartesian(Math.toRadians(ra), Math.toRadians(dec), dist,
			new Vector3d());

		/** PROPER MOTIONS in mas/yr **/
		double mualpha = Parser.parseDouble(st[indices[MUALPHA]].trim()) * AstroUtils.MILLARCSEC_TO_DEG;
		double mudelta = Parser.parseDouble(st[indices[MUDELTA]].trim()) * AstroUtils.MILLARCSEC_TO_DEG;

		/** RADIAL VELOCITY in km/s **/
		double radvel = 0;
		if (indices[RADVEL] >= 0) {
		    radvel = Parser.parseDouble(st[indices[RADVEL]].trim());
		}

		/** PROPER MOTION VECTOR = (pos+dx) - pos **/
		Vector3d pm = Coordinates.sphericalToCartesian(Math.toRadians(ra + mualpha),
			Math.toRadians(dec + mudelta), dist, new Vector3d());
		pm.sub(pos);

		// TODO Use radial velocity if necessary to get a 3D proper
		// motion

		Vector3 pmfloat = pm.toVector3();
		Vector3 pmSph = new Vector3(Parser.parseFloat(st[indices[MUALPHA]].trim()),
			Parser.parseFloat(st[indices[MUDELTA]].trim()), 0f);

		/** COLOR, we use the tycBV map if present **/
		float colorbv = 0;
		if (indices[BP_MAG] >= 0 && indices[RP_MAG] >= 0) {
		    // Real TGAS
		    float bp = new Double(Parser.parseDouble(st[indices[BP_MAG]].trim())).floatValue();
		    float rp = new Double(Parser.parseDouble(st[indices[RP_MAG]].trim())).floatValue();
		    colorbv = bp - rp;
		} else {
		    // Use color value in BP
		    colorbv = new Double(Parser.parseDouble(st[indices[BP_MAG]].trim())).floatValue();
		}

		double distpc = 1000d / pllx;
		float absmag = (float) (appmag - 2.5 * Math.log10(Math.pow(distpc / 10d, 2d)));
		String name = Long.toString(sourceid);

		Star star = new Star(pos, pmfloat, pmSph, appmag, absmag, colorbv, name, (float) ra, (float) dec,
			sourceid, -1, null, (byte) 1);
		if (runFiltersAnd(star))
		    stars.add(star);
	    }

	}
    }

    private Map<Long, Integer> loadSourceidHipCorrespondences(String file) {
	Map<Long, Integer> result = new HashMap<Long, Integer>();

	FileHandle f = Gdx.files.internal(file);
	InputStream data = f.read();
	BufferedReader br = new BufferedReader(new InputStreamReader(data));
	try {
	    // skip first line with headers
	    br.readLine();

	    String line;
	    while ((line = br.readLine()) != null) {
		if (!line.startsWith(comment))
		    // Add correspondence
		    addCorrespondence(line, result);
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

	return result;
    }

    private void addCorrespondence(String line, Map<Long, Integer> map) {
	String[] st = line.split(comma);
	int hip = Parser.parseInt(st[2].trim());
	long sourceId = Parser.parseLong(st[3].trim());

	map.put(sourceId, hip);
    }

    private Map<String, Float> loadTYCBVColours(String file) {
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
		    addColour(line, result);
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

    private void addColour(String line, Map<String, Float> map) {
	String[] st = line.split(comma);
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
