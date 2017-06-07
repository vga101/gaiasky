package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;

/**
 * This provider loads point data in the internal reference system format, [x,
 * y, z]
 * 
 * @author tsagrista
 *
 */
public class PointDataProvider implements IParticleGroupDataProvider {

    public List<double[]> loadData(String file) {
	List<double[]> pointData = new ArrayList<double[]>();
	FileHandle f = Gdx.files.internal(file);

	try {
	    int tokenslen;
	    BufferedReader br = new BufferedReader(new InputStreamReader(f.read()));
	    String line;
	    while ((line = br.readLine()) != null) {
		if (!line.isEmpty() && !line.startsWith("#")) {
		    // Read line
		    String[] tokens = line.split("\\s+");
		    tokenslen = tokens.length;
		    double[] point = new double[tokenslen];
		    for (int j = 0; j < tokenslen; j++) {
			point[j] = Parser.parseDouble(tokens[j]);
		    }
		    pointData.add(point);
		}
	    }

	    br.close();

	    Logger.info(this.getClass().getSimpleName(),
		    I18n.bundle.format("notif.nodeloader", pointData.size(), file));
	} catch (Exception e) {
	    Logger.error(e, PointDataProvider.class.getName());
	}

	return pointData;
    }
}
