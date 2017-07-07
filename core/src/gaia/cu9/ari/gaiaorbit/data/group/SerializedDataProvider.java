package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

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
        FileHandle f = Gdx.files.internal(file);
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(f.read());
            List<Object> main = (List<Object>) ois.readObject(); // cast is needed.
            ois.close();

            // Main contains pointData, index, names
            List<double[]> l = (List<double[]>) main.get(0);
            index = (Map<String, Integer>) main.get(1);
            names = (List<String>) main.get(2);

            Array<double[]> pointData = new Array<double[]>(l.size());
            for (double[] point : l)
                pointData.add(point);

            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", pointData.size, file));
            return pointData;
        } catch (Exception e) {
            Logger.error(e, this.getClass().getSimpleName());
        }
        return null;
    }

    @Override
    public Map<String, Integer> getIndex() {
        return index;
    }

    @Override
    public List<String> getNames() {
        return names;
    }

}
