package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;
import gaia.cu9.ari.gaiaorbit.util.units.Position;
import gaia.cu9.ari.gaiaorbit.util.units.Position.PositionType;

public class SDSSDataProvider implements IParticleGroupDataProvider {

    public List<double[]> loadData(String file) {
	return loadData(file, 1d);
    }

    public List<double[]> loadData(String file, double factor) {
	List<double[]> pointData = new ArrayList<double[]>();
	FileHandle f = Gdx.files.internal(file);

	try {
	    int tokenslen;
	    BufferedReader br = new BufferedReader(new InputStreamReader(f.read()));
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
			    pointData.add(point);
			}
		    }
		}
	    }

	    br.close();

	    Logger.info(this.getClass().getSimpleName(),
		    I18n.bundle.format("notif.nodeloader", pointData.size(), file));
	} catch (Exception e) {
	    Logger.error(e, SDSSDataProvider.class.getName());
	}

	return pointData;
    }

    /**
     * See
     * http://www.einsteins-theory-of-relativity-4engineers.com/cosmocalc.htm
     * See
     * https://www.physicsforums.com/threads/redshift-to-megaparsec-formula.561906/
     * 
     * @param OmegaM
     *            Omega matter
     * @param OmegaR
     *            Omega radiation
     * @param OmegaL
     *            Omega lambda
     * @param HubbleP
     *            Hubble constant
     * @param zo
     *            Redshift (z) of the source
     * @return The distance in megaparsecs for a given cosmology and redshift
     */
    private double redshiftToDistance(double OmegaM, double OmegaR, double OmegaL, double HubbleP, double zo) {
	// >>>>>> Latest update 2012-08-12

	// >>>>>> input

	double Ol = OmegaL * 1; // Lambda density par
	double Or = OmegaR * 1; // Radiation density par
	double Om = OmegaM * 1; // matter density par
	double Ok = 1 - Om - Or - Ol; // curvature densioty par
	double H = HubbleP * 1; // Hubble const now
	double zt = zo * 1; // redshift of object
	double at = (1 / (1 + zt)); // requested redshift value

	// >>>>>> output
	double Dp = 0; // proper distance

	// >>>>>> auxiliary
	double N = 100000;
	double a = 0;
	double a_ic = 0;
	double qa = 0;
	double pa = 1 / N; // Incremental a

	double Eset = 0;
	double Dtp = 0;

	// >>>>>> conversion 1/H to Myr
	double Tyr = 978000;

	// >>>>>> Loop from a = 0 to a = 1, stop to get at values
	while (a_ic < 1) {
	    a = a_ic + (pa / 2); // expansion factor as incremented
	    qa = Math.sqrt((Om / a) + Ok + (Or / (a * a)) + (Ol * a * a)); // time
									   // variable
									   // density
									   // function
									   // (Or
									   // input
									   // 10000
									   // times
									   // hi)

	    Dp = Dp + ((1 / qa) * pa); // proper distance then
	    a_ic = a_ic + pa; // new a
	    if ((a > at) && (Eset == 0)) {
		Dtp = Dp;
		Eset = 1;
	    }
	    ;
	}

	// >>>>>> Conversion
	Dp = Dp * (Tyr / H);
	Dtp = Dtp * (Tyr / H);

	// 1 Mly = 0.306 Mpc
	return (Math.round((Dp - Dtp) * 10000) / 10000) * 0.306391534731;

    }
}
