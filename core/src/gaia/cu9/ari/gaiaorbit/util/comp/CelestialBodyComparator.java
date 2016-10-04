package gaia.cu9.ari.gaiaorbit.util.comp;

import java.util.Comparator;

import gaia.cu9.ari.gaiaorbit.scenegraph.CelestialBody;

public class CelestialBodyComparator implements Comparator<CelestialBody> {

    @Override
    public int compare(CelestialBody a, CelestialBody b) {
        return b.name.compareTo(a.name);
    }

}
