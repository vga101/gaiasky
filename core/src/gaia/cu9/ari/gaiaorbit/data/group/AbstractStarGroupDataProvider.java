package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.util.Logger;

public abstract class AbstractStarGroupDataProvider implements IStarGroupDataProvider {

    protected Map<String, Integer> index;
    protected List<String> names;
    protected List<Long> ids;

    public AbstractStarGroupDataProvider() {
        super();
        ids = new ArrayList<Long>(100000);
        index = new HashMap<String, Integer>();
        names = new ArrayList<String>(100000);
    }

    protected void dumpToDisk(Array<double[]> pointData, String filename) {
        List<double[]> l = new ArrayList<double[]>(pointData.size);
        for (double[] p : pointData)
            l.add(p);

        try {
            List<Object> main = new ArrayList<Object>(3);
            main.add(l);
            main.add(ids);
            main.add(index);
            main.add(names);

            ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filename));
            oos.writeObject(main);
            oos.close();

        } catch (Exception e) {
            Logger.error(e);
        }
    }

    @Override
    public Map<String, Integer> getIndex() {
        return index;
    }

    @Override
    public List<String> getNames() {
        return names;
    }

    @Override
    public List<Long> getIds() {
        return ids;
    }

}
