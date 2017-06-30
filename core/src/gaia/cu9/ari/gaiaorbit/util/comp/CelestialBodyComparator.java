package gaia.cu9.ari.gaiaorbit.util.comp;

import java.util.Comparator;

import gaia.cu9.ari.gaiaorbit.scenegraph.IFocus;

public class CelestialBodyComparator implements Comparator<IFocus> {

    @Override
    public int compare(IFocus a, IFocus b) {
        return b.getName().compareTo(a.getName());
    }

}
