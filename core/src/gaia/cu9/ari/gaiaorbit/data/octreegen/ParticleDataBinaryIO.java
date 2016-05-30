package gaia.cu9.ari.gaiaorbit.data.octreegen;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.math.Vector3;

import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;
import gaia.cu9.ari.gaiaorbit.scenegraph.Particle;
import gaia.cu9.ari.gaiaorbit.scenegraph.Star;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.GlobalConf;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.coord.AstroUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;

/**
 * Loads and writes particle data to/from our own binary format. The format is
 * defined as follows
 * 
 * - 32 bits (int) with the number of stars, starNum repeat the following
 * starNum times (for each star) - 32 bits (int) - The the length of the name,
 * or nameLength - 16 bits * nameLength (chars) - The name of the star - 32 bits
 * (float) - appmag - 32 bits (float) - absmag - 32 bits (float) - colorbv - 32
 * bits (float) - x - 32 bits (float) - y - 32 bits (float) - z - 64 bits (long)
 * - id - 32 bits (int) - HIP - 32 bits (int) - TYC - 8 bits (byte) -
 * catalogSource - 64 bits (long) - pageId - 32 bits (int) - particleType
 * 
 * @author Toni Sagrista
 *
 */
public class ParticleDataBinaryIO {

    public void writeParticles(List<Particle> particles, OutputStream out) {

        try {
            // Wrap the FileOutputStream with a DataOutputStream
            DataOutputStream data_out = new DataOutputStream(out);

            // Size of stars
            data_out.writeInt(particles.size());
            for (Particle s : particles) {
                // name_length, name, appmag, absmag, colorbv, ra[deg], dec[deg], dist[u], mualpha[mas/yr], mudelta[mas/yr], radvel[km/s], id, hip, tychoLength, tycho, sourceCatalog, pageid, type
                data_out.writeInt(s.name.length());
                data_out.writeChars(s.name);
                data_out.writeFloat(s.appmag);
                data_out.writeFloat(s.absmag);
                data_out.writeFloat(s.colorbv);
                data_out.writeFloat((float) s.posSph.x);
                data_out.writeFloat((float) s.posSph.y);
                data_out.writeFloat((float) s.pos.len());
                data_out.writeFloat(s.pmSph != null ? s.pmSph.x : 0f);
                data_out.writeFloat(s.pmSph != null ? s.pmSph.y : 0f);
                data_out.writeFloat(s.pmSph != null ? s.pmSph.z : 0f);
                data_out.writeLong(s.id);
                data_out.writeInt(s instanceof Star ? ((Star) s).hip : -1);
                if (((Star) s).tycho != null && ((Star) s).tycho.length() > 0) {
                    data_out.writeInt(((Star) s).tycho.length());
                    data_out.writeChars(((Star) s).tycho);
                } else {
                    data_out.writeInt(0);
                }
                data_out.writeByte(s.catalogSource);
                data_out.writeInt((int) s.octantId);
                data_out.writeInt(s.type);
            }
            data_out.close();
            out.close();
        } catch (Exception e) {
            Logger.error(e);
        }
    }

    public List<CelestialBody> readParticles(InputStream in) throws FileNotFoundException {
        List<CelestialBody> stars = new ArrayList<CelestialBody>();
        DataInputStream data_in = new DataInputStream(in);

        try {
            // Read size of stars
            int size = data_in.readInt();

            for (int idx = 0; idx < size; idx++) {
                try {
                    // name_length, name, appmag, absmag, colorbv, ra[deg], dec[deg], dist[u], mualpha[mas/yr], mudelta[mas/yr], radvel[km/s], id, hip, tychoLength, tycho, sourceCatalog, pageid, type
                    int nameLength = data_in.readInt();
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < nameLength; i++) {
                        sb.append(data_in.readChar());
                    }
                    String name = sb.toString();
                    float appmag = data_in.readFloat();
                    float absmag = data_in.readFloat();
                    float colorbv = data_in.readFloat();
                    float ra = data_in.readFloat();
                    float dec = data_in.readFloat();
                    float dist = data_in.readFloat();
                    float mualpha = data_in.readFloat();
                    float mudelta = data_in.readFloat();
                    float radvel = data_in.readFloat();
                    long id = data_in.readLong();
                    int hip = data_in.readInt();
                    int tychoLength = data_in.readInt();
                    sb = new StringBuilder();
                    for (int i = 0; i < tychoLength; i++) {
                        sb.append(data_in.readChar());
                    }
                    String tycho = sb.toString();

                    byte source = data_in.readByte();
                    long pageId = data_in.readInt();
                    int type = data_in.readInt();
                    if (appmag < GlobalConf.data.LIMIT_MAG_LOAD) {
                        Vector3d pos = Coordinates.sphericalToCartesian(Math.toRadians(ra), Math.toRadians(dec), dist, new Vector3d());
                        Vector3 pmSph = new Vector3(mualpha, mudelta, radvel);
                        Vector3d pm = Coordinates.sphericalToCartesian(Math.toRadians(ra + mualpha * AstroUtils.MILLARCSEC_TO_DEG), Math.toRadians(dec + mudelta * AstroUtils.MILLARCSEC_TO_DEG), dist + radvel * Constants.KM_TO_U * Constants.S_TO_Y, new Vector3d());
                        pm.sub(pos);
                        Vector3 pmfloat = pm.toVector3();

                        Star s = new Star(pos, pmfloat, pmSph, appmag, absmag, colorbv, name, ra, dec, id, hip, tycho, source);
                        s.octantId = pageId;
                        s.type = type;
                        s.initialize();
                        stars.add(s);
                    }
                } catch (EOFException eof) {
                    Logger.error(eof);
                }
            }

        } catch (IOException e) {
            Logger.error(e);
        }

        return stars;
    }
}
