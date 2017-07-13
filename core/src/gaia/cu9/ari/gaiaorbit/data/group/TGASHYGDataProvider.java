package gaia.cu9.ari.gaiaorbit.data.group;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.scenegraph.StarGroup;
import gaia.cu9.ari.gaiaorbit.util.GlobalResources;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;

public class TGASHYGDataProvider extends AbstractStarGroupDataProvider {
    private static final boolean dumpToDisk = true;

    HYGDataProvider hyg;
    TGASDataProvider tgas;

    public TGASHYGDataProvider() {
        super();
        hyg = new HYGDataProvider();
        tgas = new TGASDataProvider();
    }

    @Override
    public Array<double[]> loadData(String file) {
        return loadData(file, 1d);
    }

    @Override
    public Array<double[]> loadData(String file, double factor) {
        Array<double[]> tgasdata = tgas.loadData("data/tgas_final/tgas.csv");
        Array<double[]> hygdata = hyg.loadData("data/hyg/hygxyz.bin");

        List<Long> tgasids = tgas.getIds();
        List<Long> hygids = hyg.getIds();

        Map<String, Integer> tgasindex = tgas.getIndex();
        Map<String, Integer> hygindex = hyg.getIndex();

        List<String> tgasnames = tgas.getNames();
        List<String> hygnames = hyg.getNames();

        // Merge everything, discarding hyg stars already in tgas
        // Contains removed HIP numbers
        Set<Integer> removed = new HashSet<Integer>();

        for (int i = 0; i < tgasdata.size; i++) {
            double[] curr = tgasdata.get(i);
            int hip = (int) curr[StarGroup.I_HIP];
            if (hip > 0 && hygindex.containsKey("HIP " + hip)) {
                removed.add((int) curr[StarGroup.I_HIP]);
            }
        }

        // Add from hip to tgas
        Map<Integer, List<String>> hygindexinv = GlobalResources.invertMap(hygindex);

        for (int i = 0; i < hygdata.size; i++) {
            double[] curr = hygdata.get(i);
            Integer hip = (int) curr[StarGroup.I_HIP];
            if (!removed.contains(hip)) {
                int newidx = tgasdata.size;
                // Add to tgasdata
                tgasdata.add(curr);

                // Add to tgasids
                tgasids.add(hygids.get(i));

                // Add to tgasnames
                tgasnames.add(hygnames.get(i));

                // Find out index and also add names
                List<String> names = hygindexinv.get(i);
                for (String n : names)
                    tgasindex.put(n, newidx);
            } else {
                // Use proper name
                if (tgasindex.containsKey("HIP " + i)) {
                    int oldidx = tgasindex.get("HIP " + i);
                    tgasnames.set(oldidx, hygnames.get(i));
                }
            }
        }

        pointData = tgasdata;
        ids = tgasids;
        index = tgasindex;
        names = tgasnames;

        if (dumpToDisk) {
            dumpToDisk(pointData, "/tmp/tgashyg.bin");
        }

        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", pointData.size, file));

        return pointData;
    }

}
