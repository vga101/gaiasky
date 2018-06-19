package gaia.cu9.ari.gaiaorbit.util.ucd;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import gaia.cu9.ari.gaiaorbit.util.ucd.UCD.UCDType;
import gaia.cu9.ari.gaiaorbit.util.units.Position.PositionType;
import uk.ac.starlink.table.ColumnInfo;
import uk.ac.starlink.table.StarTable;

/**
 * Parses the ucds of a star table and builds some metadata on
 * the relevant quantities for gaia sky (position, proper motion, magnitudes, colors, etc.) 
 * @author tsagrista
 *
 */
public class UCDParser {

    public Map<UCDType, Set<UCD>> ucdmap;

    // IDS
    public boolean hasid = false;
    public Set<UCD> ID;

    // POSITIONS
    public boolean haspos = false;
    public Set<UCD> POS1, POS2, POS3;

    // PROPER MOTIONS
    // TODO - not supported yet

    // MAGNITUDES
    public boolean hasmag = false;
    public Set<UCD> MAG;

    // COLORS
    public boolean hascol = false;
    public Set<UCD> COL;

    // PHYSICAL PARAMS
    // TODO - not supported yet

    public UCDParser() {
        super();
        ucdmap = new HashMap<UCDType, Set<UCD>>();
        ID = new HashSet<UCD>();
        POS1 = new HashSet<UCD>();
        POS2 = new HashSet<UCD>();
        POS3 = new HashSet<UCD>();
        MAG = new HashSet<UCD>();
        COL = new HashSet<UCD>();
    }

    /**
     * Parses the given table and puts the UCD info 
     * into the ucdmap. The map and all the indices are overwritten.
     * @param ucds
     */
    public void parse(StarTable table) {
        ucdmap.clear();
        int count = table.getColumnCount();
        ColumnInfo[] colInfo = new ColumnInfo[count];
        for (int i = 0; i < count; i++) {
            // Get column
            ColumnInfo col = table.getColumnInfo(i);
            colInfo[i] = col;

            // Parse and add
            UCD ucd = new UCD(col.getUCD(), col.getName(), col.getUnitString(), i);
            addToMap(ucd);
        }

        /** ID and NAME **/
        Set<UCD> meta = ucdmap.get(UCDType.META);
        if (meta != null)
            for (UCD candidate : meta) {
                if (candidate.ucdstrings[0].equals("meta.id")) {
                    this.ID.add(candidate);
                }
            }
        if (this.ID.isEmpty()) {
            this.ID.addAll(getByColNames("hip", "id", "source_id", "tycho2_id"));
        }
        this.hasid = this.ID != null;

        /** POSITIONS **/
        Set<UCD> pos = ucdmap.get(UCDType.POS);
        if (pos != null) {
            String posrefsys = getBestRefsys(pos);
            for (UCD candidate : pos) {
                String meaning = candidate.ucd[0][1];
                String coord = candidate.ucd[0].length > 2 ? candidate.ucd[0][2] : null;

                // Filter using best reference system (posrefsys)
                if (meaning.equals(posrefsys) || meaning.equals("parallax") || meaning.equals("distance")) {
                    switch (meaning) {
                    case "eq":
                        switch (coord) {
                        case "ra":
                            this.POS1.add(candidate);
                            break;
                        case "dec":
                            this.POS2.add(candidate);
                            break;
                        }
                        break;
                    case "ecliptic":
                    case "galactic":
                        switch (coord) {
                        case "lon":
                            this.POS1.add(candidate);
                            break;
                        case "lat":
                            this.POS2.add(candidate);
                            break;
                        }
                        break;
                    case "cartesian":
                        switch (coord) {
                        case "x":
                            this.POS1.add(candidate);
                            break;
                        case "y":
                            this.POS2.add(candidate);
                            break;
                        case "z":
                            this.POS3.add(candidate);
                            break;
                        }
                        break;
                    case "parallax":
                        this.POS3.add(candidate);
                        break;
                    case "distance":
                        this.POS3.add(candidate);
                        break;
                    }
                }
            }
        }
        if (this.POS1.isEmpty() || this.POS2.isEmpty()) {
            // Try to work out from names
            this.POS1 = getByColNames("ra", "right_ascension", "rightascension", "alpha");
            if (this.POS1 != null) {
                this.POS2 = getByColNames("dec", "declination", "delta");
                this.POS3 = getByColNames("dist", "distance");
                if (this.POS3 == null) {
                    this.POS3 = getByColNames("plx", "parallax", "pllx");
                } else {
                }
            }
        }

        this.haspos = !this.POS1.isEmpty() && !this.POS2.isEmpty();

        /** PROPER MOTIONS **/
        // TODO - not supported yet

        /** MAGNITUDES **/
        Set<UCD> mag = ucdmap.get(UCDType.PHOT);
        if (mag != null)
            for (UCD candidate : mag) {
                if (candidate.ucd[0][1].equals("mag") && candidate.ucd[0].length < 3) {
                    if (candidate.ucd.length > 1) {
                        if (candidate.ucdstrings[1].equals("stat.mean") || candidate.ucdstrings[1].toLowerCase().startsWith("em.opt.")) {
                            this.MAG.add(candidate);
                        }
                    } else {
                        if (this.MAG == null)
                            this.MAG.add(candidate);
                    }
                }
            }
        if (this.MAG == null) {
            this.MAG = getByColNames("mag", "bmag", "gmag", "phot_g_mean_mag");
        }
        this.hasmag = this.MAG != null;

        /** COLORS **/
        Set<UCD> col = ucdmap.get(UCDType.PHOT);
        if (col != null)
            for (UCD candidate : col) {
                if (candidate.ucd[0][1].equals("color")) {
                    this.COL.add(candidate);
                    break;
                }
            }
        if (this.COL == null) {
            this.COL = getByColNames("b_v", "v_i", "bp_rp", "bp_g", "g_rp");
        }
        this.hascol = this.COL != null;

        /** PHYSICAL QUANTITIES **/
        // TODO - not supported yet

    }

    public PositionType getPositionType(UCD pos1, UCD pos2, UCD pos3) {
        String meaning = pos1.ucd[0][1];
        String postypestr = null, disttype = null;
        PositionType postype = null;
        switch (meaning) {
        case "eq":
            postypestr = "EQ_SPH_";
            break;
        case "ecliptic":
            postypestr = "ECL_SPH_";
            break;
        case "galactic":
            postypestr = "GAL_SPH_";
            break;
        case "cartesian":
            postype = PositionType.EQ_XYZ;
            break;
        }

        if (pos3 != null) {
        meaning = pos3.ucd[0][1];
        switch (meaning) {
        case "parallax":
            disttype = "PLX";
            break;
        case "distance":
            disttype = "DIST";
            break;
        }
        } else {
            disttype = "PLX";
        }

        if (postype == null && postypestr != null && disttype != null) {
            // Construct from postypestr and disttype
            postype = PositionType.valueOf(postypestr + disttype);
        }

        return postype;
    }

    private Set<UCD> getByColNames(String... colnames) {
        return getByColNames(new UCDType[] { UCDType.UNKNOWN, UCDType.MISSING }, colnames);
    }

    private Set<UCD> getByColNames(UCDType[] types, String... colnames) {
        Set<UCD> candidates = new HashSet<UCD>();
        for (UCDType type : types) {
            // Get all unknown and missing
            if (ucdmap.containsKey(type)) {
                Set<UCD> set = ucdmap.get(type);
                // Check column names
                for (UCD candidate : set) {
                    if (contains(colnames, candidate.colname))
                        candidates.add(candidate);
                }
            }
        }
        return candidates;
    }

    private String getBestRefsys(Set<UCD> ucds) {
        boolean eq = false, ecl = false, gal = false, cart = false;
        for (UCD candidate : ucds) {
            eq = eq || candidate.ucd[0][1].equals("eq");
            ecl = ecl || candidate.ucd[0][1].equals("ecliptic");
            gal = gal || candidate.ucd[0][1].equals("galactic");
            cart = cart || candidate.ucd[0][1].equals("cartesian");
        }
        if (eq)
            return "eq";
        else if (gal)
            return "galactic";
        else if (ecl)
            return "ecliptic";
        else if (cart)
            return "cartesian";
        return "";
    }

    private boolean contains(String[] list, String key) {
        for (String candidate : list) {
            if (candidate.equals(key))
                return true;
        }
        return false;
    }

    private void addToMap(UCD ucd) {
        if (!ucdmap.containsKey(ucd.type)) {
            Set<UCD> set = new HashSet<UCD>();
            set.add(ucd);
            ucdmap.put(ucd.type, set);
        } else {
            ucdmap.get(ucd.type).add(ucd);
        }
    }

}
