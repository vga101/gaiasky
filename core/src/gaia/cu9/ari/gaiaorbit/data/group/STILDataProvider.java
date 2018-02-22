package gaia.cu9.ari.gaiaorbit.data.group;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Array;

import gaia.cu9.ari.gaiaorbit.scenegraph.ParticleGroup.ParticleBean;
import gaia.cu9.ari.gaiaorbit.scenegraph.StarGroup.StarBean;
import gaia.cu9.ari.gaiaorbit.util.Constants;
import gaia.cu9.ari.gaiaorbit.util.I18n;
import gaia.cu9.ari.gaiaorbit.util.Logger;
import gaia.cu9.ari.gaiaorbit.util.color.ColourUtils;
import gaia.cu9.ari.gaiaorbit.util.coord.Coordinates;
import gaia.cu9.ari.gaiaorbit.util.math.Vector3d;
import gaia.cu9.ari.gaiaorbit.util.units.Position;
import gaia.cu9.ari.gaiaorbit.util.units.Position.PositionType;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.StarTable;
import uk.ac.starlink.table.StarTableFactory;
import uk.ac.starlink.table.TableSequence;
import uk.ac.starlink.util.DataSource;
import uk.ac.starlink.util.FileDataSource;

/**
 * Loads VOTables, FITS, etc.
 * @author tsagrista
 *
 */
public class STILDataProvider extends AbstractStarGroupDataProvider {
    private StarTableFactory factory;
    private long starid = 10000000;

    public STILDataProvider() {
        super();
        // Disable logging
        java.util.logging.Logger.getLogger("org.astrogrid").setLevel(Level.OFF);
        factory = new StarTableFactory();
    }

    @Override
    public Array<? extends ParticleBean> loadData(String file) {
        return loadData(file, 1.0f);
    }

    @Override
    public Array<? extends ParticleBean> loadData(String file, double factor) {
        Logger.info(this.getClass().getSimpleName(), I18n.bundle.format("notif.datafile", file));
        try {
            loadData(new FileDataSource(file), factor);
        } catch (Exception e) {
            Logger.error(e);
        }
        return list;
    }

    public Array<? extends ParticleBean> loadData(DataSource ds, double factor) {


        try {
            Map<String, ColumnInfo> ucds = new HashMap<String, ColumnInfo>();
            Map<String, Integer> ucdsi = new HashMap<String, Integer>();
            Map<String, ColumnInfo> colnames = new HashMap<String, ColumnInfo>();
            Map<String, Integer> colnamesi = new HashMap<String, Integer>();

            TableSequence ts = factory.makeStarTables(ds);
            // Find table
            List<StarTable> tables = new LinkedList<StarTable>();
            StarTable table = null;
            long maxElems = 0;
            for (StarTable t; (t = ts.nextTable()) != null;) {
                tables.add(t);
                if (t.getRowCount() > maxElems) {
                    maxElems = t.getRowCount();
                    table = t;
                }
            }
            Logger.info(this.getClass().getSimpleName(), "Selected table " + table.getName() + ": " + table.getRowCount() + " elements");

            initLists((int) table.getRowCount());

            int count = table.getColumnCount();
            ColumnInfo[] colInfo = new ColumnInfo[count];
            for (int i = 0; i < count; i++) {
                colInfo[i] = table.getColumnInfo(i);
                ucds.put(colInfo[i].getUCD(), colInfo[i]);
                ucdsi.put(colInfo[i].getUCD(), i);
                colnames.put(colInfo[i].getName(), colInfo[i]);
                colnamesi.put(colInfo[i].getName(), i);
            }

            /** POSITION **/

            ColumnInfo ac = null, bc = null, cc = null;
            int ai, bi, ci;
            PositionType type = null;
            int tychoi = -1;
            int hipi = -1;

            // Check positions
            if (ucds.containsKey("pos.eq.ra")) {
                // RA_DEC_DIST or RA_DEC_PLX
                ac = ucds.get("pos.eq.ra");
                bc = ucds.get("pos.eq.dec");
                cc = ucds.containsKey("pos.parallax") ? ucds.get("pos.parallax") : ucds.get("pos.distance");
                type = ucds.containsKey("pos.parallax") ? PositionType.RA_DEC_PLX : PositionType.RA_DEC_DIST;
            }

            if (type == null && ucds.containsKey("pos.galactic.lon")) {
                // GLON_GLAT_DIST or GLON_GLAT_PLX
                ac = ucds.get("pos.galactic.lon");
                bc = ucds.get("pos.galactic.lat");
                cc = ucds.containsKey("pos.parallax") ? ucds.get("pos.parallax") : ucds.get("pos.distance");
                type = ucds.containsKey("pos.parallax") ? PositionType.GLON_GLAT_PLX : PositionType.GLON_GLAT_DIST;
            }

            if (type == null && ucds.containsKey("pos.eq.x")) {
                // Equatorial XYZ
                ac = ucds.get("pos.eq.x");
                bc = ucds.get("pos.eq.y");
                cc = ucds.get("pos.eq.z");
                type = PositionType.XYZ_EQUATORIAL;
            }

            if (type == null && ucds.containsKey("pos.galactic.x")) {
                // Galactic XYZ
                ac = ucds.get("pos.galactic.x");
                bc = ucds.get("pos.galactic.y");
                cc = ucds.get("pos.galactic.z");
                type = PositionType.XYZ_GALACTIC;
            }

            if (type == null) {
                throw new RuntimeException("Could not find suitable position candidate columns");
            } else {
                ai = ucdsi.get(ac.getUCD());
                bi = ucdsi.get(bc.getUCD());
                ci = ucdsi.get(cc.getUCD());
            }

            /** CHECK FOR TYCHO NUMBER **/
            if (colnames.containsKey("TYC")) {
                tychoi = colnamesi.get("TYC");
            }

            /** CHECK FOR HIP NUMBER **/
            if (colnames.containsKey("HIP")) {
                hipi = colnamesi.get("HIP");
            }

            /** APP MAGNITUDE **/
            ColumnInfo magc = null;
            int magi;
            if (ucds.containsKey("phot.mag;em.opt.V")) {
                magc = ucds.get("phot.mag;em.opt.V");
            } else if (ucds.containsKey("phot.mag;em.opt.B")) {
                magc = ucds.get("phot.mag;em.opt.B");
            } else if (ucds.containsKey("phot.mag;em.opt.I")) {
                magc = ucds.get("phot.mag;em.opt.I");
            } else if (ucds.containsKey("phot.mag;em.opt.R")) {
                magc = ucds.get("phot.mag;em.opt.R");
            } else if (ucds.containsKey("phot.mag;stat.mean;em.opt")) {
                magc = ucds.get("phot.mag;stat.mean;em.opt");
            } else {
                throw new RuntimeException("Could not find suitable magnitude candidate column");
            }
            magi = ucdsi.get(magc.getUCD());

            /** ABS MAGNITUDE **/
            ColumnInfo abmagc = null;
            int abmagi = -1;
            if (ucds.containsKey("phys.magAbs;em.opt.V")) {
                abmagc = ucds.get("phys.magAbs;em.opt.V");
            } else if (ucds.containsKey("phys.magAbs;em.opt.B")) {
                abmagc = ucds.get("phys.magAbs;em.opt.B");
            } else if (ucds.containsKey("phys.magAbs;em.opt.I")) {
                abmagc = ucds.get("phys.magAbs;em.opt.I");
            } else if (ucds.containsKey("phys.magAbs;em.opt.R")) {
                abmagc = ucds.get("phys.magAbs;em.opt.R");
            }
            if (abmagc != null)
                abmagi = ucdsi.get(abmagc.getUCD());

            /** COLOR **/
            ColumnInfo colc = null;
            int coli = -1;
            if (ucds.containsKey("phot.color;em.opt.B;em.opt.V")) {
                // B-V
                colc = ucds.get("phot.color;em.opt.B;em.opt.V");
            }
            if (colc != null) {
                coli = ucdsi.get(colc.getUCD());
            }

            /** NAME **/
            ColumnInfo idstrc = null;
            ColumnInfo idc = null;
            int idstri = 0, idi = 0;
            if (ucds.containsKey("meta.id")) {
                idc = ucds.get("meta.id");
                idi = ucdsi.get("meta.id");
            }
            if (ucds.containsKey("meta.id;meta.main")) {
                idc = ucds.get("meta.id;meta.main");
                idi = ucdsi.get("meta.id;meta.main");
            }

            long rowcount = table.getRowCount();
            for (long i = 0; i < rowcount; i++) {
                Object[] row = table.getRow(i);

                String tycho = "";
                if (tychoi >= 0 && row[tychoi] != null)
                    tycho = (String) row[tychoi];

                int hip = -1;
                if (hipi >= 0 && row[hipi] != null)
                    hip = ((Number) row[hipi]).intValue();

                /** POSITION **/
                double a = ((Number) row[ai]).doubleValue();
                double b = ((Number) row[bi]).doubleValue();
                double c = ((Number) row[ci]).doubleValue();
                Position p = new Position(a, ac.getUnitString(), b, bc.getUnitString(), c, cc == null ? "" : cc.getUnitString(), type);
                double distpc = p.gsposition.len();
                p.gsposition.scl(Constants.PC_TO_U);
                // Find out RA/DEC/Dist
                Vector3d sph = new Vector3d();
                Coordinates.cartesianToSpherical(p.gsposition, sph);

                double appmag = ((Number) row[magi]).floatValue();
                double absmag;
                if (abmagi >= 0) {
                    absmag = ((Number) row[abmagi]).floatValue();
                } else {
                    absmag = (appmag - 2.5 * Math.log10(Math.pow(distpc / 10d, 2d)));
                }
                double flux = Math.pow(10, -absmag / 2.5f);
                double size = Math.min((Math.pow(flux, 0.5f) * Constants.PC_TO_U * 0.16f), 1e9f) / 1.5;

                float bv = coli > 0 ? ((Number) row[coli]).floatValue() : 0.656f;
                float[] rgb = ColourUtils.BVtoRGB(bv);
                double col = Color.toFloatBits(rgb[0], rgb[1], rgb[2], 1.0f);

                Long id = (idc == null || !idc.getContentClass().equals(Long.class)) ? ++starid : ((Number) row[idi]).longValue();

                String idstr = null;
                if (idstrc == null || !idstrc.getContentClass().isAssignableFrom(String.class)) {
                    // ID string from catalog if possible
                    if (hipi >= 0 && row[hipi] != null) {
                        idstr = "HIP" + row[hipi];
                    } else if (tychoi >= 0 && row[tychoi] != null) {
                        idstr = "TYC" + row[tychoi];
                    } else {
                        idstr = id.toString();
                    }
                } else {
                    idstr = (String) row[idstri];
                }

                double[] point = new double[StarBean.SIZE];
                point[StarBean.I_HIP] = -1;
                point[StarBean.I_TYC1] = -1;
                point[StarBean.I_TYC2] = -1;
                point[StarBean.I_TYC3] = -1;
                point[StarBean.I_X] = p.gsposition.x;
                point[StarBean.I_Y] = p.gsposition.y;
                point[StarBean.I_Z] = p.gsposition.z;
                point[StarBean.I_PMX] = 0;
                point[StarBean.I_PMY] = 0;
                point[StarBean.I_PMZ] = 0;
                point[StarBean.I_MUALPHA] = 0;
                point[StarBean.I_MUDELTA] = 0;
                point[StarBean.I_RADVEL] = 0;
                point[StarBean.I_COL] = col;
                point[StarBean.I_SIZE] = size;
                //point[StarBean.I_RADIUS] = radius;
                //point[StarBean.I_TEFF] = teff;
                point[StarBean.I_APPMAG] = appmag;
                point[StarBean.I_ABSMAG] = absmag;

                list.add(new StarBean(point, id, idstr));


            }

        } catch (Exception e) {
            Logger.error(e);
        }

        return list;
    }

    @Override
    public Array<? extends ParticleBean> loadData(InputStream is, double factor) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Array<? extends ParticleBean> loadDataMapped(String file, double factor) {
        return null;
    }

    @Override
    public void setFileNumberCap(int cap) {
    }

    @Override
    public Map<Long, float[]> getColors() {
        return null;
    }

    @Override
    public void setParallaxErrorFactor(double parallaxErrorFactor) {

    }

    @Override
    public void setParallaxZeroPoint(double parallaxZeroPoint) {
    }

    @Override
    public void setMagCorrections(boolean magCorrections) {
    }

}
