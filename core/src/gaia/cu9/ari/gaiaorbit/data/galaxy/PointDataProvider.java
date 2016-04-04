package gaia.cu9.ari.gaiaorbit.data.galaxy;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.parse.Parser;

public class PointDataProvider {

    public List<float[]> loadData(String file) {
        List<float[]> pointData = new ArrayList<float[]>();
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
                    float[] point = new float[tokenslen];
                    for (int j = 0; j < tokenslen; j++) {
                        point[j] = Parser.parseFloat(tokens[j]);
                    }
                    pointData.add(point);
                }
            }

            br.close();

            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", pointData.size(), file));
        } catch (Exception e) {
            Logger.error(e, PointDataProvider.class.getName());
        }

        return pointData;
    }
}
