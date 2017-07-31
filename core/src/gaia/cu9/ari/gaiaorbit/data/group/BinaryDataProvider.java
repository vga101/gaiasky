package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.scenegraph.ParticleGroup.ParticleBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.StarGroup.StarBean;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;

public class BinaryDataProvider extends AbstractStarGroupDataProvider {

    @Override
    public Array<? extends ParticleBean> loadData(String file) {
        return loadData(file, 1d);
    }

    @Override
    public Array<? extends ParticleBean> loadData(String file, double factor) {
        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.datafile", file));

        FileHandle f = Gdx.files.internal(file);
        loadData(f.read(), factor);
        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.nodeloader", list.size, file));

        return list;
    }

    @Override
    public Array<? extends ParticleBean> loadData(InputStream is, double factor) {
        list = readData(is);
        return list;
    }

    public void writeData(Array<StarBean> data, OutputStream out) {
        // Wrap the FileOutputStream with a DataOutputStream
        DataOutputStream data_out = new DataOutputStream(out);
        try {
            // Size of stars
            data_out.writeInt(data.size);
            for (StarBean sb : data) {
                writeStarBean(sb, data_out);
            }

        } catch (Exception e) {
            Logger.error(e);
        } finally {
            try {
                data_out.close();
            } catch (IOException e) {
                Logger.error(e);
            }
        }

    }

    protected void writeStarBean(StarBean sb, DataOutputStream out) throws IOException {
        int i = 0;
        for (double d : sb.data) {
            if (i >= StarBean.I_HIP && i <= StarBean.I_TYC3)
                out.writeInt((int) d);
            else
                out.writeDouble(d);

            i++;
        }
        out.writeLong(sb.id);
        out.writeInt(sb.name.length());
        out.writeChars(sb.name);
    }

    public Array<StarBean> readData(InputStream in) {
        Array<StarBean> data = null;
        DataInputStream data_in = new DataInputStream(in);

        try {
            // Read size of stars
            int size = data_in.readInt();
            data = new Array<StarBean>(size);
            for (int i = 0; i < size; i++) {
                data.add(readStarBean(data_in));
            }

        } catch (IOException e) {
            Logger.error(e);
        } finally {
            try {
                data_in.close();
            } catch (IOException e) {
                Logger.error(e);
            }
        }

        return data;
    }

    protected StarBean readStarBean(DataInputStream in) throws IOException {
        double data[] = new double[StarBean.SIZE];
        for (int i = 0; i < StarBean.SIZE; i++) {
            if (i >= StarBean.I_HIP && i <= StarBean.I_TYC3)
                data[i] = in.readInt();
            else
                data[i] = in.readDouble();
        }
        Long id = in.readLong();
        int nameLength = in.readInt();
        StringBuilder name = new StringBuilder();
        for (int i = 0; i < nameLength; i++)
            name.append(in.readChar());

        return new StarBean(data, id, name.toString());
    }

}
