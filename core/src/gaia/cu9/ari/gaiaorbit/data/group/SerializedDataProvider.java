package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.scenegraph.StarGroup;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;

public class SerializedDataProvider extends AbstractStarGroupDataProvider {

    public SerializedDataProvider() {
    }

    public Array<double[]> loadData(String file) {
        return loadData(file, 1d);
    }

    @SuppressWarnings("unchecked")
    public Array<double[]> loadData(String file, double factor) {
        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.datafile", file));

        FileHandle f = Gdx.files.internal(file);
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(f.read());
            List<Object> main = (List<Object>) ois.readObject(); // cast is needed.
            ois.close();

            // Main contains pointData, index, names
            List<double[]> l = (List<double[]>) main.get(0);
            ids = (List<Long>) main.get(1);
            names = (List<String>) main.get(2);
            index = new HashMap<String, Integer>();

            // Convert to Array, reconstruct index
            int n = l.size();
            Array<double[]> pointData = new Array<double[]>(n);
            for (int i = 0; i < n; i++) {
                double[] point = l.get(i);
                pointData.add(point);
                index.put(names.get(i), i);
                if (point[StarGroup.I_HIP] > 0) {
                    index.put("hip " + (int) point[StarGroup.I_HIP], i);
                }
                if (point[StarGroup.I_TYC1] > 0) {
                    index.put("tyc " + (int) point[StarGroup.I_TYC1] + "-" + (int) point[StarGroup.I_TYC2] + "-" + (int) point[StarGroup.I_TYC3], i);
                }
                index.put(Long.toString(ids.get(i)), i);
            }

            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", pointData.size, file));
            return pointData;
        } catch (Exception e) {
            Logger.error(e, this.getClass().getSimpleName());
        }
        return null;
    }

}
