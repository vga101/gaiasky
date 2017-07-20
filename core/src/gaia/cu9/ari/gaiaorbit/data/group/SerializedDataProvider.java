package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.ObjectInputStream;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.scenegraph.StarGroup.StarBean;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;

public class SerializedDataProvider extends AbstractStarGroupDataProvider {

    public SerializedDataProvider() {
    }

    public Array<StarBean> loadData(String file) {
        return loadData(file, 1d);
    }

    @SuppressWarnings("unchecked")
    public Array<StarBean> loadData(String file, double factor) {
        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.datafile", file));

        FileHandle f = Gdx.files.internal(file);
        ObjectInputStream ois;
        try {
            ois = new ObjectInputStream(f.read());
            List<StarBean> l = (List<StarBean>) ois.readObject(); // cast is needed.
            ois.close();

            // Convert to Array, reconstruct index
            int n = l.size();
            initLists(n);

            for (int i = 0; i < n; i++) {
                StarBean point = l.get(i);
                list.add(point);
                index.put(point.name, i);
                if (point.data[StarBean.I_HIP] > 0) {
                    index.put("hip " + (int) point.data[StarBean.I_HIP], i);
                }
                if (point.data[StarBean.I_TYC1] > 0) {
                    index.put("tyc " + (int) point.data[StarBean.I_TYC1] + "-" + (int) point.data[StarBean.I_TYC2] + "-" + (int) point.data[StarBean.I_TYC3], i);
                }
                index.put(Long.toString(point.id), i);
            }

            Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", list.size, file));
            return list;
        } catch (Exception e) {
            Logger.error(e, this.getClass().getSimpleName());
        }
        return null;
    }

}
