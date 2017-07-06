package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;

public class TGASSerializedDataProvider implements IStarGroupDataProvider {

    private Map<String, Integer> index;

    public Array<double[]> loadData(String file) {
        return loadData(file, 1d);
    }

    public Array<double[]> loadData(String file, double factor) {
        FileHandle f = Gdx.files.internal(file);
        FileHandle fi = Gdx.files.internal(file + ".index");
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(f.read());

            List<double[]> l = (List<double[]>) ois.readObject(); // cast is needed.
            ois.close();

            Array<double[]> pointData = new Array<double[]>(l.size());
            for (double[] point : l)
                pointData.add(point);

            ois = new ObjectInputStream(fi.read());

            index = (Map<String, Integer>) ois.readObject(); // cast is needed.
            ois.close();

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

}
